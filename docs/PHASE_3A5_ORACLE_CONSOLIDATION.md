# Phase 3A.5 — Oracle consolidation & provenance hardening

> **KHÔNG CÓ PRODUCTION CODE.** `app/` không bị đụng · không Kotlin · không Room ·
> không UI · không dependency · không network trong app.
>
> Nhãn: **FACT** · **INFERENCE** · **HYPOTHESIS** · **UNVERIFIED/UNKNOWN** ·
> **LEGAL-UNKNOWN**.
>
> ⚠️ Toàn bộ tài liệu này là **đánh giá kỹ thuật và provenance, KHÔNG phải tư vấn
> pháp lý.**

**KẾT QUẢ: GATE VẪN BLOCKED.** Nhưng phase này sửa **hai kết luận sai của chính tôi**
và tìm được một ứng viên có thể gỡ hẳn blocker Meeus.

---

## 1. ⛔ ĐÍNH CHÍNH 1 — robots.txt không phải giấy phép

**Phase 3A.4 tôi viết:** *"robots.txt Allow ClaudeBot ⇒ việc truy cập tự động không
bị cấm"*, rồi xếp "truy vấn thủ công số lượng nhỏ" vào ô ✅. Cách trình bày đó **lỏng
và dễ gây hiểu nhầm**.

**Ba thứ hoàn toàn khác nhau:**

| Lớp | Là gì | lichviet.app |
|---|---|---|
| `robots.txt` | **Chỉ dẫn cho crawler.** Không phải hợp đồng, không phải giấy phép | Allow `*`, `ClaudeBot`, `anthropic-ai` |
| **Terms of Service** | **Ràng buộc hợp đồng** với người dùng | Mục 3 cấm sao chép/tái tạo/phân phối **"dữ liệu"** nếu không có văn bản cho phép |
| **Quyền tác giả / quyền dữ liệu** | Vấn đề **riêng**, tồn tại độc lập với hai thứ trên | **LEGAL-UNKNOWN** |

**Phân loại hành vi — bản đã sửa:**

| | Hành vi | Kết luận |
|---|---|---|
| **A** | Tra cứu thủ công như một người dùng bình thường | Được, **nhưng đây KHÔNG phải giấy phép tái sử dụng dữ liệu** |
| **B** | Crawl tự động | **KHÔNG** khi chưa có permission rõ ràng |
| **C** | Trích xuất hàng loạt | **KHÔNG** |
| **D** | Commit kết quả vào repository | **KHÔNG** |
| **E** | Đóng gói vào APK | **KHÔNG** |

**FACT.** Ở Phase 3A.4 tôi đã truy vấn 8 URL thủ công để xác định provenance
(loại A) và **không** commit giá trị nào làm test vector. Việc này nằm trong loại A,
nhưng lẽ ra phải nói rõ ngay từ đầu rằng nó không tạo ra quyền gì.

---

## 2. ⛔ ĐÍNH CHÍNH 2 — NASA và ERFA KHÔNG độc lập về thuật toán

Đây là sai lầm **nghiêm trọng hơn**, và nó làm suy yếu chính benchmark tôi tự hào
nhất ở Phase 3A.3.

**FACT.** Trang catalog NASA: *"The phases of the Moon as well as eclipses are based
on **Jean Meeus' Astronomical Algorithms** (Willmann-Bell, Inc., Richmond, 1998)."*

**FACT.** Header `moon98.c` của ERFA: *"a full implementation of the algorithm
published by **Meeus**"*, tham chiếu *Astronomical Algorithms*, 2nd ed., p337.

**INFERENCE — hệ quả.** Benchmark 2.474 điểm Sóc ở Phase 3A.3 so ERFA với NASA là
**so hai implementation của cùng một cuốn sách**, không phải hai phương pháp thiên
văn độc lập.

### Bốn loại độc lập phải tách riêng

| Loại | ERFA vs NASA |
|---|---|
| **Algorithmic independence** | ❌ **KHÔNG** — cùng Meeus |
| **Implementation independence** | ✅ Có — hai bên code riêng |
| **Data-source independence** | ❌ Không rõ nguồn hằng số | 
| **Institutional independence** | ✅ Có — IAU SOFA vs NASA/GSFC |

**Benchmark 3A.3 nói lại cho đúng:** đó là **implementation/institutional
cross-check**, **không phải** independent astronomical-method proof. Con số σ≈20 s là
chênh lệch giữa hai bản hiện thực cùng lý thuyết — nó **không** đo được sai số so với
thực tế thiên văn.

