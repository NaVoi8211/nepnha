# Phase 3A.5 — Final provenance & NASA-first policy gate

> **KHÔNG CÓ PRODUCTION CODE.** `app/` không bị đụng · không Kotlin · không Room ·
> không UI · không dependency · không native code · không network trong app.
>
> Nhãn: **FACT** · **VERIFIED** · **INFERENCE** · **HYPOTHESIS** · **UNVERIFIED** ·
> **LEGAL-UNKNOWN** · **NOT INDEPENDENTLY VERIFIED**.
>
> ⚠️ Tài liệu này là **đánh giá kỹ thuật và provenance. KHÔNG phải tư vấn pháp lý.**

# 🔴 FINAL GATE: **BLOCKED**

Nhưng blocker đã đổi bản chất. Ba trong bốn blocker cũ **không còn cần cuốn sách**, và
phase này tìm được **một đường kiểm chứng có thể thực hiện ngay** (§8) cùng **bằng
chứng mạnh nhất từ trước tới nay** cho G9 (§7).

---

## A1. NASA — TERMS VERIFIED

**FACT** (trang catalog, nguyên văn):

> "Permission is freely granted to reproduce this data when accompanied by an
> acknowledgment: *'Moon Phase Predictions by Fred Espenak, NASA/GSFC'*"

**FACT** (trang chính sách bản quyền NASA mà catalog trỏ tới):

> "NASA material is not protected by copyright unless noted. If copyrighted,
> permission should be obtained from the copyright owner prior to use. **If not
> copyrighted, NASA material may be reproduced and distributed without further
> permission from NASA.**"

| Câu hỏi | Trả lời | Nhãn |
|---|---|---|
| Dùng thương mại được không? | Chính sách không phân biệt thương mại/phi thương mại; cho phép tái tạo và phân phối | **TERMS VERIFIED** |
| Sinh dataset cho app được không? | Có — đó chính là "reproduce this data" | **TERMS VERIFIED** |
| Nghĩa vụ ghi công | **Bắt buộc**: `Moon Phase Predictions by Fred Espenak, NASA/GSFC` | **FACT** |
| Có phải giữ notice trong repo không? | Chính sách không yêu cầu notice trong repo, chỉ yêu cầu **acknowledgment khi tái tạo dữ liệu** | **INFERENCE** — ta sẽ ghi ở cả repo lẫn app cho chắc |

**Rủi ro còn lại:** dữ liệu NASA *"based on Meeus"*. Việc NASA có quyền cấp phép cho
đầu ra của họ hay không **không phải là điều Nếp Nhà xác định được** — nhưng đây là
**tuyên bố cho phép trực tiếp từ chính bên công bố dữ liệu ta dùng**. **LEGAL-UNKNOWN**
ở lớp đó; ta không thể làm tốt hơn ngoài việc ghi nhận.

## A2. ERFA — dev-only

**FACT.** LICENSE là BSD-3-Clause, bản quyền NumFOCUS Foundation.
**FACT.** `INFO` chứa nguyên văn email cho phép relicense của Chủ tịch IAU SOFA Board.
**FACT.** `moon98.c` nằm trong cùng cây source, cùng một LICENSE — **không có điều
kiện bổ sung riêng nào cho file này**.

**Nghĩa vụ BSD-3 gắn vào cái gì:**

| Điều kiện BSD-3 | Kích hoạt khi | Nếp Nhà dev-only |
|---|---|---|
| 1. Giữ notice trong bản phân phối **source** | Ta phân phối source ERFA | ❌ không phân phối |
| 2. In lại notice trong bản phân phối **binary** | Ta phân phối binary ERFA | ❌ không phân phối |
| 3. Không dùng tên SOFA Board/IAU để quảng bá | **Luôn luôn** | ✅ tuân thủ — không quảng bá bằng tên đó |

