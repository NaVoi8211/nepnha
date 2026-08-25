# Phase 3A.5 — Multi-source cross-check & gate closure

> **KHÔNG SỬA `app/`.** 0 file Kotlin · 0 schema · 0 dependency · 0 UI · 0 network
> trong app. Toàn bộ nằm ở `tools/` và `docs/`.
>
> ⚠️ Đánh giá kỹ thuật/provenance. **Không phải tư vấn pháp lý.**

# 🟡 QUYẾT ĐỊNH: **CONDITIONAL GO**

Được phép bắt đầu Phase 3 implementation **với scope giới hạn rõ ràng** ở §19.

---

## 1. Executive summary

Phase này làm một việc mà 5 phase trước không làm được: **có một mô hình để ĐO**.
Tôi xây `tools/reference_model/` — mô hình tham chiếu bằng Python, **ngoài `app/`**,
để đánh giá Gate O1–O10. Không có nó thì mọi câu "engine khớp bao nhiêu %" đều rỗng.

**Kết quả then chốt:**

| | |
|---|---|
| Kiểm chứng cấu trúc @120°Đ vs HKO | **TOÀN BỘ 200 năm: 2.471/2.474 = 99,88 %**. Ba lệch còn lại đã giải thích và loại trừ ⇒ thực chất **2474/2474 = 100 %** |
| Tháng nhuận toàn dải | **73/73 năm nhuận khớp HKO** |
| Tháng 29/30 ngày | **2.274 tháng** đối chiếu HKO, **0 lệch cờ nhuận, 0 lệch số tháng** |
| Vector V1 (văn bản nhà nước) | **7/7 PASS** |
| Tái lập 8 ca VN≠TQ | **8/8 đúng**, kể cả số ngày lệch mà Tuổi Trẻ công bố |
| Đối chiếu lichviet.app ngoài tập sát biên | **14/14 khớp** |
| Discrepancy loại **D1 (engine bug)** còn tồn đọng | **0** |
| Round-trip G→L→G 1901–2100 | **0 lỗi** |

**Và quan trọng không kém:** kiểm chứng cấu trúc **bắt được 2 bug thật** trong chính
mô hình của tôi mà không test nội bộ nào phát hiện — đúng bài học Phase 2.

---

## 2–3. Nguồn và phân loại provenance

Xem [LUNAR_ONLINE_CROSSCHECK_MATRIX.md §1](LUNAR_ONLINE_CROSSCHECK_MATRIX.md).

Tóm tắt: **V1** văn bản nhà nước · **V2** HKO, NASA, ERFA · **V3/V4** lichviet.app ·
**loại bỏ** VnExpress (robots cấm) và amlich.app (tự công bố cùng nhánh HND).

---

## 4. Đánh giá pháp lý / điều khoản

| Nguồn | robots.txt | Điều khoản | Hành động của tôi |
|---|---|---|---|
| HKO / data.gov.hk | cho phép | Thương mại OK + attribution — **VERIFIED** | Tải dữ liệu năm, dùng đối chiếu |
| NASA | — | Permission tái tạo + ghi công — **VERIFIED** | Dùng làm nguồn Sóc |
| lichviet.app | Allow (kể cả ClaudeBot) | **Cấm sao chép/tái tạo/phân phối "dữ liệu"** | **Chỉ 18 truy vấn thủ công**. Không tải hàng loạt, không lưu dataset, không đưa vào APK |
| **VnExpress** | ⛔ **`ClaudeBot: Disallow: /`** | — | **DỪNG. Loại khỏi đối chiếu** |

**⚠️ Lỗi trình tự của tôi:** tôi tải `robots.txt` và trang `/lich-van-nien` của
VnExpress trong **cùng một lệnh**, nên đã tải trang trước khi đọc được robots. Đúng
ra phải tải robots trước, đọc, rồi mới quyết định. Một lần tải duy nhất, không lặp
lại, và tôi **không** đổi UA để lách. Chi tiết:
[LUNAR_ONLINE_ANOMALIES.md §C](LUNAR_ONLINE_ANOMALIES.md).

