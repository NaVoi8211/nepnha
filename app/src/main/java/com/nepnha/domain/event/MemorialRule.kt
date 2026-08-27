package com.nepnha.domain.event

import java.time.LocalDate

/**
 * Quy tắc nghiệp vụ cho việc quy đổi một ngày giỗ âm lịch ra ngày dương của một năm
 * cụ thể.
 *
 * Đây là **business rule tách rời**, cố ý KHÔNG nằm trong UI và không bị chôn trong
 * một hàm khó sửa. Lý do: tập quán từng gia đình khác nhau, và quy tắc mặc định của
 * MVP có thể phải đổi sau khi có người dùng thật.
 *
 * Hợp đồng khai báo ở Phase 0; `MemorialDateResolver` (Phase 5) hiện thực và **nhận
 * [MemorialRule] làm tham số** chứ không tự quyết.
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
     * MẶC ĐỊNH (chốt Phase 0): luôn dùng **tháng thường**, không tự động nhảy sang
     * tháng nhuận. Đúng cho đại đa số trường hợp.
     */
    COMMON_MONTH_DEFAULT,

    /**
     * Người mất vào **tháng nhuận**, và gia đình vẫn giỗ hằng năm: năm nào có tháng
     * nhuận thì giỗ tháng nhuận, năm không có thì giỗ tháng thường.
     *
     * Đây là lựa chọn dùng được trong thực tế. Tháng nhuận mang một số cụ thể chỉ
     * quay lại sau nhiều chục năm, nên nếu không có phương án lùi về tháng thường
     * thì gia đình sẽ **không có ngày giỗ nào** trong hầu hết các năm.
     */
    LEAP_MONTH_PREFERRED,

    /**
     * Chỉ tính vào **tháng nhuận**, tuyệt đối không lùi về tháng thường. Năm nào
     * không có tháng nhuận mang số đó thì năm ấy **không có ngày giỗ** —
     * `MemorialResolution.Skipped(NO_LEAP_MONTH)`.
     *
     * Dành cho gia đình muốn giữ đúng nguyên tắc; hệ quả là nhiều năm bị bỏ trống,
     * và UI phải nói rõ điều đó trước khi người dùng chọn.
     */
    LEAP_MONTH_ONLY,
}

/**
 * Tháng âm có 29 hoặc 30 ngày. Ngày giỗ mùng 30 sẽ không tồn tại trong những năm
 * mà tháng đó chỉ có 29 ngày.
 */
enum class MissingDayPolicy {
    /**
     * MẶC ĐỊNH (chốt Phase 0): dùng **ngày cuối cùng có thật** của tháng đó (29).
     *
     * Bắt buộc: **không được sửa dữ liệu gốc**. `Memorial.lunarDay` vẫn giữ 30 vĩnh
     * viễn; 29 chỉ là giá trị dẫn xuất khi tính cho một năm cụ thể — xem
     * [ResolvedMemorialDate].
     */
    LAST_VALID_DAY_OF_MONTH,

    /**
     * Năm nào tháng đó không có ngày này thì **bỏ qua năm ấy** —
     * `MemorialResolution.Skipped(MISSING_DAY)`. Không tự lùi ngày.
     */
    SKIP,
}

/**
 * Kết quả quy đổi ngày giỗ cho một năm âm cụ thể.
 *
 * Dùng sealed thay vì nhồi một trường `status` vào [ResolvedMemorialDate]: một năm
 * bị bỏ qua thì **không có** ngày dương, nên để trường đó nullable là mời gọi lỗi.
 * Kiểu ở đây bảo đảm có `Resolved` thì chắc chắn có ngày dương.
 *
 * **Không dùng `null` để che nghiệp vụ, và không dùng exception làm luồng bình thường.**
 */
sealed interface MemorialResolution {

    data class Resolved(val date: ResolvedMemorialDate) : MemorialResolution

    data class Skipped(val lunarYear: Int, val reason: Reason) : MemorialResolution

    enum class Reason {
        /** Tháng đó năm ấy không có ngày này, và policy là [MissingDayPolicy.SKIP]. */
        MISSING_DAY,

        /** Năm ấy không có tháng nhuận mang số này, policy là [LeapMonthPolicy.LEAP_MONTH_ONLY]. */
        NO_LEAP_MONTH,

        /** Ngày rơi ra ngoài 1901–2100. Engine không ngoại suy — xem `docs/LUNAR_API.md`. */
        OUT_OF_SUPPORTED_RANGE,
    }
}

/**
 * Ngày giỗ đã quy đổi xong cho một năm cụ thể.
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
    /** Ngày dương tương ứng — thứ dùng để so với "hôm nay" và để hiện lên lịch. */
    val solarDate: LocalDate,
    /**
     * Tháng âm của năm đó không có ngày người dùng khai (thường là mùng 30 trong
     * tháng thiếu) nên đã dùng ngày cuối tháng, theo
     * [MissingDayPolicy.LAST_VALID_DAY_OF_MONTH].
     */
    val dayWasShortened: Boolean = false,
    /**
     * Người dùng khai ngày thuộc tháng nhuận, nhưng năm này không có tháng nhuận mang
     * số đó nên đã tính vào **tháng thường**, theo
     * [LeapMonthPolicy.LEAP_MONTH_PREFERRED].
     */
    val fellBackToCommonMonth: Boolean = false,
) {
    /**
     * Hai điều chỉnh trên **độc lập nhau và có thể xảy ra cùng lúc** — ví dụ giỗ mùng
     * 30 tháng 7 nhuận, năm nay không có tháng 7 nhuận (lùi tháng thường) mà tháng 7
     * thường lại chỉ có 29 ngày (lùi ngày).
     *
     * Bản đầu dùng một enum `AdjustmentReason` nên nhánh sau **ghi đè** nhánh trước
     * và người dùng chỉ được nghe một nửa sự thật về quyết định mà app tự làm thay họ.
     * Hai cờ riêng để không bao giờ mất thông tin nữa.
     */
    val wasAdjusted: Boolean get() = dayWasShortened || fellBackToCommonMonth
}
