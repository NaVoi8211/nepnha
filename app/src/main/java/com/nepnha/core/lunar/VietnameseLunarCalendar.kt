package com.nepnha.core.lunar

import java.time.DateTimeException
import java.time.LocalDate

/**
 * Lịch âm Việt Nam, tính từ dữ liệu thiên văn theo quy tắc lịch Việt Nam.
 *
 * **Thuần, tất định, an toàn đa luồng.** Không đọc đồng hồ hệ thống, không đọc múi
 * giờ thiết bị, không đọc locale, không chạm mạng, không chạm database. Cùng đầu vào
 * luôn cho cùng đầu ra, mãi mãi.
 *
 * ### Quy tắc (provenance độc lập với Hồ Ngọc Đức)
 *
 * R1–R5 theo Aslaksen, *The Mathematics of the Chinese Calendar* (NUS) và
 * *Explanatory Supplement to the Astronomical Almanac*:
 *
 * 1. Ngày chứa thời điểm Sóc là **mùng 1**; ngày được lấy trọn, bất kể Sóc rơi vào
 *    giờ nào.
 * 2. Mỗi điểm Sóc mở một tháng âm.
 * 3. **Đông chí luôn nằm trong tháng 11.**
 * 4. Giữa hai tháng 11 liên tiếp mà có 13 tháng thì đó là năm nhuận; tháng nhuận là
 *    tháng **đầu tiên không chứa trung khí**, và nó mang **số của tháng liền trước**.
 * 5. Trung khí là các thời điểm hoàng kinh Mặt Trời ở bội số 30°.
 *
 * R6 — khác biệt duy nhất so với lịch Trung Quốc: Việt Nam quy chiếu theo **kinh
 * tuyến 105°Đ (UTC+7)**, theo Quyết định 121-CP điều 1. Trung Quốc dùng 120°Đ.
 *
 * ### Nguồn dữ liệu
 * Thời điểm Sóc từ catalog NASA/GSFC; thời điểm trung khí sinh bằng ERFA ở máy dev.
 * Cả hai đã đóng gói sẵn trong [LunarDataset]. **Không** có tính toán thiên văn lúc
 * chạy, **không** có thư viện native, **không** có mạng.
 */
interface VietnameseLunarCalendar {

    /** Dải năm **dương** được bảo đảm. Ngoài dải này mọi hàm trả [LunarError.UnsupportedYear]. */
    val supportedYears: IntRange

    fun toLunar(
        solar: LocalDate,
        context: CalendarContext = CalendarContext.OfficialVietnam,
    ): LunarResult<LunarDate>

    fun toSolar(
        lunar: LunarDate,
        context: CalendarContext = CalendarContext.OfficialVietnam,
    ): LunarResult<LocalDate>

    /** Số ngày của một tháng âm: 29 hoặc 30. */
    fun daysInLunarMonth(
        year: Int,
        month: Int,
        isLeapMonth: Boolean = false,
        context: CalendarContext = CalendarContext.OfficialVietnam,
    ): LunarResult<Int>

    fun monthsInLunarYear(
        lunarYear: Int,
        context: CalendarContext = CalendarContext.OfficialVietnam,
    ): LunarResult<Int>

    fun leapMonthOf(
        lunarYear: Int,
        context: CalendarContext = CalendarContext.OfficialVietnam,
    ): LunarResult<LeapMonthInfo>

    /** Can Chi của **năm âm**, ví dụ Bính Ngọ. */
    fun sexagenaryYear(lunarYear: Int): LunarResult<SexagenaryYear>

    companion object {
        /**
         * Dựng engine từ dataset. Việc dựng chỉ số nội bộ làm **một lần** ở đây, nên
         * mọi lời gọi sau đó là tra cứu thuần và an toàn đa luồng.
         */
        fun create(dataset: LunarDataset): VietnameseLunarCalendar =
            DatasetLunarCalendar(dataset)
    }
}

private const val VN_OFFSET_MINUTES = 7 * 60          // R6: UTC+7
private const val MINUTES_PER_DAY = 24 * 60

private val CAN = listOf("Giáp", "Ất", "Bính", "Đinh", "Mậu", "Kỷ", "Canh", "Tân", "Nhâm", "Quý")
private val CHI = listOf("Tý", "Sửu", "Dần", "Mão", "Thìn", "Tỵ", "Ngọ", "Mùi", "Thân", "Dậu", "Tuất", "Hợi")

/**
 * Hiện thực dựa trên bảng thời điểm đóng gói sẵn.
 *
 * Toàn bộ trạng thái được tính trong khối `init` rồi **không bao giờ thay đổi** ⇒
 * an toàn đa luồng mà không cần khoá.
 */
private class DatasetLunarCalendar(private val data: LunarDataset) : VietnameseLunarCalendar {

    override val supportedYears: IntRange = data.supportedYears

    /** Số thứ tự ngày (kể từ [LunarDataset.EPOCH]) của mùng 1 mỗi tháng âm, tăng dần. */
    private val monthStartDay: IntArray

