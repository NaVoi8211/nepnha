# Phase 3A.2 — Astronomical Engine & License Preflight

> **KHÔNG CÓ PRODUCTION CODE.** `app/` không bị đụng. Không Room, không UI, không
> dependency. Nghiên cứu 2026-08-25.
>
> Nhãn kết luận: **FACT** · **INFERENCE** · **HYPOTHESIS** · **VERIFIED** /
> **NOT VERIFIED** / **INDEPENDENTLY VERIFIED**.

**KẾT QUẢ: GATE VẪN ⛔ BLOCKED.** Nhưng G14 có bước tiến lớn — tìm được ứng viên
giấy phép **sạch hơn cả SOFA**. Chi tiết §A.

---

## A. SOFA — và một phát hiện tốt hơn: ERFA

### A.1 SOFA: không đọc được chính văn giấy phép

**NOT VERIFIED.** `iausofa.org/tandc.html` và `copyr.html` đều **404** ngày
2026-08-25. Chưa trích được nguyên văn điều khoản SOFA.

Theo chỉ đạo mục 5 và 6: **không kết luận "SOFA PASS"**, và không copy dòng code
SOFA nào.

### A.2 SOFA không có Mặt Trăng — ⛔ ĐÃ ĐÍNH CHÍNH

> **SAI. Xem [PHASE_3A3 §2](PHASE_3A3_ASTRONOMICAL_BENCHMARK.md).** ERFA có
> `eraMoon98`. Benchmark ở Phase 3A.3 cho thấy nó đủ chính xác (trung vị 18,9 s so
> với NASA). Phần dưới giữ lại để đối chiếu lịch sử sai lầm.

**FACT.** SOFA phủ lịch, thang thời gian, ephemeris, tuế sai–chương động, toạ độ —
**không có ephemeris Mặt Trăng**. Vị trí Trái Đất quanh Mặt Trời có qua `iau_EPV00`.

**INFERENCE.** ⇒ Dù giấy phép SOFA có sạch đi nữa, nó **chỉ giải được một nửa bài
toán**: trung khí (Mặt Trời). Điểm Sóc (Mặt Trăng) vẫn phải lấy nơi khác. Đây là
điều mà nếu chỉ đọc trang giới thiệu thì không thấy.

### A.3 ✅ ERFA — chính văn giấy phép đã đọc được

**FACT.** Đã tải và đọc nguyên văn `LICENSE` của ERFA
(*Essential Routines for Fundamental Astronomy*, NumFOCUS Foundation):

> This library is derived, **with permission**, from the International Astronomical
> Union's "Standards of Fundamental Astronomy" library… made distinct through
> different function and file names, **as set out in the SOFA license conditions**.
> The SOFA original… redistribution is permitted only in its unaltered state. **The
> ERFA version is not subject to this restriction.**

Điều khoản: **BSD 3-Clause** đầy đủ, ba điều kiện chuẩn (giữ thông báo bản quyền ở
bản source; in lại ở tài liệu bản binary; không dùng tên SOFA Board/IAU để quảng bá).

**FACT.** ERFA khuyến nghị ghi công là *"a library derived from SOFA, rather than
SOFA itself"*.

**INFERENCE.** ERFA phù hợp với Nếp Nhà hơn SOFA:

| Tiêu chí | SOFA | ERFA |
|---|---|---|
| Chính văn giấy phép đọc được? | ❌ 404 | ✅ **đã đọc** |
| Thương mại | chưa xác minh | ✅ BSD-3 |
| Phân phối lại | chỉ nguyên vẹn | ✅ tự do |
| Nghĩa vụ đổi tên routine | ta phải làm | ✅ **đã làm sẵn** |
| Android redistribution | chưa xác minh | ✅ BSD-3 không cản |
| Nghĩa vụ công bố source | — | ❌ **không có** (BSD ≠ GPL) |

### A.4 Routine cần dùng — đã kiểm có đủ

**VERIFIED** (tải trực tiếp từ `liberfa/erfa`):

