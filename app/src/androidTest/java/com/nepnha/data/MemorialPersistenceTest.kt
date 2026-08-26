package com.nepnha.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nepnha.data.db.NepNhaDatabase
import com.nepnha.data.repository.FamilyRepository
import com.nepnha.data.repository.MemorialRepository
import com.nepnha.domain.event.LeapMonthPolicy
import com.nepnha.domain.event.MemorialRule
import com.nepnha.domain.event.MissingDayPolicy
import com.nepnha.domain.model.MemorialDraft
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Ngày giỗ phải sống sót khi **đóng hẳn database rồi mở lại từ file** — tương đương
 * force-stop rồi mở lại app.
 *
 * Dùng database trên đĩa chứ không in-memory: in-memory chết cùng tiến trình nên
 * không chứng minh được gì về việc lưu trữ.
 */
@RunWith(AndroidJUnit4::class)
class MemorialPersistenceTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val dbFile: File = context.getDatabasePath("persistence_test.db")

    private fun open(): NepNhaDatabase =
        Room.databaseBuilder(context, NepNhaDatabase::class.java, dbFile.absolutePath)
            .addMigrations(NepNhaDatabase.MIGRATION_1_2)
            .build()

    @After
    fun tearDown() {
        dbFile.delete()
        File("${dbFile.path}-wal").delete()
        File("${dbFile.path}-shm").delete()
    }

    /**
     * Sai thì: người dùng nhập ngày giỗ, tắt app, mở lại và mất hết.
     */
    @Test
    fun ngay_gio_song_sot_qua_dong_mo_lai_database() {
        dbFile.parentFile?.mkdirs()
        dbFile.delete()

        val first = open()
        val id: Long
        val familyId: Long
        runBlocking {
            FamilyRepository(first.familyDao()).ensureDefaultFamily("Gia đình tôi")
            familyId = first.familyDao().firstId()!!
            id = MemorialRepository(first.memorialDao()).add(
                familyId,
                MemorialDraft(
                    name = "Cụ ông",
                    lunarDay = 30,
                    lunarMonth = 7,
                    rule = MemorialRule(
                        leapMonthPolicy = LeapMonthPolicy.LEAP_MONTH_ONLY,
                        missingDayPolicy = MissingDayPolicy.SKIP,
                    ),
                    note = "ghi chú",
                ),
            )
        }
        first.close()

        val second = open()
        try {
            runBlocking {
                val loaded = MemorialRepository(second.memorialDao()).observe(familyId).first()
                assertEquals(1, loaded.size)
                val m = loaded.single()
                assertEquals(id, m.id)
                assertEquals("Cụ ông", m.name)
                // Ngày âm GỐC còn nguyên 30 — không bị ghi đè bởi bất kỳ phép điều chỉnh nào.
                assertEquals(30, m.lunarDay)
                assertEquals(7, m.lunarMonth)
                // Policy cũng phải sống sót, nếu không quy tắc của gia đình bị đổi âm thầm.
                assertEquals(LeapMonthPolicy.LEAP_MONTH_ONLY, m.rule.leapMonthPolicy)
                assertEquals(MissingDayPolicy.SKIP, m.rule.missingDayPolicy)
                assertEquals("ghi chú", m.note)
            }
        } finally {
            second.close()
        }
    }

    /**
     * Xoá gia đình phải kéo theo ngày giỗ — nếu không sẽ còn bản ghi mồ côi.
     *
     * Sai thì: database tích tụ dữ liệu rác không ai truy cập được.
     */
    @Test
    fun xoa_gia_dinh_keo_theo_ngay_gio() {
        dbFile.parentFile?.mkdirs()
        dbFile.delete()
        val db = open()
        try {
            runBlocking {
                FamilyRepository(db.familyDao()).ensureDefaultFamily("Gia đình tôi")
                val familyId = db.familyDao().firstId()!!
                MemorialRepository(db.memorialDao()).add(
                    familyId,
                    MemorialDraft("Cụ bà", 1, 1, MemorialRule(), null),
                )
                db.openHelper.writableDatabase.execSQL("PRAGMA foreign_keys=ON")
                db.openHelper.writableDatabase.execSQL("DELETE FROM families WHERE id = $familyId")
                assertEquals(0, MemorialRepository(db.memorialDao()).observe(familyId).first().size)
            }
        } finally {
            db.close()
        }
    }
}
