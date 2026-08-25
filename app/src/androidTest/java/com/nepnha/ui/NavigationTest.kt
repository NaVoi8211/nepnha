package com.nepnha.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nepnha.R
import com.nepnha.ui.theme.NepNhaTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Kiểm tra bộ khung điều hướng: bốn tab, luồng vào màn hình con, nút Back.
 *
 * Chạy trên thiết bị thật (Samsung A32).
 *
 * Bám vào `testTag` cho màn hình và tab (đổi câu chữ không làm vỡ test); riêng nút
 * bấm thì tra bằng chuỗi lấy từ `strings.xml` — nếu đổi chữ mà quên đổi ở đây thì
 * test hỏng đúng chỗ cần hỏng.
 *
 * Cố ý KHÔNG dùng `TestNavHostController` để khỏi thêm dependency
 * `navigation-testing`: màn hình nào đang hiển thị đã là bằng chứng đủ cho một app
 * shell.
 */
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    // Đã thử `junit4.v2.createAndroidComposeRule` (bản khuyến nghị mới): với
    // StandardTestDispatcher, `setContent` trong @Before không được compose kịp và
    // cả 4 test hỏng với "No compose hierarchies found" — kể cả khi thêm
    // waitForIdle(). Giữ bản v1 (chỉ deprecated, chạy đúng); sẽ migrate khi có
    // hướng dẫn rõ ràng, ghi lại đây để lần sau khỏi thử lại vô ích.
    @Suppress("DEPRECATION")
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun setUp() {
        rule.setContent {
            NepNhaTheme { NepNhaShell() }
        }
    }

    private fun text(resId: Int): String = rule.activity.getString(resId)

    @Test
    fun mo_app_thi_dung_o_man_hinh_Nha() {
        rule.onNodeWithTag("screen_home").assertIsDisplayed()
        rule.onNodeWithTag("bottom_bar").assertIsDisplayed()
    }

    @Test
    fun bon_tab_deu_dieu_huong_duoc() {
        rule.onNodeWithTag("tab_calendar").performClick()
        rule.onNodeWithTag("screen_calendar").assertIsDisplayed()

        rule.onNodeWithTag("tab_family").performClick()
        rule.onNodeWithTag("screen_family").assertIsDisplayed()

        rule.onNodeWithTag("tab_settings").performClick()
        rule.onNodeWithTag("screen_settings").assertIsDisplayed()

        rule.onNodeWithTag("tab_home").performClick()
        rule.onNodeWithTag("screen_home").assertIsDisplayed()
    }

    @Test
    fun vao_man_hinh_con_roi_back_thi_quay_lai_Gia_dinh() {
        rule.onNodeWithTag("tab_family").performClick()
        rule.onNodeWithTag("screen_family").assertIsDisplayed()

        rule.onNodeWithText(text(R.string.family_add_member)).performClick()
        rule.onNodeWithTag("screen_add_member").assertIsDisplayed()
        // Ở màn hình con, thanh dưới phải biến mất.
        rule.onNodeWithTag("bottom_bar").assertDoesNotExist()

        rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }
        rule.waitForIdle()
        rule.onNodeWithTag("screen_family").assertIsDisplayed()
        rule.onNodeWithTag("bottom_bar").assertIsDisplayed()
    }

    @Test
    fun nut_thiet_lap_gia_dinh_o_man_Nha_dan_sang_tab_Gia_dinh() {
        rule.onNodeWithTag("screen_home").assertIsDisplayed()
        rule.onNodeWithText(text(R.string.home_setup_family)).performClick()
        rule.onNodeWithTag("screen_family").assertIsDisplayed()
    }
}