    /** Song song với [monthStartDay]: số tháng, cờ nhuận, năm âm. */
    private val monthNumber: IntArray
    private val monthIsLeap: BooleanArray
    private val monthLunarYear: IntArray

    /** Chỉ số tháng đầu tiên và cuối cùng đã được gán nhãn. */
    private val firstLabelled: Int
    private val lastLabelled: Int

    init {
        monthStartDay = data.newMoonMinutes
            .map { toLocalDayIndex(it) }
            .distinct()
            .toIntArray()

        val termDay = IntArray(data.principalTermMinutes.size) {
            toLocalDayIndex(data.principalTermMinutes[it])
        }

        monthNumber = IntArray(monthStartDay.size) { -1 }
        monthIsLeap = BooleanArray(monthStartDay.size)
        monthLunarYear = IntArray(monthStartDay.size)

        // R3 — tháng chứa Đông chí là tháng 11. Chỉ xét các Đông chí nằm trọn trong
        // dải Sóc đã có; dataset cố ý sinh dư ở hai đầu nên có mốc rơi ra ngoài.
        val month11 = ArrayList<Int>()
        for (i in termDay.indices) {
            if (i % 12 != LunarDataset.WINTER_SOLSTICE_INDEX) continue
            val d = termDay[i]
            if (d < monthStartDay.first() || d >= monthStartDay.last()) continue
            val m = monthIndexOf(d)
            if (month11.isEmpty() || month11.last() != m) month11.add(m)
        }

        for (k in 0 until month11.size - 1) {
            val a = month11[k]
            val b = month11[k + 1]

            // R4 — 13 tháng giữa hai tháng 11 ⇒ năm nhuận. Tháng nhuận là tháng ĐẦU
            // TIÊN không chứa trung khí nào.
            var leapAt = -1
            if (b - a == 13) {
                for (m in a + 1 until b) {
                    if (!hasPrincipalTerm(m, termDay)) { leapAt = m; break }
                }
                if (leapAt < 0) leapAt = b - 1     // phòng hờ, không nên xảy ra
            }

            var number = 11
            var lunarYear = dateOf(monthStartDay[a]).year
            var prevNumber = -1
            var prevYear = -1
            for (m in a until b) {
                if (m == leapAt && prevNumber > 0) {
                    // Tháng nhuận mang SỐ CỦA THÁNG LIỀN TRƯỚC, không phải số kế tiếp.
                    monthNumber[m] = prevNumber
                    monthIsLeap[m] = true
                    monthLunarYear[m] = prevYear
                } else {
                    monthNumber[m] = number
                    monthIsLeap[m] = false
                    monthLunarYear[m] = lunarYear
                    prevNumber = number
                    prevYear = lunarYear
                    number += 1
                    if (number == 13) { number = 1; lunarYear += 1 }
                }
            }
        }

        firstLabelled = monthNumber.indexOfFirst { it > 0 }
        lastLabelled = monthNumber.indexOfLast { it > 0 }
    }

    // ---------- API ----------

    override fun toLunar(solar: LocalDate, context: CalendarContext): LunarResult<LunarDate> {
        supportedOrNull(solar.year)?.let { return it }
        val day = dayIndexOf(solar)
        val m = monthIndexOf(day)
        if (m < firstLabelled || m > lastLabelled || monthNumber[m] < 0) {
            return LunarResult.Failure(
                LunarError.UnsupportedYear(solar.year, supportedYears),
            )
        }
        return LunarResult.Success(
            LunarDate(
                day = day - monthStartDay[m] + 1,
                month = monthNumber[m],
                year = monthLunarYear[m],
                isLeapMonth = monthIsLeap[m],
            ),
        )
    }

    override fun toSolar(lunar: LunarDate, context: CalendarContext): LunarResult<LocalDate> {
        validateShape(lunar)?.let { return it }
        val m = findMonth(lunar.year, lunar.month, lunar.isLeapMonth)
            ?: return LunarResult.Failure(
                if (lunar.isLeapMonth) {
                    LunarError.NoSuchLeapMonth(lunar.year, lunar.month)
                } else {
                    LunarError.UnsupportedYear(lunar.year, supportedYears)
                },
            )
        val length = monthStartDay[m + 1] - monthStartDay[m]
        if (lunar.day > length) {
            return LunarResult.Failure(LunarError.NonexistentLunarDate(lunar, length))
        }
        val result = dateOf(monthStartDay[m] + lunar.day - 1)
        supportedOrNull(result.year)?.let { return it }
        return LunarResult.Success(result)
    }

    override fun daysInLunarMonth(
        year: Int,
        month: Int,
        isLeapMonth: Boolean,
        context: CalendarContext,
    ): LunarResult<Int> {
        if (month !in 1..12) {
            return LunarResult.Failure(
                LunarError.InvalidLunarDate(
                    LunarDate(1, month, year, isLeapMonth),
                    LunarError.InvalidLunarDate.Reason.MONTH_OUT_OF_RANGE,
                ),
            )
        }
        val m = findMonth(year, month, isLeapMonth)
            ?: return LunarResult.Failure(
                if (isLeapMonth) {
                    LunarError.NoSuchLeapMonth(year, month)
                } else {
                    LunarError.UnsupportedYear(year, supportedYears)
                },
            )
        return LunarResult.Success(monthStartDay[m + 1] - monthStartDay[m])
    }

