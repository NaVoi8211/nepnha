package com.nepnha.core.lunar

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

/**
 * TIER 4 — chạy trên thiết bị thật (Samsung A32).
 *
 * Test JVM đọc asset từ đĩa; test này đọc qua `AssetManager` của Android. Cả hai
 * phải cho **cùng** kết quả — nếu khác thì việc đóng gói asset vào APK có vấn đề.
 *
 * Không cần Internet.
 */
@RunWith(AndroidJUnit4::class)
class LunarAssetTest {

    private fun loadFromApk(): VietnameseLunarCalendar {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        val bytes = ctx.assets.open("lunar/vn_lunar_v1.bin").use { it.readBytes() }
        return VietnameseLunarCalendar.create(LunarDataset.parse(bytes))
    }

    @Test
    fun dataset_duoc_dong_goi_va_doc_duoc_tu_apk() {
        val cal = loadFromApk()
        assertEquals(1901..2100, cal.supportedYears)
    }

    @Test
    fun ket_qua_tren_thiet_bi_khop_vector_van_ban_nha_nuoc() {
        val cal = loadFromApk()
        assertEquals(
            LunarResult.Success(LunarDate(1, 1, 2026)),
            cal.toLunar(LocalDate.of(2026, 2, 17)),   // mùng 1 Tết Bính Ngọ, 9441/TB-BNV
        )
        assertEquals(
            LunarResult.Success(LunarDate(1, 1, 2025)),
            cal.toLunar(LocalDate.of(2025, 1, 29)),   // mùng 1 Tết Ất Tỵ, 6150/TB-BLĐTBXH
        )
        assertEquals(
            LunarResult.Success(LunarDate(1, 1, 1985)),
            cal.toLunar(LocalDate.of(1985, 1, 21)),   // Tết Ất Sửu — lịch VN, không phải TQ
        )
    }

    @Test
    fun hieu_nang_tren_thiet_bi_that() {
        val cal = loadFromApk()
        val start = System.nanoTime()
        var d = LocalDate.of(2026, 1, 1)
        repeat(365) {
            cal.toLunar(d)
            d = d.plusDays(1)
        }
        val ms = (System.nanoTime() - start) / 1_000_000.0
        // Một năm lịch phải xong trong chớp mắt trên A32.
        assertTrue("chuyển đổi 365 ngày mất ${ms}ms", ms < 500)
    }

    @Test
    fun khong_phu_thuoc_timezone_cua_thiet_bi() {
        val cal = loadFromApk()
        val goc = java.util.TimeZone.getDefault()
        try {
            val mong1Tet = LocalDate.of(2026, 2, 17)
            val chuan = cal.toLunar(mong1Tet)
            for (tz in listOf("Asia/Tokyo", "UTC", "America/New_York")) {
                java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone(tz))
                assertEquals("timezone $tz làm đổi kết quả", chuan, cal.toLunar(mong1Tet))
            }
        } finally {
            java.util.TimeZone.setDefault(goc)
        }
    }
}
