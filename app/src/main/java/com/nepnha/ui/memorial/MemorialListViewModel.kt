package com.nepnha.ui.memorial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nepnha.AppContainer
import com.nepnha.domain.event.MemorialDateResolver
import com.nepnha.domain.event.UpcomingMemorial
import java.time.LocalDate
import com.nepnha.domain.model.displayName
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
    private val today: LocalDate = LocalDate.now(),
) : ViewModel() {

    private val resolver: MemorialDateResolver = container.memorialResolver

    val state: StateFlow<MemorialListUiState> = combine(
        container.memorials.observe(),
        container.familyOverview.observe(),
    ) { list, overview ->
        MemorialListUiState(
            items = resolver.upcoming(list, today) { it.displayName(overview.members) },
            isLoaded = true,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MemorialListUiState())

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
