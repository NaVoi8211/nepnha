package com.nepnha

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Quét mã nguồn tìm dấu vết của việc sửa file bằng thay-chuỗi hỏng escaping.
 *
 * Bối cảnh: ở Phase 5 đã có sáu chỗ lọt chuỗi `${'$'}` vào mã Kotlin. Trong Kotlin
 * biểu thức đó cho ra **ký tự `$` theo nghĩa đen**, nên `"memorial_dot_${'$'}date"`
 * không nội suy giá trị mà in ra đúng chữ `$date`. Ba trong sáu chỗ đó hiển thị chuỗi
 * thô ra màn hình người dùng, và trình biên dịch không hề báo lỗi.
 *
 * Đây là loại lỗi trình biên dịch im lặng và mắt người dễ bỏ qua, nên để test canh.
 */
class SourceHygieneTest {

    private val mainSources = File("src/main/java")

    /**
     * Sai thì: có chuỗi `${'$'}` sót lại — gần như chắc chắn là một biểu thức nội suy
     * bị hỏng, không phải ý đồ.
     */
    @Test
    fun `khong con dau vet escaping hong trong ma nguon`() {
        val marker = "\${'$'}"
        val offenders = mainSources.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .filter { it.readText().contains(marker) }
            .map { it.relativeTo(mainSources).path }
            .toList()
        assertTrue("còn escaping hỏng ở: $offenders", offenders.isEmpty())
    }

    /** Sai thì: quét nhầm thư mục rỗng và test thành vô nghĩa. */
    @Test
    fun `bo quet thuc su nhin thay ma nguon`() {
        val count = mainSources.walkTopDown().count { it.isFile && it.extension == "kt" }
        assertTrue("chỉ thấy $count file Kotlin — đường dẫn quét sai?", count > 25)
    }
}
