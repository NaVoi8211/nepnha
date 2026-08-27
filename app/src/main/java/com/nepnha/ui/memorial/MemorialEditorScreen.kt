package com.nepnha.ui.memorial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import com.nepnha.R
import com.nepnha.core.time.VietnameseDateFormatter
import com.nepnha.domain.event.LeapMonthPolicy
import com.nepnha.domain.event.MemorialResolution
import com.nepnha.domain.event.MissingDayPolicy
import com.nepnha.domain.model.MemorialFormError
import com.nepnha.ui.components.InfoCard

/**
 * Biểu mẫu thêm/sửa ngày giỗ.
 *
 * Nguyên tắc trình bày: **không có thuật ngữ kỹ thuật nào lọt ra màn hình.** Người
 * dùng chỉ thấy hai câu hỏi bằng tiếng Việt thường — "có phải tháng nhuận không" và
 * "nếu năm đó không có thì sao" — còn `LeapMonthPolicy` nằm hẳn trong domain.
 *
 * Ô xem trước ở cuối cho biết ngày giỗ sẽ rơi vào ngày dương nào, **ngay trong lúc
 * gõ**. Nếu ngày 30 phải lùi về 29 thì người dùng biết trước khi lưu.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemorialEditorScreen(
    state: MemorialEditorUiState,
    onInputChange: ((com.nepnha.domain.model.MemorialFormInput) -> com.nepnha.domain.model.MemorialFormInput) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var confirmDelete by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.testTag("screen_memorial_editor"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (state.isEditing) R.string.memorial_edit else R.string.memorial_add,
                        ),
                    )
                },
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
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = state.input.name,
                onValueChange = { v -> onInputChange { it.copy(name = v) } },
                label = { Text(stringResource(R.string.memorial_name)) },
                placeholder = { Text(stringResource(R.string.memorial_name_hint)) },
                isError = MemorialFormError.NAME_REQUIRED in state.errors,
                supportingText = {
                    if (MemorialFormError.NAME_REQUIRED in state.errors) {
                        Text(stringResource(R.string.error_memorial_name))
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("memorial_name"),
            )

            Text(
                text = stringResource(R.string.memorial_lunar_date),
                style = MaterialTheme.typography.titleSmall,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                NumberField(
                    value = state.input.lunarDay,
                    onValueChange = { v -> onInputChange { it.copy(lunarDay = v) } },
                    label = stringResource(R.string.memorial_day),
                    isError = MemorialFormError.DAY_INVALID in state.errors,
                    tag = "memorial_day",
                    modifier = Modifier.weight(1f),
                )
                NumberField(
                    value = state.input.lunarMonth,
                    onValueChange = { v -> onInputChange { it.copy(lunarMonth = v) } },
                    label = stringResource(R.string.memorial_month),
                    isError = MemorialFormError.MONTH_INVALID in state.errors,
                    tag = "memorial_month",
                    modifier = Modifier.weight(1f),
                )
            }
            if (MemorialFormError.DAY_INVALID in state.errors) {
                ErrorText(stringResource(R.string.error_memorial_day))
            }
            if (MemorialFormError.MONTH_INVALID in state.errors) {
                ErrorText(stringResource(R.string.error_memorial_month))
            }

            // --- Tháng nhuận: hỏi dần, không đổ hết ba lựa chọn vào mặt người dùng ---
            LabeledCheckbox(
                checked = state.input.leapMonthPolicy.isLeapChoice,
                label = stringResource(R.string.memorial_leap_checkbox),
                tag = "memorial_leap",
                onCheckedChange = { checked ->
                    onInputChange {
                        it.copy(
                            leapMonthPolicy = if (checked) {
                                LeapMonthPolicy.LEAP_MONTH_PREFERRED
                            } else {
                                LeapMonthPolicy.COMMON_MONTH_DEFAULT
                            },
                        )
                    }
                },
            )
            if (state.input.leapMonthPolicy.isLeapChoice) {
                Column(Modifier.padding(start = 12.dp)) {
                    Text(
                        text = stringResource(R.string.memorial_leap_fallback_title),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    LabeledRadio(
                        selected = state.input.leapMonthPolicy == LeapMonthPolicy.LEAP_MONTH_PREFERRED,
                        label = stringResource(R.string.memorial_leap_fallback_common),
                        tag = "memorial_leap_common",
                    ) { onInputChange { it.copy(leapMonthPolicy = LeapMonthPolicy.LEAP_MONTH_PREFERRED) } }
                    LabeledRadio(
                        selected = state.input.leapMonthPolicy == LeapMonthPolicy.LEAP_MONTH_ONLY,
                        label = stringResource(R.string.memorial_leap_fallback_skip),
                        tag = "memorial_leap_only",
                    ) { onInputChange { it.copy(leapMonthPolicy = LeapMonthPolicy.LEAP_MONTH_ONLY) } }
                }
            }

            // --- Ngày 30 trong tháng thiếu ---
            Text(
                text = stringResource(R.string.memorial_missing_title),
                style = MaterialTheme.typography.titleSmall,
            )
            LabeledRadio(
                selected = state.input.missingDayPolicy == MissingDayPolicy.LAST_VALID_DAY_OF_MONTH,
                label = stringResource(R.string.memorial_missing_last),
                tag = "memorial_missing_last",
            ) { onInputChange { it.copy(missingDayPolicy = MissingDayPolicy.LAST_VALID_DAY_OF_MONTH) } }
            LabeledRadio(
                selected = state.input.missingDayPolicy == MissingDayPolicy.SKIP,
                label = stringResource(R.string.memorial_missing_skip),
                tag = "memorial_missing_skip",
            ) { onInputChange { it.copy(missingDayPolicy = MissingDayPolicy.SKIP) } }

            OutlinedTextField(
                value = state.input.note,
                onValueChange = { v -> onInputChange { it.copy(note = v) } },
                label = { Text(stringResource(R.string.memorial_note)) },
                modifier = Modifier.fillMaxWidth(),
            )

            PreviewCard(state)

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("memorial_save"),
            ) { Text(stringResource(R.string.memorial_save)) }

            if (state.isEditing) {
                TextButton(
                    onClick = { confirmDelete = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("memorial_delete"),
                ) {
                    Text(
                        text = stringResource(R.string.memorial_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.memorial_delete_confirm)) },
            text = { Text(stringResource(R.string.memorial_delete_confirm_body)) },
            confirmButton = {
                TextButton(
                    onClick = { confirmDelete = false; onDelete() },
                    modifier = Modifier.testTag("memorial_delete_confirm"),
                ) {
                    Text(
                        text = stringResource(R.string.memorial_delete),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

/**
 * Xem trước lần giỗ tới. Đây là chỗ **nói thật** với người dùng: nếu ngày bị lùi
 * hoặc tháng nhuận bị thay bằng tháng thường, câu giải thích hiện ngay ở đây chứ
 * không giấu đi.
 */