**INFERENCE.** Giấy phép BSD-3 đặt điều kiện lên *"redistribution and use in source
and binary forms"*. Nó **không nói gì** về đầu ra của việc chạy phần mềm. Cách đọc
thông thường: **số liệu sinh ra không phải tác phẩm phái sinh của mã nguồn**, nên
dùng ERFA làm công cụ dev **không tạo nghĩa vụ giấy phép lên dataset đầu ra**.

> ⚠️ Đây là **INFERENCE**, không phải FACT pháp lý. **LEGAL-UNKNOWN** nếu cần chắc
> chắn tuyệt đối.

## A3. Meeus — 5 lớp, vẫn OPEN

| Lớp | Trạng thái |
|---|---|
| 1. Cuốn sách | Được bảo hộ. Ta **chưa từng** mở |
| 2. Thuật toán mô tả trong sách | **LEGAL-UNKNOWN** — không tự suy "thuật toán thì chắc chắn dùng được" |
| 3. Hiện thực `eraMoon98` | BSD-3 **VERIFIED** ở lớp giấy phép code |
| 4. Dataset NASA | Permission **VERIFIED** trực tiếp từ bên công bố |
| 5. Dataset Nếp Nhà sinh ra | **LEGAL-UNKNOWN** ở lớp 2 truyền xuống |

**Không kết luận** "ERFA BSD-3 nên mọi thứ bên trong đều sạch". Không kết luận
"thuật toán toán học thì chắc chắn dùng được".

## A4. Trả lời câu hỏi trọng tâm của mục A

> *"Có thể dùng NASA Moon Phase Catalog làm nguồn dữ liệu Sóc cho app thương mại,
> đồng thời dùng ERFA `eraMoon98` chỉ trên máy dev để kiểm chứng, mà không phân phối
> ERFA trong APK hay repo production hay không?"*

**Trả lời: CÓ — với căn cứ sau, và với một vùng chưa chắc chắn được ghi rõ.**

| Thành phần | Căn cứ | Nhãn |
|---|---|---|
| NASA làm nguồn Sóc trong sản phẩm thương mại | Permission tường minh của NASA + chính sách bản quyền NASA | **TERMS VERIFIED** |
| Nghĩa vụ | Ghi công `Moon Phase Predictions by Fred Espenak, NASA/GSFC` | **FACT** |
| ERFA chỉ ở máy dev | BSD-3 điều 1&2 chỉ kích hoạt khi **phân phối** ERFA; ta không phân phối | **INFERENCE từ FACT giấy phép** |
| Điều 3 BSD-3 | Không dùng tên SOFA Board/IAU quảng bá — tuân thủ | **FACT** |
| Dataset đầu ra | BSD-3 không đặt điều kiện lên đầu ra | **INFERENCE** |
| **Vùng chưa chắc chắn** | Ancestry Meeus ở lớp thuật toán (A3 lớp 2) | **LEGAL-UNKNOWN** |

⚠️ Đây là **đánh giá provenance kỹ thuật**, không phải kết luận pháp lý.

---

## B. NASA-first ≠ NASA định nghĩa lịch Việt

**Ghi rõ, bắt buộc dùng đúng tên:**

> NASA chỉ cung cấp **dữ liệu thiên văn về pha Mặt Trăng**. Toàn bộ **quy tắc lịch
> Việt Nam do Nếp Nhà tự hiện thực**.
>
> ✅ Tên đúng: **"Vietnamese Lunar Calendar engine using NASA astronomical data"**
> ⛔ Tên sai: ~~"NASA Vietnamese Lunar Calendar"~~

Không nguồn thiên văn nào — NASA, ERFA, JPL hay HKO — định nghĩa lịch Việt Nam.
Chúng chỉ cho **thời điểm**. Việc **áp quy tắc** lên thời điểm là việc của Nếp Nhà,
và đó mới là chỗ engine có thể sai.

---

## C. Specification quy tắc lịch Việt Nam — provenance từng quy tắc

