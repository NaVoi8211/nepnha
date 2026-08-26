# Phase 3 — Bàn giao sang Phase 4

> **PHASE 3 FROZEN — READY FOR PHASE 4**
>
> ⚠️ Đánh giá kỹ thuật/provenance. **Không phải tư vấn pháp lý.**

---

## 1. Kiến trúc đã đóng băng

```
[ MÁY DEV — không có gì trong đây vào APK ]
NASA/GSFC Six Millennium Catalog ──┐
ΔT Espenak & Meeus (NASA/GSFC) ────┼─► generate_lunar_dataset.py ─► vn_lunar_v1.bin
ERFA epv00 + eqec06 + nut06a ──────┘                                 + vn_lunar_v1.json
HKO · văn bản nhà nước ─► kiểm chứng ngoài      verify_lunar_dataset.py ─► kiểm độc lập

[ APK — Kotlin thuần ]
AssetManager ─► LunarDataset.parse ─► VietnameseLunarCalendar ─► LunarResult<LunarDate>
```

Không C · không NDK · không JNI · không mạng · không tính thiên văn lúc chạy · không
dấu phẩy động trên đường chạy.

| Quyền uy nguồn | Giá trị |
|---|---|
| Điểm Sóc — **DỮ LIỆU** | **NASA/GSFC Six Millennium Catalog** — dùng nguyên phút NASA công bố |
| Trung khí — **DỮ LIỆU** | **ERFA** `eraEpv00` + `eraEqec06` + `eraNut06a` — hiện thực các thủ tục IAU SOFA. Mã nguồn ba hàm này không phái sinh từ Meeus |
| ΔT — **MÔ HÌNH** | **Đa thức NASA/Fred Espenak** (*Polynomial Expressions for Delta T*, [Espenak and Meeus]). Là **mô hình**, không phải số đo — xem [PHASE_3_MEEUS_PROVENANCE.md](PHASE_3_MEEUS_PROVENANCE.md) |
| Quy tắc R1–R5 — **THUẬT TOÁN** | Aslaksen (NUS) · Explanatory Supplement — độc lập Hồ Ngọc Đức |
| Kinh tuyến R6 | **105°Đ** — Quyết định 121-CP điều 1 |
| Bối cảnh | `CalendarContext.OfficialVietnam` = **UTC+7** |

---

## 2. Dataset đã đóng băng

```
app/src/main/assets/lunar/vn_lunar_v1.bin
  sha256   b9f9613a0d1974ac82a024a737b8b40cbd1588869881db13c90f4c90a020f33d
  kích thước 19.946 byte    2.534 điểm Sóc    2.448 trung khí
  epoch    1890-01-01T00:00:00Z, đơn vị phút UTC, u32 big-endian
  lượng tử hoá  floor   (KHÔNG phải round — xem PHASE_3_DATASET_CORRECTION.md §C)
```

Chứa **chỉ dữ kiện thiên văn**. Không chứa lịch đã tính. Quy tắc nằm trong Kotlin.

Checksum bị khoá ở `LunarTestSupport.DATASET_SHA256`. Sinh lại:

```bash
./tools/build_erfa_bench.sh
python3 tools/generate_lunar_dataset.py     # phải ra đúng sha256 trên
python3 tools/verify_lunar_dataset.py       # kiểm độc lập
python3 tools/test_generator.py             # self-test ΔT + floor
```

---

## 3. API đã đóng băng

```kotlin
val supportedYears: IntRange                                  // 1901..2100 (năm DƯƠNG)

fun toLunar(solar: LocalDate, context = OfficialVietnam): LunarResult<LunarDate>
fun toSolar(lunar: LunarDate, context = OfficialVietnam): LunarResult<LocalDate>
fun daysInLunarMonth(year, month, isLeapMonth = false):       LunarResult<Int>
fun monthsInLunarYear(lunarYear):                             LunarResult<Int>
fun leapMonthOf(lunarYear):                                   LunarResult<LeapMonthInfo>
fun sexagenaryYear(lunarYear):                                LunarResult<SexagenaryYear>
```

