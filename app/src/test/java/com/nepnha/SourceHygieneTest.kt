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
    private val mainRes = File("src/main/res")

    private fun sources(): List<File> =
        (mainSources.walkTopDown().filter { it.isFile && it.extension == "kt" } +
            mainRes.walkTopDown().filter { it.isFile && it.extension == "xml" }).toList()

    /**
     * Dấu vết bị cấm trong mã nguồn, kèm lý do. Mỗi mục là một loại lỗi mà trình biên
     * dịch **không** bắt được.
     */
    private val forbidden = listOf(
        "\${'$'}" to
            "escaping hỏng: trong Kotlin biểu thức này cho ra ký tự \$ theo nghĩa đen, " +
            "nên chuỗi nội suy im lặng biến thành chữ thô",
        "<<<<<<<" to "dấu xung đột merge còn sót",
        ">>>>>>>" to "dấu xung đột merge còn sót",
        "=======\n" to "dấu xung đột merge còn sót",
        "TODO()" to "TODO() ném NotImplementedError khi chạy — không được có trên đường sản xuất",
        "printStackTrace()" to "nuốt lỗi ra logcat thay vì xử lý",
        "System.out.print" to "in ra stdout trong mã sản xuất",
    )

    /**
     * Sai thì: một dấu vết ở trên lọt vào bản phát hành. Loại lỗi này trình biên dịch
     * im lặng và mắt người dễ bỏ qua — Phase 5 đã để lọt sáu chỗ `\${'$'}`, ba trong
     * số đó hiển thị chuỗi thô ra màn hình người dùng.
     */
    @Test
    fun `khong con dau vet escaping hong trong ma nguon`() {
        val all = sources()
        val offenders = buildList {
            for ((marker, why) in forbidden) {
                for (f in all) {
                    if (f.readText().contains(marker)) add("${f.name}: $marker ($why)")
                }
            }
        }
        assertTrue("dấu vết bị cấm còn sót:\n" + offenders.joinToString("\n"), offenders.isEmpty())
    }

    /**
     * Mọi chuỗi có tham số trong `strings.xml` phải dùng chỉ số tường minh (`%1${'$'}s`).
     *
     * Sai thì: `%s` không chỉ số sẽ ném `IllegalFormatException` trên máy đặt ngôn ngữ
     * khác khi thứ tự tham số đổi — lỗi chỉ xuất hiện ở một số máy.
     */
    @Test
    fun `chuoi co tham so dung chi so tuong minh`() {
        val strings = File(mainRes, "values/strings.xml").readText()
        val bad = Regex("%[sd]").findAll(strings).map { it.value }.toList()
        assertTrue("có tham số không chỉ số trong strings.xml: $bad", bad.isEmpty())
    }

    /** Sai thì: quét nhầm thư mục rỗng và test thành vô nghĩa. */
    @Test
    fun `bo quet thuc su nhin thay ma nguon`() {
        val kt = mainSources.walkTopDown().count { it.isFile && it.extension == "kt" }
        val xml = mainRes.walkTopDown().count { it.isFile && it.extension == "xml" }
        assertTrue("chỉ thấy $kt file Kotlin — đường dẫn quét sai?", kt > 25)
        assertTrue("chỉ thấy $xml file XML — đường dẫn quét sai?", xml > 5)
    }

    /**
     * Bộ quy tắc phải thực sự bắt được lỗi, không phải một cái sàng thủng.
     *
     * Sai thì: test luôn xanh vì marker viết sai và chẳng bao giờ khớp gì.
     */
    @Test
    fun `bo quy tac bat duoc mau lỗi that`() {
        val sample = "val x = \"memorial_dot_\${'$'}{'\$'}date\""
        assertTrue(
            "marker escaping không khớp được mẫu lỗi thật",
            forbidden.any { (m, _) -> sample.contains(m) },
        )
    }
}
