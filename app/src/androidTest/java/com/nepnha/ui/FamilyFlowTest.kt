package com.nepnha.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nepnha.R
import com.nepnha.data.TestEnvironment
import com.nepnha.ui.theme.NepNhaTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Luồng thật của Phase 2: thêm → sửa → chọn tín chủ → xoá.
 *
 * Chạy trên Samsung A32, dữ liệu đi qua Room in-memory của [TestEnvironment].
 */
@RunWith(AndroidJUnit4::class)
class FamilyFlowTest {

    @Suppress("DEPRECATION")
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var env: TestEnvironment

    @Before
    fun setUp() {
        env = TestEnvironment(rule.activity)
        runBlocking { env.familyRepository.ensureDefaultFamily("Gia đình tôi") }
        rule.setContent { NepNhaTheme { NepNhaShell(container = env.container) } }
        rule.onNodeWithTag("tab_family").performClick()
    }

    @After
    fun tearDown() = env.close()

    private fun text(resId: Int) = rule.activity.getString(resId)

    private fun addMember(name: String) {
        rule.onNodeWithText(text(R.string.family_add_member)).performClick()
        rule.onNodeWithTag("field_full_name").performTextInput(name)
        rule.onNodeWithTag("btn_save_member").performScrollTo().performClick()
        rule.waitForIdle()
    }

    @Test
    fun family_screen_hien_empty_state_khi_chua_co_thanh_vien() {
        rule.onNodeWithTag("screen_family").assertIsDisplayed()
        rule.onNodeWithText(text(R.string.family_empty_title)).assertIsDisplayed()
        rule.onNodeWithTag("card_no_worshipper").assertIsDisplayed()
    }

    @Test
    fun ten_trong_thi_bao_loi_chu_khong_luu_im_lang() {
        rule.onNodeWithText(text(R.string.family_add_member)).performClick()
        rule.onNodeWithTag("btn_save_member").performScrollTo().performClick()
        rule.waitForIdle()

        // Vẫn ở lại biểu mẫu và có thông báo lỗi rõ ràng.
        rule.onNodeWithTag("screen_member_editor").assertIsDisplayed()
        rule.onNodeWithText(text(R.string.error_name_required)).assertIsDisplayed()
    }

    @Test
    fun them_thanh_vien_thi_xuat_hien_trong_danh_sach() {
        addMember("Nguyễn Văn A")
        rule.onNodeWithTag("screen_family").assertIsDisplayed()
        rule.onNodeWithText("Nguyễn Văn A").assertIsDisplayed()

        val familyId = runBlocking { env.familyRepository.observeFamily().first()!!.id }
        val saved = runBlocking { env.memberRepository.observeMembers(familyId).first() }
        assertEquals(1, saved.size)
        assertEquals("Nguyễn Văn A", saved.first().fullName)
    }

    @Test
    fun sua_thanh_vien() {
        addMember("Tên Cũ")
        rule.onNodeWithText("Tên Cũ").performClick()
        rule.onNodeWithTag("field_full_name").performTextClearance()
        rule.onNodeWithTag("field_full_name").performTextInput("Tên Mới")
        rule.onNodeWithTag("btn_save_member").performScrollTo().performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Tên Mới").assertIsDisplayed()
    }

    @Test
    fun xoa_thanh_vien_phai_qua_xac_nhan() {
        addMember("Nguyễn Văn A")
        val memberId = runBlocking {
            val familyId = env.familyRepository.observeFamily().first()!!.id
            env.memberRepository.observeMembers(familyId).first().first().id
        }

        rule.onNodeWithTag("btn_delete_$memberId").performScrollTo().performClick()
        rule.onNodeWithTag("dialog_delete_member").assertIsDisplayed()

        // Huỷ thì không xoá gì.
        rule.onNodeWithText(text(R.string.action_cancel)).performClick()
        rule.waitForIdle()
        rule.onNodeWithText("Nguyễn Văn A").assertIsDisplayed()

        rule.onNodeWithTag("btn_delete_$memberId").performScrollTo().performClick()
        rule.onNodeWithTag("btn_confirm_delete").performClick()
        rule.waitForIdle()
        rule.onNodeWithText(text(R.string.family_empty_title)).assertIsDisplayed()
    }

    @Test
    fun chon_tin_chu_va_xoa_tin_chu_thi_ve_chua_chon() {
        addMember("Nguyễn Văn A")
        val memberId = runBlocking {
            val familyId = env.familyRepository.observeFamily().first()!!.id
            env.memberRepository.observeMembers(familyId).first().first().id
        }

        rule.onNodeWithText(text(R.string.family_choose_worshipper)).performScrollTo().performClick()
        rule.onNodeWithTag("screen_choose_worshipper").assertIsDisplayed()
        rule.onNodeWithTag("worshipper_option_$memberId").performClick()
        rule.waitForIdle()

        rule.onNodeWithTag("card_worshipper").assertIsDisplayed()
        assertEquals(memberId, runBlocking { env.settingsRepository.primaryMemberId.first() })

        // Xoá chính tín chủ ⇒ quay về "chưa chọn", không ai được tự động thay thế.
        rule.onNodeWithTag("btn_delete_$memberId").performScrollTo().performClick()
        rule.onNodeWithTag("btn_confirm_delete").performClick()
        rule.waitForIdle()

        // Xoá tín chủ ghi xuống DataStore một cách bất đồng bộ; `waitForIdle` chỉ chờ
        // Compose chứ không chờ ghi xong. Chờ có giới hạn thay vì đọc ngay — đọc ngay
        // là một cuộc đua, và nó đã thực sự đỏ khi Phase 5 làm đổi nhịp luồng dữ liệu.
        rule.waitUntil(timeoutMillis = 5_000) {
            runBlocking { env.settingsRepository.primaryMemberId.first() } == null
        }
        assertNull(runBlocking { env.settingsRepository.primaryMemberId.first() })
        rule.onNodeWithTag("card_no_worshipper").assertIsDisplayed()
    }

    @Test
    fun doi_ten_gia_dinh() {
        rule.onNodeWithTag("btn_rename_family").performClick()
        rule.onNodeWithTag("field_family_name").performTextClearance()
        rule.onNodeWithTag("field_family_name").performTextInput("Gia đình họ Nguyễn")
        rule.onNodeWithTag("btn_confirm_rename").performClick()
        rule.waitForIdle()

        rule.onNodeWithText("Gia đình họ Nguyễn").assertIsDisplayed()
    }

    @Test
    fun home_hien_thi_gia_dinh_that() {
        addMember("Nguyễn Văn A")
        rule.onNodeWithTag("tab_home").performClick()
        // Có người nhưng chưa chọn tín chủ ⇒ Home phải nhắc, không tự chọn.
        rule.onNodeWithTag("home_no_worshipper").performScrollTo().assertIsDisplayed()
    }
}