Thuần · tất định · an toàn đa luồng. Không đọc đồng hồ, múi giờ, locale, mạng, DB.

---

## 4. Hợp đồng cho Phase 4

| Điều | Bắt buộc |
|---|---|
| **Ngày giỗ** | Dùng `NonexistentLunarDate.lastValidDay` + `MemorialRule.missingDayPolicy`. **Engine không bao giờ tự lùi 30 → 29** — quyết định đó thuộc tầng nghiệp vụ và phải nhìn thấy được |
| **Khởi tạo** | `VietnameseLunarCalendar.create(LunarDataset.parse(bytes))` trong `AppContainer`. Đo trên A32: 365 lượt chuyển đổi hết **24 ms** ⇒ nạp thẳng lúc khởi động, **không** cần lazy-load hay background thread |
| **Đọc asset** | Việc đọc byte thuộc tầng ngoài. `core/lunar` không được biết Android tồn tại — hiện có **0 import `android.*`**, phải giữ nguyên |
| **Lỗi** | Không dùng `null` che nghiệp vụ. `LeapMonthInfo.None` là **thành công**, không phải lỗi |
| **Giới hạn L1** | Tuyên bố UTC+7 hồi tố phải **xuất hiện trong app**, không chỉ nằm trong tài liệu |
| **Không được** | Thêm `InsufficientPrecision` · hardcode ngoại lệ lịch · sửa dataset để test xanh · đưa dữ liệu lịch online vào APK |

---

## 5. Bằng chứng

| Tầng | Nội dung | Kết luận rút ra được |
|---|---|---|
| **1 — vector ngoài** | 7 vector văn bản nhà nước · Tết 1985/2007/2030/2053 · nhuận 1984, 1987 theo Ban Lịch Nhà nước · can chi | ✅ **engine đúng** trên các ca này |
| **2 — bất biến** | 9 test trên **toàn bộ 73.049 ngày** | nhất quán nội tại — **không** chứng minh đúng |
| **3 — cross-implementation** | 1.975 vector, sinh bằng bộ quy tắc Python đi thẳng NASA + ERFA, **không đọc `.bin`** | bắt lỗi **quy tắc lẫn lỗi đọc dataset** |
| **4 — thiết bị** | 4 test trên SM-A325F | đóng gói và chạy thật đúng |
| **5 — hồi quy biên** | 6 test khoá 1938 và toàn bộ 15 sự kiện sát ranh giới ngày | chặn tái phát blocker |

**Kiểm chứng ngoài, toàn dải:**

```
HKO 1901-2100 @120°Đ : 2.474 tháng, khớp 2.471 (99,88%)
                        lệch số tháng 0 · lệch cờ nhuận 0 · 73/73 năm nhuận khớp
                        3 khác biệt 1914/1916/1920 — đều trước 1929, khi Trung Quốc
                        dùng giờ mặt trời trung bình Bắc Kinh (UTC+7:45:40)
NASA điểm Sóc         : 2.474/2.474 có mặt, đúng tới phút        [dữ liệu ngoài]
NASA cột ΔT           : 200/200 năm khớp đa thức trong lượng tử phút [dữ liệu ngoài]
ERFA trung khí        : 2.448/2.448 khớp — đây là KIỂM CHÉO HIỆN THỰC,
                        KHÔNG phải oracle thiên văn độc lập: verifier và
                        generator dùng chung ERFA và chung mô hình ΔT
```

**Kết quả chạy:**

```
:app:assembleDebug              BUILD SUCCESSFUL, 0 lỗi 0 cảnh báo
:app:testDebugUnitTest          59/59
:app:connectedDebugAndroidTest  30/30 trên SM-A325F (Android 13, arm64-v8a)
tools/verify_lunar_dataset.py   DATASET HỢP LỆ
tools/test_generator.py         TẤT CẢ ĐỀU QUA
sinh lại 4 lần, 3 múi giờ, 3 locale — giống hệt từng byte
```

