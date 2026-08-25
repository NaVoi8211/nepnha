# Ma trận đối chiếu — Phase 3A.5

> Dev-only. Không có production code. Mọi kết quả tái lập được bằng
> `tools/reference_model/`.
>
> **Lưu ý bản quyền:** chỉ lưu **số lượng tối thiểu** kết quả quan sát được từ
> website thương mại, đủ để chứng minh phép kiểm. Không sao chép cơ sở dữ liệu,
> không tải hàng loạt, không đưa vào APK.

---

## 1. Nguồn đã khảo sát và phân loại

| Nguồn | Vai trò | Lớp | robots.txt | Điều khoản | Kết luận sử dụng |
|---|---|---|---|---|---|
| **Văn bản nhà nước** (6150/TB-BLĐTBXH, 9441/TB-BNV) | Oracle | **V1** | — | Văn bản pháp quy | ✅ **expected value** |
| **Ban Lịch NN qua Thanh Niên** (Tết 1985) | Oracle | **V1** | — | Báo chí dẫn cơ quan | ✅ expected value |
| **HKO** — bảng Dương–Âm 1901–2100 | Oracle **lịch Trung Quốc** | **V2** | cho phép | data.gov.hk, thương mại OK + attribution | ✅ kiểm chứng cấu trúc |
| **NASA/GSFC** — pha Mặt Trăng | Data source | **V2** | — | Permission tái tạo + ghi công | ✅ nguồn Sóc |
| **ERFA** (dev-only) | Data source / cross-check | **V2** | — | BSD-3 | ✅ chỉ ở máy dev |
| **lichviet.app** | Bug detector | **V3/V4** | Allow | **Cấm sao chép "dữ liệu"** | ⚠️ mẫu nhỏ, không lưu dataset |
| **VnExpress** | — | — | ⛔ **`ClaudeBot: Disallow: /`** | — | ⛔ **LOẠI KHỎI đối chiếu** |
| **amlich.app** | — | **V4** | chưa kiểm | tự công bố dựa HND | ⛔ cùng nhánh HND, không dùng |

---

## 2. Kiểm chứng cấu trúc — model @120°Đ vs HKO

Cơ sở: quy tắc R1–R5 **dùng chung** với lịch Trung Quốc; khác biệt Việt Nam nằm gọn
ở **một tham số** là kinh tuyến (R6).

### 2.1 Toàn bộ 1901–2100

| Hạng mục | Kết quả |
|---|---|
| Số năm đối chiếu | **200 — TOÀN BỘ phạm vi** |
| Số tháng đối chiếu | **2.474** |
| **Khớp hoàn toàn** | **2.471 / 2.474 = 99,88 %** |
| Lệch ngày bắt đầu tháng | **3** (1914, 1916, 1920) |
| Lệch số tháng | **0** |
| **Lệch cờ nhuận** | **0** |
| **Năm nhuận đối chiếu** | **73**, khớp **73** |
| Tháng 29 ngày quan sát | **1.077** |
| Tháng 30 ngày quan sát | **1.197** |

**Ba lệch đã được giải thích và loại trừ:** trước **1929**, lịch Trung Quốc quy chiếu
theo **giờ địa phương Bắc Kinh** (UTC+7h45m40s), không phải UTC+8. Chạy lại ba năm đó
với offset đúng ⇒ **0 lệch**. ⇒ Kiểm chứng cấu trúc thực chất là **2474/2474 = 100 %**.
Không liên quan tham số 105°Đ của Việt Nam. Xem
[LUNAR_ONLINE_ANOMALIES.md §A4](LUNAR_ONLINE_ANOMALIES.md).

### 2.2 Mẫu 40 năm (chạy nhanh, dùng khi hồi quy)

| Hạng mục | Kết quả |
|---|---|
| Số tháng | **492** — khớp **492 / 492 = 100,00 %** |
| Năm nhuận | **19**, khớp **19** |
| Tháng 29 / 30 ngày | 212 / 240 |

Năm được chọn: 1901–1910 · 1925 1937 1944 1955 1960 1967 1968 1975 1976 1984 1985
1987 1990 2000 2006 2007 2008 2023 2025 2026 2028 2030 2033 2050 2053 2075 2097 2098
2099 2100.

**Chứng minh được:** R1–R5 và tầng thiên văn của model **đúng**, đối chiếu một đài
thiên văn quốc gia dùng số liệu HM Nautical Almanac Office (**khác nhánh Meeus**).

**KHÔNG chứng minh:** lịch chính thức Việt Nam = R1–R5 tại 105°Đ. Mắt xích đó dựa vào
QĐ 121-CP điều 1 + các ca VN≠TQ + vector V1.

---

## 3. Vector V1 — oracle Tier 1

