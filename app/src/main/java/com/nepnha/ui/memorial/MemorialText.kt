package com.nepnha.ui.memorial

import com.nepnha.domain.event.LeapMonthPolicy

/**
 * Cách đọc **ngày âm người dùng khai báo** cho một ngày giỗ.
 *
 * Khác với `VietnameseLunarFormatter` — file kia định dạng một ngày âm **cụ thể của
 * một năm**, còn ở đây là lời khai lặp lại hằng năm, nên tính nhuận đến từ *policy*
 * chứ không từ một `LunarDate`.
 *
 * Kotlin thuần để test được trên JVM.
 */
fun lunarDeclaration(day: Int, month: Int, policy: LeapMonthPolicy): String {
    val base = "$day tháng $month"
    return when (policy) {
        LeapMonthPolicy.COMMON_MONTH_DEFAULT -> base
        LeapMonthPolicy.LEAP_MONTH_PREFERRED -> "$base nhuận"
        // Nói rõ "chỉ" để người dùng nhớ mình đã chọn phương án bỏ qua năm không nhuận.
        LeapMonthPolicy.LEAP_MONTH_ONLY -> "$base nhuận (chỉ năm nhuận)"
    }
}