---

## 6. Giới hạn còn lại — không phải việc còn dở

| # | Giới hạn |
|---|---|
| **L1** | `OfficialVietnam` áp **UTC+7 hồi tố** cho toàn phạm vi 1901–2100. **Không** tuyên bố phản ánh đúng tập quán lịch đương thời mọi thời kỳ, đặc biệt **miền Bắc 1954–1967** |
| **L2** | `HistoricalRegion.NORTH/SOUTH` **chưa hiện thực** — roadmap, cố ý không tạo API hứa suông |
| **L3** | Một số ít sự kiện thiên văn **nhạy cảm với ranh giới ngày**: 8 điểm Sóc và 7 trung khí nằm trong ±120 s quanh 17:00:00Z, hẹp nhất là 6,3 s. Điểm Sóc lấy nguyên phút NASA công bố nên bất định còn lại là **của NASA** |
| **L4** | **Nhuận tháng 8 năm 1938 chưa có nguồn lịch sử Việt Nam bậc 1 trong tập bằng chứng của dự án.** Chỗ dựa là pipeline đã sửa + quy tắc 105°Đ + nhất quán với 1984/1987 |
| **L5** | **ΔT tương lai phụ thuộc mô hình.** Đường sản xuất dùng đa thức NASA/Espenak cho **toàn bộ** 1901–2100. NASA ghi rõ đoạn 2050–2150 *"is derived from estimated values"*, mốc 2010 và 2050 đều là ngoại suy tuyến tính. Nếp Nhà **không** tuyên bố biết trước lịch sử quay của Trái Đất |
| **L6** | **Quan hệ với Meeus đã được nêu đúng phạm vi, không còn là câu hỏi treo.** Không mã nguồn Meeus nào trong ứng dụng; nhưng dữ liệu NASA và mô hình ΔT đều có dẫn Meeus. Xem [PHASE_3_MEEUS_PROVENANCE.md](PHASE_3_MEEUS_PROVENANCE.md) |

**Dataset 1901–2100 KHÔNG phải bản ghi lịch Việt Nam đã được kiểm chứng lịch sử cho
từng năm.** Nó là kết quả của một mô hình thiên văn có provenance rõ ràng, được đối
chiếu ngoài ở một số điểm.

Tầng 2 và 3 **không** chứng minh lịch đúng. Cách gọi đúng cho sản phẩm này là
**"mô hình lịch âm Việt Nam tính từ thiên văn, có provenance rõ ràng"** — không phải
*"đã được chứng minh khoa học là đúng"*.

---

## 7. Ghi công bắt buộc

```
Moon Phase Predictions by Fred Espenak, NASA/GSFC
Dữ liệu tiết khí tạo bằng ERFA, thư viện phái sinh có phép từ IAU SOFA
Mô hình ΔT: đa thức NASA/Fred Espenak, Five Millennium Canon of Solar Eclipses
```

Điều khoản NASA áp dụng, nguyên văn:

> *"Permission is freely granted to reproduce this data when accompanied by an
> acknowledgment."*

Dự án **không** tuyên bố "NASA data is public domain" — chỗ dựa là câu cho phép trên,
kèm ghi công. Ứng dụng không hiển thị văn bản pháp lý thừa cho người dùng; provenance
đầy đủ nằm trong kho mã.

---

## 8. Tài liệu

[LUNAR_API.md](LUNAR_API.md) · [LUNAR_DATASET_PROVENANCE.md](LUNAR_DATASET_PROVENANCE.md) ·
[PHASE_3_IMPLEMENTATION.md](PHASE_3_IMPLEMENTATION.md) ·
[PHASE_3_FINAL_AUDIT.md](PHASE_3_FINAL_AUDIT.md) ·
[PHASE_3_DATASET_CORRECTION.md](PHASE_3_DATASET_CORRECTION.md) ·
[PHASE_3_MEEUS_PROVENANCE.md](PHASE_3_MEEUS_PROVENANCE.md)
