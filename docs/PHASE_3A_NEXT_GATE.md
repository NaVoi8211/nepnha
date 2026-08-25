# Phase 3A.4 — Gate report & decision memo

> **KHÔNG CÓ PRODUCTION CODE.** `app/` không bị đụng · không Kotlin · không Room ·
> không UI · không dependency. Nghiên cứu 2026-08-25.
>
> Nhãn: **FACT** (nguồn trực tiếp) · **VERIFIED** (đo/tái lập được) · **INFERENCE** ·
> **HYPOTHESIS** · **UNVERIFIED/BLOCKED** · **UNKNOWN**.

---

## A. ERFA / eraMoon98 / Meeus — điều tra dứt điểm

### A.1 Inventory Mặt Trăng — quét toàn bộ 251 file source

**VERIFIED** (grep nội dung trên source đã tải, không đoán theo tên file):

| Câu hỏi | Trả lời |
|---|---|
| ERFA có bao nhiêu ephemeris Mặt Trăng? | **Đúng một: `moon98.c`** |
| Có routine Mặt Trăng nào khác dùng thay được không? | **KHÔNG.** `faf03/fad03/fal03/faom03` chỉ là *đối số cơ bản* của Mặt Trăng dùng cho chương động; `nut*`, `s06`, `xy06`, `dtdb`, `plan94` không cho vị trí Mặt Trăng |
| File nào trong ERFA nhắc tới Meeus? | **Đúng một: `moon98.c`** |

**INFERENCE — kết luận quan trọng nhất của phase này:**

> Vết Meeus trong ERFA **gói gọn trong đúng một file**. Nhánh Mặt Trời
> (`epv00`, `eqec06`, `nut06a`, `c2s`, `anp`) **hoàn toàn không dính Meeus**.
> Không dùng `moon98` thì ERFA vẫn dùng được cho tiết khí — nhưng **mất hẳn** khả
> năng tự tính điểm Sóc.

### A.2 Câu hỏi 1 — LICENSE của ERFA có cho phép dùng thương mại không?

**FACT.** File `INFO` của ERFA chứa **nguyên văn email cho phép** của Chủ tịch IAU
SOFA Board:

> The IAU Standards Of Fundamental Astronomy Board approves the relicensing of a
> changed SOFA library by the NumFOCUS Foundation to use a "Three Clause BSD"
> license. The changed, relicensed version shall differ from the SOFA version in that
> all function names shall change to use "era" as a prefix in place of "iau", and
> that the SOFA Board shall be removed as a copyright holder in the relicensed
> version.
>
> — Catherine [Hohenkerk], Chair, IAU SOFA Board, HM Nautical Almanac Office, UKHO

**FACT.** `README.rst`: *"Permission for this release has been obtained from the SOFA
board, and is available in the `LICENSE` file."*

**FACT.** `LICENSE` là BSD 3-Clause đầy đủ, bản quyền NumFOCUS Foundation.

**INFERENCE — trả lời câu hỏi 1:** *Xét riêng LICENSE của ERFA*, **có** — BSD-3 cho
phép dùng thương mại, không buộc công bố source, chỉ buộc giữ thông báo bản quyền và
không dùng tên SOFA Board/IAU để quảng bá. Điều kiện đổi tiền tố `iau→era` đã do
ERFA thực hiện, không phải nghĩa vụ của ta.

> ⚠️ **LEGAL UNCERTAINTY — phải nói rõ.** Giấy phép trên là của **SOFA Board** về
> quyền của **SOFA**. Nó **không nói gì** về quyền của Willmann-Bell đối với thuật
> toán Meeus mà `moon98` hiện thực. Đây là vùng chưa chắc chắn, tôi **không đưa ra
> kết luận pháp lý**. Nếu Nếp Nhà thương mại hoá và muốn chắc chắn, cần luật sư.
>
> Lưu ý cân bằng: quyết định hiện thực thuật toán Meeus và phát hành nó là của
> **IAU SOFA Board** — một cơ quan chuẩn quốc tế — chứ không phải của Nếp Nhà.

