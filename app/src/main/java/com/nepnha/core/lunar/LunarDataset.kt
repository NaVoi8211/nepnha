package com.nepnha.core.lunar

import java.time.LocalDate

/**
 * Dữ liệu thiên văn đã đóng gói sẵn: thời điểm Sóc và thời điểm trung khí.
 *
 * Dataset **chỉ chứa dữ kiện thiên văn**, tuyệt đối không chứa lịch đã tính sẵn —
 * toàn bộ quy tắc lịch nằm trong [VietnameseLunarCalendar]. Tách như vậy để việc kiểm
 * thử không bị vòng tròn: dữ liệu đến từ NASA và ERFA, quy tắc do ta viết, và kỳ vọng
 * trong test đến từ nguồn thứ ba (HKO, văn bản nhà nước).
 *
 * Kotlin thuần: **không** import `android.*`. Việc đọc byte từ assets là của tầng
 * ngoài; ở đây chỉ nhận [ByteArray].
 *
 * Định dạng nhị phân, big-endian, không dấu phẩy động:
 * ```
 * 0   4B   magic "NNLD"
 * 4   u16  version
 * 6   u16  supportedFrom (năm dương)
 * 8   u16  supportedTo
 * 10  u32  newMoonCount
 * 14  u32  principalTermCount
 * 18  u32[] thời điểm Sóc — phút UTC kể từ 1890-01-01T00:00Z
 * ..  u32[] thời điểm trung khí — cùng đơn vị, tăng dần, 12 mốc mỗi chu kỳ từ 0°
 * ```
 *
 * Dataset chứa **đệm** vượt ngoài dải công bố (cần vì tháng 11 âm neo vào Đông chí).
 * Đệm là dữ liệu tính toán nội bộ, **không** mở rộng [supportedYears].
 */
class LunarDataset private constructor(
    val version: Int,
    val supportedYears: IntRange,
    /** Phút UTC kể từ [EPOCH], tăng dần. */
    internal val newMoonMinutes: IntArray,
    /** Phút UTC kể từ [EPOCH], tăng dần; mốc thứ `i` có hoàng kinh `(i % 12) * 30`. */
    internal val principalTermMinutes: IntArray,
) {
    companion object {
        val EPOCH: LocalDate = LocalDate.of(1890, 1, 1)
        private const val MAGIC = 0x4E4E4C44           // "NNLD"
        private const val HEADER_BYTES = 18
        private const val TERMS_PER_CYCLE = 12
        /** Chỉ số của Đông chí (hoàng kinh 270°) trong mỗi chu kỳ 12 trung khí. */
        internal const val WINTER_SOLSTICE_INDEX = 9

        fun parse(bytes: ByteArray): LunarDataset {
            require(bytes.size >= HEADER_BYTES) { "dataset quá ngắn" }
            require(readInt(bytes, 0) == MAGIC) { "magic không phải NNLD" }

            val version = readShort(bytes, 4)
            val from = readShort(bytes, 6)
            val to = readShort(bytes, 8)
            val moonCount = readInt(bytes, 10)
            val termCount = readInt(bytes, 14)

            val expected = HEADER_BYTES + 4 * (moonCount + termCount)
            require(bytes.size == expected) {
                "kích thước sai: mong $expected byte, có ${bytes.size}"
            }
            require(termCount % TERMS_PER_CYCLE == 0) {
                "số trung khí phải chia hết cho $TERMS_PER_CYCLE, có $termCount"
            }

            val moons = IntArray(moonCount) { readInt(bytes, HEADER_BYTES + 4 * it) }
            val termsOffset = HEADER_BYTES + 4 * moonCount
            val terms = IntArray(termCount) { readInt(bytes, termsOffset + 4 * it) }

            requireStrictlyIncreasing(moons, "Sóc")
            requireStrictlyIncreasing(terms, "trung khí")

            return LunarDataset(version, from..to, moons, terms)
        }

        private fun requireStrictlyIncreasing(values: IntArray, label: String) {
            for (i in 1 until values.size) {
                require(values[i] > values[i - 1]) { "$label không tăng đơn điệu tại $i" }
            }
        }

        private fun readInt(b: ByteArray, at: Int): Int =
            (b[at].toInt() and 0xFF shl 24) or
                (b[at + 1].toInt() and 0xFF shl 16) or
                (b[at + 2].toInt() and 0xFF shl 8) or
                (b[at + 3].toInt() and 0xFF)

        private fun readShort(b: ByteArray, at: Int): Int =
            (b[at].toInt() and 0xFF shl 8) or (b[at + 1].toInt() and 0xFF)
    }
}
