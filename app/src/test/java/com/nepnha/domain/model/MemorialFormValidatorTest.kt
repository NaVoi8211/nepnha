package com.nepnha.domain.model

import com.nepnha.domain.event.LeapMonthPolicy
import com.nepnha.domain.event.MissingDayPolicy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MemorialFormValidatorTest {

    private fun valid(input: MemorialFormInput) =
        (MemorialFormValidator.validate(input) as MemorialFormResult.Valid).draft

    private fun errors(input: MemorialFormInput) =
        (MemorialFormValidator.validate(input) as MemorialFormResult.Invalid).errors

    /** Sai thì: lưu được ngày giỗ không tên, danh sách toàn dòng trống. */
    @Test
    fun `ten la bat buoc`() {
        assertTrue(
            MemorialFormError.NAME_REQUIRED in
                errors(MemorialFormInput(name = "   ", lunarDay = "1", lunarMonth = "1")),
        )
    }

    /**
     * Ngày 30 luôn được chấp nhận, kể cả khi nhiều năm tháng đó chỉ có 29 ngày.
     *
     * Sai thì: người dùng không khai báo được ngày giỗ mùng 30 — trong khi đó là một
     * ngày âm hoàn toàn có thật.
     */
    @Test
    fun `ngay 30 luon duoc chap nhan`() {
        val d = valid(MemorialFormInput(name = "Cụ ông", lunarDay = "30", lunarMonth = "7"))
        assertEquals(30, d.lunarDay)
        assertEquals(7, d.lunarMonth)
    }

    /** Sai thì: gõ nhầm 31 hoặc tháng 13 vẫn lưu được và hỏng khi quy đổi. */
    @Test
    fun `ngay va thang ngoai khoang bi tu choi`() {
        assertTrue(
            MemorialFormError.DAY_INVALID in
                errors(MemorialFormInput(name = "A", lunarDay = "31", lunarMonth = "7")),
        )
        assertTrue(
            MemorialFormError.MONTH_INVALID in
                errors(MemorialFormInput(name = "A", lunarDay = "1", lunarMonth = "13")),
        )
        assertTrue(
            MemorialFormError.DAY_INVALID in
                errors(MemorialFormInput(name = "A", lunarDay = "0", lunarMonth = "1")),
        )
    }

    /** Sai thì: người dùng chọn tháng nhuận nhưng lưu xuống thành tháng thường. */
    @Test
    fun `policy duoc giu nguyen khi luu`() {
        val d = valid(
            MemorialFormInput(
                name = "Cụ bà",
                lunarDay = "15",
                lunarMonth = "7",
                leapMonthPolicy = LeapMonthPolicy.LEAP_MONTH_ONLY,
                missingDayPolicy = MissingDayPolicy.SKIP,
            ),
        )
        assertEquals(LeapMonthPolicy.LEAP_MONTH_ONLY, d.rule.leapMonthPolicy)
        assertEquals(MissingDayPolicy.SKIP, d.rule.missingDayPolicy)
    }

    /** Sai thì: ghi chú toàn khoảng trắng biến thành một dòng trống trong DB. */
    @Test
    fun `ten duoc trim va ghi chu trong thanh null`() {
        val d = valid(
            MemorialFormInput(name = "  Cụ ông  ", lunarDay = "1", lunarMonth = "1", note = "   "),
        )
        assertEquals("Cụ ông", d.name)
        assertNull(d.note)
    }

    /** Sai thì: chỉ báo một lỗi mỗi lần, người dùng phải sửa nhiều vòng. */
    @Test
    fun `nhieu loi cung luc deu duoc bao`() {
        val e = errors(MemorialFormInput(name = "", lunarDay = "99", lunarMonth = "99"))
        assertEquals(
            setOf(
                MemorialFormError.NAME_REQUIRED,
                MemorialFormError.DAY_INVALID,
                MemorialFormError.MONTH_INVALID,
            ),
            e,
        )
    }
}
