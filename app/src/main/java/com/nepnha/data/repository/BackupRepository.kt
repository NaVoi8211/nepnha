package com.nepnha.data.repository

import androidx.room.withTransaction
import com.nepnha.data.db.MemberEntity
import com.nepnha.data.db.MemorialEntity
import com.nepnha.data.db.NepNhaDatabase
import com.nepnha.data.prefs.SettingsRepository
import com.nepnha.domain.backup.BackupData
import com.nepnha.domain.backup.BackupFormat
import com.nepnha.domain.backup.BackupLunarBirth
import com.nepnha.domain.backup.BackupMember
import com.nepnha.domain.backup.BackupMemorial
import com.nepnha.domain.model.Gender
import com.nepnha.domain.model.LunarBirthDate
import kotlinx.coroutines.flow.first

/**
 * Đọc toàn bộ dữ liệu người dùng ra [BackupData], và ghi một [BackupData] vào máy.
 *
 * Hai bảo đảm quan trọng nhất:
 *
 * 1. **Nhập là nguyên tử.** Toàn bộ nằm trong một `withTransaction`; bất kỳ lỗi nào
 *    cũng làm rollback sạch và database không đổi một dòng.
 * 2. **Nhập chỉ THÊM.** Không xoá, không ghi đè, không gộp theo tên. Hai người trùng
 *    tên là hai người — máy không có cách nào biết khác đi, và đoán sai ở đây là trộn
 *    lẫn hai người trong gia phả.
 */
