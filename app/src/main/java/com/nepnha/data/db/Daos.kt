package com.nepnha.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyDao {

    /** MVP chỉ có một gia đình: lấy bản ghi cũ nhất. */
    @Query("SELECT * FROM families ORDER BY id ASC LIMIT 1")
    fun observeFirst(): Flow<FamilyEntity?>

    @Query("SELECT id FROM families ORDER BY id ASC LIMIT 1")
    suspend fun firstId(): Long?

    @Query("SELECT COUNT(*) FROM families")
    suspend fun count(): Int

    @Insert
    suspend fun insert(family: FamilyEntity): Long

    @Query("UPDATE families SET name = :name, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateName(id: Long, name: String, updatedAt: Long)
}

@Dao
interface MemberDao {

    @Query("SELECT * FROM members WHERE familyId = :familyId ORDER BY createdAt ASC, id ASC")
    fun observeByFamily(familyId: Long): Flow<List<MemberEntity>>

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun getById(id: Long): MemberEntity?

    @Query("SELECT COUNT(*) FROM members WHERE familyId = :familyId")
    suspend fun countByFamily(familyId: Long): Int

    @Insert
    suspend fun insert(member: MemberEntity): Long

    @Update
    suspend fun update(member: MemberEntity)

    @Query("DELETE FROM members WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface MemorialDao {

    @Query("SELECT * FROM memorials WHERE familyId = :familyId ORDER BY lunarMonth ASC, lunarDay ASC, id ASC")
    fun observeByFamily(familyId: Long): Flow<List<MemorialEntity>>

    @Query("SELECT * FROM memorials WHERE id = :id")
    suspend fun getById(id: Long): MemorialEntity?

    @Insert
    suspend fun insert(memorial: MemorialEntity): Long

    @Update
    suspend fun update(memorial: MemorialEntity)

    @Query("DELETE FROM memorials WHERE id = :id")
    suspend fun deleteById(id: Long)
}