| Routine | Vai trò | Kích thước |
|---|---|---|
| `epv00` | Trái Đất quanh Mặt Trời → hoàng kinh Mặt Trời | **150.627 B / 2.603 dòng** |
| `obl06` | Độ nghiêng hoàng đạo | 5.037 B |
| `nut06a` | Chương động | 6.567 B |
| `dat` | ΔAT = TAI−UTC | 11.826 B |
| `dtf2d` / `cal2jd` / `jd2cal` | Ngày giờ ↔ Julian Date | ~20 KB |
| `utctai` / `taitt` | UTC → TAI → TT | ~11 KB |
| `anp` | Chuẩn hoá góc | 3.558 B |

Không cần toàn bộ 257 file của ERFA.

### A.5 ⚠️ Vấn đề kỹ thuật: `epv00` quá lớn để port thẳng

**FACT.** `epv00.c` = 150 KB / 2.603 dòng, phần lớn là bảng hệ số.

**HYPOTHESIS — NOT VERIFIED.** Port thẳng sang Kotlin dạng mảng literal có nguy cơ
vượt **giới hạn 64 KB bytecode cho một method** (khởi tạo mảng nằm trong `<clinit>`).
Chưa thử biên dịch nên chưa khẳng định.

**INFERENCE.** Dù giả thuyết trên đúng hay sai, việc chép 2.600 dòng hệ số vào app
là lựa chọn tồi: khó audit, khó kiểm chứng, phình APK. Dẫn tới §D.

---

## B. NASA — độ chính xác và ΔT

### B.1 Bước 1 của chỉ đạo mục 3: NASA có dữ liệu chi tiết hơn phút không?

**FACT — trả lời: KHÔNG (theo những gì tìm được).** Trang theo năm
`phase2001gmt.html` ("Moon Phases: 2001 to 2025") **cũng chỉ có phút**. Không tìm
được bản nào của NASA có giây.

**INFERENCE.** ⇒ 5 ca `17:00 UT` **không giải được bằng cách lấy thêm chữ số từ
NASA**. Phải đi đường khác: nguồn Mặt Trăng khác, hoặc oracle có thẩm quyền.

### B.2 ΔT — mục 14 của chỉ đạo

**FACT.** Catalog NASA **có cột ΔT theo từng năm** (định dạng `00h01m`), 101 dòng
trong file 2001–2100.

**INFERENCE.** ⇒ ΔT có sẵn **cùng nguồn, cùng giấy phép** với điểm Sóc. Không phải
đi tìm nguồn ΔT thứ ba.

**FACT.** Thời điểm NASA công bố là **Universal Time (UT)** — chính là thang ta cần
để đổi sang giờ Việt Nam. Không phải TT.

**INFERENCE.** Chuỗi thang thời gian cho phần **Mặt Trăng** đơn giản hơn dự kiến:
`UT (NASA) → +7h → giờ VN`. Không cần ΔT cho bước này, vì NASA đã áp dụng rồi.

**HYPOTHESIS — NOT VERIFIED.** Với phần **Mặt Trời** (nếu ta tự tính bằng ERFA), thì
mới cần chuỗi đầy đủ `UTC → TAI → TT` và ΔT để quay về UT. Với **1901–1960 chưa có
UTC**, `eraDat` không định nghĩa ⇒ phải dùng bảng ΔT lịch sử. Chưa nghiên cứu tới nơi.
**Đây là một mục con còn hở của G14/G15.**

### B.3 Bước 2 & 3 của chỉ đạo mục 3 — chưa làm được

- **Bước 2** (xác minh 5 ca bằng nguồn độc lập): ⛔ **chưa làm được** — cần oracle
  Tier 1, tức sách Trần Tiến Bình. Bốn trong 5 ca (1944, 2054, 2077, 2085) nằm ngoài
  vùng phủ của văn bản nghỉ Tết.
- **Bước 3** (độ chính xác thật của catalog có đủ quyết định day-boundary không):
  ⛔ **chưa xác minh** — NASA không công bố sai số của bảng pha Mặt Trăng ở dạng tìm
  được.

⇒ **Chưa được thiết kế API cho vấn đề này.** Đúng chỉ đạo mục 13: chưa thêm
`InsufficientPrecision`.

---

## C. Nguồn thay thế — theo thứ tự ưu tiên của chỉ đạo

