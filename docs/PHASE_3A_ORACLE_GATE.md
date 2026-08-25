# Phase 3A — Oracle & Provenance Gate

> **KHÔNG CÓ PRODUCTION CODE.** Không engine, không đổi Room schema, không nối UI,
> không thêm dependency. Nghiên cứu 2026-08-25.
>
> Mục tiêu: trả lời dứt điểm **"có được phép viết engine chưa"** — và nếu chưa thì
> thiếu chính xác cái gì.

**KẾT QUẢ CỔNG: ⛔ BLOCKED.** Chi tiết ở §G.
>
> **Cập nhật sau Phase 3A.3 (2026-08-25):** đã **biên dịch ERFA và benchmark thật**.
> Kỹ thuật đạt yêu cầu (Sóc: trung vị 18,9 s so NASA trên 2.474 mốc; tiết khí: max
> 33 s so HKO trên 72 mốc; chỉ 0,24% ca có rủi ro đổi ngày). **G16 → PASS.** Nhưng
> G14 vẫn PARTIAL vì `eraMoon98` là implementation **thuật toán Meeus** — vấn đề
> chính sách cần chủ dự án quyết. Xem
> **[PHASE_3A3_ASTRONOMICAL_BENCHMARK.md](PHASE_3A3_ASTRONOMICAL_BENCHMARK.md)** ·
> **[ASTRONOMICAL_BACKEND_DECISION.md](ASTRONOMICAL_BACKEND_DECISION.md)**.
>
> **Cập nhật sau Phase 3A.2 (2026-08-25):** G14 chuyển từ BLOCKED sang PARTIAL nhờ
> tìm được **ERFA** — bản phái sinh SOFA dưới **BSD-3-Clause**, đã đọc được chính văn
> giấy phép (bản thân SOFA thì trang giấy phép trả 404). Đồng thời phát sinh **G16**:
> ΔT cho 1901–1960. Chi tiết:
> **[PHASE_3A2_ASTRONOMICAL_PREFLIGHT.md](PHASE_3A2_ASTRONOMICAL_PREFLIGHT.md)** ·
> **[ASTRONOMICAL_PROVENANCE.md](ASTRONOMICAL_PROVENANCE.md)** ·
> **[HISTORICAL_TIME_MODEL.md](HISTORICAL_TIME_MODEL.md)**.
>
> **Cập nhật sau Phase 3A.1 (2026-08-25):** đã kiểm chứng dữ liệu thật của NASA và
> HKO. Kết quả: mở được một phần G8, nhưng **phát hiện thêm blocker mới** —
> HKO không có giờ tiết khí cho 1901–2100, làm "Hướng 1 thuần dữ liệu" không khả
> thi. Bảng gate cập nhật ở §G. Chi tiết:
> **[PHASE_3A1_DATASET_VERIFICATION.md](PHASE_3A1_DATASET_VERIFICATION.md)**.

---

## A. Provenance audit

