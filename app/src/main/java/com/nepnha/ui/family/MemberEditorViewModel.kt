package com.nepnha.ui.family

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nepnha.AppContainer
import com.nepnha.domain.model.FamilyMember
import com.nepnha.domain.model.MemberFormError
import com.nepnha.domain.model.MemberFormInput
import com.nepnha.domain.model.MemberFormResult
import com.nepnha.domain.model.MemberFormValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Một ViewModel cho cả **thêm mới** lẫn **sửa**: cùng biểu mẫu, cùng luật kiểm tra,
 * khác nhau đúng chỗ có `memberId` hay không. Tách thành hai màn hình chỉ để "rõ
 * ràng" sẽ tạo ra hai bản sao của cùng một logic.
 */
class MemberEditorViewModel(
    private val container: AppContainer,
    private val memberId: Long?,
) : ViewModel() {

    private val _state = MutableStateFlow(MemberEditorUiState(isEditing = memberId != null))
    val state: StateFlow<MemberEditorUiState> = _state.asStateFlow()

    init {
        if (memberId != null) {
            viewModelScope.launch {
                container.memberRepository.getMember(memberId)?.let { member ->
                    _state.update { it.copy(input = member.toFormInput(), isLoading = false) }
                } ?: _state.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateInput(transform: (MemberFormInput) -> MemberFormInput) {
        // Người dùng vừa sửa ⇒ dọn lỗi cũ, không để chữ đỏ bám mãi trên màn hình.
        _state.update { it.copy(input = transform(it.input), errors = emptySet()) }
    }

    /** [onSaved] chỉ được gọi khi đã ghi xuống Room xong. */
    fun save(onSaved: () -> Unit) {
        when (val result = MemberFormValidator.validate(_state.value.input)) {
            is MemberFormResult.Invalid -> _state.update { it.copy(errors = result.errors) }
            is MemberFormResult.Valid -> viewModelScope.launch {
                val familyId = container.familyRepository.observeFamily().first()?.id ?: return@launch
                if (memberId == null) {
                    container.memberRepository.add(familyId, result.draft)
                } else {
                    container.memberRepository.update(memberId, result.draft)
                }
                onSaved()
            }
        }
    }

    companion object {
        fun factory(container: AppContainer, memberId: Long?): ViewModelProvider.Factory =
            viewModelFactory { initializer { MemberEditorViewModel(container, memberId) } }
    }
}

data class MemberEditorUiState(
    val input: MemberFormInput = MemberFormInput(),
    val errors: Set<MemberFormError> = emptySet(),
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
)

private fun FamilyMember.toFormInput() = MemberFormInput(
    fullName = fullName,
    gender = gender,
    solarDay = solarBirthDate?.dayOfMonth?.toString().orEmpty(),
    solarMonth = solarBirthDate?.monthValue?.toString().orEmpty(),
    solarYear = solarBirthDate?.year?.toString().orEmpty(),
    lunarDay = lunarBirthDate?.day?.toString().orEmpty(),
    lunarMonth = lunarBirthDate?.month?.toString().orEmpty(),
    lunarYear = lunarBirthDate?.year?.toString().orEmpty(),
    lunarIsLeapMonth = lunarBirthDate?.isLeapMonth == true,
    role = role.orEmpty(),
    note = note.orEmpty(),
)
