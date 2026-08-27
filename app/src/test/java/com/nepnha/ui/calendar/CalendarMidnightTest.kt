package com.nepnha.ui.calendar

import com.nepnha.core.lunar.LunarTestSupport
import com.nepnha.domain.calendar.LunarCalendarService
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Qua nửa đêm, ô "hôm nay" trên lưới lịch phải nhảy sang ngày mới.
 *
 * Mô phỏng bằng cách gọi thẳng `refreshToday` với ngày giả — **không** chờ đồng hồ
 * thật, nên test chạy trong vài mili giây và luôn cho cùng kết quả.
 */
class CalendarMidnightTest {

    private val service = LunarCalendarService(LunarTestSupport.calendar)

    /**
     * Sai thì: app mở qua nửa đêm vẫn tô "hôm nay" vào ngày hôm qua.
     */
    @Test
    fun qua_nua_dem_thi_o_hom_nay_nhay_sang_ngay_moi() {
        val vm = CalendarViewModel(service, LocalDate.of(2026, 8, 26))
        val before = vm.state.value.cells.filterIsInstance<CalendarCell.Day>().single { it.isToday }
        assertEquals(LocalDate.of(2026, 8, 26), before.lunar.solar)

        vm.refreshToday(LocalDate.of(2026, 8, 27))

        val after = vm.state.value.cells.filterIsInstance<CalendarCell.Day>().single { it.isToday }
        assertEquals(LocalDate.of(2026, 8, 27), after.lunar.solar)
        assertEquals(LocalDate.of(2026, 8, 27), vm.state.value.today)
    }

    /**
     * Người dùng đang xem chính ngày hôm nay ⇒ lựa chọn dời sang ngày mới, và ngày âm
     * ở thẻ chi tiết đổi theo.
     *
     * Sai thì: mở app buổi sáng vẫn thấy thẻ chi tiết của ngày hôm qua.
     */
    @Test
    fun dang_chon_hom_nay_thi_lua_chon_doi_theo_va_ngay_am_doi_theo() {
        val vm = CalendarViewModel(service, LocalDate.of(2026, 8, 26))
        assertEquals(service.dayOf(LocalDate.of(2026, 8, 26)), vm.state.value.selectedLunar)

        vm.refreshToday(LocalDate.of(2026, 8, 27))

        assertEquals(LocalDate.of(2026, 8, 27), vm.state.value.selected)
        assertEquals(service.dayOf(LocalDate.of(2026, 8, 27)), vm.state.value.selectedLunar)
    }

    /**
     * Nếu người dùng đang cố ý xem một ngày khác thì đổi ngày **không được** giật
     * lựa chọn của họ về hôm nay.
     *
     * Sai thì: đang xem tháng 12 mà qua nửa đêm bị kéo về tháng 8.
     */
    @Test
    fun dang_xem_ngay_khac_thi_khong_bi_giat_lua_chon() {
        val vm = CalendarViewModel(service, LocalDate.of(2026, 8, 26))
        vm.select(LocalDate.of(2026, 12, 3))

        vm.refreshToday(LocalDate.of(2026, 8, 27))

        assertEquals(LocalDate.of(2026, 12, 3), vm.state.value.selected)
        assertEquals(YearMonth.of(2026, 12), vm.state.value.month)
    }

    /**
     * Ngày chưa đổi thì gọi bao nhiêu lần cũng vô hại — `ON_RESUME` bắn mỗi lần quay
     * lại tiền cảnh, không được nhấp nháy giao diện.
     *
     * Sai thì: mỗi lần chuyển tab là lưới bị dựng lại và lựa chọn bị mất.
     */
    @Test
    fun goi_lai_voi_cung_ngay_thi_khong_doi_gi() {
        val vm = CalendarViewModel(service, LocalDate.of(2026, 8, 26))
        vm.select(LocalDate.of(2026, 8, 10))
        val snapshot = vm.state.value

        repeat(5) { vm.refreshToday(LocalDate.of(2026, 8, 26)) }

        assertTrue("state bị dựng lại dù ngày không đổi", vm.state.value === snapshot)
    }
}
