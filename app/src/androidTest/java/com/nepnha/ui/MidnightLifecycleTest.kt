package com.nepnha.ui

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.lifecycle.Lifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nepnha.data.TestEnvironment
import com.nepnha.domain.calendar.LunarDay
import com.nepnha.domain.event.MemorialRule
import com.nepnha.domain.model.MemorialDraft
import com.nepnha.ui.theme.NepNhaTheme
import java.time.LocalDate
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Vòng đời qua nửa đêm — lỗi đã ghi ở `PHASE_5_AUDIT.md §C`.
 *
 * Trước Gate 2, "hôm nay" bị chốt lúc tạo ViewModel: app mở qua 00:00 vẫn hiện ngày
 * hôm qua, và ngày giỗ **đúng hôm nay** bị hiện thành "Ngày mai" — thông tin sai trên
 * màn hình quan trọng nhất.
 *
 * Test dùng **nguồn ngày giả** nên không chờ đồng hồ thật một giây nào. Không có bộ
 * đếm, không `AlarmManager`, không việc chạy nền nào được thêm để làm test này xanh.
 */
@RunWith(AndroidJUnit4::class)
class MidnightLifecycleTest {

    // Xem chú thích ở NavigationTest: bản v2 của rule làm hỏng cả bộ test.
    @Suppress("DEPRECATION")
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private lateinit var env: TestEnvironment
    private var familyId: Long = 0

    /** Hôm qua và hôm nay, chọn cố định để kết quả không phụ thuộc ngày chạy test. */
    private val dayBefore = LocalDate.of(2026, 9, 10)
    private val theDay = LocalDate.of(2026, 9, 11)

    @Before
    fun setUp() {
        env = TestEnvironment(rule.activity)
        env.fakeToday = dayBefore
        runBlocking {
            env.familyRepository.ensureDefaultFamily("Gia đình tôi")
            familyId = env.familyRepository.observeFamily().first()!!.id
        }
    }

    @After
    fun tearDown() = env.close()

    /**
     * Ngày giỗ rơi đúng vào [theDay]: hôm trước phải là "Ngày mai", sau khi qua nửa
     * đêm và quay lại tiền cảnh phải là **đúng chữ "Hôm nay"** — không phải "Ngày mai",
     * không phải "Còn 0 ngày".
     *
     * Sai thì: đúng ngày phải làm cỗ mà app vẫn bảo là ngày mai.
     */
    @Test
    fun qua_nua_dem_thi_ngay_gio_hom_nay_hien_dung_chu_Hom_nay() {
        // Lấy ngày âm của theDay để đặt ngày giỗ rơi đúng vào hôm đó.
        val lunar = (env.container.lunarCalendar.dayOf(theDay) as LunarDay.Known).lunar
        val id = runBlocking {
            env.memorialRepository.add(
                familyId,
                MemorialDraft("Cụ ông", lunar.day, lunar.month, MemorialRule(), null),
            )
        }

        rule.setContent { NepNhaTheme { NepNhaShell(container = env.container) } }
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithTag("home_countdown_$id", useUnmergedTree = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        rule.onNodeWithTag("home_countdown_$id", useUnmergedTree = true)
            .assertTextEquals("Ngày mai")
        rule.onNodeWithTag("home_solar_date").assertTextEquals("10 tháng 9, 2026")

        // --- qua nửa đêm, rồi app quay lại tiền cảnh ---
        env.fakeToday = theDay
        rule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        rule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)

        rule.waitUntil(timeoutMillis = 5_000) {
            runCatching {
                rule.onNodeWithTag("home_countdown_$id", useUnmergedTree = true)
                    .assertTextEquals("Hôm nay")
            }.isSuccess
        }
        // Đúng chữ "Hôm nay", không phải "Ngày mai", không phải "Còn 0 ngày".
        rule.onNodeWithTag("home_countdown_$id", useUnmergedTree = true)
            .assertTextEquals("Hôm nay")
        // Ngày dương trên đầu màn Nhà cũng phải đổi theo.
        rule.onNodeWithTag("home_solar_date").assertTextEquals("11 tháng 9, 2026")
    }

}
