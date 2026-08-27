package com.nepnha.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.nepnha.AppContainer
import com.nepnha.data.repository.FamilyOverview
import com.nepnha.domain.calendar.LunarDay
import com.nepnha.domain.event.UpcomingMemorial
import com.nepnha.domain.model.displayName
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Màn Nhà: dữ liệu gia đình thật từ Room, cộng ngày âm thật từ engine lịch.
 *
 * Ngày âm được tính **ở đây**, không phải trong Composable: màn hình chỉ được nhận
 * kết quả đã xong. Nhờ vậy test đo được mà không cần dựng UI, và không có đường nào
 * để giao diện lỡ tay tự suy ra tháng nhuận.
 *
 * [today] nhận qua tham số để test tiêm được ngày cố định. Ngày được chốt **một lần**
 * lúc tạo ViewModel — app mở qua nửa đêm sẽ chưa tự đổi ngày; đó là hành vi đã biết,
 * chưa cần bộ đếm cho tới khi có nhắc việc theo giờ.
 */
class HomeViewModel(
    private val container: AppContainer,
) : ViewModel() {

    /**
     * "Hôm nay" là **state**, không phải hằng số chốt lúc khởi tạo. Đọc lại khi màn
     * hình quay lại tiền cảnh — xem [refreshToday].
     */
    private val todayFlow = MutableStateFlow(container.dateProvider.today())

    val state: StateFlow<HomeUiState> = combine(
        container.familyOverview.observe(),
        container.memorials.observe(),
        todayFlow,
    ) { overview, memorials, today ->
        overview.toHomeState(
            HomeUiState(today = today, lunar = container.lunarCalendar.dayOf(today)),
        ).copy(
            // Chỉ vài mục gần nhất — màn Nhà là nơi trả lời "hôm nay nhà mình có việc
            // gì", không phải danh sách đầy đủ.
            upcoming = container.memorialResolver
                .upcoming(memorials, today) { it.displayName(overview.members) }
                .take(3),
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeUiState(
            today = container.dateProvider.today(),
            lunar = container.lunarCalendar.dayOf(container.dateProvider.today()),
        ),
    )

    /**
     * Đọc lại ngày. Gọi khi màn hình quay lại tiền cảnh; không làm gì nếu ngày chưa
     * đổi, nên gọi thừa cũng vô hại.
     */
    fun refreshToday() {
        todayFlow.value = container.dateProvider.today()
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory = viewModelFactory {
            initializer { HomeViewModel(container) }
        }
    }
}

data class HomeUiState(
    val today: LocalDate,
    val lunar: LunarDay,
    val upcoming: List<UpcomingMemorial> = emptyList(),
    val familyName: String? = null,
    val memberCount: Int = 0,
    val primaryMemberName: String? = null,
)

private fun FamilyOverview.toHomeState(base: HomeUiState) = base.copy(
    familyName = family?.name,
    memberCount = members.size,
    primaryMemberName = primaryMember?.fullName,
)
