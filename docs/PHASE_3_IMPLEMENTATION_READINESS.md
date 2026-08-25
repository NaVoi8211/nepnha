# Phase 3 — Implementation readiness freeze

> Checkpoint bắt buộc trước khi viết file Kotlin. Nhãn: **FACT** · **FROZEN** ·
> **LIMITATION** · **UNRESOLVED**.
>
> ⚠️ Đánh giá kỹ thuật/provenance. **Không phải tư vấn pháp lý.**

# ✅ KẾT LUẬN: **READY → IMPLEMENT**

Với ba **limitation đã được scope rõ**, không có blocker CRITICAL nào.

---

## Q1. Dataset format đã freeze chưa? — ✅ **FROZEN**

Dataset **chỉ chứa dữ kiện thiên văn**, **không chứa lịch đã tính sẵn**. Quy tắc lịch
nằm trong Kotlin. Đây là điều kiện để tránh circular validation (Q9).

```
app/src/main/assets/lunar/vn_lunar_v1.bin      định dạng nhị phân, big-endian
app/src/main/assets/lunar/vn_lunar_v1.json     provenance + checksum (không đọc lúc chạy)
```

| Offset | Kiểu | Nội dung |
|---|---|---|
| 0 | 4 byte | magic `NNLD` |
| 4 | u16 | version = 1 |
| 6 | u16 | supportedFromGregorian = 1901 |
| 8 | u16 | supportedToGregorian = 2100 |
| 10 | u32 | newMoonCount |
| 14 | u32 | principalTermCount |
| 18 | u32 × n | thời điểm Sóc — **phút UTC kể từ 1900-01-01T00:00Z** |
| … | u32 × m | thời điểm trung khí — cùng đơn vị, thứ tự tăng dần, chu kỳ 12 mốc/năm bắt đầu từ 0° |

Không dấu phẩy động. Không phụ thuộc endian máy. Đọc bằng số nguyên thuần ⇒ **tất
định tuyệt đối**.

**Padding:** dataset chứa Sóc và trung khí **vượt ra ngoài** 1901–2100 (≥12 tuần
trăng và ≥1 chu kỳ trung khí mỗi đầu). Đây là **dữ liệu tính toán nội bộ**, **KHÔNG**
mở rộng phạm vi công bố. Không có padding thì hai năm biên sai — bug A2 của Phase 3A.5.

---

## Q2. NASA/ERFA source choice đã freeze chưa? — ✅ **FROZEN**

| Đại lượng | Nguồn sản xuất | Vai trò của ERFA |
|---|---|---|
| **Điểm Sóc** | **NASA/GSFC Six Millennium Catalog** | ❌ **KHÔNG dùng `eraMoon98`** |
| **Trung khí** | **ERFA `eraEpv00` + `eraEqec06` + `eraNut06a`** (máy dev) | ✅ nhánh **không dính Meeus** |
| ΔT | Cột ΔT của chính catalog NASA | — |

### 🔑 Điều này tách blocker Meeus ra khỏi đường sản xuất

**FACT** (Phase 3A.4, quét toàn bộ 251 file ERFA): `moon98.c` là **file duy nhất**
nhắc tới Meeus, và là **ephemeris Mặt Trăng duy nhất** trong ERFA.

**FACT:** pipeline sản xuất **không gọi `moon98`**. Điểm Sóc lấy từ **dữ liệu NASA**,
thứ có **permission tường minh** của chính bên công bố.

⇒ Câu hỏi chính sách về `moon98` **vẫn UNRESOLVED**, nhưng nó **không còn nằm trên
đường sản xuất**. `moon98` chỉ tồn tại trong `tools/benchmark_erfa_astronomy/` với
vai trò **đối chiếu ở máy dev**.

**LIMITATION còn lại:** dữ liệu NASA *"based on Meeus"*. Ta dựa vào **permission trực
tiếp của NASA** cho chính dữ liệu đó. Đây là vị thế mạnh nhất có được; phần còn lại
là **LEGAL-UNKNOWN** ở lớp thuật toán, không phải điều Nếp Nhà xác định được.

---

## Q3. 6 ca precision-sensitive xử lý thế nào? — ✅ **FROZEN**

**Nguyên tắc:** dataset định nghĩa **theo nguồn đã khai báo**. Giá trị sản xuất =
**giá trị NASA công bố**. Tất định, không ngẫu nhiên, không phụ thuộc đồng hồ/múi giờ.

### Bảng `LUNAR_NEW_MOON_SOURCE_DECISION`

