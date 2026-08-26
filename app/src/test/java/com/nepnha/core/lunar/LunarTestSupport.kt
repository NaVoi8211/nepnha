package com.nepnha.core.lunar

import java.io.File

/**
 * Tiện ích dùng chung cho test lịch âm.
 *
 * Test JVM đọc thẳng file asset từ đĩa. Test trên thiết bị đọc qua `AssetManager` —
 * xem `androidTest/.../LunarAssetTest.kt`. Cả hai phải cho cùng kết quả.
 */
object LunarTestSupport {

    /** SHA-256 của dataset đã đóng băng. Đổi dataset mà quên cập nhật ⇒ test đỏ. */
    const val DATASET_SHA256 = "b9f9613a0d1974ac82a024a737b8b40cbd1588869881db13c90f4c90a020f33d"

    val datasetBytes: ByteArray by lazy {
        File("src/main/assets/lunar/vn_lunar_v1.bin").readBytes()
    }

    val calendar: VietnameseLunarCalendar by lazy {
        VietnameseLunarCalendar.create(LunarDataset.parse(datasetBytes))
    }

    fun sha256(bytes: ByteArray): String =
        java.security.MessageDigest.getInstance("SHA-256")
            .digest(bytes)
            .joinToString("") { "%02x".format(it) }

    /** Mỗi dòng: gregorian, day, month, year, isLeap. */
    data class FixtureRow(
        val gregorian: java.time.LocalDate,
        val lunar: LunarDate,
    )

    val fixture: List<FixtureRow> by lazy {
        File("src/test/resources/lunar_fixture_v1.tsv").readLines()
            .filter { it.isNotBlank() && !it.startsWith("#") }
            .map { line ->
                val p = line.split("\t")
                FixtureRow(
                    gregorian = java.time.LocalDate.parse(p[0]),
                    lunar = LunarDate(p[1].toInt(), p[2].toInt(), p[3].toInt(), p[4] == "1"),
                )
            }
    }

    fun lunarOf(y: Int, m: Int, d: Int): LunarDate {
        val r = calendar.toLunar(java.time.LocalDate.of(y, m, d))
        return (r as LunarResult.Success).value
    }
}
