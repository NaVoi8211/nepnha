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

> ⛔ **ĐÍNH CHÍNH (Phase 3A.3, 2026-08-25):** đoạn dưới đây **SAI**. ERFA/SOFA **CÓ**
> routine Mặt Trăng: `eraMoon98` (`moon98.c`, 23.973 byte). Sai vì tôi tra danh sách
> chức năng tổng quát thay vì liệt kê thư mục source. Xem
> [PHASE_3A3_ASTRONOMICAL_BENCHMARK.md §2](PHASE_3A3_ASTRONOMICAL_BENCHMARK.md).
> Lưu ý thêm: `eraMoon98` là implementation **thuật toán Meeus** — vấn đề chính sách,
> xem §4.2 của tài liệu đó.

**FACT ~~(SAI, đã đính chính ở trên)~~.** SOFA **không có ephemeris Mặt Trăng.** Muốn vị trí Mặt Trăng phải dùng
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

**FACT (3A.4) — bằng chứng primary source về quyền relicense.** File `INFO` của ERFA
chứa **nguyên văn email cho phép** của Chủ tịch IAU SOFA Board:

> The IAU Standards Of Fundamental Astronomy Board approves the relicensing of a
> changed SOFA library by the NumFOCUS Foundation to use a "Three Clause BSD"
> license… all function names shall change to use "era" as a prefix in place of
> "iau", and… the SOFA Board shall be removed as a copyright holder.
>
> — Catherine [Hohenkerk], Chair, IAU SOFA Board, HM Nautical Almanac Office, UKHO

`README.rst` xác nhận: *"Permission for this release has been obtained from the SOFA
board."*

**FACT (3A.4) — inventory Mặt Trăng, quét toàn bộ 251 file:**

| | |
|---|---|
| Ephemeris Mặt Trăng trong ERFA | **đúng một: `moon98.c`** |
| Routine Mặt Trăng thay thế | **không có** (`faf03/fad03/fal03/faom03` chỉ là đối số cơ bản cho chương động) |
| File nhắc tới Meeus | **đúng một: `moon98.c`** |

⇒ Vết Meeus **gói gọn trong một file**. Nhánh Mặt Trời sạch hoàn toàn.

⚠️ **LEGAL UNCERTAINTY:** phép của SOFA Board nói về quyền của **SOFA**, không nói gì
về quyền của Willmann-Bell với thuật toán Meeus mà `moon98` hiện thực. Xem
[PHASE_3A_NEXT_GATE.md §A.2](PHASE_3A_NEXT_GATE.md).

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

## 2b. ⛔ ĐÍNH CHÍNH (Phase 3A.5) — NASA và ERFA KHÔNG độc lập về thuật toán

**FACT.** NASA: *"The phases of the Moon… are based on Jean Meeus' Astronomical
Algorithms."* **FACT.** ERFA `moon98`: *"a full implementation of the algorithm
published by Meeus"*, p337.

**INFERENCE.** Benchmark ERFA↔NASA ở Phase 3A.3 là **implementation/institutional
cross-check**, **KHÔNG** phải independent astronomical-method proof. Bốn loại độc lập
phải tách: thuật toán ❌ · hiện thực ✅ · nguồn dữ liệu ❓ · thể chế ✅.

Benchmark **tiết khí** với HKO vẫn đứng vững — HKO dùng số liệu HM Nautical Almanac
Office, khác nhánh Meeus.

## 2c. 🆕 JPL DE440 — nguồn Mặt Trăng KHÔNG thuộc nhánh Meeus

**FACT.** DE440/DE441 sinh bằng **tích phân số quỹ đạo khớp quan trắc**, không phải
chuỗi giải tích của Meeus. **FACT.** DE440 phủ **1550–2650** ⇒ trọn 1901–2100.

**FACT (naif.jpl.nasa.gov/naif/rules.html):** *"Use of SPICE components in commercial
products is allowed… No fees or licensing are required."* · *"Redistribution of SPICE
kernels distributed by NAIF is permitted as long as they have not been modified."* ·
ghi công *"encouraged"*.

⛔ **Đính chính trong chính phase này:** kết quả tìm kiếm nói SPICE là *"public
domain"* — **trang gốc NAIF nói KHÔNG**: SPICE được xếp **TSPA**, không phải public
domain. Dùng phát biểu của nguồn gốc.

**NOT VERIFIED:** chưa tải kernel, chưa benchmark. Là **ứng viên**, không phải kết luận.

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

## 3b. NASA — TERMS VERIFIED (Phase 3A.5)

**FACT** (chính sách bản quyền NASA mà trang catalog trỏ tới):

> "NASA material is not protected by copyright unless noted… **If not copyrighted,
> NASA material may be reproduced and distributed without further permission from
> NASA.**"

Kết hợp với permission riêng của catalog ⇒ **TERMS VERIFIED** cho việc dùng dữ liệu
Sóc của NASA trong sản phẩm thương mại, **kèm ghi công bắt buộc**
`Moon Phase Predictions by Fred Espenak, NASA/GSFC`.

Rủi ro tồn dư: dữ liệu *"based on Meeus"* — **LEGAL-UNKNOWN** ở lớp thuật toán. Xem
[PHASE_3A5_FINAL_PROVENANCE_GATE.md §A](PHASE_3A5_FINAL_PROVENANCE_GATE.md).

## 3c. ERFA dev-only — nghĩa vụ gắn vào PHÂN PHỐI

**INFERENCE.** BSD-3 đặt điều kiện lên *"redistribution and use in source and binary
forms"*. Điều 1 và 2 chỉ kích hoạt khi **phân phối** ERFA — ta không phân phối. Điều 3
(không dùng tên SOFA Board/IAU để quảng bá) luôn áp dụng và ta tuân thủ. Giấy phép
**không nói gì** về đầu ra của việc chạy phần mềm.

⚠️ **LEGAL-UNKNOWN** nếu cần chắc chắn tuyệt đối.

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
