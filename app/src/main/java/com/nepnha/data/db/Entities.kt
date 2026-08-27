package com.nepnha.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room CHỈ lưu dữ liệu do người dùng tạo. Nội dung nghi lễ/văn khấn nằm ở
 * `assets/content` — xem `docs/CONTENT_SCHEMA.md`.
 */
@Entity(tableName = "families")
data class FamilyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
)

/**
 * Thành viên gia đình.
 *
 * Vì sao ngày sinh âm tách thành 4 cột thay vì một chuỗi: một chuỗi `"15/07/1950"`
 * không biểu diễn được **tháng 7 nhuận**. Lưu số nguyên còn cho phép sau này truy
 * vấn thẳng "ai sinh tháng 7 âm".
 *
 * `lunarBirthSource` phân biệt ngày âm **người dùng khai** với ngày âm **máy quy
 * đổi** (sẽ có ở Phase 3). Trộn hai loại này lại là mất dấu vết dữ liệu gốc.
 */
@Entity(
    tableName = "members",
    foreignKeys = [
        ForeignKey(
            entity = FamilyEntity::class,
            parentColumns = ["id"],
            childColumns = ["familyId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("familyId")],
)
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val familyId: Long,
    val fullName: String,
    /** `MALE` / `FEMALE` / `UNSPECIFIED` — lưu dạng tên enum để dump DB đọc được. */
    val gender: String,
    /** ISO `yyyy-MM-dd`. `null` nghĩa là **chưa biết**, không phải một ngày mặc định. */
    val solarBirthDate: String?,
    val lunarBirthDay: Int?,
    val lunarBirthMonth: Int?,
    val lunarBirthYear: Int?,
    val lunarBirthIsLeapMonth: Boolean,
    /** `USER_PROVIDED` khi có ngày âm; `null` khi không có. */
    val lunarBirthSource: String?,
    val role: String?,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

/**
 * Ngày giỗ, khai báo theo **ngày âm gốc**.
 *
 * Cố ý KHÔNG lưu ngày dương: nó đổi mỗi năm và phụ thuộc quy tắc, lưu vào đây là
 * đóng băng một năm cụ thể và mất ý định ban đầu của người dùng.
 *
 * Cũng KHÔNG có cột `isLeapMonth` riêng — tính nhuận đã nằm trong [leapMonthPolicy].
 * Hai nguồn sự thật cho cùng một điều là mở đường cho mâu thuẫn.
 *
 * Policy lưu **theo từng ngày giỗ** chứ không phải cài đặt toàn app: mỗi người mất
 * trong nhà có thể theo một tập quán khác nhau — chốt từ Phase 0.
 */
@Entity(
    tableName = "memorials",
    foreignKeys = [
        ForeignKey(
            entity = FamilyEntity::class,
            parentColumns = ["id"],
            childColumns = ["familyId"],
            onDelete = ForeignKey.CASCADE,
        ),
        // SET_NULL, **không** CASCADE: xoá một thành viên tuyệt đối không được kéo
        // theo ngày giỗ. Liên kết đứt thì quay về tên đã lưu, dữ liệu người dùng
        // nhập không bao giờ biến mất vì một thao tác ở màn hình khác.
        ForeignKey(
            entity = MemberEntity::class,
            parentColumns = ["id"],
            childColumns = ["memberId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("familyId"), Index("memberId")],
)
data class MemorialEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val familyId: Long,
    /**
     * Tên hiển thị khi **không** liên kết thành viên, và là **bản chụp** để quay về
     * khi liên kết bị đứt. Không bao giờ bị xoá.
     */
    val name: String,
    /** Liên kết tuỳ chọn tới một thành viên trong nhà. `null` = nhập tên tự do. */
    val memberId: Long? = null,
    /** 1..30 — giữ nguyên vĩnh viễn, kể cả năm phải lùi về 29. */
    val lunarDay: Int,
    /** 1..12. */
    val lunarMonth: Int,
    /** Tên enum `LeapMonthPolicy`, lưu dạng chữ để dump DB đọc được. */
    val leapMonthPolicy: String,
    /** Tên enum `MissingDayPolicy`. */
    val missingDayPolicy: String,
    val note: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
