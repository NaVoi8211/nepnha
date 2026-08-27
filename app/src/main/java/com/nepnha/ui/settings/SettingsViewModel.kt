package com.nepnha.ui.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nepnha.AppContainer
import com.nepnha.domain.backup.BackupCodec
import com.nepnha.domain.backup.BackupError
import com.nepnha.domain.backup.BackupFile
import com.nepnha.domain.backup.BackupResult
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Xuất/nhập dữ liệu cục bộ.
 *
 * Luồng nhập **bắt buộc** đi đủ các bước, không được rút gọn:
 * ```
 * file → đọc → phân tích → kiểm tra TOÀN BỘ → xem trước → người dùng xác nhận
 *      → MỘT giao dịch → commit
 * ```
 * Hỏng ở bất kỳ bước nào trước `commit` thì database **không bị chạm tới một dòng nào**.
 *
 * Không ghi log nội dung file, tên người, hay ngày giỗ — xem hợp đồng §J.
 */
class SettingsViewModel(
    private val container: AppContainer,
    private val resolver: ContentResolver,
    private val appVersionName: String?,
    private val nowIso: () -> String = { Instant.now().toString() },
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsUiState())
    val state: StateFlow<SettingsUiState> = _state.asStateFlow()

    /** Tên file gợi ý cho bộ chọn của Android. */
    fun suggestedFileName(): String = "nepnha-${nowIso().take(10)}.json"

    fun export(uri: Uri) {
        val repo = container.backupRepository
        _state.update { it.copy(busy = true, message = null) }
        viewModelScope.launch {
            val ok = runCatching {
                val data = repo.readAll()
                val text = BackupCodec.encode(data, nowIso(), appVersionName)
                withContext(Dispatchers.IO) {
                    resolver.openOutputStream(uri, "wt")?.use { it.write(text.toByteArray(Charsets.UTF_8)) }
                        ?: error("không mở được file để ghi")
                }
                data
            }.getOrNull()
            _state.update {
                it.copy(
                    busy = false,
                    message = if (ok == null) {
                        SettingsMessage.ExportFailed
                    } else {
                        SettingsMessage.ExportDone(ok.members.size, ok.memorials.size)
                    },
                )
            }
        }
    }

    /** Đọc và kiểm tra file. **Không** ghi gì vào database ở bước này. */
    fun prepareImport(uri: Uri) {
        _state.update { it.copy(busy = true, message = null, preview = null) }
        viewModelScope.launch {
            val text = runCatching {
                withContext(Dispatchers.IO) {
                    resolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                }
            }.getOrNull()
            if (text == null) {
                _state.update { it.copy(busy = false, message = SettingsMessage.ImportUnreadable) }
                return@launch
            }
            when (val result = BackupCodec.decode(text)) {
                is BackupResult.Invalid ->
                    _state.update { it.copy(busy = false, message = SettingsMessage.ImportInvalid(result.errors)) }
                is BackupResult.Valid -> {
                    val hasPrimary = container.settingsRepository.primaryMemberId.first() != null
                    _state.update {
                        it.copy(
                            busy = false,
                            preview = ImportPreview(
                                file = result.file,
                                memberCount = result.file.data.members.size,
                                memorialCount = result.file.data.memorials.size,
                                willApplyPrimaryMember =
                                    result.file.data.primaryMemberRef != null && !hasPrimary,
                            ),
                        )
                    }
                }
            }
        }
    }

    fun cancelImport() {
        _state.update { it.copy(preview = null) }
    }

    /** Chỉ chạy sau khi người dùng đã xem trước và xác nhận. */
    fun confirmImport() {
        val preview = _state.value.preview ?: return
        val repo = container.backupRepository
        _state.update { it.copy(busy = true, preview = null) }
        viewModelScope.launch {
            val outcome = runCatching { repo.importAdditive(preview.file.data) }.getOrNull()
            _state.update {
                it.copy(
                    busy = false,
                    message = if (outcome == null) {
                        SettingsMessage.ImportFailed
                    } else {
                        SettingsMessage.ImportDone(outcome.membersAdded, outcome.memorialsAdded)
                    },
                )
            }
        }
    }

    fun dismissMessage() {
        _state.update { it.copy(message = null) }
    }

    companion object {
        fun factory(
            container: AppContainer,
            resolver: ContentResolver,
            appVersionName: String?,
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { SettingsViewModel(container, resolver, appVersionName) }
        }
    }
}

data class SettingsUiState(
    val busy: Boolean = false,
    val preview: ImportPreview? = null,
    val message: SettingsMessage? = null,
)

/** Những gì người dùng thấy **trước khi** bất cứ thứ gì được ghi. */
data class ImportPreview(
    val file: BackupFile,
    val memberCount: Int,
    val memorialCount: Int,
    val willApplyPrimaryMember: Boolean,
)

sealed interface SettingsMessage {
    data class ExportDone(val members: Int, val memorials: Int) : SettingsMessage
    data object ExportFailed : SettingsMessage
    data object ImportUnreadable : SettingsMessage
    data class ImportInvalid(val errors: List<BackupError>) : SettingsMessage
    data class ImportDone(val members: Int, val memorials: Int) : SettingsMessage
    data object ImportFailed : SettingsMessage
}
