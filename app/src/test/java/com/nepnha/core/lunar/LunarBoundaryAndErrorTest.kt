package com.nepnha.core.lunar

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Biên phạm vi, ngày không tồn tại, và mô hình lỗi. */
class LunarBoundaryAndErrorTest {

    private val cal = LunarTestSupport.calendar

    // ----- Biên -----

    @Test
    fun `hai dau pham vi tinh duoc`() {
        assertTrue(cal.toLunar(LocalDate.of(1901, 1, 1)) is LunarResult.Success)
        assertTrue(cal.toLunar(LocalDate.of(2100, 12, 31)) is LunarResult.Success)
    }

    @Test
    fun `nhan nam am duoc phep vuot ngoai 1901-2100`() {
        // 01/01/1901 thuộc năm âm 1900. Cấm điều này sẽ khiến 20 ngày đầu năm 1901 —
        // vốn NẰM TRONG phạm vi công bố — không biểu diễn được. Xem readiness Q4.
        assertEquals(1900, LunarTestSupport.lunarOf(1901, 1, 1).year)
    }

    @Test
    fun `ngoai pham vi tra UnsupportedYear chu khong ngoai suy`() {
        for (d in listOf(LocalDate.of(1900, 12, 31), LocalDate.of(2101, 1, 1))) {
            val r = cal.toLunar(d)
            assertTrue("$d phải lỗi", r is LunarResult.Failure)
            assertTrue((r as LunarResult.Failure).error is LunarError.UnsupportedYear)
        }
    }

    // ----- Ngày âm không tồn tại -----

    @Test
    fun `mung 30 cua thang thieu tra NonexistentLunarDate kem lastValidDay`() {
        // Tìm một tháng thật sự chỉ có 29 ngày rồi hỏi mùng 30.
        var checked = 0
        outer@ for (y in 1950..2050) {
            for (m in 1..12) {
                val len = cal.daysInLunarMonth(y, m, false)
                if (len !is LunarResult.Success || len.value != 29) continue
                val r = cal.toSolar(LunarDate(30, m, y, false))
                assertTrue("tháng $m/$y chỉ 29 ngày, mùng 30 phải lỗi", r is LunarResult.Failure)
                val e = (r as LunarResult.Failure).error
                assertTrue(e is LunarError.NonexistentLunarDate)
                assertEquals(29, (e as LunarError.NonexistentLunarDate).lastValidDay)
                checked++
                if (checked >= 5) break@outer
            }
        }
        assertEquals("phải kiểm được 5 tháng thiếu", 5, checked)
    }

    @Test
    fun `mung 30 cua thang du van hop le`() {
        var checked = 0
        outer@ for (y in 1950..2050) {
            for (m in 1..12) {
                val len = cal.daysInLunarMonth(y, m, false)
                if (len !is LunarResult.Success || len.value != 30) continue
                assertTrue(cal.toSolar(LunarDate(30, m, y, false)) is LunarResult.Success)
                checked++
                if (checked >= 5) break@outer
            }
        }
        assertEquals(5, checked)
    }

    @Test
    fun `engine KHONG tu lui 30 ve 29`() {
        // Việc lùi ngày là quy tắc NGÀY GIỖ ở tầng khác (MemorialRule), không phải
        // việc của engine lịch. Engine chỉ báo ngày không tồn tại.
        var found = false
        outer@ for (y in 1950..2050) {
            for (m in 1..12) {
                val len = cal.daysInLunarMonth(y, m, false)
                if (len !is LunarResult.Success || len.value != 29) continue
                val r = cal.toSolar(LunarDate(30, m, y, false))
                assertTrue(r is LunarResult.Failure)   // KHÔNG phải Success với ngày 29
                found = true
                break@outer
            }
        }
        assertTrue(found)
    }

    // ----- Tháng nhuận -----

    @Test
    fun `hoi thang nhuan khong ton tai tra NoSuchLeapMonth`() {
        val y = 2026
        assertEquals(LunarResult.Success(LeapMonthInfo.None), cal.leapMonthOf(y))
        val r = cal.toSolar(LunarDate(1, 5, y, isLeapMonth = true))
        assertTrue(r is LunarResult.Failure)
        assertTrue((r as LunarResult.Failure).error is LunarError.NoSuchLeapMonth)
    }