### A.3 Câu hỏi 2 — POLICY của Nếp Nhà có nên cho phép không?

**Câu hỏi này tách hẳn khỏi A.2 và không phải câu hỏi kỹ thuật.** Trước hết phải làm
rõ "không dùng Meeus" nghĩa là gì:

| Cách hiểu | Nội dung | `moon98` có vi phạm? | Hệ quả kéo theo |
|---|---|---|---|
| **A** | Không **sao chép source code** từ sách Meeus | ❌ Không | Ta chưa từng mở sách. Cả `moon98` lẫn dữ liệu NASA đều dùng được |
| **B** | Không dùng **bất kỳ implementation nào dựa trên** Meeus | ✅ **Có** | Cấm `moon98`. **Cũng cấm dữ liệu NASA** (NASA ghi rõ dựa trên Meeus). Mất cả hai nguồn Mặt Trăng đang có |
| **C** | Không lấy **công thức/thuật toán** Meeus | ✅ **Có** | Như B, cộng thêm: mọi nguồn thứ cấp phải truy nguyên gốc |
| **D** | Nghiêm ngặt hơn nữa | ✅ | Chỉ còn ELP/MPP02 hoặc JPL DE — **UNKNOWN** giấy phép |

**INFERENCE — điểm mấu chốt bị bỏ sót ở các phase trước:** Cách hiểu **B và C loại
luôn cả NASA**, vì trang catalog NASA ghi:

> "The phases of the Moon as well as eclipses are based on Jean Meeus' Astronomical
> Algorithms (Willmann-Bell, Inc., Richmond, 1998)."

Nghĩa là **không có phương án "bỏ moon98, dùng NASA" nếu policy là B/C** — hai nguồn
Mặt Trăng đang có **cùng một tổ tiên**. Đây chính là lỗi "hai nguồn tưởng độc lập
hoá ra cùng gốc" mà dự án đã học ở Phase 3A, lặp lại ở tầng thuật toán.

**Tôi không tự chọn.** Trade-off ở §M.

### A.4 Có đường ERFA/IAU nào tính Sóc mà không qua Meeus không?

**VERIFIED: KHÔNG.** Đã quét toàn bộ 251 file. `moon98` là lối duy nhất trong ERFA.

**UNKNOWN:** ELP/MPP02 và JPL DE — chưa nghiên cứu giấy phép, chưa benchmark.

---

## B. Sách Trần Tiến Bình — provenance trước khi gọi là "oracle tuyệt đối"

**FACT (nguồn thứ cấp — giới thiệu sách của nhà phát hành):**

> Sách là *"lịch Việt Nam cho người Việt Nam, biên soạn theo múi giờ thứ 7, phù hợp
> với Quyết định 121/CP của Chính phủ"*, đồng thời trình bày *"quá trình phát triển
> của lịch Việt Nam, cơ sở tính toán, nguyên nhân và sự khác biệt với lịch Trung
> Quốc"*.

**FACT.** Nội dung được liệt kê: đối chiếu dương–âm · can chi · ngày Julius · giờ mọc
lặn của Mặt Trời · nhật thực/nguyệt thực · tham số cổ học phương Đông · ngày lễ tết.

**FACT.** Tác giả công tác tại **Ban Lịch Nhà nước** (Trung tâm Thông tin – Tư liệu,
Viện Hàn lâm KH&CN Việt Nam). Tái bản bởi Công ty Sách và Lịch Đại Nam + NXB Khoa học
Tự nhiên và Công nghệ.

### Đánh giá provenance

