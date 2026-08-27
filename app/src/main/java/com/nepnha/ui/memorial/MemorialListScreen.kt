package com.nepnha.ui.memorial

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nepnha.R
import com.nepnha.core.time.VietnameseDateFormatter
import com.nepnha.domain.event.UpcomingMemorial
import com.nepnha.ui.components.EmptyStateCard

/**
 * Danh sách ngày giỗ, sắp theo lần kế tiếp gần nhất.
 *
 * Màn hình **không tính gì cả**: ngày dương, ngày âm và cờ "đã điều chỉnh" đều do
 * `MemorialListViewModel` chuẩn bị sẵn từ `MemorialDateResolver`.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemorialListScreen(
    state: MemorialListUiState,
    onAdd: () -> Unit,
    onEdit: (Long) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.testTag("screen_memorials"),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.memorial_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                text = { Text(stringResource(R.string.memorial_add)) },
                icon = {},
                modifier = Modifier.testTag("memorial_add"),
            )
        },
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.isLoaded && state.items.isEmpty()) {
                EmptyStateCard(
                    title = stringResource(R.string.memorial_empty_title),
                    body = stringResource(R.string.memorial_empty_body),
                    actionLabel = stringResource(R.string.memorial_add),
                    onAction = onAdd,
                )
            }
            state.items.forEach { item ->
                MemorialRow(item = item, onClick = { onEdit(item.memorial.id) })
            }
            // Chừa chỗ cho nút nổi khỏi che mục cuối.
            Text(text = "", modifier = Modifier.padding(bottom = 56.dp))
        }
    }
}

@Composable
private fun MemorialRow(item: UpcomingMemorial, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("memorial_row_${item.memorial.id}"),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                // Tên phải nhường chỗ cho phần đếm ngược. Không có `weight` thì tên
                // dài bóp cột đếm ngược còn một ký tự và nó xuống dòng theo từng chữ
                // cái — audit trực quan ở 720×1600 đã bắt đúng lỗi này.
                Text(
                    text = item.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                item.daysUntil?.let {
                    Text(
                        text = countdownLabel(it),
                        style = MaterialTheme.typography.labelLarge,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(start = 12.dp),
                    )
                }
            }
            Text(
                text = stringResource(
                    R.string.memorial_lunar_line,
                    lunarDeclaration(item.memorial.lunarDay, item.memorial.lunarMonth, item.memorial.rule.leapMonthPolicy),
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            val next = item.next
            if (next == null) {
                Text(
                    text = stringResource(R.string.memorial_none_upcoming),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            } else {
                Text(
                    text = "${VietnameseDateFormatter.dayOfWeek(next.solarDate)}, " +
                        VietnameseDateFormatter.fullDate(next.solarDate),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (next.wasAdjusted) {
                    Text(
                        text = stringResource(R.string.memorial_adjusted_badge),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.testTag("memorial_adjusted_${item.memorial.id}"),
                    )
                }
            }
        }
    }
}

@Composable
internal fun countdownLabel(days: Long): String = when (days) {
    0L -> stringResource(R.string.memorial_today)
    1L -> stringResource(R.string.memorial_tomorrow)
    else -> stringResource(R.string.memorial_days_until, days)
}
