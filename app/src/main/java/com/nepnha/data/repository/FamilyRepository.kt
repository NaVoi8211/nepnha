package com.nepnha.data.repository

import com.nepnha.data.db.FamilyDao
import com.nepnha.data.db.FamilyEntity
import com.nepnha.domain.model.Family
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Ranh giới giữa UI và Room cho dữ liệu gia đình.
 *
 * UI không bao giờ được chạm DAO: Composable không biết Room tồn tại.
 */
class FamilyRepository(
    private val dao: FamilyDao,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val ensureLock = Mutex()

    fun observeFamily(): Flow<Family?> =
        dao.observeFirst().map { entity -> entity?.let { Family(id = it.id, name = it.name) } }

    /**
     * Lần chạy đầu tiên: tạo sẵn một gia đình mặc định để app mở ra là dùng được
     * ngay, không bắt người lớn tuổi phải khai báo gì trước.
     *
     * Idempotent — gọi bao nhiêu lần cũng chỉ có một gia đình. `Mutex` chặn trường
     * hợp hai coroutine cùng thấy database rỗng rồi cùng insert.
     */
    suspend fun ensureDefaultFamily(defaultName: String): Long = ensureLock.withLock {
        dao.firstId() ?: run {
            val timestamp = now()
            dao.insert(
                FamilyEntity(name = defaultName, createdAt = timestamp, updatedAt = timestamp),
            )
        }
    }

    suspend fun rename(familyId: Long, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return // tên rỗng thì giữ nguyên tên cũ, không xoá trắng
        dao.updateName(id = familyId, name = trimmed, updatedAt = now())
    }
}
