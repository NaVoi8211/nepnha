package com.nepnha.ui

import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nepnha.MainActivity
import com.nepnha.core.time.VietnameseDateFormatter
import com.nepnha.domain.calendar.LunarDay
import com.nepnha.domain.event.MemorialRule
import com.nepnha.domain.model.MemorialDraft
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Xoay máy = hệ thống **huỷ Activity rồi dựng lại từ đầu**. Bộ test này kiểm đúng
 * chuyện đó, trên `MainActivity` thật.
 *
 * Vì sao phải là `MainActivity` chứ không phải `ComponentActivity` + `rule.setContent`:
 * `setContent` gọi từ test gắn vào **Activity cũ**, nên sau `recreate()` phải gọi lại
 * bằng tay — và lúc đó ta đang kiểm một đường mà app thật không đi. `MainActivity` tự
 * gọi `setContent` trong `onCreate`, nên `recreate()` chạy đúng dòng đời thật: Activity
 * mới, `NavController` khôi phục từ saved state, `ViewModelStore` được giữ lại.
 *
 * Bộ test này **không ghi gì vào database**: màn Lịch không cần dữ liệu, còn màn soạn
 * ngày giỗ chỉ gõ vào form rồi bỏ đi, không bấm Lưu. Nó dùng container thật của app nên
 * sẽ nhìn thấy dữ liệu đang có trên máy — nhưng không sửa gì.
 */
@RunWith(AndroidJUnit4::class)
class RotationStateTest {

    // Xem chú thích ở NavigationTest: bản v2 của rule làm hỏng cả bộ test.
    @Suppress("DEPRECATION")
    @get:Rule
    val rule = createAndroidComposeRule<MainActivity>()

    /**
     * Gom text của một node **và mọi node con** — để so sánh trước và sau khi Activity
     * dựng lại.
     *
     * Phải đi xuống cây con: `calendar_selected` gắn trên một `InfoCard`, tức là một
     * container không tự gộp semantics, nên bản thân node đó **không có** thuộc tính
     * `Text` nào. Đọc mỗi node gắn tag thì được chuỗi rỗng và phép so sánh trở thành
     * "rỗng bằng rỗng" — một test luôn xanh mà chẳng chứng minh gì.
     */
    /**
     * Đọc **đúng** nội dung người dùng đã gõ trong một ô nhập.
     *
     * Tách khỏi [textOf] vì gom cả cây con sẽ nhặt luôn nhãn của ô ("Tên người mất")
     * và phép so sánh trở nên phụ thuộc vào chuỗi giao diện, không phải vào dữ liệu.
     */
    private fun fieldTextOf(tag: String): String =
        rule.onNodeWithTag(tag, useUnmergedTree = true)
            .fetchSemanticsNode()
            .config
            .getOrElse(SemanticsProperties.EditableText) { AnnotatedString("") }
            .text

    private fun textOf(tag: String): String {
        fun collect(node: SemanticsNode): List<String> {
            val here = buildList {
                node.config.getOrElse(SemanticsProperties.Text) { emptyList() }
                    .forEach { add(it.text) }
                node.config.getOrElse(SemanticsProperties.EditableText) { AnnotatedString("") }
                    .text.takeIf { it.isNotEmpty() }?.let { add(it) }
            }
            return here + node.children.flatMap { collect(it) }
        }
        val node = rule.onNodeWithTag(tag, useUnmergedTree = true).fetchSemanticsNode()
        return collect(node).joinToString(" ").trim()
    }