| Câu hỏi của mục B | Trả lời |
|---|---|
| Dựa trên phương pháp nào? | 🟡 "cơ sở tính toán" có trình bày trong sách — **chưa đọc được** |
| Có dựa vào HND/Meeus không? | **UNKNOWN.** Sách xuất bản 2005; HND công bố 2004. Không có bằng chứng theo chiều nào |
| Có bảng âm–dương toàn bộ 1901–2100? | **FACT: có** — đó là nội dung chính |
| Có tháng nhuận? | **INFERENCE: có** — bảng đối chiếu đầy đủ tất yếu thể hiện tháng nhuận |
| Có số ngày 29/30? | **INFERENCE: có** — suy ra được từ ngày đầu các tháng liên tiếp |
| Có giờ chuyển tiết? | **UNKNOWN** |
| Có thông tin múi giờ lịch sử? | 🟡 phần "quá trình phát triển của lịch Việt Nam" — **có thể có**, chưa đọc |
| Có ghi cơ sở dữ liệu thiên văn? | **UNKNOWN** |

**⚠️ Cảnh báo quan trọng.** Sách *"biên soạn theo múi giờ thứ 7"*. Nếu hiểu là **múi
giờ 7 cho toàn bộ 1901–2100**, thì sách **không mô hình hoá** giai đoạn miền Nam
UTC+8 (1960–1975) và miền Bắc trước 1968. Khi đó sách **không giải được G9** — nó chỉ
cho biết lịch chính thức *ngày nay* quy chiếu ngược lại quá khứ như thế nào, chứ
không cho biết *hồi đó người ta thực sự tính thế nào*.

**Đây là INFERENCE, chưa VERIFIED** — phải đọc phần dẫn nhập của sách mới biết.

### Quy tắc sử dụng khi có sách

- Chỉ trích **test vector tối thiểu cần thiết**, không chép bảng vào repo.
- Ghi provenance từng vector (§K).
- Không commit ảnh chụp trang nếu không cần.
- **Không** đưa nội dung có bản quyền vào APK.

---

## C. Thiết kế cách trích oracle G5/G6/G7/G10 (khi có sách)

| Gate | Trích gì | Vì sao đủ |
|---|---|---|
| **G5** | 3 ngày/tháng × 12 tháng × 8 năm rải đều 1901–2100 ≈ 288 vector | Phủ mọi thập niên, mọi tháng |
| **G6** | Danh sách **năm nhuận + tháng nhuận** cho toàn bộ 200 năm (~74 năm nhuận) + ngày đầu của tháng thường và tháng nhuận cùng tên, ≥5 năm | Bắt được cả "có nhuận không" lẫn "nhuận tháng nào" |
| **G7** | Ngày đầu của **mọi tháng âm** trong ≥6 năm chọn có chủ đích (gồm năm nhuận) ⇒ suy ra 29/30 bằng hiệu hai mốc liên tiếp | Đây là **đọc từ oracle**, không phải suy quy tắc |
| **G10** | Toàn bộ ngày đầu tháng của 1901, 1902, 2099, 2100 | Kiểm biên và năm âm đầu/cuối |

**Ghi chú G7:** lấy hiệu hai ngày đầu tháng liên tiếp **do oracle cung cấp** là phép
đọc dữ liệu, khác hẳn với việc suy "Rằm = Tết + 14". Ranh giới này phải giữ.

---

## D. G15 — 5 ca NASA `17:00 UT`

**VERIFIED (mới ở phase này) — tách thành phần sai số** bằng mô phỏng Monte Carlo
200.000 mẫu:

| Giả thiết | trung vị | p95 |
|---|---|---|
| **Quan sát thực tế** (ERFA vs NASA, 2.474 mốc) | **18,9 s** | **49,3 s** |
| Chỉ do NASA làm tròn phút | 15,0 s | 28,5 s |
| Làm tròn + sai số thiên văn σ=10 s | 15,3 s | 36,7 s |
| Làm tròn + sai số thiên văn σ=**20 s** | **18,4 s** | **51,1 s** |

**INFERENCE.** Thành phần thiên văn thực tế **σ ≈ 20 giây** — gấp ~3,5 lần RMS 2,9″
(≈5,7 s) mà ERFA công bố cho `moon98`.

