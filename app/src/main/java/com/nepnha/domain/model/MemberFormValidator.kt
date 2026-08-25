package com.nepnha.domain.model

import java.time.LocalDate

/**
 * Kiểm tra dữ liệu biểu mẫu thành viên.
 *
 * Kotlin thuần, không Android ⇒ test được trên JVM. Đặt ở `domain` vì đây là **quy
 * tắc nghiệp vụ**, không phải chuyện trình bày: chỉ họ tên là bắt buộc, mọi thứ
 * khác để trống được, và "chưa biết" phải khác "ngày 1 tháng 1".
 */
object MemberFormValidator {

    /** Giới hạn năm hợp lệ — đủ rộng cho các cụ và đủ chặt để bắt lỗi gõ nhầm. */
    private val YEAR_RANGE = 1900..2100

    fun validate(input: MemberFormInput): MemberFormResult {
        val errors = mutableSetOf<MemberFormError>()

        val name = input.fullName.trim()
        if (name.isEmpty()) errors += MemberFormError.NAME_REQUIRED

        // --- Ngày sinh dương ---
        var solar: LocalDate? = null
        val solarFields = listOf(input.solarDay, input.solarMonth, input.solarYear).map { it.trim() }
        when {
            solarFields.all { it.isEmpty() } -> Unit // để trống là hợp lệ: "chưa biết"
            solarFields.any { it.isEmpty() } -> errors += MemberFormError.SOLAR_DATE_INCOMPLETE
            else -> {
                val d = solarFields[0].toIntOrNull()
                val m = solarFields[1].toIntOrNull()
                val y = solarFields[2].toIntOrNull()
                solar = if (d == null || m == null || y == null || y !in YEAR_RANGE) {
                    null
                } else {
                    // LocalDate.of kiểm tra ngày có thật (bắt được 31/02, năm nhuận…).
                    runCatching { LocalDate.of(y, m, d) }.getOrNull()
                }
                if (solar == null) errors += MemberFormError.SOLAR_DATE_INVALID
            }
        }

        // --- Ngày sinh âm ---
        var lunar: LunarBirthDate? = null
        val lunarFields = listOf(input.lunarDay, input.lunarMonth, input.lunarYear).map { it.trim() }
        when {
            lunarFields.all { it.isEmpty() } -> Unit
            lunarFields.any { it.isEmpty() } -> errors += MemberFormError.LUNAR_DATE_INCOMPLETE
            else -> {
                val d = lunarFields[0].toIntOrNull()
                val m = lunarFields[1].toIntOrNull()
                val y = lunarFields[2].toIntOrNull()
                // CHỈ kiểm tra khoảng giá trị. Muốn biết "30 tháng 8 âm năm đó có
                // thật không" thì phải có lịch âm Việt Nam — Phase 3. Không đoán.
                lunar = if (d != null && m != null && y != null &&
                    d in 1..30 && m in 1..12 && y in YEAR_RANGE
                ) {
                    LunarBirthDate(
                        day = d,
                        month = m,
                        year = y,
                        isLeapMonth = input.lunarIsLeapMonth,
                        source = LunarBirthDate.Source.USER_PROVIDED,
                    )
                } else {
                    null
                }
                if (lunar == null) errors += MemberFormError.LUNAR_DATE_INVALID
            }
        }

        return if (errors.isEmpty()) {
            MemberFormResult.Valid(
                FamilyMemberDraft(
                    fullName = name,
                    gender = input.gender,
                    solarBirthDate = solar,
                    lunarBirthDate = lunar,
                    role = input.role.trim().takeIf { it.isNotEmpty() },
                    note = input.note.trim().takeIf { it.isNotEmpty() },
                ),
            )
        } else {
            MemberFormResult.Invalid(errors)
        }
    }
}

/** Dữ liệu thô từ biểu mẫu — chuỗi, vì người dùng gõ chuỗi. */
data class MemberFormInput(
    val fullName: String = "",
    val gender: Gender = Gender.UNSPECIFIED,
    val solarDay: String = "",
    val solarMonth: String = "",
    val solarYear: String = "",
    val lunarDay: String = "",
    val lunarMonth: String = "",
    val lunarYear: String = "",
    val lunarIsLeapMonth: Boolean = false,
    val role: String = "",
    val note: String = "",
)

enum class MemberFormError {
    NAME_REQUIRED,
    SOLAR_DATE_INCOMPLETE,
    SOLAR_DATE_INVALID,
    LUNAR_DATE_INCOMPLETE,
    LUNAR_DATE_INVALID,
}

sealed interface MemberFormResult {
    data class Valid(val draft: FamilyMemberDraft) : MemberFormResult
    data class Invalid(val errors: Set<MemberFormError>) : MemberFormResult
}