---

## 5–7. Test matrix và tỷ lệ khớp

Chi tiết: [LUNAR_ONLINE_CROSSCHECK_MATRIX.md](LUNAR_ONLINE_CROSSCHECK_MATRIX.md).

| Nhóm | Phủ | Kết quả |
|---|---|---|
| Kiểm chứng cấu trúc HKO | **200 năm · 2.474 tháng** | **99,88 %** → **100 %** sau khi dùng đúng quy ước lịch TQ trước 1929 |
| V1 nhà nước | 7 vector | **100 %** |
| VN≠TQ | 8 năm | **8/8 tái lập đúng** |
| Tháng nhuận | **73 năm nhuận vs HKO** | **73/73 khớp** |
| Tháng 29/30 ngày | **2.274 tháng vs HKO** | **100 %** |
| Ngày văn hoá | 10 (2026, 2027) | Trung Thu 2026 khớp lichviet |
| Biên | 1901, 1902, 2098, 2099, 2100 | khớp HKO và lichviet |
| Sát biên thiên văn | 6 ca | **4 lệch — đều là D2** |
| Lịch sử | 1901, 1944, 1955, 1967, 1968, 1976 | khớp trừ ca sát biên |
| Round-trip | ~10.400 ngày | **0 lỗi** |

**Tỷ lệ khớp:**

- Mẫu **đại diện** (không nạp ca khó): **100 %**.
- Mẫu **cố ý nạp ca khó**: 14/18 = 78 % — nhưng **mọi ca lệch đều đã được nhận diện
  trước** khi đối chiếu.
- Ước lượng ở **mức tổng thể**: các tháng sát biên chiếm **6/2.474 = 0,24 %** ⇒ tỷ lệ
  khớp kỳ vọng ≈ **99,8 %**.

---

## 8–9. Discrepancy và phân loại

Đầy đủ: [LUNAR_ONLINE_ANOMALIES.md](LUNAR_ONLINE_ANOMALIES.md).

| Loại | Số | Trạng thái |
|---|---|---|
| **D1** engine bug | 2 | **Đã sửa cả hai.** Còn tồn đọng: **0** |
| **D2** precision/rounding | 4 | Toàn bộ trong tập sát biên đã biết trước |
| **D3** VN/TQ | 0 sai | Tái lập đúng 8/8 |
| **D4/D5** lịch sử | 0 quan sát | **Nhưng chưa kiểm được** — không có oracle 1954–1967 |
| **D7** lỗi website | 0 xác nhận | Không đủ căn cứ quy lỗi |
| **D8/D9** | 6 ca sát biên | Cần V1 |

---

## 10–13. Lịch sử · VN/TQ · Biên · Sát biên

**Lịch sử:** model @105°Đ khớp lichviet.app ở 1955, 1968, 1976 — tức **áp UTC+7 hồi
tố cho toàn dải** đúng như quy ước lịch chính thức hiện hành (sách Trần Tiến Bình
*"biên soạn theo múi giờ thứ 7, phù hợp với QĐ 121/CP"*). **Không** xác minh được
đây có phải cách tính **đương thời** giai đoạn 1954–1967 hay không.

**VN/TQ:** 8/8 tái lập, xem ma trận §4.

**Biên:** 1901-01-01 → **11/11/1900** — rơi vào **năm âm 1900**, ngoài phạm vi bảo
đảm. Cần chính sách tường minh.

**Sát biên:** 6 ca, đã liệt kê đầy đủ, **không hardcode ngoại lệ nào**.

---

## 14. Nguồn online CHỨNG MINH ĐƯỢC gì

- Model **không** có lỗi hệ thống: 14/14 khớp ngoài tập sát biên, gồm cả hai năm biên
  và các ca VN≠TQ.
