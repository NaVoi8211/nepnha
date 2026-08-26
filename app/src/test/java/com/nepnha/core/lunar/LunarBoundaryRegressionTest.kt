package com.nepnha.core.lunar

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Hồi quy cho blocker phát hiện ở audit cuối Phase 3.
 *
 * Bối cảnh: dataset đầu tiên lấy ΔT từ cột phân giải PHÚT của trang NASA và lượng tử
 * hoá bằng `round`. Hai lỗi đó cộng lại đẩy trung khí Thu phân 1938 qua ranh giới ngày
 * Việt Nam, khiến tháng nhuận năm 1938 bị đặt sai một tháng.
 * Xem docs/PHASE_3_DATASET_CORRECTION.md.
 *
 * Kỳ vọng dưới đây được suy ra từ pipeline thiên văn ĐÃ SỬA chạy độc lập
 * (tools/reference_model, ăn thẳng NASA + ERFA), KHÔNG chép từ dataset cũ.
 */
class LunarBoundaryRegressionTest {

    private val cal = LunarTestSupport.calendar

    private fun leapOf(year: Int): LeapMonthInfo =
        (cal.leapMonthOf(year) as LunarResult.Success).value

    /**
     * Sai thì: tháng nhuận 1938 quay lại tháng 7 — nghĩa là ΔT hoặc quy tắc lượng tử
     * hoá đã bị đổi ngược về hành vi lỗi cũ.
     */
    @Test
    fun `1938 nhuan thang 8 chu khong phai thang 7`() {
        assertEquals(LeapMonthInfo.Month(8), leapOf(1938))
        assertEquals(13, (cal.monthsInLunarYear(1938) as LunarResult.Success).value)
    }

    /**
     * Sai thì: ranh giới hai tháng quanh tháng nhuận 1938 bị xê dịch — bắt được cả
     * trường hợp số tháng đúng nhưng mốc ngày sai.
     */
    @Test
    fun `moc ngay quanh thang nhuan 1938`() {
        assertEquals(LunarDate(1, 8, 1938), LunarTestSupport.lunarOf(1938, 8, 25))
        assertEquals(LunarDate(30, 8, 1938), LunarTestSupport.lunarOf(1938, 9, 23))
        assertEquals(LunarDate(1, 8, 1938, isLeapMonth = true), LunarTestSupport.lunarOf(1938, 9, 24))
        assertEquals(LunarDate(29, 8, 1938, isLeapMonth = true), LunarTestSupport.lunarOf(1938, 10, 22))
        assertEquals(LunarDate(1, 9, 1938), LunarTestSupport.lunarOf(1938, 10, 23))
    }

    /**
     * Tám điểm Sóc nằm trong ±120 giây quanh 17:00:00Z — ranh giới ngày Việt Nam.
     * Đây là toàn bộ các ca sát biên trong 1901-2100, quét từ dataset chứ không phải
     * danh sách chép tay.
     *
     * Sai thì: một điểm Sóc bị lượng tử hoá đẩy sang ngày khác, làm mùng 1 lệch.
     */
    @Test
    fun `moi diem Soc sat ranh gioi ngay deu mo dung mot thang`() {
        val cases = listOf(
            Triple(LocalDate.of(1944, 6, 21), LunarDate(1, 5, 1944), LunarDate(30, 4, 1944, true)),
            Triple(LocalDate.of(1967, 7, 8), LunarDate(1, 6, 1967), LunarDate(30, 5, 1967)),
            Triple(LocalDate.of(1998, 9, 21), LunarDate(1, 8, 1998), LunarDate(30, 7, 1998)),
            Triple(LocalDate.of(2054, 5, 8), LunarDate(1, 4, 2054), LunarDate(30, 3, 2054)),
            Triple(LocalDate.of(2072, 12, 9), LunarDate(1, 11, 2072), LunarDate(29, 10, 2072)),
            Triple(LocalDate.of(2077, 11, 16), LunarDate(1, 10, 2077), LunarDate(30, 9, 2077)),
            Triple(LocalDate.of(2079, 8, 27), LunarDate(1, 8, 2079), LunarDate(30, 7, 2079)),
            Triple(LocalDate.of(2085, 10, 19), LunarDate(1, 9, 2085), LunarDate(30, 8, 2085)),
        )
        for ((g, first, prev) in cases) {
            assertEquals("mùng 1 sai tại $g", first, LunarTestSupport.lunarOf(g.year, g.monthValue, g.dayOfMonth))
            val p = g.minusDays(1)
            assertEquals("ngày cuối tháng trước sai tại $p", prev,
                LunarTestSupport.lunarOf(p.year, p.monthValue, p.dayOfMonth))
        }
    }

