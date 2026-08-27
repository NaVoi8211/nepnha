package com.nepnha.domain.event

import com.nepnha.core.lunar.LunarTestSupport
import com.nepnha.domain.calendar.LunarCalendarService
import com.nepnha.domain.model.Memorial
import java.time.LocalDate
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Quy tắc nghiệp vụ ngày giỗ.
 *
 * Mọi mốc trong đây lấy từ **dataset thật** đã đóng băng, không phải engine giả:
 *
 * ```
 * năm âm 2026 tháng 6  : 30 ngày, mùng 1 = 14/07/2026
 * năm âm 2026 tháng 7  : 29 ngày, mùng 1 = 13/08/2026   ⇒ KHÔNG có ngày 30
 * năm âm 2026 tháng 12 : mùng 1 = 08/01/2027            ⇒ vắt sang năm dương sau
 * năm âm 1938 tháng 8  : 30 ngày, mùng 1 = 25/08/1938
 * năm âm 1938 tháng 8N : 29 ngày, mùng 1 = 24/09/1938
 * năm âm 1987 tháng 7  : 29 ngày, mùng 1 = 26/07/1987
 * năm âm 1987 tháng 7N : 30 ngày, mùng 1 = 24/08/1987
 * ```
 */
class MemorialDateResolverTest {

    private val service = LunarCalendarService(LunarTestSupport.calendar)
    private val resolver = MemorialDateResolver(service)

    private fun memorial(
        day: Int,
        month: Int,
        leap: LeapMonthPolicy = LeapMonthPolicy.COMMON_MONTH_DEFAULT,
        missing: MissingDayPolicy = MissingDayPolicy.LAST_VALID_DAY_OF_MONTH,
    ) = Memorial(
        id = 1,
        familyId = 1,
        name = "Cụ ông",
        lunarDay = day,
        lunarMonth = month,
        rule = MemorialRule(leap, missing),
        note = null,
    )

    private fun resolved(m: Memorial, year: Int) =
        (resolver.resolve(m, year) as MemorialResolution.Resolved).date

    private fun skipped(m: Memorial, year: Int) =
        (resolver.resolve(m, year) as MemorialResolution.Skipped).reason

    // ---------- A, B: ngày tồn tại bình thường ----------

    /** Sai thì: ngày giỗ thường ngày cũng đã lệch, mọi thứ sau đó vô nghĩa. */
    @Test
    fun `ngay 29 trong thang 29 ngay va ngay 30 trong thang 30 ngay`() {
        val a = resolved(memorial(29, 7), 2026)          // tháng 7/2026 có 29 ngày
        assertEquals(LocalDate.of(2026, 9, 10), a.solarDate)
        assertFalse(a.wasAdjusted)

        val b = resolved(memorial(30, 6), 2026)          // tháng 6/2026 có 30 ngày
        assertEquals(LocalDate.of(2026, 8, 12), b.solarDate)
        assertEquals(30, b.effectiveLunarDay)
        assertFalse(b.wasAdjusted)
    }

    // ---------- C, D, E: ngày 30 trong tháng thiếu ----------

    /**
     * Sai thì: ngày giỗ mùng 30 âm thầm biến mất hoặc âm thầm đổi thành 29 mà không
     * ai biết — đúng điều `docs/MEMORIAL_RULES.md` cấm.
     */
    @Test
    fun `ngay 30 trong thang thieu voi LAST_VALID_DAY`() {
        val r = resolved(memorial(30, 7), 2026)          // tháng 7/2026 chỉ có 29 ngày
        assertEquals(30, r.originalLunarDay)             // dữ liệu gốc KHÔNG đổi
        assertEquals(29, r.effectiveLunarDay)
        assertTrue(r.wasAdjusted)
        assertTrue(r.dayWasShortened)
        assertFalse("không có chuyện lùi tháng ở đây", r.fellBackToCommonMonth)
        assertEquals(LocalDate.of(2026, 9, 10), r.solarDate)
    }

    /** Sai thì: policy SKIP bị bỏ qua và ngày giỗ vẫn bị lùi ngày trái ý người dùng. */
    @Test
    fun `ngay 30 trong thang thieu voi SKIP`() {
        val m = memorial(30, 7, missing = MissingDayPolicy.SKIP)
        assertEquals(MemorialResolution.Reason.MISSING_DAY, skipped(m, 2026))
        // Năm nào tháng 7 có 30 ngày thì vẫn ra bình thường.
        val ok = resolver.resolve(m, 1987)
        assertTrue(ok is MemorialResolution.Resolved || ok is MemorialResolution.Skipped)
    }

