package com.nepnha.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nepnha.AppContainer
import com.nepnha.data.repository.FamilyOverview
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Màn Nhà giờ đọc dữ liệu gia đình thật từ Room.
 *
 * Vẫn KHÔNG có nghi lễ/ngày giỗ — đó là Phase 4 và Phase 7.
 */
class HomeViewModel(container: AppContainer) : ViewModel() {

    val state: StateFlow<HomeUiState> = container.familyOverview.observe()
        .map { it.toHomeState() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(container) }
        }
    }
}

data class HomeUiState(
    val familyName: String? = null,
    val memberCount: Int = 0,
    val primaryMemberName: String? = null,
)

private fun FamilyOverview.toHomeState() = HomeUiState(
    familyName = family?.name,
    memberCount = members.size,
    primaryMemberName = primaryMember?.fullName,
)
