package com.nepnha.domain.calendar

import com.nepnha.core.lunar.LunarDate
import com.nepnha.core.lunar.LunarTestSupport
import java.time.LocalDate
import java.time.YearMonth
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Tầng nối engine → UI.
 *
 * Test ở đây kiểm **việc nối**, không kiểm lại thiên văn: tính đúng của lịch đã được
 * khoá ở bộ test Phase 3. Điều cần chứng minh là service không bóp méo, không nuốt
 * lỗi, và không tự ý sửa kết quả engine.
 */
class LunarCalendarServiceTest {

    private val service = LunarCalendarService(LunarTestSupport.calendar)

    private fun lunarOf(y: Int, m: Int, d: Int): LunarDate =
        (service.dayOf(LocalDate.of(y, m, d)) as LunarDay.Known).lunar

    /**
     * Vector Tier 1 đi xuyên qua service, không gọi thẳng engine.
     *
     * Sai thì: service đã làm lệch kết quả engine trên đường ra UI.
     */
    @Test
    fun `vector nha nuoc di qua service van dung`() {
        // 6150/TB-BLĐTBXH — Tết Ất Tỵ 2025 rơi vào 29/01/2025.
        assertEquals(LunarDate(1, 1, 2025), lunarOf(2025, 1, 29))
        // 9441/TB-BNV — Tết Bính Ngọ 2026 rơi vào 17/02/2026.
        assertEquals(LunarDate(1, 1, 2026), lunarOf(2026, 2, 17))
        // Tết Ất Sửu 1985: 21/01, KHÔNG phải 20/02 — ca Việt Nam khác Trung Quốc.
        assertEquals(LunarDate(1, 1, 1985), lunarOf(1985, 1, 21))
    }

    /**
     * Can chi phải theo năm ÂM, không phải năm dương.
     *
     * Sai thì: những ngày đầu tháng 1 dương sẽ hiện sai tên năm.
     */
    @Test
    fun `can chi lay theo nam am`() {
        val tet2026 = service.dayOf(LocalDate.of(2026, 2, 17)) as LunarDay.Known
        assertEquals("Bính Ngọ", tet2026.sexagenaryYear.toString())
        // 01/01/2026 dương vẫn thuộc năm âm 2025 (Ất Tỵ).
        val newYear = service.dayOf(LocalDate.of(2026, 1, 1)) as LunarDay.Known
        assertEquals(2025, newYear.lunar.year)
        assertEquals("Ất Tỵ", newYear.sexagenaryYear.toString())
    }

    /**
     * Ba năm nhuận đã được nguồn ngoài xác nhận, cộng ca 1938 của audit cuối Phase 3.
     *
     * Sai thì: dataset hoặc engine đã bị đổi — 1938 quay lại tháng 7 là dấu hiệu lỗi
     * ΔT/lượng tử hoá tái phát.
     */
    @Test
    fun `thang nhuan cua nhung nam da biet`() {
        assertEquals(2, service.leapMonthOf(1985))   // Ban Lịch Nhà nước
        assertEquals(7, service.leapMonthOf(1987))   // Ban Lịch Nhà nước
        assertEquals(8, service.leapMonthOf(1938))   // audit cuối Phase 3
        assertNull(service.leapMonthOf(1984))        // Việt Nam KHÔNG nhuận, Trung Quốc có
        assertNull(service.leapMonthOf(2026))        // năm thường
    }

    /**
     * Ngày trong tháng nhuận phải mang cờ nhuận, và tháng thường cùng số thì không.
     *
     * Sai thì: UI hiển thị "tháng 8" cho cả hai lần xuất hiện.
     */
    @Test
    fun `ngay trong thang nhuan mang co nhuan`() {
        assertEquals(LunarDate(1, 8, 1938, isLeapMonth = false), lunarOf(1938, 8, 25))
        assertEquals(LunarDate(1, 8, 1938, isLeapMonth = true), lunarOf(1938, 9, 24))
        assertEquals(LunarDate(1, 9, 1938), lunarOf(1938, 10, 23))
    }

