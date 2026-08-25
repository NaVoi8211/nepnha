# Provenance của nguồn thiên văn

> Phase 3A.2, nghiên cứu 2026-08-25. Không có production code.
>
> Mọi kết luận dán nhãn: **FACT** (chứng minh trực tiếp từ nguồn) · **INFERENCE**
> (suy ra từ FACT) · **HYPOTHESIS** (chưa chứng minh) · **VERIFIED** / **NOT
> VERIFIED** / **INDEPENDENTLY VERIFIED**.

---

## 1. IAU SOFA

**FACT.** SOFA (Standards of Fundamental Astronomy) là thư viện chuẩn của Liên đoàn
Thiên văn Quốc tế, phủ: lịch, thang thời gian, ephemeris, tuế sai–chương động,
chuyển đổi toạ độ.

**FACT.** SOFA **không có ephemeris Mặt Trăng.** Muốn vị trí Mặt Trăng phải dùng
nguồn khác (JPL DE…). Với Trái Đất/Mặt Trời, SOFA có `iau_EPV00`.

**INFERENCE.** ⇒ SOFA (và ERFA) giải được **trung khí** (cần hoàng kinh Mặt Trời),
nhưng **không giải được điểm Sóc**. Điểm Sóc vẫn phải lấy từ NASA hoặc nguồn Mặt
Trăng khác.

**NOT VERIFIED.** Chính văn giấy phép SOFA: `iausofa.org/tandc.html` và `copyr.html`
đều trả **404** ngày 2026-08-25; trang chủ tải được nhưng chưa trích được nguyên văn
điều khoản. **Chưa đọc được chính văn ⇒ không kết luận gì về SOFA.**

---

## 2. ERFA — ứng viên mạnh nhất

**FACT.** ERFA (*Essential Routines for Fundamental Astronomy*) là bản phái sinh của
SOFA. Đã tải và đọc **nguyên văn** file `LICENSE`:

> Copyright (C) 2013-2021, NumFOCUS Foundation. All rights reserved.
>
> This library is derived, **with permission**, from the International Astronomical
> Union's "Standards of Fundamental Astronomy" library, available from
> http://www.iausofa.org.
>
> The ERFA version is intended to retain identical functionality to the SOFA
> library, but made distinct through different function and file names, **as set out
> in the SOFA license conditions**. The SOFA original has a role as a reference
> standard for the IAU and IERS, and consequently **redistribution is permitted only
> in its unaltered state**. The ERFA version is **not subject to this restriction**
> and therefore can be included in distributions which do not support the concept of
> "read only" software.

**FACT.** Điều khoản ERFA là **BSD 3-Clause** nguyên văn:

1. Bản phân phối dạng source phải giữ thông báo bản quyền, danh sách điều kiện và
   disclaimer.
2. Bản phân phối dạng binary phải in lại các thứ trên trong tài liệu kèm theo.
3. Không được dùng tên SOFA Board, IAU hay tên người đóng góp để quảng bá sản phẩm
   phái sinh nếu không có văn bản cho phép.

**FACT.** ERFA khuyến nghị cách ghi công:

> If you wish to acknowledge the SOFA heritage, please acknowledge that you are using
> **a library derived from SOFA, rather than SOFA itself**.

**INFERENCE.** ERFA sạch hơn SOFA cho mục đích của Nếp Nhà, vì ba lý do:

| | SOFA | ERFA |
|---|---|---|
| Phân phối lại | chỉ ở trạng thái **nguyên vẹn** | không bị ràng buộc đó |
| Đổi tên routine khi sửa | **ta phải tự làm** | **đã làm sẵn** (`era*`) |
| Dạng giấy phép | điều kiện riêng, phải đọc kỹ | **BSD-3-Clause** quen thuộc |
| Thương mại | cần đọc chính văn | ✅ BSD-3 cho phép |

**VERIFIED.** Đã kiểm ERFA có đủ routine cần:

| Routine | Dùng để | Có? | Kích thước |
|---|---|---|---|
| `epv00` | Vị trí Trái Đất quanh Mặt Trời → hoàng kinh Mặt Trời | ✅ | **150.627 B / 2.603 dòng** |
| `obl06` | Độ nghiêng hoàng đạo | ✅ | 5.037 B |
| `nut06a` | Chương động | ✅ | 6.567 B |
| `dat` | ΔAT = TAI−UTC (giây nhuận) | ✅ | 11.826 B |
| `dtf2d`, `cal2jd`, `jd2cal` | Ngày giờ ↔ Julian Date | ✅ | ~20 KB |
| `utctai`, `taitt` | UTC → TAI → TT | ✅ | ~11 KB |
| `anp` | Chuẩn hoá góc | ✅ | 3.558 B |

**FACT.** `epv00.c` **150 KB / 2.603 dòng**, phần lớn là bảng hệ số chuỗi lượng giác.

