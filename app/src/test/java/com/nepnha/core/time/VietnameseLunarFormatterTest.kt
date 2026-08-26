package com.nepnha.core.time

import com.nepnha.core.lunar.LunarDate
import com.nepnha.core.lunar.SexagenaryYear
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Cách đọc ngày âm bằng tiếng Việt.
 *
 * Chữ "nhuận" là phần quan trọng nhất ở đây: hiển thị thiếu nó là làm sai ngày giỗ
 * của người ta.
 */
class VietnameseLunarFormatterTest {

    /**
     * Sai thì: tháng nhuận trông y hệt tháng thường trên màn hình.
     */
    @Test
    fun `thang nhuan luon kem chu nhuan`() {
        assertEquals("14 tháng 7", VietnameseLunarFormatter.dayAndMonth(LunarDate(14, 7, 2026)))
        assertEquals(
            "14 tháng 7 nhuận",
            VietnameseLunarFormatter.dayAndMonth(LunarDate(14, 7, 2026, isLeapMonth = true)),
        )
    }

    /**
     * Tháng nhuận mang số của tháng liền trước — không bao giờ được cộng một.
     *
     * Sai thì: lần xuất hiện thứ hai của tháng 7 bị gọi là tháng 8.
     */
    @Test
    fun `khong bao gio doi so thang nhuan thanh thang ke tiep`() {
        val leap = LunarDate(1, 8, 1938, isLeapMonth = true)
        val text = VietnameseLunarFormatter.dayAndMonth(leap)
        assertEquals("1 tháng 8 nhuận", text)
        assert(!text.contains("tháng 9")) { "tháng nhuận bị đánh số cộng một" }
    }

    /**
     * Sai thì: thẻ ngày trên màn Nhà mất tên năm can chi.
     */
    @Test
    fun `dang day du kem can chi`() {
        assertEquals(
            "14 tháng 7 năm Bính Ngọ",
            VietnameseLunarFormatter.full(LunarDate(14, 7, 2026), SexagenaryYear("Bính", "Ngọ")),
        )
    }

    /**
     * Ô lịch chỉ hiện số ngày, trừ mùng 1 thì kèm tháng để định vị.
     *
     * Sai thì: lướt lưới không biết đang ở tháng âm nào.
     */
    @Test
    fun `nhan o luoi gon nhung mung 1 co kem thang`() {
        assertEquals("14", VietnameseLunarFormatter.gridLabel(LunarDate(14, 7, 2026)))
        assertEquals("1/7", VietnameseLunarFormatter.gridLabel(LunarDate(1, 7, 2026)))
        assertEquals(
            "1/7N",
            VietnameseLunarFormatter.gridLabel(LunarDate(1, 7, 2026, isLeapMonth = true)),
        )
    }

    /**
     * Không được phụ thuộc Locale mặc định — máy đặt tiếng Ả Rập từng làm
     * `String.format` sinh chữ số Đông Ả Rập, đúng lỗi mà test Phase 1 đã bắt.
     *
     * Sai thì: người dùng đổi ngôn ngữ hệ thống là ngày âm hiện sai chữ số.
     */
    @Test
    fun `khong phu thuoc Locale mac dinh`() {
        val goc = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("ar-EG"))
            assertEquals("14 tháng 7", VietnameseLunarFormatter.dayAndMonth(LunarDate(14, 7, 2026)))
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            assertEquals(
                "1 tháng 7 nhuận",
                VietnameseLunarFormatter.dayAndMonth(LunarDate(1, 7, 2026, isLeapMonth = true)),
            )
        } finally {
            Locale.setDefault(goc)
        }
    }
}
