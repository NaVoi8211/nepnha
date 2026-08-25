package com.nepnha.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class MemberFormValidatorTest {

    private fun valid(input: MemberFormInput): FamilyMemberDraft {
        val result = MemberFormValidator.validate(input)
        assertTrue("Mong đợi hợp lệ nhưng nhận: $result", result is MemberFormResult.Valid)
        return (result as MemberFormResult.Valid).draft
    }

    private fun errors(input: MemberFormInput): Set<MemberFormError> {
        val result = MemberFormValidator.validate(input)
        assertTrue("Mong đợi lỗi nhưng lại hợp lệ", result is MemberFormResult.Invalid)
        return (result as MemberFormResult.Invalid).errors
    }

    @Test
    fun `chi ho ten la bat buoc`() {
        val draft = valid(MemberFormInput(fullName = "Nguyễn Văn A"))
        assertEquals("Nguyễn Văn A", draft.fullName)
        assertNull(draft.solarBirthDate)
        assertNull(draft.lunarBirthDate)
        assertNull(draft.role)
        assertNull(draft.note)
        assertEquals(Gender.UNSPECIFIED, draft.gender)
    }

    @Test
    fun `ten trong bi tu choi`() {
        assertEquals(setOf(MemberFormError.NAME_REQUIRED), errors(MemberFormInput(fullName = "   ")))
    }

    @Test
    fun `ten duoc trim`() {
        assertEquals("Trần Thị B", valid(MemberFormInput(fullName = "  Trần Thị B  ")).fullName)
    }

    @Test
    fun `ngay sinh duong day du thi duoc parse`() {
        val draft = valid(
            MemberFormInput(fullName = "A", solarDay = "24", solarMonth = "8", solarYear = "1950"),
        )
        assertEquals(LocalDate.of(1950, 8, 24), draft.solarBirthDate)
    }

    @Test
    fun `ngay sinh duong thieu mot o thi bao loi`() {
        assertTrue(
            MemberFormError.SOLAR_DATE_INCOMPLETE in
                errors(MemberFormInput(fullName = "A", solarDay = "24", solarMonth = "8")),
        )
    }

    @Test
    fun `ngay duong khong co that thi bao loi`() {
        // 31/02 không tồn tại; 29/02/1950 cũng không (1950 không nhuận).
        assertTrue(
            MemberFormError.SOLAR_DATE_INVALID in
                errors(MemberFormInput(fullName = "A", solarDay = "31", solarMonth = "2", solarYear = "1950")),
        )
        assertTrue(
            MemberFormError.SOLAR_DATE_INVALID in
                errors(MemberFormInput(fullName = "A", solarDay = "29", solarMonth = "2", solarYear = "1950")),
        )
    }

    @Test
    fun `nam ngoai khoang cho phep bi tu choi`() {
        assertTrue(
            MemberFormError.SOLAR_DATE_INVALID in
                errors(MemberFormInput(fullName = "A", solarDay = "1", solarMonth = "1", solarYear = "1800")),
        )
    }

    @Test
    fun `ngay sinh am duoc danh dau la nguoi dung cung cap`() {
        val draft = valid(
            MemberFormInput(
                fullName = "A",
                lunarDay = "15", lunarMonth = "7", lunarYear = "1950",
                lunarIsLeapMonth = true,
            ),
        )
        val lunar = requireNotNull(draft.lunarBirthDate)
        assertEquals(15, lunar.day)
        assertEquals(7, lunar.month)
        assertEquals(1950, lunar.year)
        assertTrue(lunar.isLeapMonth)
        // Phase 2 chưa có engine ⇒ mọi ngày âm đều là do người dùng khai.
        assertEquals(LunarBirthDate.Source.USER_PROVIDED, lunar.source)
    }

    @Test
    fun `ngay am 30 van duoc chap nhan`() {
        // Tháng âm có thể có 30 ngày. Không thể biết năm đó tháng đó có 30 hay không
        // nếu chưa có lịch âm — và tuyệt đối không được đoán.
        val draft = valid(
            MemberFormInput(fullName = "A", lunarDay = "30", lunarMonth = "8", lunarYear = "1960"),
        )
        assertEquals(30, requireNotNull(draft.lunarBirthDate).day)
    }

    @Test
    fun `ngay am ngoai khoang bi tu choi`() {
        assertTrue(
            MemberFormError.LUNAR_DATE_INVALID in
                errors(MemberFormInput(fullName = "A", lunarDay = "31", lunarMonth = "8", lunarYear = "1960")),
        )
        assertTrue(
            MemberFormError.LUNAR_DATE_INVALID in
                errors(MemberFormInput(fullName = "A", lunarDay = "5", lunarMonth = "13", lunarYear = "1960")),
        )
    }

    @Test
    fun `khong nhap gi thi khong sinh ra ngay mac dinh`() {
        val draft = valid(MemberFormInput(fullName = "A"))
        // "chưa biết" phải là null, không phải 1/1 hay 0/0/0.
        assertNull(draft.solarBirthDate)
        assertNull(draft.lunarBirthDate)
    }

    @Test
    fun `role va note trong thi thanh null`() {
        val draft = valid(MemberFormInput(fullName = "A", role = "  ", note = ""))
        assertNull(draft.role)
        assertNull(draft.note)
    }

    @Test
    fun `nhieu loi cung luc deu duoc bao`() {
        val e = errors(
            MemberFormInput(fullName = "", solarDay = "1", solarMonth = "", solarYear = "1950"),
        )
        assertTrue(MemberFormError.NAME_REQUIRED in e)
        assertTrue(MemberFormError.SOLAR_DATE_INCOMPLETE in e)
    }
}