Điểm mấu chốt: **không lấy quy tắc nào từ HND làm nguồn duy nhất**, và **không lấy
expected value nào từ lichviet.app**.

| # | Quy tắc | Nguồn provenance | Nhãn |
|---|---|---|---|
| 1 | Ngày chứa thời điểm Sóc là **mùng 1**; ngày lấy trọn, bất kể Sóc xảy ra lúc nào trong ngày | Aslaksen, *The Mathematics of the Chinese Calendar* (NUS, học thuật) — **độc lập với HND** | **FACT** |
| 2 | Tháng âm bắt đầu tại mỗi điểm Sóc | như trên | **FACT** |
| 3 | **Đông chí luôn nằm trong tháng 11** | như trên + *Explanatory Supplement to the Astronomical Almanac* | **FACT** |
| 4 | Năm nhuận có 13 tháng; tháng **không chứa trung khí** là tháng nhuận; nếu có hai thì lấy tháng **đầu tiên sau Đông chí** | như trên | **FACT** |
| 5 | 12 trung khí ở bội số **30°** hoàng kinh Mặt Trời | như trên | **FACT** |
| 6 | **Kinh tuyến 105°Đ / múi giờ 7** cho Việt Nam | **Quyết định 121-CP điều 1** | **FACT nhưng NOT INDEPENDENTLY VERIFIED** — xem §7 |
| 7 | Ngày dân sự Việt Nam để quy chiếu Sóc/trung khí | Hệ quả của #6 | **INFERENCE** |
| 8 | Số ngày tháng âm = 29 hoặc 30, suy từ hiệu hai điểm Sóc liên tiếp | Hệ quả của #1, #2 | **INFERENCE** |
| 9 | Can chi năm | Chu kỳ 60, quy ước phổ biến | **FACT** |
| 10 | Phạm vi 1901–2100 | Quyết định của dự án, theo độ phủ oracle | — |
| 11 | Ngoài phạm vi ⇒ `UnsupportedYear` | Quyết định của dự án | — |
| 12 | `CalendarContext` lịch sử | Xem [HISTORICAL_TIME_MODEL.md](HISTORICAL_TIME_MODEL.md) | ⛔ **BLOCKED cho 1954–1967** |

**INFERENCE quan trọng.** Quy tắc 1–5 là **quy tắc âm dương lịch dùng chung** với
Trung Quốc, có nguồn học thuật độc lập với HND. Khác biệt Việt Nam **nằm gọn ở quy
tắc 6** — một tham số duy nhất, và tham số đó có văn bản pháp quy. Đây là cấu trúc
rất thuận lợi cho việc kiểm chứng — xem §8.

---

## D. Online calendar policy — đã chốt

| Được dùng để | Không được dùng để |
|---|---|
| Sanity check khi debug | Làm oracle |
| Phát hiện discrepancy | Sinh expected value |
| Điều tra hồi quy | Copy dataset |
| Phân tích tương thích UX | Scrape hàng loạt |
| | Hardcode kết quả |
| | **Sửa engine chỉ để giống họ** |

**"Khác website" ≠ "bug".** Khi lệch: ghi nhận discrepancy, tìm nguyên nhân, xét theo
thứ tự V1 → V2 → quy tắc → provenance. Chỉ sửa engine khi bằng chứng cho thấy engine
sai.

---

## E. Pipeline đề xuất

```
NASA Moon Phase Catalog (permission VERIFIED)
        ↓ giữ nguyên bản gốc + ghi provenance
raw input, có checksum
        ↓
generator phía DEV (ngoài app/)
        ↓  áp quy tắc lịch Việt Nam §C tại 105°Đ
dataset sinh ra + expected vectors
        ↓
KIỂM CHỨNG ĐỘC LẬP  ── HKO (nhánh HMNAO) · V1 nhà nước · ERFA (dev-only)
        ↓
APK: Kotlin thuần + dataset tĩnh + quy tắc lịch
```