**UNKNOWN.** Không quy được cho bên nào: đây là **sai số chung của cả hai phương
pháp**. Có thể NASA dùng lý thuyết Mặt Trăng đầy đủ hơn bản rút gọn trong `moon98`,
có thể `moon98` kém hơn trong ứng dụng này, có thể cả hai.

**INFERENCE — hệ quả cho 5 ca.** Margin của chúng là 6,5–37,5 s, tức nằm trong khoảng
**1–2σ**. Hai ước lượng độc lập lệch nhau nhiều hơn khoảng cách tới ranh giới ⇒
**không nguồn nào trong hai nguồn hiện có phân giải được.**

**Giữ UNVERIFIED.** Không hardcode, không thêm `InsufficientPrecision`, không quyết
định API. Sách Trần Tiến Bình có giải được không: **UNKNOWN** — phụ thuộc sách có ghi
ngày đầu tháng cho các tháng chứa 5 ca đó không (nhiều khả năng có, vì đó là bảng đối
chiếu đầy đủ).

---

## E. G9 — múi giờ lịch sử 1954–1967

### FACT MỚI, quan trọng

**FACT.** Quyết định 121-CP có tiêu đề chính thức: **"về việc tính lịch và quản lý
lịch của Nhà nước"** — *không phải* một quyết định thuần về giờ dân sự.

**INFERENCE.** Đây **đúng là văn bản cần tìm**: nó nói về *tính lịch*, tức chạm thẳng
vào cột "múi giờ dùng để tính lịch" mà [HISTORICAL_TIME_MODEL.md](HISTORICAL_TIME_MODEL.md)
đang để trống.

**FACT.** *"giờ pháp định của VN Dân chủ Cộng hoà là múi giờ 7 quốc tế, kể từ 0 giờ
ngày 1-1-1968"* (nguồn thứ cấp).

**BLOCKED.** Toàn văn vẫn không đọc được: `thuvienphapluat.vn` trả **HTTP 403** cho
cả hai bản ghi (18212 và 18929), kể cả khi gửi User-Agent trình duyệt;
`hethongphapluat.com` trả body rỗng.

**Chưa làm:** lịch nhà nước in 1954–1967 · tài liệu Ban Lịch Nhà nước · lịch tháng
7/1967.

**G9 giữ BLOCKED. Không chọn P1 hay P2.**

---

## F. Tiết khí — đã sinh và kiểm tra toàn vẹn bảng 1901–2100

**VERIFIED.** Sinh bằng **nhánh không dính Meeus** (`epv00` + `eqec06` + `nut06a`),
`moon98` **không được gọi**. Tái lập: `tools/benchmark_erfa_astronomy/generate_solar_terms.py`

| Kiểm tra (mục F.6) | Kết quả |
|---|---|
| Hoàng kinh sai lệch > 0,0001° | **0** |
| Timestamp không tăng đơn điệu | **0** |
| Khoảng cách hai tiết khí ngoài [13,17] ngày | **0** (min 14,714 · max 15,736) |
| Mốc trùng nhau | **0** |
| Năm không có đúng 24 tiết khí | **0** |
| Năm thiếu hoàn toàn | **0** |
| **Tổng** | ✅ **4.800 mốc, toàn vẹn** |

**Kiểm tra toàn vẹn đã bắt được một lỗi thật:** lần chạy đầu, năm 1901 chỉ có 19 mốc
— vì chu kỳ tiết khí bắt đầu từ Xuân phân nên các mốc tháng 1–3 của năm Y thuộc chu
kỳ năm Y−1. Đã sửa bằng cách sinh dư một chu kỳ ở hai đầu. **Đây là lý do phải có
kiểm tra toàn vẹn thay vì tin vào vòng lặp.**

**VERIFIED — đối chiếu oracle độc lập:** bảng vừa sinh so với HKO (nhánh B), 72 mốc
2026–2028: **|lệch| lớn nhất 38 giây**, trong đó ±30 s là do chính HKO làm tròn phút.

**Kích thước dữ liệu:**

