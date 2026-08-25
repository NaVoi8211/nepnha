# Phase 3A.3 — Astronomical Backend Benchmark

> **KHÔNG CÓ PRODUCTION CODE.** `app/` không bị đụng, không Kotlin, không dependency,
> không Room, không UI. Benchmark 2026-08-25.
>
> Nhãn: **FACT** · **VERIFIED** · **INFERENCE** · **HYPOTHESIS** · **UNKNOWN**.
>
> Tái lập: `tools/benchmark_erfa_astronomy/`

---

## 1. Executive conclusion

**ERFA đủ chính xác cho lịch âm Việt Nam 1901–2100** — đã đo, không phải cảm nhận:

| Đại lượng | Đối chiếu với | Kết quả |
|---|---|---|
| Điểm Sóc | NASA (2.474 mốc, 200 năm) | trung vị **18,9 s** · p95 **49,3 s** · max **78,9 s** |
| Tiết khí | HKO (72 mốc, 2026–2028) | max **33 s** · **0** ca sát biên |
| Rủi ro đổi ngày | — | **6/2.474 (0,24%)** có margin < 79 s |
| Độ nhạy ΔT | — | ΔT sai 5 s ⇒ **0** ngày đổi; sai 60 s ⇒ **1** ngày đổi |

**NHƯNG có hai vật cản, cả hai đều cần bạn quyết, không phải tôi:**

1. ⚠️ **`eraMoon98` là implementation thuật toán MEEUS** — tài liệu ERFA ghi thẳng
   *"Meeus, J., Astronomical Algorithms, 2nd edition, Willmann-Bell, 1998, p337"*.
   Dự án đã cấm Meeus. §4.2.
2. ⛔ **Oracle Tier 1 vẫn thiếu** (G5/G6/G7/G10) và **G9 vẫn BLOCKED**.

**Không đề nghị mở gate.** Benchmark chỉ trả lời câu hỏi *"ERFA có đủ chính xác
không"* — câu trả lời là **có**. Nó không trả lời *"lịch ta tính ra có đúng lịch Việt
Nam không"*.

---

## 2. Đính chính Phase 3A.2 — kết luận trước đây SAI

**Phase 3A.2 viết:**

> "SOFA không có ephemeris Mặt Trăng… ERFA chỉ giải được một nửa bài toán."

**FACT — sai.** `moon98.c` tồn tại trong ERFA, 23.973 byte, header ghi:

> **eraMoon98** — *Approximate geocentric position and velocity of the Moon.*
> *n.b. Not IAU-endorsed and without canonical status.*
> Given: TT date · Returned: `pv[2][3]` Moon p,v, GCRS (au, au/d)

Sai vì Phase 3A.2 chỉ tra danh sách chức năng tổng quát của SOFA thay vì đọc thư mục
source. Bài học: **liệt kê thư mục, đừng tin trang giới thiệu.**

---

## 3. ERFA routine inventory

**VERIFIED** — tải trực tiếp từ `liberfa/erfa`, biên dịch thành công **250/251** file
(`erfaversion.c` cần macro của build system, không cần cho ta), `liberfa.a` = 1,05 MB.

| Routine | Vai trò | Thang thời gian vào | Có dùng? |
|---|---|---|---|
| `eraMoon98` | Vị trí Mặt Trăng địa tâm, GCRS | **TT** | ✅ điểm Sóc |
| `eraEpv00` | Vị trí Trái Đất nhật tâm/tâm khối | TDB (≈TT) | ✅ hoàng kinh Mặt Trời |
| `eraEqec06` | Xích đạo ICRS → hoàng đạo trung bình của ngày | TT | ✅ **3A.2 bỏ sót** |
| `eraNut06a` | Chương động Δψ, Δε | TT | ✅ hiệu chỉnh sang biểu kiến |
| `eraC2s` | Vector → toạ độ cầu | — | ✅ |
| `eraAnp` / `eraAnpm` | Chuẩn hoá góc | — | ✅ |
| `eraD2dtf` | JD → lịch + giờ | — | ✅ (chỉ trong công cụ) |
| `eraObl06` | Độ nghiêng hoàng đạo | TT | ❌ **không cần** — `eqec06` đã bao gồm |
| `eraDat`, `eraUtctai`, `eraTaitt` | Chuỗi UTC→TAI→TT | — | ❌ **không cần** — xem §8 |
| `eraPlan94` | Vị trí hành tinh | — | ❌ không liên quan |

**INFERENCE.** Chỉ cần **7 routine**, không phải 10 như Phase 3A.2 ước tính. Ba
routine chuyển thang thời gian bị loại nhờ phân tích ΔT ở §8 — đó cũng là lý do
**G16 không còn là blocker**.

