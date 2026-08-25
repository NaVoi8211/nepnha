package com.nepnha.core.lunar

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Dataset phải nguyên vẹn trước khi bất kỳ test nào khác có ý nghĩa. */
class LunarDatasetTest {

    @Test
    fun `checksum cua dataset khong doi`() {
        // Nếu test này đỏ: dataset đã bị đổi. Chỉ cập nhật hằng số khi việc đổi là
        // CÓ CHỦ ĐÍCH và đã chạy lại tools/verify_lunar_dataset.py.
        assertEquals(
            LunarTestSupport.DATASET_SHA256,
            LunarTestSupport.sha256(LunarTestSupport.datasetBytes),
        )
    }

    @Test
    fun `header dung dinh dang`() {
        val d = LunarDataset.parse(LunarTestSupport.datasetBytes)
        assertEquals(1, d.version)
        assertEquals(1901..2100, d.supportedYears)
    }

    @Test
    fun `tu choi du lieu hong`() {
        val good = LunarTestSupport.datasetBytes
        // magic sai
        val badMagic = good.copyOf().also { it[0] = 'X'.code.toByte() }
        assertTrue(runCatching { LunarDataset.parse(badMagic) }.isFailure)
        // cắt cụt
        assertTrue(runCatching { LunarDataset.parse(good.copyOf(good.size - 4)) }.isFailure)
        // rỗng
        assertTrue(runCatching { LunarDataset.parse(ByteArray(0)) }.isFailure)
    }
}