| Cách mã hoá | Kích thước |
|---|---|
| Thô, 4 byte/mốc | 18,8 KB |
| Hiệu số phút, 2 byte/mốc (min 21.188 · max 22.661 ⇒ vừa 16 bit) | **9,4 KB** |

**INFERENCE.** Định dạng đề xuất: `uint16` big-endian, hiệu số phút giữa hai mốc liên
tiếp, mốc đầu là số phút tuyệt đối kể từ 1901-01-01T00:00Z. Không phụ thuộc endian
của máy, không dấu phẩy động ⇒ **tất định tuyệt đối**.

### ⚠️ Chưa được gọi G14 PASS

Theo mục F.8–F.9: **không extrapolate** từ 2026–2028 ra "1901–2100 chắc chắn đúng".
Ta mới có oracle giờ cho **3/200 năm**. Tiêu chí PASS cho G14 phải là:

- [x] Provenance nguồn rõ ràng
- [x] Benchmark có số đo
- [x] Test tái lập được bằng script
- [ ] **Oracle độc lập phủ nhiều hơn 3 năm** ← chưa đạt
- [ ] **Quyết định chính sách về nhánh Mặt Trăng** ← chưa có

---

## G. Độ chính xác moon98 — envelope

**VERIFIED.** 2.474 điểm Sóc: trung vị 18,9 s · p95 49,3 s · max 78,9 s (năm 1954).
Không suy giảm trước 1950 (1901–1949: trung vị 18,8 s, max 65,7 s).

**Tách thành phần** (§D): NASA làm tròn ±30 s (định nghĩa) + thiên văn σ≈20 s (đo
được) + ΔT ≈ 0 (đo được, §H).

**Miền hỏng dự kiến (expected failure domain):** điểm Sóc có margin tới 17:00 UTC
nhỏ hơn ~2σ ≈ 40 s. **VERIFIED: 6/2.474 = 0,24%**, danh sách đầy đủ đã có ở
[3A.3 §9](PHASE_3A3_ASTRONOMICAL_BENCHMARK.md).

---

## H. ΔT / UT / TT — chứng minh cho G16

**FACT.** Catalog NASA công bố thời điểm pha Mặt Trăng ở **Universal Time (UT)**,
kèm cột **ΔT theo từng năm**.

**Định nghĩa:**

| | |
|---|---|
| **TT** | Terrestrial Time — thang đều, dùng cho tính toán thiên văn |
| **UT1** | Theo vòng quay Trái Đất — quyết định **ngày dân sự** |
| **ΔT** | `TT − UT1` |
| **UTC** | UT1 + giây nhuận, **chỉ tồn tại từ 1960** |
| **TAI−UTC** | Giây nhuận, `eraDat` |

**INFERENCE — vì sao không cần UTC trước 1960:** để biết điểm Sóc rơi ngày nào ở Việt
Nam ta cần **UT1**, không cần UTC. `UT1 = TT − ΔT` là đủ. `eraDat`/`eraUtctai`/
`eraTaitt` **không nằm trên đường đi** ⇒ việc chúng không định nghĩa trước 1960 là
**không liên quan**.

**Chính sách nội suy ΔT:** dùng nguyên giá trị ΔT của **năm chứa thời điểm**, không
nội suy tuyến tính. Lý do: ΔT biến thiên < 1 s/năm trong 1901–2100, trong khi §H.2
cho thấy sai số 5 s cũng không đổi ngày nào.

**VERIFIED — độ nhạy, đo trên cả 2.474 mốc:**

| ΔT sai | Số ngày âm đổi |
|---|---|
| +1 s | **0** / 2.474 |
| +5 s | **0** / 2.474 |
| +10 s | 1 / 2.474 |
| +60 s | 1 / 2.474 |

**INFERENCE.** Kể cả sai 60 giây cũng chỉ đổi **một** ngày trong 200 năm ⇒ ΔT không
phải rủi ro. **G16 = PASS.**

**Khi tạo dataset phải ghi:** nguồn (NASA catalog) · đơn vị (giây) · độ phân giải
(phút, theo cột ΔT) · chính sách nội suy (theo năm, không nội suy).

