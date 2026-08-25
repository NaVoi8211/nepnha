package com.nepnha.data.repository

import com.nepnha.data.db.MemberDao
import com.nepnha.data.db.MemberEntity
import com.nepnha.data.prefs.SettingsRepository
import com.nepnha.domain.model.FamilyMember
import com.nepnha.domain.model.FamilyMemberDraft
import com.nepnha.domain.model.Gender
import com.nepnha.domain.model.LunarBirthDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/**
 * Ranh giới giữa UI và Room cho thành viên.
 *
 * Repository này cũng giữ một quy tắc nghiệp vụ nhỏ nhưng quan trọng: **xoá thành
 * viên đang là tín chủ thì tín chủ trở về "chưa chọn"**, tuyệt đối không tự đẩy
 * người khác vào vị trí đó. Đặt ở đây vì nó phải đúng dù lệnh xoá đến từ màn hình
 * nào.
 */
class MemberRepository(
    private val dao: MemberDao,
    private val settings: SettingsRepository,
    private val now: () -> Long = System::currentTimeMillis,
) {

    fun observeMembers(familyId: Long): Flow<List<FamilyMember>> =
        dao.observeByFamily(familyId).map { list -> list.map { it.toDomain() } }

    suspend fun getMember(id: Long): FamilyMember? = dao.getById(id)?.toDomain()

    /** Thêm mới. Trả về id vừa tạo. */
    suspend fun add(familyId: Long, draft: FamilyMemberDraft): Long {
        val timestamp = now()
        return dao.insert(draft.toEntity(id = 0, familyId = familyId, createdAt = timestamp, updatedAt = timestamp))
    }

    /** Sửa. Giữ nguyên `createdAt` của bản ghi cũ. */
    suspend fun update(id: Long, draft: FamilyMemberDraft) {
        val existing = dao.getById(id) ?: return
        dao.update(
            draft.toEntity(
                id = existing.id,
                familyId = existing.familyId,
                createdAt = existing.createdAt,
                updatedAt = now(),
            ),
        )
    }

    suspend fun delete(id: Long) {
        dao.deleteById(id)
        if (settings.primaryMemberId.first() == id) {
            settings.setPrimaryMemberId(null)
        }
    }
}

private fun MemberEntity.toDomain(): FamilyMember = FamilyMember(
    id = id,
    familyId = familyId,
    fullName = fullName,
    gender = Gender.fromStorage(gender),
    solarBirthDate = solarBirthDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
    lunarBirthDate = if (lunarBirthDay != null && lunarBirthMonth != null && lunarBirthYear != null) {
        LunarBirthDate(
            day = lunarBirthDay,
            month = lunarBirthMonth,
            year = lunarBirthYear,
            isLeapMonth = lunarBirthIsLeapMonth,
            source = LunarBirthDate.Source.USER_PROVIDED,
        )
    } else {
        null
    },
    role = role,
    note = note,
)

private fun FamilyMemberDraft.toEntity(
    id: Long,
    familyId: Long,
    createdAt: Long,
    updatedAt: Long,
): MemberEntity = MemberEntity(
    id = id,
    familyId = familyId,
    fullName = fullName,
    gender = gender.name,
    solarBirthDate = solarBirthDate?.toString(),
    lunarBirthDay = lunarBirthDate?.day,
    lunarBirthMonth = lunarBirthDate?.month,
    lunarBirthYear = lunarBirthDate?.year,
    lunarBirthIsLeapMonth = lunarBirthDate?.isLeapMonth == true,
    lunarBirthSource = lunarBirthDate?.source?.name,
    role = role,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
