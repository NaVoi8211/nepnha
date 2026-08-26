package com.nepnha.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

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
    entities = [FamilyEntity::class, MemberEntity::class, MemorialEntity::class],
    version = 2,
    exportSchema = true,
)
abstract class NepNhaDatabase : RoomDatabase() {

    abstract fun familyDao(): FamilyDao
    abstract fun memberDao(): MemberDao
    abstract fun memorialDao(): MemorialDao

    companion object {
        private const val NAME = "nepnha.db"

        /**
         * v1 → v2: thêm bảng `memorials` (Phase 5).
         *
         * Chỉ **thêm** bảng, không đụng `families` và `members` ⇒ dữ liệu gia phả
         * người dùng đã nhập ở Phase 2 còn nguyên sau khi cập nhật app.
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `memorials` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `familyId` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `lunarDay` INTEGER NOT NULL,
                        `lunarMonth` INTEGER NOT NULL,
                        `leapMonthPolicy` TEXT NOT NULL,
                        `missingDayPolicy` TEXT NOT NULL,
                        `note` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        FOREIGN KEY(`familyId`) REFERENCES `families`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent(),
                )
                connection.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_memorials_familyId` ON `memorials` (`familyId`)",
                )
            }
        }

        fun create(context: Context): NepNhaDatabase =
            Room.databaseBuilder(context.applicationContext, NepNhaDatabase::class.java, NAME)
                // Bật ràng buộc khoá ngoại: xoá Family phải kéo theo Member.
                .addMigrations(MIGRATION_1_2)
                .build()
    }
}