### Phần nào của 3A.3 vẫn đứng vững?

**Benchmark tiết khí vẫn đứng vững.** HKO dùng số liệu của **HM Nautical Almanac
Office**, không phải Meeus ⇒ đối chiếu ERFA `epv00` với HKO **là** kiểm chứng độc lập
thật. 72/72 mốc trong 33–38 giây vẫn có giá trị đầy đủ.

⇒ **Nhánh Mặt Trời: đã được kiểm chứng độc lập. Nhánh Mặt Trăng: chưa.**

---

## 3. Nguyên tắc sản phẩm — đã chốt

> **Nếp Nhà KHÔNG cố "giống đa số website".**
>
> Nếp Nhà ưu tiên **đúng theo quy tắc lịch Việt Nam có provenance mạnh nhất**.

Khi Nếp Nhà ≠ lichviet.app: **không sửa Nếp Nhà** chỉ để giống. Phải xét theo thứ tự:
bằng chứng chính thức/lịch sử → bằng chứng thiên văn → quy tắc lịch VN → provenance →
ancestry của implementation → tương thích trực tuyến. **Chỉ sửa engine khi bằng chứng
cho thấy engine sai.**

Đã ghi vào [LUNAR_CALENDAR.md](LUNAR_CALENDAR.md).

---

## 4. Provenance graph — theo ancestry, không theo Tier

```
LỚP THUẬT TOÁN                      LỚP HIỆN THỰC              LỚP DỮ LIỆU/SẢN PHẨM

Meeus, Astronomical Algorithms 1998
   ├──────────────► SOFA ──► ERFA `moon98`  ──► (ta có thể sinh bảng Sóc)
   ├──────────────► NASA/GSFC Espenak       ──► Moon Phases catalog 1901–2100
   └──────────────► Hồ Ngọc Đức (?)         ──► amlich.js ──► amlich.app
                                                          └─► hàng chục web lịch VN

Tích phân số từ quan trắc (KHÔNG Meeus)
   └──────────────► JPL DE440/DE441         ──► kernel SPICE (Mặt Trăng + Trái Đất)

HM Nautical Almanac Office
   └──────────────► HKO                     ──► bảng Dương–Âm TQ 1901–2100 · 24 tiết khí

Nhà nước Việt Nam
   └──────────────► Ban Lịch Nhà nước       ──► Trần Tiến Bình (sách in)
                                            └─► văn bản công bố nghỉ Tết

lichviet.app ── ancestry UNKNOWN (tính phía server, không quan sát được)
```

**INFERENCE — điều đồ thị này phơi ra:** ba trong bốn nguồn Mặt Trăng ta từng coi là
khác nhau (**ERFA, NASA, và có thể cả HND**) đều xuất phát từ **cùng một cuốn sách**.
Chỉ có **JPL DE440** nằm ở nhánh hoàn toàn khác.

### Bảng provenance

| Nguồn | Tác giả | Ancestry thuật toán | Ancestry dữ liệu | Điều khoản | Redistribution | Lớp độc lập | Tin cậy |
|---|---|---|---|---|---|---|---|
| ERFA `moon98` | IAU SOFA → NumFOCUS | **Meeus 1998 p337** | — | BSD-3 **VERIFIED** | Cho phép | cùng nhánh NASA | Cao (kỹ thuật) |
| ERFA `epv00`… | IAU SOFA → NumFOCUS | IAU/VSOP, **không Meeus** | — | BSD-3 **VERIFIED** | Cho phép | độc lập với Meeus | Cao |
| NASA Moon Phases | Espenak, NASA/GSFC | **Meeus 1998** | — | Permission tái tạo kèm ghi công **VERIFIED** | Cho phép | cùng nhánh ERFA | Cao (kỹ thuật) |
| **JPL DE440** | NASA/JPL | **Tích phân số từ quan trắc** | quan trắc mặt đất + tàu vũ trụ | **TSPA**, xem §6 | Kernel chưa sửa: cho phép | **Nhánh riêng** | Cao |
| HKO | Đài Thiên văn Hồng Kông | HM Nautical Almanac Office | tự tính | data.gov.hk **VERIFIED** | Thương mại được, kèm attribution | **Nhánh riêng** | Cao |
| Hồ Ngọc Đức | HND | Meeus + Reingold–Dershowitz | — | **Phi thương mại** | ⛔ Không | nhánh Meeus | — |
| amlich.app | Trần Trọng Thanh | **tự công bố là HND** | — | không rõ | ⛔ | nhánh HND | Thấp |
| lichviet.app | PPCLink / CP Lịch Việt | **UNKNOWN** | **UNKNOWN** | Cấm sao chép dữ liệu | ⛔ | **KHÔNG GÁN ĐƯỢC** | V3/V4 |
| Trần Tiến Bình | Ban Lịch Nhà nước | trong sách | — | sách có bản quyền | Chỉ trích vector tối thiểu | **Nhánh A** | Cao nhất |
| Văn bản nghỉ Tết | Chính phủ VN | quyết định hành chính | — | văn bản pháp quy | Được | **Nhánh A** | Cao nhất |

