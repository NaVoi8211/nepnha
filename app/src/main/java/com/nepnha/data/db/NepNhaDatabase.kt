package com.nepnha.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Database cục bộ của người dùng.
 *
 * **Không dùng `fallbackToDestructiveMigration()`.** Từ Phase 2 trở đi dữ liệu ở
 * đây là thật: gia phả, ngày giỗ — và cố ý không có bản sao trên cloud. Mất là mất
 * hẳn. Đổi schema thì phải tăng version và viết migration.
 *
 * Schema được export ra `app/schemas/` (bật ở `build.gradle.kts`) và **có commit**,
 * để lần sau còn diff mà viết migration.
 */
@Database(
    entities = [FamilyEntity::class, MemberEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class NepNhaDatabase : RoomDatabase() {

    abstract fun familyDao(): FamilyDao
    abstract fun memberDao(): MemberDao

    companion object {
        private const val NAME = "nepnha.db"

        fun create(context: Context): NepNhaDatabase =
            Room.databaseBuilder(context.applicationContext, NepNhaDatabase::class.java, NAME)
                // Bật ràng buộc khoá ngoại: xoá Family phải kéo theo Member.
                .build()
    }
}