@Composable
private fun PreviewCard(state: MemorialEditorUiState) {
    val next = state.preview
    when {
        next != null -> {
            // Hai điều chỉnh có thể xảy ra CÙNG LÚC ⇒ nói cả hai, không chọn một.
            val explanation = listOfNotNull(
                if (next.fellBackToCommonMonth) {
                    stringResource(R.string.memorial_adjusted_leap_fallback, next.lunarMonth)
                } else {
                    null
                },
                if (next.dayWasShortened) {
                    stringResource(
                        R.string.memorial_adjusted_missing_day,
                        next.lunarMonth,
                        next.originalLunarDay,
                        next.effectiveLunarDay,
                    )
                } else {
                    null
                },
            ).joinToString("\n\n").takeIf { it.isNotEmpty() }
            InfoCard(
                title = stringResource(R.string.memorial_preview_title),
                body = "${VietnameseDateFormatter.dayOfWeek(next.solarDate)}, " +
                    VietnameseDateFormatter.fullDate(next.solarDate) +
                    (explanation?.let { "\n\n$it" } ?: ""),
                modifier = Modifier.testTag("memorial_preview"),
            )
        }
        state.previewSkipReason == MemorialResolution.Reason.NO_LEAP_MONTH ||
            state.previewSkipReason == MemorialResolution.Reason.MISSING_DAY -> InfoCard(
            title = stringResource(R.string.memorial_preview_title),
            body = stringResource(R.string.memorial_no_occurrence),
            modifier = Modifier.testTag("memorial_preview"),
        )
        else -> Unit
    }
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isError: Boolean,
    tag: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(it.filter(Char::isDigit).take(2)) },
        label = { Text(label) },
        isError = isError,
        singleLine = true,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
        ),
        modifier = modifier.testTag(tag),
    )
}

@Composable
private fun LabeledCheckbox(
    checked: Boolean,
    label: String,
    tag: String,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, onValueChange = onCheckedChange)
            .testTag(tag),
    ) {
        Checkbox(checked = checked, onCheckedChange = null)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun LabeledRadio(selected: Boolean, label: String, tag: String, onSelect: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .selectable(selected = selected, onClick = onSelect)
            .testTag(tag),
    ) {
        RadioButton(selected = selected, onClick = null)
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ErrorText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error,
    )
}