---

## 5. Composite oracle — 5 lớp

| Lớp | Tên | Nguồn | Đóng được gate nào |
|---|---|---|---|
| **V1** | Independent Tier-1 historical/official | Trần Tiến Bình · văn bản nhà nước | **G5 G6 G7 G9 G10** |
| **V2** | Independent astronomical | HKO · **JPL DE440** · ERFA `epv00` | **G14 G15 G16** (phần thiên văn) |
| **V3** | Vietnamese-calendar implementation | lichviet.app, lịch VN khác | ❌ không đóng gate nào |
| **V4** | Online black-box compatibility | nguồn ancestry UNKNOWN | ❌ chỉ phát hiện sai lệch |
| **V5** | Unresolved | nguồn mâu thuẫn | ❌ điều tra |

**Quy tắc tối hậu:** V3 + V4 dù bao nhiêu nguồn đồng ý cũng **không** nâng thành
verified. **Và từ nay: ERFA + NASA cùng đồng ý cũng chỉ là V2 một-nhánh**, không phải
hai xác nhận độc lập.

---

## 6. Meeus / ERFA — phân tích 9 lớp

| # | Lớp | Trạng thái |
|---|---|---|
| 6.1 | **Thuật toán toán học** | Ở nhiều hệ thống pháp luật, phương pháp/quy trình không thuộc đối tượng quyền tác giả. **LEGAL-UNKNOWN** — tôi không kết luận |
| 6.2 | **Biểu đạt văn bản trong sách** | **Được bảo hộ.** Willmann-Bell yêu cầu văn bản cho phép để tái tạo. Ta **chưa từng** mở sách |
| 6.3 | **Hằng số/bảng của Meeus** | Nằm trong `moon98.c`. Bảng số có thể được bảo hộ như *compilation* ⇒ **LEGAL-UNKNOWN** |
| 6.4 | **Hiện thực của SOFA** | IAU SOFA viết. SOFA Board cấp phép relicense — **VERIFIED** bằng email nguyên văn trong `INFO` |
| 6.5 | **Hiện thực của ERFA** | BSD-3-Clause — **LICENSE VERIFIED** |
| 6.6 | **Giấy phép BSD-3** | Cho phép dùng trong sản phẩm thương mại, không buộc công bố source — **TERMS VERIFIED** |
| 6.7 | **Số liệu sinh ra** | Kết quả chạy phần mềm. Là *sự kiện thiên văn*, thường không phải tác phẩm phái sinh của mã nguồn — **LEGAL-UNKNOWN** |
| 6.8 | **Công cụ build của ta** | Ngoài `app/`, không phân phối |
| 6.9 | **APK production** | Không chứa ERFA, không chứa code, chỉ chứa bảng số |

### 6.10 Câu hỏi trọng tâm: dev-tool khác gì runtime?

| | ERFA trong APK | ERFA chỉ ở máy dev |
|---|---|---|
| Phân phối mã nguồn/binary ERFA | **Có** ⇒ phải kèm notice BSD-3 | **Không** ⇒ không phát sinh nghĩa vụ phân phối |
| Người dùng cuối nhận gì | code | **chỉ số liệu** |
| Lớp 6.3 (bảng hằng số Meeus) đi vào sản phẩm | **Có** | **Không** |
| Lớp 6.7 (số liệu) đi vào sản phẩm | Có | Có |

**INFERENCE.** Dùng ERFA làm **công cụ sinh dữ liệu** thu hẹp bề mặt rủi ro đáng kể:
sản phẩm chỉ chứa lớp 6.7, không chứa 6.3/6.5. Nhưng **không xoá được** câu hỏi ở
lớp 6.7. **LEGAL-UNKNOWN.**

