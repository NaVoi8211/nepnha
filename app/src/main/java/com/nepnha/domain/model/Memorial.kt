package com.nepnha.domain.model

import com.nepnha.domain.event.MemorialRule

/**
 * Một ngày giỗ, khai báo theo **ngày âm**.
 *
 * Điều quan trọng nhất của kiểu này: nó lưu **ngày âm gốc người dùng nhập**, không
 * lưu ngày dương đã tính. Ngày dương đổi mỗi năm và phụ thuộc quy tắc; lưu nó là
 * đóng băng một năm cụ thể vào dữ liệu và mất luôn ý định ban đầu.
 *
 * Không có `lunarYear`: ngày giỗ lặp lại **hằng năm**, năm mất là thông tin khác và
 * chưa cần cho MVP.
 *
 * `isLeapMonth` cũng cố ý **không** nằm ở đây — nó đã được mã hoá trong
 * [MemorialRule.leapMonthPolicy]. Lưu cả hai là mở đường cho hai nguồn sự thật mâu
 * thuẫn nhau.
 */
data class Memorial(
    val id: Long,
    val familyId: Long,
    val name: String,
    /** 1..30. Giữ nguyên vĩnh viễn, kể cả khi một năm nào đó phải lùi về 29. */
    val lunarDay: Int,
    /** 1..12. */
    val lunarMonth: Int,
    val rule: MemorialRule,
    val note: String?,
)

/** Dữ liệu đã kiểm tra hợp lệ, sẵn sàng ghi xuống Room. */
data class MemorialDraft(
    val name: String,
    val lunarDay: Int,
    val lunarMonth: Int,
    val rule: MemorialRule,
    val note: String?,
)
