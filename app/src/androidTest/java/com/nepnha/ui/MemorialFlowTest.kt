package com.nepnha.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nepnha.data.TestEnvironment
import com.nepnha.domain.event.LeapMonthPolicy
import com.nepnha.domain.event.MemorialRule
import com.nepnha.domain.model.MemorialDraft
import com.nepnha.ui.theme.NepNhaTheme
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
 * Ngày giỗ chạy thật trên thiết bị: tạo, lưu xuống Room, đọc lại, sửa, xoá.
 *
 * Dùng **engine lịch âm thật** nạp từ asset trong APK — không có engine giả nào ở
 * đây, vì thứ cần chứng minh chính là ngày giỗ quy đổi đúng trong app thật.
 */
@RunWith(AndroidJUnit4::class)
class MemorialFlowTest {

    // Xem chú thích ở NavigationTest: bản v2 của rule làm hỏng cả bộ test.
    @Suppress("DEPRECATION")
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var env: TestEnvironment
    private var familyId: Long = 0

    @Before
    fun setUp() {
        env = TestEnvironment(rule.activity)
        runBlocking {
            env.familyRepository.ensureDefaultFamily("Gia đình tôi")
            familyId = env.familyRepository.observeFamily().first()!!.id
        }
    }

    @After
    fun tearDown() = env.close()

    private fun launch() {
        rule.setContent { NepNhaTheme { NepNhaShell(container = env.container) } }
    }

    private fun openMemorials() {
        // Nút "Xem tất cả" chỉ xuất hiện khi danh sách từ Room đã về.
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithTag("home_memorial_all").fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("home_memorial_all").performScrollTo().performClick()
        rule.onNodeWithTag("screen_memorials").assertIsDisplayed()
    }

    private fun seed(name: String, day: Int, month: Int, rule0: MemorialRule = MemorialRule()) =
        runBlocking {
            env.memorialRepository.add(
                familyId,
                MemorialDraft(name = name, lunarDay = day, lunarMonth = month, rule = rule0, note = null),
            )
        }

    /**
     * Sai thì: người dùng nhập xong bấm Lưu mà không có gì được ghi lại.
     */
    @Test
    fun tao_ngay_gio_moi_va_thay_trong_danh_sach() {
        launch()
        // Màn Nhà mời thêm ⇒ mở danh sách, rồi mới vào biểu mẫu.
        rule.onNodeWithText("Thêm ngày giỗ").performScrollTo().performClick()
        rule.onNodeWithTag("screen_memorials").assertIsDisplayed()
        rule.onNodeWithTag("memorial_add").performClick()
        rule.onNodeWithTag("screen_memorial_editor").assertIsDisplayed()

        rule.onNodeWithTag("memorial_name").performTextInput("Cụ ông Nguyễn Văn A")
        rule.onNodeWithTag("memorial_day").performTextInput("15")
        rule.onNodeWithTag("memorial_month").performTextInput("7")
        rule.onNodeWithTag("memorial_save").performScrollTo().performClick()

        rule.waitForIdle()
        val saved = runBlocking { env.memorialRepository.observe(familyId).first() }
        assertEquals(1, saved.size)
        assertEquals(15, saved[0].lunarDay)
        assertEquals(7, saved[0].lunarMonth)
        rule.onNodeWithText("Cụ ông Nguyễn Văn A", substring = true).assertIsDisplayed()
    }

    /**
     * Ngày giỗ đã lưu phải hiện lên khi mở màn danh sách.
     *
     * Việc dữ liệu sống sót qua khởi động lại tiến trình được chứng minh ở
     * `MemorialPersistenceTest` — chỗ đó đóng hẳn database rồi mở lại từ file.
     */
    @Test
    fun ngay_gio_da_luu_hien_tren_danh_sach() {
        seed("Cụ bà", 10, 3)
        launch()
        openMemorials()
        rule.onNodeWithText("Cụ bà", substring = true).assertIsDisplayed()
    }

