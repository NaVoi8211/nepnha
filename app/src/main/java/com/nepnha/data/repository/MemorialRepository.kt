package com.nepnha.data.repository

import com.nepnha.data.db.MemorialDao
import com.nepnha.data.db.MemorialEntity
import com.nepnha.domain.event.LeapMonthPolicy
import com.nepnha.domain.event.MemorialRule
import com.nepnha.domain.event.MissingDayPolicy
import com.nepnha.domain.model.Memorial
import com.nepnha.domain.model.MemorialDraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Ranh giới giữa UI và Room cho ngày giỗ.
 *
 * Repository giữ **nguyên văn** ngày âm người dùng nhập. Không có đường nào ở đây
 * ghi đè `lunarDay`: việc lùi 30 về 29 là giá trị dẫn xuất cho một năm cụ thể, do
 * `MemorialDateResolver` tính, và không bao giờ được ghi ngược xuống DB.
 */
class MemorialRepository(
    private val dao: MemorialDao,
    private val now: () -> Long = System::currentTimeMillis,
) {

    fun observe(familyId: Long): Flow<List<Memorial>> =
        dao.observeByFamily(familyId).map { list -> list.map { it.toDomain() } }

    suspend fun get(id: Long): Memorial? = dao.getById(id)?.toDomain()

    suspend fun add(familyId: Long, draft: MemorialDraft): Long {
        val t = now()
        return dao.insert(draft.toEntity(id = 0, familyId = familyId, createdAt = t, updatedAt = t))
    }

    /** Sửa. Giữ nguyên `createdAt` của bản ghi cũ. */
    suspend fun update(id: Long, draft: MemorialDraft) {
        val existing = dao.getById(id) ?: return
        dao.update(
            draft.toEntity(
                id = existing.id,
                familyId = existing.familyId,
                createdAt = existing.createdAt,
                updatedAt = now(),
            ),
        )
    }

    suspend fun delete(id: Long) = dao.deleteById(id)
}

private fun MemorialEntity.toDomain(): Memorial = Memorial(
    id = id,
    familyId = familyId,
    name = name,
    memberId = memberId,
    lunarDay = lunarDay,
    lunarMonth = lunarMonth,
    rule = MemorialRule(
        // Giá trị lạ trong DB (bản cũ, sửa tay) quay về mặc định đã chốt ở Phase 0
        // thay vì làm sập app.
        leapMonthPolicy = LeapMonthPolicy.entries.firstOrNull { it.name == leapMonthPolicy }
            ?: LeapMonthPolicy.COMMON_MONTH_DEFAULT,
        missingDayPolicy = MissingDayPolicy.entries.firstOrNull { it.name == missingDayPolicy }
            ?: MissingDayPolicy.LAST_VALID_DAY_OF_MONTH,
    ),
    note = note,
)

private fun MemorialDraft.toEntity(
    id: Long,
    familyId: Long,
    createdAt: Long,
    updatedAt: Long,
): MemorialEntity = MemorialEntity(
    id = id,
    familyId = familyId,
    name = name,
    memberId = memberId,
    lunarDay = lunarDay,
    lunarMonth = lunarMonth,
    leapMonthPolicy = rule.leapMonthPolicy.name,
    missingDayPolicy = rule.missingDayPolicy.name,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