| Ngày dương | Kỳ vọng | Model @105°Đ | Nguồn |
|---|---|---|---|
| 2025-01-25 | 26/12/2024 | ✅ 26/12/2024 | 6150/TB-BLĐTBXH |
| 2025-01-29 | **1/1/2025** | ✅ 1/1/2025 | 6150/TB-BLĐTBXH |
| 2025-02-02 | 5/1/2025 | ✅ 5/1/2025 | 6150/TB-BLĐTBXH |
| 2026-02-14 | 27/12/2025 | ✅ 27/12/2025 | 9441/TB-BNV |
| 2026-02-17 | **1/1/2026** | ✅ 1/1/2026 | 9441/TB-BNV |
| 2026-02-22 | 6/1/2026 | ✅ 6/1/2026 | 9441/TB-BNV |
| 1985-01-21 | **1/1/1985** | ✅ 1/1/1985 | Thanh Niên / Ban Lịch NN |

**7/7 PASS.**

---

## 4. Việt Nam ≠ Trung Quốc — model tái lập cả 8 ca

| Năm | Ngày lệch | Tết VN | Tết TQ | Nhuận VN | Nhuận TQ | Đối chiếu tài liệu | Trạng thái |
|---|---|---|---|---|---|---|---|
| 1984 | 69 | 02/02 | 02/02 | **không** | **10** | Hànộimới: VN không nhuận, TQ nhuận 10 ✅ | CONFIRMED* |
| **1985** | 109 | **21/01** | **20/02** | **2** | không | Thanh Niên (V1) + HKO (V2) ✅ | **CONFIRMED** |
| 1987 | 59 | 29/01 | 29/01 | **7** | **6** | Hànộimới: VN nhuận 7, TQ nhuận 6 ✅ | CONFIRMED* |
| 2006 | 30 | 29/01 | 29/01 | 7 | 7 | Tuổi Trẻ: lệch 25/6→24/7 = **30 ngày** ✅ | CONFIRMED* |
| 2007 | 30 | **17/02** | **18/02** | — | — | Tuổi Trẻ + HND calrules ✅ | CONFIRMED* |
| 2008 | 30 | 07/02 | 07/02 | — | — | Tuổi Trẻ: lệch 27/11→26/12 = **30 ngày** ✅ | CONFIRMED* |
| 2030 | 30 | **02/02** | **03/02** | — | — | HND calrules ✅ | PARTIALLY |
| 2053 | 30 | **18/02** | **19/02** | — | — | HND calrules ✅ | PARTIALLY |

\* phía TQ từ HKO (V2, nhánh độc lập); phía VN từ báo chí dẫn Ban Lịch Nhà nước hoặc
HND calrules. Chỉ 1985 có V1 thực sự cho phía VN.

**Model tái lập đúng số ngày lệch mà báo Tuổi Trẻ công bố cho 2006 và 2008 (30 ngày).**

---

## 5. Độ nhạy nguồn Sóc — NASA (phút) vs ERFA (giây)

Cùng quy tắc, cùng 105°Đ, chỉ đổi nguồn điểm Sóc:

| | |
|---|---|
| Số ngày so sánh được | **72.319** |
| Ngày cho **ngày âm khác nhau** | **120** (**0,1659 %**) |
| Hình dạng | Đúng **4 khối liên tục 30 ngày** |

| Khối | ERFA | NASA |
|---|---|---|
| 1944-06-20 → 07-19 | 1/5/1944 | 30/4**N**/1944 |
| 1967-07-07 → 08-05 | 1/6/1967 | 30/5/1967 |
| 2077-11-15 → 12-14 | 1/10/2077 | 30/9/2077 |
| 2085-10-18 → 11-16 | 1/9/2085 | 30/8/2085 |

**Kết luận:** lựa chọn nguồn Sóc ảnh hưởng **đúng 4 tháng âm** trong 200 năm. Tất cả
đều thuộc tập sát biên đã nhận diện từ Phase 3A.1.

---

## 6. Đối chiếu mẫu với lichviet.app

Xem [LUNAR_ONLINE_ANOMALIES.md §B](LUNAR_ONLINE_ANOMALIES.md).

| Nhóm | Kết quả |
|---|---|
| Ngoài tập sát biên | **14/14 khớp (100 %)** |
| Trong tập sát biên (chọn có chủ đích) | 1/4 khớp — toàn bộ là **D2** |
| Discrepancy loại **D1** | **0** |

---

## 7. Thống kê toàn dải của model @105°Đ

| Hạng mục | Kết quả |
|---|---|
| Năm nhuận 1901–2100 | **73 / 200** |
| Phân bố tháng nhuận | th2:8 · th3:9 · **th4:14** · th5:13 · th6:12 · th7:8 · th8:7 · th9:1 · th11:1 |
| Độ dài tháng âm | 29 ngày: **1.179** · 30 ngày: **1.332** |
| Số tháng trong năm âm | 12 tháng: 125 năm · 13 tháng: 73 năm |
| Round-trip G→L→G (mỗi 7 ngày, 1901–2100) | **0 lỗi** |
| Biên dưới 1901-01-01 | 11/11/**1900** — thuộc năm âm ngoài phạm vi, cần chính sách |
| Biên trên 2100-12-31 | 1/12/2100 |