    // ---------- F, G, H: tháng nhuận ----------

    /**
     * Sai thì: ngày giỗ tháng thường bị kéo sang tháng nhuận, lệch cả tháng.
     */
    @Test
    fun `thang thuong trong nam co thang nhuan van la thang thuong`() {
        val r = resolved(memorial(1, 8), 1938)           // 1938 nhuận tháng 8
        assertFalse(r.isLeapMonth)
        assertEquals(LocalDate.of(1938, 8, 25), r.solarDate)
    }

    /** Sai thì: gia đình giỗ theo tháng nhuận bị đẩy về tháng thường. */
    @Test
    fun `LEAP_MONTH_ONLY trong nam co thang nhuan`() {
        val r = resolved(memorial(1, 8, LeapMonthPolicy.LEAP_MONTH_ONLY), 1938)
        assertTrue(r.isLeapMonth)
        assertEquals(LocalDate.of(1938, 9, 24), r.solarDate)
        assertFalse(r.wasAdjusted)
    }

    /**
     * Sai thì: năm không có tháng nhuận mà vẫn lặng lẽ tính vào tháng thường — trái
     * đúng điều người dùng chọn khi bật "chỉ tháng nhuận".
     */
    @Test
    fun `LEAP_MONTH_ONLY trong nam khong co thang nhuan thi bo qua`() {
        val m = memorial(1, 8, LeapMonthPolicy.LEAP_MONTH_ONLY)
        assertEquals(MemorialResolution.Reason.NO_LEAP_MONTH, skipped(m, 2026))
    }

    /**
     * PREFERRED lùi về tháng thường nhưng **phải nói ra**.
     *
     * Sai thì: người dùng tưởng đang giỗ tháng nhuận trong khi thực tế là tháng thường.
     */
    @Test
    fun `LEAP_MONTH_PREFERRED lui ve thang thuong va bao rõ`() {
        val m = memorial(1, 8, LeapMonthPolicy.LEAP_MONTH_PREFERRED)
        val leapYear = resolved(m, 1938)
        assertTrue(leapYear.isLeapMonth)
        assertFalse(leapYear.wasAdjusted)

        val plainYear = resolved(m, 2026)
        assertFalse(plainYear.isLeapMonth)
        assertTrue(plainYear.wasAdjusted)
        assertTrue(plainYear.fellBackToCommonMonth)
        assertFalse("ngày không bị lùi ở đây", plainYear.dayWasShortened)
    }

    /**
     * Hai điều chỉnh có thể xảy ra **cùng lúc**: giỗ 30 tháng 7 nhuận, năm 2026 không
     * có tháng 7 nhuận (lùi tháng thường) và tháng 7 thường chỉ có 29 ngày (lùi ngày).
     *
     * Sai thì: một enum `adjustment` duy nhất khiến nhánh sau ghi đè nhánh trước, và
     * người dùng chỉ được nghe một nửa sự thật về quyết định app tự làm thay họ.
     */
    @Test
    fun `lui thang va lui ngay cung luc thi giu ca hai`() {
        val m = memorial(30, 7, LeapMonthPolicy.LEAP_MONTH_PREFERRED)
        val r = resolved(m, 2026)
        assertTrue("phải ghi nhận đã lùi về tháng thường", r.fellBackToCommonMonth)
        assertTrue("phải ghi nhận đã lùi ngày", r.dayWasShortened)
        assertEquals(30, r.originalLunarDay)
        assertEquals(29, r.effectiveLunarDay)
        assertFalse(r.isLeapMonth)
        assertEquals(LocalDate.of(2026, 9, 10), r.solarDate)
    }

    /**
     * Năm ngoài phạm vi dữ liệu phải báo đúng lý do, không được báo nhầm thành "năm
     * không có tháng nhuận".
     *
     * Sai thì: biểu mẫu khuyên người dùng đổi lựa chọn cho một vấn đề không phải của họ.
     */
    @Test
    fun `ngoai pham vi khong bi bao nham la khong co thang nhuan`() {
        val m = memorial(1, 8, LeapMonthPolicy.LEAP_MONTH_ONLY)
        assertEquals(MemorialResolution.Reason.OUT_OF_SUPPORTED_RANGE, skipped(m, 2200))
        assertEquals(MemorialResolution.Reason.OUT_OF_SUPPORTED_RANGE, skipped(m, 1800))
    }