---

## 4. License / provenance

### 4.1 Bảng provenance

| Routine | File | Heritage | License | Thương mại? | Attribution | Ship source? |
|---|---|---|---|---|---|---|
| `eraMoon98` | `moon98.c` | SOFA → **thuật toán Meeus 1998 p337** | BSD-3 (ERFA) | ✅ | ✅ | ❌ không bắt buộc |
| `eraEpv00` | `epv00.c` | SOFA (VSOP-type) | BSD-3 | ✅ | ✅ | ❌ |
| `eraEqec06` | `eqec06.c` | SOFA, IAU 2006 | BSD-3 | ✅ | ✅ | ❌ |
| `eraNut06a` | `nut06a.c` | SOFA, IAU 2000A/2006 | BSD-3 | ✅ | ✅ | ❌ |
| `eraC2s`, `eraAnp`, `eraAnpm` | | SOFA | BSD-3 | ✅ | ✅ | ❌ |

**FACT.** Toàn bộ dưới **một** file `LICENSE` BSD-3-Clause của ERFA (NumFOCUS
Foundation), đã đọc nguyên văn ở Phase 3A.2. Nghĩa vụ: giữ thông báo bản quyền +
disclaimer; không dùng tên SOFA Board/IAU để quảng bá. **Không có nghĩa vụ công bố
source** (BSD ≠ GPL).

**FACT.** Kết luận "commercial OK" lấy từ **file LICENSE chính thức của ERFA**, không
từ package manager hay wrapper.

### 4.2 ⚠️ Xung đột cần chủ dự án quyết: `eraMoon98` ↔ lệnh cấm Meeus

**FACT.** Header `moon98.c`:

> *"This function is a full implementation of the algorithm published by Meeus"*
> **References:** *Meeus, J., Astronomical Algorithms, 2nd edition, Willmann-Bell,
> 1998, p337.*

Dự án đã chốt: **không dùng Meeus.**

Hai cách đọc, tôi **không tự chọn**:

| | Lập luận |
|---|---|
| **Cho phép** | Ta không đọc sách, không chép code từ sách. Ta dùng một implementation do IAU SOFA viết và ERFA phân phối dưới BSD-3. Trách nhiệm về việc SOFA hiện thực thuật toán Meeus thuộc về SOFA/IAU, không thuộc Nếp Nhà. Ranh giới "thuật toán vs biểu đạt" mà chính dự án đã dựng ở Phase 3A ủng hộ cách đọc này |
| **Không cho phép** | Lệnh cấm là "không dùng Meeus", không phải "không chép code Meeus". Dùng `moon98` là dùng thuật toán Meeus, dù qua tay ai |

**Nếu bạn chọn "không cho phép"**, còn hai đường:

- **(a)** Chỉ dùng NASA cho điểm Sóc (NASA cũng dựa trên Meeus — cùng vấn đề, ở dạng
  *dữ liệu* thay vì *thuật toán*).
- **(b)** Tìm nguồn Mặt Trăng khác: ELP/MPP02, hoặc JPL ephemeris. **UNKNOWN** —
  chưa nghiên cứu giấy phép.

> ⚠️ Đây là ranh giới chính sách, không phải kỹ thuật. Tôi nêu ra, bạn quyết.

---

## 5. Độ chính xác `eraMoon98`

**FACT** (nguyên văn header):

> Comparisons with ELP/MPP02 over the interval **1950-2100** gave RMS errors of
> **2.9 arcsec** in geocentric direction… The worst case errors were **18.3 arcsec**.

**FACT** (Note 4): thuật toán cần **dynamical time (TT hoặc TDB)**. *"UT cannot be
used without incurring significant errors (30 arcsec in the present era)."*

**INFERENCE — quy đổi sang thời gian.** Elongation Trăng−Trời biến thiên
≈ 12,19°/ngày = 1.830″/giờ.

| Sai số vị trí | Sai số thời gian điểm Sóc |
|---|---|
| RMS 2,9″ | ≈ **5,7 giây** |
| Worst 18,3″ | ≈ **36 giây** |

**UNKNOWN.** ERFA không công bố sai số cho **1901–1949**. Đo thực nghiệm ở §6 cho
thấy **không xấu đi** ở giai đoạn đó.

---

## 6. New Moon benchmark

**VERIFIED.** 2.474 điểm Sóc, 1901–2100, đối chiếu NASA. NASA là **oracle độc lập**,
không dùng để hiệu chỉnh ERFA.

