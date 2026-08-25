package com.nepnha.ui.family

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.nepnha.R
import com.nepnha.domain.model.Gender
import com.nepnha.domain.model.MemberFormError
import com.nepnha.domain.model.MemberFormInput

/**
 * Biểu mẫu thành viên — dùng chung cho thêm mới và sửa.
 *
 * Chỉ **họ và tên** là bắt buộc. Ngày tháng nhập bằng ba ô số riêng thay vì
 * DatePicker: cụ sinh năm 1940 mà phải cuộn lịch về 85 năm trước thì khổ hơn gõ
 * "1940" rất nhiều.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberEditorScreen(
    state: MemberEditorUiState,
    onInputChange: ((MemberFormInput) -> MemberFormInput) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val input = state.input
    Scaffold(
        modifier = modifier.testTag("screen_member_editor"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(
                            if (state.isEditing) R.string.member_edit_title else R.string.member_new_title,
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
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            OutlinedTextField(
                value = input.fullName,
                onValueChange = { v -> onInputChange { it.copy(fullName = v) } },
                label = { Text(stringResource(R.string.member_full_name_required)) },
                singleLine = true,
                isError = MemberFormError.NAME_REQUIRED in state.errors,
                supportingText = {
                    if (MemberFormError.NAME_REQUIRED in state.errors) {
                        Text(stringResource(R.string.error_name_required))
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("field_full_name"),
            )

            GenderPicker(
                selected = input.gender,
                onSelect = { g -> onInputChange { it.copy(gender = g) } },
            )

            DateFields(
                title = stringResource(R.string.member_solar_birth),
                hint = stringResource(R.string.member_optional_hint),
                day = input.solarDay,
                month = input.solarMonth,
                year = input.solarYear,
                onDay = { v -> onInputChange { it.copy(solarDay = v) } },
                onMonth = { v -> onInputChange { it.copy(solarMonth = v) } },
                onYear = { v -> onInputChange { it.copy(solarYear = v) } },
                errorText = when {
                    MemberFormError.SOLAR_DATE_INCOMPLETE in state.errors ->
                        stringResource(R.string.error_solar_incomplete)
                    MemberFormError.SOLAR_DATE_INVALID in state.errors ->
                        stringResource(R.string.error_solar_invalid)
                    else -> null
                },
                tagPrefix = "solar",
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DateFields(
                    title = stringResource(R.string.member_lunar_birth),
                    // Nói rõ đây là ngày người dùng khai, KHÔNG phải kết quả quy đổi.
                    hint = stringResource(R.string.member_lunar_birth_hint),
                    day = input.lunarDay,
                    month = input.lunarMonth,
                    year = input.lunarYear,
                    onDay = { v -> onInputChange { it.copy(lunarDay = v) } },
                    onMonth = { v -> onInputChange { it.copy(lunarMonth = v) } },
                    onYear = { v -> onInputChange { it.copy(lunarYear = v) } },
                    errorText = when {
                        MemberFormError.LUNAR_DATE_INCOMPLETE in state.errors ->
                            stringResource(R.string.error_lunar_incomplete)
                        MemberFormError.LUNAR_DATE_INVALID in state.errors ->
                            stringResource(R.string.error_lunar_invalid)
                        else -> null
                    },
                    tagPrefix = "lunar",
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = input.lunarIsLeapMonth,
                        onCheckedChange = { v -> onInputChange { it.copy(lunarIsLeapMonth = v) } },
                        modifier = Modifier.testTag("check_leap_month"),
                    )
                    Text(
                        text = stringResource(R.string.member_leap_month),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            }

            OutlinedTextField(
                value = input.role,
                onValueChange = { v -> onInputChange { it.copy(role = v) } },
                label = { Text(stringResource(R.string.member_role)) },
                placeholder = { Text(stringResource(R.string.member_role_hint)) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("field_role"),
            )

            OutlinedTextField(
                value = input.note,
                onValueChange = { v -> onInputChange { it.copy(note = v) } },
                label = { Text(stringResource(R.string.member_note)) },
                minLines = 2,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("field_note"),
            )

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .testTag("btn_save_member"),
            ) {
                Text(
                    text = stringResource(R.string.member_save),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenderPicker(selected: Gender, onSelect: (Gender) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.member_gender),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            GenderChip(Gender.MALE, R.string.member_gender_male, selected, onSelect)
            GenderChip(Gender.FEMALE, R.string.member_gender_female, selected, onSelect)
            GenderChip(Gender.UNSPECIFIED, R.string.member_gender_unspecified, selected, onSelect)
        }
    }
}

@Composable
private fun GenderChip(
    gender: Gender,
    labelRes: Int,
    selected: Gender,
    onSelect: (Gender) -> Unit,
) {
    FilterChip(
        selected = selected == gender,
        onClick = { onSelect(gender) },
        label = { Text(stringResource(labelRes)) },
        modifier = Modifier.testTag("chip_gender_${gender.name}"),
    )
}

@Composable
private fun DateFields(
    title: String,
    hint: String,
    day: String,
    month: String,
    year: String,
    onDay: (String) -> Unit,
    onMonth: (String) -> Unit,
    onYear: (String) -> Unit,
    errorText: String?,
    tagPrefix: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(
            text = hint,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            NumberField(day, onDay, R.string.member_field_day, "field_${tagPrefix}_day", Modifier.weight(1f))
            NumberField(month, onMonth, R.string.member_field_month, "field_${tagPrefix}_month", Modifier.weight(1f))
            NumberField(year, onYear, R.string.member_field_year, "field_${tagPrefix}_year", Modifier.weight(1.4f))
        }
        errorText?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    labelRes: Int,
    tag: String,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = value,
        // Lọc ngay tại nguồn: bàn phím số vẫn có thể gửi ký tự lạ.
        onValueChange = { raw -> onValueChange(raw.filter(Char::isDigit).take(4)) },
        label = { Text(stringResource(labelRes)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier.testTag(tag),
    )
}
