package com.nepnha.ui.memorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nepnha.AppContainer
import com.nepnha.domain.event.MemorialDateResolver
import com.nepnha.domain.event.UpcomingMemorial
import com.nepnha.domain.model.displayName
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Danh sách ngày giỗ, mỗi mục kèm **lần kế tiếp** đã quy đổi sẵn.
 *
 * Việc quy đổi làm ở đây chứ không trong Composable: quy tắc tháng nhuận và ngày
 * thiếu là nghiệp vụ, không phải chuyện trình bày.
 */
class MemorialListViewModel(
    private val container: AppContainer,
) : ViewModel() {

    private val resolver: MemorialDateResolver = container.memorialResolver
    private val todayFlow = MutableStateFlow(container.dateProvider.today())

    val state: StateFlow<MemorialListUiState> = combine(
        container.memorials.observe(),
        container.familyOverview.observe(),
        todayFlow,
    ) { list, overview, today ->
        MemorialListUiState(
            items = resolver.upcoming(list, today) { it.displayName(overview.members) },
            isLoaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MemorialListUiState())

    /** Đọc lại ngày khi màn hình quay lại tiền cảnh. */
    fun refreshToday() {
        todayFlow.value = container.dateProvider.today()
    }

    fun delete(id: Long) {
        viewModelScope.launch { container.memorialRepository.delete(id) }
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            viewModelFactory { initializer { MemorialListViewModel(container) } }
    }
}

data class MemorialListUiState(
    val items: List<UpcomingMemorial> = emptyList(),
    val isLoaded: Boolean = false,
)
