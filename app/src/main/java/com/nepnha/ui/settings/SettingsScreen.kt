package com.nepnha.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.nepnha.R
import com.nepnha.domain.backup.BackupError
import com.nepnha.ui.components.InfoCard
import com.nepnha.ui.theme.NepNhaTheme

/**
 * Cài đặt tối giản: giới thiệu, sao lưu, nguồn lịch, phiên bản.
 *
 * Không tài khoản, không đồng bộ, không "sao lưu đám mây" — đó là chủ đích sản phẩm
 * chứ không phải tính năng còn thiếu, nên mục "Dữ liệu của bạn" nói thẳng điều đó, và
 * mục sao lưu nói thẳng rằng file xuất ra **không mã hoá**.
 *
 * Chọn chỗ lưu và chọn file đều qua **bộ chọn file của Android** (Storage Access
 * Framework): app không xin quyền lưu trữ và chỉ chạm đúng file người dùng chỉ định.
 */
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    versionName: String,
    suggestedFileName: String,
    onExportTo: (android.net.Uri) -> Unit,
    onImportFrom: (android.net.Uri) -> Unit,
    onConfirmImport: () -> Unit,
    onCancelImport: () -> Unit,
    onDismissMessage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json"),
    ) { uri -> uri?.let(onExportTo) }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri -> uri?.let(onImportFrom) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("screen_settings"),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        InfoCard(
            title = stringResource(R.string.settings_about_title),
            body = stringResource(R.string.settings_about_body),
        )
        InfoCard(
            title = stringResource(R.string.settings_privacy_title),
            body = stringResource(R.string.settings_privacy_body),
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            InfoCard(
                title = stringResource(R.string.settings_backup_title),
                body = stringResource(R.string.settings_backup_body),
            )
            if (state.busy) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            Button(
                onClick = { exportLauncher.launch(suggestedFileName) },
                enabled = !state.busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_export"),
            ) { Text(stringResource(R.string.settings_export)) }
            OutlinedButton(
                // Nhiều máy trả kiểu MIME khác nhau cho cùng một file .json, nên nhận
                // rộng rồi để bước phân tích quyết định — từ chối ngay ở bộ chọn chỉ
                // làm người dùng không thấy chính file của mình.
                onClick = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                enabled = !state.busy,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_import"),
            ) { Text(stringResource(R.string.settings_import)) }
        }

        InfoCard(
            title = stringResource(R.string.settings_lunar_title),
            body = stringResource(R.string.settings_lunar_body),
            modifier = Modifier.testTag("settings_lunar_method"),
        )
        InfoCard(
            title = stringResource(R.string.settings_display_title),
            body = stringResource(R.string.settings_display_body),
        )
        InfoCard(
            title = stringResource(R.string.settings_version_title),
            body = versionName,
        )
    }

    state.preview?.let { preview ->
        AlertDialog(
            onDismissRequest = onCancelImport,
            title = { Text(stringResource(R.string.import_preview_title)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        stringResource(
                            R.string.import_preview_counts,
                            preview.memberCount,
                            preview.memorialCount,
                        ),
                    )
                    Text(stringResource(R.string.import_preview_warning))
                    if (preview.willApplyPrimaryMember) {
                        Text(stringResource(R.string.import_preview_primary))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirmImport, modifier = Modifier.testTag("btn_import_confirm")) {
                    Text(stringResource(R.string.import_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelImport) { Text(stringResource(R.string.action_cancel)) }
            },
            modifier = Modifier.testTag("dialog_import_preview"),
        )
    }

    state.message?.let { message ->
        AlertDialog(
            onDismissRequest = onDismissMessage,
            title = { Text(messageTitle(message)) },
            text = { Text(messageBody(message)) },
            confirmButton = {
                TextButton(onClick = onDismissMessage, modifier = Modifier.testTag("btn_message_ok")) {
                    Text(stringResource(R.string.action_ok))
                }
            },
            modifier = Modifier.testTag("dialog_message"),
        )
    }
}

@Composable
private fun messageTitle(message: SettingsMessage): String = when (message) {
    is SettingsMessage.ImportInvalid -> stringResource(R.string.import_invalid_title)
    else -> stringResource(R.string.settings_backup_title)
}

@Composable
private fun messageBody(message: SettingsMessage): String = when (message) {
    is SettingsMessage.ExportDone ->
        stringResource(R.string.export_done, message.members, message.memorials)
    SettingsMessage.ExportFailed -> stringResource(R.string.export_failed)
    SettingsMessage.ImportUnreadable -> stringResource(R.string.import_unreadable)
    SettingsMessage.ImportFailed -> stringResource(R.string.import_failed)
    is SettingsMessage.ImportDone ->
        stringResource(R.string.import_done, message.members, message.memorials)
    is SettingsMessage.ImportInvalid -> message.errors.take(5).joinToString("\n") { describe(it) }
}

/**
 * Mô tả lỗi bằng tiếng Việt thường, kèm **vị trí** trong file.
 *
 * "File không hợp lệ" là câu vô dụng với người đang cầm một file 200 dòng.
 */
private fun describe(error: BackupError): String = when (error) {
    BackupError.EmptyFile -> "File rỗng."
    BackupError.NotJson -> "File này không đúng định dạng Nếp Nhà."
    BackupError.MissingFormatVersion -> "File thiếu thông tin phiên bản."
    is BackupError.UnsupportedFormatVersion ->
        "File được tạo bởi phiên bản Nếp Nhà mới hơn. Hãy cập nhật app rồi thử lại."
    is BackupError.MissingField -> "Thiếu thông tin ở ${error.where}."
    is BackupError.WrongType -> "Sai kiểu dữ liệu ở ${error.where}."
    is BackupError.OutOfRange ->
        "Giá trị ${error.value} ở ${error.where} nằm ngoài khoảng ${error.min}–${error.max}."
    is BackupError.BadEnum ->
        "Giá trị \"${error.value}\" ở ${error.where} không hợp lệ."
    is BackupError.BadDate -> "Ngày \"${error.value}\" ở ${error.where} không có thật."
    is BackupError.TooLong -> "Nội dung ở ${error.where} dài quá ${error.max} ký tự."
    is BackupError.DuplicateRef -> "Có hai thành viên trùng mã ${error.ref} trong file."
    is BackupError.DanglingReference ->
        "Mục ${error.where} trỏ tới thành viên ${error.ref} không có trong file."
    BackupError.ChecksumMismatch ->
        "File có dấu hiệu bị hỏng. Nếp Nhà không nhập để tránh sai ngày giỗ."
}

@Preview(showBackground = true)
@Composable
private fun SettingsScreenPreview() {
    NepNhaTheme {
        SettingsScreen(
            state = SettingsUiState(),
            versionName = "0.1.0-mvp",
            suggestedFileName = "nepnha-2026-08-27.json",
            onExportTo = {}, onImportFrom = {}, onConfirmImport = {},
            onCancelImport = {}, onDismissMessage = {},
        )
    }
}
