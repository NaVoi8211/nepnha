package com.nepnha.ui.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nepnha.R
import com.nepnha.core.time.VietnameseDateFormatter
import com.nepnha.ui.components.EmptyStateCard
import com.nepnha.ui.components.InfoCard
import com.nepnha.ui.theme.NepNhaTheme
import java.time.LocalDate

/**
 * ⚠️ CALENDAR ENGINE CHƯA ĐƯỢC IMPLEMENT.
 *
 * Màn hình này ở Phase 1 chỉ là nền UI. Nó hiển thị **duy nhất** những gì có thật
 * từ `java.time`: tháng hiện tại và ngày hôm nay.
 *
 * Cố ý KHÔNG làm ở đây:
 *  - không viết thuật toán lịch âm giả để lấp chỗ trống;
 *  - không hard-code danh sách ngày lễ;
 *  - không hiển thị bất kỳ con số âm lịch nào.
 *
 * Lưới tháng, ngày âm, ngày lễ và ngày giỗ sẽ được nối vào ở **Phase 3** sau khi
 * `VietnameseLunarCalendar` có và test pass. Xem `docs/LUNAR_CALENDAR.md`.
 */
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("screen_calendar"),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.calendar_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        InfoCard(
            title = VietnameseDateFormatter.monthTitle(today),
            body = "${stringResource(R.string.calendar_today_marker)}: " +
                "${VietnameseDateFormatter.dayOfWeek(today)}, " +
                VietnameseDateFormatter.fullDate(today),
        )

        EmptyStateCard(
            title = stringResource(R.string.calendar_placeholder_title),
            body = stringResource(R.string.calendar_placeholder_body),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CalendarScreenPreview() {
    NepNhaTheme {
        CalendarScreen(today = LocalDate.of(2026, 8, 24))
    }
}
