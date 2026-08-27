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
    version = 3,
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

        /**
         * v2 → v3: thêm liên kết tuỳ chọn `memorials.memberId` → `members.id`.
         *
         * SQLite **không** ALTER TABLE thêm được khoá ngoại, nên phải dựng bảng mới
         * rồi chép sang. Cột mới để `NULL` cho mọi bản ghi cũ — không đoán xem ngày
         * giỗ cũ ứng với thành viên nào; đoán sai còn tệ hơn để trống.
         *
         * `ON DELETE SET NULL`, **không** CASCADE: xoá thành viên chỉ làm đứt liên
         * kết, ngày giỗ vẫn còn nguyên và quay về dùng tên đã lưu.
         */
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(connection: SQLiteConnection) {
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `memorials_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `familyId` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `memberId` INTEGER,
                        `lunarDay` INTEGER NOT NULL,
                        `lunarMonth` INTEGER NOT NULL,
                        `leapMonthPolicy` TEXT NOT NULL,
                        `missingDayPolicy` TEXT NOT NULL,
                        `note` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL,
                        FOREIGN KEY(`familyId`) REFERENCES `families`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE,
                        FOREIGN KEY(`memberId`) REFERENCES `members`(`id`)
                            ON UPDATE NO ACTION ON DELETE SET NULL
                    )
                    """.trimIndent(),
                )
                connection.execSQL(
                    """
                    INSERT INTO `memorials_new`
                        (`id`, `familyId`, `name`, `memberId`, `lunarDay`, `lunarMonth`,
                         `leapMonthPolicy`, `missingDayPolicy`, `note`, `createdAt`, `updatedAt`)
                    SELECT `id`, `familyId`, `name`, NULL, `lunarDay`, `lunarMonth`,
                           `leapMonthPolicy`, `missingDayPolicy`, `note`, `createdAt`, `updatedAt`
                    FROM `memorials`
                    """.trimIndent(),
                )
                connection.execSQL("DROP TABLE `memorials`")
                connection.execSQL("ALTER TABLE `memorials_new` RENAME TO `memorials`")
                connection.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_memorials_familyId` ON `memorials` (`familyId`)",
                )
                connection.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_memorials_memberId` ON `memorials` (`memberId`)",
                )
            }
        }

        fun create(context: Context): NepNhaDatabase =
            Room.databaseBuilder(context.applicationContext, NepNhaDatabase::class.java, NAME)
                // Bật ràng buộc khoá ngoại: xoá Family phải kéo theo Member.
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .build()
    }
}