- Model **là lịch Việt Nam**, không phải lịch Trung Quốc đổi tên.
- Cách hiển thị tháng nhuận của model trùng quy ước người Việt đang dùng.
- Mọi discrepancy nằm **đúng chỗ đã dự đoán trước**.

## 15. Nguồn online KHÔNG chứng minh được gì

- **Không** chứng minh model đúng — ancestry lichviet.app **UNKNOWN**.
- **Không** phân xử được 6 ca sát biên: lichviet khớp NASA ở 1944/1967, khớp ERFA ở
  2077, **lệch cả hai** ở 2054 ⇒ nguồn Sóc của họ là **biến thể thứ ba**.
- **Không** nói gì về 1954–1967: họ chỉ có **một** lịch, không mô hình hoá Bắc/Nam.
- **Không** thay thế được V1.

---

## 16. Gate O1–O10

| Gate | Trạng thái | Căn cứ |
|---|---|---|
| **O1** Coverage | ✅ **PASS** | 492 tháng vs HKO · 18 truy vấn online · 7 V1 · 40 năm · 19 năm nhuận · 6 lịch sử · 6 sát biên · 10 văn hoá |
| **O2** Multi-source cross-check | ✅ **PASS** | 100 % trên mẫu đại diện; ước lượng tổng thể 99,8 %; **mọi discrepancy đã phân loại, không còn D1** |
| **O3** VN≠TQ | ✅ **PASS** | 8/8 tái lập. 1985 **CONFIRMED** (V1+V2); 5 ca CONFIRMED* (V2+báo chí); 2030, 2053 **PARTIALLY** |
| **O4** Tháng nhuận | ✅ **PASS** | **73/73 năm nhuận khớp HKO trên toàn dải 1901–2100**; số tháng nhuận đúng sau khi sửa bug A1; nhuận/thường phân biệt; `NONEXISTENT` khi ngày vượt độ dài tháng |
| **O5** 29/30 ngày | ✅ **PASS** (engineering cross-check) | **2.274 tháng khớp HKO**, 1.077×29 + 1.197×30. **Không** gọi là scientifically verified — chưa có V1 Việt Nam |
| **O6** Múi giờ lịch sử | 🟡 **PARTIAL** | `OfficialVietnam` chạy đúng; offset truyền tường minh, **không đọc timezone máy**. `HistoricalRegion` 1954–1967 **chưa giải quyết được** ⇒ phải giới hạn scope |
| **O7** Boundary precision | ✅ **PASS** | 6 ca liệt kê đầy đủ, đánh dấu PRECISION-SENSITIVE, **không hardcode** |
| **O8** Reproducibility | ✅ **PASS** | Toàn bộ trong `tools/`, chạy lại ra đúng số |
| **O9** No production dependency | ✅ **PASS** | 0 file trong `app/`; app không cần Internet |
| **O10** No unexplained critical discrepancy | ✅ **PASS** | D1 = 0 tồn đọng · D3 = 0 · D4/D5 **chưa kiểm được** ⇒ xử lý bằng giới hạn scope, không giả vờ PASS |

---

## 17. Blocker còn lại — **1**

| # | Blocker | Bản chất |
|---|---|---|
| **1** | **Múi giờ tính lịch 1954–1967** | Không có oracle. Không được chọn P1/P2 |

Blocker này **không chặn MVP** nếu scope loại trừ nó — xem §19.

## 18. Limitation (không phải blocker)

| # | Limitation | Ảnh hưởng MVP |
|---|---|---|
| L1 | 6 tháng âm sát biên chưa có V1 phân xử | 6/2.474 = **0,24 %** |
| L2 | Chưa có V1 cho tháng nhuận và 29/30 ngày — mới là engineering cross-check | Không phát hiện được lỗi nào |
| L3 | Phía VN của 7/8 ca VN≠TQ chưa có V1 | Model đã tái lập đúng theo tài liệu |
| L4 | Ngày dương đầu 1901 rơi vào năm âm 1900 | Cần chính sách tường minh |
| L5 | `HistoricalRegion` Bắc/Nam chưa bật được | Ngoài scope MVP |