| # | Nguồn | Loại | Thương mại? | License | Derivative của HND? | Dùng để làm gì | Attribution | Tin cậy |
|---|---|---|---|---|---|---|---|---|
| A1 | **Hồ Ngọc Đức — `amlich.js` v0.5 (2004)** | source code | ⛔ **KHÔNG** | `personal, non-commercial use` (nguyên văn §A.1) | gốc | **KHÔNG DÙNG** | — | High (license rõ) |
| A2 | **HND — `amlich-aa98.js`** | source code | ⛔ **CHƯA XÁC MINH** | không tìm được file gốc | gốc | **KHÔNG DÙNG** | — | — |
| A3 | **HND — bản mô tả quy tắc** (`calrules_en.html`) | mathematical rule | 🟡 quy tắc là phương pháp; trang không có tuyên bố bản quyền | không nêu | gốc | **Đọc hiểu 5 quy tắc** rồi tự viết Kotlin | Có — ghi công tác giả | High |
| A4 | `baolanlequang/VietnameseLunar-android` | source code | ⛔ | tự khai `MIT © 2022 Lan Le` | **Có** | **KHÔNG DÙNG** | — | Low |
| A5 | `teddyvn/VietnameseCalendar` | source code | ⛔ | tự khai `MIT © 2022 Phuoc Nguyen` | **Có** | **KHÔNG DÙNG** | — | Low |
| A6 | `nhatanh2996/LunarCalendar4J` | source code | ⛔ | tự khai `MIT © 2020 nhatanh2996` | **Có** | **KHÔNG DÙNG** | — | Low |
| A7 | `vanng822/amlich` | source code | ⛔ | **không có LICENSE** | **Có** | **KHÔNG DÙNG** | — | Low |
| A8 | **Trần Tiến Bình, _Lịch VN thế kỷ XX–XXI (1901–2100)_**, NXB Văn hoá – Thông tin 2005 | calendar table | 🟡 đọc để **đối chiếu**, không sao chép bảng | sách có bản quyền | Không | **Oracle Tier 1 cho lịch VN** | Trích dẫn thư mục | **High** |
| A9 | **Văn bản nhà nước công bố nghỉ Tết** (vd Thông báo 9441/TB-BNV) | historical / official | ✅ văn bản pháp quy | — | Không | **Oracle Tier 1 cho ngày Tết** | Ghi số hiệu văn bản | **High** |
| A10 | **HKO — bảng đối chiếu Dương–Âm 1901–2100** (data.gov.hk) | calendar table | ✅ **CÓ** | data.gov.hk TOU: *"browse, download, distribute, reproduce … for both **commercial** and non-commercial purposes on a free-of-charge basis"* | Không | **Oracle cho phía Trung Quốc** trong test VN≠TQ | **Bắt buộc** | **High** |
| A11 | **HKO — 24 tiết khí** | astronomical | ✅ (cùng TOU nếu qua data.gov.hk) | như trên | Không | Thời điểm **trung khí** | **Bắt buộc** | **High** |
| A12 | **NASA/GSFC — Six Millennium Catalog of the Phases of the Moon** (Espenak) | astronomical | ✅ **CÓ** | *"Permission is freely granted to reproduce this data when accompanied by an acknowledgment"* | Không | Thời điểm **Sóc** | **Bắt buộc**: `Moon Phase Predictions by Fred Espenak, NASA/GSFC` | **High** |
| A13 | **Jean Meeus, _Astronomical Algorithms_** (Willmann-Bell) | astronomical | ⛔ **BLOCKED** | *"no part … may be reproduced … without the written permission of the publisher"* | Không | **LOẠI KHỎI KẾ HOẠCH** | — | High (license rõ, và rõ là không dùng được) |
| A14 | Reingold & Dershowitz, *Calendrical Calculations* | source code | ⛔ | cần **xin phép riêng** (chính HND phải cảm ơn vì đã được cho phép) | Không | **KHÔNG DÙNG** | — | High |
| A15 | **USNO — Computing Approximate Solar Coordinates** | astronomical | 🟡 **CHƯA XÁC MINH** | tác phẩm cơ quan liên bang Hoa Kỳ ⇒ *thường* là public domain, **nhưng tôi chưa đọc được trang** (DNS `aa.usno.navy.mil` không phân giải được từ máy này) | Không | *Có thể* dùng cho hoàng kinh Mặt Trời — **chưa được duyệt** | Chưa rõ | Medium |
| A16 | Wikipedia — *Time in Vietnam* | historical | ✅ CC BY-SA | CC BY-SA | Không | Lịch sử múi giờ | Có | Medium |
| A17 | Wikipedia — bảng ngày Tết | test data | ✅ CC BY-SA | CC BY-SA | ⚠️ **không rõ** — bảng không ghi nguồn tính | Sanity check | Có | 🟡 **Medium** (đã hạ cấp, xem §D) |
| A18 | Hànộimới / Tuổi Trẻ / Thanh Niên | historical | báo chí | — | Không | Ca đặc biệt 1984/85/87, 2006–2008, mốc 121/CP | Trích dẫn | Medium |

### A.1 Nguyên văn license của HND (bằng chứng cốt lõi)

```
Copyright 2004 Ho Ngoc Duc. All Rights Reserved.
Permission to use, copy, modify, and redistribute this software and its
documentation for personal, non-commercial use is hereby granted provided that
this copyright notice appears in all copies.
```

### A.2 Nguyên văn license của Meeus

> "no part of the book may be reproduced by any mechanical, photographic, or
> electronic process, nor may it be stored in any information retrieval system,
> transmitted, or otherwise copied for public or private use, without the written
> permission of the publisher."

⇒ Muốn dùng phải **xin phép bằng văn bản** Willmann-Bell. Chưa xin thì **BLOCKED**.
Theo quyết định số 10 của chủ dự án, Meeus **bị loại khỏi kế hoạch implementation**.