### 6.11 Dùng thẳng dữ liệu NASA thì khác gì?

**FACT.** NASA: *"Permission is freely granted to reproduce this data when accompanied
by an acknowledgment: 'Moon Phase Predictions by Fred Espenak, NASA/GSFC'."*

**INFERENCE.** Với phương án NASA, ta **không** chạm vào bất kỳ hiện thực nào của
Meeus — chỉ nhận **kết quả** kèm permission tường minh của bên công bố. Về provenance,
điều này **sạch hơn** phương án ERFA ở chỗ có một tuyên bố cho phép rõ ràng đúng cho
thứ ta dùng.

**Nhưng:** NASA vẫn *"based on Meeus"*, nên nếu policy của dự án là cách hiểu **B/C**
(cấm mọi thứ dựa trên Meeus) thì **NASA cũng bị loại**. Điểm này không đổi.

**Nhược điểm kỹ thuật:** NASA chỉ có độ phân giải **phút** ⇒ 5 ca sát biên vĩnh viễn
không giải được từ nguồn này.

---

## 7. 🆕 Phương án C — JPL DE440: đường thoát khỏi Meeus

**FACT.** DE440/DE441 là ephemeris hành tinh và Mặt Trăng của JPL, **sinh ra bằng
tích phân số quỹ đạo khớp với quan trắc mặt đất và tàu vũ trụ** — **không** phải
chuỗi giải tích của Meeus. Đây là **nhánh ancestry hoàn toàn khác**.

**FACT.** Phạm vi **DE440: 1550 → 2650** ⇒ phủ trọn 1901–2100.

**FACT (nguyên văn, naif.jpl.nasa.gov/naif/rules.html):**

> "Use of SPICE components in commercial products is allowed… **No fees or licensing
> are required.**"
>
> "Redistribution of SPICE kernels distributed by NAIF is permitted **as long as they
> have not been modified**."
>
> "Acknowledgement in your publications, tools and findings of use of SPICE or
> NAIF/PDS resources… is **encouraged**."

**⛔ ĐÍNH CHÍNH ngay trong phase này:** kết quả tìm kiếm ban đầu nói SPICE là *"public
domain"*. **Trang gốc của NAIF nói khác:** SPICE **không** được xếp là public domain,
mà là **TSPA** (Technology and Software Publicly Available). Tôi dùng phát biểu của
nguồn gốc, không dùng bản tóm tắt.

**INFERENCE.** Nếu dùng DE440 cho **cả Mặt Trăng lẫn Mặt Trời**, thì:

- Không có Meeus ở bất kỳ lớp nào.
- ERFA chỉ còn dùng cho **chuyển hệ quy chiếu và thang thời gian** — phần **không**
  dính Meeus.
- NASA và HKO trở thành **oracle đối chiếu độc lập thật sự**, không còn cùng nhánh.

**NOT VERIFIED — chưa làm:** chưa tải kernel, chưa benchmark, chưa đo sai số, chưa
kiểm 5 ca sát biên. Đây là **ứng viên**, không phải kết luận.

---

## 8. So sánh 4 phương án nguồn Mặt Trăng

| | **A — ERFA moon98** | **B — dữ liệu NASA** | **C — JPL DE440** | **D — lai** |
|---|---|---|---|---|
| Độ chính xác | σ≈20 s (so NASA, cùng nhánh) | phân giải **phút** | **cao nhất** (chuẩn tham chiếu) | cao |
| Ancestry Meeus | **Có** | **Có** | **KHÔNG** | tuỳ |
| Điều khoản | BSD-3 **VERIFIED** | Permission **VERIFIED** | TSPA, thương mại được, **VERIFIED** | — |
| Tái lập | ✅ script | ✅ tải catalog | ✅ kernel công khai | ✅ |
| Kích thước APK | ~5 KB bảng | ~5 KB bảng | ~5 KB bảng | ~5 KB |
| Độ phức tạp dev | Thấp (đã build xong) | Rất thấp | **Trung bình** — cần đọc kernel SPICE/`.bsp` ở máy dev | Trung bình |
| Giải được 5 ca sát biên? | ❌ (σ > margin) | ❌ (chỉ có phút) | **Có thể** — cần đo | có thể |
| Cho phép NASA/ERFA thành oracle độc lập? | ❌ | ❌ | ✅ **Có** | ✅ |
| Rủi ro | LEGAL-UNKNOWN lớp 6.3/6.7 | LEGAL-UNKNOWN lớp 6.7 | Chưa benchmark | — |