    /**
     * Ngày 30 tháng 7 âm năm 2026 không tồn tại (tháng đó 29 ngày).
     *
     * Sai thì: app âm thầm đổi 30 thành 29 mà không nói gì — đúng điều
     * `docs/MEMORIAL_RULES.md` cấm.
     */
    @Test
    fun ngay_30_bi_dieu_chinh_thi_phai_noi_ro() {
        seed("Cụ tổ", 30, 7)
        launch()
        openMemorials()
        rule.onNodeWithText("Đã điều chỉnh ngày", substring = true).assertIsDisplayed()

        // Dữ liệu gốc trong Room vẫn là 30 — không bị ghi đè.
        val stored = runBlocking { env.memorialRepository.observe(familyId).first() }
        assertEquals(30, stored.single().lunarDay)
    }

    /**
     * Sai thì: sửa xong không lưu, hoặc lưu thành bản ghi mới thay vì cập nhật.
     */
    @Test
    fun sua_ngay_gio() {
        val id = seed("Tên cũ", 5, 5)
        launch()
        openMemorials()
        rule.onNodeWithTag("memorial_row_$id").performClick()
        rule.onNodeWithTag("memorial_month").performTextInput("")
        rule.onNodeWithTag("memorial_name").performTextInput(" (sửa)")
        rule.onNodeWithTag("memorial_save").performScrollTo().performClick()
        rule.waitForIdle()

        val after = runBlocking { env.memorialRepository.observe(familyId).first() }
        assertEquals(1, after.size)
        assertTrue(after.single().name.contains("sửa"))
        assertEquals(id, after.single().id)
    }

    /**
     * Sai thì: xoá xong ngày giỗ vẫn còn trong DB hoặc vẫn hiện trên danh sách.
     */
    @Test
    fun xoa_ngay_gio_phai_qua_xac_nhan() {
        val id = seed("Cụ cố", 2, 2)
        launch()
        openMemorials()
        rule.onNodeWithTag("memorial_row_$id").performClick()
        rule.onNodeWithTag("memorial_delete").performScrollTo().performClick()
        rule.onNodeWithTag("memorial_delete_confirm").performClick()
        rule.waitForIdle()

        assertEquals(0, runBlocking { env.memorialRepository.observe(familyId).first() }.size)
        rule.onNodeWithTag("memorial_row_$id").assertIsNotDisplayed()
    }

    /**
     * Ngày giỗ tháng nhuận: 1987 nhuận tháng 7, nên "chỉ tháng nhuận" tính được;
     * còn năm thường thì phải nói là không có.
     *
     * Sai thì: người dùng chọn "chỉ tháng nhuận" nhưng app vẫn lặng lẽ giỗ tháng thường.
     */
    @Test
    fun ngay_gio_chi_thang_nhuan_bao_ro_khi_nam_khong_nhuan() {
        seed("Cụ nhuận", 15, 7, MemorialRule(leapMonthPolicy = LeapMonthPolicy.LEAP_MONTH_ONLY))
        launch()
        openMemorials()
        // Từ 2026 trở đi, tháng 7 nhuận rất hiếm ⇒ hoặc có ngày, hoặc nói rõ là không.
        val hasDate = runBlocking {
            val m = env.memorialRepository.observe(familyId).first().single()
            env.container.memorialResolver.nextOccurrence(m, java.time.LocalDate.now())
        }
        if (hasDate == null) {
            rule.onNodeWithText("Chưa tính được ngày giỗ tới", substring = true).assertIsDisplayed()
        } else {
            assertTrue("phải rơi vào tháng nhuận", hasDate.isLeapMonth)
        }
    }