| Ưu tiên | Nguồn | Trạng thái sau 3A.2 |
|---|---|---|
| 1 | **IAU SOFA** | ❌ chính văn giấy phép **404**, không đọc được · ✅ nhưng dẫn tới **ERFA** |
| — | **ERFA** *(mới)* | ✅ **BSD-3, đã đọc chính văn, đủ routine** |
| 2 | Nguồn dữ liệu mở khác | ✅ NASA (Sóc + ΔT) · ❌ HKO không đủ (chỉ 3 năm có giờ tiết khí) |
| 3 | USNO | ❌ vẫn không phân giải DNS · nghi ngờ độ chính xác ~0,01° |
| 4 | VSOP87 | ⛔ **chưa nghiên cứu** — không được coi là tự do chỉ vì công bố khoa học |
| 5 | Xin phép Meeus | ⛔ **chưa liên hệ** — đúng chỉ đạo mục 16, chưa cần |

---

## D. Kiến trúc đề xuất — ERFA làm CÔNG CỤ, không phải THƯ VIỆN

**INFERENCE** từ A.2 + A.5 + B.2:

```
   [ NGOÀI app — chạy một lần trên máy dev ]

   ERFA (C, BSD-3)   ──►  bảng thời điểm 24 tiết khí, 1901–2100  (~4.800 mốc)
   NASA catalog      ──►  bảng thời điểm Sóc, 1901–2100          (~2.474 mốc)
                     ──►  bảng ΔT theo năm                        (~200 mốc)
                                    │
                                    ▼  asset, vài chục KB
   [ TRONG app — Kotlin thuần, không C, không NDK, không JNI ]

   quy tắc lịch Việt Nam (105°Đ · Sóc đầu tháng · trung khí · tháng nhuận)
                                    │
                                    ▼
                        VietnameseLunarCalendar
```

Vì sao hướng này tốt hơn nhúng thư viện:

| | |
|---|---|
| Ràng buộc dự án | Giữ app thuần Kotlin, không NDK — đúng tinh thần "đơn giản, nhẹ, dễ debug" |
| Rủi ro `epv00` 64 KB | **Biến mất hoàn toàn** |
| Kích thước | ~7.300 timestamp ≈ vài chục KB, thay vì port 150 KB source |
| Tất định | Bảng cố định ⇒ không phụ thuộc dấu phẩy động của từng thiết bị |
| Auditability | Bảng dữ liệu có thể **in ra đối chiếu bằng mắt** với lịch giấy |
| Kiểm chứng pipeline | Tiết khí ta sinh ra đối chiếu được với **3 năm HKO có công bố giờ** |

**FACT (giấy phép).** BSD-3 cho phép dùng phần mềm để sinh dữ liệu. Vẫn ghi công
theo khuyến nghị của ERFA.

> ⚠️ Nhận định kỹ thuật, **không phải ý kiến pháp lý**.

**NOT VERIFIED — phải làm trước khi chốt:**

1. Biên dịch ERFA trên macOS Intel.
2. Sinh thử bảng tiết khí một năm, đối chiếu 3 năm HKO có giờ.
3. Đo kích thước asset thật.
4. Giải bài toán ΔT cho 1901–1960 (§B.2).

---

## E. Decision matrix

