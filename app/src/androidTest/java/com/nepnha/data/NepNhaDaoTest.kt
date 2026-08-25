package com.nepnha.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nepnha.data.db.FamilyEntity
import com.nepnha.data.db.MemberEntity
import com.nepnha.data.db.NepNhaDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NepNhaDaoTest {

    private lateinit var env: TestEnvironment

    @Before
    fun setUp() {
        env = TestEnvironment(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() = env.close()

    private fun member(familyId: Long, name: String) = MemberEntity(
        familyId = familyId,
        fullName = name,
        gender = "UNSPECIFIED",
        solarBirthDate = null,
        lunarBirthDay = null,
        lunarBirthMonth = null,
        lunarBirthYear = null,
        lunarBirthIsLeapMonth = false,
        lunarBirthSource = null,
        role = null,
        note = null,
        createdAt = 1L,
        updatedAt = 1L,
    )

    @Test
    fun family_CRUD() = runBlocking {
        val dao = env.database.familyDao()
        assertEquals(0, dao.count())

        val id = dao.insert(FamilyEntity(name = "Gia đình tôi", createdAt = 1, updatedAt = 1))
        assertEquals(1, dao.count())
        assertEquals("Gia đình tôi", dao.observeFirst().first()?.name)

        dao.updateName(id, "Gia đình họ Nguyễn", updatedAt = 2)
        assertEquals("Gia đình họ Nguyễn", dao.observeFirst().first()?.name)
    }

    @Test
    fun member_CRUD_va_quan_he_family_members() = runBlocking {
        val familyId = env.database.familyDao()
            .insert(FamilyEntity(name = "F", createdAt = 1, updatedAt = 1))
        val dao = env.database.memberDao()

        val id = dao.insert(member(familyId, "Nguyễn Văn A"))
        assertEquals(1, dao.countByFamily(familyId))
        assertEquals("Nguyễn Văn A", dao.getById(id)?.fullName)
        assertEquals(1, dao.observeByFamily(familyId).first().size)

        dao.update(dao.getById(id)!!.copy(fullName = "Nguyễn Văn B", updatedAt = 2))
        assertEquals("Nguyễn Văn B", dao.getById(id)?.fullName)

        dao.deleteById(id)
        assertNull(dao.getById(id))
        assertEquals(0, dao.countByFamily(familyId))
    }

    @Test
    fun xoa_family_thi_members_bi_cascade() = runBlocking {
        val familyDao = env.database.familyDao()
        val memberDao = env.database.memberDao()
        val familyId = familyDao.insert(FamilyEntity(name = "F", createdAt = 1, updatedAt = 1))
        memberDao.insert(member(familyId, "A"))
        memberDao.insert(member(familyId, "B"))
        assertEquals(2, memberDao.countByFamily(familyId))

        env.database.openHelper.writableDatabase.execSQL("DELETE FROM families WHERE id = $familyId")
        assertEquals(0, memberDao.countByFamily(familyId))
    }

    /**
     * Test quan trọng nhất của Phase 2: đóng database rồi mở lại, dữ liệu phải còn.
     * Dùng file thật chứ không in-memory — in-memory thì test này vô nghĩa.
     */
    @Test
    fun du_lieu_con_nguyen_sau_khi_dong_va_mo_lai_database() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val name = "persistence_test_${System.nanoTime()}.db"
        context.deleteDatabase(name)
        try {
            var db = Room.databaseBuilder(context, NepNhaDatabase::class.java, name).build()
            val familyId = db.familyDao()
                .insert(FamilyEntity(name = "Gia đình tôi", createdAt = 1, updatedAt = 1))
            db.memberDao().insert(member(familyId, "Nguyễn Văn A"))
            db.close()

            db = Room.databaseBuilder(context, NepNhaDatabase::class.java, name).build()
            assertNotNull(db.familyDao().observeFirst().first())
            assertEquals("Gia đình tôi", db.familyDao().observeFirst().first()?.name)
            assertEquals(1, db.memberDao().countByFamily(familyId))
            assertEquals("Nguyễn Văn A", db.memberDao().observeByFamily(familyId).first().first().fullName)
            db.close()
        } finally {
            context.deleteDatabase(name)
        }
    }
}
