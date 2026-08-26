package com.nepnha.core.time

import com.nepnha.core.lunar.LunarDate
import com.nepnha.core.lunar.SexagenaryYear

/**
 * Chuyển [LunarDate] thành chữ theo cách người Việt đọc.
 *
 * Kotlin thuần, không `Context`, không `Locale` — cùng lý do như
 * [VietnameseDateFormatter]: đây là **dữ liệu của hàm thuần**, cần test được trên JVM
 * và không được đổi khi người dùng đổi ngôn ngữ hệ thống.
 *
 * File này **không tính lịch**. Nó chỉ định dạng thứ engine đã trả về.
 *
 * THÁNG NHUẬN: luôn kèm chữ "nhuận". Tháng 7 nhuận là **lần xuất hiện thứ hai của
 * tháng 7**, không phải tháng 8 — hiển thị sai chỗ này là làm sai ngày giỗ của người
 * ta, nên không có biến thể nào bỏ chữ "nhuận" đi cho gọn.
 */
object VietnameseLunarFormatter {

    /** "14 tháng 7" · "14 tháng 7 nhuận". */
    fun dayAndMonth(lunar: LunarDate): String =
        "${lunar.day} tháng ${lunar.month}" + if (lunar.isLeapMonth) " nhuận" else ""

    /** "14 tháng 7 năm Bính Ngọ" · "14 tháng 7 nhuận năm Bính Ngọ". */
    fun full(lunar: LunarDate, year: SexagenaryYear): String =
        "${dayAndMonth(lunar)} năm $year"

    /**
     * Nhãn ngắn cho ô lịch: chỉ số ngày, trừ mùng 1 thì kèm tháng để người xem biết
     * mình đang ở tháng âm nào — "1/7" hoặc "1/7N".
     */
    fun gridLabel(lunar: LunarDate): String =
        if (lunar.day == 1) "1/${lunar.month}${if (lunar.isLeapMonth) "N" else ""}"
        else lunar.day.toString()
}
