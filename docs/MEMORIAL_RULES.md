# Ngày giỗ — business rules (chốt Phase 0, implement Phase 7)

Hai quy tắc dưới đây do chủ dự án chốt. Chúng được khai báo tường minh tại
`domain/event/MemorialRule.kt` và **không** được chôn trong UI hay trong một hàm
khó sửa: `EventCalculator` phải **nhận `MemorialRule` làm tham số**, không tự quyết.

## Quy tắc 1 — Ngày giỗ rơi vào tháng nhuận

Năm âm lịch có thể có cả tháng N thường lẫn tháng N nhuận. Giỗ khai báo
"15 tháng 7" khi đó rơi vào tháng nào?

**Mặc định: `LeapMonthPolicy.COMMON_MONTH_DEFAULT` — luôn dùng tháng THƯỜNG.**
Không tự động nhảy sang tháng nhuận.

Nhưng data model **phải hỗ trợ** gia đình chọn tháng nhuận:

- `LeapMonthPolicy.LEAP_MONTH_PREFERRED` là lựa chọn hợp lệ.
- Lưu **theo từng `Memorial`** (cột `leapMonthPolicy`), không phải cài đặt toàn app
   — mỗi người mất trong nhà có thể theo một tập quán khác nhau.
- Phase 7 chỉ cần lưu và tôn trọng giá trị này; UI cho phép đổi có thể để sau, dữ
   liệu đã sẵn sàng nên không cần migration.

## Quy tắc 2 — Ngày giỗ là mùng 30 nhưng tháng đó chỉ có 29 ngày

**Mặc định: `MissingDayPolicy.LAST_VALID_DAY_OF_MONTH` — dùng ngày cuối cùng có
thật của tháng (29).**

Ràng buộc bắt buộc:

- **Không được sửa dữ liệu gốc.** `Memorial.lunarDay` vẫn giữ `30` vĩnh viễn.
- `29` chỉ là **giá trị dẫn xuất cho một năm cụ thể**, trả về qua
  `ResolvedMemorialDate.effectiveLunarDay`.
- `ResolvedMemorialDate.adjustment == MISSING_DAY_IN_MONTH` ⇒ UI **phải** giải
  thích, không được âm thầm đổi ngày:

  > "Tháng này không có ngày 30 âm lịch. Nếp Nhà đang tính ngày giỗ vào ngày cuối tháng."

## Hợp đồng kiểu dữ liệu

```kotlin
data class MemorialRule(
    val leapMonthPolicy: LeapMonthPolicy = COMMON_MONTH_DEFAULT,
    val missingDayPolicy: MissingDayPolicy = LAST_VALID_DAY_OF_MONTH,
)

data class ResolvedMemorialDate(
    val originalLunarDay: Int,    // người dùng khai báo — bất biến
    val effectiveLunarDay: Int,   // tính cho năm cụ thể
    val lunarMonth: Int,
    val lunarYear: Int,
    val isLeapMonth: Boolean,
    val adjustment: AdjustmentReason,   // NONE | MISSING_DAY_IN_MONTH
)
```

## Test bắt buộc ở Phase 7

| Trường hợp | Kỳ vọng |
|---|---|
| Giỗ 15/7, năm có tháng 7 nhuận, policy mặc định | Rơi vào tháng 7 **thường** |
| Giỗ 15/7, cùng năm đó, policy `LEAP_MONTH_PREFERRED` | Rơi vào tháng 7 **nhuận** |
| Giỗ 30/8, năm tháng 8 có 30 ngày | `effective = 30`, `adjustment = NONE` |
| Giỗ 30/8, năm tháng 8 có 29 ngày | `effective = 29`, `adjustment = MISSING_DAY_IN_MONTH`, `original` vẫn là 30 |
| Đọc lại từ Room sau khi điều chỉnh | `lunarDay` trong DB vẫn là 30 — chứng minh không ghi đè dữ liệu gốc |
| Nhiều năm liên tiếp | Cùng một `Memorial` cho ra ngày dương khác nhau mỗi năm, không tích luỹ sai lệch |