    /**
     * Nhãn năm âm được phép nằm ngoài 1901–2100 — hợp đồng API Phase 3.
     *
     * Sai thì: 20 ngày đầu năm 1901 không biểu diễn được.
     */
    @Test
    fun `dau nam duong co the thuoc nam am truoc`() {
        assertEquals(LunarDate(11, 11, 1900), lunarOf(1901, 1, 1))
        val last = service.dayOf(LocalDate.of(2100, 12, 31)) as LunarDay.Known
        assertEquals(2100, last.lunar.year)
    }

    /**
     * Ngoài phạm vi thì nói rõ lý do, không ngoại suy và không trả về số bừa.
     *
     * Sai thì: UI hiển thị một ngày âm bịa cho năm 1900 hoặc 2101.
     */
    @Test
    fun `ngoai pham vi tra ve Unknown kem ly do`() {
        val before = service.dayOf(LocalDate.of(1900, 12, 31))
        val after = service.dayOf(LocalDate.of(2101, 1, 1))
        assertEquals(LunarDay.Reason.OUT_OF_SUPPORTED_RANGE, (before as LunarDay.Unknown).reason)
        assertEquals(LunarDay.Reason.OUT_OF_SUPPORTED_RANGE, (after as LunarDay.Unknown).reason)
    }

    /**
     * Không có dataset thì app vẫn chạy và nói thật, thay vì ném lỗi lúc mở.
     *
     * Sai thì: đóng gói hỏng sẽ làm app crash ngay màn hình đầu.
     */
    @Test
    fun `khong co engine thi bao ENGINE_UNAVAILABLE chu khong nem`() {
        val broken = LunarCalendarService(null)
        val day = broken.dayOf(LocalDate.of(2026, 8, 26))
        assertEquals(LunarDay.Reason.ENGINE_UNAVAILABLE, (day as LunarDay.Unknown).reason)
        assertNull(broken.supportedYears)
        assertNull(broken.leapMonthOf(1985))
        assertEquals(31, broken.daysOfMonth(YearMonth.of(2026, 8)).size)
    }

    /**
     * `daysOfMonth` phải trả đúng số ngày, đúng thứ tự, không thiếu không thừa.
     *
     * Sai thì: lưới lịch lệch ngày hoặc mất ngày cuối tháng.
     */
    @Test
    fun `daysOfMonth tra dung so ngay va dung thu tu`() {
        val feb = service.daysOfMonth(YearMonth.of(2024, 2))   // năm nhuận dương
        assertEquals(29, feb.size)
        assertEquals(LocalDate.of(2024, 2, 1), feb.first().solar)
        assertEquals(LocalDate.of(2024, 2, 29), feb.last().solar)
        assertTrue(feb.zipWithNext().all { (a, b) -> b.solar == a.solar.plusDays(1) })
    }

    /**
     * Kết quả không được phụ thuộc múi giờ máy — yêu cầu §X của Phase 4.
     *
     * Sai thì: cùng một ngày cho ra ngày âm khác nhau ở hai máy đặt múi giờ khác nhau.
     */
    @Test
    fun `khong phu thuoc timezone cua may`() {
        val goc = TimeZone.getDefault()
        try {
            val mocs = listOf(
                LocalDate.of(1901, 1, 1), LocalDate.of(1938, 9, 24),
                LocalDate.of(1985, 1, 21), LocalDate.of(2026, 8, 26),
                LocalDate.of(2100, 12, 31),
            )
            val chuan = mocs.map { service.dayOf(it) }
            for (tz in listOf("Asia/Ho_Chi_Minh", "Asia/Shanghai", "America/Los_Angeles", "UTC")) {
                TimeZone.setDefault(TimeZone.getTimeZone(tz))
                assertEquals("đổi timezone sang $tz làm đổi kết quả", chuan, mocs.map { service.dayOf(it) })
            }
        } finally {
            TimeZone.setDefault(goc)
        }
    }
}
