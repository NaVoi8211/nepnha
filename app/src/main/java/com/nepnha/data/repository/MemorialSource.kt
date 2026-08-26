package com.nepnha.data.repository

import com.nepnha.domain.model.Memorial
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * Ngày giỗ của gia đình hiện tại, dưới dạng một luồng duy nhất.
 *
 * Tồn tại vì cả màn Nhà, màn Lịch lẫn màn danh sách đều cần đúng bộ dữ liệu này —
 * không có nó thì logic "lấy gia đình rồi mới lấy ngày giỗ" bị chép ba lần và sẽ
 * lệch nhau vào một ngày nào đó. Cùng lý do với `FamilyOverviewSource`.
 */
class MemorialSource(
    private val familyRepository: FamilyRepository,
    private val memorialRepository: MemorialRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(): Flow<List<Memorial>> =
        familyRepository.observeFamily().flatMapLatest { family ->
            if (family == null) flowOf(emptyList()) else memorialRepository.observe(family.id)
        }
}
