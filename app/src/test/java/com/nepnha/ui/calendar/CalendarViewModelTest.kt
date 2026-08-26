package com.nepnha.ui.calendar

import com.nepnha.core.lunar.LunarTestSupport
import com.nepnha.domain.calendar.LunarCalendarService
import com.nepnha.domain.calendar.LunarDay
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
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

/**
 * Marker ngày giỗ trên lưới lịch.
 *
 * Tách khỏi lớp trên vì cần một luồng ngày giỗ; dùng `flowOf` chứ không dựng Room —
 * đây là test hành vi dựng lưới, phần lưu trữ đã có test riêng.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CalendarMemorialMarkerTest {

    private val service = LunarCalendarService(LunarTestSupport.calendar)

    /**
     * `viewModelScope` chạy trên `Dispatchers.Main`, thứ không tồn tại trong test JVM
     * ⇒ luồng ngày giỗ sẽ không bao giờ được thu. `UnconfinedTestDispatcher` cho nó
     * chạy ngay tại chỗ, nên `state.value` đọc được ngay sau khi dựng ViewModel.
     */
    @org.junit.Before
    fun setUpDispatcher() {
        kotlinx.coroutines.Dispatchers.setMain(kotlinx.coroutines.test.UnconfinedTestDispatcher())
    }

    @org.junit.After
    fun tearDownDispatcher() {
        kotlinx.coroutines.Dispatchers.resetMain()
    }

    private fun memorial(id: Long, day: Int, month: Int) = com.nepnha.domain.model.Memorial(
        id = id,
        familyId = 1,
        name = "Cụ $id",
        lunarDay = day,
        lunarMonth = month,
        rule = com.nepnha.domain.event.MemorialRule(),
        note = null,
    )

    /**
     * Mùng 1 tháng 8 âm năm 2026 = 11/09/2026.
     *
     * Sai thì: lịch không đánh dấu ngày giỗ, người dùng lướt qua mà không biết.
     */
    @Test
    fun `ngay co ngay gio duoc danh dau`() {
        val vm = CalendarViewModel(
            service,
            LocalDate.of(2026, 9, 1),
            kotlinx.coroutines.flow.flowOf(listOf(memorial(1, 1, 8))),
        )
        val marked = vm.state.value.cells.filterIsInstance<CalendarCell.Day>()
            .filter { it.memorialCount > 0 }
        assertEquals(1, marked.size)
        assertEquals(LocalDate.of(2026, 9, 11), marked.single().lunar.solar)
    }

    /**
     * Sai thì: hai người mất cùng ngày chỉ hiện một, mất dấu người kia.
     */
    @Test
    fun `nhieu ngay gio cung ngay deu duoc dem va liet ke`() {
        val vm = CalendarViewModel(
            service,
            LocalDate.of(2026, 9, 1),
            kotlinx.coroutines.flow.flowOf(listOf(memorial(1, 1, 8), memorial(2, 1, 8))),
        )
        val cell = vm.state.value.cells.filterIsInstance<CalendarCell.Day>()
            .single { it.lunar.solar == LocalDate.of(2026, 9, 11) }
        assertEquals(2, cell.memorialCount)

        vm.select(LocalDate.of(2026, 9, 11))
        assertEquals(2, vm.state.value.selectedMemorials.size)
    }

    /**
     * Ngày giỗ mùng 30 tháng 7 âm 2026 phải hiện vào 10/09 (ngày 29) kèm cờ điều chỉnh.
     *
     * Sai thì: lịch đánh dấu sai ngày, hoặc đánh dấu mà không cho biết đã lùi ngày.
     */
    @Test
    fun `ngay gio bi dieu chinh van hien dung cho va co co bao`() {
        val vm = CalendarViewModel(
            service,
            LocalDate.of(2026, 9, 1),
            kotlinx.coroutines.flow.flowOf(listOf(memorial(1, 30, 7))),
        )
        vm.select(LocalDate.of(2026, 9, 10))
        val item = vm.state.value.selectedMemorials.single()
        assertEquals(30, item.resolved.originalLunarDay)
        assertEquals(29, item.resolved.effectiveLunarDay)
        assertTrue(item.resolved.wasAdjusted)
    }

    /** Sai thì: tháng không có ngày giỗ nào vẫn hiện chấm đỏ lung tung. */
    @Test
    fun `thang khong co ngay gio thi khong co marker`() {
        val vm = CalendarViewModel(
            service,
            LocalDate.of(2026, 9, 1),
            kotlinx.coroutines.flow.flowOf(listOf(memorial(1, 1, 8))),
        )
        vm.showNextMonth()
        assertTrue(
            vm.state.value.cells.filterIsInstance<CalendarCell.Day>()
                .all { it.memorialCount == 0 },
        )
    }
}
