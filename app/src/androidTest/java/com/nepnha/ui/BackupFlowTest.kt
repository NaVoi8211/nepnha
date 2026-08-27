package com.nepnha.ui

import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nepnha.data.TestEnvironment
import com.nepnha.domain.event.LeapMonthPolicy
import com.nepnha.domain.event.MemorialRule
import com.nepnha.domain.event.MissingDayPolicy
import com.nepnha.domain.model.FamilyMemberDraft
import com.nepnha.domain.model.Gender
import com.nepnha.domain.model.MemorialDraft
import com.nepnha.ui.settings.SettingsScreen
import com.nepnha.ui.settings.SettingsViewModel
import com.nepnha.ui.theme.NepNhaTheme
import java.io.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Luồng xuất/nhập trên giao diện.
 *
 * Bộ chọn file là của **hệ thống Android**, không phải của app, nên test lái ViewModel
 * đúng bằng những gì callback của bộ chọn sẽ đưa vào — một `Uri`. Phần còn lại là
 * đường thật: đọc/ghi qua `ContentResolver`, phân tích, xem trước, xác nhận, giao dịch.
 */
@RunWith(AndroidJUnit4::class)
class BackupFlowTest {

    // Xem chú thích ở NavigationTest: bản v2 của rule làm hỏng cả bộ test.
    @Suppress("DEPRECATION")
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var env: TestEnvironment
    private lateinit var file: File
    private var familyId: Long = 0

    @Before
    fun setUp() {
        env = TestEnvironment(rule.activity)
        file = File(rule.activity.cacheDir, "backup_ui_${System.nanoTime()}.json")
        runBlocking {
            env.familyRepository.ensureDefaultFamily("Gia đình tôi")
            familyId = env.familyRepository.observeFamily().first()!!.id
        }
    }

    @After
    fun tearDown() {
        file.delete()
        env.close()
    }

    private fun viewModel() = SettingsViewModel(
        env.container,
        rule.activity.contentResolver,
        "test",
    )

    private fun show(vm: SettingsViewModel) {
        rule.setContent {
            val state by vm.state.collectAsStateWithLifecycle()
            NepNhaTheme {
                SettingsScreen(
                    state = state,
                    versionName = "test",
                    suggestedFileName = "nepnha.json",
                    onExportTo = vm::export,
                    onImportFrom = vm::prepareImport,
                    onConfirmImport = vm::confirmImport,
                    onCancelImport = vm::cancelImport,
                    onDismissMessage = vm::dismissMessage,
                )
            }
        }
    }

    private fun seed() = runBlocking {
        val memberId = env.memberRepository.add(
            familyId,
            FamilyMemberDraft("Nguyễn Văn A", Gender.MALE, null, null, "Trưởng nam", null),
        )
        env.memorialRepository.add(
            familyId,
            MemorialDraft(
                "Cụ ông", 30, 7,
                MemorialRule(LeapMonthPolicy.LEAP_MONTH_ONLY, MissingDayPolicy.SKIP),
                null, memberId = memberId,
            ),
        )
        memberId
    }

    /**
     * Xuất phải ghi ra một file đọc được và báo thành công bằng con số cụ thể.
     *
     * Sai thì: người dùng bấm xuất, không thấy gì, và không biết đã có file hay chưa.
     */
    @Test
    fun xuat_du_lieu_ghi_ra_file_va_bao_thanh_cong() {
        seed()
        val vm = viewModel()
        show(vm)

        rule.onNodeWithTag("btn_export").assertIsDisplayed()
        rule.runOnIdle { vm.export(Uri.fromFile(file)) }

        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithTag("dialog_message").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("1 thành viên", substring = true).assertIsDisplayed()
        assertTrue("file phải tồn tại", file.exists())
        assertTrue("file phải có nội dung", file.readText().contains("\"formatVersion\""))
    }