| Ngày (UT) | NASA | ERFA | Giờ VN | Cách 17:00 UTC | Ngày âm theo NASA | Ngày âm theo ERFA | Chọn | Lý do |
|---|---|---|---|---|---|---|---|---|
| 1944-06-20 | 17:00 | 16:59:52 | 00:00 | **7,9 s** | 30/4**N** | 1/5 | **NASA** | Chính sách NASA-first; không có V1 phân xử |
| 1967-07-07 | 17:00 | 16:59:41 | 00:00 | **18,6 s** | 30/5 | 1/6 | **NASA** | như trên |
| 2054-05-07 | 17:00 | 17:00:06 | 00:00 | **6,5 s** | 30/3 | 30/3 | **NASA** | Hai nguồn **cùng kết quả** |
| 2072-12-09 | 16:59 | 16:59:09 | 23:59 | **50,3 s** | cùng | cùng | **NASA** | Hai nguồn cùng kết quả |
| 2077-11-15 | 17:00 | 16:59:25 | 00:00 | **34,7 s** | 30/9 | 1/10 | **NASA** | Không có V1 phân xử |
| 2085-10-18 | 17:00 | 16:59:22 | 00:00 | **37,5 s** | 30/8 | 1/9 | **NASA** | như trên |

**Ghi chú không được xoá:** chọn NASA thay ERFA làm **đổi đúng 4 tháng âm** trong 200
năm (1944, 1967, 2077, 2085) — **120/72.319 ngày = 0,166 %**. Giá trị ERFA được giữ
trong tài liệu này làm bằng chứng, **không** bị xoá.

**Đây là `astronomical uncertainty`, KHÔNG phải `engine bug`.**

**Không** thêm `LunarError.InsufficientPrecision`. **Không** hardcode ngoại lệ.
Cờ `PRECISION_SENSITIVE` chỉ nằm trong **file provenance JSON**, không nằm trong
binary, không lộ ra API, không được dùng để đổi kết quả.

---

## Q4. Boundary semantics 1901/2100 là gì? — ✅ **FROZEN**

| | Quy định |
|---|---|
| Phạm vi công bố | Theo **ngày Gregorian**: 1901-01-01 → 2100-12-31 |
| `toLunar` ngoài phạm vi | `UnsupportedYear` |
| **Nhãn năm âm** | **ĐƯỢC PHÉP** nằm ngoài 1901–2100. Ví dụ 1901-01-01 → **11/11/1900** — đó là ngày âm đúng, không phải lỗi |
| `toSolar` | Nhận năm âm **1900–2101**; nếu ngày Gregorian kết quả ngoài phạm vi công bố ⇒ `UnsupportedYear` |
| Dữ liệu dùng | Chỉ dùng padding đã sinh sẵn. **Không ngoại suy**, không wrap-around |

Lý do cho phép nhãn năm âm vượt biên: cấm nó sẽ khiến 20 ngày đầu 1901 **không biểu
diễn được**, dù chúng nằm trong phạm vi công bố.

---

## Q5. Leap month semantics đã freeze chưa? — ✅ **FROZEN**

> **Tháng nhuận mang SỐ CỦA THÁNG LIỀN TRƯỚC.** Nhuận tháng 4 = lần xuất hiện thứ hai
> của tháng 4. **Không** phải "tháng 5".

**Đây chính là bug A1** mà kiểm chứng cấu trúc HKO đã bắt được ở Phase 3A.5. Engine
Kotlin **bắt buộc** có test riêng cho nó.

`isLeapMonth` là **thuộc tính của `LunarDate`**, không phải tham số rời rạc ở API
chuyển đổi. Phân biệt bốn trạng thái: tháng thường · tháng nhuận · năm không nhuận
(`LeapMonthInfo.None`) · tháng nhuận không tồn tại (`NoSuchLeapMonth`).

---

## Q6. OfficialVietnam UTC+7 limitation đã freeze chưa? — ✅ **FROZEN**

MVP chỉ có `CalendarContext.OfficialVietnam` = **UTC+7 cố định cho toàn dải**.

> **Tuyên bố bắt buộc trong tài liệu và trong app:**
> *"OfficialVietnam trong MVP sử dụng quy chiếu UTC+7 thống nhất cho mục đích tính
> toán. Điều này KHÔNG phải tuyên bố rằng kết quả phản ánh chính xác tập quán lịch
> đương thời của mọi giai đoạn lịch sử, đặc biệt miền Bắc 1954–1967."*

`HistoricalRegion.NORTH/SOUTH`: **ROADMAP ONLY**. **Không** tạo API giả để hứa hỗ trợ.

Engine **không** đọc `ZoneId.systemDefault()`, không đọc locale, không đọc đồng hồ.

---

## Q7. API contract đã freeze chưa? — ✅ **FROZEN**

Xem [LUNAR_API.md](LUNAR_API.md). Tóm tắt: `toLunar` · `toSolar` ·
`daysInLunarMonth` · `monthsInLunarYear` · `leapMonthOf` · `sexagenaryYear`, tất cả
trả `LunarResult<T>`; `LunarError` sealed; `LeapMonthInfo` thay cho `Int?`;
`SexagenaryYear(can, chi)` thay cho `String`. **Không dùng `null`** để che nghiệp vụ.

