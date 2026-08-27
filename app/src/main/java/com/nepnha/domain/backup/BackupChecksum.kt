package com.nepnha.domain.backup

import java.security.MessageDigest

/**
 * Checksum của một bản sao lưu.
 *
 * PHẠM VI — nói cho đúng, đừng hứa quá:
 *  · ✅ bắt được **hỏng dữ liệu ngoài ý muốn**: một chữ số bị lật trong `lunarDay` mà
 *    JSON vẫn phân tích được thì checksum không khớp và app từ chối nhập;
 *  · ❌ **không phải mã hoá** — file vẫn đọc được bằng mắt thường;
 *  · ❌ **không chống sửa có chủ ý** — ai sửa file cũng tính lại được.
 *
 * Tính trên một **chuỗi chuẩn hoá dựng từ giá trị nghiệp vụ**, không phải trên văn bản
 * JSON thô: nhờ vậy nó không phụ thuộc thứ tự khoá, khoảng trắng hay cách thư viện JSON
 * xuống dòng. Cùng một hàm dùng cho cả xuất lẫn nhập.
 */
object BackupChecksum {

    private const val SEP = ""
    private const val ROW = ""

    fun of(data: BackupData): String {
        val sb = StringBuilder()
        sb.append(data.familyName.orEmpty()).append(SEP)
        sb.append(data.primaryMemberRef?.toString().orEmpty()).append(ROW)
        // KHÔNG sắp xếp lại ở đây: checksum phải tính trên đúng thứ tự đang có trong
        // file, nếu không thì hai bên tính ra hai kết quả khác nhau.
        //
        // Lưu ý cho người đọc sau: thứ tự này KHÔNG quyết định thứ tự hiển thị sau khi
        // nhập. Thành viên giữ thứ tự chèn (`ORDER BY createdAt, id`) nhưng ngày giỗ
        // được sắp lại theo `lunarMonth, lunarDay` — nên bản xuất lần hai có thể xếp
        // ngày giỗ khác bản gốc trong khi dữ liệu vẫn y hệt.
        for (m in data.members) {
            sb.append(m.ref).append(SEP)
                .append(m.fullName).append(SEP)
                .append(BackupFormat.wire(m.gender)).append(SEP)
                .append(m.solarBirthDate.orEmpty()).append(SEP)
                .append(m.lunarBirthDate?.let { "${it.day}/${it.month}/${it.year}/${it.leapMonth}" }.orEmpty())
                .append(SEP)
                .append(m.role.orEmpty()).append(SEP)
                .append(m.note.orEmpty()).append(ROW)
        }
        for (x in data.memorials) {
            sb.append(x.name).append(SEP)
                .append(x.memberRef?.toString().orEmpty()).append(SEP)
                .append(x.lunarDay).append(SEP)
                .append(x.lunarMonth).append(SEP)
                .append(BackupFormat.wire(x.leapMonthPolicy)).append(SEP)
                .append(BackupFormat.wire(x.missingDayPolicy)).append(SEP)
                .append(x.note.orEmpty()).append(ROW)
        }
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(sb.toString().toByteArray(Charsets.UTF_8))
        return "sha256:" + digest.joinToString("") { "%02x".format(it) }
    }
}
