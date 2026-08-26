package com.nepnha.domain.calendar

import com.nepnha.core.lunar.CalendarContext
import com.nepnha.core.lunar.LunarDate
import com.nepnha.core.lunar.LunarError
import com.nepnha.core.lunar.LunarResult
import com.nepnha.core.lunar.SexagenaryYear
import com.nepnha.core.lunar.VietnameseLunarCalendar
import java.time.LocalDate
import java.time.YearMonth

/**
 * Cầu nối giữa engine lịch âm đã đóng băng và tầng UI.
 *
 * Lý do tồn tại: UI **không được** tự xử lý [LunarResult] hay [LunarError]. Nếu để
 * mỗi màn hình tự `when` trên sealed error thì sớm muộn sẽ có chỗ quên một nhánh và
 * hiển thị số sai. Ở đây quy về đúng hai trạng thái: biết hoặc không biết, kèm lý do.
 *
 * Service này **không** chứa quy tắc lịch và **không** chứa quy tắc nghiệp vụ ngày
 * giỗ. Quy tắc lịch nằm trong `core/lunar`; quy tắc ngày giỗ nằm ở
 * `domain/event/MemorialRule` và tầng tính sự kiện của phase sau.
 *
 * [calendar] cho phép `null`: nếu asset lỗi thì app vẫn chạy và nói thật là lịch âm
 * chưa dùng được, thay vì crash ngay lúc mở. Bản thân dataset đã bị khoá checksum
 * trong test nên trường hợp này chỉ xảy ra khi đóng gói hỏng.
 */
class LunarCalendarService(private val calendar: VietnameseLunarCalendar?) {

    val supportedYears: IntRange? = calendar?.supportedYears

    /** Ngày âm của một ngày dương. Không bao giờ ném lỗi. */
    fun dayOf(solar: LocalDate, context: CalendarContext = CalendarContext.OfficialVietnam): LunarDay {
        val engine = calendar ?: return LunarDay.Unknown(solar, LunarDay.Reason.ENGINE_UNAVAILABLE)
        val lunar = when (val r = engine.toLunar(solar, context)) {
            is LunarResult.Success -> r.value
            is LunarResult.Failure -> return LunarDay.Unknown(solar, r.error.toReason())
        }
        val canChi = engine.sexagenaryYear(lunar.year).getOrNull()
            ?: return LunarDay.Unknown(solar, LunarDay.Reason.OUT_OF_SUPPORTED_RANGE)
        return LunarDay.Known(solar, lunar, canChi)
    }

    /**
     * Cả tháng dương, theo đúng thứ tự ngày. Dùng cho lưới màn Lịch.
     *
     * Một tháng là 28–31 lời gọi tra bảng; đo trên A32 là 365 lượt trong ~30 ms nên
     * gọi thẳng ở luồng chính, không cần cache hay coroutine.
     */
    fun daysOfMonth(month: YearMonth): List<LunarDay> =
        (1..month.lengthOfMonth()).map { dayOf(month.atDay(it)) }

    /** Năm âm của một ngày dương, `null` khi ngoài phạm vi. */
    fun lunarYearOf(solar: LocalDate): Int? = (dayOf(solar) as? LunarDay.Known)?.lunar?.year

    /**
     * Số ngày của một tháng âm — 29 hoặc 30. `null` khi không tra được.
     *
     * Đây là câu hỏi engine trả lời được; **quyết định** phải làm gì khi ngày giỗ
     * vượt quá con số này thuộc `domain/event`, không thuộc engine.
     */
    fun daysInLunarMonth(lunarYear: Int, month: Int, isLeapMonth: Boolean): Int? =
        calendar?.daysInLunarMonth(lunarYear, month, isLeapMonth)?.getOrNull()

    /** Ngày dương của một ngày âm cụ thể, `null` khi không tồn tại hoặc ngoài phạm vi. */
    fun toSolar(day: Int, month: Int, year: Int, isLeapMonth: Boolean): LocalDate? =
        calendar?.toSolar(LunarDate(day, month, year, isLeapMonth))?.getOrNull()

    /** Tháng nhuận của một năm âm, `null` khi không tra được. */
    fun leapMonthOf(lunarYear: Int): Int? = when (val r = calendar?.leapMonthOf(lunarYear)) {
        is LunarResult.Success -> (r.value as? com.nepnha.core.lunar.LeapMonthInfo.Month)?.month
        else -> null
    }
}

/**
 * Một ngày dương kèm ngày âm của nó — hoặc lý do không có.
 *
 * Cố ý không dùng `LunarDate?`: "không biết" cần mang theo **vì sao** để UI nói đúng
 * chuyện với người dùng, thay vì để trống một cách khó hiểu.
 */
sealed interface LunarDay {

    val solar: LocalDate

    data class Known(
        override val solar: LocalDate,
        val lunar: LunarDate,
        val sexagenaryYear: SexagenaryYear,
    ) : LunarDay

    data class Unknown(
        override val solar: LocalDate,
        val reason: Reason,
    ) : LunarDay

    enum class Reason {
        /** Ngày nằm ngoài 1901–2100. Engine không ngoại suy — xem `docs/LUNAR_API.md`. */
        OUT_OF_SUPPORTED_RANGE,

        /** Không nạp được dataset. Lỗi đóng gói, không phải lỗi dữ liệu người dùng. */
        ENGINE_UNAVAILABLE,
    }
}

private fun LunarError.toReason(): LunarDay.Reason = when (this) {
    is LunarError.UnsupportedYear -> LunarDay.Reason.OUT_OF_SUPPORTED_RANGE
    // Các nhánh còn lại không thể xảy ra khi đầu vào là một LocalDate hợp lệ, nhưng
    // vẫn phải xử lý đủ: `when` không có `else` để thêm lỗi mới là biết ngay ở đây.
    is LunarError.InvalidGregorianDate,
    is LunarError.InvalidLunarDate,
    is LunarError.NonexistentLunarDate,
    is LunarError.NoSuchLeapMonth,
    -> LunarDay.Reason.ENGINE_UNAVAILABLE
}
