package com.nepnha.data.repository

import com.nepnha.data.prefs.SettingsRepository
import com.nepnha.domain.model.Family
import com.nepnha.domain.model.FamilyMember
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

/**
 * Gộp ba nguồn (gia đình / thành viên / tín chủ) thành một luồng duy nhất.
 *
 * Tồn tại vì cả màn Nhà lẫn màn Gia đình đều cần đúng bộ dữ liệu này — không có nó
 * thì logic combine bị chép hai lần và sẽ lệch nhau vào một ngày nào đó.
 */
class FamilyOverviewSource(
    private val familyRepository: FamilyRepository,
    private val memberRepository: MemberRepository,
    private val settingsRepository: SettingsRepository,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    fun observe(): Flow<FamilyOverview> =
        familyRepository.observeFamily().flatMapLatest { family ->
            if (family == null) {
                flowOf(FamilyOverview())
            } else {
                combine(
                    memberRepository.observeMembers(family.id),
                    settingsRepository.primaryMemberId,
                ) { members, primaryId ->
                    FamilyOverview(
                        family = family,
                        members = members,
                        // Tín chủ đã bị xoá thì coi như chưa chọn — không suy ra người khác.
                        primaryMember = members.firstOrNull { it.id == primaryId },
                        isLoaded = true,
                    )
                }
            }
        }
}

data class FamilyOverview(
    val family: Family? = null,
    val members: List<FamilyMember> = emptyList(),
    val primaryMember: FamilyMember? = null,
    val isLoaded: Boolean = false,
)
