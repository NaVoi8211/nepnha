package com.nepnha.data

import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nepnha.data.db.NepNhaDatabase
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
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

        // Mở bằng Room ở phiên bản HIỆN TẠI, đăng ký đủ chuỗi migration. Database
        // đã lên v3 nên chỉ có MIGRATION_1_2 là Room không tìm được đường đi.
        val db = Room.databaseBuilder(context, NepNhaDatabase::class.java, dbFile.absolutePath)
            .addMigrations(NepNhaDatabase.MIGRATION_1_2, NepNhaDatabase.MIGRATION_2_3)
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
                // Đi qua cả 1→2 lẫn 2→3: cột mới của v3 phải có mặt và để trống.
                assertNull(db.memorialDao().getById(id)?.memberId)
            }
        } finally {
            db.close()
        }
    }
}

/**
 * Nâng cấp v2 → v3: thêm liên kết tuỳ chọn `memorials.memberId`.
 *
 * SQLite không ALTER TABLE thêm được khoá ngoại nên migration phải dựng bảng mới rồi
 * chép sang — đúng chỗ dễ làm mất dữ liệu nhất. Test này là thứ chứng minh không mất.
 */
@RunWith(AndroidJUnit4::class)
class MemorialMigrationV2V3Test {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val dbFile: java.io.File = context.getDatabasePath("migration_v3_test.db")

    /** Hash schema v2, lấy từ `app/schemas/.../2.json`. */
    private val v2IdentityHash = "cb525de22f005aad0a15b53d3705aeb9"

    @org.junit.After
    fun tearDown() {
        dbFile.delete()
        java.io.File("${dbFile.path}-wal").delete()
        java.io.File("${dbFile.path}-shm").delete()
    }