---

## I. Kiến trúc — ràng buộc phải ghi vào tài liệu

Nếu chọn hướng dataset-first:

- [ ] ERFA **không** được đóng gói vào APK
- [ ] Không NDK · không JNI · không native library
- [ ] Không tính toán thiên văn lúc chạy
- [ ] APK chỉ chứa **dataset + engine Kotlin**
- [ ] Dataset có provenance, checksum và version
- [ ] Generator tái lập được (đã có trong `tools/`)
- [ ] Engine không đọc timezone hệ thống, locale, hay đồng hồ
- [ ] Thread-safe, tất định

---

## J. Test strategy 4 tầng

| Tầng | Nguồn | Chứng minh được gì |
|---|---|---|
| **1** | Trần Tiến Bình · văn bản nhà nước | **Lịch có đúng lịch Việt Nam không** |
| **2** | NASA · HKO · ERFA | Thiên văn có đúng không |
| **3** | HND/amlich.js | **Chỉ để phát hiện sai lệch.** KHÔNG bao giờ là oracle cuối |
| **4** | Round-trip · độ dài tháng · thứ tự · can chi | **Chỉ chứng minh nhất quán nội bộ.** KHÔNG chứng minh lịch đúng |

Bài học Phase 2 giữ nguyên: **test xanh không đồng nghĩa với đúng.**

---

## K. Phân loại nguồn — bốn khái niệm không được gộp

| Khái niệm | Ví dụ | Ràng buộc |
|---|---|---|
| **SOURCE CODE** | `moon98.c`, `epv00.c` | Giấy phép của code. BSD-3 |
| **ALGORITHM** | Thuật toán Meeus trong `moon98` | Vấn đề **policy**, khác với giấy phép code |
| **ASTRONOMICAL DATA** | Bảng Sóc NASA, tiết khí HKO | Điều khoản của dữ liệu |
| **EXPECTED TEST VECTOR** | Ngày Tết trong văn bản nhà nước | Oracle. **Không bao giờ được sinh từ chính engine** |

---

## L. Gate matrix

| # | Gate | Trạng thái | Căn cứ |
|---|---|---|---|
| G1–G4 | HND / Meeus source / phạm vi 1901–2100 | ✅ **PASS** | |
| **G5** | Oracle độc lập lịch âm VN | ⛔ **BLOCKED** | Chưa có sách |
| **G6** | Vector tháng nhuận | ⛔ **BLOCKED** | Chưa có sách |
| **G7** | Vector 29/30 ngày | ⛔ **BLOCKED** | Chưa có sách |
| **G8** | VN ≠ TQ | 🟡 **PARTIAL** | 1985 PASS (A vs B); 7 năm còn lại thiếu phía VN |
| **G9** | Múi giờ lịch sử 1954–1967 | ⛔ **BLOCKED** | 121-CP: **có FACT mới về tiêu đề**, toàn văn vẫn 403 |
| G10 | Biên 1901/2100 | ⛔ **BLOCKED** | Chưa có sách |
| G11–G13 | Meeus loại / model / schema | ✅ **PASS** | |
| **G14** | Nguồn thiên văn | 🟡 **PARTIAL** | ✅ Tiết khí: bảng 1901–2100 đã sinh, toàn vẹn, khớp HKO 38 s · ⛔ Mặt Trăng: **chờ quyết định policy** |
| **G15** | Mô hình độ chính xác | 🟡 **PARTIAL** | ✅ Đã tách σ≈20 s · ⛔ 5 ca vẫn UNVERIFIED |
| **G16** | ΔT | ✅ **PASS** | Đo: sai 5 s ⇒ 0/2.474 ngày đổi |

### ⛔ GATE VẪN BLOCKED — CHƯA ĐỦ ORACLE ĐỂ IMPLEMENT

Không gate nào được chuyển sang PASS bằng suy luận. Phase này **thêm** bằng chứng cho
G14/G15/G16 và **thêm FACT mới** cho G9, nhưng bốn gate oracle vẫn trống vì lý do
không đổi: **chưa có sách**.

