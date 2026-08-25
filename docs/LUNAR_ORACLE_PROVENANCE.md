# Oracle provenance & independence analysis

> Phase 3A. Nghiên cứu 2026-08-25. Không có code.
>
> **Nguyên tắc trung tâm của tài liệu này:**
>
> > **Agreement with multiple websites does not imply independent correctness if
> > they share the same implementation.**
>
> Hai trang cho cùng kết quả chỉ chứng minh chúng chạy cùng một đoạn code.

---

## 1. Đồ thị phụ thuộc — vì sao "nhiều nguồn khớp nhau" là ảo tưởng

```
                    ┌──────────────────────────────────────┐
                    │  Hồ Ngọc Đức — amlich.js (2004)      │
                    │  license: personal, NON-COMMERCIAL   │
                    └──────────────────┬───────────────────┘
                                       │  port / sao chép
        ┌──────────────┬───────────────┼───────────────┬──────────────┐
        ▼              ▼               ▼               ▼              ▼
  VietnameseLunar  Vietnamese     LunarCalendar4J   vanng822/     hàng chục
  -android (MIT?)  Calendar(MIT?)      (MIT?)        amlich       web lịch VN
        │              │               │               │              │
        └──────────────┴───────────────┴───────────────┴──────────────┘
                                       │
                                       ▼
                    ✗ TẤT CẢ LÀ MỘT NGUỒN, KHÔNG PHẢI NĂM NGUỒN
```

Năm "nguồn" ở hàng dưới **không độc lập với nhau**. Chúng khớp nhau vì cùng một
implementation. Dùng chúng để "đối chiếu chéo" là tự lừa mình.

Ba cái gắn nhãn `MIT?` còn nguy hiểm hơn: nhãn MIT do người port tự gắn cho một tác
phẩm phái sinh của code phi thương mại — xem [PHASE_3_PREFLIGHT.md §1.3](PHASE_3_PREFLIGHT.md).

### Các nhánh THỰC SỰ độc lập

```
NHÁNH A — Nhà nước Việt Nam
  Ban Lịch Nhà nước (TT Thông tin Tư liệu, Viện Hàn lâm KH&CN VN)
      ├─→ Trần Tiến Bình, "Lịch Việt Nam thế kỷ XX–XXI (1901–2100)", NXB VHTT 2005
      └─→ Văn bản pháp quy công bố ngày nghỉ Tết (Chính phủ / Bộ)
              → báo chí đưa lại  (KHÔNG tính là nguồn thứ hai)

NHÁNH B — Đài Thiên văn Hồng Kông (HKO)
  HKO tự tính (dựa số liệu HM Nautical Almanac Office)
      ├─→ Bảng đối chiếu Dương–Âm 1901–2100
      └─→ Bảng 24 tiết khí
              → LỊCH TRUNG QUỐC (UTC+8), KHÔNG phải lịch Việt Nam

NHÁNH C — NASA / GSFC
  Fred Espenak, Six Millennium Catalog of Phases of the Moon
      → thời điểm Sóc (UT), độc lập hoàn toàn với A và B

NHÁNH D — Hồ Ngọc Đức
  → xem đồ thị trên. MỘT nhánh duy nhất, dù xuất hiện ở hàng chục nơi.
```

**Đối chiếu chéo hợp lệ = lấy hai nhánh KHÁC CHỮ CÁI.** A vs D là hợp lệ. Hai trang
web lịch VN với nhau thì không.

---

## 2. Phân tích từng nguồn test

