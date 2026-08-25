# API lịch âm Việt Nam — hợp đồng đã đóng băng

> `com.nepnha.core.lunar` · Kotlin thuần · **không** import `android.*`.

## Nguyên tắc

**Thuần · tất định · an toàn đa luồng.** Không đọc đồng hồ hệ thống, múi giờ thiết bị,
locale, mạng hay database. Cùng đầu vào luôn cho cùng đầu ra.

Toàn bộ chỉ số nội bộ dựng **một lần** lúc khởi tạo rồi bất biến ⇒ không cần khoá.

## Kiểu dữ liệu

```kotlin
data class LunarDate(day: Int, month: Int, year: Int, isLeapMonth: Boolean = false)
data class SexagenaryYear(can: String, chi: String)          // "Bính Ngọ"
sealed interface LeapMonthInfo { None; Month(month: Int) }
sealed interface CalendarContext { OfficialVietnam }
```

`isLeapMonth` là **thuộc tính của `LunarDate`**, không phải tham số rời ở API chuyển
đổi — không thể lỡ quên ở một lời gọi nào đó.

## Hàm

```kotlin
val supportedYears: IntRange                                  // 1901..2100 (năm DƯƠNG)

fun toLunar(solar: LocalDate, context = OfficialVietnam): LunarResult<LunarDate>
fun toSolar(lunar: LunarDate, context = OfficialVietnam): LunarResult<LocalDate>
fun daysInLunarMonth(year, month, isLeapMonth = false, context = …): LunarResult<Int>
fun monthsInLunarYear(lunarYear, context = …): LunarResult<Int>
fun leapMonthOf(lunarYear, context = …): LunarResult<LeapMonthInfo>
fun sexagenaryYear(lunarYear): LunarResult<SexagenaryYear>
```

Khởi tạo: `VietnameseLunarCalendar.create(LunarDataset.parse(bytes))`.
Việc đọc byte từ assets thuộc tầng ngoài — `core/lunar` không biết Android tồn tại.

## Mô hình lỗi

**Không dùng `null` để che nghiệp vụ.**

```kotlin
sealed interface LunarResult<out T> { Success(value); Failure(error) }

sealed interface LunarError {
    UnsupportedYear(year, supported)
    InvalidGregorianDate(year, month, day)
    InvalidLunarDate(date, reason)              // DAY_OUT_OF_RANGE | MONTH_OUT_OF_RANGE
    NonexistentLunarDate(date, lastValidDay)
    NoSuchLeapMonth(year, month)
}
```

| Tình huống | Kết quả |
|---|---|
| Năm dương ngoài 1901–2100 | `UnsupportedYear` — **không ngoại suy** |
| Năm âm không có tháng nhuận | `Success(LeapMonthInfo.None)` — **thành công**, không phải lỗi |
| Hỏi tháng nhuận không tồn tại | `NoSuchLeapMonth` |
| Mùng 30 ở tháng thiếu | `NonexistentLunarDate(lastValidDay = 29)` |

**`NonexistentLunarDate` mang sẵn `lastValidDay`** để `EventCalculator` áp
`MemorialRule.missingDayPolicy` mà không phải hỏi lại engine.
**Engine không bao giờ tự lùi 30 → 29** — đó là quy tắc ngày giỗ, tầng khác.

## Biên phạm vi

Phạm vi công bố tính theo **ngày dương**: 1901-01-01 … 2100-12-31.

**Nhãn năm âm được phép vượt biên.** `toLunar(1901-01-01)` trả `11/11/1900` — đó là
ngày âm đúng. Cấm nó sẽ khiến 20 ngày đầu 1901, vốn nằm trong phạm vi công bố, không
biểu diễn được.

## Bối cảnh lịch

MVP chỉ có `CalendarContext.OfficialVietnam` = **UTC+7 cho toàn dải**.

> ⚠️ Đây **không** phải tuyên bố rằng kết quả phản ánh đúng tập quán lịch đương thời
> của mọi thời kỳ, đặc biệt miền Bắc 1954–1967.

`HistoricalRegion.NORTH/SOUTH`: **roadmap**, chưa có API — không hứa suông.

## Không có trong API

`InsufficientPrecision` — cố ý **không** thêm. Sáu tháng sát ranh giới ngày được ghi
nhận trong metadata của dataset, không lộ ra API, không dùng để đổi kết quả. Xem
[LUNAR_DATASET_PROVENANCE.md](LUNAR_DATASET_PROVENANCE.md).