    @Test
    fun `thang nhuan mang so cua thang lien truoc`() {
        // 2025 nhuận tháng 6: phải có HAI tháng mang số 6, tháng sau đó là tháng 7.
        val info = (cal.leapMonthOf(2025) as LunarResult.Success).value
        assertEquals(LeapMonthInfo.Month(6), info)
        val thuong = (cal.toSolar(LunarDate(1, 6, 2025, false)) as LunarResult.Success).value
        val nhuan = (cal.toSolar(LunarDate(1, 6, 2025, true)) as LunarResult.Success).value
        val sau = (cal.toSolar(LunarDate(1, 7, 2025, false)) as LunarResult.Success).value
        assertTrue("nhuận phải sau thường", nhuan.isAfter(thuong))
        assertTrue("tháng 7 phải sau tháng nhuận", sau.isAfter(nhuan))
        val cach = nhuan.toEpochDay() - thuong.toEpochDay()
        assertTrue("hai mùng 1 cách nhau 29-30 ngày, thực tế $cach", cach in 29..30)
    }

    // ----- Giá trị đầu vào sai -----

    @Test
    fun `gia tri ngay am sai khoang bi tu choi`() {
        val cases = listOf(
            LunarDate(0, 1, 2026) to LunarError.InvalidLunarDate.Reason.DAY_OUT_OF_RANGE,
            LunarDate(31, 1, 2026) to LunarError.InvalidLunarDate.Reason.DAY_OUT_OF_RANGE,
            LunarDate(1, 0, 2026) to LunarError.InvalidLunarDate.Reason.MONTH_OUT_OF_RANGE,
            LunarDate(1, 13, 2026) to LunarError.InvalidLunarDate.Reason.MONTH_OUT_OF_RANGE,
        )
        for ((d, reason) in cases) {
            val r = cal.toSolar(d)
            assertTrue("$d phải lỗi", r is LunarResult.Failure)
            val e = (r as LunarResult.Failure).error
            assertTrue("$d -> $e", e is LunarError.InvalidLunarDate)
            assertEquals(reason, (e as LunarError.InvalidLunarDate).reason)
        }
    }

    @Test
    fun `ngay duong khong ton tai tra InvalidGregorianDate`() {
        val r = gregorianDateOrError(2026, 2, 31)
        assertTrue(r is LunarResult.Failure)
        assertTrue((r as LunarResult.Failure).error is LunarError.InvalidGregorianDate)
    }

    // ----- Tất định -----

    @Test
    fun `ket qua khong phu thuoc timezone mac dinh cua may`() {
        val goc = java.util.TimeZone.getDefault()
        try {
            val mocs = listOf(
                LocalDate.of(1901, 1, 1), LocalDate.of(1985, 1, 21),
                LocalDate.of(2026, 2, 17), LocalDate.of(2100, 12, 31),
            )
            val chuan = mocs.map { LunarTestSupport.lunarOf(it.year, it.monthValue, it.dayOfMonth) }
            // Phủ đúng các zone mà audit §XIII T2 yêu cầu, cộng vài zone cực đoan:
            // UTC+7 (chính quy chiếu), UTC+8 (lệch một giờ — dễ lộ nếu engine lỡ đọc
            // giờ máy), UTC, Los Angeles (lệch ngày), Kiritimati (UTC+14).
            val zones = listOf(
                "UTC", "Asia/Ho_Chi_Minh", "Asia/Shanghai", "America/Los_Angeles",
                "Asia/Tokyo", "America/New_York", "Pacific/Kiritimati",
            )
            for (tz in zones) {
                java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone(tz))
                assertEquals(
                    "đổi timezone sang $tz làm đổi kết quả",
                    chuan,
                    mocs.map { LunarTestSupport.lunarOf(it.year, it.monthValue, it.dayOfMonth) },
                )
            }
        } finally {
            java.util.TimeZone.setDefault(goc)
        }
    }
}