| Nguồn | Lấy dữ liệu từ đâu | Có phải HND-derived? | Độc lập? | Dùng được làm gì |
|---|---|---|---|---|
| Trần Tiến Bình (sách in) | Ban Lịch Nhà nước, tự tính | **Không** | ✅ Nhánh A | **Oracle Tier 1 cho lịch VN** |
| Văn bản công bố nghỉ Tết | Quyết định hành chính nhà nước | **Không** | ✅ Nhánh A | **Oracle Tier 1 cho ngày Tết** |
| HKO — bảng đối chiếu 1901–2100 | HKO tự tính | **Không** | ✅ Nhánh B | Oracle Tier 1 **cho phía Trung Quốc** |
| HKO — 24 tiết khí | HM Nautical Almanac Office | **Không** | ✅ Nhánh B | Thời điểm trung khí (dữ liệu thiên văn) |
| NASA/GSFC — pha Mặt Trăng | Espenak, ephemeris JPL | **Không** | ✅ Nhánh C | Thời điểm Sóc |
| Wikipedia (bảng ngày Tết) | ⚠️ **không ghi nguồn tính** | Không rõ | 🟡 Tier 2 | Sanity check; **không** làm oracle duy nhất |
| Hànộimới / Tuổi Trẻ / Thanh Niên | Phỏng vấn Ban Lịch Nhà nước | Không | 🟡 dẫn lại nhánh A | Tốt cho ca đặc biệt; **không** tính là nguồn thứ hai độc lập với sách |
| `calrules_en.html` của HND | Chính tác giả | — | ⛔ Nhánh D | Đọc hiểu **quy tắc**; **không** làm oracle |
| Web lịch vạn niên VN | Gần như chắc chắn HND | **Có** | ⛔ | **Không dùng** |
| Sách lịch dịch từ TQ | Nguồn Trung Quốc | Không, nhưng **sai lịch VN** | ⛔ | **Không dùng** — báo Tuổi Trẻ đã cảnh báo |

---

## 3. Bẫy đã suýt mắc

**Wikipedia và HND cùng cho Tết 2030 = 02/02/2030.** Ở
[LUNAR_TEST_VECTORS.md](LUNAR_TEST_VECTORS.md) tôi từng đánh dấu ✅ "đã đối chiếu
chéo".

Đánh giá lại: **bài Wikipedia không ghi nguồn tính bảng ngày Tết**. Nếu bảng đó được
soạn từ một web lịch VN chạy code HND thì "hai nguồn" thực chất là một. Chưa chứng
minh được Wikipedia thuộc nhánh nào ⇒ **hạ xuống 🟡**, không phải ✅.

Đây đúng loại sai lầm mà tài liệu này sinh ra để chặn.

---

## 4. Nguồn Tier 1 mới tìm được: văn bản nhà nước công bố nghỉ Tết

Mỗi năm Chính phủ/Bộ ra văn bản ấn định lịch nghỉ Tết, **ghi kèm cả ngày âm lẫn ngày
dương**. Đây là **quyết định hành chính**, không phải kết quả của phần mềm nào.

Ví dụ 2026 (Thông báo 9441/TB-BNV, Bộ Nội vụ):

| Ngày âm | Ngày dương |
|---|---|
| 27 tháng Chạp Ất Tỵ | 14/02/2026 |
| **Mùng 1 Tết Bính Ngọ** | **17/02/2026** |
| Mùng 6 Tết | 22/02/2026 |

Giá trị: mỗi năm cho **ít nhất hai cặp** dương↔âm đã được nhà nước xác nhận, và
chuỗi này kéo dài nhiều năm về trước. Đây là nguồn Tier 1 **truy cập online được**,
bổ khuyết cho sách Trần Tiến Bình (chỉ có bản in).

> ⚠️ Giới hạn: chỉ phủ vùng quanh Tết. Không giúp gì cho Rằm tháng 7, Trung Thu,
> tháng nhuận hay số ngày của tháng âm.

---

## 4b. Provenance của sách Trần Tiến Bình (Phase 3A.4)

Trước khi gọi sách là "oracle độc lập tuyệt đối", đã tra phần giới thiệu của nhà
phát hành:

**FACT (nguồn thứ cấp).** Sách *"biên soạn theo múi giờ thứ 7, phù hợp với Quyết định
121/CP"*, có trình bày *"quá trình phát triển của lịch Việt Nam, cơ sở tính toán,
nguyên nhân và sự khác biệt với lịch Trung Quốc"*.

**FACT.** Nội dung: đối chiếu dương–âm · can chi · ngày Julius · giờ mọc lặn Mặt Trời
· nhật/nguyệt thực · ngày lễ tết.

| Câu hỏi | Trả lời |
|---|---|
| Có bảng âm–dương 1901–2100? | **FACT: có** |
| Có tháng nhuận? | **INFERENCE: có** (bảng đầy đủ tất yếu thể hiện) |
| Có 29/30 ngày? | **INFERENCE: có** (suy từ ngày đầu các tháng liên tiếp) |
| Có dựa vào HND/Meeus không? | **UNKNOWN** — sách 2005, HND 2004, không có bằng chứng theo chiều nào |
| Có giờ chuyển tiết? | **UNKNOWN** |
| Có cơ sở dữ liệu thiên văn? | **UNKNOWN** |

