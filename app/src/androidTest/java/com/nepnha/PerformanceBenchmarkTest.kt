package com.nepnha

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nepnha.domain.event.MemorialDateResolver
import com.nepnha.domain.event.MemorialRule
import com.nepnha.domain.event.LeapMonthPolicy
import com.nepnha.domain.event.MissingDayPolicy
import com.nepnha.domain.model.Memorial
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Đo hiệu năng trên **thiết bị thật**, không phải trên JVM máy dev.
 *
 * Nguyên tắc: **đo trước, tối ưu sau**. Test này không tối ưu gì cả — nó chỉ in ra
 * số đo và khẳng định các ngưỡng chấp nhận đã tuyên bố. Ngưỡng đặt rộng rãi vì mục
 * đích là bắt hồi quy thảm hoạ, không phải khoá chặt một con số.
 *
 * Cách đo: mỗi phép chạy nóng máy vài vòng rồi lấy **trung vị** của nhiều lần lặp —
 * trung vị chứ không phải trung bình, để một lần GC không làm lệch kết quả.
 */
@RunWith(AndroidJUnit4::class)
class PerformanceBenchmarkTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val calendar = AppContainer.loadLunarCalendar(context)
    private val resolver = MemorialDateResolver(calendar)
    private val today = LocalDate.of(2026, 8, 26)

    private fun memorials(n: Int): List<Memorial> = (1..n).map {
        Memorial(
            id = it.toLong(), familyId = 1, name = "Cụ $it",
            lunarDay = (it % 30) + 1, lunarMonth = (it % 12) + 1,
            rule = MemorialRule(), note = null,
        )
    }

    /**
     * Lần gọi **đầu tiên**, không làm nóng gì cả — đây là thứ người dùng thực sự chờ
     * khi mở app lần đầu, lúc ART còn thông dịch.
     */
    private fun firstCallMs(label: String, block: () -> Unit): Double {
        val t0 = System.nanoTime()
        block()
        val ms = (System.nanoTime() - t0) / 1_000_000.0
        println("PERF %-42s LẦN ĐẦU  %8.2f ms".format(label, ms))
        return ms
    }

    /**
     * Trung vị ở **trạng thái ổn định**, sau khi làm nóng đủ để ART biên dịch đường
     * nóng. Bản đầu chỉ làm nóng 3 vòng và cho ra 84 ms cho một tháng trong khi 365
     * ngày chỉ mất 29 ms — con số vô lý đó là **lỗi phương pháp đo**, không phải lỗi
     * sản phẩm. Làm nóng đủ thì hai phép đo mới so được với nhau.
     */
    private fun medianMs(label: String, warmup: Int = 200, times: Int = 21, block: () -> Unit): Double {
        repeat(warmup) { block() }
        val samples = (1..times).map {
            val t0 = System.nanoTime()
            block()
            (System.nanoTime() - t0) / 1_000_000.0
        }.sorted()
        val median = samples[samples.size / 2]
        println("PERF %-42s trung vị %8.2f ms   (min %.2f, max %.2f)"
            .format(label, median, samples.first(), samples.last()))
        return median
    }

    /**
     * Sai thì: một trong các thao tác thường xuyên đã chậm tới mức người dùng cảm
     * thấy giật. Ngưỡng 16 ms = một khung hình 60 Hz.
     */
    @Test
    fun do_hieu_nang_cac_thao_tac_thuong_gap() {
        // --- lần đầu, ART còn thông dịch: đây là độ trễ người dùng thật sự gặp ---
        val nạpLầnĐầu = firstCallMs("nạp dataset + dựng engine") {
            AppContainer.loadLunarCalendar(context)
        }
        val thángLầnĐầu = firstCallMs("dựng lưới 1 tháng (31 ngày)") {
            AppContainer.loadLunarCalendar(context).daysOfMonth(YearMonth.of(2026, 8))
        }

        // --- trạng thái ổn định ---
        val nạpDataset = medianMs("nạp dataset + dựng engine", warmup = 5, times = 11) {
            AppContainer.loadLunarCalendar(context)
        }
        val mộtTháng = medianMs("dựng lưới 1 tháng (31 ngày)") {
            calendar.daysOfMonth(YearMonth.of(2026, 8))
        }
        val mộtNămDương = medianMs("365 lượt chuyển đổi") {
            var d = LocalDate.of(2026, 1, 1)
            repeat(365) { calendar.dayOf(d); d = d.plusDays(1) }
        }
        val mộtNgàyGiỗ = medianMs("quy đổi 1 ngày giỗ") {
            resolver.nextOccurrence(memorials(1).first(), today)
        }
        val m10 = memorials(10); val m50 = memorials(50); val m100 = memorials(100)
        val g10 = medianMs("quy đổi 10 ngày giỗ") { resolver.upcoming(m10, today) }
        val g50 = medianMs("quy đổi 50 ngày giỗ") { resolver.upcoming(m50, today) }
        val g100 = medianMs("quy đổi 100 ngày giỗ") { resolver.upcoming(m100, today) }
        val marker = medianMs("marker lịch cho 50 ngày giỗ / 1 tháng") {
            resolver.occurrencesBetween(m50, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31))
        }
        // Trường hợp tệ nhất đã biết: chỉ tháng nhuận + bỏ qua năm thiếu, dò tới hết
        // phạm vi dữ liệu (~200 năm âm) rồi mới trả null.
        val tệNhất = medianMs("quy đổi TỆ NHẤT (dò hết 1901-2100)") {
            resolver.nextOccurrence(
                Memorial(
                    1, 1, "x", 30, 4,
                    MemorialRule(LeapMonthPolicy.LEAP_MONTH_ONLY, MissingDayPolicy.SKIP), null,
                ),
                LocalDate.of(2099, 1, 1),
            )
        }

        // --- ngưỡng chấp nhận, tuyên bố trước khi đo ---
        assertTrue("nạp lần đầu $nạpLầnĐầu ms — người dùng chờ quá lâu", nạpLầnĐầu < 500)
        assertTrue("lưới tháng lần đầu $thángLầnĐầu ms — mở màn Lịch giật", thángLầnĐầu < 500)
        assertTrue("nạp dataset $nạpDataset ms — quá chậm cho khởi động", nạpDataset < 300)
        assertTrue("lưới 1 tháng $mộtTháng ms — vượt 1 khung hình", mộtTháng < 16)
        assertTrue("365 lượt $mộtNămDương ms", mộtNămDương < 200)
        assertTrue("1 ngày giỗ $mộtNgàyGiỗ ms", mộtNgàyGiỗ < 16)
        assertTrue("10 ngày giỗ $g10 ms", g10 < 16)
        assertTrue("50 ngày giỗ $g50 ms", g50 < 50)
        assertTrue("100 ngày giỗ $g100 ms", g100 < 100)
        assertTrue("marker lịch $marker ms — vượt 1 khung hình", marker < 16)
        assertTrue("tệ nhất $tệNhất ms — người dùng sẽ thấy giật", tệNhất < 300)
    }
}