**ERFA chỉ xuất hiện ở nhánh kiểm chứng**, không nằm trên đường sinh dữ liệu sản
phẩm nếu NASA đủ dùng. Không ERFA trong APK, không NDK, không JNI.

Bảng tiết khí 1901–2100 đã sinh ở Phase 3A.4 bằng `epv00` (không dính Meeus), toàn
vẹn 6/6, khớp HKO trong 38 giây, **9,4 KB**.

---

## F. 5 ca NASA `17:00 UT`

**Không hardcode. Không thêm `InsufficientPrecision` để né.**

| Yếu tố | Số đo |
|---|---|
| Độ phân giải NASA | **phút** ⇒ giá trị thật trong ±30 s |
| Sai số ERFA vs NASA | trung vị 18,9 s · max 78,9 s — **nhưng cùng nhánh Meeus** ⇒ không phải kiểm chứng độc lập |
| Margin của 5 ca | 6,5 – 37,5 s |
| Xác minh độc lập | **Chưa có** |
| Số ca ảnh hưởng | **6/2.474 = 0,24%** |

**Quyết định về dataset sản xuất — đề xuất, cần bạn duyệt:**

Dataset **định nghĩa theo nguồn**: *"giá trị công bố của NASA"*. Khi đó dataset
**tất định và không mơ hồ** — mơ hồ nằm ở câu hỏi *"giá trị công bố có khớp thực tế
không"*, và câu hỏi đó được **ghi lại**, không bị giấu.

| | |
|---|---|
| Có đưa 5 ca vào dataset không? | **Có** — loại chúng ra sẽ tạo lỗ hổng trong chuỗi tháng âm |
| Confidence level | Đánh dấu `LOW_CONFIDENCE_DAY_BOUNDARY` trong **metadata của dataset**, không phải trong API |
| Test biểu diễn thế nào | Test **khẳng định** engine tái lập đúng giá trị NASA; **và** một test riêng ghi nhận 6 ca này là UNVERIFIED so với thực tế |

Cách này không giả vờ chính xác, không hardcode ngoại lệ, và không làm bẩn API công
khai.

---

## G. Lịch sử 1954–1967 — bằng chứng mạnh nhất từ trước tới nay

Xem §7 dưới.

---

# 7. 🔴 PHÁT HIỆN QUAN TRỌNG NHẤT CỦA PHASE — nội dung 121-CP

Truy được **cấu trúc điều khoản** của Quyết định 121-CP năm 1967, tiêu đề **"về việc
tính lịch và quản lý lịch của Nhà nước"**:

| Điều | Nội dung |
|---|---|
| **1** | Việt Nam nằm trọn trong **múi giờ thứ 7**; giờ chính thức là giờ múi 7 |
| **2** | **Dương lịch** là lịch chính thức của Việt Nam |
| **3** | **"Việc sửa đổi cách tính âm lịch"** là cần thiết để bảo đảm chính xác và **phù hợp với giờ chính thức của nước nhà** |
| **4** | Giao **Nha Khí tượng** tính **âm lịch thống nhất của cả nước** đối chiếu với dương lịch, và quản lý công việc về lịch |
| **6** | Chủ nhiệm Ủy ban Khoa học và Kỹ thuật Nhà nước, Bộ trưởng Bộ Nội vụ, Tổng giám đốc Việt Nam Thông tấn xã, Giám đốc Nha Khí tượng chịu trách nhiệm thi hành |

**FACT.** Còn tồn tại **Thông tư 01-VLĐC (1967) của Nha Khí tượng** *"giải thích và
hướng dẫn thi hành Quyết định 121-CP"*.