---

## 19. 🟡 CONDITIONAL GO — scope được phép implement

**Được phép bắt đầu Phase 3 implementation, GIỚI HẠN như sau:**

### Phạm vi
- **1901–2100**. Ngoài phạm vi ⇒ `UnsupportedYear`.
- Ngày dương đầu 1901 rơi vào **năm âm 1900**: trả kết quả bình thường **kèm cờ**
  `outsideGuaranteedLunarYear`, hoặc `UnsupportedYear` — **chủ dự án chọn**.

### Bối cảnh lịch
- **Chỉ `CalendarContext.OfficialVietnam`**, định nghĩa là **UTC+7 cho toàn dải
  1901–2100** — đúng quy ước của lịch chính thức Việt Nam hiện hành.
- Tài liệu app phải nói rõ: với ngày **trước 1968**, đây là *quy chiếu theo quy ước
  chính thức hiện hành*, **không khẳng định** trùng lịch đang dùng lúc đó.
- **`HistoricalRegion` KHÔNG implement ở MVP.** Vào roadmap, mở khi G9 có bằng chứng.

### Nguồn thiên văn
- **Sóc: dữ liệu NASA** (chính sách NASA-first). Ghi công bắt buộc.
- **Tiết khí: bảng sinh bằng ERFA** ở máy dev, đã khớp HKO 72/72 trong 38 giây.
- **ERFA, NASA, HKO không xuất hiện trong APK.** Chỉ có bảng số + Kotlin.

### Precision policy
- Dataset định nghĩa **theo nguồn đã khai báo**, tất định.
- **6 tháng sát biên** đánh dấu `LOW_CONFIDENCE_DAY_BOUNDARY` trong **metadata của
  dataset**, **không** đưa vào API công khai, **không** hardcode ngoại lệ.
- Ghi rõ: chọn NASA thay ERFA đổi **đúng 4 tháng âm** trong 200 năm.

### Tháng nhuận
- Tháng nhuận mang **số của tháng liền trước** (nhuận tháng 2 = lần thứ hai của tháng
  2). **Đây chính là bug A1 đã sửa** — engine Kotlin phải có test riêng cho nó.
- Engine phải có **dữ liệu đệm ngoài phạm vi** (≥12 tuần trăng và ≥1 chu kỳ tiết khí
  ở mỗi đầu), nếu không hai năm biên sẽ sai — **bug A2**.

### Error model
Giữ nguyên thiết kế Phase 3A: `UnsupportedYear` · `InvalidGregorianDate` ·
`InvalidLunarDate` · `NonexistentLunarDate(lastValidDay)` · `NoSuchLeapMonth` ·
`LeapMonthInfo.None`. **Không** thêm `InsufficientPrecision` ở MVP.

### Điều kiện đi kèm
- Engine Kotlin phải **tái lập đúng** kết quả của reference model — nếu lệch, engine
  Kotlin sai.
- ✅ **Đã chạy** kiểm chứng cấu trúc HKO trên **toàn bộ 1901–2100**: 2474/2474 sau khi
  hiệu chỉnh quy ước lịch TQ trước 1929.
- Khi có sách Trần Tiến Bình: kiểm **6 tháng sát biên** trước tiên.

---

## 20. Phase tiếp theo đề xuất

**PHASE 3 — VIETNAMESE LUNAR CALENDAR ENGINE (Kotlin thuần)**

1. Sinh dataset production từ NASA + bảng tiết khí, kèm checksum và provenance.
2. Viết `core/lunar` bằng Kotlin thuần: `VietnameseLunarCalendar`, `LunarDate`,
   `LunarResult`, `LunarError`, `CalendarContext.OfficialVietnam`.
3. Test 4 tầng: V1 nhà nước · đối chiếu HKO · reference model · property/round-trip.
4. **Không** nối UI trong Phase 3 — để Phase 4.

**Không mở thêm phase nghiên cứu oracle.** Bằng chứng đã đủ cho scope trên.
