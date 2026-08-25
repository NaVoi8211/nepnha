package com.nepnha.core.lunar

import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * TIER 2 — bất biến toán học, chạy trên **toàn bộ** 1901–2100.
 *
 * ⚠️ Các test này chứng minh engine **nhất quán nội bộ**, KHÔNG chứng minh lịch đúng.
 * Một engine sai một cách nhất quán vẫn qua hết tầng này. Bằng chứng "đúng" nằm ở
 * [LunarExternalVectorTest].
 */
class LunarInvariantTest {

    private val cal = LunarTestSupport.calendar
    private val first = LocalDate.of(1901, 1, 1)
    private val last = LocalDate.of(2100, 12, 31)

    private inline fun eachDay(block: (LocalDate) -> Unit) {
        var d = first
        while (!d.isAfter(last)) { block(d); d = d.plusDays(1) }
    }

    @Test
    fun `A - moi ngay duong trong pham vi map toi dung mot ngay am`() {
        var n = 0
        eachDay { d ->
            val r = cal.toLunar(d)
            assertTrue("$d không chuyển đổi được: $r", r is LunarResult.Success)
            n++
        }
        // 200 x 365 + 49 ngày nhuận (1904..2096; 2100 không nhuận)
        assertEquals(73_049, n)
    }

    @Test
    fun `C - round trip Gregorian to Lunar to Gregorian`() {
        eachDay { d ->
            val l = (cal.toLunar(d) as LunarResult.Success).value
            val back = cal.toSolar(l)
            assertEquals("$d -> $l -> ?", LunarResult.Success(d), back)
        }
    }

    @Test
    fun `D - round trip Lunar to Gregorian to Lunar`() {
        // Duyệt mọi ngày âm hợp lệ bằng cách đi qua từng ngày dương rồi quay lại.
        eachDay { d ->
            val l = (cal.toLunar(d) as LunarResult.Success).value
            val g = (cal.toSolar(l) as LunarResult.Success).value
            assertEquals(l, (cal.toLunar(g) as LunarResult.Success).value)
        }
    }

    @Test
    fun `E - moi thang am co 29 hoac 30 ngay`() {
        val seen = HashSet<Triple<Int, Int, Boolean>>()
        eachDay { d ->
            val l = (cal.toLunar(d) as LunarResult.Success).value
            seen.add(Triple(l.year, l.month, l.isLeapMonth))
        }
        var c29 = 0
        var c30 = 0
        for ((y, m, leap) in seen) {
            when (val r = cal.daysInLunarMonth(y, m, leap)) {
                is LunarResult.Success -> when (r.value) {
                    29 -> c29++
                    30 -> c30++
                    else -> throw AssertionError("tháng $m/$y nhuận=$leap có ${r.value} ngày")
                }
                is LunarResult.Failure -> {
                    // tháng ở rìa dải, không tra được độ dài — chấp nhận
                }
            }
        }
        assertTrue("phải có cả tháng thiếu lẫn tháng đủ", c29 > 1000 && c30 > 1000)
    }

    @Test
    fun `F - ngay am co co nhuan thi nam do phai that su co thang nhuan do`() {
        eachDay { d ->
            val l = (cal.toLunar(d) as LunarResult.Success).value
            if (!l.isLeapMonth) return@eachDay
            assertEquals(
                "$d -> $l nhưng leapMonthOf(${l.year}) không khớp",
                LunarResult.Success(LeapMonthInfo.Month(l.month)),
                cal.leapMonthOf(l.year),
            )
        }
    }

    @Test
    fun `G - khong ngay duong nao vua thuong vua nhuan`() {
        // Suy trực tiếp từ A: toLunar là hàm, mỗi ngày dương chỉ ra một kết quả.
        // Ở đây kiểm chiều ngược: tháng thường và tháng nhuận cùng số phải cho hai
        // ngày dương KHÁC nhau.
        for (y in 1902..2099) {
            val leap = cal.leapMonthOf(y)
            if (leap !is LunarResult.Success) continue
            val info = leap.value
            if (info !is LeapMonthInfo.Month) continue
            val a = cal.toSolar(LunarDate(1, info.month, y, false))
            val b = cal.toSolar(LunarDate(1, info.month, y, true))
            assertTrue("năm $y tháng ${info.month}: cả hai phải tra được", a is LunarResult.Success)
            assertTrue(b is LunarResult.Success)
            assertTrue(
                "năm $y: tháng ${info.month} thường và nhuận trùng ngày",
                (a as LunarResult.Success).value != (b as LunarResult.Success).value,
            )
        }
    }

    @Test
    fun `H_I_J - cac thang lien tuc, khong hong, khong chong lan`() {
        var prev: LunarDate? = null
        eachDay { d ->
            val l = (cal.toLunar(d) as LunarResult.Success).value
            val p = prev
            if (p != null) {
                val sameMonth = p.month == l.month && p.year == l.year &&
                    p.isLeapMonth == l.isLeapMonth
                if (sameMonth) {
                    assertEquals("$d: ngày trong tháng phải tăng 1", p.day + 1, l.day)
                } else {
                    assertEquals("$d: tháng mới phải bắt đầu từ mùng 1", 1, l.day)
                    assertTrue("$d: tháng trước kết thúc ở ngày ${p.day}", p.day in 29..30)
                }
            }
            prev = l
        }
    }

    @Test
    fun `K - daysInLunarMonth khop voi khoang cach thuc te giua hai mung mot`() {
        for (y in 1902..2099) {
            for (m in 1..12) {
                val len = cal.daysInLunarMonth(y, m, false)
                if (len !is LunarResult.Success) continue
                val start = cal.toSolar(LunarDate(1, m, y, false))
                val end = cal.toSolar(LunarDate(len.value, m, y, false))
                assertTrue(start is LunarResult.Success && end is LunarResult.Success)
                assertEquals(
                    "độ dài tháng $m/$y",
                    (len.value - 1).toLong(),
                    (end as LunarResult.Success).value.toEpochDay() -
                        (start as LunarResult.Success).value.toEpochDay(),
                )
            }
        }
    }

    @Test
    fun `so thang trong nam am la 12 hoac 13`() {
        for (y in 1902..2099) {
            val n = cal.monthsInLunarYear(y)
            assertTrue("năm $y: $n", n is LunarResult.Success)
            assertTrue("năm $y có ${(n as LunarResult.Success).value} tháng", n.value in 12..13)
            val leap = (cal.leapMonthOf(y) as LunarResult.Success).value
            assertEquals(
                "năm $y: số tháng phải khớp cờ nhuận",
                if (leap is LeapMonthInfo.Month) 13 else 12,
                n.value,
            )
        }
    }
}