**HYPOTHESIS — cần kiểm ở phase sau.** Port thẳng `epv00` sang Kotlin dưới dạng mảng
literal có nguy cơ vượt **giới hạn 64 KB bytecode cho một method** của JVM/Dalvik
(khởi tạo mảng lớn nằm trong `<clinit>`). **NOT VERIFIED** — chưa thử biên dịch.
Nhưng đây là một trong những lý do dẫn tới đề xuất kiến trúc ở §5.

---

## 3. NASA / GSFC Moon Phases

**FACT** (Phase 3A.1, đã kiểm bằng dữ liệu): phủ 1901–2100 trong 2 file, 2.474 điểm
Sóc, thang **Universal Time**, độ phân giải **phút**, cho phép tái sử dụng kèm ghi
công `Moon Phase Predictions by Fred Espenak, NASA/GSFC`.

**FACT.** Catalog **có cột ΔT theo từng năm** (định dạng `00h01m`), 101 dòng ΔT trong
file 2001–2100. ⇒ ΔT có sẵn **cùng nguồn, cùng giấy phép**.

**FACT.** Tìm bản độ phân giải cao hơn phút: trang theo năm
`phase2001gmt.html` ("Moon Phases: 2001 to 2025") **cũng chỉ có phút**, không có
giây. **Chưa tìm được nguồn NASA nào có giây.**

**INFERENCE.** ⇒ 5 ca "đúng 17:00 UT" ở Phase 3A.1 **không thể giải bằng cách lấy
thêm chữ số từ NASA**. Phải giải bằng đường khác — xem §5 và
[PHASE_3A2_ASTRONOMICAL_PREFLIGHT.md §B](PHASE_3A2_ASTRONOMICAL_PREFLIGHT.md).

---

## 4. Các nguồn khác — chưa nghiên cứu tới nơi

| Nguồn | Trạng thái |
|---|---|
| **USNO** *Approximate Solar Coordinates* | **NOT VERIFIED** — `aa.usno.navy.mil` vẫn không phân giải DNS. Và nếu độ chính xác ~0,01° (~15 phút thời gian) là đúng thì **không đủ** cho quy tắc trung khí |
| **VSOP87** | **NOT VERIFIED** — chưa tra provenance/giấy phép. Theo chỉ đạo: không được coi là tự do chỉ vì được công bố trên tạp chí khoa học |
| **Meeus / Willmann-Bell** | **BLOCKED** — cần xin phép bằng văn bản. Là phương án cuối, chưa liên hệ |

---

## 5. Kiến trúc đề xuất — hệ quả trực tiếp của phần trên

**INFERENCE.** Ba sự thật gộp lại cho một kết luận kiến trúc:

1. ERFA giải được Mặt Trời nhưng **không giải được Mặt Trăng**.
2. `epv00` quá lớn để port thẳng sang Kotlin một cách an toàn.
3. Ta chỉ cần **~2.400 thời điểm trung khí** cho toàn bộ 1901–2100 — một tập hữu hạn,
   tính một lần là xong.

⇒ **Dùng ERFA như CÔNG CỤ SINH DỮ LIỆU ngoài app, không đưa ERFA vào app.**

```
   [ ngoài app, chạy một lần trên máy dev ]
   ERFA (C, BSD-3)  ──►  bảng thời điểm 24 tiết khí 1901–2100
                              │
   NASA catalog     ──►  bảng thời điểm Sóc 1901–2100  +  ΔT
                              │
                              ▼
   [ trong app: Kotlin thuần ]
   asset dữ liệu  ──►  quy tắc lịch Việt Nam (105°Đ, tháng nhuận, trung khí)
                  ──►  VietnameseLunarCalendar
```

Lợi ích:

- App **không có C, không NDK, không JNI** — giữ đúng ràng buộc đơn giản của dự án.
- **Tránh hẳn** rủi ro giới hạn 64 KB method của `epv00`.
- Dữ liệu ~7.300 timestamp ≈ **vài chục KB** asset.
- Tất định tuyệt đối: cùng input luôn cùng output, không phụ thuộc dấu phẩy động của
  từng thiết bị.
- **Kiểm chứng được**: thời điểm tiết khí ta sinh ra có thể đối chiếu với 3 năm mà
  HKO công bố kèm giờ — một phép kiểm độc lập cho chính pipeline sinh dữ liệu.

**FACT (giấy phép).** BSD-3 cho phép dùng phần mềm để sinh dữ liệu; **dữ liệu kết
quả không phải tác phẩm phái sinh của mã nguồn ERFA**. Dù vậy vẫn nên ghi công theo
khuyến nghị của chính ERFA.

> ⚠️ **Đây là nhận định kỹ thuật của tôi, không phải ý kiến pháp lý.** Với sản phẩm
> thương mại, nếu chủ dự án muốn chắc chắn tuyệt đối thì cần luật sư xác nhận.

**NOT VERIFIED — việc còn phải làm trước khi chốt:**

- Biên dịch được ERFA trên máy dev (macOS Intel) hay chưa.
- Sinh thử bảng tiết khí một năm rồi đối chiếu với HKO.
- Kích thước asset thật sau khi nén.
