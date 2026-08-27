package com.nepnha.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nepnha.AppContainer
import com.nepnha.domain.calendar.LunarCalendarService
import com.nepnha.domain.calendar.LunarDay
import com.nepnha.domain.event.MemorialDateResolver
import com.nepnha.domain.event.ResolvedMemorialDate
import com.nepnha.domain.model.FamilyMember
import com.nepnha.domain.model.Memorial
import com.nepnha.domain.model.displayName
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Màn Lịch: lưới một tháng dương, mỗi ô kèm ngày âm và dấu ngày giỗ.
 *
 * Toàn bộ việc tra lịch và quy đổi ngày giỗ nằm ở đây và ở tầng domain — Composable
 * chỉ vẽ lại thứ đã tính xong. Một tháng là 28–31 lượt tra bảng, đo trên A32 là 365
 * lượt trong ~30 ms, nên dựng thẳng khi đổi tháng, không cache, không coroutine.
 */
class CalendarViewModel(
    private val service: LunarCalendarService,
    private val today: LocalDate = LocalDate.now(),
    memorials: Flow<List<Memorial>> = flowOf(emptyList()),
    members: Flow<List<FamilyMember>> = flowOf(emptyList()),
) : ViewModel() {

    /** Stateless, nên dựng tại chỗ cho gọn thay vì kéo cả container vào ViewModel này. */
    private val resolver = MemorialDateResolver(service)

    private var view = ViewSelection(YearMonth.from(today), today)
    private var knownMemorials: List<Memorial> = emptyList()
    private var knownMembers: List<FamilyMember> = emptyList()

    private val _state = MutableStateFlow(build(view, knownMemorials))

    /**
     * State dựng **đồng bộ** trong `_state` chứ không qua `stateIn(WhileSubscribed)`.
     *
     * Với `WhileSubscribed`, lật tháng lúc chưa có collector sẽ không cập nhật `value`
     * — đúng lỗi mà `CalendarViewModelTest` bắt được. Ở đây thao tác người dùng đổi
     * state ngay lập tức, còn danh sách ngày giỗ từ Room về lúc nào thì dựng lại
     * lúc ấy.
     */
    val state: StateFlow<CalendarUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            memorials.collect { list ->
                knownMemorials = list
                _state.value = build(view, list)
            }
        }
        viewModelScope.launch {
            members.collect { list ->
                knownMembers = list
                _state.value = build(view, knownMemorials)
            }
        }
    }

    fun showPreviousMonth() = moveMonth(-1)

    fun showNextMonth() = moveMonth(1)

    fun select(date: LocalDate) {
        view = ViewSelection(YearMonth.from(date), date)
        _state.value = build(view, knownMemorials)
    }

    /**
     * Đổi tháng thì chọn sẵn ngày tương ứng trong tháng mới, kẹp về ngày cuối tháng
     * nếu tháng mới ngắn hơn — tránh nhảy về mùng 1 mỗi lần lật, và tránh
     * `DateTimeException` khi đang ở ngày 31.
     */
    private fun moveMonth(delta: Long) {
        val target = view.month.plusMonths(delta)
        val day = minOf(view.selected.dayOfMonth, target.lengthOfMonth())
        view = ViewSelection(target, target.atDay(day))
        _state.value = build(view, knownMemorials)
    }

    private fun build(v: ViewSelection, memorials: List<Memorial>): CalendarUiState {
        val days = service.daysOfMonth(v.month)
        val byDate = resolver.occurrencesBetween(
            memorials,
            v.month.atDay(1),
            v.month.atEndOfMonth(),
        )
        // Tuần bắt đầu Thứ Hai, đúng cách người Việt đọc lịch.
        val blanks = v.month.atDay(1).dayOfWeek.value - DayOfWeek.MONDAY.value
        val cells = List(blanks) { CalendarCell.Blank } + days.map { day ->
            CalendarCell.Day(
                lunar = day,
                isToday = day.solar == today,
                isSelected = day.solar == v.selected,
                memorialCount = byDate[day.solar]?.size ?: 0,
            )
        }
        return CalendarUiState(
            month = v.month,
            today = today,
            selected = v.selected,
            selectedLunar = service.dayOf(v.selected),
            selectedMemorials = byDate[v.selected].orEmpty()
                .map { (m, r) -> MemorialOnDay(m, m.displayName(knownMembers), r) },
            cells = cells,
        )
    }

    private data class ViewSelection(val month: YearMonth, val selected: LocalDate)

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CalendarViewModel(
                    container.lunarCalendar,
                    memorials = container.memorials.observe(),
                    members = container.familyOverview.observe().map { it.members },
                )
            }
        }
    }
}

data class CalendarUiState(
    val month: YearMonth,
    val today: LocalDate,
    val selected: LocalDate,
    val selectedLunar: LunarDay,
    val selectedMemorials: List<MemorialOnDay> = emptyList(),
    val cells: List<CalendarCell> = emptyList(),
)

/** Một ngày giỗ rơi vào ngày đang chọn, kèm kết quả quy đổi của chính năm đó. */
data class MemorialOnDay(
    val memorial: Memorial,
    /** Đã áp quy tắc liên kết thành viên — màn hình không tự tra danh sách. */
    val displayName: String,
    val resolved: ResolvedMemorialDate,
)

/** Ô lưới. [Blank] là chỗ đệm đầu tháng, không phải một ngày. */
sealed interface CalendarCell {
    data object Blank : CalendarCell
    data class Day(
        val lunar: LunarDay,
        val isToday: Boolean,
        val isSelected: Boolean,
        /** Số ngày giỗ rơi vào ngày này. Nhiều người có thể mất cùng một ngày âm. */
        val memorialCount: Int = 0,
    ) : CalendarCell
}