> ⚠️ **NOT INDEPENDENTLY VERIFIED.** Tôi **không đọc được toàn văn** của cả hai văn
> bản: `thuvienphapluat.vn` (cả bản desktop 18212, 18929 và bản mobile) và
> `luatminhkhue.vn` đều trả **HTTP 403**, kể cả với User-Agent trình duyệt. Nội dung
> trên đến từ **trích xuất của công cụ tìm kiếm**, không phải từ việc tôi đọc trực
> tiếp văn bản gốc.

## 7.1 Điều 3 là bằng chứng trực tiếp cho P2

**INFERENCE.** Điều 3 nói **"sửa đổi cách tính âm lịch"** cho **phù hợp với giờ chính
thức**. Một quyết định "sửa đổi cách tính" chỉ có nghĩa nếu **cách tính trước đó khác
đi**. Điều này nghiêng mạnh về **P2** (miền Bắc trước 1968 tính lịch theo múi giờ
khác 7).

**Nhưng KHÔNG chuyển G9 sang PASS**, vì ba lý do:

1. Nội dung điều 3 **chưa đọc được từ văn bản gốc**.
2. Điều 3 nói *có sửa đổi*, **không nói** trước đó là múi giờ nào (+8? +7:06? khác?).
3. Nó nói về **miền Bắc**; miền Nam là chuyện khác.

## 7.2 Điều 4 giải thích nguồn gốc của Ban Lịch Nhà nước

**INFERENCE.** Nha Khí tượng được giao tính **"âm lịch thống nhất của cả nước"** ⇒
lịch âm chính thức Việt Nam là **sản phẩm do nhà nước tính**, không phải một quy tắc
tự nhiên. Đây chính là chuỗi dẫn tới Ban Lịch Nhà nước → Trần Tiến Bình.

⇒ Củng cố kết luận ở Phase 3A.5 trước: **không nguồn thiên văn nào chứng minh được
lịch chính thức Việt Nam**; chỉ sản phẩm của cơ quan được giao mới là V1.

## 7.3 Thông tư 01-VLĐC có thể là văn bản quan trọng nhất chưa đọc được

**HYPOTHESIS.** Một thông tư *"giải thích và hướng dẫn thi hành"* việc **tính lịch**
nhiều khả năng nêu **phương pháp tính** — tức có thể là nguồn V1 cho chính các quy
tắc ở §C, thay cho mô tả của HND.

**Chưa xác minh.** Đây là mục tiêu tra cứu ưu tiên cao.

---

# 8. 🟢 ĐƯỜNG KIỂM CHỨNG KHÔNG CẦN SÁCH — kiểm chứng cấu trúc qua HKO

Đây là đóng góp kỹ thuật chính của phase này.

**Cơ sở (từ §C):** quy tắc 1–5 **dùng chung** giữa lịch Việt Nam và lịch Trung Quốc.
Khác biệt **duy nhất** là quy tắc 6 — kinh tuyến 105°Đ thay vì 120°Đ.

**Đề xuất:**

```
Bước 1  Chạy engine của Nếp Nhà với kinh tuyến 120°Đ
Bước 2  So với bảng lịch Trung Quốc 1901-2100 của HKO — TOÀN BỘ 200 năm
          · ngày đầu của mọi tháng âm      (~2.474 mốc)
          · tháng nhuận của mọi năm
          · độ dài 29/30 của mọi tháng
Bước 3  Nếu khớp 100% ⇒ quy tắc 1-5 và tầng thiên văn ĐÃ ĐƯỢC KIỂM CHỨNG
          trên toàn phạm vi, đối chiếu một đài thiên văn quốc gia
Bước 4  Đổi tham số sang 105°Đ theo QĐ 121-CP điều 1
Bước 5  Kiểm ngược bằng các ca VN≠TQ đã biết (1984, 1985, 1987, 2006-2008, 2030, 2053)
```

**Vì sao mạnh:**

