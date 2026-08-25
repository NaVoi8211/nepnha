# Lịch âm Việt Nam — chiến lược (chốt Phase 0, thực thi Phase 3)

> **CẬP NHẬT sau Phase 3 Preflight (2026-08-25):** phần "port thuật toán Hồ Ngọc Đức"
> dưới đây **đã bị thay đổi**. Giấy phép source code của tác giả là *personal,
> non-commercial* ⇒ **không dùng code**, chỉ dùng bản mô tả quy tắc rồi tự viết
> Kotlin. Phạm vi bảo đảm rút về **1901–2100** (theo oracle chính thức) thay vì
> 1800–2199. Chi tiết và bằng chứng: **[PHASE_3_PREFLIGHT.md](PHASE_3_PREFLIGHT.md)**.
>
> **CẬP NHẬT Phase 3A (2026-08-25):** Meeus **đã bị loại khỏi kế hoạch** (sách yêu
> cầu xin phép bằng văn bản của Willmann-Bell). Hướng thay thế: dựng lịch từ **dữ
> liệu thiên văn có giấy phép rõ ràng** (NASA/GSFC cho điểm Sóc, HKO cho tiết khí).
> Cổng an toàn thương mại hiện **⛔ BLOCKED** vì chưa có oracle Tier 1 phủ toàn bộ
> lịch âm VN. Xem **[PHASE_3A_ORACLE_GATE.md](PHASE_3A_ORACLE_GATE.md)** và
> **[LUNAR_ORACLE_PROVENANCE.md](LUNAR_ORACLE_PROVENANCE.md)**.

Đây là phần **critical** nhất của app. Sai lịch âm ⇒ sai ngày giỗ ⇒ app vô dụng.

## 1. Vì sao không dùng thư viện có sẵn

Đã tra Maven Central ở Phase 0 với các từ khoá `amlich`, `lunar vietnam`,
`vietnamese calendar`, `lunarcalendar`:

- **Không có artifact nào cho lịch âm Việt Nam.**
- Kết quả duy nhất là `com.xhinliang:LunarCalendar` — **lịch âm Trung Quốc**, dùng
  múi giờ UTC+8. Không dùng được.
- `java.time.chrono` không có Vietnamese chronology.

## 2. Lịch âm VN ≠ lịch âm TQ

Cùng một thuật toán thiên văn (điểm sóc / trung khí), nhưng thời điểm được quy về
**múi giờ địa phương**:

- Việt Nam: **UTC+7**
- Trung Quốc: UTC+8

Hệ quả: có những năm hai lịch **lệch nhau 1 ngày**, hoặc **đặt tháng nhuận vào
tháng khác nhau**. Đây là lý do tuyệt đối không được lấy thư viện lịch TQ dùng thay.

## 3. Phương án

Port thuật toán **"Âm lịch Việt Nam" của Hồ Ngọc Đức** — reference implementation
được hầu hết ứng dụng lịch Việt Nam sử dụng, dựa trên Jean Meeus (*Astronomical
Algorithms*) cho điểm sóc & vị trí Mặt Trời, cộng quy tắc đặt tháng nhuận theo
trung khí, quy về giờ Việt Nam.

Đặt tại `core/lunar`, **Kotlin thuần, không import `android.*`** ⇒ unit test chạy
trên JVM trong vài giây, UI không bao giờ phụ thuộc trực tiếp vào thuật toán.

API dự kiến (sẽ chốt khi implement):

```kotlin
data class LunarDate(
    val day: Int, val month: Int, val year: Int,
    val isLeapMonth: Boolean,
)

interface VietnameseLunarCalendar {
    fun toLunar(solar: LocalDate): LunarDate
    fun toSolar(lunar: LunarDate): LocalDate
    fun canChiOfYear(lunarYear: Int): String   // ví dụ "Bính Ngọ"
    fun isLeapMonth(lunarYear: Int, month: Int): Boolean
}
```

UI và ViewModel chỉ được nói chuyện qua interface này.

## 4. ĐIỂM CHƯA CHẮC CHẮN — phải xác minh đầu Phase 3, không được đoán

1. **Mốc đổi múi giờ.** Việt Nam dùng UTC+8 trước, UTC+7 sau (mốc quanh
   1967–1968). Bản gốc xử lý mốc này thế nào, và có ảnh hưởng tới ngày sinh của
   người cao tuổi trong app không (sinh trước 1968 là hoàn toàn có thật) — **phải
   đọc kỹ implementation gốc rồi mới quyết**, không tự chọn hằng số.
2. **Thuật toán thuần hay bảng tra cứu.** Một số bản của Hồ Ngọc Đức dùng bảng
   precomputed cho khoảng 1800–2199 thay vì tính trực tiếp. Phải quyết định port
   bản nào và ghi rõ **phạm vi năm được bảo đảm đúng**; ngoài phạm vi đó app phải
   báo cho người dùng chứ không im lặng trả kết quả sai.
3. **Giấy phép.** Chưa xác minh điều khoản phân phối của bản gốc. Phải làm rõ và
   ghi credit trong app + header source trước khi merge code Phase 3.
4. **Tháng nhuận và ngày giỗ.** Nếu tháng giỗ rơi vào năm có nhuận đúng tháng đó,
   lấy tháng thường hay tháng nhuận? Đây là **quyết định nghiệp vụ, không phải kỹ
   thuật** → sẽ hỏi chủ dự án ở Phase 7, mặc định đề xuất: dùng **tháng thường**.
5. **Ngày âm không tồn tại.** Tháng âm có 29 hoặc 30 ngày; giỗ ngày 30 sẽ không có
   trong năm mà tháng đó chỉ 29 ngày → phải có quy tắc lùi về ngày 29. Cần chốt.

> Quy tắc làm việc: gặp bất kỳ điểm nào ở trên mà kết quả không khớp lịch chính
> thống, **dừng và báo cáo**, tuyệt đối không chỉnh thuật toán cho khớp test.

## 5. Kế hoạch test (Phase 3 — bắt buộc pass trước khi qua Phase 4)

| Nhóm | Nội dung |
|---|---|
| solar → lunar | Nhiều mốc rải đều nhiều thập kỷ |
| lunar → solar | Round-trip: `toSolar(toLunar(d)) == d` cho toàn bộ ngày trong nhiều năm liên tiếp |
| Tết | Mùng 1 Tết của một dải năm, đối chiếu lịch đã công bố |
| Rằm | Ngày 15 âm của một số tháng |
| Giao thừa | Ngày dương liền trước mùng 1 Tết |
| Tháng nhuận | Các năm nhuận đã biết; xác nhận nhuận đúng tháng |
| Chuyển năm | Ranh giới 31/12 ↔ 01/01 và ranh giới năm âm |
| Khác biệt VN/TQ | Ít nhất một năm mà lịch VN khác lịch TQ — test này chính là bằng chứng ta không dùng nhầm lịch TQ |
| Múi giờ | Kết quả không đổi khi đổi default timezone của JVM (engine phải cố định UTC+7, không đọc timezone máy) |
| Biên | Năm nhỏ nhất / lớn nhất được hỗ trợ; ngoài phạm vi phải ném lỗi rõ ràng |

Nguồn đối chiếu phải là lịch đã công bố công khai. **Không tự bịa số liệu kỳ vọng.**