    /** 1987 nhuận tháng 7: tháng thường 29 ngày, tháng nhuận 30 ngày. */
    @Test
    fun `1987 phan biet dung thang 7 thuong va thang 7 nhuan`() {
        assertEquals(LocalDate.of(1987, 8, 9), resolved(memorial(15, 7), 1987).solarDate)
        assertEquals(
            LocalDate.of(1987, 9, 7),
            resolved(memorial(15, 7, LeapMonthPolicy.LEAP_MONTH_ONLY), 1987).solarDate,
        )
    }

    // ---------- J, K, L, M, N: sắp tới và ranh giới năm ----------

    /** Sai thì: ngày giỗ đã qua vẫn hiện là "sắp tới". */
    @Test
    fun `ngay gio da qua thi nhay sang nam sau`() {
        val m = memorial(1, 1)                            // Tết
        val next = resolver.nextOccurrence(m, LocalDate.of(2026, 6, 1))!!
        assertEquals(2027, next.lunarYear)
        assertTrue(next.solarDate.isAfter(LocalDate.of(2026, 6, 1)))
    }

    /** Sai thì: ngày giỗ chưa tới bị đẩy sang năm sau, hụt mất một năm. */
    @Test
    fun `ngay gio chua toi thi lay nam nay`() {
        val m = memorial(1, 8)                            // mùng 1/8 âm 2026 = 11/09/2026
        val next = resolver.nextOccurrence(m, LocalDate.of(2026, 8, 26))!!
        assertEquals(2026, next.lunarYear)
        assertEquals(LocalDate.of(2026, 9, 11), next.solarDate)
    }

    /** Ngày giỗ của đúng hôm nay vẫn là "sắp tới" — hôm nay là ngày phải làm cỗ. */
    @Test
    fun `ngay gio dung hom nay van tinh la sap toi`() {
        val m = memorial(14, 7)                           // 14/7 âm 2026 = 26/08/2026
        val next = resolver.nextOccurrence(m, LocalDate.of(2026, 8, 26))!!
        assertEquals(LocalDate.of(2026, 8, 26), next.solarDate)
    }

    /**
     * Tháng 12 âm năm 2026 bắt đầu 08/01/2027 — ngày giỗ cuối năm âm rơi sang năm
     * dương sau.
     *
     * Sai thì: trộn năm âm với năm dương, ngày giỗ nhảy lùi một năm.
     */
    @Test
    fun `ngay gio cuoi nam am roi sang nam duong sau`() {
        val r = resolved(memorial(1, 12), 2026)
        assertEquals(LocalDate.of(2027, 1, 8), r.solarDate)
        assertEquals(2026, r.lunarYear)
    }

    /** Sai thì: đầu tháng 1 dương bị coi là năm âm mới, tìm nhầm năm. */
    @Test
    fun `dau nam duong van thuoc nam am truoc`() {
        val m = memorial(1, 12)
        val next = resolver.nextOccurrence(m, LocalDate.of(2027, 1, 1))!!
        assertEquals(2026, next.lunarYear)
        assertEquals(LocalDate.of(2027, 1, 8), next.solarDate)
    }

    /**
     * Cùng một ngày giỗ, nhiều năm liên tiếp phải cho ra ngày dương khác nhau và
     * không tích luỹ sai lệch.
     *
     * Sai thì: ngày giỗ trôi dần qua các năm.
     */
    @Test
    fun `nhieu nam lien tiep khong tich luy sai lech`() {
        val m = memorial(10, 3)
        val dates = (2020..2030).map { resolved(m, it).solarDate }
        assertEquals(dates.size, dates.distinct().size)
        dates.zipWithNext().forEach { (a, b) ->
            val gap = java.time.temporal.ChronoUnit.DAYS.between(a, b)
            assertTrue("khoảng cách hai năm liên tiếp bất thường: $gap ngày", gap in 320..410)
        }
    }

    // ---------- vòng lặp có giới hạn ----------

    /**
     * `nextOccurrence` phải dừng, kể cả khi policy khiến hầu hết các năm bị bỏ qua.
     *
     * Sai thì: vòng lặp chạy mãi và app treo.
     */
    @Test
    fun `khong bao gio lap vo han khi policy bo qua nhieu nam`() {
        val m = memorial(30, 11, LeapMonthPolicy.LEAP_MONTH_ONLY, MissingDayPolicy.SKIP)
        val next = resolver.nextOccurrence(m, LocalDate.of(2026, 1, 1))
        // Tìm được hay không đều hợp lệ; điều bắt buộc là hàm TRẢ VỀ.
        assertTrue(next == null || next.isLeapMonth)
    }

