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
