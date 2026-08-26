package com.nepnha.ui.calendar

import com.nepnha.core.lunar.LunarTestSupport
import com.nepnha.domain.calendar.LunarCalendarService
import com.nepnha.domain.calendar.LunarDay
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Lưới màn Lịch.
 *
 * ViewModel nhận ngày "hôm nay" qua tham số nên test chốt được một ngày cố định —
 * không có `LocalDate.now()` nào lọt vào kết quả.
 */
class CalendarViewModelTest {

    private val service = LunarCalendarService(LunarTestSupport.calendar)

    private fun vm(today: LocalDate) = CalendarViewModel(service, today)

    /**
     * Sai thì: các cột lệch, ngày rơi nhầm thứ trong tuần.
     */
    @Test
    fun `luoi bat dau Thu Hai va co du o dem`() {
        // 01/08/2026 là Thứ Bảy ⇒ cần 5 ô đệm trước nó.
        val state = vm(LocalDate.of(2026, 8, 26)).state.value
        assertEquals(YearMonth.of(2026, 8), state.month)
        assertEquals(5, state.cells.takeWhile { it is CalendarCell.Blank }.size)
        val days = state.cells.filterIsInstance<CalendarCell.Day>()
        assertEquals(31, days.size)
        assertEquals(LocalDate.of(2026, 8, 1), days.first().lunar.solar)
        assertEquals(LocalDate.of(2026, 8, 31), days.last().lunar.solar)
    }

    /**
     * Sai thì: mở app không biết hôm nay là ngày nào trong lưới.
     */
    @Test
    fun `hom nay duoc danh dau va duoc chon san`() {
        val today = LocalDate.of(2026, 8, 26)
        val state = vm(today).state.value
        val days = state.cells.filterIsInstance<CalendarCell.Day>()
        assertEquals(1, days.count { it.isToday })
        assertEquals(today, days.first { it.isToday }.lunar.solar)
        assertEquals(today, state.selected)
    }

    /**
     * Lật tháng phải giữ ngày đang chọn, và kẹp lại khi tháng mới ngắn hơn.
     *
     * Sai thì: đang ở 31/01 bấm sang tháng 2 sẽ ném `DateTimeException`.
     */
    @Test
    fun `lat thang giu ngay va kep ve cuoi thang khi can`() {
        val m = vm(LocalDate.of(2026, 1, 31))
        m.showNextMonth()
        assertEquals(YearMonth.of(2026, 2), m.state.value.month)
        assertEquals(LocalDate.of(2026, 2, 28), m.state.value.selected)
        m.showPreviousMonth()
        assertEquals(LocalDate.of(2026, 1, 28), m.state.value.selected)
    }

    /**
     * Ngày âm trong lưới phải là ngày âm thật của đúng ô đó.
     *
     * Sai thì: lưới lệch một ngày so với thẻ chi tiết.
     */
    @Test
    fun `moi o mang dung ngay am cua chinh no`() {
        val state = vm(LocalDate.of(2026, 8, 26)).state.value
        state.cells.filterIsInstance<CalendarCell.Day>().forEach { cell ->
            assertEquals(service.dayOf(cell.lunar.solar), cell.lunar)
        }
    }

    /**
     * Tháng nhuận 1938: tháng 8 thường rồi tháng 8 nhuận, ranh giới 24/09.
     *
     * Sai thì: màn Lịch không phân biệt được hai lần xuất hiện của tháng 8.
     */
    @Test
    fun `thang nhuan 1938 hien dung trong luoi`() {
        val state = vm(LocalDate.of(1938, 9, 20)).state.value
        val byDate = state.cells.filterIsInstance<CalendarCell.Day>()
            .associate { it.lunar.solar to (it.lunar as LunarDay.Known).lunar }
        assertFalse(byDate.getValue(LocalDate.of(1938, 9, 23)).isLeapMonth)
        assertEquals(30, byDate.getValue(LocalDate.of(1938, 9, 23)).day)
        assertTrue(byDate.getValue(LocalDate.of(1938, 9, 24)).isLeapMonth)
        assertEquals(1, byDate.getValue(LocalDate.of(1938, 9, 24)).day)
        assertEquals(8, byDate.getValue(LocalDate.of(1938, 9, 24)).month)
    }

    /**
     * Sang tháng ngoài phạm vi thì các ô nói rõ không tra được, không bịa số.
     *
     * Sai thì: lịch hiển thị ngày âm bịa cho năm 2101.
     */
    @Test
    fun `thang ngoai pham vi khong bia so`() {
        val m = vm(LocalDate.of(2100, 12, 15))
        m.showNextMonth()
        val days = m.state.value.cells.filterIsInstance<CalendarCell.Day>()
        assertTrue(days.isNotEmpty())
        assertTrue(
            days.all {
                (it.lunar as? LunarDay.Unknown)?.reason == LunarDay.Reason.OUT_OF_SUPPORTED_RANGE
            },
        )
    }

    /**
     * Chọn một ngày ở tháng khác thì lưới nhảy theo.
     *
     * Sai thì: thẻ chi tiết và lưới nói hai chuyện khác nhau.
     */
    @Test
    fun `chon ngay o thang khac thi luoi doi theo`() {
        val m = vm(LocalDate.of(2026, 8, 26))
        m.select(LocalDate.of(2026, 12, 3))
        assertEquals(YearMonth.of(2026, 12), m.state.value.month)
        assertEquals(LocalDate.of(2026, 12, 3), m.state.value.selected)
        assertEquals(service.dayOf(LocalDate.of(2026, 12, 3)), m.state.value.selectedLunar)
    }
}