> ⚠️ Tôi **không** kết luận "công thức toán học thì đương nhiên dùng được". Đó chính
> là kiểu suy luận bị cấm.

---

## B. Oracle strategy — hệ thống phân tầng

> **Agreement with multiple websites does not imply independent correctness if they
> share the same implementation.**

| Tier | Định nghĩa | Nguồn cụ thể | Được dùng làm gì |
|---|---|---|---|
| **1** | Cơ quan lịch chính thức / văn bản nhà nước / đài thiên văn quốc gia. Tự tính, không phụ thuộc code bên thứ ba | A8 Trần Tiến Bình · A9 văn bản nghỉ Tết · A10–A11 HKO · A12 NASA | **Expected value trong test** |
| **2** | Nguồn độc lập nhưng **chưa chứng minh được provenance** | A17 Wikipedia · A18 báo chí | Đối chiếu phụ. **Không** được là oracle duy nhất |
| **3** | Website/app phổ thông không rõ implementation | các web lịch vạn niên VN | **Chỉ sanity check.** Không vào test |
| **4** | HND và mọi thứ phái sinh | A1–A7 | **Chỉ đọc hiểu quy tắc** (A3). Tuyệt đối không đưa source hay data vào app |

**Quy tắc đối chiếu chéo:** hai nguồn chỉ tính là độc lập khi thuộc **hai nhánh khác
nhau** trong đồ thị ở [LUNAR_ORACLE_PROVENANCE.md](LUNAR_ORACLE_PROVENANCE.md).

---

## C. Test vector specification

Đặc tả đầy đủ 7 nhóm nằm ở [LUNAR_TEST_VECTORS.md](LUNAR_TEST_VECTORS.md).

**Quy tắc tuyệt đối:** không bao giờ sinh expected value bằng cách chạy chính
implementation mà ta đang định viết. Test kiểu đó chỉ chứng minh code nhất quán với
chính nó.

Tình trạng lấp đầy hiện tại:

| Nhóm | Yêu cầu | Trạng thái |
|---|---|---|
| C1 | Dương→Âm: Tết, Rằm Giêng, Đoan Ngọ, Rằm 7, Trung Thu, cuối/đầu năm âm | 🟡 chỉ có Tết |
| C2 | Âm→Dương: ngày 1, 15, 29, 30 (tháng đủ), tháng thiếu, tháng nhuận | ⛔ **trống** |
| C3 | Tháng nhuận nhiều năm, nhiều chu kỳ | ⛔ **chưa đạt chuẩn nguồn** |
| C4 | VN≠TQ: 1984, 1985, 1987, 2006, 2007, 2008, 2030, 2053 | 🟡 có ngày, **thiếu phía TQ từ HKO** |
| C5 | Múi giờ lịch sử 1967–68, Tết Mậu Thân, Bắc và Nam | 🟡 có ngày, thiếu nguồn Tier 1 |
| C6 | Biên 1901, 1902, 2099, 2100, 1900→lỗi, 2101→lỗi | ⛔ **trống** |
| C7 | Input không hợp lệ | ✅ đặc tả xong (không cần oracle ngoài) |

---

## D. Independence analysis

Toàn bộ ở [LUNAR_ORACLE_PROVENANCE.md](LUNAR_ORACLE_PROVENANCE.md), gồm đồ thị phụ
thuộc và bảng phân tích từng nguồn.

**Một kết quả của phân tích này là tôi phải tự hạ cấp một kết luận cũ.** Ở Phase 3
Preflight tôi đánh dấu Tết 2030 là "✅ đã đối chiếu chéo" vì Wikipedia và HND cùng
cho 02/02/2030. Kiểm lại: **bảng ngày Tết trên Wikipedia không ghi nguồn tính**. Nếu
nó được soạn từ một web lịch VN chạy code HND thì "hai nguồn" thực chất là một.
⇒ Hạ xuống 🟡.

---

## E. Historical timezone model

### E.1 Dữ liệu lịch sử (nguồn A16 + A18)

