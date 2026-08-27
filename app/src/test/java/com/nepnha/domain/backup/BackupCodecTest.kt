package com.nepnha.domain.backup

import com.nepnha.domain.event.LeapMonthPolicy
import com.nepnha.domain.event.MissingDayPolicy
import com.nepnha.domain.model.Gender
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Đọc/ghi file sao lưu — phủ toàn bộ danh sách kiểm tra ở hợp đồng
 * `docs/PHASE_7_EXPORT_IMPORT.md`.
 *
 * File sao lưu nằm trên đĩa của người dùng và phải sống lâu hơn nhiều phiên bản app.
 * Mọi test ở đây bảo vệ một lời hứa cụ thể về nó.
 */
class BackupCodecTest {

    private fun sample() = BackupData(
        familyName = "Gia đình tôi",
        primaryMemberRef = 1,
        members = listOf(
            BackupMember(
                ref = 1, fullName = "Nguyễn Văn A", gender = Gender.MALE,
                solarBirthDate = "1950-03-14",
                lunarBirthDate = BackupLunarBirth(26, 1, 1950, false),
                role = "Trưởng nam", note = null,
            ),
            BackupMember(
                ref = 2, fullName = "Trần Thị B", gender = Gender.FEMALE,
                solarBirthDate = null, lunarBirthDate = null, role = null, note = "ghi chú",
            ),
        ),
        memorials = listOf(
            BackupMemorial(
                name = "Cụ ông", memberRef = 1, lunarDay = 30, lunarMonth = 7,
                leapMonthPolicy = LeapMonthPolicy.LEAP_MONTH_ONLY,
                missingDayPolicy = MissingDayPolicy.SKIP, note = null,
            ),
            BackupMemorial(
                name = "Cụ bà không liên kết", memberRef = null, lunarDay = 5, lunarMonth = 5,
                leapMonthPolicy = LeapMonthPolicy.COMMON_MONTH_DEFAULT,
                missingDayPolicy = MissingDayPolicy.LAST_VALID_DAY_OF_MONTH, note = null,
            ),
        ),
    )

    private fun encoded() = BackupCodec.encode(sample(), "2026-08-27T10:00:00Z", "0.1.0-mvp")

    private fun valid(text: String) = (BackupCodec.decode(text) as BackupResult.Valid).file
    private fun errors(text: String) = (BackupCodec.decode(text) as BackupResult.Invalid).errors

    // ---------------------------------------------------------------- vòng tròn

    /**
     * Ghi ra rồi đọc lại phải được **đúng** dữ liệu ban đầu.
     *
     * Sai thì: file sao lưu vô dụng — cái người dùng khôi phục không phải cái họ đã lưu.
     */
    @Test
    fun `ghi ra roi doc lai duoc dung du lieu ban dau`() {
        val back = valid(encoded()).data
        assertEquals(sample(), back)
    }

    /**
     * Sai thì: một bản build sau đọc file cũ mà không biết nó thuộc phiên bản nào.
     */
    @Test
    fun `file mang du sieu du lieu`() {
        val f = valid(encoded())
        assertEquals(BackupFormat.SUPPORTED_VERSION, f.formatVersion)
        assertEquals("2026-08-27T10:00:00Z", f.exportedAt)
        assertEquals("0.1.0-mvp", f.appVersionName)
    }

    /**
     * Giá trị enum trên file phải là **hằng chuỗi tường minh**, không phải tên ký hiệu
     * trong mã Kotlin.
     *
     * Sai thì: R8 đổi tên hằng (Phase 6 đã xảy ra) hoặc ai đó đổi tên enum, và mọi file
     * sao lưu cũ của người dùng thành không đọc được.
     */
    @Test
    fun `enum tren file la hang chuoi co dinh khong phai ten ky hieu`() {
        val text = encoded()
        assertTrue("phải dùng hằng wire", text.contains("\"leap_month_only\""))
        assertTrue(text.contains("\"last_valid_day\""))
        assertTrue(text.contains("\"male\""))
        assertTrue("không được rò tên ký hiệu ra file", !text.contains("LEAP_MONTH_ONLY"))
        assertTrue(!text.contains("LAST_VALID_DAY_OF_MONTH"))
        assertTrue(!text.contains("\"MALE\""))
    }

    // ---------------------------------------------------------------- file hỏng

    /** Sai thì: chọn nhầm một file rỗng và app im lặng hoặc sập. */
    @Test
    fun `file rong bi tu choi`() {
        assertEquals(listOf(BackupError.EmptyFile), errors(""))
        assertEquals(listOf(BackupError.EmptyFile), errors("   \n  "))
    }

    /** Sai thì: chọn nhầm một tấm ảnh và app sập thay vì báo lỗi. */
    @Test
    fun `khong phai JSON bi tu choi`() {
        assertEquals(listOf(BackupError.NotJson), errors("đây không phải json"))
        assertEquals(listOf(BackupError.NotJson), errors("{ hỏng"))
        assertEquals(listOf(BackupError.NotJson), errors("[1,2,3]"))
    }

