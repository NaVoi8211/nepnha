package com.nepnha.domain.model

import com.nepnha.domain.event.LeapMonthPolicy
import com.nepnha.domain.event.MemorialRule
import com.nepnha.domain.event.MissingDayPolicy

/**
 * Kiểm tra dữ liệu biểu mẫu ngày giỗ.
 *
 * Kotlin thuần, không Android ⇒ test được trên JVM. Đặt ở `domain` vì đây là quy tắc
 * nghiệp vụ: tên và ngày âm là bắt buộc, còn **ngày 30 luôn được chấp nhận** kể cả
 * khi nhiều năm tháng đó chỉ có 29 ngày — việc xử lý năm thiếu là của
 * `MemorialDateResolver`, không phải của biểu mẫu.
 */
object MemorialFormValidator {

    fun validate(input: MemorialFormInput): MemorialFormResult {
        val errors = mutableSetOf<MemorialFormError>()

        val name = input.name.trim()
        if (name.isEmpty()) errors += MemorialFormError.NAME_REQUIRED

        val day = input.lunarDay.trim().toIntOrNull()
        val month = input.lunarMonth.trim().toIntOrNull()
        // 1..30: ngày âm không bao giờ quá 30. Không kiểm "năm nay tháng này có 30
        // ngày không" — ngày giỗ lặp hằng năm, và năm nào cũng khác.
        if (day == null || day !in 1..30) errors += MemorialFormError.DAY_INVALID
        if (month == null || month !in 1..12) errors += MemorialFormError.MONTH_INVALID

        if (errors.isNotEmpty()) return MemorialFormResult.Invalid(errors)

        return MemorialFormResult.Valid(
            MemorialDraft(
                name = name,
                memberId = input.memberId,
                lunarDay = day!!,
                lunarMonth = month!!,
                rule = MemorialRule(
                    leapMonthPolicy = input.leapMonthPolicy,
                    missingDayPolicy = input.missingDayPolicy,
                ),
                note = input.note.trim().takeIf { it.isNotEmpty() },
            ),
        )
    }
}

/** Nội dung thô của biểu mẫu — chuỗi, đúng như người dùng gõ. */
data class MemorialFormInput(
    val name: String = "",
    /** Thành viên được chọn. `null` = nhập tên tự do. */
    val memberId: Long? = null,
    val lunarDay: String = "",
    val lunarMonth: String = "",
    val leapMonthPolicy: LeapMonthPolicy = LeapMonthPolicy.COMMON_MONTH_DEFAULT,
    val missingDayPolicy: MissingDayPolicy = MissingDayPolicy.LAST_VALID_DAY_OF_MONTH,
    val note: String = "",
)

sealed interface MemorialFormResult {
    data class Valid(val draft: MemorialDraft) : MemorialFormResult
    data class Invalid(val errors: Set<MemorialFormError>) : MemorialFormResult
}

enum class MemorialFormError { NAME_REQUIRED, DAY_INVALID, MONTH_INVALID }