| Giai đoạn | Vùng áp dụng | Offset |
|---|---|---|
| trước 1906-07-01 | Đông Dương | UTC+07:06:40 |
| 1906-07-01 → 1911-04-30 | Đông Dương | UTC+07:06:30 |
| 1911-05-01 → 1942-12-30 | Đông Dương | UTC+07:00 |
| 1942-12-31 → 1945-03-13 | Đông Dương | UTC+08:00 |
| 1945-03-14 → 1945-09-01 | Đông Dương | UTC+09:00 |
| 1945-09-02 → 1947-03-31 | Bắc | UTC+07:00 |
| 1947-04-01 → Geneva 1954 | Bắc | không thống nhất (+7 / +8 tuỳ vùng) |
| Geneva 1954 → 1955-06-30 | Nam | UTC+08:00 |
| 1955-07-01 → 1959-12-31 | Nam | UTC+07:00 |
| 1960-01-01 → 1975-06-12 | Nam | UTC+08:00 |
| **1968-01-01** → 1975-06-12 | Bắc | UTC+07:00 |
| 1975-06-13 → nay | Toàn quốc | UTC+07:00 |

Mốc 1968: **Quyết định 121/CP**, Thủ tướng Phạm Văn Đồng **ký 8/8/1967**, **hiệu lực
từ 1968** (nguồn A18 — Hànộimới dẫn Ban Lịch Nhà nước). Đây là lời giải cho mâu
thuẫn "1967 hay 1968" giữa hai nguồn: một bên ghi ngày ký, bên kia ghi ngày hiệu lực.

> ⚠️ **CHƯA XÁC MINH:** giai đoạn **1954 → 1967 ở miền Bắc** dùng offset nào. Bảng
> Wikipedia không nêu; HND ghi miền Bắc dùng GMT+8 "trước 7/8/1967". Cần chính văn
> Quyết định 121/CP hoặc sách Trần Tiến Bình. **Không suy đoán.**

### E.2 Domain model đề xuất

```kotlin
package com.nepnha.core.lunar

/**
 * Bối cảnh lịch: quyết định lịch âm được tính theo múi giờ nào.
 * KHÔNG phải tuỳ chọn kỹ thuật cho caller — là một sự thật lịch sử.
 */
sealed interface CalendarContext {

    /** Mặc định: lịch chính thức của nhà nước Việt Nam theo từng thời kỳ. */
    data object OfficialVietnam : CalendarContext

    /** Chỉ có ý nghĩa cho ngày trong 1954–1975. */
    data class HistoricalRegion(val region: VietnamRegion) : CalendarContext
}

enum class VietnamRegion { NORTH, SOUTH }

internal object VietnamTimeZone {
    /** Offset (giây) đang có hiệu lực tại thời điểm đó, theo [context]. */
    fun offsetSecondsAt(utcInstant: Long, context: CalendarContext): Int
}
```

Ba tính chất bắt buộc:

1. **Caller không truyền `TimeZone`.** API chỉ nhận `CalendarContext` — một khái
   niệm lịch sử, không phải tham số kỹ thuật. Loại bỏ khả năng gọi sai.
2. **Engine không đọc múi giờ máy.** `TimeZone.getDefault()` và `ZoneId.systemDefault()`
   bị cấm trong `core/lunar`, có test canh giữ.
3. **`CalendarContext` mặc định là `OfficialVietnam`**, phủ 100% nhu cầu MVP.
   `HistoricalRegion` tồn tại để domain **biểu diễn được** khác biệt Bắc/Nam mà
   **chưa cần** dùng tới.

### E.3 Room schema — chưa đổi

`CalendarContext` **không** cần cột mới ở giai đoạn này: MVP dùng `OfficialVietnam`
cho mọi tính toán. Chỉ khi chủ dự án chọn phương án B (cho người dùng chọn Bắc/Nam)
mới cần thêm cột, và khi đó phải có migration riêng, review riêng.

**Kết luận: KHÔNG migration ở Phase 3A.** Đúng yêu cầu mục 9 và 24.

---

## F. API review

### F.1 Rà soát API đã đề xuất ở Phase 3 Preflight

