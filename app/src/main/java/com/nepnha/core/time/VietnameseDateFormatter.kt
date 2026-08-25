package com.nepnha.core.time

import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

/**
 * Định dạng ngày **dương lịch** theo cách người Việt đọc.
 *
 * Kotlin thuần, không `Context`, không phụ thuộc `Locale` của máy — tên thứ được
 * ánh xạ tường minh thay vì nhờ ICU, để kết quả giống hệt nhau trên JVM (unit test)
 * và trên máy Android, không đổi khi người dùng đổi ngôn ngữ hệ thống.
 *
 * Tên thứ nằm ở đây chứ không ở `strings.xml` vì đây là **dữ liệu của hàm thuần**,
 * cần test được mà không cần Android. Mọi câu chữ giao diện khác vẫn ở `strings.xml`.
 *
 * Lịch ÂM không thuộc file này — xem `docs/LUNAR_CALENDAR.md` (Phase 3).
 */
object VietnameseDateFormatter {

    /** "Thứ Hai" … "Chủ Nhật". */
    fun dayOfWeek(date: LocalDate): String = when (date.dayOfWeek) {
        DayOfWeek.MONDAY -> "Thứ Hai"
        DayOfWeek.TUESDAY -> "Thứ Ba"
        DayOfWeek.WEDNESDAY -> "Thứ Tư"
        DayOfWeek.THURSDAY -> "Thứ Năm"
        DayOfWeek.FRIDAY -> "Thứ Sáu"
        DayOfWeek.SATURDAY -> "Thứ Bảy"
        DayOfWeek.SUNDAY -> "Chủ Nhật"
    }

    /** "24 tháng 8, 2026" — dạng đầy đủ, dễ đọc cho người lớn tuổi. */
    fun fullDate(date: LocalDate): String =
        "${date.dayOfMonth} tháng ${date.monthValue}, ${date.year}"

    /**
     * "24/08/2026" — dạng gọn, dùng trong danh sách.
     *
     * `Locale.ROOT` là bắt buộc: `format` không có locale sẽ dùng locale mặc định,
     * và trên máy đặt ngôn ngữ như tiếng Ả Rập nó cho ra chữ số Đông Ả Rập
     * (٠١/٠٩/٢٠٢٦). Unit test đã bắt đúng lỗi này.
     */
    fun shortDate(date: LocalDate): String =
        "%02d/%02d/%d".format(Locale.ROOT, date.dayOfMonth, date.monthValue, date.year)

    /** "Tháng 8, 2026" — tiêu đề tháng ở màn hình Lịch. */
    fun monthTitle(date: LocalDate): String =
        "Tháng ${date.monthValue}, ${date.year}"
}
