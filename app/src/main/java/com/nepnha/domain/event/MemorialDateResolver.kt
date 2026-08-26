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

    /**
     * Số năm âm tối đa dò tới khi tìm ngày giỗ kế tiếp.
     *
     * 25 chứ không phải 2–3: với [LeapMonthPolicy.LEAP_MONTH_ONLY], một tháng nhuận
     * mang số cụ thể chỉ quay lại sau nhiều chục năm. 25 phủ trọn một chu kỳ Meton
     * (19 năm) và vẫn là số hữu hạn — vòng lặp không bao giờ chạy mãi.
     */
    private val lookAheadYears = 25

    /** Quy đổi cho đúng một năm âm. */
    fun resolve(memorial: Memorial, lunarYear: Int): MemorialResolution {
        val leapMonth = calendar.leapMonthOf(lunarYear)
        val wantsLeap = memorial.rule.leapMonthPolicy != LeapMonthPolicy.COMMON_MONTH_DEFAULT
        val yearHasIt = leapMonth == memorial.lunarMonth

        val useLeap: Boolean
        var adjustment = ResolvedMemorialDate.AdjustmentReason.NONE
        when {
            !wantsLeap -> useLeap = false
            yearHasIt -> useLeap = true
            memorial.rule.leapMonthPolicy == LeapMonthPolicy.LEAP_MONTH_ONLY ->
                return MemorialResolution.Skipped(lunarYear, MemorialResolution.Reason.NO_LEAP_MONTH)
            else -> {
                // LEAP_MONTH_PREFERRED: lùi về tháng thường, nhưng phải nói ra.
                useLeap = false
                adjustment = ResolvedMemorialDate.AdjustmentReason.LEAP_MONTH_FELL_BACK_TO_COMMON
            }
        }

        val length = calendar.daysInLunarMonth(lunarYear, memorial.lunarMonth, useLeap)
            ?: return MemorialResolution.Skipped(
                lunarYear,
                MemorialResolution.Reason.OUT_OF_SUPPORTED_RANGE,
            )

        val effectiveDay: Int
        if (memorial.lunarDay > length) {
            when (memorial.rule.missingDayPolicy) {
                MissingDayPolicy.SKIP ->
                    return MemorialResolution.Skipped(
                        lunarYear,
                        MemorialResolution.Reason.MISSING_DAY,
                    )
                MissingDayPolicy.LAST_VALID_DAY_OF_MONTH -> {
                    effectiveDay = length
                    // Điều chỉnh ngày quan trọng hơn việc đã lùi tháng ⇒ ghi đè lý do.
                    adjustment = ResolvedMemorialDate.AdjustmentReason.MISSING_DAY_IN_MONTH
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
                adjustment = adjustment,
            ),
        )
    }

    /**
     * Ngày giỗ kế tiếp tính từ [today], theo **ngày dương**.
     *
     * Ngày giỗ của hôm nay vẫn tính là "sắp tới" — hôm nay là ngày phải làm cỗ, không
     * phải ngày đã lỡ.
     *
     * Năm nào bị policy bỏ qua thì dò tiếp năm sau, tối đa [lookAheadYears] năm. Trả
     * `null` khi không tìm được trong khoảng đó — một trạng thái hợp lệ mà UI phải
     * nói ra, không phải lỗi.
     */
    fun nextOccurrence(memorial: Memorial, today: LocalDate): ResolvedMemorialDate? {
        val startYear = calendar.lunarYearOf(today) ?: return null
        for (offset in 0..lookAheadYears) {
            val r = resolve(memorial, startYear + offset)
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
    fun upcoming(memorials: List<Memorial>, today: LocalDate): List<UpcomingMemorial> =
        memorials
            .map { m ->
                val next = nextOccurrence(m, today)
                UpcomingMemorial(
                    memorial = m,
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
    val next: ResolvedMemorialDate?,
    val daysUntil: Long?,
)
