package com.nepnha.ui.memorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nepnha.AppContainer
import com.nepnha.domain.event.LeapMonthPolicy
import com.nepnha.domain.event.MemorialResolution
import com.nepnha.domain.event.ResolvedMemorialDate
import com.nepnha.domain.model.FamilyMember
import com.nepnha.domain.model.Memorial
import com.nepnha.domain.model.MemorialFormError
import com.nepnha.domain.model.MemorialFormInput
import com.nepnha.domain.model.MemorialFormResult
import com.nepnha.domain.model.MemorialFormValidator
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Một ViewModel cho cả **thêm mới** lẫn **sửa** — cùng lý do như `MemberEditorViewModel`.
 *
 * Điểm riêng của màn này: nó **xem trước** ngày giỗ sẽ rơi vào ngày dương nào, ngay
 * khi người dùng còn đang gõ. Nhờ vậy nếu ngày 30 phải lùi về 29, người dùng biết
 * **trước khi lưu**, không phải phát hiện sau.
 */
class MemorialEditorViewModel(
    private val container: AppContainer,
    private val memorialId: Long?,
    private val today: LocalDate = container.dateProvider.today(),
) : ViewModel() {

    private val _state = MutableStateFlow(MemorialEditorUiState(isEditing = memorialId != null))
    val state: StateFlow<MemorialEditorUiState> = _state.asStateFlow()

    init {
        // Danh sách thành viên để người dùng chọn. Không có ai thì biểu mẫu chỉ còn
        // ô nhập tên, đúng như trước Gate 1.
        viewModelScope.launch {
            container.familyOverview.observe().collect { overview ->
                _state.update { it.copy(members = overview.members) }
            }
        }
        if (memorialId != null) {
            viewModelScope.launch {
                container.memorialRepository.get(memorialId)?.let { m ->
                    _state.update { it.copy(input = m.toFormInput()) }
                    refreshPreview()
                }
            }
        }
    }

    /**
     * Chọn một thành viên, hoặc bỏ chọn để quay lại nhập tên tự do.
     *
     * Khi chọn, **chép tên thành viên vào `name`** làm bản chụp: nếu sau này thành
     * viên bị xoá thì ngày giỗ vẫn còn tên chứ không thành bản ghi vô danh.
     */
    fun selectMember(member: FamilyMember?) {
        updateInput { input ->
            if (member == null) input.copy(memberId = null)
            else input.copy(memberId = member.id, name = member.fullName)
        }
    }

    fun updateInput(transform: (MemorialFormInput) -> MemorialFormInput) {
        // Người dùng vừa sửa ⇒ dọn lỗi cũ, không để chữ đỏ bám mãi trên màn hình.
        _state.update { it.copy(input = transform(it.input), errors = emptySet()) }
        refreshPreview()
    }

    /**
     * Xem trước lần giỗ kế tiếp. Chỉ chạy khi biểu mẫu đã hợp lệ — không nháy kết quả
     * lung tung trong lúc người dùng còn đang gõ dở.
     */
    private fun refreshPreview() {
        val result = MemorialFormValidator.validate(_state.value.input)
        if (result !is MemorialFormResult.Valid) {
            _state.update { it.copy(preview = null, previewSkipReason = null) }
            return
        }
        val probe = Memorial(
            id = 0,
            familyId = 0,
            name = result.draft.name,
            lunarDay = result.draft.lunarDay,
            lunarMonth = result.draft.lunarMonth,
            rule = result.draft.rule,
            note = result.draft.note,
        )
        val next = container.memorialResolver.nextOccurrence(probe, today)
        if (next != null) {
            _state.update { it.copy(preview = next, previewSkipReason = null) }
        } else {
            // Không tìm được trong tầm dò — thường là "chỉ tháng nhuận" cho một tháng
            // lâu không nhuận. Phải nói ra chứ không để trống.
            val year = container.lunarCalendar.lunarYearOf(today)
            val reason = year?.let {
                (container.memorialResolver.resolve(probe, it) as? MemorialResolution.Skipped)?.reason
            }
            _state.update { it.copy(preview = null, previewSkipReason = reason) }
        }
    }

    /** [onSaved] chỉ được gọi khi đã ghi xuống Room xong. */
    fun save(onSaved: () -> Unit) {
        when (val result = MemorialFormValidator.validate(_state.value.input)) {
            is MemorialFormResult.Invalid -> _state.update { it.copy(errors = result.errors) }
            is MemorialFormResult.Valid -> viewModelScope.launch {
                val familyId = container.familyRepository.observeFamily().first()?.id ?: return@launch
                if (memorialId == null) {
                    container.memorialRepository.add(familyId, result.draft)
                } else {
                    container.memorialRepository.update(memorialId, result.draft)
                }
                onSaved()
            }
        }
    }

    fun delete(onDeleted: () -> Unit) {
        val id = memorialId ?: return
        viewModelScope.launch {
            container.memorialRepository.delete(id)
            onDeleted()
        }
    }

    companion object {
        fun factory(container: AppContainer, memorialId: Long?): ViewModelProvider.Factory =
            viewModelFactory { initializer { MemorialEditorViewModel(container, memorialId) } }
    }
}

data class MemorialEditorUiState(
    val input: MemorialFormInput = MemorialFormInput(),
    /** Thành viên trong nhà, để chọn thay vì gõ tay. */
    val members: List<FamilyMember> = emptyList(),
    val errors: Set<MemorialFormError> = emptySet(),
    val isEditing: Boolean = false,
    /** Lần giỗ kế tiếp nếu tính được — dùng để xem trước và cảnh báo điều chỉnh. */
    val preview: ResolvedMemorialDate? = null,
    /** Vì sao không tính được lần nào, khi [preview] là `null`. */
    val previewSkipReason: MemorialResolution.Reason? = null,
)

private fun Memorial.toFormInput() = MemorialFormInput(
    name = name,
    memberId = memberId,
    lunarDay = lunarDay.toString(),
    lunarMonth = lunarMonth.toString(),
    leapMonthPolicy = rule.leapMonthPolicy,
    missingDayPolicy = rule.missingDayPolicy,
    note = note.orEmpty(),
)

/** Người dùng chỉ thấy hai câu hỏi: có phải tháng nhuận không, và nếu năm không nhuận thì sao. */
val LeapMonthPolicy.isLeapChoice: Boolean get() = this != LeapMonthPolicy.COMMON_MONTH_DEFAULT
