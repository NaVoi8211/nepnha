package com.nepnha.core.lunar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TIER 3 — **cross-implementation check**, KHÔNG phải oracle độc lập.
 *
 * Fixture do mô hình tham chiếu Python sinh ra từ **chính** file dataset mà engine
 * Kotlin đọc. Nên mọi khác biệt ở đây là **lỗi chuyển ngữ sang Kotlin**, không phải
 * khác biệt nguồn dữ liệu.
 *
 * Chuỗi bằng chứng: HKO (2.474/2.474 tháng, nguồn ngoài) → mô hình tham chiếu →
 * fixture → Kotlin. Bằng chứng độc lập nằm ở đầu chuỗi, không nằm ở test này.
 */
class LunarFixtureTest {

    @Test
    fun `Kotlin tai lap dung toan bo fixture`() {
        val cal = LunarTestSupport.calendar
        val rows = LunarTestSupport.fixture
        assertTrue("fixture phải có đủ mẫu", rows.size > 1500)

        val wrong = rows.filter { row ->
            (cal.toLunar(row.gregorian) as? LunarResult.Success)?.value != row.lunar
        }
        assertEquals(
            "lệch ${wrong.size}/${rows.size}; ví dụ: " +
                wrong.take(3).joinToString { "${it.gregorian} kỳ vọng ${it.lunar}" },
            0,
            wrong.size,
        )
    }

    @Test
    fun `fixture co phu thang nhuan`() {
        assertTrue(LunarTestSupport.fixture.count { it.lunar.isLeapMonth } >= 30)
    }
}