    /**
     * Liên kết ngày giỗ với một thành viên: danh sách phải hiện **tên hiện tại của
     * thành viên**, và đổi tên thành viên thì ngày giỗ đổi theo.
     *
     * Sai thì: liên kết trở nên vô nghĩa — người dùng vẫn phải sửa tên ở hai chỗ.
     */
    @Test
    fun lien_ket_thanh_vien_va_doi_ten_thi_ngay_gio_doi_theo() {
        val memberId = runBlocking {
            env.memberRepository.add(
                familyId,
                com.nepnha.domain.model.FamilyMemberDraft(
                    fullName = "Nguyễn Văn A",
                    gender = com.nepnha.domain.model.Gender.MALE,
                    solarBirthDate = null, lunarBirthDate = null, role = null, note = null,
                ),
            )
        }
        val id = runBlocking {
            env.memorialRepository.add(
                familyId,
                MemorialDraft("Tên cũ", 5, 5, MemorialRule(), null, memberId = memberId),
            )
        }

        launch()
        openMemorials()
        // Hiện tên THÀNH VIÊN, không phải "Tên cũ" đã lưu trong ngày giỗ.
        rule.onNodeWithText("Nguyễn Văn A", substring = true).assertIsDisplayed()

        // Đổi tên thành viên ⇒ danh sách ngày giỗ đổi theo, không phải sửa hai chỗ.
        runBlocking {
            env.memberRepository.update(
                memberId,
                com.nepnha.domain.model.FamilyMemberDraft(
                    fullName = "Nguyễn Văn B",
                    gender = com.nepnha.domain.model.Gender.MALE,
                    solarBirthDate = null, lunarBirthDate = null, role = null, note = null,
                ),
            )
        }
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Nguyễn Văn B", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        assertEquals(memberId, runBlocking { env.memorialRepository.get(id)!!.memberId })
    }

    /**
     * Xoá thành viên chỉ làm đứt liên kết — ngày giỗ vẫn còn và quay về tên đã lưu.
     *
     * Sai thì: xoá một người trong danh sách gia đình làm bay mất ngày giỗ của họ.
     */
    @Test
    fun xoa_thanh_vien_thi_ngay_gio_quay_ve_ten_da_luu() {
        val memberId = runBlocking {
            env.memberRepository.add(
                familyId,
                com.nepnha.domain.model.FamilyMemberDraft(
                    fullName = "Nguyễn Văn A",
                    gender = com.nepnha.domain.model.Gender.MALE,
                    solarBirthDate = null, lunarBirthDate = null, role = null, note = null,
                ),
            )
        }
        runBlocking {
            env.memorialRepository.add(
                familyId,
                MemorialDraft("Cụ ông Nguyễn Văn A", 5, 5, MemorialRule(), null, memberId = memberId),
            )
            env.memberRepository.delete(memberId)
        }

        launch()
        openMemorials()
        rule.onNodeWithText("Cụ ông Nguyễn Văn A", substring = true).assertIsDisplayed()
        val after = runBlocking { env.memorialRepository.observe(familyId).first() }
        assertEquals(1, after.size)
        org.junit.Assert.assertNull("liên kết phải đứt", after.single().memberId)
    }

    /**
     * Ngày giỗ phải hiện trên màn Lịch đúng ngày dương đã quy đổi.
     *
     * Sai thì: lịch và danh sách nói hai chuyện khác nhau.
     */
    @Test
    fun ngay_gio_hien_tren_man_Lich() {
        val today = java.time.LocalDate.now()
        val lunarToday = env.container.lunarCalendar.dayOf(today)
        val lunar = (lunarToday as com.nepnha.domain.calendar.LunarDay.Known).lunar
        // Đặt ngày giỗ đúng vào hôm nay để chắc chắn nó nằm trong tháng đang xem.
        val id = seed("Giỗ hôm nay", lunar.day, lunar.month)

        launch()
        rule.onNodeWithTag("tab_calendar").performClick()
        // Ô ngày là một node `clickable` nên Compose GỘP semantics của con vào nó —
        // testTag của chấm đánh dấu không xuất hiện trong merged tree. Kiểm bằng
        // contentDescription của chính ô, cũng là thứ người dùng screen reader nghe.
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithContentDescription("có ngày giỗ", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithContentDescription("có ngày giỗ", substring = true).assertIsDisplayed()
        rule.onNodeWithTag("day_$today").performClick()
        rule.onNodeWithTag("calendar_memorial_$id").assertIsDisplayed()
    }
}