    /**
     * Bảy trung khí nằm trong ±120 giây quanh ranh giới. Hệ quả quan sát được của
     * chúng là việc xác định tháng nhuận, nên khoá thẳng kết quả đó.
     *
     * Sai thì: quy tắc lượng tử hoá quay lại `round`, hoặc ΔT bị đổi — cả hai đều làm
     * ít nhất một năm trong danh sách đổi tháng nhuận.
     */
    @Test
    fun `nam chua trung khi sat ranh gioi co thang nhuan dung`() {
        assertEquals(LeapMonthInfo.None, leapOf(1924))
        assertEquals(LeapMonthInfo.Month(8), leapOf(1938))
        assertEquals(LeapMonthInfo.None, leapOf(1953))
        assertEquals(LeapMonthInfo.Month(2), leapOf(2004))
        assertEquals(LeapMonthInfo.None, leapOf(2038))
        assertEquals(LeapMonthInfo.Month(6), leapOf(2074))
    }

    /**
     * Ba trung khí mà `floor` và `round` cho ra phút khác nhau. Với hai trong số đó,
     * `round` còn đẩy sang hẳn ngày Việt Nam khác.
     *
     * Sai thì: ngày dương lịch bị dịch một ngày quanh các mốc này.
     */
    @Test
    fun `floor giu nguyen ngay Viet Nam tai ba moc nhay cam`() {
        assertEquals(LunarDate(20, 5, 1924), LunarTestSupport.lunarOf(1924, 6, 21))
        assertEquals(LunarDate(21, 6, 2038), LunarTestSupport.lunarOf(2038, 7, 22))
        assertEquals(LunarDate(30, 8, 1938), LunarTestSupport.lunarOf(1938, 9, 23))
    }

    /**
     * Bất biến chung: không tháng âm nào trong toàn dải dài hơn 30 hoặc ngắn hơn 29
     * ngày. Một lỗi lượng tử hoá đẩy mùng 1 lệch sẽ tạo ra tháng 28 hoặc 31 ngày.
     *
     * Sai thì: có mốc Sóc bị lệch ngày.
     */
    @Test
    fun `khong thang am nao dai 28 hoac 31 ngay`() {
        // Bỏ qua tháng đầu và tháng cuối vì chúng bị dải 1901-2100 cắt cụt.
        val lengths = mutableListOf<Int>()
        var d = LocalDate.of(1901, 1, 1)
        val end = LocalDate.of(2100, 12, 31)
        var prev = LunarTestSupport.lunarOf(1901, 1, 1)
        var run = 0
        while (d <= end) {
            val l = LunarTestSupport.lunarOf(d.year, d.monthValue, d.dayOfMonth)
            if (l.month != prev.month || l.isLeapMonth != prev.isLeapMonth || l.year != prev.year) {
                lengths.add(run)
                run = 0
            }
            run++
            prev = l
            d = d.plusDays(1)
        }
        val whole = lengths.drop(1)          // tháng đầu bị cắt cụt
        assertTrue("không có tháng nào trọn vẹn để kiểm", whole.size > 2400)
        val bad = whole.filter { it != 29 && it != 30 }
        assertTrue("tháng âm có độ dài lạ: $bad", bad.isEmpty())
    }
}