---

## Q8. Legal/provenance còn blocker nào không? — 🟡 **KHÔNG CÓ BLOCKER TRÊN ĐƯỜNG SẢN XUẤT**

| Thành phần | Vai trò | Trạng thái |
|---|---|---|
| **NASA lunar catalog** | **DATA SOURCE** sản xuất | ✅ **TERMS VERIFIED** — permission tường minh + chính sách bản quyền NASA. Ghi công bắt buộc |
| **ERFA `epv00`/`eqec06`/`nut06a`** | **DEV TOOL** sinh trung khí | ✅ **LICENSE VERIFIED** — BSD-3, phái sinh có phép của IAU SOFA. **Không dính Meeus** |
| **ERFA `moon98`** | **CHỈ đối chiếu ở máy dev** | ⚠️ **UNRESOLVED LEGAL POLICY** — **đã loại khỏi đường sản xuất** (Q2) |
| **HKO** | **VALIDATION SOURCE** | ✅ TERMS VERIFIED — không vào APK |
| **Quy tắc lịch** | **ALGORITHM SOURCE** | ✅ Aslaksen (NUS) + Explanatory Supplement — **độc lập HND**; kinh tuyến 105°Đ theo QĐ 121-CP |
| Hồ Ngọc Đức | — | ⛔ **Không dùng bất kỳ dòng nào** |
| lichviet.app / VnExpress / amlich.app | — | ⛔ Không copy, không runtime, không vào APK |

**Ba khái niệm giữ tách bạch:** DATA SOURCE ≠ ALGORITHM SOURCE ≠ VALIDATION SOURCE.

---

## Q9. Có circular validation nào không? — ✅ **ĐÃ CHẶN BẰNG THIẾT KẾ**

Nguy cơ: *Python model → dataset → Kotlin → test lại bằng chính Python model*.

**Cách chặn:**

1. **Dataset chỉ chứa dữ kiện thiên văn** (thời điểm Sóc, thời điểm trung khí) —
   **không** chứa lịch đã tính. Python model **không** sinh ra ngày âm nào trong
   dataset.
2. **Quy tắc lịch được hiện thực lại độc lập bằng Kotlin** từ đặc tả R1–R6.
3. **Expected value trong test đến từ nguồn NGOÀI:**
   - **HKO** — 2.474 tháng, nhánh HM Nautical Almanac Office
   - **Văn bản nhà nước** — 7 vector V1
   - **Bất biến toán học** — tự chứng, không cần oracle
4. Python model chỉ còn vai trò **cross-implementation check**, ghi rõ là như vậy.

---

## Q10. Chính xác những file Kotlin nào sẽ tạo/sửa?

**Tạo mới — `app/src/main/java/com/nepnha/core/lunar/` (Kotlin thuần, cấm `android.*`)**

| File | Nội dung |
|---|---|
| `LunarDate.kt` | `LunarDate`, `SexagenaryYear`, `LeapMonthInfo`, `CalendarContext` |
| `LunarResult.kt` | `LunarResult<T>`, `LunarError` |
| `LunarDataset.kt` | Parse binary asset. Kotlin thuần, nhận `ByteArray` |
| `VietnameseLunarCalendar.kt` | Interface + hiện thực quy tắc R1–R6 |

**Tạo mới — asset**

`app/src/main/assets/lunar/vn_lunar_v1.bin` · `vn_lunar_v1.json`

**Tạo mới — test**

`app/src/test/java/com/nepnha/core/lunar/` — vector HKO, vector V1, bất biến, biên,
nhuận, 29/30, can chi, round-trip
`app/src/androidTest/java/com/nepnha/core/lunar/LunarAssetTest.kt` — nạp asset trên máy thật

**Tạo mới — tools**

`tools/generate_lunar_dataset.py` · `tools/verify_lunar_dataset.py`

**KHÔNG sửa:** Room/schema · UI/Compose/navigation · `libs.versions.toml` ·
`build.gradle.kts` · bất kỳ file nào của Phase 1/2.

---

## Ba LIMITATION đã scope (không phải blocker)

| # | Limitation | Phạm vi ảnh hưởng |
|---|---|---|
| **L1** | Múi giờ tính lịch **1954–1967** chưa xác định | `HistoricalRegion` không implement; OfficialVietnam dùng UTC+7 hồi tố, **có tuyên bố rõ** |
| **L2** | 6 tháng sát biên chưa có V1 phân xử | **0,24 %** số tháng. Chọn NASA, ghi lại giá trị ERFA |
| **L3** | Tháng nhuận và 29/30 mới ở mức **engineering cross-check** (HKO), chưa có V1 Việt Nam | 2.474 tháng khớp HKO; chưa gọi là *scientifically verified* |

Không limitation nào chặn MVP.

---

# ✅ READY → IMPLEMENT