    /**
     * Chờ một tag xuất hiện — **luôn** trên cây chưa gộp.
     *
     * `useUnmergedTree = true` không phải tuỳ chọn: `home_countdown_<id>` nằm trong một
     * hàng `clickable`, mà `clickable` gộp semantics của con vào cha. Trên cây đã gộp,
     * tag bên trong **không tồn tại** — chờ nó là chờ mãi, và lỗi hiện ra dưới dạng
     * "hết giờ" chứ không phải "sai chỗ tìm".
     */
    private fun waitForTag(tag: String, timeout: Long = 10_000) {
        rule.waitUntil(timeoutMillis = timeout) {
            rule.onAllNodesWithTag(tag, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * Dựng lại Activity đúng như hệ thống làm khi xoay máy, rồi chờ giao diện mới sẵn sàng.
     */
    private fun recreateActivity(waitFor: String) {
        rule.activityRule.scenario.recreate()
        rule.waitForIdle()
        waitForTag(waitFor)
    }

    /**
     * Màn Lịch: tháng đang xem và ngày đang chọn phải sống sót qua lần dựng lại.
     *
     * Sai thì: người dùng lật tới tháng 10, chọn ngày giỗ cần xem, xoay máy một cái là
     * quay về tháng này — mất chỗ đang đứng, phải lật lại từ đầu.
     */
    @Test
    fun man_lich_giu_nguyen_thang_va_ngay_dang_chon_sau_khi_dung_lai() {
        rule.onNodeWithTag("tab_calendar").performClick()
        waitForTag("screen_calendar")

        // Lật hai tháng để chắc chắn không còn ở tháng mặc định, rồi chọn một ngày cụ thể.
        rule.onNodeWithTag("calendar_next").performClick()
        rule.onNodeWithTag("calendar_next").performClick()
        rule.waitForIdle()

        val target = YearMonth.from(LocalDate.now()).plusMonths(2).atDay(15)
        rule.onNodeWithTag("day_$target").performClick()
        rule.waitForIdle()

        val monthBefore = textOf("calendar_month_title")
        val selectedBefore = textOf("calendar_selected")
        assertTrue("thẻ ngày đang chọn phải có nội dung", selectedBefore.isNotBlank())
        assertTrue(
            "thẻ ngày đang chọn phải nói về ngày 15, nhận được: $selectedBefore",
            selectedBefore.contains("15"),
        )

        recreateActivity("screen_calendar")

        assertEquals("tháng đang xem phải giữ nguyên", monthBefore, textOf("calendar_month_title"))
        assertEquals("ngày đang chọn phải giữ nguyên", selectedBefore, textOf("calendar_selected"))
        // Và ô ngày đó vẫn phải nằm trên lưới.
        rule.onNodeWithTag("day_$target").assertIsDisplayed()
    }

    /**
     * Màn soạn ngày giỗ: chữ người dùng đã gõ mà chưa lưu phải còn sau khi dựng lại.
     *
     * Sai thì: đang nhập ngày giỗ của cụ, lỡ nghiêng máy, gõ lại từ đầu.
     */
    @Test
    fun form_ngay_gio_chua_luu_khong_bi_mat_sau_khi_dung_lai() {
        rule.onNodeWithTag("tab_home").performClick()
        waitForTag("screen_home")

        // Vào danh sách ngày giỗ: nút đổi theo việc máy đã có ngày giỗ hay chưa, nên
        // nhận cả hai lối chứ không giả định trạng thái database của máy đang chạy test.
        if (rule.onAllNodesWithTag("home_memorial_all").fetchSemanticsNodes().isNotEmpty()) {
            rule.onNodeWithTag("home_memorial_all").performClick()
        } else {
            rule.onAllNodesWithText("Thêm ngày giỗ")[0].performClick()
        }
        waitForTag("screen_memorials")

        rule.onNodeWithTag("memorial_add").performClick()
        waitForTag("screen_memorial_editor")

        val typed = "Cụ tổ khảo xoay máy"
        rule.onNodeWithTag("memorial_name").performTextInput(typed)
        rule.waitForIdle()
        assertEquals("gõ vào chưa được thì phần sau vô nghĩa", typed, fieldTextOf("memorial_name"))

        recreateActivity("screen_memorial_editor")

        assertEquals("chữ đã gõ phải còn nguyên", typed, fieldTextOf("memorial_name"))
    }

    /**
     * Ngày trên màn Nhà phải đúng ngay sau khi dựng lại — không phải chờ lần resume sau.
     *
     * Sai thì: xoay máy xong màn hình chính trống ngày, hoặc hiện ngày cũ.
     */
    @Test
    fun ngay_tren_man_nha_van_dung_sau_khi_dung_lai() {
        rule.onNodeWithTag("tab_home").performClick()
        waitForTag("home_solar_date")

        val dateBefore = textOf("home_solar_date")
        val lunarBefore = textOf("home_lunar_date")
        assertTrue("ngày dương phải có nội dung", dateBefore.isNotBlank())

        recreateActivity("home_solar_date")

        assertEquals("ngày dương phải giữ nguyên", dateBefore, textOf("home_solar_date"))
        assertEquals("ngày âm phải giữ nguyên", lunarBefore, textOf("home_lunar_date"))
        // Không chỉ "không đổi" — phải đúng bằng ngày thật của máy, dựng bằng chính
        // formatter của sản phẩm. "Không đổi" cũng đúng khi cả hai lần đều sai.
        assertEquals(
            "ngày dương phải đúng bằng hôm nay",
            VietnameseDateFormatter.fullDate(LocalDate.now()),
            textOf("home_solar_date"),
        )
    }

    // ----------------------------------------------------- giá trị nghiệp vụ + xoay thật

    /** Container thật của app — cùng database mà người dùng đang dùng. */
    private val container get() =
        (rule.activity.application as com.nepnha.NepNhaApp).container

    /** Ngày giỗ do test tạo, dọn sạch ở [tearDown] dù test đỏ hay xanh. */
    private var seededMemorialId: Long? = null

    @After
    fun tearDown() {
        seededMemorialId?.let { id -> runBlocking { container.memorialRepository.delete(id) } }
    }

    /**
     * Gieo một ngày giỗ rơi đúng **ngày mai**, để đếm ngược có một giá trị biết trước.
     *
     * Dùng ngày âm của mai chứ không phải một ngày cố định: như vậy khẳng định
     * "Ngày mai" đúng vào bất kỳ hôm nào chạy test.
     */
    private fun seedTomorrowMemorial(): Long = runBlocking {
        val familyId = container.familyRepository.ensureDefaultFamily("Gia đình tôi")
        val lunar = (container.lunarCalendar.dayOf(LocalDate.now().plusDays(1)) as LunarDay.Known).lunar
        container.memorialRepository.add(
            familyId,
            MemorialDraft("Cụ xoay máy", lunar.day, lunar.month, MemorialRule(), null),
        ).also { seededMemorialId = it }
    }

    /**
     * Sau khi dựng lại Activity, ngày giỗ phải giữ **giá trị nghiệp vụ** — tên, đếm
     * ngược — và **không được nhân bản** trong database.
     *
     * Sai thì: xoay máy một cái là danh sách ngày giỗ dài gấp đôi, hoặc đếm ngược nhảy
     * sai một ngày. Kiểm bằng chuỗi người dùng đọc được, không phải bằng `assertIsDisplayed`.
     */
    @Test
    fun ngay_gio_giu_gia_tri_nghiep_vu_va_khong_nhan_ban_sau_khi_dung_lai() {
        val id = seedTomorrowMemorial()
        val familyId = runBlocking { container.familyRepository.observeFamily().first()!!.id }
        val rowsBefore = runBlocking { container.memorialRepository.observe(familyId).first().size }

        rule.onNodeWithTag("tab_home").performClick()
        waitForTag("home_countdown_$id")

        assertEquals("ngày giỗ rơi vào mai phải hiện đúng chữ", "Ngày mai", textOf("home_countdown_$id"))
        val rowBefore = textOf("home_upcoming_$id")
        assertTrue("dòng ngày giỗ phải mang tên đã lưu", rowBefore.contains("Cụ xoay máy"))

        recreateActivity("home_countdown_$id")

        assertEquals("đếm ngược phải giữ nguyên", "Ngày mai", textOf("home_countdown_$id"))
        assertEquals("cả dòng phải giữ nguyên từng chữ", rowBefore, textOf("home_upcoming_$id"))
        assertEquals(
            "dựng lại Activity KHÔNG được nhân bản bản ghi",
            rowsBefore,
            runBlocking { container.memorialRepository.observe(familyId).first().size },
        )
    }

    /**
     * **Xoay màn hình thật**, không phải `recreate()` mô phỏng.
     *
     * `requestedOrientation` bắt hệ thống đổi cấu hình thật sự; `MainActivity` không khai
     * `configChanges` nên nó bị huỷ và dựng lại đúng như khi người dùng nghiêng máy.
     *
     * Sai thì: nghiêng máy là mất chỗ đang đứng trên lịch.
     */
    @Test
    fun xoay_ngang_roi_doc_that_su_giu_nguyen_thang_va_ngay_dang_chon() {
        rule.onNodeWithTag("tab_calendar").performClick()
        waitForTag("screen_calendar")
        rule.onNodeWithTag("calendar_next").performClick()
        rule.waitForIdle()

        val target = YearMonth.from(LocalDate.now()).plusMonths(1).atDay(12)
        rule.onNodeWithTag("day_$target").performClick()
        rule.waitForIdle()
        val monthBefore = textOf("calendar_month_title")
        val selectedBefore = textOf("calendar_selected")

        for (orientation in listOf(
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
        )) {
            rule.activityRule.scenario.onActivity { it.requestedOrientation = orientation }
            rule.waitForIdle()
            waitForTag("screen_calendar")
            assertEquals("tháng phải giữ nguyên qua lần xoay", monthBefore, textOf("calendar_month_title"))
            assertEquals("ngày đang chọn phải giữ nguyên qua lần xoay", selectedBefore, textOf("calendar_selected"))
        }
        rule.activityRule.scenario.onActivity {
            it.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
}
