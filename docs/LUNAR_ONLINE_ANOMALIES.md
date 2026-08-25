# Nhật ký bất thường & phân loại discrepancy

> Phase 3A.5. Dev-only. Không có production code.
>
> Phân loại bắt buộc: **D1** engine bug · **D2** precision/rounding · **D3** VN vs TQ
> kinh tuyến · **D4** múi giờ lịch sử · **D5** lịch vùng miền lịch sử · **D6** khác
> biệt implementation của website · **D7** lỗi dữ liệu website · **D8** provenance
> không rõ · **D9** thiếu bằng chứng.

---

## A. Bug tìm thấy trong CHÍNH reference model — và cách tìm ra

Kiểm chứng cấu trúc với HKO đã bắt được **hai lỗi thật** mà không test nội bộ nào
phát hiện được. Đây là bằng chứng trực tiếp cho bài học Phase 2: *test xanh không
đồng nghĩa với đúng*.

### A1 — Đánh số tháng nhuận lệch 1 (D1, ĐÃ SỬA)

| | |
|---|---|
| Triệu chứng | 8/28 năm nhuận sai: HKO `th2N`, model `th3N` |
| Ngày bắt đầu tháng | **khớp 100%** — chỉ số tháng sai |
| Nguyên nhân | Tháng nhuận phải mang **số của tháng liền trước** (nhuận tháng 2 = lần thứ hai của tháng 2). Model gán số kế tiếp |
| Phát hiện bởi | Kiểm chứng cấu trúc HKO |
| Trạng thái | ✅ **ĐÃ SỬA** — sau sửa: 492/492 khớp |

### A2 — Thiếu tháng ở hai đầu dải (D1, ĐÃ SỬA)

| | |
|---|---|
| Triệu chứng | 1901 chỉ có 1/12 tháng; 2100 có 11/13 tháng |
| Nguyên nhân | Tháng 11 âm neo vào Đông chí; một khoảng neo cần Đông chí ở **cả hai đầu**. Dữ liệu Sóc và tiết khí chỉ có 1901–2100 nên hai đầu không được gán |
| Phát hiện bởi | Kiểm chứng cấu trúc HKO |
| Trạng thái | ✅ **ĐÃ SỬA** — sinh dư 30 tuần trăng và tiết khí 1899–2102 |
| **Bài học cho Phase 3** | Engine phải có **dữ liệu đệm ngoài phạm vi bảo đảm**, nếu không hai năm biên sẽ sai |

### A3 — Lỗi trong parser HKO của tôi, không phải model (D7-tự-gây)

| | |
|---|---|
| Triệu chứng | 7 ca "HKO th12N != model th12" |
| Nguyên nhân | Parser coi "số tháng đã xuất hiện trong năm dương" là nhuận. Nhưng **tháng 12 có thể xuất hiện hai lần trong một năm dương thuộc hai năm âm khác nhau** |
| Trạng thái | ✅ **ĐÃ SỬA** — nhuận = trùng số với tháng **liền trước** |
| Bài học | Khi engine và oracle lệch nhau, **oracle-reader cũng có thể là thủ phạm** |

### A4 — 3 lệch trước 1929: quy ước của LỊCH TRUNG QUỐC, không phải lỗi model (D6)

Chạy kiểm chứng cấu trúc trên **toàn bộ 200 năm** cho **2471/2474 = 99,88 %**. Ba
lệch còn lại, tất cả ở **1914, 1916, 1920**, đều dạng "model muộn hơn HKO đúng 1 ngày".

| | |
|---|---|
| Giả thuyết | Trước **1929**, lịch Trung Quốc quy chiếu theo **giờ địa phương Bắc Kinh** (kinh tuyến 116°25′Đ = **UTC+7h45m40s**), không phải UTC+8 |
| Kiểm chứng | Chạy lại 1914/1916/1920 với offset đó |
| Kết quả | **1 → 0 lệch** ở cả ba năm |
| Xác suất kỳ vọng | Chênh 14m20s / 1440 phút ≈ 0,995 %; số tháng 1901–1928 ≈ 346 ⇒ kỳ vọng **≈ 3,4 ca**. Quan sát: **3** |
| Phân loại | **D6** — khác biệt quy ước của lịch Trung Quốc |
| Ảnh hưởng lịch Việt Nam | **KHÔNG.** Việt Nam dùng 105°Đ theo QĐ 121-CP; đây là tham số của phía Trung Quốc |

⇒ Sau khi dùng đúng quy ước, kiểm chứng cấu trúc là **2474/2474 = 100 %**.

**Bài học:** khi engine và oracle lệch nhau, **tham số truyền cho oracle** cũng là một
nghi phạm — đây là lần thứ ba trong phase này (A1 model, A3 parser, A4 tham số).

---

## B. Discrepancy với lichviet.app

Tập đối chiếu **cố ý nạp nhiều ca khó**, không chọn theo cách dễ.

### B1 — Khớp hoàn toàn (14/14 ngoài tập sát biên)

