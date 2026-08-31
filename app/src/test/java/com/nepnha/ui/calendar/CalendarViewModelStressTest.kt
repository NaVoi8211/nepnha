package com.nepnha.ui.calendar

import com.nepnha.core.lunar.LunarTestSupport
import com.nepnha.domain.calendar.LunarCalendarService
import com.nepnha.domain.event.MemorialRule
import com.nepnha.domain.model.FamilyMember
import com.nepnha.domain.model.Gender
import com.nepnha.domain.model.Memorial
import java.time.LocalDate
import java.time.YearMonth
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Truy một lần đỏ **chưa rõ nguyên nhân** của `CalendarViewModelTest`.
 *
 * Bối cảnh: `chon ngay o thang khac thi luoi doi theo` đỏ đúng một lần rồi pass 468 lượt
 * sau đó, và thông báo lỗi đã mất. "Chạy lại thấy xanh" không phải là một lời giải thích,
 * nên bộ này đi tìm điều kiện làm nó đỏ thay vì chờ nó đỏ lại.
 *
 * Giả thuyết đang kiểm — đọc ra từ chính `CalendarViewModel`:
 *
 *  · `view`, `today`, `knownMemorials`, `knownMembers` là `var` **không đồng bộ**;
 *  · `init` mở hai coroutine trên `viewModelScope`, và **cả hai đều ghi `_state.value`**;
 *  · trong app thật `viewModelScope` là `Dispatchers.Main.immediate`, nên mọi lần ghi
 *    nằm trên cùng một luồng và không có race;
 *  · trong unit test **không gọi `Dispatchers.setMain`**, `viewModelScope` của
 *    lifecycle 2.11 lùi về một context không có dispatcher ⇒ coroutine chạy trên
 *    `Dispatchers.Default`, tức là **luồng khác** với luồng test.
 *
 * Nếu giả thuyết đúng thì luồng nền có thể đọc `view` cũ (không có rào cản bộ nhớ nào
 * bảo đảm nhìn thấy giá trị mới) rồi ghi đè `_state.value` bằng tháng cũ — đúng kiểu đỏ
 * đã quan sát được.
 *
 * KẾT QUẢ ĐO ĐƯỢC (31/08/2026, JDK 25, `Test worker`):
 *
 *  · chẩn đoán: collector chạy trên `DefaultDispatcher-worker-3`, **không** phải luồng
 *    test ⇒ giả thuyết đúng;
 *  · stress điều hướng **không** `setMain`: đỏ ở vòng 95 — `expected 2002-02, was 2002-03`;
 *  · stress collector bận rộn **không** `setMain`: **6 lần lệch / 2.000 vòng**, và giá
 *    trị nhận được đúng bằng tháng khởi tạo của ViewModel đó ⇒ luồng nền đã dựng state
 *    từ `view` cũ.
 *
 * Kết luận: race có thật nhưng **không thể xảy ra trong app**, vì ở đó mọi lần ghi đều
 * nằm trên luồng chính (callback Compose + `ON_RESUME`, và `viewModelScope` là
 * `Dispatchers.Main.immediate`). Lỗi nằm ở chỗ test cũ chạy ViewModel dưới một mô hình
 * luồng mà sản phẩm không bao giờ dùng.
 *
 * Vì vậy các test dưới đây chạy **dưới đúng mô hình luồng của production** và trở thành
 * lưới chắn hồi quy: nếu ai đó đẩy một collector sang `Dispatchers.IO`/`Default` hay
 * thêm `flowOn(...)`, chúng sẽ đỏ.
 */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CalendarViewModelStressTest {

    private val service = LunarCalendarService(LunarTestSupport.calendar)

    /** Cùng mô hình luồng với app thật: mọi thứ trên một luồng. */
    @org.junit.Before
    fun setUpDispatcher() {
        Dispatchers.setMain(kotlinx.coroutines.test.UnconfinedTestDispatcher())
    }

    @org.junit.After
    fun tearDownDispatcher() {
        Dispatchers.resetMain()
    }

    /** Trong dải dataset (1901–2100) và tránh hai mép để lật tháng không rơi ra ngoài. */
    private fun randomDate(rnd: Random): LocalDate {
        val year = rnd.nextInt(1950, 2090)
        val month = rnd.nextInt(1, 13)
        val day = rnd.nextInt(1, YearMonth.of(year, month).lengthOfMonth() + 1)
        return LocalDate.of(year, month, day)
    }

    private fun memorials(n: Int) = (1..n).map {
        Memorial(
            id = it.toLong(), familyId = 1, name = "Cụ $it",
            lunarDay = (it % 29) + 1, lunarMonth = (it % 12) + 1,
            rule = MemorialRule(), note = null,
        )
    }

    private fun members(n: Int) = (1..n).map {
        FamilyMember(
            id = it.toLong(), familyId = 1, fullName = "Người $it",
            gender = Gender.UNSPECIFIED, solarBirthDate = null, lunarBirthDate = null,
            role = null, note = null,
        )
    }

    /**
     * Điều kiện y hệt `CalendarViewModelTest`: không `setMain`, luồng mặc định.
     *
     * Sai thì: lưới và thẻ chi tiết nói hai chuyện khác nhau — đúng lời hứa mà test gốc
     * bảo vệ.
     */
    @Test
    fun `stress 2000 lan dieu huong khong bao gio lam lech thang va ngay dang chon`() {
        val rnd = Random(20260831)
        var iterations = 0
        repeat(2_000) {
            val today = randomDate(rnd)
            val vm = CalendarViewModel(service, today)

            // Một chuỗi thao tác ngẫu nhiên, rồi tự tính ra kỳ vọng bằng cùng quy tắc
            // kẹp ngày mà `moveMonth` dùng — không đọc kỳ vọng từ chính state đang kiểm.
            var expectedMonth = YearMonth.from(today)
            var expectedSelected = today
            repeat(rnd.nextInt(1, 6)) {
                when (rnd.nextInt(3)) {
                    0 -> {
                        val target = randomDate(rnd)
                        vm.select(target)
                        expectedMonth = YearMonth.from(target)
                        expectedSelected = target
                    }
                    1 -> {
                        vm.showNextMonth()
                        expectedMonth = expectedMonth.plusMonths(1)
                        expectedSelected = expectedMonth.atDay(
                            minOf(expectedSelected.dayOfMonth, expectedMonth.lengthOfMonth()),
                        )
                    }
                    else -> {
                        vm.showPreviousMonth()
                        expectedMonth = expectedMonth.minusMonths(1)
                        expectedSelected = expectedMonth.atDay(
                            minOf(expectedSelected.dayOfMonth, expectedMonth.lengthOfMonth()),
                        )
                    }
                }
            }

            val s = vm.state.value
            assertEquals("lần $iterations: tháng lệch", expectedMonth, s.month)
            assertEquals("lần $iterations: ngày chọn lệch", expectedSelected, s.selected)
            assertEquals("lần $iterations: thẻ chi tiết lệch", expectedSelected, s.selected)
            assertEquals(
                "lần $iterations: ngày âm của thẻ không khớp ngày đang chọn",
                service.dayOf(expectedSelected), s.selectedLunar,
            )
            val days = s.cells.filterIsInstance<CalendarCell.Day>()
            assertEquals("lần $iterations: số ô ngày sai", expectedMonth.lengthOfMonth(), days.size)
            assertEquals(
                "lần $iterations: đúng một ô được chọn",
                1, days.count { it.isSelected },
            )
            iterations++
        }
        println("stress điều hướng: $iterations lượt, không tái hiện được lỗi")
    }

    /**
     * Bản khuếch đại: cho collector **bận rộn** trong lúc luồng test điều hướng.
     *
     * Test trên dùng `flowOf(emptyList())` — phát đúng một giá trị rồi xong, nên cửa sổ
     * va chạm rất hẹp. Ở đây luồng ngày giỗ phát liên tục, mỗi lần phát đều khiến
     * coroutine ghi `_state.value = build(view, …)`. Nếu coroutine đó chạy trên luồng
     * khác và đọc `view` cũ, đây là chỗ nó lộ ra.
     */
    @Test
    fun `stress 2000 lan voi collector ban ron`() {
        val rnd = Random(31082026)
        val emissions = AtomicInteger()
        fun busyMemorials(): Flow<List<Memorial>> = flow {
            repeat(20) { i ->
                emissions.incrementAndGet()
                emit(memorials(i % 5))
            }
        }
        fun busyMembers(): Flow<List<FamilyMember>> = flow {
            repeat(20) { i -> emit(members(i % 3)) }
        }

        var mismatches = 0
        repeat(2_000) { iteration ->
            val today = randomDate(rnd)
            val vm = CalendarViewModel(service, today, busyMemorials(), busyMembers())

            val target = randomDate(rnd)
            vm.select(target)
            // Nhường luồng để collector nền (nếu có) kịp chạy xen vào.
            Thread.yield()

            val s = vm.state.value
            if (s.month != YearMonth.from(target) || s.selected != target) {
                mismatches++
                println(
                    "LỆCH ở lần $iteration: kỳ vọng ${YearMonth.from(target)}/$target, " +
                        "nhận ${s.month}/${s.selected}",
                )
            }
        }
        println("collector bận rộn: ${emissions.get()} lượt phát, $mismatches lần lệch")
        assertEquals("state bị ghi đè bởi luồng khác", 0, mismatches)
    }

    /**
     * Chốt **bất biến luồng** mà tính đúng đắn của `CalendarViewModel` dựa vào:
     * collector trong `init` phải chạy trên **cùng luồng** với người gọi.
     *
     * Đây là test tất định, không phụ thuộc may rủi. Thêm `flowOn(Dispatchers.IO)` vào
     * luồng ngày giỗ, hay đổi `viewModelScope` sang dispatcher nền, là test này đỏ ngay —
     * trước khi race kịp trở thành một lần đỏ ngẫu nhiên không ai giải thích được.
     */
    @Test
    fun `collector trong init phai chay tren cung luong voi nguoi goi`() {
        // So bằng ID luồng, không phải tên: trình gỡ lỗi coroutine gắn thêm hậu tố
        // `@coroutine#N` vào tên, nên so tên sẽ đỏ dù vẫn đúng một luồng.
        val callerThreadId = Thread.currentThread().threadId()
        val seen = java.util.concurrent.atomic.AtomicLong(-1)
        val probe = flow {
            seen.set(Thread.currentThread().threadId())
            emit(emptyList<Memorial>())
        }
        CalendarViewModel(service, LocalDate.of(2026, 8, 26), probe, flowOf(emptyList()))
        assertEquals(
            "collector phải ở cùng luồng với người gọi, nếu không bốn biến `var` trong " +
                "CalendarViewModel mất an toàn",
            callerThreadId,
            seen.get(),
        )
    }

    /**
     * Chẩn đoán: coroutine trong `init` có thực sự chạy khi **không** có `Dispatchers.Main`
     * hay không, và chạy trên luồng nào.
     *
     * Đây là dữ kiện quyết định giả thuyết đúng hay sai, nên đo chứ không đoán. Test
     * không khẳng định kết quả nào là "đúng" — nó ghi lại sự thật để báo cáo.
     */
    @Test
    fun `chan doan luong cua coroutine trong init`() {
        // Bỏ Main để đo lại đúng điều kiện đã sinh ra lần đỏ cũ.
        Dispatchers.resetMain()
        val testThread = Thread.currentThread().name
        // `AtomicReference` chứ không phải `@Volatile var` cục bộ: biến cục bộ không
        // nhận được annotation đó, và ở đây ta cần một rào cản bộ nhớ thật để đọc được
        // giá trị luồng nền ghi.
        val collectorThread = java.util.concurrent.atomic.AtomicReference<String?>(null)
        val probe = flow {
            collectorThread.set(Thread.currentThread().name)
            emit(emptyList<Memorial>())
        }
        CalendarViewModel(service, LocalDate.of(2026, 8, 26), probe, flowOf(emptyList()))
        // Cho luồng nền một khoảng rộng rãi để chạy, nếu nó có chạy.
        val deadline = System.nanoTime() + 2_000_000_000L
        while (collectorThread.get() == null && System.nanoTime() < deadline) Thread.yield()

        println("luồng test        : $testThread")
        println("luồng collector   : ${collectorThread.get() ?: "KHÔNG CHẠY"}")
        println("Dispatchers.Main  : " + runCatching { Dispatchers.Main.toString() }.getOrElse { "lỗi: ${it::class.simpleName}" })
        assertTrue(
            "không có Dispatchers.Main thì collector phải rơi sang luồng nền — " +
                "đó chính là điều kiện đã sinh ra lần đỏ cũ",
            collectorThread.get() != null && collectorThread.get() != testThread,
        )
    }
}