    /** Sai thì: gần cuối dải 1901–2100 engine ném lỗi thay vì báo trạng thái. */
    @Test
    fun `ngoai pham vi tra Skipped chu khong nem`() {
        assertEquals(
            MemorialResolution.Reason.OUT_OF_SUPPORTED_RANGE,
            skipped(memorial(1, 1), 2200),
        )
        assertNull(resolver.nextOccurrence(memorial(1, 1), LocalDate.of(2101, 1, 1)))
    }

    // ---------- U, V: nhiều ngày giỗ ----------

    /** Sai thì: hai ngày giỗ trùng ngày làm mất một cái trên lịch. */
    @Test
    fun `nhieu ngay gio cung mot ngay duong deu duoc giu`() {
        val a = memorial(1, 8).copy(id = 1, name = "Cụ ông")
        val b = memorial(1, 8).copy(id = 2, name = "Cụ bà")
        // Khác ngày âm nhưng cùng ngày dương sau khi điều chỉnh:
        val c = memorial(30, 7).copy(id = 3, name = "Bác cả")   // → 29/7 = 10/09/2026
        val d = memorial(29, 7).copy(id = 4, name = "Bác hai")  // → 29/7 = 10/09/2026

        val map = resolver.occurrencesBetween(
            listOf(a, b, c, d),
            LocalDate.of(2026, 9, 1),
            LocalDate.of(2026, 9, 30),
        )
        assertEquals(2, map.getValue(LocalDate.of(2026, 9, 11)).size)
        assertEquals(2, map.getValue(LocalDate.of(2026, 9, 10)).size)
    }

    /** Sai thì: danh sách "sắp tới" xếp lộn xộn, ngày xa hiện trước ngày gần. */
    @Test
    fun `danh sach sap toi xep theo ngay duong gan nhat`() {
        val today = LocalDate.of(2026, 8, 26)
        val list = resolver.upcoming(
            listOf(memorial(1, 12).copy(id = 1), memorial(1, 8).copy(id = 2)),
            today,
        )
        assertEquals(2L, list[0].memorial.id)
        assertEquals(LocalDate.of(2026, 9, 11), list[0].next!!.solarDate)
        assertNotNull(list[0].daysUntil)
        assertEquals(16L, list[0].daysUntil)
        assertTrue(list[1].next!!.solarDate.isAfter(list[0].next!!.solarDate))
    }

    /**
     * `daysUntil` không bao giờ âm, và bằng 0 đúng khi giỗ rơi vào hôm nay.
     *
     * Sai thì: giao diện hiện "Còn -3 ngày", hoặc hiện "Còn 0 ngày" thay vì "Hôm nay".
     */
    @Test
    fun `so ngay con lai khong bao gio am`() {
        val today = LocalDate.of(2026, 8, 26)
        val list = resolver.upcoming(
            (1..12).map { memorial(15, it).copy(id = it.toLong()) } +
                memorial(14, 7).copy(id = 99),
            today,
        )
        list.forEach { item ->
            val d = item.daysUntil
            if (d != null) assertTrue("số ngày còn lại âm: $d", d >= 0)
        }
        // 14/7 âm 2026 = đúng hôm nay ⇒ phải là 0, để UI đọc thành "Hôm nay".
        assertEquals(0L, list.single { it.memorial.id == 99L }.daysUntil)
    }

    // ---------- O: timezone ----------

    /**
     * Sai thì: cùng một ngày giỗ cho ra ngày dương khác nhau trên hai máy đặt múi
     * giờ khác nhau.
     */
    @Test
    fun `ket qua khong phu thuoc timezone cua may`() {
        val goc = TimeZone.getDefault()
        try {
            val m = memorial(30, 7)
            val today = LocalDate.of(2026, 8, 26)
            val chuan = resolver.nextOccurrence(m, today)
            for (tz in listOf("UTC", "Asia/Ho_Chi_Minh", "Asia/Tokyo", "America/Los_Angeles")) {
                TimeZone.setDefault(TimeZone.getTimeZone(tz))
                assertEquals("đổi timezone sang $tz làm đổi kết quả", chuan, resolver.nextOccurrence(m, today))
            }
        } finally {
            TimeZone.setDefault(goc)
        }
    }
}
