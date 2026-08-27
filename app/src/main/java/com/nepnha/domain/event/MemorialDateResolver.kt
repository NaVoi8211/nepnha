package com.nepnha.domain.event

import com.nepnha.domain.calendar.LunarCalendarService
import com.nepnha.domain.model.Memorial
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Quy đổi một [Memorial] ra ngày dương của một năm âm cụ thể, theo đúng
 * [MemorialRule] của chính ngày giỗ đó.
 *
 * ĐÂY LÀ NƠI DUY NHẤT quyết định:
 *  · tháng thường hay tháng nhuận;
 *  · mùng 30 trong tháng thiếu thì làm gì;
 *  · năm nào bị bỏ qua.
 *
 * Engine lịch âm **không** tham gia vào những quyết định này. Nó chỉ trả lời "tháng
 * đó có bao nhiêu ngày", "năm đó nhuận tháng mấy", "ngày âm này là ngày dương nào" —
 * xem `docs/LUNAR_API.md`. Engine **không bao giờ tự lùi 30 về 29**.
 */
class MemorialDateResolver(private val calendar: LunarCalendarService) {

    /** Quy đổi cho đúng một năm âm. */
    fun resolve(memorial: Memorial, lunarYear: Int): MemorialResolution {
        // Hỏi trước xem năm này có dữ liệu không, bằng THÁNG THƯỜNG — tháng thường
        // luôn tồn tại nếu năm âm đó nằm trong dữ liệu. Không tách bước này thì năm
        // ngoài phạm vi sẽ bị báo nhầm thành "năm không có tháng nhuận", và biểu mẫu
        // khuyên người dùng đổi lựa chọn cho một vấn đề không phải của họ.
        val commonLength = calendar.daysInLunarMonth(lunarYear, memorial.lunarMonth, false)
            ?: return MemorialResolution.Skipped(
                lunarYear,
                MemorialResolution.Reason.OUT_OF_SUPPORTED_RANGE,
            )

        val leapMonth = calendar.leapMonthOf(lunarYear)
        val wantsLeap = memorial.rule.leapMonthPolicy != LeapMonthPolicy.COMMON_MONTH_DEFAULT
        val yearHasIt = leapMonth == memorial.lunarMonth

        val useLeap: Boolean
        var fellBackToCommonMonth = false
        when {
            !wantsLeap -> useLeap = false
            yearHasIt -> useLeap = true
            memorial.rule.leapMonthPolicy == LeapMonthPolicy.LEAP_MONTH_ONLY ->
                return MemorialResolution.Skipped(lunarYear, MemorialResolution.Reason.NO_LEAP_MONTH)
            else -> {
                // LEAP_MONTH_PREFERRED: lùi về tháng thường, nhưng phải nói ra.
                useLeap = false
                fellBackToCommonMonth = true
            }
        }

        val length = if (useLeap) {
            calendar.daysInLunarMonth(lunarYear, memorial.lunarMonth, true)
                ?: return MemorialResolution.Skipped(
                    lunarYear,
                    MemorialResolution.Reason.OUT_OF_SUPPORTED_RANGE,
                )
        } else {
            commonLength
        }

        val effectiveDay: Int
        var dayWasShortened = false
        if (memorial.lunarDay > length) {
            when (memorial.rule.missingDayPolicy) {
                MissingDayPolicy.SKIP ->
                    return MemorialResolution.Skipped(
                        lunarYear,
                        MemorialResolution.Reason.MISSING_DAY,
                    )
                MissingDayPolicy.LAST_VALID_DAY_OF_MONTH -> {
                    effectiveDay = length
                    dayWasShortened = true
                }
            }
        } else {
            effectiveDay = memorial.lunarDay
        }

        val solar = calendar.toSolar(effectiveDay, memorial.lunarMonth, lunarYear, useLeap)
            ?: return MemorialResolution.Skipped(
                lunarYear,
                MemorialResolution.Reason.OUT_OF_SUPPORTED_RANGE,
            )

        return MemorialResolution.Resolved(
            ResolvedMemorialDate(
                originalLunarDay = memorial.lunarDay,
                effectiveLunarDay = effectiveDay,
                lunarMonth = memorial.lunarMonth,
                lunarYear = lunarYear,
                isLeapMonth = useLeap,
                solarDate = solar,
                dayWasShortened = dayWasShortened,
                fellBackToCommonMonth = fellBackToCommonMonth,
            ),
        )
    }

