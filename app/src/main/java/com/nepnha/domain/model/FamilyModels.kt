package com.nepnha.domain.model

import java.time.LocalDate

/** Một gia đình. MVP chỉ dùng một, nhưng schema không chặn nhiều. */
data class Family(
    val id: Long,
    val name: String,
)

/**
 * Giới tính. Cố ý KHÔNG bắt buộc: MVP chưa có bài khấn nào phân biệt giới tính,
 * nên ép người dùng chọn chỉ tạo thêm thao tác thừa.
 */
enum class Gender {
    MALE, FEMALE, UNSPECIFIED;

    companion object {
        fun fromStorage(raw: String?): Gender =
            entries.firstOrNull { it.name == raw } ?: UNSPECIFIED
    }
}

/**
 * Ngày sinh âm lịch **do người dùng tự cung cấp**.
 *
 * [source] tồn tại vì Phase 3 sẽ có thêm ngày âm do engine quy đổi ra. Hai loại đó
 * không được lẫn: một cái là điều gia đình nói, một cái là điều máy tính. Ở Phase 2
 * chỉ có thể là [Source.USER_PROVIDED] — chưa có engine, và **không được đoán**.
 */
data class LunarBirthDate(
    val day: Int,
    val month: Int,
    val year: Int,
    val isLeapMonth: Boolean,
    val source: Source = Source.USER_PROVIDED,
) {
    enum class Source { USER_PROVIDED }
}

/** Một thành viên trong gia đình. Đây KHÔNG phải danh bạ: không avatar/điện thoại/địa chỉ. */
data class FamilyMember(
    val id: Long,
    val familyId: Long,
    val fullName: String,
    val gender: Gender,
    val solarBirthDate: LocalDate?,
    val lunarBirthDate: LunarBirthDate?,
    val role: String?,
    val note: String?,
)

/** Dữ liệu đã được kiểm tra hợp lệ, sẵn sàng ghi xuống Room. */
data class FamilyMemberDraft(
    val fullName: String,
    val gender: Gender,
    val solarBirthDate: LocalDate?,
    val lunarBirthDate: LunarBirthDate?,
    val role: String?,
    val note: String?,
)