| Ứng viên | Thương mại | Độ chính xác | Phủ 1901–2100 | Android | Provenance | Kết luận |
|---|---|---|---|---|---|---|
| **NASA Moon Phases** | ✅ permission rõ, kèm ghi công | ⚠️ **chỉ tới phút** — 5 ca mơ hồ | ✅ đã kiểm, 2.474 mốc | ✅ dữ liệu, không phải code | ✅ FACT | ✅ **Dùng cho điểm Sóc + ΔT** |
| **ERFA** | ✅ **BSD-3, đã đọc chính văn** | ✅ chuẩn IAU | ✅ | ⚠️ C — dùng **ngoài app** làm công cụ | ✅ FACT | ✅ **Dùng sinh bảng tiết khí** |
| **IAU SOFA** | ❓ chính văn 404 | ✅ | ✅ | ⚠️ C | ⚠️ NOT VERIFIED | ⛔ Không dùng — dùng ERFA thay |
| **HKO — bảng 1901–2100** | ✅ data.gov.hk | — | ✅ | ✅ | ✅ FACT | ✅ **Chỉ làm oracle phía Trung Quốc** |
| **HKO — giờ tiết khí** | ✅ | ✅ | ❌ **chỉ 3 năm** | — | ✅ | ⛔ Không đủ làm data source · ✅ dùng **kiểm chứng pipeline** |
| **USNO** | ❓ | ⚠️ ~0,01° ≈ 15 phút | ❓ | — | ⚠️ DNS không tới | ⛔ Chưa xác minh được |
| **VSOP87** | ❓ | ✅ | ✅ | — | ⛔ chưa nghiên cứu | ⛔ Chưa xét — chỉ dùng nếu ERFA hỏng |
| **Meeus** | ⛔ cần xin phép | ✅ | ✅ | — | ✅ FACT (là cấm) | ⛔ Phương án cuối, chưa liên hệ |

---

## F. Gate matrix sau 3A.2

| # | Tiêu chí | Sau 3A.1 | Sau 3A.2 | Ghi chú |
|---|---|---|---|---|
| G1–G4 | HND / Meeus / phạm vi | ✅ PASS | ✅ PASS | |
| G5 | Oracle độc lập | ⛔ | ⛔ **BLOCKED** | Cần sách Trần Tiến Bình |
| G6 | Tháng nhuận | ⛔ | ⛔ **BLOCKED** | |
| G7 | 29/30 ngày | ⛔ | ⛔ **BLOCKED** | |
| G8 | VN ≠ TQ | 🟡 | 🟡 **PARTIAL** | 1985 PASS; 7 năm còn lại chưa |
| G9 | Múi giờ lịch sử | ⛔ | ⛔ **BLOCKED** | Có thêm FACT về 121-CP, nhưng 1954–1967 vẫn không giải được |
| G10 | Biên 1901/2100 | ⛔ | ⛔ **BLOCKED** | |
| G11–G13 | Meeus loại / model / schema | ✅ PASS | ✅ PASS | |
| **G14** | Nguồn công thức Mặt Trời | ⛔ | 🟡 **PARTIAL — tiến bộ lớn** | ERFA BSD-3 đã xác minh; còn phải chứng minh **biên dịch + sinh bảng + đối chiếu** |
| **G15** | Mô hình độ chính xác | 🟡 | 🟡 **PARTIAL** | Đã biết NASA không có bản mịn hơn; **chưa** xác minh được 5 ca bằng oracle độc lập |
| **G16** | **ΔT cho 1901–1960** *(mới)* | — | ⛔ **BLOCKED (mới)** | `eraDat` không định nghĩa trước 1960; cần bảng ΔT lịch sử cho phần tính Mặt Trời |

### ⛔ GATE VẪN BLOCKED

**Không chuyển G14 → PASS chỉ vì tìm thấy ERFA**, đúng chỉ đạo mục 20. ERFA mới giải
được câu hỏi *giấy phép*; chưa giải được câu hỏi *chạy được và ra số đúng*.

---

## G. Việc tiếp theo — đề xuất

| # | Việc | Ai | Mở khoá |
|---|---|---|---|
| 1 | Biên dịch ERFA trên máy dev, sinh thử bảng tiết khí 1 năm, đối chiếu 3 năm HKO | Tôi, khi được duyệt | G14 |
| 2 | Nghiên cứu ΔT lịch sử 1901–1960 | Tôi, khi được duyệt | G16 |
| 3 | **Sách Trần Tiến Bình** | **Chủ dự án** | G5, G6, G7, G10, và 5 ca của G15 |
| 4 | Toàn văn 121-CP / lịch nhà nước 1954–1967 | Cả hai | G9 |
| 5 | Bảng HKO cho 7 năm VN≠TQ còn lại | Tôi, khi được duyệt | G8 |

Việc **#1 rẻ và nhanh** — chỉ biên dịch một thư viện C rồi chạy, không đụng `app/`.
Nếu bước đó thành công thì G14 gần như xong.