```
|lệch| ERFA − NASA:   trung vị 18,9 s   p95 49,3 s   max 78,9 s (năm 1954)
```

Phân bố theo thời kỳ — **không có suy giảm trước 1950**:

| Thời kỳ | n | trung vị | p95 | max |
|---|---|---|---|---|
| 1901–1949 | 606 | 18,8 s | 51,2 s | 65,7 s |
| 1950–2100 *(khoảng ERFA công bố)* | 1.868 | 18,9 s | 48,6 s | 78,9 s |

**INFERENCE.** 78,9 s lớn hơn tổng ước tính (36 s của `moon98` + 30 s làm tròn của
NASA = 66 s). ⇒ NASA có lẽ dùng lý thuyết Mặt Trăng chính xác hơn bản rút gọn trong
`moon98`. **UNKNOWN** — NASA không công bố chi tiết.

### Margin tới ranh giới ngày Việt Nam (17:00:00 UTC)

```
nhỏ nhất 6,5 s | p1 = 406 s (6,8 phút) | p5 = 2.587 s (43 phút) | trung vị 5,9 giờ
số ca margin < 79 s (= sai số lớn nhất quan sát được):  6 / 2.474  = 0,24%
```

**INFERENCE.** Với **99,76%** điểm Sóc, sai số phương pháp nhỏ hơn margin hàng chục
đến hàng nghìn lần ⇒ **không thể làm đổi ngày âm**.

---

## 7. Solar-term benchmark

**VERIFIED.** 72 tiết khí (2026–2028) đối chiếu HKO — nguồn duy nhất công bố **kèm
giờ**, đúng 3 năm như Phase 3A.1 đã phát hiện.

```
|lệch| ERFA − HKO:  max 33 giây  trên 72/72 mốc
số ca BORDERLINE (margin < 2 phút tới nửa đêm VN):  0
số ca FAIL:                                         0
```

**FACT.** 33 s nằm trong chính độ làm tròn phút của HKO (±30 s).

**FACT.** Hoàng kinh Mặt Trời phải là **biểu kiến**: có hiệu chỉnh thời gian truyền
sáng (~8,3 phút thời gian) **và** chương động Δψ (~7 phút thời gian). Bỏ một trong
hai là sai tới ~15 phút — đủ đổi ngày ở ca sát biên. Công cụ benchmark đã làm cả hai.

**INFERENCE.** Phần Mặt Trời **sạch hơn phần Mặt Trăng**: `epv00` không dính Meeus,
sai số nhỏ hơn, và 0 ca sát biên trong mẫu.

---

## 8. ΔT analysis — G16 KHÔNG phải blocker

### 8.1 Phân biệt thang thời gian

| | Là gì | Ai cần |
|---|---|---|
| **TT** | Terrestrial Time | `eraMoon98`, `eraEpv00`, `eraNut06a`, `eraEqec06` **cần** |
| **UT1** | Theo vòng quay Trái Đất | Cái quyết định **ngày dân sự** |
| **UTC** | UT1 + giây nhuận, chỉ có **từ 1960** | ❌ **ta không cần** |
| **ΔT** | TT − UT1 | Cầu nối giữa hai thứ trên |
| **TAI−UTC** | Giây nhuận (`eraDat`) | ❌ **ta không cần** |

**INFERENCE — mấu chốt.** Muốn biết điểm Sóc rơi vào ngày nào ở Việt Nam, ta cần
**UT1**, không cần UTC. Vậy thì `eraDat` / `eraUtctai` / `eraTaitt` — cái gây ra G16
vì không định nghĩa trước 1960 — **hoàn toàn không nằm trên đường đi**. Chỉ cần
`UT = TT − ΔT`.

**FACT.** Catalog NASA công bố **ΔT theo từng năm** trong cùng file, cùng giấy phép.

**FACT.** Thời điểm pha Mặt Trăng của NASA đã ở **Universal Time** — NASA đã áp ΔT rồi.

### 8.2 Đo độ nhạy thay vì đoán

**VERIFIED.** Nhiễu ΔT rồi đếm số ngày âm đổi, trên cả 2.474 điểm Sóc:

| ΔT sai | Số ngày âm đổi |
|---|---|
| +1 s | **0** / 2.474 |
| +5 s | **0** / 2.474 |
| +10 s | 1 / 2.474 (0,04%) |
| +30 s | 1 / 2.474 |
| +60 s | 1 / 2.474 |

**INFERENCE.** ΔT lịch sử 1901–1960 được xác định từ quan trắc, sai số cỡ **dưới
1 giây** — thấp hơn hai bậc so với ngưỡng gây ảnh hưởng. ⇒ **G16 chuyển từ BLOCKED
sang PASS.**

