package com.nepnha.domain.model

import com.nepnha.domain.event.MemorialRule
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tên hiển thị của ngày giỗ — quy tắc đã chốt ở Gate 1.
 *
 * Đây là hàm thuần, nên mọi màn hình dùng chung một quy tắc thay vì mỗi nơi tự nghĩ.
 */
class MemorialDisplayNameTest {

    private fun member(id: Long, name: String) = FamilyMember(
        id = id,
        familyId = 1,
        fullName = name,
        gender = Gender.UNSPECIFIED,
        solarBirthDate = null,
        lunarBirthDate = null,
        role = null,
        note = null,
    )

    private fun memorial(name: String, memberId: Long?) = Memorial(
        id = 1,
        familyId = 1,
        name = name,
        memberId = memberId,
        lunarDay = 1,
        lunarMonth = 1,
        rule = MemorialRule(),
        note = null,
    )

    /** Sai thì: ngày giỗ không liên kết bị mất tên người dùng đã nhập. */
    @Test
    fun `khong lien ket thi dung ten da luu`() {
        assertEquals("Cụ ông", memorial("Cụ ông", null).displayName(emptyList()))
        assertEquals(
            "Cụ ông",
            memorial("Cụ ông", null).displayName(listOf(member(7, "Người khác"))),
        )
    }

    /**
     * Sai thì: liên kết trở nên vô nghĩa — đổi tên ở màn Gia đình mà ngày giỗ không
     * đổi theo thì chẳng ai cần liên kết làm gì.
     */
    @Test
    fun `co lien ket thi lay ten hien tai cua thanh vien`() {
        val m = memorial("Tên lúc tạo", memberId = 7)
        assertEquals("Nguyễn Văn A", m.displayName(listOf(member(7, "Nguyễn Văn A"))))
        // Đổi tên thành viên ⇒ ngày giỗ đổi theo ngay, không cần sửa lại ngày giỗ.
        assertEquals("Nguyễn Văn B", m.displayName(listOf(member(7, "Nguyễn Văn B"))))
    }

    /**
     * Thành viên bị xoá ⇒ quay về tên đã lưu, **không** thành bản ghi vô danh.
     *
     * Sai thì: xoá một thành viên làm ngày giỗ mất tên — mất dữ liệu người dùng vì
     * một thao tác ở màn hình khác.
     */
    @Test
    fun `thanh vien bi xoa thi quay ve ten da luu`() {
        val m = memorial("Nguyễn Văn A", memberId = 7)
        assertEquals("Nguyễn Văn A", m.displayName(emptyList()))
        assertEquals("Nguyễn Văn A", m.displayName(listOf(member(9, "Người khác"))))
    }
}
