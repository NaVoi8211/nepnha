package com.nepnha.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nepnha.AppContainer
import com.nepnha.core.time.VietnameseLunarFormatter
import com.nepnha.data.TestEnvironment
import com.nepnha.domain.calendar.LunarDay
import com.nepnha.ui.calendar.CalendarScreen
import com.nepnha.ui.calendar.CalendarViewModel
import com.nepnha.ui.theme.NepNhaTheme
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Lịch âm chạy thật trên thiết bị: asset nạp được từ APK, và những gì màn hình hiện
 * lên đúng bằng những gì tầng domain trả về.
 *
 * Test tính đúng của lịch nằm ở bộ unit test; ở đây kiểm **việc nối dây** và **việc
 * đóng gói** — hai thứ unit test trên JVM không chứng minh được.
 */
@RunWith(AndroidJUnit4::class)
class LunarIntegrationTest {

    // Xem chú thích ở NavigationTest: bản v2 của rule làm hỏng cả bộ test.
    @Suppress("DEPRECATION")
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var env: TestEnvironment

    @Before
    fun setUp() {
        env = TestEnvironment(rule.activity)
        runBlocking { env.familyRepository.ensureDefaultFamily("Gia đình tôi") }
    }

    @After
    fun tearDown() = env.close()

    private fun launchShell() {
        rule.setContent { NepNhaTheme { NepNhaShell(container = env.container) } }
    }

    /**
     * Sai thì: dataset không được đóng gói vào APK, hoặc AppContainer nạp hỏng —
     * người dùng mở app ra thấy "lịch âm tạm thời chưa dùng được".
     */
    @Test
    fun man_Nha_hien_ngay_am_that_tu_asset_trong_APK() {
        val service = AppContainer.loadLunarCalendar(rule.activity)
        val today = service.dayOf(LocalDate.now())
        assertTrue("không nạp được dataset từ APK", today is LunarDay.Known)

        launchShell()
        rule.onNodeWithTag("home_lunar_date").assertIsDisplayed()
        // Đúng bằng thứ tầng domain trả về, không phải một chuỗi khác.
        val known = today as LunarDay.Known
        rule.onNodeWithText(VietnameseLunarFormatter.full(known.lunar, known.sexagenaryYear))
            .assertIsDisplayed()
    }

    /**
     * Sai thì: tab Lịch vẫn là màn hình giữ chỗ, hoặc lưới không dựng được.
     */
    @Test
    fun man_Lich_hien_luoi_thang_kem_ngay_am() {
        launchShell()
        rule.onNodeWithTag("tab_calendar").performClick()
        rule.onNodeWithTag("calendar_month_title").assertIsDisplayed()
        rule.onNodeWithTag("calendar_selected").assertIsDisplayed()
        rule.onNodeWithTag("day_${LocalDate.now().withDayOfMonth(1)}").assertIsDisplayed()
    }

    /**
     * Bấm một ngày trong lưới thì thẻ chi tiết phải đổi theo.
     *
     * Sai thì: lưới và thẻ chi tiết nói hai chuyện khác nhau.
     */
    @Test
    fun bam_mot_ngay_thi_the_chi_tiet_doi_theo() {
        val service = AppContainer.loadLunarCalendar(rule.activity)
        val target = LocalDate.now().withDayOfMonth(15)
        val expected = service.dayOf(target) as LunarDay.Known

        launchShell()
        rule.onNodeWithTag("tab_calendar").performClick()
        rule.onNodeWithTag("day_$target").performClick()
        rule.onNodeWithText(
            VietnameseLunarFormatter.dayAndMonth(expected.lunar),
            substring = true,
        ).assertIsDisplayed()
    }

    /**
     * Tháng nhuận trên thiết bị thật: tháng 8 thường và tháng 8 nhuận của năm 1938
     * phải hiện khác nhau, và chữ "nhuận" phải có mặt.
     *
     * Sai thì: hai lần xuất hiện của tháng 8 trông giống hệt nhau — đúng loại lỗi
     * làm sai ngày giỗ.
     */
    @Test
    fun thang_nhuan_1938_hien_ro_chu_nhuan_tren_thiet_bi() {
        val service = AppContainer.loadLunarCalendar(rule.activity)
        val vm = CalendarViewModel(service, LocalDate.of(1938, 9, 24))
        rule.setContent {
            // Đọc qua `collectAsStateWithLifecycle` chứ không phải `.value`: ở đây state
            // đứng yên nên cả hai cùng chạy được, nhưng `.value` trong composition là
            // thói quen sai — nó không đăng ký nhận cập nhật.
            val state by vm.state.collectAsStateWithLifecycle()
            NepNhaTheme {
                CalendarScreen(
                    state = state,
                    onPreviousMonth = {},
                    onNextMonth = {},
                    onSelectDay = {},
                )
            }
        }
        rule.onNodeWithText("1 tháng 8 nhuận", substring = true).assertIsDisplayed()
        rule.onNodeWithTag("day_1938-09-24").assertIsDisplayed()
        rule.onNodeWithTag("day_1938-09-23").assertIsDisplayed()
    }
}