| Ngày | Model | lichviet.app | Loại |
|---|---|---|---|
| 1901-01-15 | 25/11/1900 | 25/11 | biên dưới |
| 1955-06-10 | 20/4/1955 | 20/4 | lịch sử |
| 1968-01-29 | 1/1/1968 | 1/1 | Tết Mậu Thân |
| 1968-01-30 | 2/1/1968 | 2/1 | |
| 1976-03-03 | 3/2/1976 | 3/2 | lịch sử |
| 1985-01-21 | 1/1/1985 | 1/1 | **VN≠TQ** |
| 1985-02-20 | 1/2/1985 | 1/2 | **VN≠TQ** |
| 2007-02-17 | 1/1/2007 | 1/1 | **VN≠TQ** |
| 2025-07-25 | 1/6**N**/2025 | 1/6 **nhuận** | **tháng nhuận** |
| 2026-08-25 | 13/7/2026 | 13/7 | hôm nay |
| 2026-09-25 | 15/8/2026 | 15/8 | Trung Thu |
| 2030-02-02 | 1/1/2030 | 1/1 | **VN≠TQ** |
| 2098-07-04 | 6/6/2098 | 6/6 | gần biên trên |
| 2100-12-31 | 1/12/2100 | 1/12 | biên trên |

### B2 — Lệch: **toàn bộ nằm trong tập sát biên đã biết trước**

| Ngày | Model (ERFA) | Model (NASA) | lichviet.app | Loại |
|---|---|---|---|---|
| 1944-06-20 | 1/5 | **30/4N** | **30/4 nhuận** | **D2** |
| 1967-07-07 | 1/6 | **30/5** | **30/5** | **D2** |
| 2054-05-07 | 30/3 | 30/3 | **1/4** | **D2** |
| 2077-11-15 | **1/10** | 30/9 | **1/10** | **D2** |

**Nhận định quan trọng:** cả 4 ca đều nằm trong **6 điểm Sóc sát ranh giới** đã được
nhận diện từ Phase 3A.1, **trước khi** đối chiếu với bất kỳ website nào. Không có
discrepancy nào **ngoài** tập đó.

**Phân loại: D2 — precision/rounding. Không có D1.**

**INFERENCE thêm:** lichviet.app khớp biến thể **NASA** ở 1944 và 1967, nhưng khớp
biến thể **ERFA** ở 2077, và **lệch cả hai** ở 2054. ⇒ Nguồn Sóc của họ là **biến thể
thứ ba**, không trùng khít NASA hay ERFA. Củng cố kết luận ancestry **UNKNOWN**.

---

## C. VnExpress — LOẠI KHỎI ĐỐI CHIẾU vì robots.txt

**FACT.** `https://vnexpress.net/robots.txt` chứa:

```
User-agent: ClaudeBot
Disallow: /
User-agent: Claude-Web
Disallow: /
```

⇒ VnExpress **cấm rõ ràng** tác nhân của tôi. **Không khảo sát tiếp.**

### ⚠️ Lỗi trình tự của chính tôi — ghi lại để audit

Tôi tải `robots.txt` **và** trang `/lich-van-nien` trong **cùng một lệnh**, nên đã
tải trang trước khi đọc được kết quả robots. Đúng ra phải tải robots trước, đọc, rồi
mới quyết định.

Hai điểm phải nói rõ:

1. Đây là **một lần tải trang duy nhất**, không lặp lại, không crawl.
2. Tôi **không** dùng UA khác để lách lệnh cấm — làm vậy chính là "bypass access
   control" mà chỉ thị đã cấm.

**Hệ quả:** VnExpress **không nằm** trong ma trận đối chiếu. Ghi nhận thêm: trang đó
**không render ngày âm phía server** (dựng bằng JS), nên dù được phép cũng không dùng
được như black-box oracle nếu không chạy JS.

---

## D. Bảng tổng hợp phân loại

| Loại | Số ca | Ghi chú |
|---|---|---|
| **D1** engine bug | **2** | A1, A2 — **cả hai đã sửa**, không còn tồn đọng |
| **D2** precision/rounding | **4** | B2 — toàn bộ trong tập sát biên đã biết trước |
| **D3** VN vs TQ kinh tuyến | 0 sai | Model tái lập **đúng** cả 8 ca lệch đã tài liệu hoá |
| **D4/D5** múi giờ / vùng miền lịch sử | **0 quan sát được** | Nhưng **chưa kiểm được** — không có oracle cho 1954–1967 |
| **D6** khác biệt implementation/quy ước | 4 + 3 | B2 (website) và A4 (quy ước lịch TQ trước 1929, đã giải thích) |
| **D7** lỗi dữ liệu website | 0 xác nhận | Không đủ căn cứ quy lỗi cho ai |
| **D8** provenance không rõ | lichviet.app | Ancestry UNKNOWN, không nâng Tier |
| **D9** thiếu bằng chứng | 6 ca sát biên | Cần V1 để phân xử |

**Không còn discrepancy loại D1 chưa giải thích.**