    /** Sai thì: app đoán bừa phiên bản và diễn giải sai dữ liệu. */
    @Test
    fun `thieu formatVersion bi tu choi`() {
        assertEquals(
            listOf(BackupError.MissingFormatVersion),
            errors("""{"data":{"members":[],"memorials":[]}}"""),
        )
    }

    /**
     * Sai thì: bản app cũ đọc file của bản mới, hiểu sai, và ghi vào database một cách
     * âm thầm sai.
     */
    @Test
    fun `formatVersion tuong lai bi tu choi ro rang`() {
        val e = errors("""{"formatVersion":99,"data":{"members":[],"memorials":[]}}""").single()
        assertEquals(BackupError.UnsupportedFormatVersion(99, 1), e)
    }

    /**
     * Trường lạ trong một phiên bản đã hỗ trợ phải được **bỏ qua**, không phải lỗi.
     *
     * Sai thì: bản app cũ không đọc nổi file do bản mới ghi ra, dù dữ liệu vẫn tương thích.
     */
    @Test
    fun `truong la duoc bo qua chu khong lam hong`() {
        val text = encoded()
            .replace("\"familyName\"", "\"tuongLaiChuaBiet\": 123,\n      \"familyName\"")
        val back = valid(text).data
        assertEquals("Gia đình tôi", back.familyName)
        assertEquals(2, back.members.size)
    }

    // ---------------------------------------------------------------- kiểm tra giá trị

    private fun withMemorial(fragment: String) = """
        {"formatVersion":1,"data":{"members":[],"memorials":[$fragment]}}
    """.trimIndent()

    /** Sai thì: ngày âm 45 lọt vào database và mọi phép quy đổi sau đó vô nghĩa. */
    @Test
    fun `ngay va thang am ngoai khoang bi tu choi`() {
        val e = errors(
            withMemorial(
                """{"name":"x","lunarDay":45,"lunarMonth":13,
                   "leapMonthPolicy":"common_month","missingDayPolicy":"skip"}""",
            ),
        )
        assertTrue(e.contains(BackupError.OutOfRange("memorials[0].lunarDay", 45, 1, 30)))
        assertTrue(e.contains(BackupError.OutOfRange("memorials[0].lunarMonth", 13, 1, 12)))
    }

    /** Sai thì: một policy lạ âm thầm biến thành mặc định và người dùng mất lựa chọn. */
    @Test
    fun `policy khong hop le bi tu choi chu khong ve mac dinh`() {
        val e = errors(
            withMemorial(
                """{"name":"x","lunarDay":1,"lunarMonth":1,
                   "leapMonthPolicy":"khong_ton_tai","missingDayPolicy":"skip"}""",
            ),
        )
        assertTrue(e.any { it is BackupError.BadEnum && it.where == "memorials[0].leapMonthPolicy" })
    }

    /** Sai thì: ngày giỗ không tên lọt vào danh sách. */
    @Test
    fun `truong bat buoc bi thieu thi bao loi kem vi tri`() {
        val e = errors(
            withMemorial("""{"lunarDay":1,"lunarMonth":1,"leapMonthPolicy":"common_month","missingDayPolicy":"skip"}"""),
        )
        assertEquals(listOf(BackupError.MissingField("memorials[0].name")), e)
    }

    /** Sai thì: kiểu sai làm app sập thay vì báo lỗi. */
    @Test
    fun `sai kieu du lieu bi bao loi chu khong sap`() {
        val e = errors("""{"formatVersion":1,"data":{"members":["chuỗi chứ không phải object"],"memorials":[]}}""")
        assertEquals(listOf(BackupError.WrongType("members[0]", "object")), e)
    }

    /** Sai thì: hai thành viên cùng `ref` làm quan hệ ngày giỗ trỏ lung tung. */
    @Test
    fun `ref trung lap trong file bi tu choi`() {
        val e = errors(
            """{"formatVersion":1,"data":{"members":[
               {"ref":1,"fullName":"A","gender":"male"},
               {"ref":1,"fullName":"B","gender":"male"}],"memorials":[]}}""",
        )
        assertTrue(e.contains(BackupError.DuplicateRef(1)))
    }

    /**
     * Sai thì: nhập xong có ngày giỗ trỏ tới một thành viên không tồn tại — khoá ngoại
     * hỏng hoặc liên kết mất im lặng.
     */
    @Test
    fun `memberRef tro toi thanh vien khong co bi tu choi`() {
        val e = errors(
            """{"formatVersion":1,"data":{"members":[{"ref":1,"fullName":"A","gender":"male"}],
               "memorials":[{"name":"x","memberRef":99,"lunarDay":1,"lunarMonth":1,
               "leapMonthPolicy":"common_month","missingDayPolicy":"skip"}]}}""",
        )
        assertTrue(e.contains(BackupError.DanglingReference("memorials[0].memberRef", 99)))
    }

