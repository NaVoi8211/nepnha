package com.nepnha.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nepnha.R
import com.nepnha.core.time.VietnameseDateFormatter
import com.nepnha.core.time.VietnameseLunarFormatter
import com.nepnha.domain.calendar.LunarDay
import com.nepnha.ui.components.InfoCard
import java.time.LocalDate

/**
 * Lịch tháng: mỗi ô là một ngày dương kèm ngày âm nhỏ bên dưới.
 *
 * Màn hình này **không tính lịch**. Mọi thứ — kể cả tháng nhuận và can chi — đến từ
 * [CalendarUiState] do `CalendarViewModel` dựng sẵn. Không có `LocalDate.now()` nào
 * trong đây ngoài preview, và không có bảng ngày lễ hard-code nào.
 *
 * Lưới bắt đầu Thứ Hai. Ô mùng 1 âm hiển thị dạng "1/7" để người xem biết mình đang
 * ở tháng âm nào; tháng nhuận thêm chữ N và được nói đủ ở thẻ chi tiết bên dưới.
 */
@Composable
fun CalendarScreen(
    state: CalendarUiState,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDay: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .testTag("screen_calendar"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        MonthHeader(
            title = VietnameseDateFormatter.monthTitle(state.month.atDay(1)),
            onPrevious = onPreviousMonth,
            onNext = onNextMonth,
        )

        WeekdayRow()
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        MonthGrid(cells = state.cells, onSelectDay = onSelectDay)

        SelectedDayCard(selected = state.selected, lunar = state.selectedLunar)

        if (state.selectedMemorials.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.calendar_day_memorials),
                    style = MaterialTheme.typography.titleSmall,
                )
                state.selectedMemorials.forEach { item ->
                    InfoCard(
                        title = item.memorial.name,
                        body = if (item.resolved.wasAdjusted) {
                            stringResource(R.string.memorial_adjusted_badge)
                        } else {
                            stringResource(
                                R.string.memorial_lunar_line,
                                "${item.resolved.effectiveLunarDay} tháng " +
                                    "${item.resolved.lunarMonth}" +
                                    if (item.resolved.isLeapMonth) " nhuận" else "",
                            )
                        },
                        modifier = Modifier.testTag("calendar_memorial_${item.memorial.id}"),
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthHeader(title: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrevious, modifier = Modifier.testTag("calendar_prev")) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_left),
                contentDescription = stringResource(R.string.calendar_previous_month),
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.testTag("calendar_month_title"),
        )
        IconButton(onClick = onNext, modifier = Modifier.testTag("calendar_next")) {
            Icon(
                painter = painterResource(R.drawable.ic_chevron_right),
                contentDescription = stringResource(R.string.calendar_next_month),
            )
        }
    }
}

private val WEEKDAYS = listOf("T2", "T3", "T4", "T5", "T6", "T7", "CN")

@Composable
private fun WeekdayRow() {
    Row(Modifier.fillMaxWidth()) {
        WEEKDAYS.forEach { label ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * Lưới dựng bằng `Column` + `Row` chứ không phải `LazyVerticalGrid`: cả tháng chỉ
 * 35–42 ô, và màn hình đã nằm trong một `verticalScroll` — lồng lazy grid vào scroll
 * là nguồn lỗi đo chiều cao kinh điển.
 */
@Composable
private fun MonthGrid(cells: List<CalendarCell>, onSelectDay: (LocalDate) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                week.forEach { cell ->
                    Box(Modifier.weight(1f)) {
                        when (cell) {
                            CalendarCell.Blank -> Box(Modifier.aspectRatio(0.82f))
                            is CalendarCell.Day -> DayCell(cell, onSelectDay)
                        }
                    }
                }
                // Tuần cuối có thể thiếu ô — chèn khoảng trống để các cột thẳng hàng.
                repeat(7 - week.size) { Box(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DayCell(cell: CalendarCell.Day, onSelectDay: (LocalDate) -> Unit) {
    val date = cell.lunar.solar
    val scheme = MaterialTheme.colorScheme
    val background = when {
        cell.isSelected -> scheme.primaryContainer
        cell.isToday -> scheme.surfaceContainerHigh
        else -> Color.Transparent
    }
    val lunarText = when (val l = cell.lunar) {
        is LunarDay.Known -> VietnameseLunarFormatter.gridLabel(l.lunar)
        is LunarDay.Unknown -> "—"
    }
    // Mùng 1 âm được nhấn để mắt bắt được nhịp tháng âm khi lướt lưới.
    val startsLunarMonth = (cell.lunar as? LunarDay.Known)?.lunar?.day == 1

    Column(
        modifier = Modifier
            .aspectRatio(0.82f)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .then(
                if (cell.isToday) Modifier.border(1.dp, scheme.primary, RoundedCornerShape(8.dp))
                else Modifier,
            )
            .clickable { onSelectDay(date) }
            .semantics {
                contentDescription = "${date.dayOfMonth} tháng ${date.monthValue}, âm lịch $lunarText" +
                    if (cell.memorialCount > 0) ", có ngày giỗ" else ""
            }
            .testTag("day_${date}"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = if (cell.isToday) FontWeight.Bold else FontWeight.Normal,
        )
        Text(
            text = lunarText,
            style = MaterialTheme.typography.labelSmall,
            color = if (startsLunarMonth) scheme.primary else scheme.onSurfaceVariant,
            fontWeight = if (startsLunarMonth) FontWeight.SemiBold else FontWeight.Normal,
        )
        // Chấm báo có ngày giỗ. Nhiều ngày giỗ cùng ngày thì chấm đậm hơn chứ không
        // xếp nhiều chấm — ô lịch quá nhỏ để đếm bằng mắt.
        if (cell.memorialCount > 0) {
            Box(
                Modifier
                    .padding(top = 2.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(scheme.error)
                    .testTag("memorial_dot_$date"),
            )
        }
    }
}

@Composable
private fun SelectedDayCard(selected: LocalDate, lunar: LunarDay) {
    val title = "${VietnameseDateFormatter.dayOfWeek(selected)}, " +
        VietnameseDateFormatter.fullDate(selected)
    val body = when (lunar) {
        is LunarDay.Known -> stringResource(
            R.string.calendar_selected_lunar,
            VietnameseLunarFormatter.dayAndMonth(lunar.lunar),
            lunar.sexagenaryYear.toString(),
        )
        is LunarDay.Unknown -> stringResource(
            when (lunar.reason) {
                LunarDay.Reason.OUT_OF_SUPPORTED_RANGE -> R.string.lunar_out_of_range
                LunarDay.Reason.ENGINE_UNAVAILABLE -> R.string.lunar_unavailable
            },
        )
    }
    InfoCard(title = title, body = body, modifier = Modifier.testTag("calendar_selected"))
}
