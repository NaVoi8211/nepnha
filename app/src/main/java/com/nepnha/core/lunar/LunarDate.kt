package com.nepnha.core.lunar

/**
 * Ngày âm lịch Việt Nam.
 *
 * `isLeapMonth` là **thuộc tính của chính ngày**, không phải tham số rời rạc truyền
 * vào các hàm chuyển đổi. Nhờ vậy không thể lỡ tay bỏ quên nó ở một lời gọi nào đó.
 *
 * Quy ước đánh số tháng nhuận: **tháng nhuận mang số của tháng liền trước**. Nhuận
 * tháng 4 là lần xuất hiện thứ hai của tháng 4, không phải "tháng 5". Đây từng là
 * một lỗi thật trong mô hình tham chiếu, chỉ bị phát hiện khi đối chiếu với bảng lịch
 * của Đài Thiên văn Hồng Kông — xem `docs/LUNAR_ONLINE_ANOMALIES.md`.
 */
data class LunarDate(
    val day: Int,
    val month: Int,
    val year: Int,
    val isLeapMonth: Boolean = false,
) {
    override fun toString(): String =
        "$day/$month${if (isLeapMonth) "N" else ""}/$year"
}

/** Can Chi của năm âm, ví dụ Bính Ngọ. Trả về kiểu có cấu trúc để caller khỏi phải tách chuỗi. */
data class SexagenaryYear(val can: String, val chi: String) {
    override fun toString(): String = "$can $chi"
}

/**
 * Kết quả hỏi "năm âm này có tháng nhuận không".
 *
 * Dùng kiểu riêng thay vì `Int?` vì `null` sẽ lẫn lộn hai chuyện khác hẳn nhau:
 * "năm thường" (một câu trả lời thành công) và "không tính được" (một lỗi).
 */
sealed interface LeapMonthInfo {
    data object None : LeapMonthInfo
    data class Month(val month: Int) : LeapMonthInfo
}

/**
 * Bối cảnh lịch — quyết định lịch âm được quy chiếu theo múi giờ nào.
 *
 * Cố ý **không** cho caller truyền `TimeZone`: múi giờ ở đây là một sự thật lịch sử
 * của lịch Việt Nam, không phải tuỳ chọn kỹ thuật.
 *
 * MVP chỉ có [OfficialVietnam]. Phân biệt Bắc/Nam giai đoạn 1954–1975 **chưa** được
 * hiện thực vì chưa đủ bằng chứng về múi giờ dùng để **tính lịch** thời kỳ đó — xem
 * `docs/HISTORICAL_TIME_MODEL.md`. Không tạo API hứa suông cho tính năng chưa có.
 */
sealed interface CalendarContext {
    /**
     * Quy chiếu **UTC+7 thống nhất cho toàn dải 1901–2100**.
     *
     * ⚠️ Đây **không** phải tuyên bố rằng kết quả phản ánh đúng tập quán lịch đương
     * thời của mọi thời kỳ, đặc biệt miền Bắc 1954–1967.
     */
    data object OfficialVietnam : CalendarContext
}