> **NOT VERIFIED:** con số "dưới 1 giây" tôi chưa tra từ nguồn IERS. Nhưng phép đo
> trên cho thấy **kể cả sai 60 giây cũng chỉ đổi đúng 1 ngày** trong 200 năm ⇒ kết
> luận không phụ thuộc con số đó.

---

## 9. Năm ca NASA `17:00 UT`

**VERIFIED.** Đây là câu trả lời định lượng cho câu hỏi "ERFA có phân giải được không":

| Ngày | NASA | ERFA | margin | Ngày âm VN |
|---|---|---|---|---|
| 1944-06-20 | 17:00 | **16:59:52** | 7,9 s | ERFA: 20/06 · NASA: 21/06 ⚠️ |
| 1967-07-07 | 17:00 | **16:59:41** | 18,6 s | ERFA: 07/07 · NASA: 08/07 ⚠️ |
| 2054-05-07 | 17:00 | **17:00:06** | 6,5 s | cùng 08/05 |
| 2077-11-15 | 17:00 | **16:59:25** | 34,7 s | ERFA: 15/11 · NASA: 16/11 ⚠️ |
| 2085-10-18 | 17:00 | **16:59:22** | 37,5 s | ERFA: 18/10 · NASA: 19/10 ⚠️ |

**INFERENCE — bốn ca "khác nhau" KHÔNG phải mâu thuẫn thật.** NASA làm tròn phút:
giá trị thật 16:59:52 sẽ được in ra là "17:00". Hai nguồn **nhất quán**; cái khác
nhau là do tôi phân loại "17:00:00" về ngày sau.

**INFERENCE — nhưng ERFA cũng KHÔNG giải được.** Margin 6,5–37,5 s **nhỏ hơn** sai số
quan sát được giữa hai phương pháp (tới 78,9 s). Hai ước lượng độc lập lệch nhau
nhiều hơn khoảng cách tới ranh giới.

**KẾT LUẬN: 5 ca này vẫn UNVERIFIED.** Không hardcode ngoại lệ, không thêm
`InsufficientPrecision` vào API. Cần **nguồn thứ ba có thẩm quyền** — lịch Trần Tiến
Bình. Bốn trong năm ca nằm ngoài vùng phủ của văn bản nghỉ Tết.

**FACT (mục K).** Đã tìm bản NASA mịn hơn phút: `phase2001gmt.html` cũng chỉ có phút.
Không tìm được bản nào có giây.

---

## 10. Historical timezone

**Không có tiến triển ở phase này.** Chỉ thử được một hướng:

**FACT.** Trang chính văn 121-CP trên thuvienphapluat trả **HTTP 403** — không đọc được.

**Chưa làm** (thiếu thời gian, phase này dồn cho benchmark): lịch nhà nước in
1954–1967, tài liệu Ban Lịch Nhà nước, lịch tháng 7/1967.

**G9 giữ nguyên BLOCKED.** Không chọn P1 hay P2.

---

## 11. Dataset-first vs runtime astronomy

| Tiêu chí | **A — dataset-first** | **B — chạy trong app** |
|---|---|---|
| License | BSD-3 dùng ngoài app; dữ liệu kết quả là của ta | BSD-3, phải ship notice trong app |
| Kích thước APK | ~7.300 mốc ≈ **vài chục KB** | ERFA .so cho 4 ABI, hoặc port `epv00` 150 KB |
| C / NDK / JNI | **Không cần** | **Bắt buộc** |
| Complexity | Thấp | Cao — NDK, ABI, crash native |
| Determinism | **Tuyệt đối** (bảng cố định) | Phụ thuộc dấu phẩy động từng thiết bị |
| Testability | Bảng so bằng mắt với lịch giấy | Phải test trên thiết bị thật |
| Bảo trì | Sinh lại khi cần | Nâng cấp NDK, ABI mới |
| Rủi ro Android | **Không có** | 64 KB method limit (Kotlin port), ABI |
| CPU/RAM (A32) | Tra bảng — không đáng kể | Tính lượng giác mỗi lần |
| Reproducibility | Script + input công khai ⇒ **tái lập được** | Khó tái lập chính xác |
| Audit dữ liệu | **In ra đối chiếu được** | Không |

**INFERENCE. Option A thắng ở mọi tiêu chí quan trọng** với dự án này.

Phạm vi cố định 1901–2100 là *hệ quả của độ phủ oracle*, không phải giới hạn kỹ
thuật — nên việc bảng dữ liệu cũng cố định trong khoảng đó **không mất gì**.

---

