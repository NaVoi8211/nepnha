package com.nepnha.data

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nepnha.domain.model.FamilyMemberDraft
import com.nepnha.domain.model.Gender
import com.nepnha.domain.model.LunarBirthDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class FamilyMemberRepositoryTest {

    private lateinit var env: TestEnvironment

    @Before
    fun setUp() {
        env = TestEnvironment(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() = env.close()

    private fun draft(name: String) = FamilyMemberDraft(
        fullName = name,
        gender = Gender.UNSPECIFIED,
        solarBirthDate = null,
        lunarBirthDate = null,
        role = null,
        note = null,
    )

    @Test
    fun lan_dau_chay_tao_dung_mot_gia_dinh_mac_dinh() = runBlocking {
        assertNull(env.familyRepository.observeFamily().first())

        val first = env.familyRepository.ensureDefaultFamily("Gia đình tôi")
        val second = env.familyRepository.ensureDefaultFamily("Gia đình tôi")

        // Idempotent: gọi lại không tạo thêm gia đình thứ hai.
        assertEquals(first, second)
        assertEquals(1, env.database.familyDao().count())
        assertEquals("Gia đình tôi", env.familyRepository.observeFamily().first()?.name)
    }

    @Test
    fun doi_ten_gia_dinh() = runBlocking {
        val id = env.familyRepository.ensureDefaultFamily("Gia đình tôi")
        env.familyRepository.rename(id, "  Gia đình họ Nguyễn  ")
        assertEquals("Gia đình họ Nguyễn", env.familyRepository.observeFamily().first()?.name)
    }

    @Test
    fun ten_gia_dinh_rong_thi_giu_nguyen_ten_cu() = runBlocking {
        val id = env.familyRepository.ensureDefaultFamily("Gia đình tôi")
        env.familyRepository.rename(id, "   ")
        assertEquals("Gia đình tôi", env.familyRepository.observeFamily().first()?.name)
    }

    @Test
    fun them_sua_xoa_thanh_vien() = runBlocking {
        val familyId = env.familyRepository.ensureDefaultFamily("F")

        val id = env.memberRepository.add(familyId, draft("Nguyễn Văn A"))
        assertEquals(1, env.memberRepository.observeMembers(familyId).first().size)

        env.memberRepository.update(
            id,
            draft("Nguyễn Văn B").copy(
                solarBirthDate = LocalDate.of(1950, 8, 24),
                lunarBirthDate = LunarBirthDate(15, 7, 1950, isLeapMonth = true),
                role = "ông nội",
            ),
        )
        val updated = requireNotNull(env.memberRepository.getMember(id))
        assertEquals("Nguyễn Văn B", updated.fullName)
        assertEquals(LocalDate.of(1950, 8, 24), updated.solarBirthDate)
        assertEquals(15, updated.lunarBirthDate?.day)
        assertTrue(updated.lunarBirthDate?.isLeapMonth == true)
        assertEquals("ông nội", updated.role)

        env.memberRepository.delete(id)
        assertNull(env.memberRepository.getMember(id))
    }

    @Test
    fun mac_dinh_chua_chon_tin_chu() = runBlocking {
        assertNull(env.settingsRepository.primaryMemberId.first())
    }

    @Test
    fun chon_va_doi_tin_chu() = runBlocking {
        val familyId = env.familyRepository.ensureDefaultFamily("F")
        val a = env.memberRepository.add(familyId, draft("A"))
        val b = env.memberRepository.add(familyId, draft("B"))

        env.settingsRepository.setPrimaryMemberId(a)
        assertEquals(a, env.settingsRepository.primaryMemberId.first())

        env.settingsRepository.setPrimaryMemberId(b)
        assertEquals(b, env.settingsRepository.primaryMemberId.first())
    }

    /** Quy tắc đã chốt: xoá tín chủ ⇒ về "chưa chọn", KHÔNG tự đẩy người khác lên. */
    @Test
    fun xoa_tin_chu_thi_tin_chu_ve_null_chu_khong_tu_chon_nguoi_khac() = runBlocking {
        val familyId = env.familyRepository.ensureDefaultFamily("F")
        val a = env.memberRepository.add(familyId, draft("A"))
        val b = env.memberRepository.add(familyId, draft("B"))
        env.settingsRepository.setPrimaryMemberId(a)

        env.memberRepository.delete(a)

        assertNull(env.settingsRepository.primaryMemberId.first())
        // B vẫn còn nguyên và KHÔNG bị tự động trở thành tín chủ.
        assertNotNull(env.memberRepository.getMember(b))
    }

    @Test
    fun xoa_thanh_vien_khac_thi_tin_chu_khong_bi_anh_huong() = runBlocking {
        val familyId = env.familyRepository.ensureDefaultFamily("F")
        val a = env.memberRepository.add(familyId, draft("A"))
        val b = env.memberRepository.add(familyId, draft("B"))
        env.settingsRepository.setPrimaryMemberId(a)

        env.memberRepository.delete(b)

        assertEquals(a, env.settingsRepository.primaryMemberId.first())
    }

    @Test
    fun overview_gop_dung_gia_dinh_thanh_vien_va_tin_chu() = runBlocking {
        val familyId = env.familyRepository.ensureDefaultFamily("Gia đình tôi")
        val a = env.memberRepository.add(familyId, draft("A"))
        env.memberRepository.add(familyId, draft("B"))
        env.settingsRepository.setPrimaryMemberId(a)

        val overview = env.container.familyOverview.observe().first { it.isLoaded && it.members.size == 2 }
        assertEquals("Gia đình tôi", overview.family?.name)
        assertEquals(2, overview.members.size)
        assertEquals("A", overview.primaryMember?.fullName)
    }

    @Test
    fun database_rong_thi_overview_khong_no() = runBlocking {
        val overview = env.container.familyOverview.observe().first()
        assertNull(overview.family)
        assertTrue(overview.members.isEmpty())
        assertNull(overview.primaryMember)
    }
}