---

## M. Decision memo — 5 phương án cho nhánh Mặt Trăng

Cần chủ dự án duyệt trước khi implement bất cứ thứ gì.

| | **A — moon98/ERFA** | **B — nguồn khác** | **C — dataset NASA** | **D — xin phép Meeus** | **E — thu hẹp phạm vi** |
|---|---|---|---|---|---|
| **Correctness** | ✅ σ≈20 s, đo được | ❓ UNKNOWN | ✅ nhưng chỉ tới **phút** | ✅ như A | ✅ trong phạm vi hẹp |
| **Legal / commercial** | ✅ BSD-3 + phép SOFA Board · ⚠️ vùng xám Willmann-Bell | ❓ UNKNOWN (ELP/MPP02, JPL DE) | ⚠️ **NASA cũng dựa trên Meeus** | ✅ sạch nhất nếu được chấp thuận | như phương án nền |
| **Provenance** | ✅ primary source, truy được tận email | ❓ | ✅ permission rõ | ✅ | như nền |
| **Reproducibility** | ✅ script + ERFA | ❓ | ✅ tải lại catalog | ✅ | ✅ |
| **APK size** | ~5 KB (2.474 mốc) | ❓ | ~5 KB | ~5 KB | nhỏ hơn |
| **Maintenance** | Thấp — bảng cố định | ❓ | Thấp | Thấp | Thấp |
| **Historical coverage** | 1901–2100 | ❓ | 1901–2100 | 1901–2100 | **hẹp hơn** |
| **Risk** | Vùng xám pháp lý; policy | Nghiên cứu lại từ đầu | **Cùng gốc Meeus với A** | Mất thời gian, có thể bị từ chối | Mất tính năng |

### Điều bắt buộc phải hiểu trước khi chọn

**INFERENCE.** **A và C có cùng tổ tiên.** Nếu policy loại Meeus theo cách hiểu B/C
(§A.3) thì **cả hai đều bị loại**, không phải chỉ A. Chọn C để "tránh Meeus" là tự
lừa mình — đúng loại sai lầm dự án đã phát hiện ở Phase 3A với các repo "MIT".

**Nhánh Mặt Trời không bị ảnh hưởng bởi quyết định này** — bảng tiết khí 1901–2100 đã
sinh xong, toàn vẹn, sạch Meeus, dùng được trong mọi phương án.

### Đề xuất của tôi (không tự quyết)

**A + C phối hợp**, nếu policy được làm rõ theo cách hiểu **A** ở §A.3:

- Điểm Sóc: `moon98` sinh bảng, **NASA làm oracle đối chiếu** (không phải input).
- 6 ca margin < 79 s: đánh dấu, chờ oracle Tier 1, **không đoán**.

**Nếu policy là B/C:** phải đi phương án **B** (ELP/MPP02 hoặc JPL DE) — cần một
phase nghiên cứu giấy phép nữa — hoặc **D**.

---

## N. Blocker còn lại và cách mở

| # | Blocker | Cách mở | Ai |
|---|---|---|---|
| **1** | **Policy Meeus** (§A.3, §M) | Chọn cách hiểu A/B/C/D | **Chủ dự án** |
| **2** | **Oracle Tier 1** — G5/G6/G7/G10, và 5 ca của G15 | Có sách Trần Tiến Bình; trích theo thiết kế §C | **Chủ dự án** cung cấp sách |
| **3** | **G9** múi giờ 1954–1967 | Toàn văn 121-CP (403 ở 2 nguồn) · lịch in 1954–1967 · phần dẫn nhập sách | Cả hai |

Blocker **1** chặn nhánh Mặt Trăng. Blocker **2** chặn mọi việc chứng minh lịch đúng.
Blocker **3** chặn riêng dữ liệu lịch sử trước 1968.

**Nhánh tiết khí không bị chặn bởi cái nào** — đã xong và kiểm chứng được.