### Đề xuất (không tự quyết)

**Phương án C**, nếu benchmark đạt. Lý do không phải "chính xác hơn" mà là
**provenance**: nó là nguồn Mặt Trăng duy nhất **không** thuộc nhánh Meeus, và nó
biến NASA + ERFA từ "cùng nhánh" thành **hai oracle đối chiếu độc lập thật**.

Kiến trúc khi đó:

```
[máy dev]  JPL DE440 (Mặt Trăng + Trái Đất, TSPA)
           + ERFA (chỉ phần chuyển hệ/thang thời gian, không Meeus)
                    ↓  sinh bảng Sóc + tiết khí 1901–2100
           đối chiếu độc lập: HKO (nhánh HMNAO) · NASA (nhánh Meeus) · V1 khi có sách
                    ↓
[APK]      Kotlin thuần + bảng số (~15 KB) + quy tắc lịch Việt Nam
```

Không C, không NDK, không JNI, không network.

---

## 9. Tiêu chí PASS khách quan cho từng gate

Trước đây gate được mô tả bằng lời. Nay định nghĩa **điều kiện kiểm được**:

| Gate | Điều kiện PASS |
|---|---|
| **G5** | ≥100 cặp dương↔âm từ **V1**, rải đều ≥8 thập niên trong 1901–2100, engine khớp 100% |
| **G6** | Danh sách **toàn bộ** năm nhuận + tháng nhuận 1901–2100 từ **V1**, engine khớp 100%; hoặc ≥20 năm nhuận V1 rải đều + lập luận phủ sóng |
| **G7** | Độ dài (29/30) của **mọi tháng âm** trong ≥6 năm V1 chọn có chủ đích, gồm ≥2 năm nhuận; engine khớp 100% |
| **G8** | Cả 8 năm VN≠TQ có **phía VN từ V1** và **phía TQ từ V2 (HKO)**; engine khớp 100% và giải thích được nguyên nhân bằng 105°Đ vs 120°Đ |
| **G9** | Có **bằng chứng tài liệu** xác định offset dùng để **tính lịch** cho từng thời kỳ 1901–2100, gồm 1954–1967; hoặc phạm vi bảo đảm được thu hẹp về vùng có bằng chứng |
| **G10** | Ngày đầu của mọi tháng âm trong 1901, 1902, 2099, 2100 từ **V1**; hành vi ngoài phạm vi được định nghĩa và test |
| **G14** | Nguồn thiên văn có **TERMS VERIFIED**, benchmark với **≥1 oracle khác nhánh**, script tái lập |
| **G15** | Mọi điểm Sóc có margin < 2× sai số phương pháp đều được **V1 phân xử**, hoặc engine từ chối trả lời cho chúng một cách tường minh |
| **G16** | Mô hình ΔT có nguồn, chính sách nội suy ghi rõ, phân tích độ nhạy có số đo |

---

## 10. Có bỏ được yêu cầu sách không?

**Trả lời: KHÔNG bỏ được, nhưng đã xác định được chính xác vì sao — và điều đó thu
hẹp yêu cầu.**

Đối chiếu 9 tiêu chí ở §9 với những gì composite evidence làm được:

| Gate | Nguồn không-phải-sách có đủ không? |
|---|---|
| G14, G16 | ✅ **Đủ** — HKO + JPL/ERFA + NASA |
| G15 | 🟡 Một phần — thiên văn giải được phần lớn, **5 ca sát biên thì không** |
| G8 | 🟡 Phía TQ ✅ HKO; **phía VN thiếu V1** cho 7/8 năm |
| **G5, G6, G7, G10** | ❌ **Không.** Đây là **quy tắc lịch Việt Nam**, không phải thiên văn. Không nguồn thiên văn nào chứng minh được "nhà nước Việt Nam công nhận tháng này nhuận" |
| **G9** | ❌ Không — cần tài liệu lịch sử |

**INFERENCE — điều then chốt.** Bằng chứng thiên văn chứng minh được **thời điểm Sóc
và trung khí**. Nó **không** chứng minh được **cách Việt Nam áp quy tắc lên các thời
điểm đó**. Hai chuyện khác nhau. Một engine có thiên văn hoàn hảo vẫn có thể sai lịch
Việt Nam nếu áp quy tắc sai — và **không nguồn thiên văn nào phát hiện được điều đó**.

