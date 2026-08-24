package com.nepnha.domain.event

/**
 * Quy tắc nghiệp vụ cho việc quy đổi một ngày giỗ âm lịch ra ngày dương của một năm
 * cụ thể.
 *
 * Đây là **business rule tách rời**, cố ý KHÔNG nằm trong UI và không bị chôn trong
 * một hàm khó sửa. Lý do: tập quán từng gia đình khác nhau, và quy tắc mặc định của
 * MVP có thể phải đổi sau khi có người dùng thật.
 *
 * Phase 0 chỉ KHAI BÁO hợp đồng. Logic tính toán thuộc `EventCalculator` (Phase 7),
 * và nó phải nhận [MemorialRule] làm tham số chứ không tự quyết.
 *
 * Xem `docs/MEMORIAL_RULES.md`.
 */
data class MemorialRule(
    val leapMonthPolicy: LeapMonthPolicy = LeapMonthPolicy.COMMON_MONTH_DEFAULT,
    val missingDayPolicy: MissingDayPolicy = MissingDayPolicy.LAST_VALID_DAY_OF_MONTH,
) {
    companion object {
        /** Quy tắc mặc định của MVP, đã được chủ dự án chốt ở Phase 0. */
        val DEFAULT = MemorialRule()
    }
}

/**
 * Năm âm lịch có thể có cả tháng N thường lẫn tháng N nhuận. Ngày giỗ khai báo
 * "15 tháng 7" khi đó rơi vào tháng nào?
 */
enum class LeapMonthPolicy {
    /**
     * MẶC ĐỊNH (đã chốt): luôn dùng **tháng thường**, không tự động nhảy sang tháng
     * nhuận.
     */
    COMMON_MONTH_DEFAULT,

    /**
     * Gia đình chọn giỗ theo **tháng nhuận**. Lựa chọn này lưu theo từng
     * `Memorial`, không phải cài đặt toàn app — mỗi người mất có thể một khác.
     */
    LEAP_MONTH_PREFERRED,
}

/**
 * Tháng âm có 29 hoặc 30 ngày. Ngày giỗ mùng 30 sẽ không tồn tại trong những năm
 * mà tháng đó chỉ có 29 ngày.
 */
enum class MissingDayPolicy {
    /**
     * MẶC ĐỊNH (đã chốt): dùng **ngày cuối cùng có thật** của tháng đó (29).
     *
     * Bắt buộc: **không được sửa dữ liệu gốc**. `Memorial.lunarDay` vẫn giữ 30 vĩnh
     * viễn; 29 chỉ là giá trị dẫn xuất khi tính cho một năm cụ thể — xem
     * [ResolvedMemorialDate].
     */
    LAST_VALID_DAY_OF_MONTH,
}

/**
 * Kết quả quy đổi ngày giỗ cho một năm cụ thể.
 *
 * Kiểu này tồn tại để phân biệt rạch ròi **dữ liệu người dùng khai báo** với **giá
 * trị tính ra**, và để UI có đủ thông tin báo cho người dùng biết đã có điều chỉnh
 * thay vì âm thầm đổi ngày.
 */
data class ResolvedMemorialDate(
    /** Ngày âm người dùng đã khai báo. Không bao giờ bị ghi đè. */
    val originalLunarDay: Int,
    /** Ngày âm thực sự dùng cho năm này. Bằng [originalLunarDay] khi không phải điều chỉnh. */
    val effectiveLunarDay: Int,
    val lunarMonth: Int,
    val lunarYear: Int,
    val isLeapMonth: Boolean,
    /** Vì sao phải điều chỉnh — UI dựa vào đây để giải thích cho người dùng. */
    val adjustment: AdjustmentReason,
) {
    val wasAdjusted: Boolean get() = adjustment != AdjustmentReason.NONE

    enum class AdjustmentReason {
        NONE,

        /**
         * Tháng âm của năm đó không có ngày mà người dùng khai báo (thường là mùng
         * 30 trong tháng thiếu). UI hiển thị đại ý:
         * "Tháng này không có ngày 30 âm lịch. Nếp Nhà đang tính ngày giỗ vào ngày cuối tháng."
         */
        MISSING_DAY_IN_MONTH,
    }
}