| Hạng mục | Đánh giá | Thay đổi |
|---|---|---|
| `toLunar` / `toSolar` | Tên rõ, đối xứng | Giữ |
| `daysInLunarMonth` | Cần cho `missingDayPolicy` | Giữ |
| `leapMonthOf` | Trả `Int?` — **null nhập nhằng**: "năm thường" hay "không tính được"? | **Sửa**: trả `LunarResult<LeapMonthInfo>` với `LeapMonthInfo.None` / `LeapMonthInfo.Month(n)` |
| `sexagenaryYear(Int): String` | Trả `String` khiến caller phải parse; không type-safe | **Sửa**: trả `SexagenaryYear(can, chi)`, có `toString()` cho hiển thị |
| Thiếu | Không có cách hỏi "năm âm này có bao nhiêu tháng" | **Thêm** `monthsInLunarYear` |
| Historical context | Chưa có | **Thêm** tham số `context: CalendarContext = OfficialVietnam` |
| Thread safety | Chưa nêu | **Thêm ràng buộc**: toàn bộ hàm thuần, không state khả biến ⇒ an toàn đa luồng |
| Determinism | Chưa nêu | **Thêm ràng buộc**: không đọc đồng hồ, không đọc locale, không đọc timezone máy ⇒ cùng input luôn cùng output, mãi mãi |

### F.2 API sau khi sửa

```kotlin
data class LunarDate(
    val day: Int,
    val month: Int,
    val year: Int,
    val isLeapMonth: Boolean = false,
)

data class SexagenaryYear(val can: String, val chi: String) {
    override fun toString() = "$can $chi"          // "Bính Ngọ"
}

sealed interface LeapMonthInfo {
    data object None : LeapMonthInfo
    data class Month(val month: Int) : LeapMonthInfo
}

/**
 * Thuần, tất định, an toàn đa luồng.
 * Không đọc đồng hồ hệ thống, không đọc locale, không đọc timezone của máy.
 */
interface VietnameseLunarCalendar {

    val supportedYears: IntRange        // 1901..2100

    fun toLunar(solar: LocalDate, context: CalendarContext = OfficialVietnam): LunarResult<LunarDate>

    fun toSolar(lunar: LunarDate, context: CalendarContext = OfficialVietnam): LunarResult<LocalDate>

    fun daysInLunarMonth(year: Int, month: Int, isLeapMonth: Boolean,
                         context: CalendarContext = OfficialVietnam): LunarResult<Int>

    fun leapMonthOf(lunarYear: Int, context: CalendarContext = OfficialVietnam): LunarResult<LeapMonthInfo>

    fun monthsInLunarYear(lunarYear: Int, context: CalendarContext = OfficialVietnam): LunarResult<Int>

    fun sexagenaryYear(lunarYear: Int): LunarResult<SexagenaryYear>
}

sealed interface LunarResult<out T> {
    data class Success<T>(val value: T) : LunarResult<T>
    data class Failure(val error: LunarError) : LunarResult<Nothing>
}

sealed interface LunarError {
    data class UnsupportedYear(val year: Int, val supported: IntRange) : LunarError
    data class InvalidGregorianDate(val year: Int, val month: Int, val day: Int) : LunarError
    data class InvalidLunarDate(val date: LunarDate, val reason: Reason) : LunarError {
        enum class Reason { DAY_OUT_OF_RANGE, MONTH_OUT_OF_RANGE }
    }
    data class NonexistentLunarDate(val date: LunarDate, val lastValidDay: Int) : LunarError
    data class NoSuchLeapMonth(val year: Int, val month: Int) : LunarError
    data class AmbiguousHistoricalRegion(val year: Int) : LunarError
}
```

### F.3 `null` bị cấm ở đâu

| Tình huống | ⛔ Không được | ✅ Phải là |
|---|---|---|
| Năm ngoài phạm vi | `null` | `UnsupportedYear` |
| Ngày dương không hợp lệ | `null` | `InvalidGregorianDate` |
| Ngày âm sai khoảng | `null` | `InvalidLunarDate` |
| Ngày âm không tồn tại trong năm đó | `null` | `NonexistentLunarDate(lastValidDay)` |
| Tháng nhuận không tồn tại | `null` | `NoSuchLeapMonth` |
| Năm thường (không có tháng nhuận) | `null` | `LeapMonthInfo.None` — **đây là kết quả thành công**, không phải lỗi |

`NonexistentLunarDate` mang sẵn `lastValidDay` để `EventCalculator` áp
`missingDayPolicy` mà không phải hỏi lại engine. Engine **không tự sửa 30→29** —
ranh giới này đã chốt ở [MEMORIAL_RULES.md](MEMORIAL_RULES.md).

### F.4 Boundary behavior

