package com.nepnha.core.time

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class VietnameseDateFormatterTest {

    @Test
    fun `ten thu dung cho ca bay ngay`() {
        // 2026-08-24 là Thứ Hai.
        val monday = LocalDate.of(2026, 8, 24)
        val expected = listOf(
            "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy", "Chủ Nhật",
        )
        expected.forEachIndexed { offset, name ->
            assertEquals(name, VietnameseDateFormatter.dayOfWeek(monday.plusDays(offset.toLong())))
        }
    }

    @Test
    fun `full date theo cach doc cua nguoi Viet`() {
        assertEquals("24 tháng 8, 2026", VietnameseDateFormatter.fullDate(LocalDate.of(2026, 8, 24)))
        assertEquals("1 tháng 1, 2027", VietnameseDateFormatter.fullDate(LocalDate.of(2027, 1, 1)))
    }

    @Test
    fun `short date luon co hai chu so`() {
        assertEquals("01/09/2026", VietnameseDateFormatter.shortDate(LocalDate.of(2026, 9, 1)))
        assertEquals("31/12/2026", VietnameseDateFormatter.shortDate(LocalDate.of(2026, 12, 31)))
    }

    @Test
    fun `month title`() {
        assertEquals("Tháng 8, 2026", VietnameseDateFormatter.monthTitle(LocalDate.of(2026, 8, 24)))
    }

    /**
     * Chống hồi quy: định dạng KHÔNG được đổi theo Locale mặc định của máy.
     * Locale Ả Rập dùng chữ số Đông Ả Rập nếu ta lỡ để `String.format` dùng
     * locale mặc định.
     */
    @Test
    fun `khong phu thuoc Locale mac dinh`() {
        val original = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("ar-EG"))
            val date = LocalDate.of(2026, 9, 1)
            assertEquals("01/09/2026", VietnameseDateFormatter.shortDate(date))
            assertEquals("Thứ Ba", VietnameseDateFormatter.dayOfWeek(date))
        } finally {
            Locale.setDefault(original)
        }
    }
}
