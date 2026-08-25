package com.nepnha.core.lunar

/**
 * Kết quả của một phép tính lịch.
 *
 * Cố ý **không dùng `null`** để biểu thị thất bại: "không có giá trị" và "không tính
 * được, vì lý do X" là hai chuyện khác nhau, và caller cần biết lý do để hiển thị
 * đúng cho người dùng.
 */
sealed interface LunarResult<out T> {
    data class Success<T>(val value: T) : LunarResult<T>
    data class Failure(val error: LunarError) : LunarResult<Nothing>

    fun getOrNull(): T? = (this as? Success)?.value
}

sealed interface LunarError {

    /** Năm nằm ngoài phạm vi được bảo đảm. Engine **không** ngoại suy. */
    data class UnsupportedYear(val year: Int, val supported: IntRange) : LunarError

    /** Ngày dương không tồn tại, ví dụ 31/02. */
    data class InvalidGregorianDate(val year: Int, val month: Int, val day: Int) : LunarError

    /** Giá trị ngày âm sai khoảng cho phép (tháng 13, ngày 31…). */
    data class InvalidLunarDate(val date: LunarDate, val reason: Reason) : LunarError {
        enum class Reason { DAY_OUT_OF_RANGE, MONTH_OUT_OF_RANGE }
    }

    /**
     * Giá trị hợp lệ về khoảng, nhưng **năm đó không có ngày này** — điển hình là
     * mùng 30 của một tháng thiếu.
     *
     * [lastValidDay] có sẵn để tầng nghiệp vụ áp `MemorialRule.missingDayPolicy` mà
     * không phải hỏi lại engine. Engine **không tự lùi 30 về 29**: đó là quy tắc ngày
     * giỗ, thuộc tầng khác — xem `docs/MEMORIAL_RULES.md`.
     */
    data class NonexistentLunarDate(val date: LunarDate, val lastValidDay: Int) : LunarError

    /** Năm âm đó không có tháng nhuận mang số này. */
    data class NoSuchLeapMonth(val year: Int, val month: Int) : LunarError
}