class BackupRepository(
    private val database: NepNhaDatabase,
    private val familyRepository: FamilyRepository,
    private val settings: SettingsRepository,
    private val now: () -> Long = System::currentTimeMillis,
) {

    /** Đọc dữ liệu hiện có. Không chạm gì vào database. */
    suspend fun readAll(): BackupData {
        val family = familyRepository.observeFamily().first()
        if (family == null) {
            return BackupData(familyName = null, primaryMemberRef = null, members = emptyList(), memorials = emptyList())
        }
        val members = database.memberDao().observeByFamily(family.id).first()
        val memorials = database.memorialDao().observeByFamily(family.id).first()
        val primaryId = settings.primaryMemberId.first()

        // `ref` chỉ có nghĩa trong chính file này. Dùng chỉ số 1..n thay vì id của Room
        // để định dạng không rò rỉ chi tiết lưu trữ ra ngoài.
        val refOf = members.mapIndexed { index, m -> m.id to (index + 1) }.toMap()

        return BackupData(
            familyName = family.name,
            primaryMemberRef = primaryId?.let { refOf[it] },
            members = members.map { m ->
                BackupMember(
                    ref = refOf.getValue(m.id),
                    fullName = m.fullName,
                    gender = Gender.fromStorage(m.gender),
                    solarBirthDate = m.solarBirthDate,
                    lunarBirthDate = if (m.lunarBirthDay != null && m.lunarBirthMonth != null &&
                        m.lunarBirthYear != null
                    ) {
                        BackupLunarBirth(
                            m.lunarBirthDay, m.lunarBirthMonth, m.lunarBirthYear, m.lunarBirthIsLeapMonth,
                        )
                    } else {
                        null
                    },
                    role = m.role,
                    note = m.note,
                )
            },
            memorials = memorials.map { x ->
                BackupMemorial(
                    name = x.name,
                    memberRef = x.memberId?.let { refOf[it] },
                    lunarDay = x.lunarDay,
                    lunarMonth = x.lunarMonth,
                    leapMonthPolicy = BackupFormat.leapPolicy(wireLeap(x.leapMonthPolicy))
                        ?: com.nepnha.domain.event.LeapMonthPolicy.COMMON_MONTH_DEFAULT,
                    missingDayPolicy = BackupFormat.missingPolicy(wireMissing(x.missingDayPolicy))
                        ?: com.nepnha.domain.event.MissingDayPolicy.LAST_VALID_DAY_OF_MONTH,
                    note = x.note,
                )
            },
        )
    }

    /**
     * Thêm toàn bộ [data] vào dữ liệu hiện có, trong **một** giao dịch.
     *
     * Trả về số bản ghi đã thêm. Ném thì database đã rollback — không có nhập một phần.
     */
    suspend fun importAdditive(data: BackupData): ImportOutcome = database.withTransaction {
        val familyName = data.familyName?.takeIf { it.isNotBlank() } ?: "Gia đình tôi"
        familyRepository.ensureDefaultFamily(familyName)
        val familyId = database.familyDao().firstId() ?: error("không tạo được gia đình")

        val timestamp = now()
        // Ánh xạ ref trong file → id mới do Room sinh. Thành viên phải vào trước để
        // ngày giỗ có id thật mà trỏ tới.
        val idOfRef = mutableMapOf<Int, Long>()
        for (m in data.members) {
            val newId = database.memberDao().insert(
                MemberEntity(
                    familyId = familyId,
                    fullName = m.fullName,
                    gender = m.gender.name,
                    solarBirthDate = m.solarBirthDate,
                    lunarBirthDay = m.lunarBirthDate?.day,
                    lunarBirthMonth = m.lunarBirthDate?.month,
                    lunarBirthYear = m.lunarBirthDate?.year,
                    lunarBirthIsLeapMonth = m.lunarBirthDate?.leapMonth == true,
                    lunarBirthSource = m.lunarBirthDate?.let { LunarBirthDate.Source.USER_PROVIDED.name },
                    role = m.role,
                    note = m.note,
                    createdAt = timestamp,
                    updatedAt = timestamp,
                ),
            )
            idOfRef[m.ref] = newId
        }

        for (x in data.memorials) {
            // `memberRef` đã được kiểm là trỏ tới ref có thật; nếu vẫn thiếu thì đó là
            // lỗi lập trình, và ném ở đây sẽ rollback cả giao dịch — đúng điều ta muốn.
            val memberId = x.memberRef?.let {
                idOfRef[it] ?: error("memberRef $it không có trong bảng ánh xạ")
            }
            database.memorialDao().insert(
                MemorialEntity(
                    familyId = familyId,
                    name = x.name,
                    memberId = memberId,
                    lunarDay = x.lunarDay,
                    lunarMonth = x.lunarMonth,
                    leapMonthPolicy = x.leapMonthPolicy.name,
                    missingDayPolicy = x.missingDayPolicy.name,
                    note = x.note,
                    createdAt = timestamp,
                    updatedAt = timestamp,
                ),
            )
        }

        // Tín chủ CHỈ được đặt khi máy chưa chọn ai. Nhập dữ liệu không được âm thầm
        // đổi một lựa chọn mà người dùng đã tự tay làm.
        var primaryApplied = false
        val currentPrimary = settings.primaryMemberId.first()
        if (currentPrimary == null && data.primaryMemberRef != null) {
            idOfRef[data.primaryMemberRef]?.let {
                settings.setPrimaryMemberId(it)
                primaryApplied = true
            }
        }

        ImportOutcome(
            membersAdded = data.members.size,
            memorialsAdded = data.memorials.size,
            primaryMemberApplied = primaryApplied,
        )
    }

    private fun wireLeap(stored: String): String = BackupFormat.wire(
        com.nepnha.domain.event.LeapMonthPolicy.entries.firstOrNull { it.name == stored }
            ?: com.nepnha.domain.event.LeapMonthPolicy.COMMON_MONTH_DEFAULT,
    )

    private fun wireMissing(stored: String): String = BackupFormat.wire(
        com.nepnha.domain.event.MissingDayPolicy.entries.firstOrNull { it.name == stored }
            ?: com.nepnha.domain.event.MissingDayPolicy.LAST_VALID_DAY_OF_MONTH,
    )
}

/** Kết quả một lần nhập đã commit. */
data class ImportOutcome(
    val membersAdded: Int,
    val memorialsAdded: Int,
    /** Tín chủ trong file có được áp dụng không — chỉ khi máy trước đó chưa chọn ai. */
    val primaryMemberApplied: Boolean,
)
