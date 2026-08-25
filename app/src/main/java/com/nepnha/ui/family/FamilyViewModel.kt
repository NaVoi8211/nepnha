package com.nepnha.ui.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nepnha.AppContainer
import com.nepnha.data.repository.FamilyOverview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel cho màn Gia đình.
 *
 * Có ViewModel ở đây là **có lý do thật**: màn hình quan sát Room qua `Flow`, thao
 * tác ghi là bất đồng bộ, và state phải sống qua xoay màn hình. Đúng tiêu chí đã
 * thống nhất — không tạo ViewModel chỉ để chứa vài biến UI.
 */
class FamilyViewModel(private val container: AppContainer) : ViewModel() {

    val state: StateFlow<FamilyOverview> = container.familyOverview.observe()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FamilyOverview())

    fun renameFamily(newName: String) {
        val familyId = state.value.family?.id ?: return
        viewModelScope.launch { container.familyRepository.rename(familyId, newName) }
    }

    fun deleteMember(memberId: Long) {
        viewModelScope.launch { container.memberRepository.delete(memberId) }
    }

    /**
     * [onDone] chỉ chạy khi đã ghi xong xuống DataStore.
     *
     * Quan trọng: màn hình chọn tín chủ `popBackStack()` ngay sau khi chọn. Nếu pop
     * trước khi ghi xong thì `viewModelScope` có thể bị huỷ giữa chừng và lựa chọn
     * mất trắng — đã gặp đúng hiện tượng này một lần khi test tay trên A32. Đảo thứ
     * tự lại (ghi xong mới điều hướng) khiến lỗi không thể xảy ra nữa, thay vì chỉ
     * hiếm xảy ra.
     */
    fun setPrimaryMember(memberId: Long?, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            container.settingsRepository.setPrimaryMemberId(memberId)
            onDone()
        }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { FamilyViewModel(container) }
        }
    }
}