| | |
|---|---|
| Độ phủ | **Toàn bộ 1901–2100**, không phải vài chục vector |
| Oracle | HKO — đài thiên văn quốc gia, số liệu HMNAO, **khác nhánh Meeus** |
| Giấy phép | data.gov.hk cho phép thương mại kèm attribution — **VERIFIED** |
| Chi phí | Dữ liệu đã tải được, miễn phí |
| Phủ được gate | **G6 tháng nhuận · G7 29/30 ngày · G10 biên** — tất cả trên toàn dải |

**⚠️ Giới hạn phải nói rõ — đây KHÔNG phải bằng chứng đầy đủ:**

Nó chứng minh **implementation quy tắc 1–5 + thiên văn của ta là đúng**. Nó **KHÔNG**
chứng minh **lịch chính thức Việt Nam bằng đúng "quy tắc 1–5 áp tại 105°Đ"**. Mắt
xích cuối đó dựa vào QĐ 121-CP điều 1 + các ca VN≠TQ, và **vẫn cần V1 Việt Nam để
đóng hoàn toàn**.

**INFERENCE.** Nhưng nó thu hẹp vai trò của sách từ *"nguồn chứng minh toàn bộ lịch"*
xuống *"nguồn xác nhận mắt xích cuối"* — có thể chỉ cần **vài chục vector** thay vì
cả cuốn.

---

# 9. GATE MATRIX

| Gate | Status | Evidence | Remaining uncertainty | Exact next action |
|---|---|---|---|---|
| **G1** Không dùng source HND | ✅ **PASS** | License HND phi thương mại, nguyên văn | — | — |
| **G2** Không dùng repo HND-derived | ✅ **PASS** | 4 repo "MIT" đã nhận diện | — | — |
| **G3** Không dùng test data thiếu provenance | ✅ **PASS** | Web lịch xếp V3/V4 | — | — |
| **G4** Phạm vi 1901–2100 | ✅ **PASS** | Khớp độ phủ HKO và NASA | — | — |
| **G5** Oracle độc lập | ⛔ **BLOCKED** | Quy tắc 1–5 có nguồn học thuật (Aslaksen) độc lập HND; quy tắc 6 có QĐ 121-CP | Chưa V1 Việt Nam cho mắt xích cuối; 121-CP chưa đọc được toàn văn | Kiểm chứng cấu trúc §8; tra Thông tư 01-VLĐC |
| **G6** Tháng nhuận | ⛔ **BLOCKED → có đường mở** | — | Chưa chạy §8 | **Kiểm chứng cấu trúc HKO toàn dải** |
| **G7** 29/30 ngày | ⛔ **BLOCKED → có đường mở** | — | Chưa chạy §8 | như trên |
| **G8** VN ≠ TQ | 🟡 **PARTIAL** | 1985: VN nhánh A + TQ nhánh B (HKO raw) | 7 năm còn lại thiếu phía VN | Bước 5 của §8 |
| **G9** Múi giờ lịch sử | ⛔ **BLOCKED** | **QĐ 121-CP điều 3 "sửa đổi cách tính âm lịch"** — nghiêng mạnh về P2 | Toàn văn 403; không nói offset trước đó là gì; miền Nam riêng | Tra Thông tư 01-VLĐC + lịch in 1954–1967 |
| **G10** Biên 1901/2100 | ⛔ **BLOCKED → có đường mở** | — | Chưa chạy §8 | Kiểm chứng cấu trúc HKO ở hai đầu dải |
| **G11** Meeus đã xử lý | 🟡 **PARTIAL** | NASA permission VERIFIED; ERFA BSD-3 VERIFIED; dev-only không phát sinh nghĩa vụ (INFERENCE) | Lớp thuật toán **LEGAL-UNKNOWN** | Chủ dự án chấp nhận rủi ro tồn dư, hoặc chọn JPL DE440 |
| **G12** Historical model | ✅ **PASS** (thiết kế) | `CalendarContext` | Hành vi 1954–1967 phụ thuộc G9 | — |
| **G13** Không đổi Room schema | ✅ **PASS** | Không cần cột mới | — | — |
| **G14** Provenance thiên văn | 🟡 **PARTIAL** | NASA **TERMS VERIFIED** cho Sóc; `epv00` khớp HKO 72/72 trong 38 s | Nhánh Mặt Trăng vẫn ancestry Meeus | Chấp nhận, hoặc benchmark JPL DE440 |
| **G15** Độ chính xác | 🟡 **PARTIAL** | Lượng hoá đầy đủ: 6/2.474 = 0,24% rủi ro | 5 ca UNVERIFIED; NASA↔ERFA cùng nhánh nên không tự phân xử được | Duyệt chính sách dataset §F; hoặc JPL DE440 |
| **G16** ΔT | ✅ **PASS** | Đo: ΔT sai 5 s ⇒ 0/2.474 ngày đổi | — | — |