    /** Sai thì: tín chủ trỏ vào hư không sau khi nhập. */
    @Test
    fun `primaryMemberRef tro sai bi tu choi`() {
        val e = errors(
            """{"formatVersion":1,"data":{"primaryMemberRef":7,
               "members":[{"ref":1,"fullName":"A","gender":"male"}],"memorials":[]}}""",
        )
        assertTrue(e.contains(BackupError.DanglingReference("primaryMemberRef", 7)))
    }

    /** Sai thì: ngày sinh "1950-13-45" lọt vào và hỏng lúc hiển thị. */
    @Test
    fun `ngay sinh duong khong co that bi tu choi`() {
        val e = errors(
            """{"formatVersion":1,"data":{"members":[
               {"ref":1,"fullName":"A","gender":"male","solarBirthDate":"1950-02-30"}],"memorials":[]}}""",
        )
        assertTrue(e.any { it is BackupError.BadDate })
    }

    /** Sai thì: một chuỗi vài megabyte lọt vào database và làm hỏng giao diện. */
    @Test
    fun `chuoi qua dai bi tu choi`() {
        val long = "x".repeat(BackupFormat.MAX_NAME_LENGTH + 1)
        val e = errors(
            """{"formatVersion":1,"data":{"members":[
               {"ref":1,"fullName":"$long","gender":"male"}],"memorials":[]}}""",
        )
        assertTrue(e.any { it is BackupError.TooLong })
    }

    /** Sai thì: người dùng phải sửa file từng lỗi một, mỗi vòng một lần thử. */
    @Test
    fun `nhieu loi duoc gom lai va bao cung luc`() {
        val e = errors(
            """{"formatVersion":1,"data":{"members":[
               {"ref":1,"fullName":"A","gender":"khong_biet"},
               {"ref":2,"fullName":"","gender":"male"}],
               "memorials":[{"name":"x","lunarDay":99,"lunarMonth":99,
               "leapMonthPolicy":"common_month","missingDayPolicy":"skip"}]}}""",
        )
        assertTrue("phải có nhiều hơn một lỗi, thấy $e", e.size >= 3)
    }

    // ---------------------------------------------------------------- checksum

    /**
     * Một chữ số bị lật mà JSON vẫn phân tích được thì checksum phải bắt.
     *
     * Sai thì: file hỏng âm thầm và ngày giỗ được khôi phục vào sai ngày — thứ tệ nhất
     * có thể xảy ra với một app ngày giỗ.
     */
    @Test
    fun `checksum bat duoc mot chu so bi lat`() {
        val corrupted = encoded().replace("\"lunarDay\": 30", "\"lunarDay\": 20")
        assertTrue("phải thực sự đổi được nội dung", corrupted != encoded())
        assertEquals(listOf(BackupError.ChecksumMismatch), errors(corrupted))
    }

    /** Sai thì: checksum khác nhau giữa hai lần chạy và mọi file đều bị coi là hỏng. */
    @Test
    fun `checksum on dinh giua cac lan tinh`() {
        assertEquals(BackupChecksum.of(sample()), BackupChecksum.of(sample()))
    }

    /**
     * File không có checksum vẫn đọc được — trường này để bảo vệ, không phải để bắt buộc.
     *
     * Sai thì: file do người dùng sửa tay hoặc do công cụ khác tạo bị từ chối vô cớ.
     */
    @Test
    fun `file khong co checksum van doc duoc`() {
        val f = valid(
            """{"formatVersion":1,"data":{"members":[{"ref":1,"fullName":"A","gender":"male"}],
               "memorials":[]}}""",
        )
        assertEquals(1, f.data.members.size)
        assertNull(f.data.familyName)
    }

    /** Sai thì: file lớn hợp lý làm app sập hoặc treo. */
    @Test
    fun `file lon hop ly van doc duoc`() {
        val big = BackupData(
            familyName = "Nhà lớn",
            primaryMemberRef = null,
            members = (1..300).map {
                BackupMember(it, "Thành viên $it", Gender.UNSPECIFIED, null, null, null, null)
            },
            memorials = (1..300).map {
                BackupMemorial(
                    "Giỗ $it", it, (it % 30) + 1, (it % 12) + 1,
                    LeapMonthPolicy.COMMON_MONTH_DEFAULT, MissingDayPolicy.LAST_VALID_DAY_OF_MONTH, null,
                )
            },
        )
        val back = valid(BackupCodec.encode(big, "2026-01-01T00:00:00Z", null)).data
        assertEquals(300, back.members.size)
        assertEquals(300, back.memorials.size)
        assertEquals(big, back)
    }
}