    /**
     * Ngày giỗ kế tiếp tính từ [today], theo **ngày dương**.
     *
     * Ngày giỗ của hôm nay vẫn tính là "sắp tới" — hôm nay là ngày phải làm cỗ, không
     * phải ngày đã lỡ.
     *
     * Dò tới **hết phạm vi dữ liệu**, không dùng một cửa sổ cố định. Bản đầu tiên
     * giới hạn 25 năm với lý do "phủ chu kỳ Meton 19 năm"; đo cạn kiệt ở
     * `MemorialSearchWindowTest` cho thấy lý do đó **sai**: tháng nhuận mang một số
     * cụ thể không lặp theo chu kỳ 19 năm, và khoảng cách thật giữa hai lần giỗ liên
     * tiếp lên tới **114 năm âm** (ngày 30 tháng 4, chỉ tháng nhuận, bỏ qua năm
     * thiếu). Cửa sổ 25 năm sẽ báo "không có ngày giỗ nào" trong khi thực tế có.
     *
     * Vòng lặp vẫn hữu hạn vì phạm vi dữ liệu hữu hạn: nhiều nhất ~200 lượt tra bảng,
     * và trường hợp thường gặp thoát ngay ở vòng đầu.
     *
     * Trả `null` khi trong toàn bộ phạm vi không có lần nào — một trạng thái hợp lệ
     * mà UI phải nói ra, không phải lỗi.
     */
    fun nextOccurrence(memorial: Memorial, today: LocalDate): ResolvedMemorialDate? {
        val startYear = calendar.lunarYearOf(today) ?: return null
        val lastYear = calendar.supportedYears?.last ?: return null
        for (year in startYear..(lastYear + 1)) {
            val r = resolve(memorial, year)
            if (r is MemorialResolution.Resolved && !r.date.solarDate.isBefore(today)) {
                return r.date
            }
        }
        return null
    }

    /**
     * Danh sách ngày giỗ kèm lần kế tiếp, sắp theo ngày dương gần nhất trước.
     *
     * Ngày giỗ không tìm được lần kế tiếp nào (thường là [LeapMonthPolicy.LEAP_MONTH_ONLY]
     * cho một tháng lâu không nhuận) vẫn nằm trong danh sách nhưng xếp cuối — người
     * dùng cần thấy nó tồn tại chứ không phải nó biến mất.
     */
    fun upcoming(
        memorials: List<Memorial>,
        today: LocalDate,
        displayName: (Memorial) -> String = { it.name },
    ): List<UpcomingMemorial> =
        memorials
            .map { m ->
                val next = nextOccurrence(m, today)
                UpcomingMemorial(
                    memorial = m,
                    displayName = displayName(m),
                    next = next,
                    daysUntil = next?.let { ChronoUnit.DAYS.between(today, it.solarDate) },
                )
            }
            .sortedWith(compareBy(nullsLast()) { it.next?.solarDate })

    /** Mọi ngày giỗ rơi vào khoảng ngày dương đã cho. Dùng cho marker trên màn Lịch. */
    fun occurrencesBetween(
        memorials: List<Memorial>,
        from: LocalDate,
        to: LocalDate,
    ): Map<LocalDate, List<Pair<Memorial, ResolvedMemorialDate>>> {
        val result = mutableMapOf<LocalDate, MutableList<Pair<Memorial, ResolvedMemorialDate>>>()
        // Một khoảng vài tuần có thể vắt qua hai năm âm ⇒ xét cả hai đầu.
        val years = listOfNotNull(calendar.lunarYearOf(from), calendar.lunarYearOf(to)).distinct()
        for (memorial in memorials) {
            for (year in years) {
                val r = resolve(memorial, year)
                if (r is MemorialResolution.Resolved) {
                    val d = r.date.solarDate
                    if (!d.isBefore(from) && !d.isAfter(to)) {
                        result.getOrPut(d) { mutableListOf() }.add(memorial to r.date)
                    }
                }
            }
        }
        return result
    }
}

/**
 * Một ngày giỗ kèm lần kế tiếp của nó.
 *
 * [next] có thể `null`: ngày giỗ chỉ tính tháng nhuận mà tháng ấy lâu không nhuận thì
 * trong tầm dò 25 năm có thể không có lần nào. Đó là trạng thái hợp lệ, UI phải nói
 * ra chứ không được giấu ngày giỗ đi.
 */
data class UpcomingMemorial(
    val memorial: Memorial,
    /**
     * Tên để hiển thị — đã áp quy tắc liên kết thành viên. Tính sẵn ở đây để không
     * màn hình nào phải tự tra danh sách thành viên.
     */
    val displayName: String = memorial.name,
    val next: ResolvedMemorialDate?,
    val daysUntil: Long?,
)