| Input | Kết quả |
|---|---|
| 1901-01-01 … 2100-12-31 | Tính bình thường |
| ≤ 1900-12-31, ≥ 2101-01-01 | `UnsupportedYear` |
| Ngày âm thuộc năm âm 1900 (rơi đầu 1901 dương) | **CHƯA QUYẾT** — cần bảng biên ở nhóm C6 rồi mới định nghĩa |
| Ngày trong 1954–1975 với `OfficialVietnam` | Dùng múi giờ nhà nước thời kỳ đó |
| Ngày trong 1954–1975 với `HistoricalRegion` | Dùng múi giờ của miền đó |

---

## G. Commercial safety gate

| # | Tiêu chí | Trạng thái | Ghi chú |
|---|---|---|---|
| G1 | Không dùng HND source code | ✅ **PASS** | Đã loại, có bằng chứng license |
| G2 | Không dùng HND-derived source code | ✅ **PASS** | 4 repo đã nhận diện và loại |
| G3 | Không dùng HND-derived test data thiếu provenance | ✅ **PASS** | Web lịch VN xếp Tier 3; Wikipedia đã hạ xuống 🟡 |
| G4 | Phạm vi 1901–2100 đã xác định | ✅ **PASS** | Khớp phạm vi oracle A8 và A10 |
| G5 | **Có oracle độc lập đủ mạnh** | ⛔ **BLOCKED** | Tier 1 cho lịch VN đầy đủ = sách Trần Tiến Bình, **chưa có trong tay**. Văn bản nghỉ Tết chỉ phủ quanh Tết |
| G6 | Test vector cho tháng nhuận | ⛔ **BLOCKED** | Chỉ có nguồn phổ thông, chưa đạt Tier 1 |
| G7 | Test vector cho 29/30 ngày | ⛔ **BLOCKED** | **Chưa có nguồn nào** |
| G8 | Test vector cho VN ≠ TQ | 🟡 **MỘT PHẦN — tiến bộ** | **1985 đã PASS** với đối chiếu chéo thật (VN nhánh A vs HKO nhánh B, đã tải `T1985e.txt`). 7 năm còn lại có phía TQ, thiếu phía VN Tier 1 |
| G9 | Test vector cho múi giờ lịch sử | ⛔ **BLOCKED (xấu đi)** | Phase 3A.1 phát hiện **mâu thuẫn trực tiếp** về miền Bắc 1954–1967 giữa Ban Lịch Nhà nước và HND. Trước đây tưởng chỉ thiếu dữ liệu; nay là hai nguồn nói ngược nhau |
| **G14** | **Nguồn công thức hoàng kinh Mặt Trời có provenance rõ** | 🟡 **MỘT PHẦN — tiến bộ lớn ở 3A.2** | Tìm được **ERFA (BSD-3-Clause)**, đã đọc chính văn giấy phép, có đủ routine. **Chưa PASS**: chưa biên dịch, chưa sinh bảng, chưa đối chiếu. Xem [PHASE_3A2](PHASE_3A2_ASTRONOMICAL_PREFLIGHT.md) |
| **G15** | **Độ chính xác dữ liệu đủ để không "fake precision"** | 🟡 **MỘT PHẦN** | Đã lượng hoá 5 điểm Sóc rơi đúng 00:00 giờ VN. **3A.2:** xác nhận NASA **không có bản mịn hơn phút**; chưa xác minh được 5 ca bằng oracle độc lập |
| **G16** | **ΔT cho 1901–1960** | ✅ **PASS (3A.3)** | Ta cần **UT1**, không cần UTC ⇒ `eraDat` không nằm trên đường đi. NASA công bố ΔT theo năm. Đo: ΔT sai 5 s ⇒ **0/2474** ngày âm đổi |
| G10 | Test vector biên 1901 / 2100 | ⛔ **BLOCKED** | Trống |
| G11 | **Meeus đã giải quyết hoặc loại bỏ** | ✅ **PASS (bằng cách loại)** | Loại khỏi kế hoạch. Nguồn thay thế xem §G.1 |
| G12 | Historical calendar model đã thiết kế | ✅ **PASS** | `CalendarContext` ở §E.2 |
| G13 | Không cần đổi Room schema | ✅ **PASS** | Không cần cột mới cho MVP |

### KẾT QUẢ: ⛔ **BLOCKED — KHÔNG ĐƯỢC BẮT ĐẦU IMPLEMENTATION**

