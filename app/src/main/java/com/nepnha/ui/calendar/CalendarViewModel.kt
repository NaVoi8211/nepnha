package com.nepnha.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nepnha.AppContainer
import com.nepnha.domain.calendar.LunarCalendarService
import com.nepnha.domain.calendar.LunarDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Màn Lịch: lưới một tháng dương, mỗi ô kèm ngày âm.
 *
 * Toàn bộ việc tra lịch nằm ở đây và ở [LunarCalendarService] — Composable chỉ vẽ
 * lại thứ đã tính xong. Một tháng là 28–31 lượt tra bảng, đo trên A32 là 365 lượt
 * trong ~30 ms, nên dựng thẳng khi đổi tháng, không cache, không coroutine.
 */
class CalendarViewModel(
    private val service: LunarCalendarService,
    private val today: LocalDate = LocalDate.now(),
) : ViewModel() {

    private val _state = MutableStateFlow(build(YearMonth.from(today), today))
    val state: StateFlow<CalendarUiState> = _state.asStateFlow()

    fun showPreviousMonth() = moveMonth(-1)

    fun showNextMonth() = moveMonth(1)

    fun select(date: LocalDate) {
        _state.value = build(YearMonth.from(date), date)
    }

    /**
     * Đổi tháng thì chọn sẵn ngày tương ứng trong tháng mới, kẹp về ngày cuối tháng
     * nếu tháng mới ngắn hơn — tránh nhảy về mùng 1 mỗi lần lật, và tránh
     * `DateTimeException` khi đang ở ngày 31.
     */
    private fun moveMonth(delta: Long) {
        val target = _state.value.month.plusMonths(delta)
        val day = minOf(_state.value.selected.dayOfMonth, target.lengthOfMonth())
        _state.value = build(target, target.atDay(day))
    }

    private fun build(month: YearMonth, selected: LocalDate): CalendarUiState {
        val days = service.daysOfMonth(month)
        // Tuần bắt đầu Thứ Hai, đúng cách người Việt đọc lịch.
        val blanks = month.atDay(1).dayOfWeek.value - DayOfWeek.MONDAY.value
        val cells = List(blanks) { CalendarCell.Blank } + days.map { day ->
            CalendarCell.Day(
                lunar = day,
                isToday = day.solar == today,
                isSelected = day.solar == selected,
            )
        }
        return CalendarUiState(
            month = month,
            today = today,
            selected = selected,
            selectedLunar = service.dayOf(selected),
            cells = cells,
        )
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { CalendarViewModel(container.lunarCalendar) }
        }
    }
}

data class CalendarUiState(
    val month: YearMonth,
    val today: LocalDate,
    val selected: LocalDate,
    val selectedLunar: LunarDay,
    val cells: List<CalendarCell>,
)

/** Ô lưới. [Blank] là chỗ đệm đầu tháng, không phải một ngày. */
sealed interface CalendarCell {
    data object Blank : CalendarCell
    data class Day(
        val lunar: LunarDay,
        val isToday: Boolean,
        val isSelected: Boolean,
    ) : CalendarCell
}
