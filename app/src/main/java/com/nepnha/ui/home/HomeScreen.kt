package com.nepnha.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nepnha.R
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TextButton
import com.nepnha.core.time.VietnameseDateFormatter
import com.nepnha.core.time.VietnameseLunarFormatter
import com.nepnha.domain.calendar.LunarDay
import com.nepnha.domain.event.UpcomingMemorial
import com.nepnha.ui.memorial.countdownLabel
import com.nepnha.ui.components.EmptyStateCard
import com.nepnha.ui.components.InfoCard
import com.nepnha.ui.components.SectionHeader
import com.nepnha.ui.theme.NepNhaTheme
import java.time.LocalDate

/**
 * Màn hình quan trọng nhất của app: **hôm nay nhà mình có việc gì?**
 *
 * Không có ViewModel ở Phase 1 — màn hình chưa có state nào cần giữ qua
 * configuration change ngoài chính ngày hôm nay, và ngày được truyền vào như tham
 * số nên vừa test được vừa không cần bộ máy nào thêm. ViewModel sẽ xuất hiện ở
 * Phase 4/7 khi có nghi lễ và ngày giỗ thật.
 *
 * TRUNG THỰC VỀ DỮ LIỆU: ngày dương từ `java.time`, ngày âm từ engine đã đóng băng
 * ở Phase 3. Màn hình **không tự tính gì cả** — kể cả tháng nhuận và can chi đều do
 * `LunarCalendarService` trả về. Không tra được thì nói thẳng, không hiển thị số giả.
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onSetupFamily: () -> Unit,
    onOpenMemorials: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("screen_home"),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        TodayHeader(today = state.today, lunar = state.lunar)

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column {
            SectionHeader(text = stringResource(R.string.home_section_today))
            EmptyStateCard(
                title = stringResource(R.string.home_no_ritual_title),
                body = stringResource(R.string.home_no_ritual_body),
            )
        }

        Column {
            SectionHeader(text = stringResource(R.string.home_section_upcoming))
            if (state.upcoming.isEmpty()) {
                EmptyStateCard(
                    title = stringResource(R.string.home_no_memorial_title),
                    body = stringResource(R.string.home_no_memorial_body),
                    actionLabel = stringResource(R.string.memorial_add),
                    onAction = onOpenMemorials,
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.upcoming.forEach { item -> UpcomingRow(item, onOpenMemorials) }
                    TextButton(
                        onClick = onOpenMemorials,
                        modifier = Modifier.testTag("home_memorial_all"),
                    ) { Text(stringResource(R.string.home_memorial_all)) }
                }
            }
        }

        Column {
            SectionHeader(text = state.familyName ?: stringResource(R.string.home_section_family))
            if (state.memberCount == 0) {
                EmptyStateCard(
                    title = stringResource(R.string.home_no_family_title),
                    body = stringResource(R.string.home_no_family_body),
                    actionLabel = stringResource(R.string.home_setup_family),
                    onAction = onSetupFamily,
                )
            } else if (state.primaryMemberName == null) {
                // Có người trong nhà nhưng chưa ai đứng khấn — nhắc, không tự chọn.
                EmptyStateCard(
                    title = stringResource(R.string.family_no_worshipper),
                    body = stringResource(R.string.home_family_members, state.memberCount),
                    actionLabel = stringResource(R.string.family_choose_worshipper),
                    onAction = onSetupFamily,
                    modifier = Modifier.testTag("home_no_worshipper"),
                )
            } else {
                InfoCard(
                    title = stringResource(R.string.home_family_members, state.memberCount),
                    body = stringResource(R.string.home_family_worshipper, state.primaryMemberName),
                    modifier = Modifier.testTag("home_family_summary"),
                )
            }
        }
    }
}

/**
 * Một dòng ngày giỗ sắp tới. Mọi giá trị ở đây đã được `MemorialDateResolver` tính
 * xong; hàng này chỉ trình bày, kể cả dấu hiệu "đã điều chỉnh".
 */
@Composable
private fun UpcomingRow(item: UpcomingMemorial, onClick: () -> Unit) {
    val next = item.next
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("home_upcoming_${item.memorial.id}"),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(item.memorial.name, style = MaterialTheme.typography.titleMedium)
                item.daysUntil?.let {
                    Text(countdownLabel(it), style = MaterialTheme.typography.labelLarge)
                }
            }
            if (next == null) {
                Text(
                    text = stringResource(R.string.memorial_none_upcoming),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                Text(
                    text = VietnameseDateFormatter.fullDate(next.solarDate),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = stringResource(
                        R.string.memorial_lunar_line,
                        "${next.effectiveLunarDay} tháng ${next.lunarMonth}" +
                            if (next.isLeapMonth) " nhuận" else "",
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (next.wasAdjusted) {
                    Text(
                        text = stringResource(R.string.memorial_adjusted_badge),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun TodayHeader(today: LocalDate, lunar: LunarDay, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = stringResource(R.string.app_name).uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.home_today_label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = VietnameseDateFormatter.dayOfWeek(today),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = VietnameseDateFormatter.fullDate(today),
            style = MaterialTheme.typography.displaySmall,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.home_lunar_label),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        when (lunar) {
            // Gộp ngày âm và can chi vào MỘT dòng: giữ header đủ thấp để nút "Thiết
            // lập gia đình" không bị đẩy khỏi màn hình trên máy nhỏ như A32.
            is LunarDay.Known -> Text(
                // Tháng nhuận luôn kèm chữ "nhuận" — xem VietnameseLunarFormatter.
                text = VietnameseLunarFormatter.full(lunar.lunar, lunar.sexagenaryYear),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.testTag("home_lunar_date"),
            )
            is LunarDay.Unknown -> Text(
                text = stringResource(
                    when (lunar.reason) {
                        LunarDay.Reason.OUT_OF_SUPPORTED_RANGE -> R.string.lunar_out_of_range
                        LunarDay.Reason.ENGINE_UNAVAILABLE -> R.string.lunar_unavailable
                    },
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("home_lunar_date"),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    NepNhaTheme {
        HomeScreen(
            state = HomeUiState(
                today = LocalDate.of(2026, 8, 24),
                lunar = LunarDay.Unknown(LocalDate.of(2026, 8, 24), LunarDay.Reason.ENGINE_UNAVAILABLE),
                familyName = "Gia đình tôi",
            ),
            onSetupFamily = {},
            onOpenMemorials = {},
        )
    }
}