**PASS: G1 G2 G3 G4 G12 G13 G16** (7) · **PARTIAL: G8 G11 G14 G15** (4) ·
**BLOCKED: G5 G6 G7 G9 G10** (5)

---

# 10. FINAL GATE — kết luận

## 🔴 BLOCKED

**Không được implement engine.**

Nhưng bản chất blocker đã đổi, và đây là điều quan trọng nhất của phase:

| Trước phase này | Sau phase này |
|---|---|
| "Cần sách Trần Tiến Bình để chứng minh lịch" | **G6/G7/G10 có thể kiểm chứng bằng HKO trên toàn dải 1901–2100, không cần sách** |
| Quy tắc lịch lấy từ mô tả của HND | **Quy tắc 1–5 có nguồn học thuật độc lập (Aslaksen); quy tắc 6 có văn bản pháp quy** |
| G9 chỉ có hai giả thuyết ngang nhau | **QĐ 121-CP điều 3 nghiêng mạnh về P2** — nhưng chưa đọc được toàn văn |
| Không rõ NASA có dùng thương mại được không | **TERMS VERIFIED** |
| Không rõ ERFA dev-only có phát sinh nghĩa vụ không | Đã phân tích: nghĩa vụ gắn vào **phân phối**, ta không phân phối |

## Ba blocker thật còn lại

| # | Blocker | Ai mở | Chi phí |
|---|---|---|---|
| **1** | **Chưa chạy kiểm chứng cấu trúc §8** | **Tôi** — chỉ cần bạn duyệt | Thấp. Dữ liệu HKO đã có, miễn phí |
| **2** | **G9** 1954–1967 | Cả hai | Trung bình — cần Thông tư 01-VLĐC hoặc lịch in |
| **3** | **Mắt xích V1 Việt Nam cuối cùng** | Bạn | Thấp hơn trước — vài chục vector, không cần cả cuốn sách |

## Điều tôi KHÔNG làm và vì sao

Tôi **không** tự chạy kiểm chứng cấu trúc §8 trong phase này, dù nó nằm trong
`tools/`. Lý do: nó đòi hiện thực đầy đủ quy tắc lịch, và dù ở Python ngoài `app/`
thì về bản chất **đó là viết engine**. Chỉ thị nói rõ *"KHÔNG IMPLEMENT ENGINE"* và
*"chỉ docs/tools cần thiết cho verification"*. Tôi để bạn quyết ranh giới này thay vì
tự nới.

## Việc rẻ nhất mà giá trị nhất

**Duyệt cho tôi chạy §8** — kiểm chứng cấu trúc engine với HKO trên toàn dải
1901–2100. Nó có thể mở **ba gate cùng lúc (G6, G7, G10)** mà không cần sách, không
tốn tiền, không tốn data đáng kể, và cho ta biết ngay quy tắc đã hiện thực đúng chưa.

Nếu bạn muốn giữ ranh giới "không viết engine kể cả trong tools", hãy nói — khi đó
việc thay thế rẻ nhất là **tra Thông tư 01-VLĐC** cho G9 và G5.