    override fun monthsInLunarYear(lunarYear: Int, context: CalendarContext): LunarResult<Int> {
        val n = (firstLabelled..lastLabelled).count { monthLunarYear[it] == lunarYear && monthNumber[it] > 0 }
        return if (n == 0) {
            LunarResult.Failure(LunarError.UnsupportedYear(lunarYear, supportedYears))
        } else {
            LunarResult.Success(n)
        }
    }

    override fun leapMonthOf(lunarYear: Int, context: CalendarContext): LunarResult<LeapMonthInfo> {
        var found = false
        for (m in firstLabelled..lastLabelled) {
            if (monthNumber[m] < 0 || monthLunarYear[m] != lunarYear) continue
            found = true
            if (monthIsLeap[m]) return LunarResult.Success(LeapMonthInfo.Month(monthNumber[m]))
        }
        return if (found) {
            LunarResult.Success(LeapMonthInfo.None)
        } else {
            LunarResult.Failure(LunarError.UnsupportedYear(lunarYear, supportedYears))
        }
    }

    override fun sexagenaryYear(lunarYear: Int): LunarResult<SexagenaryYear> {
        // Can Chi tính theo NĂM ÂM, không phải năm dương: một ngày dương trước Tết
        // vẫn thuộc can chi của năm âm trước.
        if (monthsInLunarYear(lunarYear) is LunarResult.Failure) {
            return LunarResult.Failure(LunarError.UnsupportedYear(lunarYear, supportedYears))
        }
        return LunarResult.Success(
            SexagenaryYear(
                can = CAN[Math.floorMod(lunarYear + 6, 10)],
                chi = CHI[Math.floorMod(lunarYear + 8, 12)],
            ),
        )
    }

    // ---------- nội bộ ----------

    private fun supportedOrNull(gregorianYear: Int): LunarResult.Failure? =
        if (gregorianYear in supportedYears) {
            null
        } else {
            LunarResult.Failure(LunarError.UnsupportedYear(gregorianYear, supportedYears))
        }

    private fun validateShape(l: LunarDate): LunarResult.Failure? = when {
        l.month !in 1..12 -> LunarResult.Failure(
            LunarError.InvalidLunarDate(l, LunarError.InvalidLunarDate.Reason.MONTH_OUT_OF_RANGE),
        )
        l.day !in 1..30 -> LunarResult.Failure(
            LunarError.InvalidLunarDate(l, LunarError.InvalidLunarDate.Reason.DAY_OUT_OF_RANGE),
        )
        else -> null
    }

    private fun findMonth(year: Int, month: Int, isLeap: Boolean): Int? {
        for (m in firstLabelled..lastLabelled) {
            if (monthNumber[m] == month && monthIsLeap[m] == isLeap && monthLunarYear[m] == year) {
                return if (m + 1 < monthStartDay.size) m else null
            }
        }
        return null
    }

    private fun hasPrincipalTerm(monthIndex: Int, termDay: IntArray): Boolean {
        val lo = monthStartDay[monthIndex]
        val hi = monthStartDay[monthIndex + 1]
        var i = termDay.binarySearch(lo)
        if (i < 0) i = -i - 1
        return i < termDay.size && termDay[i] < hi
    }

    /** Chỉ số tháng chứa ngày [day]; -1 nếu trước mốc Sóc đầu tiên. */
    private fun monthIndexOf(day: Int): Int {
        var lo = 0
        var hi = monthStartDay.size - 1
        if (day < monthStartDay[0]) return -1
        while (lo < hi) {
            val mid = (lo + hi + 1) / 2
            if (monthStartDay[mid] <= day) lo = mid else hi = mid - 1
        }
        return lo
    }

    /** Phút UTC → số thứ tự ngày theo giờ Việt Nam. */
    private fun toLocalDayIndex(utcMinutes: Int): Int =
        Math.floorDiv(utcMinutes + VN_OFFSET_MINUTES, MINUTES_PER_DAY)

    private fun dayIndexOf(date: LocalDate): Int =
        (date.toEpochDay() - LunarDataset.EPOCH.toEpochDay()).toInt()

    private fun dateOf(dayIndex: Int): LocalDate =
        LunarDataset.EPOCH.plusDays(dayIndex.toLong())
}

/** Tiện ích: dựng [LocalDate] và trả lỗi có tên thay vì ném exception. */
fun gregorianDateOrError(year: Int, month: Int, day: Int): LunarResult<LocalDate> = try {
    LunarResult.Success(LocalDate.of(year, month, day))
} catch (_: DateTimeException) {
    LunarResult.Failure(LunarError.InvalidGregorianDate(year, month, day))
}