    /**
     * Nhập phải **xem trước rồi mới xác nhận** — không được ghi gì trước khi người
     * dùng đồng ý.
     *
     * Sai thì: chọn nhầm file là dữ liệu bị đổi ngay, không có đường lui.
     */
    @Test
    fun nhap_du_lieu_phai_xem_truoc_roi_moi_ghi() {
        seed()
        val vm = viewModel()
        show(vm)
        rule.runOnIdle { vm.export(Uri.fromFile(file)) }
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithTag("dialog_message").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("btn_message_ok").performClick()

        val beforeMembers = runBlocking { env.memberRepository.observeMembers(familyId).first().size }

        rule.runOnIdle { vm.prepareImport(Uri.fromFile(file)) }
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithTag("dialog_import_preview").fetchSemanticsNodes().isNotEmpty()
        }
        // Xem trước phải nói rõ số lượng và cảnh báo là THÊM chứ không thay thế.
        rule.onNodeWithText("1 thành viên và 1 ngày giỗ", substring = true).assertIsDisplayed()
        rule.onNodeWithText("THÊM", substring = true).assertIsDisplayed()
        // Và chưa ghi gì cả.
        assertEquals(
            beforeMembers,
            runBlocking { env.memberRepository.observeMembers(familyId).first().size },
        )

        rule.onNodeWithTag("btn_import_confirm").performClick()
        rule.waitUntil(timeoutMillis = 5_000) {
            runBlocking { env.memberRepository.observeMembers(familyId).first().size } == beforeMembers + 1
        }
        val memorials = runBlocking { env.memorialRepository.observe(familyId).first() }
        assertEquals(2, memorials.size)
        // Bản nhập giữ đúng policy không mặc định.
        assertTrue(
            memorials.any { it.rule.leapMonthPolicy == LeapMonthPolicy.LEAP_MONTH_ONLY },
        )
    }

    /**
     * Huỷ ở màn xem trước phải **không ghi gì**.
     *
     * Sai thì: nút Huỷ là nói dối.
     */
    @Test
    fun huy_o_man_xem_truoc_thi_khong_ghi_gi() {
        seed()
        val vm = viewModel()
        show(vm)
        rule.runOnIdle { vm.export(Uri.fromFile(file)) }
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithTag("dialog_message").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("btn_message_ok").performClick()

        val before = runBlocking { env.memberRepository.observeMembers(familyId).first().size }
        rule.runOnIdle { vm.prepareImport(Uri.fromFile(file)) }
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithTag("dialog_import_preview").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("Huỷ").performClick()
        rule.waitForIdle()

        assertEquals(before, runBlocking { env.memberRepository.observeMembers(familyId).first().size })
    }

    /**
     * File hỏng phải bị từ chối kèm lý do đọc được, và **không** chạm vào database.
     *
     * Sai thì: người dùng chọn nhầm file và mất dữ liệu, hoặc chỉ nhận được thông báo
     * vô nghĩa "có lỗi".
     */
    @Test
    fun file_hong_bi_tu_choi_kem_ly_do_va_khong_ghi_gi() {
        seed()
        val before = runBlocking { env.memberRepository.observeMembers(familyId).first().size }
        file.writeText("đây không phải file của Nếp Nhà")

        val vm = viewModel()
        show(vm)
        rule.runOnIdle { vm.prepareImport(Uri.fromFile(file)) }
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithTag("dialog_message").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("File không dùng được").assertIsDisplayed()
        rule.onNodeWithText("không đúng định dạng", substring = true).assertIsDisplayed()
        assertEquals(before, runBlocking { env.memberRepository.observeMembers(familyId).first().size })
    }

    /**
     * File bị sửa một chữ số nhưng vẫn là JSON hợp lệ phải bị checksum bắt.
     *
     * Sai thì: ngày giỗ được khôi phục vào **sai ngày** — thứ tệ nhất có thể xảy ra
     * với một app ngày giỗ.
     */
    @Test
    fun file_bi_hong_mot_chu_so_bi_checksum_bat() {
        seed()
        val vm = viewModel()
        show(vm)
        rule.runOnIdle { vm.export(Uri.fromFile(file)) }
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithTag("dialog_message").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("btn_message_ok").performClick()

        val text = file.readText()
        assertTrue("phải có lunarDay 30 để sửa", text.contains("\"lunarDay\": 30"))
        file.writeText(text.replace("\"lunarDay\": 30", "\"lunarDay\": 20"))

        rule.runOnIdle { vm.prepareImport(Uri.fromFile(file)) }
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithTag("dialog_message").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithText("dấu hiệu bị hỏng", substring = true).assertIsDisplayed()
    }
}
