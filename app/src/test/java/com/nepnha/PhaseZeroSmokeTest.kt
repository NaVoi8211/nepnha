package com.nepnha

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/**
 * Smoke test của Phase 0: chứng minh JVM unit-test harness sống, coroutines-test
 * hoạt động, và `java.time` dùng được native ở minSdk 26 (không cần desugaring).
 * Test lịch âm thật thuộc Phase 3.
 */
class PhaseZeroSmokeTest {

    @Test
    fun `java_time kha dung khong can desugaring`() {
        val d = LocalDate.of(2026, 8, 24)
        assertEquals(2026, d.year)
        assertEquals(8, d.monthValue)
        assertEquals(24, d.dayOfMonth)
    }

    @Test
    fun `timezone Viet Nam la UTC+7`() {
        val offset = ZoneId.of("Asia/Ho_Chi_Minh")
            .rules
            .getOffset(LocalDate.of(2026, 8, 24).atStartOfDay())
        assertEquals(7 * 3600, offset.totalSeconds)
    }

    @Test
    fun `coroutines test harness chay duoc`() = runTest {
        var done = false
        done = true
        assertTrue(done)
    }
}
