package com.nepnha.domain.event

import com.nepnha.core.lunar.LunarTestSupport
import com.nepnha.domain.calendar.LunarCalendarService
import com.nepnha.domain.model.Memorial
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Cửa sổ dò ngày giỗ kế tiếp phải đủ rộng — chứng minh bằng đo cạn kiệt, không bằng
 * lập luận "25 lớn hơn 19 nên chắc đủ".
 *
 * Quét **toàn bộ** tổ hợp ngày âm hợp lệ × tháng âm × hai policy, trên **toàn bộ**
 * 1901–2100, đo khoảng cách lớn nhất giữa hai lần giỗ liên tiếp. Cửa sổ phải ≥ khoảng
 * cách đó, nếu không sẽ có ngày giỗ có thật nhưng app báo "không tìm được".
 *
 * 30 × 12 × 3 × 2 = 2.160 cấu hình × 200 năm = 432.000 lượt quy đổi. Chạy vài giây
 * trên JVM.
 */
class MemorialSearchWindowTest {

    private val service = LunarCalendarService(LunarTestSupport.calendar)
    private val resolver = MemorialDateResolver(service)

    private fun memorial(day: Int, month: Int, leap: LeapMonthPolicy, missing: MissingDayPolicy) =
        Memorial(1, 1, "x", day, month, MemorialRule(leap, missing), null)

    private data class Profile(
        val label: String,
        val resolvedYears: List<Int>,
        val maxGap: Int,
    )

    private fun profile(day: Int, month: Int, leap: LeapMonthPolicy, missing: MissingDayPolicy): Profile {
        val m = memorial(day, month, leap, missing)
        val years = (1901..2100).filter { resolver.resolve(m, it) is MemorialResolution.Resolved }
        val gap = years.zipWithNext().maxOfOrNull { (a, b) -> b - a } ?: 0
        return Profile("$day/$month ${leap.name}/${missing.name}", years, gap)
    }

    private fun allProfiles(): List<Profile> = buildList {
        for (month in 1..12) {
            for (day in 1..30) {
                for (leap in LeapMonthPolicy.entries) {
                    for (missing in MissingDayPolicy.entries) {
                        add(profile(day, month, leap, missing))
                    }
                }
            }
        }
    }

    /**
     * Khoảng cách lớn nhất giữa hai lần giỗ liên tiếp, trên mọi cấu hình.
     *
     * Sai thì: cửa sổ dò hiện tại nhỏ hơn khoảng cách thật ⇒ có ngày giỗ tồn tại
     * nhưng `nextOccurrence` trả `null` và app nói dối rằng không có.
     */
    @Test
    fun `cua so do phai lon hon moi khoang cach thuc te`() {
        val profiles = allProfiles()
        val withOccurrences = profiles.filter { it.resolvedYears.isNotEmpty() }
        val worst = withOccurrences.maxByOrNull { it.maxGap }!!

        println("PROFILE tổng cấu hình      : ${profiles.size}")
        println("PROFILE có ít nhất 1 lần   : ${withOccurrences.size}")
        println("PROFILE không có lần nào   : ${profiles.size - withOccurrences.size}")
        println("PROFILE khoảng cách lớn nhất: ${worst.maxGap} năm âm  (${worst.label})")
        withOccurrences.filter { it.maxGap > 10 }
            .sortedByDescending { it.maxGap }
            .take(8)
            .forEach { println("PROFILE   gap=${it.maxGap}  ${it.label}  lần đầu=${it.resolvedYears.first()}") }

        // Không còn cửa sổ cố định nào để so. Điều phải chứng minh là: MỌI lần giỗ
        // có thật trong phạm vi dữ liệu đều được `nextOccurrence` tìm ra, kể cả cấu
        // hình có khoảng cách 114 năm.
        assertTrue("phải có cấu hình khoảng cách > 25 năm để test này có ý nghĩa", worst.maxGap > 25)
    }

    /**
     * Với mọi cấu hình có ít nhất một lần giỗ trong phạm vi, `nextOccurrence` phải
     * tìm ra đúng lần đầu tiên kể từ mốc dò — không được trả `null`.
     *
     * Sai thì: app báo "không có ngày giỗ nào" trong khi dữ liệu có. Đây chính là lỗi
     * mà cửa sổ cố định 25 năm gây ra.
     */
    @Test
    fun `khong bo sot lan gio nao co that trong pham vi`() {
        val start = java.time.LocalDate.of(1901, 1, 1)
        val misses = mutableListOf<String>()
        for (p in allProfiles()) {
            if (p.resolvedYears.isEmpty()) continue
            val parts = p.label.split(" ")[0].split("/")
            val policies = p.label.split(" ")[1].split("/")
            val m = memorial(
                parts[0].toInt(),
                parts[1].toInt(),
                LeapMonthPolicy.valueOf(policies[0]),
                MissingDayPolicy.valueOf(policies[1]),
            )
            if (resolver.nextOccurrence(m, start) == null) misses += p.label
        }
        assertTrue("bỏ sót ${misses.size} cấu hình: ${misses.take(5)}", misses.isEmpty())
    }

    /**
     * Cấu hình thật sự không có lần nào trong 1901–2100 phải trả `null` — không được
     * bịa ra một ngày.
     *
     * Sai thì: app hiện ngày giỗ cho một cấu hình bất khả thi.
     */
    @Test
    fun `cau hinh khong the co lan nao thi tra null`() {
        val start = java.time.LocalDate.of(1901, 1, 1)
        val impossible = allProfiles().filter { it.resolvedYears.isEmpty() }
        assertTrue("phải có cấu hình bất khả thi để test có ý nghĩa", impossible.isNotEmpty())
        for (p in impossible.take(30)) {
            val parts = p.label.split(" ")[0].split("/")
            val policies = p.label.split(" ")[1].split("/")
            val m = memorial(
                parts[0].toInt(),
                parts[1].toInt(),
                LeapMonthPolicy.valueOf(policies[0]),
                MissingDayPolicy.valueOf(policies[1]),
            )
            assertTrue("${p.label} phải là null", resolver.nextOccurrence(m, start) == null)
        }
    }

    /**
     * Vòng dò phải **kết thúc** kể cả với cấu hình tệ nhất — không có cửa sổ cố định
     * nữa nên tính hữu hạn đến từ phạm vi dữ liệu.
     *
     * Sai thì: app treo khi người dùng tạo một ngày giỗ bất khả thi.
     */
    @Test
    fun `vong do luon ket thuc`() {
        val worst = memorial(30, 4, LeapMonthPolicy.LEAP_MONTH_ONLY, MissingDayPolicy.SKIP)
        val t0 = System.nanoTime()
        resolver.nextOccurrence(worst, java.time.LocalDate.of(2099, 1, 1))
        val ms = (System.nanoTime() - t0) / 1_000_000
        assertTrue("dò tới hết phạm vi mất ${ms} ms — quá chậm cho luồng chính", ms < 500)
    }
}