    /**
     * Sai thì: người dùng cập nhật app và mất ngày giỗ đã nhập, hoặc app crash vì
     * schema không khớp.
     */
    @Test
    fun nang_cap_v2_len_v3_giu_nguyen_ngay_gio() {
        dbFile.parentFile?.mkdirs(); dbFile.delete()

        android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(dbFile, null).use { db ->
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
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `memorials` (`id` INTEGER PRIMARY KEY AUTOINCREMENT " +
                    "NOT NULL, `familyId` INTEGER NOT NULL, `name` TEXT NOT NULL, " +
                    "`lunarDay` INTEGER NOT NULL, `lunarMonth` INTEGER NOT NULL, " +
                    "`leapMonthPolicy` TEXT NOT NULL, `missingDayPolicy` TEXT NOT NULL, " +
                    "`note` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`familyId`) REFERENCES `families`(`id`) " +
                    "ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_memorials_familyId` ON `memorials` (`familyId`)")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)",
            )
            db.execSQL(
                "INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES (42, ?)",
                arrayOf(v2IdentityHash),
            )
            db.execSQL("INSERT INTO families (name, createdAt, updatedAt) VALUES ('Gia đình tôi', 1, 1)")
            db.execSQL(
                "INSERT INTO memorials (familyId, name, lunarDay, lunarMonth, leapMonthPolicy, " +
                    "missingDayPolicy, note, createdAt, updatedAt) VALUES " +
                    "(1, 'Cụ ông', 30, 7, 'LEAP_MONTH_ONLY', 'SKIP', 'ghi chú', 111, 222)",
            )
            db.version = 2
        }

        val db = Room.databaseBuilder(context, NepNhaDatabase::class.java, dbFile.absolutePath)
            .addMigrations(NepNhaDatabase.MIGRATION_1_2, NepNhaDatabase.MIGRATION_2_3)
            .build()
        try {
            runBlocking {
                val m = db.memorialDao().getById(1)!!
                // Mọi cột cũ giữ nguyên từng giá trị.
                assertEquals("Cụ ông", m.name)
                assertEquals(30, m.lunarDay)
                assertEquals(7, m.lunarMonth)
                assertEquals("LEAP_MONTH_ONLY", m.leapMonthPolicy)
                assertEquals("SKIP", m.missingDayPolicy)
                assertEquals("ghi chú", m.note)
                assertEquals(111L, m.createdAt)
                assertEquals(222L, m.updatedAt)
                // Cột mới để trống — không đoán ngày giỗ cũ ứng với ai.
                assertNull(m.memberId)
            }
        } finally {
            db.close()
        }
    }

    /**
     * Xoá thành viên chỉ được làm **đứt liên kết**, tuyệt đối không xoá ngày giỗ.
     *
     * Sai thì: xoá một người trong danh sách gia đình làm bay mất ngày giỗ của họ —
     * mất dữ liệu người dùng vì một thao tác ở màn hình khác.
     */
    @Test
    fun xoa_thanh_vien_chi_lam_dut_lien_ket_khong_xoa_ngay_gio() {
        dbFile.parentFile?.mkdirs(); dbFile.delete()
        val db = Room.databaseBuilder(context, NepNhaDatabase::class.java, dbFile.absolutePath)
            .addMigrations(NepNhaDatabase.MIGRATION_1_2, NepNhaDatabase.MIGRATION_2_3)
            .build()
        try {
            runBlocking {
                com.nepnha.data.repository.FamilyRepository(db.familyDao())
                    .ensureDefaultFamily("Gia đình tôi")
                val familyId = db.familyDao().firstId()!!
                val memberId = db.memberDao().insert(
                    com.nepnha.data.db.MemberEntity(
                        familyId = familyId, fullName = "Nguyễn Văn A", gender = "MALE",
                        solarBirthDate = null, lunarBirthDay = null, lunarBirthMonth = null,
                        lunarBirthYear = null, lunarBirthIsLeapMonth = false,
                        lunarBirthSource = null, role = null, note = null,
                        createdAt = 1, updatedAt = 1,
                    ),
                )
                val memorialId = db.memorialDao().insert(
                    com.nepnha.data.db.MemorialEntity(
                        familyId = familyId, name = "Nguyễn Văn A", memberId = memberId,
                        lunarDay = 1, lunarMonth = 1,
                        leapMonthPolicy = "COMMON_MONTH_DEFAULT",
                        missingDayPolicy = "LAST_VALID_DAY_OF_MONTH",
                        note = null, createdAt = 1, updatedAt = 1,
                    ),
                )
                assertEquals(memberId, db.memorialDao().getById(memorialId)?.memberId)

                db.memberDao().deleteById(memberId)

                val after = db.memorialDao().getById(memorialId)
                assertNotNull("ngày giỗ bị xoá theo thành viên", after)
                assertNull("liên kết phải đứt", after!!.memberId)
                // Tên đã lưu vẫn còn ⇒ vẫn hiển thị được cho người dùng.
                assertEquals("Nguyễn Văn A", after.name)
            }
        } finally {
            db.close()
        }
    }

    /**
     * v2 → v3 với **nhiều** bản ghi: id phải giữ nguyên và không ngày giỗ nào bị gán
     * nhầm sang người khác.
     *
     * Test v2→v3 kia chỉ có một ngày giỗ, nên nó không phân biệt được "chép đúng" với
     * "chép một dòng thì kiểu gì cũng đúng". Migration này dựng bảng mới rồi chép sang
     * — nếu quên `id` trong câu INSERT, SQLite tự đánh số lại và mọi thứ vẫn *trông*
     * bình thường: đúng số dòng, đúng nội dung, nhưng id đã lệch.
     *
     * Sai thì: sau khi cập nhật app, ngày giỗ của cụ ông hiện dưới tên cụ bà.
     */
    @Test
    fun nang_cap_v2_len_v3_nhieu_ban_ghi_khong_gan_nham_va_khong_danh_so_lai() {
        dbFile.parentFile?.mkdirs(); dbFile.delete()

        val names = listOf("Cụ ông nội", "Cụ bà nội", "Cụ ông ngoại", "Bác cả")
        android.database.sqlite.SQLiteDatabase.openOrCreateDatabase(dbFile, null).use { db ->
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
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS `memorials` (`id` INTEGER PRIMARY KEY AUTOINCREMENT " +
                    "NOT NULL, `familyId` INTEGER NOT NULL, `name` TEXT NOT NULL, " +
                    "`lunarDay` INTEGER NOT NULL, `lunarMonth` INTEGER NOT NULL, " +
                    "`leapMonthPolicy` TEXT NOT NULL, `missingDayPolicy` TEXT NOT NULL, " +
                    "`note` TEXT, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, " +
                    "FOREIGN KEY(`familyId`) REFERENCES `families`(`id`) " +
                    "ON UPDATE NO ACTION ON DELETE CASCADE )",
            )
            db.execSQL("CREATE INDEX IF NOT EXISTS `index_memorials_familyId` ON `memorials` (`familyId`)")
            db.execSQL(
                "CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY, identity_hash TEXT)",
            )
            db.execSQL(
                "INSERT OR REPLACE INTO room_master_table (id, identity_hash) VALUES (42, ?)",
                arrayOf(v2IdentityHash),
            )
            db.execSQL("INSERT INTO families (name, createdAt, updatedAt) VALUES ('Nhà họ Nguyễn', 1, 1)")
            db.execSQL(
                "INSERT INTO members (familyId, fullName, gender, lunarBirthIsLeapMonth, " +
                    "createdAt, updatedAt) VALUES (1, 'Người đang sống', 'MALE', 0, 1, 1)",
            )
            names.forEachIndexed { index, name ->
                db.execSQL(
                    "INSERT INTO memorials (familyId, name, lunarDay, lunarMonth, leapMonthPolicy, " +
                        "missingDayPolicy, note, createdAt, updatedAt) VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?)",
                    arrayOf<Any>(
                        name, index + 1, index + 1,
                        if (index % 2 == 0) "LEAP_MONTH_ONLY" else "COMMON_MONTH_DEFAULT",
                        if (index % 2 == 0) "SKIP" else "LAST_VALID_DAY_OF_MONTH",
                        "ghi chú $index", 1000L + index, 2000L + index,
                    ),
                )
            }
            db.version = 2
        }

        val db = Room.databaseBuilder(context, NepNhaDatabase::class.java, dbFile.absolutePath)
            .addMigrations(NepNhaDatabase.MIGRATION_1_2, NepNhaDatabase.MIGRATION_2_3)
            .build()
        try {
            runBlocking {
                names.forEachIndexed { index, name ->
                    // Tra theo id CŨ: nếu migration đánh số lại thì dòng này không còn
                    // đúng tên nữa.
                    val m = db.memorialDao().getById((index + 1).toLong())
                    assertNotNull("mất ngày giỗ id=${index + 1}", m)
                    assertEquals(name, m!!.name)
                    assertEquals(index + 1, m.lunarDay)
                    assertEquals(index + 1, m.lunarMonth)
                    assertEquals("ghi chú $index", m.note)
                    assertEquals(1000L + index, m.createdAt)
                    assertEquals(2000L + index, m.updatedAt)
                    assertNull("không được đoán ngày giỗ cũ là của ai", m.memberId)
                }
                assertEquals(
                    "không được thừa hay thiếu dòng nào",
                    names.size,
                    db.memorialDao().observeByFamily(1).first().size,
                )

                // Khoá ngoại mới phải là khoá ngoại THẬT: liên kết rồi xoá người thì
                // liên kết đứt chứ ngày giỗ không biến mất.
                db.memorialDao().insert(
                    com.nepnha.data.db.MemorialEntity(
                        familyId = 1, name = "Người đang sống", memberId = 1,
                        lunarDay = 9, lunarMonth = 9,
                        leapMonthPolicy = "COMMON_MONTH_DEFAULT",
                        missingDayPolicy = "LAST_VALID_DAY_OF_MONTH",
                        note = null, createdAt = 1, updatedAt = 1,
                    ),
                )
                db.memberDao().deleteById(1)
                val linked = db.memorialDao().observeByFamily(1).first()
                    .single { it.name == "Người đang sống" }
                assertNull("ON DELETE SET NULL phải có hiệu lực sau migration", linked.memberId)
                assertEquals(
                    "xoá người không được xoá ngày giỗ",
                    names.size + 1,
                    db.memorialDao().observeByFamily(1).first().size,
                )
            }
        } finally {
            db.close()
        }
    }
}