⚠️ **Cảnh báo.** Nếu *"biên soạn theo múi giờ thứ 7"* nghĩa là dùng UTC+7 cho **toàn
bộ** 1901–2100, sách **không mô hình hoá** miền Nam UTC+8 (1960–1975) và miền Bắc
trước 1968 ⇒ **không giải được G9**. Đây là **INFERENCE, chưa VERIFIED** — phải đọc
phần dẫn nhập mới biết.

---

## 4c. ⛔ ĐÍNH CHÍNH (Phase 3A.5) — ba lớp phải tách

| Lớp | Là gì | Không phải là gì |
|---|---|---|
| `robots.txt` | Chỉ dẫn cho crawler | **Không** phải giấy phép, **không** override Terms |
| Terms of Service | Ràng buộc hợp đồng | Không phải tuyên bố về quyền tác giả |
| Quyền tác giả / quyền dữ liệu | Vấn đề riêng | **LEGAL-UNKNOWN** |

Phase 3A.4 trình bày ba thứ này lẫn vào nhau. Đã sửa ở
[PHASE_3A5_ORACLE_CONSOLIDATION.md §1](PHASE_3A5_ORACLE_CONSOLIDATION.md).

## 4d. ⛔ ĐÍNH CHÍNH — NASA và ERFA cùng nhánh Meeus

Nhánh C ở §1 (NASA) và ERFA **không** phải hai nhánh độc lập về thuật toán: cả hai
dựa trên Meeus 1998. Nhánh thiên văn thật sự độc lập hiện có: **HKO** (HM Nautical
Almanac Office) và **JPL DE440** (tích phân số). Xem
[PHASE_3A5_ORACLE_CONSOLIDATION.md §2, §7](PHASE_3A5_ORACLE_CONSOLIDATION.md).

## 4e. Nguồn QUY TẮC độc lập với HND (Phase 3A.5)

Trước đây quy tắc lịch chỉ lấy từ mô tả của HND. Nay có nguồn học thuật độc lập:

| Quy tắc | Nguồn độc lập |
|---|---|
| Ngày chứa Sóc là mùng 1, lấy trọn ngày | **Aslaksen**, *The Mathematics of the Chinese Calendar* (NUS) |
| Đông chí nằm trong tháng 11 | Aslaksen · *Explanatory Supplement to the Astronomical Almanac* |
| Tháng không có trung khí là tháng nhuận; hai tháng thì lấy tháng đầu sau Đông chí | Aslaksen |
| 12 trung khí ở bội số 30° | Aslaksen |
| **Kinh tuyến 105°Đ cho Việt Nam** | **Quyết định 121-CP điều 1** (NOT INDEPENDENTLY VERIFIED — 403) |

**INFERENCE.** Quy tắc 1–5 **dùng chung với lịch Trung Quốc**; khác biệt Việt Nam nằm
gọn ở **một tham số**. Cấu trúc này cho phép kiểm chứng cấu trúc qua HKO — xem
[PHASE_3A5_FINAL_PROVENANCE_GATE.md §8](PHASE_3A5_FINAL_PROVENANCE_GATE.md).

## 5. Kết luận về độ độc lập

| Câu hỏi | Trả lời |
|---|---|
| Có oracle Tier 1 cho **ngày Tết** không? | ✅ Có — văn bản nhà nước, truy cập online |
| Có oracle Tier 1 cho **lịch âm VN đầy đủ** không? | ⛔ **Chưa** — chỉ có sách in Trần Tiến Bình, chưa có trong tay |
| Có oracle Tier 1 cho **phía Trung Quốc** (để test VN≠TQ) không? | ✅ Có — HKO, open data |
| Có oracle cho **tháng nhuận / 29–30 ngày** của VN không? | ⛔ **Chưa** |
| Đã có hai nhánh độc lập cho bất kỳ vector nào chưa? | 🟡 Chỉ với ngày Tết (nhà nước + HKO cho ca VN≠TQ) |