**Cập nhật sau Phase 3A.1.** BLOCKED: **G5, G6, G7, G9, G10, G14**. Hai nguyên nhân
gốc, không phải một:

1. **Chưa có oracle Tier 1 phủ toàn bộ lịch âm Việt Nam** (G5, G6, G7, G10) — cần
   sách Trần Tiến Bình.
2. **Chưa có nguồn công thức hoàng kinh Mặt Trời hợp lệ** (G14) — blocker kỹ thuật
   mới, Phase 3A chưa thấy vì lúc đó mới đọc landing page của HKO.

Và G9 **xấu đi**: từ "thiếu dữ liệu" thành "hai nguồn có thẩm quyền nói ngược nhau".

### G.1 Đường đi thay thế cho phần thiên văn (sau khi loại Meeus)

Sau khi loại Meeus, engine vẫn cần hai đại lượng: **thời điểm Sóc** và **thời điểm
trung khí**. Nghiên cứu tìm được hai hướng:

**Hướng 1 — dữ liệu có giấy phép rõ ràng (khuyến nghị)**

| Đại lượng | Nguồn | Giấy phép |
|---|---|---|
| Thời điểm Sóc | NASA/GSFC Six Millennium Catalog (A12) | *"Permission is freely granted to reproduce this data when accompanied by an acknowledgment"* |
| Thời điểm 24 tiết khí | HKO (A11) | data.gov.hk TOU — **cho phép thương mại**, kèm attribution |

Ta tự viết code áp 5 quy tắc lên bảng thời điểm này. Không dùng code của ai, không
dùng công thức của ai. Quy mô ước tính cho 1901–2100: ~2.500 điểm Sóc + ~4.800 tiết
khí ≈ **vài chục KB** — không phải "asset JSON khổng lồ".

> ⚠️ **CHƯA XÁC MINH:** hai nguồn này có phủ **đủ 1901–2100** ở dạng tải về hàng
> loạt được hay không. Phải kiểm tra trước khi chốt hướng.

**Hướng 2 — công thức (chưa được duyệt)**

USNO *Computing Approximate Solar Coordinates* (A15) là ứng viên cho hoàng kinh Mặt
Trời. Hai vấn đề chưa giải quyết:

1. **Không truy cập được** `aa.usno.navy.mil` từ máy này (DNS không phân giải) ⇒
   chưa đọc được điều khoản và bảng sai số ⇒ **CHƯA XÁC MINH**.
2. **Sai số có thể không đủ.** Tài liệu thứ cấp nói độ chính xác ~**0.01°** tới năm
   2050. Mặt Trời đi ~0.9856°/ngày ⇒ 0.01° ≈ **~15 phút thời gian**. Quy tắc trung
   khí phụ thuộc việc trung khí rơi **trước hay sau** ranh giới ngày/tháng âm; sai
   15 phút là đủ gây sai ở các ca sát biên — mà đó đúng là những ca tạo ra khác biệt
   VN/TQ. **Phải phân tích sai số biên trước khi dùng.**

**Khuyến nghị: Hướng 1.** Dữ liệu quan trắc có thẩm quyền loại bỏ hoàn toàn câu hỏi
sai số, và giấy phép đã rõ ràng bằng văn bản.

### G.2 Chính xác cần thêm bằng chứng gì

| # | Cần | Ai làm | Mở khoá |
|---|---|---|---|
| 1 | **Sách Trần Tiến Bình** trong tay | Chủ dự án | G5, G6, G7, G10 |
| 2 | Tải bảng HKO 1901–2100 + 24 tiết khí, kiểm tra độ phủ | Tôi, khi được duyệt | G8, và Hướng 1 |
| 3 | Kiểm tra độ phủ catalog NASA cho 1901–2100 | Tôi, khi được duyệt | Hướng 1 |
| 4 | Múi giờ **miền Bắc 1954–1967** | Cần chính văn QĐ 121/CP hoặc sách A8 | G9 |
| 5 | Chốt phương án Bắc/Nam A/B/C | **Chủ dự án** | Nhóm test C5 |
| 6 | (Nếu chọn Hướng 2) Điều khoản + sai số USNO | Tôi, cần mạng tới `aa.usno.navy.mil` | Hướng 2 |

Việc **#1 là đường găng**. Không có nó thì bốn mục CRITICAL không thể mở khoá, và
engine viết ra sẽ không có gì chứng minh là đúng.
