package com.nepnha.data

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nepnha.data.db.NepNhaDatabase
import java.io.File
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Nâng cấp cơ sở dữ liệu v1 → v2 **không được làm mất dữ liệu người dùng**.
 *
 * `docs/ROOM_SCHEMA.md` và `NepNhaDatabase` đã cấm `fallbackToDestructiveMigration`:
 * gia phả và ngày giỗ là dữ liệu thật, cố ý không có bản sao trên cloud. Mất là mất
 * hẳn. Test này là thứ chứng minh điều đó đúng chứ không chỉ là lời hứa trong tài liệu.
 *
 * Cố ý **không** dùng `androidx.room:room-testing`: dựng thẳng một file SQLite phiên
 * bản 1 rồi mở bằng Room là đủ, và Room tự đối chiếu schema khi mở — sai một cột là
 * nó ném ngay. Thêm một dependency chỉ để có `MigrationTestHelper` là không đáng.
 */
@RunWith(AndroidJUnit4::class)
class MemorialMigrationTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val dbFile: File = context.getDatabasePath("migration_test.db")

    /** Hash định danh schema v1, lấy từ `app/schemas/.../1.json`. */
    private val v1IdentityHash = "739e9dfaa318142ce23f742509a27304"

    @After
    fun tearDown() {
        dbFile.delete()
        File("${dbFile.path}-wal").delete()
        File("${dbFile.path}-shm").delete()
    }

    /**
     * Sai thì: người dùng cập nhật app và mất sạch gia phả đã nhập ở Phase 2 — hoặc
     * app crash ngay lúc mở vì schema không khớp.
     */
    @Test
    fun nang_cap_v1_len_v2_giu_nguyen_du_lieu_cu() {
        dbFile.parentFile?.mkdirs()
        dbFile.delete()

        // --- dựng một database v1 đúng như bản đã phát hành ---
        SQLiteDatabase.openOrCreateDatabase(dbFile, null).use { db ->
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `families` (`id` INTEGER PRIMARY KEY AUTOINCREMENT " +
                    "NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL)",
            )
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `members` (`id` INTEGER PRIMARY KEY AUTOINCREMENT " +
                    "NOT NULL, `familyId` INTEGER NOT NULL, `fullName` TEXT NOT NULL, " +
                    "`gender` TEXT NOT NULL, `solarBirthDate` TEXT, `lunarBirthDay` INTEGER, " +
                    "`lunarBirthMonth` INTEGER, `lunarBirthYear` INTEGER, " +
                    "`lunarBirthIsLeapMonth` INTEGER NOT NULL, `lunarBirthSource` TEXT, " +
                    "`role` TEXT, `note` TEXT, `createdAt` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL, FOREIGN KEY(`familyId`) " +
                    "REFERENCES `families`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_members_familyId` ON `members` (`familyId`)")
            // Room nhận diện phiên bản schema qua bảng này; thiếu nó thì Room coi
            // database là hỏng và từ chối mở.
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS room_master_table " +
                    "(id INTEGER PRIMARY KEY, identity_hash TEXT)",
            )
            db.execSQL(
                "INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES (42, ?)",
                arrayOf(v1IdentityHash),
            )
            db.execSQL("INSERT INTO families (name, createdAt, updatedAt) VALUES ('Gia đình tôi', 1, 1)")
            db.execSQL(
                "INSERT INTO members (familyId, fullName, gender, lunarBirthIsLeapMonth, " +
                    "createdAt, updatedAt) VALUES (1, 'Cụ ông', 'MALE', 0, 1, 1)",
            )
            db.version = 1
        }

        // --- mở bằng Room v2 kèm migration thật ---
        val db = Room.databaseBuilder(context, NepNhaDatabase::class.java, dbFile.absolutePath)
            .addMigrations(NepNhaDatabase.MIGRATION_1_2)
            .build()

        try {
            runBlocking {
                // Dữ liệu cũ còn nguyên.
                assertEquals(1, db.familyDao().count())
                val familyId = db.familyDao().firstId()
                assertNotNull(familyId)
                assertEquals(1, db.memberDao().countByFamily(familyId!!))

                // Bảng mới dùng được ngay.
                val id = db.memorialDao().insert(
                    com.nepnha.data.db.MemorialEntity(
                        familyId = familyId,
                        name = "Cụ bà",
                        lunarDay = 30,
                        lunarMonth = 7,
                        leapMonthPolicy = "COMMON_MONTH_DEFAULT",
                        missingDayPolicy = "LAST_VALID_DAY_OF_MONTH",
                        note = null,
                        createdAt = 1,
                        updatedAt = 1,
                    ),
                )
                assertEquals("Cụ bà", db.memorialDao().getById(id)?.name)
            }
        } finally {
            db.close()
        }
    }
}