**Nhưng yêu cầu đã thu hẹp:** không cần "mua sách và số hoá toàn bộ". Chỉ cần **tra
đúng những gì §9 liệt kê** — ước tính vài trang, làm được tại thư viện.

---

## 11. 1967-07-07 — vẫn UNVERIFIED

| Nguồn | Kết quả | Nhánh |
|---|---|---|
| NASA (17:00 UT, làm tròn phút) | Sóc ⇒ 08/07 | Meeus |
| ERFA `moon98` (16:59:41 UT) | Sóc ⇒ **07/07** | **cùng nhánh Meeus** |
| lichviet.app | 08/07 | UNKNOWN |

**INFERENCE.** Sau đính chính §2, tình hình **xấu đi chứ không tốt lên**: NASA và
ERFA **cùng nhánh**, nên "hai nguồn nói 08/07" thực chất là **một nhánh nói 08/07 với
hai giọng**, và chính hai giọng đó lại **mâu thuẫn nhau** (16:59:41 vs 17:00).

**Chưa tìm được** nguồn lưu trữ/học thuật/nhà nước nào cho tháng 7/1967.

**Giữ UNVERIFIED. Không hardcode. Không đếm phiếu.**

**HYPOTHESIS đáng thử:** JPL DE440 là nguồn **khác nhánh** duy nhất có thể phân xử ca
này — nhưng phải benchmark trước.

---

## 12. 1954–1967 — không đổi

**G9 vẫn BLOCKED.** Toàn văn 121-CP vẫn 403 ở mọi đường đã thử. Chưa tiếp cận được
lịch in thời kỳ đó.

---

## 13. Gate matrix

| # | Gate | Trạng thái | Thay đổi ở 3A.5 |
|---|---|---|---|
| G1–G4, G11–G13 | | ✅ **PASS** | — |
| **G5** | Oracle độc lập | ⛔ **BLOCKED** | Có **tiêu chí PASS khách quan** (§9) |
| **G6** | Tháng nhuận | ⛔ **BLOCKED** | Có tiêu chí |
| **G7** | 29/30 ngày | ⛔ **BLOCKED** | Có tiêu chí |
| **G8** | VN ≠ TQ | 🟡 **PARTIAL** | Có tiêu chí |
| **G9** | Múi giờ lịch sử | ⛔ **BLOCKED** | — |
| **G10** | Biên | ⛔ **BLOCKED** | Có tiêu chí |
| **G14** | Provenance thiên văn | 🟡 **PARTIAL → có đường mở** | **JPL DE440** có thể gỡ hẳn blocker Meeus |
| **G15** | Độ chính xác | 🟡 **PARTIAL → yếu đi** | Đính chính §2: NASA↔ERFA cùng nhánh ⇒ benchmark Mặt Trăng **không phải** kiểm chứng độc lập |
| **G16** | ΔT | ✅ **PASS** | — |

### ⛔ CHƯA ĐƯỢC IMPLEMENT ENGINE

Không gate nào mở. **G15 thực chất yếu đi** sau đính chính — đây là kết quả trung
thực, không phải tiến bộ.

---

## 14. Việc tiếp theo

**Cần chủ dự án quyết:**

1. **Policy Meeus** — vẫn treo. Nhưng nếu phương án **C (JPL DE440)** khả thi thì câu
   hỏi này **có thể trở nên không cần thiết**, vì không còn Meeus ở đâu cả.
2. **Có cho phép tải kernel DE440 không?** File `de440s.bsp` (1849–2150) khoảng
   **32 MB**. Máy đang dùng **data di động qua hotspot A32** ⇒ tôi **không tự tải**.

**Tôi làm được ngay khi được duyệt:**

3. Benchmark DE440: tính Sóc, so với ERFA và NASA (nay là **ba nhánh**, trong đó
   DE440 khác nhánh), kiểm 5 ca sát biên. Đây là việc có giá trị cao nhất còn lại về
   mặt kỹ thuật.
4. Tiếp tục truy 121-CP và lịch in 1954–1967.

**Cần chủ dự án cung cấp:** tra sách theo đúng danh sách §9 — nay đã là danh sách
**cụ thể, đếm được**, không còn là "cần cả cuốn sách".
