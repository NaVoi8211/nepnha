package com.nepnha.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.nepnha.core.time.VietnameseDateFormatter
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
 * TRUNG THỰC VỀ DỮ LIỆU: ngày dương là thật (`java.time`), còn ngày âm **không**
 * được bịa. Bộ lịch âm Việt Nam thuộc Phase 3; tới lúc đó chỗ này chỉ nói rõ là
 * chưa có, tuyệt đối không hiển thị một con số trông như thật.
 */
@Composable
fun HomeScreen(
    state: HomeUiState,
    onSetupFamily: () -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("screen_home"),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        TodayHeader(today = today)

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
            EmptyStateCard(
                title = stringResource(R.string.home_no_memorial_title),
                body = stringResource(R.string.home_no_memorial_body),
            )
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

@Composable
private fun TodayHeader(today: LocalDate, modifier: Modifier = Modifier) {
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
        // Chỗ dành cho ngày âm. Chưa có engine ⇒ nói thẳng, không hiển thị số giả.
        Text(
            text = stringResource(R.string.lunar_not_ready),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    NepNhaTheme {
        HomeScreen(
            state = HomeUiState(familyName = "Gia đình tôi"),
            onSetupFamily = {},
            today = LocalDate.of(2026, 8, 24),
        )
    }
}
