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
    /**
     * Tên đã lưu. Khi có [memberId] thì tên hiển thị lấy từ thành viên; trường này
     * vẫn giữ nguyên làm **bản chụp lúc liên kết**, để nếu thành viên bị xoá thì ngày
     * giỗ vẫn còn tên chứ không thành bản ghi vô danh.
     */
    val name: String,
    /** 1..30. Giữ nguyên vĩnh viễn, kể cả khi một năm nào đó phải lùi về 29. */
    val lunarDay: Int,
    /** 1..12. */
    val lunarMonth: Int,
    val rule: MemorialRule,
    val note: String?,
    /**
     * Liên kết tuỳ chọn tới thành viên. `null` = tên tự do.
     *
     * Đặt **cuối** danh sách tham số một cách có chủ đích: trường thêm sau không được
     * làm lệch mọi lời gọi theo vị trí đã có.
     */
    val memberId: Long? = null,
)

/** Dữ liệu đã kiểm tra hợp lệ, sẵn sàng ghi xuống Room. */
data class MemorialDraft(
    val name: String,
    val lunarDay: Int,
    val lunarMonth: Int,
    val rule: MemorialRule,
    val note: String?,
    val memberId: Long? = null,
)

/**
 * Tên hiển thị cho một ngày giỗ.
 *
 * Quy tắc đã chốt ở Gate 1:
 *  · có liên kết **và** thành viên còn tồn tại ⇒ dùng **tên hiện tại của thành viên**,
 *    nên đổi tên ở màn Gia đình là ngày giỗ đổi theo — đó chính là lý do liên kết;
 *  · còn lại ⇒ dùng [Memorial.name] đã lưu.
 *
 * Hàm thuần để test được trên JVM và để không màn hình nào tự nghĩ ra quy tắc riêng.
 */
fun Memorial.displayName(members: List<FamilyMember>): String =
    memberId?.let { id -> members.firstOrNull { it.id == id }?.fullName } ?: name
