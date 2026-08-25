package com.nepnha.core.lunar

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * TIER 1 — kỳ vọng đến từ nguồn NGOÀI, không phải từ implementation của ta.
 *
 * Đây là tầng test duy nhất có thể nói "engine đúng". Các tầng khác chỉ nói "engine
 * nhất quán".
 */
class LunarExternalVectorTest {

    private val cal = LunarTestSupport.calendar

    private fun assertLunar(g: LocalDate, d: Int, m: Int, y: Int, leap: Boolean, source: String) {
        val r = cal.toLunar(g)
        val got = (r as? LunarResult.Success)?.value
        assertEquals("$g — nguồn: $source", LunarDate(d, m, y, leap), got)
    }

    // ----- Văn bản nhà nước: oracle Tier 1 -----

    @Test
    fun `van ban nha nuoc 6150 TB-BLDTBXH nam 2025`() {
        val src = "Thông báo 6150/TB-BLĐTBXH ngày 03/12/2024"
        assertLunar(LocalDate.of(2025, 1, 25), 26, 12, 2024, false, src)
        assertLunar(LocalDate.of(2025, 1, 29), 1, 1, 2025, false, src)   // mùng 1 Tết Ất Tỵ
        assertLunar(LocalDate.of(2025, 2, 2), 5, 1, 2025, false, src)
    }

    @Test
    fun `van ban nha nuoc 9441 TB-BNV nam 2026`() {
        val src = "Thông báo 9441/TB-BNV"
        assertLunar(LocalDate.of(2026, 2, 14), 27, 12, 2025, false, src)
        assertLunar(LocalDate.of(2026, 2, 17), 1, 1, 2026, false, src)   // mùng 1 Tết Bính Ngọ
        assertLunar(LocalDate.of(2026, 2, 22), 6, 1, 2026, false, src)
    }

    // ----- Việt Nam khác Trung Quốc: bằng chứng engine dùng 105 độ Đông -----

    @Test
    fun `Tet At Suu 1985 la 21 thang 1 chu khong phai 20 thang 2`() {
        // Ban Lịch Nhà nước qua báo Thanh Niên: Việt Nam ăn Tết trước Trung Quốc MỘT
        // THÁNG. Nếu engine trả 20/02 thì nó đang là lịch Trung Quốc.
        assertLunar(LocalDate.of(1985, 1, 21), 1, 1, 1985, false, "Thanh Niên / Ban Lịch NN")
        assertLunar(LocalDate.of(1985, 2, 20), 1, 2, 1985, false, "suy ra từ cùng nguồn")
    }

    @Test
    fun `Tet 2007 2030 2053 lech mot ngay so voi Trung Quoc`() {
        assertLunar(LocalDate.of(2007, 2, 17), 1, 1, 2007, false, "Tuổi Trẻ; HND calrules")
        assertLunar(LocalDate.of(2030, 2, 2), 1, 1, 2030, false, "HND calrules")
        assertLunar(LocalDate.of(2053, 2, 18), 1, 1, 2053, false, "HND calrules")
    }

    @Test
    fun `thang nhuan 1984 va 1987 theo Ban Lich Nha nuoc`() {
        // Hànộimới dẫn Ban Lịch Nhà nước: 1984 Việt Nam KHÔNG nhuận (Trung Quốc nhuận
        // tháng 10); 1987 Việt Nam nhuận tháng 7 (Trung Quốc nhuận tháng 6).
        assertEquals(
            LunarResult.Success(LeapMonthInfo.None),
            cal.leapMonthOf(1984),
        )
        assertEquals(
            LunarResult.Success(LeapMonthInfo.Month(7)),
            cal.leapMonthOf(1987),
        )
    }

    // ----- Can Chi -----

    @Test
    fun `can chi cua nam am`() {
        assertEquals(LunarResult.Success(SexagenaryYear("Bính", "Ngọ")), cal.sexagenaryYear(2026))
        assertEquals(LunarResult.Success(SexagenaryYear("Ất", "Sửu")), cal.sexagenaryYear(1985))
        assertEquals(LunarResult.Success(SexagenaryYear("Giáp", "Thìn")), cal.sexagenaryYear(2024))
        assertEquals(LunarResult.Success(SexagenaryYear("Mậu", "Thân")), cal.sexagenaryYear(1968))
    }

    @Test
    fun `can chi theo nam AM chu khong theo nam duong`() {
        // 16/02/2026 là ngày dương năm 2026 nhưng còn thuộc năm âm Ất Tỵ (2025),
        // vì Tết Bính Ngọ là 17/02. Lấy modulo năm dương sẽ sai ở đây.
        val truocTet = LunarTestSupport.lunarOf(2026, 2, 16)
        assertEquals(2025, truocTet.year)
        assertEquals(
            LunarResult.Success(SexagenaryYear("Ất", "Tỵ")),
            cal.sexagenaryYear(truocTet.year),
        )
    }
}
