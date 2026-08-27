package com.nepnha.domain.backup

import com.nepnha.domain.event.LeapMonthPolicy
import com.nepnha.domain.event.MissingDayPolicy
import com.nepnha.domain.model.Gender

/**
 * Định dạng file sao lưu — hợp đồng ở `docs/PHASE_7_EXPORT_IMPORT.md`.
 *
 * Cố ý **không** phái sinh từ schema Room và **không** dùng `enum.name`.
 *
 * Bài học Phase 6: R8 đổi tên hằng enum, và dữ liệu ghi bởi bản build trước thành
 * không đọc được ở bản sau. Một định dạng nằm trên đĩa của người dùng còn phải sống
 * lâu hơn thế nhiều — nên mọi giá trị enum ở đây là **hằng chuỗi tường minh**, cố định
 * vĩnh viễn, không bao giờ được đổi theo tên ký hiệu trong mã.
 */
object BackupFormat {

    /** Phiên bản định dạng cao nhất app này hiểu được. */
    const val SUPPORTED_VERSION = 1

    const val MAX_NAME_LENGTH = 200
    const val MAX_TEXT_LENGTH = 1000

    /** Năm âm chấp nhận được cho ngày sinh — cùng khoảng với biểu mẫu thành viên. */
    val LUNAR_YEAR_RANGE = 1900..2100

    private val genderWire = mapOf(
        Gender.MALE to "male",
        Gender.FEMALE to "female",
        Gender.UNSPECIFIED to "unspecified",
    )

    private val leapWire = mapOf(
        LeapMonthPolicy.COMMON_MONTH_DEFAULT to "common_month",
        LeapMonthPolicy.LEAP_MONTH_PREFERRED to "leap_month_preferred",
        LeapMonthPolicy.LEAP_MONTH_ONLY to "leap_month_only",
    )

    private val missingWire = mapOf(
        MissingDayPolicy.LAST_VALID_DAY_OF_MONTH to "last_valid_day",
        MissingDayPolicy.SKIP to "skip",
    )

    fun wire(value: Gender): String = genderWire.getValue(value)
    fun wire(value: LeapMonthPolicy): String = leapWire.getValue(value)
    fun wire(value: MissingDayPolicy): String = missingWire.getValue(value)

    fun gender(wire: String): Gender? = genderWire.entries.firstOrNull { it.value == wire }?.key
    fun leapPolicy(wire: String): LeapMonthPolicy? =
        leapWire.entries.firstOrNull { it.value == wire }?.key
    fun missingPolicy(wire: String): MissingDayPolicy? =
        missingWire.entries.firstOrNull { it.value == wire }?.key

    /** Tên hằng hợp lệ, để thông báo lỗi nói được "phải là một trong…". */
    val genderValues: List<String> get() = genderWire.values.toList()
    val leapPolicyValues: List<String> get() = leapWire.values.toList()
    val missingPolicyValues: List<String> get() = missingWire.values.toList()
}

/**
 * Nội dung một bản sao lưu, đã tách khỏi mọi chuyện lưu trữ.
 *
 * [BackupMember.ref] chỉ có nghĩa **bên trong một file**: nó tồn tại để nối ngày giỗ
 * với thành viên, không phải id của bất kỳ database nào.
 */
data class BackupData(
    val familyName: String?,
    val primaryMemberRef: Int?,
    val members: List<BackupMember>,
    val memorials: List<BackupMemorial>,
)

data class BackupMember(
    val ref: Int,
    val fullName: String,
    val gender: Gender,
    val solarBirthDate: String?,
    val lunarBirthDate: BackupLunarBirth?,
    val role: String?,
    val note: String?,
)

data class BackupLunarBirth(
    val day: Int,
    val month: Int,
    val year: Int,
    val leapMonth: Boolean,
)

data class BackupMemorial(
    val name: String,
    val memberRef: Int?,
    val lunarDay: Int,
    val lunarMonth: Int,
    val leapMonthPolicy: LeapMonthPolicy,
    val missingDayPolicy: MissingDayPolicy,
    val note: String?,
)

/** Phần bao ngoài: siêu dữ liệu + nội dung. */
data class BackupFile(
    val formatVersion: Int,
    val exportedAt: String,
    val appVersionName: String?,
    val data: BackupData,
)