## 12. Recommended architecture

```
   [ NGOÀI app — máy dev, chạy một lần, có script tái lập ]

   ERFA (C, BSD-3)  ──►  2.474 điểm Sóc          (nếu được duyệt dùng moon98)
                    ──►  4.800 thời điểm tiết khí (epv00 — không dính Meeus)
   NASA catalog     ──►  ΔT theo năm  +  oracle đối chiếu độc lập
   HKO              ──►  oracle đối chiếu tiết khí (3 năm) và lịch Trung Quốc
                                  │
                                  ▼   asset vài chục KB, kèm checksum
   [ TRONG app — Kotlin thuần: không C, không NDK, không JNI ]

   quy tắc lịch Việt Nam:  Sóc = mùng 1 · trung khí · tháng nhuận · 105°Đ · múi giờ lịch sử
                                  │
                                  ▼
                       VietnameseLunarCalendar
```

**Nếu bạn cấm `moon98` vì dính Meeus:** thay nhánh Mặt Trăng bằng **dữ liệu NASA
trực tiếp** (2.474 mốc, độ phân giải phút). Khi đó 5 ca `17:00` **chắc chắn** cần
oracle bên thứ ba; phần tiết khí không bị ảnh hưởng.

---

## 13. Remaining blockers

| # | Blocker | Vì sao chưa mở |
|---|---|---|
| B1 | **Oracle Tier 1 lịch âm VN** | Chưa có sách Trần Tiến Bình. Chặn G5/G6/G7/G10 |
| B2 | **Múi giờ miền Bắc 1954–1967** | 121-CP bị 403; chưa tiếp cận lịch in thời kỳ đó |
| B3 | **`moon98` ↔ lệnh cấm Meeus** | **Quyết định chính sách của chủ dự án** |
| B4 | 5 ca `17:00 UT` | Cả NASA lẫn ERFA đều không phân giải được |

**Không còn là blocker:** G16 (ΔT) — đã chứng minh bằng đo đạc.

---

## 14. Gate matrix

| # | 3A.2 | **3A.3** | Căn cứ |
|---|---|---|---|
| G1–G4, G11–G13 | ✅ | ✅ **PASS** | |
| G5 Oracle độc lập | ⛔ | ⛔ **BLOCKED** | Chưa có sách |
| G6 Tháng nhuận | ⛔ | ⛔ **BLOCKED** | |
| G7 29/30 ngày | ⛔ | ⛔ **BLOCKED** | |
| G8 VN ≠ TQ | 🟡 | 🟡 **PARTIAL** | 1985 PASS; 7 năm còn lại chưa |
| G9 Múi giờ lịch sử | ⛔ | ⛔ **BLOCKED** | 121-CP HTTP 403 |
| G10 Biên 1901/2100 | ⛔ | ⛔ **BLOCKED** | |
| **G14** Nguồn thiên văn | 🟡 | 🟡 **PARTIAL** | ✅ Kỹ thuật đã chứng minh · ⛔ **vướng chính sách Meeus (B3)** |
| **G15** Mô hình độ chính xác | 🟡 | 🟡 **PARTIAL** | ✅ Đã lượng hoá toàn bộ · ⛔ 5 ca vẫn UNVERIFIED |
| **G16** ΔT 1901–1960 | ⛔ | ✅ **PASS** | Đo: ΔT sai 5 s ⇒ 0 ngày đổi |

### ⛔ GATE VẪN BLOCKED — KHÔNG IMPLEMENT ENGINE

G14 **không chuyển PASS** dù benchmark tốt: còn vướng chính sách Meeus. G15 **không
chuyển PASS**: 5 ca chưa xác minh được. Bốn gate oracle vẫn trống.

---

## 15. Exact next action

**Cần bạn quyết — chặn mọi thứ khác:**

1. **`eraMoon98` có được dùng không?** (§4.2). Đây là quyết định chính sách. Trả lời
   xong mới chốt được kiến trúc nhánh Mặt Trăng.

**Cần bạn cung cấp:**

2. **Sách Trần Tiến Bình** — mở G5/G6/G7/G10 và 4/5 ca của G15.

**Tôi làm được ngay khi được duyệt, không cần chờ gì:**

3. Sinh thử bảng tiết khí đầy đủ 1901–2100 bằng `epv00`, đo kích thước asset thật —
   **nhánh này không dính Meeus nên làm được bất kể câu trả lời ở mục 1**.
4. Tải bảng HKO cho 7 năm VN≠TQ còn lại → củng cố G8.
5. Tiếp tục truy 121-CP qua đường khác; tìm lịch nhà nước 1954–1967 → G9.
