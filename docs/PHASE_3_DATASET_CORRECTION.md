# Phase 3 — Sửa dataset có kiểm soát

> Xử lý blocker của [PHASE_3_FINAL_AUDIT.md](PHASE_3_FINAL_AUDIT.md). Được uỷ quyền
> mở lại freeze **chỉ cho hai lỗi dưới đây**.
>
> ⚠️ Đánh giá kỹ thuật/provenance. **Không phải tư vấn pháp lý.**

```
sha256   a9b36e14e9efc455a8341e631b9402a81775ec55aa1152eec8c7816dc02f3ba0   (cũ)
     →   b9f9613a0d1974ac82a024a737b8b40cbd1588869881db13c90f4c90a020f33d   (mới)
kích thước 19.946 B — không đổi        Sóc 2.534 · trung khí 2.448 — không đổi
```

**Không đổi:** NASA vẫn là nguồn điểm Sóc · ERFA vẫn là nguồn trung khí · 105°Đ ·
UTC+7 · kiến trúc và API Kotlin · không Meeus/`moon98` trên đường sản xuất.

---

## A. Hai lỗi

| | Lỗi | Tầng |
|---|---|---|
| **A** | ΔT lấy từ cột phân giải **PHÚT** của trang NASA | dữ liệu vào |
| **B** | Lượng tử hoá bằng `round` thay vì `floor` | biểu diễn |

---

## B. Lỗi A — nguyên nhân thật khác với chẩn đoán ban đầu

Audit trước ghi *"regex ΔT sai"*. **Chẩn đoán đó chưa đúng.** Regex đọc **đúng** cột.
Vấn đề là **bản thân cột chỉ có độ phân giải phút**:

```
' 1901       ...      Jan 12  20:38      00h00m'
' 1952       ...      Jan 20  06:09      00h01m'
```

Suốt 1901–2000 cột chỉ nhận hai giá trị `00h00m` và `00h01m`. Pipeline này quyết định
ngày âm bằng những khoảng cách **vài giây**, nên cột đó không thể làm đầu vào.

**Vì vậy cách sửa không phải "parse cho khéo hơn"** mà là dùng một nguồn ΔT có đủ
phân giải: **Espenak & Meeus, "Polynomial Expressions for Delta T", NASA/GSFC**, phụ
lục *Five Millennium Canon of Solar Eclipses* (NASA/TP-2006-214141). Cùng một cơ quan
đã cung cấp điểm Sóc, nên **chính sách NASA-first không đổi**. Hiện thực:
[`tools/deltat.py`](../tools/deltat.py).

### Cột ΔT của NASA giờ thành nguồn kiểm chứng độc lập

Xác định bằng thực nghiệm: **cột NASA = `round(ΔT / 60)` phút**. Các năm cột nhảy bậc
là **1952, 2047, 2077** — đúng chỗ đa thức cắt 30 s, 90 s, 150 s. Khớp **199/200 năm**;
riêng 2046 đa thức cho 90,02 s, sát điểm hoà 90 s, NASA làm tròn xuống.

⇒ Đây là **xác nhận độc lập** rằng hiện thực ΔT của Nếp Nhà khớp ΔT của chính NASA,
trong giới hạn phân giải NASA công bố. Kiểm tự động ở `verify_lunar_dataset.py §3`.

---

## C. Lỗi B — vì sao `floor` là đúng, không phải sở thích

Ranh giới ngày dương lịch Việt Nam nằm **đúng tại một mốc phút**: 17:00:00Z = 00:00
UTC+7. Phút *chứa* một thời điểm luôn nằm **trọn** về một phía mốc đó.

> ⇒ `floor` **không bao giờ** làm đổi ngày Việt Nam. `round` thì có — bất cứ khi nào
> sự kiện nằm trong 30 giây **trước** ranh giới.

Sai số dư của `floor` luôn thuộc **[0, 60) giây**, luôn lùi về trước, không bao giờ
vượt biên. Kiểm ở `tools/test_generator.py §C, §D` với các mốc 16:59:29,9 · 16:59:30,0
· 16:59:59,9 · 17:00:00,0 · 17:00:00,1 · 17:00:30,5.

Đo trên dataset thật: **nếu dùng `round` thì 2 trung khí sẽ sai ngày** (1953-06-21 và
2038-07-22).

---

## D. Kiểm toán ΔT toàn dải 1901–2100

| | |
|---|---|
| Nguồn | Espenak & Meeus, *Polynomial Expressions for Delta T*, NASA/GSFC — **MÔ HÌNH**, không phải số đo |
| Biến | `y = year + (month − 0,5) / 12` |
| Nội suy | không — đa thức khớp từng đoạn, đánh giá tại ngày của chính sự kiện |
| Số điểm dùng | 2.448 (một cho mỗi trung khí) |
| Dải | **−3,93 s … 209,73 s** |
| Giá trị phân biệt | **2.448 / 2.448** — không có ca nào trùng |
| Sập về {0, 60, 120, 180} | **không có giá trị nào** |
| Liên tục tại mối nối | nhảy lớn nhất **0,1205 s** tại 2050 (đoạn 2050–2150 khác dạng hàm) |
| Dung sai test | ±1,5 s so với giá trị công bố; ±30 s so với cột NASA |

| Năm | ΔT (s) | cột NASA |
|---|---:|---:|
| 1901 | −0,61 | 0 |
| 1914 | 16,52 | 0 |
| 1920 | 21,64 | 0 |
| **1938** | **24,05** | 0 |
| 1944 | 26,66 | 0 |
| 1954 | 30,87 | 60 |
| 1967 | 37,86 | 60 |
| 1985 | 54,64 | 60 |
| 2000 | 64,02 | 60 |
| **2026** | **75,41** | 60 |
| 2054 | 102,31 | 120 |
| 2077 | 151,47 | 180 |
| 2085 | 169,36 | 180 |
| 2100 | 204,02 | 180 |

### ΔT năm 2026 — làm rõ dứt điểm

Uỷ quyền của audit trước yêu cầu `2026 ≈ 69,5 s`. **Yêu cầu đó đã được rút** và không
còn hiệu lực.

Đường sản xuất dùng **mô hình đa thức NASA/Espenak**, đoạn 2005–2050:

```
ΔT = 62,92 + 0,32217·t + 0,005589·t²        t = y − 2000
```

Với 2026-08 mô hình cho **75,46 s**, khớp con số **75,4 s** mà tài liệu nhật thực của
NASA nêu cho 2026-08-12. **Đây không phải lỗi.**

Ba giá trị NASA tự nêu trên trang nguồn đều khớp hiện thực:

| | Tính được | NASA nêu |
|---|---:|---:|
| ΔT(2010) | 66,94 s | 66,9 s |
| ΔT(2050) | 93,00 s | 93 s |
| ΔT(2026-08) | 75,46 s | 75,4 s |

**Không ép hiện thực về 69,5 s.** Con số ~69 s là **ΔT quan trắc hiện đại**, một loại
đại lượng khác — xem ba loại A/B/C ở đầu `tools/deltat.py`.

---

## E. Vì sao bốn tầng kiểm thử cũ không bắt được

| Tầng | Lý do trượt |
|---|---|
| Đối chiếu HKO | `build_inputs.py` dùng **chung** cột ΔT hỏng ⇒ mô hình tham chiếu sai **cùng kiểu**. Thêm nữa HKO ở 120°Đ có ranh giới ngày 16:00Z nên các ca sát 17:00Z không nằm gần biên của họ |
| Vector nhà nước | Đều thuộc 1984–2053, không chạm 1938 |
| Bất biến toán học | 1938 vẫn song ánh, vẫn 13 tháng, vẫn đúng một tháng nhuận — chỉ **sai chỗ** |
| Fixture | **Sinh từ chính dataset** ⇒ chỉ kiểm được việc đọc file, không kiểm được quy tắc |

Cả bốn đã được xử lý — xem §G.

---

## F. Pháp y ca 1938

```
ΔT cũ = 0 s  (cột NASA làm tròn)          ΔT mới = 24,05 s
        ↓                                         ↓
trung khí 180° = 16:59:51,2Z              trung khí 180° = 16:59:27,1Z
        ↓ round → 17:00                           ↓ floor → 16:59
ngày VN = 24/09/1938  (mùng 1)            ngày VN = 23/09/1938  (ngày 30)
        ↓                                         ↓
tháng thiếu trung khí = tháng             tháng thiếu trung khí = tháng
bắt đầu 25/08/1938                        bắt đầu 24/09/1938
        ↓                                         ↓
   NHUẬN THÁNG 7                              NHUẬN THÁNG 8
```

| | Cũ | Mới |
|---|---|---|
| Tháng nhuận 1938 | **7** | **8** |
| 25/08/1938 | 1/7 nhuận | **1/8** |
| 24/09/1938 | 1/8 | **1/8 nhuận** |
| Số tháng đổi nhãn | — | **2** |
| Số ngày đổi nhãn | — | **59** |

**Nguyên nhân thiên văn:** Thu phân 1938 rơi cách ranh giới ngày Việt Nam **32,9
giây**. Đây là trung khí sát biên nhất trong toàn dải mà lại rơi đúng ranh giới tháng.

**Nguyên nhân phần mềm:** ΔT sai 24 s đẩy thời điểm tới 16:59:51, rồi `round` đẩy nốt
qua 17:00. **Hai lỗi cộng dồn**; chỉ cần sửa một trong hai là 1938 đã đúng, nhưng
sửa cả hai mới làm việc đúng có thể chứng minh được thay vì may mắn.

**Test chặn tái phát:** `LunarBoundaryRegressionTest`, 6 test — khoá tháng nhuận 1938,
các mốc ngày quanh nó, toàn bộ 8 điểm Sóc và 7 trung khí sát biên.

### 1938 trở thành ca Việt Nam ≠ Trung Quốc

| Năm | Việt Nam 105°Đ | Trung Quốc 120°Đ |
|---|---|---|
| 1917 | nhuận 3 | nhuận 2 |
| 1922 | nhuận 6 | nhuận 5 |
| **1938** | **nhuận 8** | **nhuận 7** |
| 1984 | không nhuận | nhuận 10 |
| 1985 | nhuận 2 | không nhuận |
| 1987 | nhuận 7 | nhuận 6 |

**1984 và 1987 đã được Ban Lịch Nhà nước xác nhận** và đang bị khoá trong
`LunarExternalVectorTest`. 1938 rơi vào **đúng nhóm hiện tượng đó**. Dataset cũ khiến
1938 trùng với Trung Quốc — tức lỗi đã **che mất** một khác biệt Việt–Trung có thật.

> **Giới hạn phải nói rõ:** chưa có nguồn Việt Nam bậc 1 nào xác nhận trực tiếp
> *nhuận tháng 8 năm 1938*. Bằng chứng là pipeline thiên văn đã sửa cộng quy tắc
> 105°Đ, cùng sự nhất quán với 1984/1987. Năm 1938 Đông Dương dùng UTC+7 (từ 1911),
> nên quy chiếu UTC+7 là hợp lý về mặt lịch sử cho ca này.

---

## G. Bốn tầng kiểm thử đã được vá

| Tầng | Đã làm |
|---|---|
| Mô hình tham chiếu | `build_inputs.py` dùng chung `deltat.py` đã sửa |
| **Fixture** | **Sinh lại từ mô hình tham chiếu đi thẳng NASA + ERFA, KHÔNG đọc `.bin`** — nay kiểm được **quy tắc**, không chỉ việc đọc file |
| Verifier | Viết lại hoàn toàn, **không import gì từ generator** |
| Hồi quy | Thêm `LunarBoundaryRegressionTest` và `tools/test_generator.py` |

---

## H. Verifier độc lập — độc lập tới đâu

`tools/verify_lunar_dataset.py` tự viết: parser trang NASA · hiện thực ΔT dạng bảng hệ
số · driver ERFA · công thức JD (mốc Unix, khác hẳn phép phân rã lịch của generator) ·
parser `.bin` · suy ngày Việt Nam · quy tắc R1–R4.

| Trục | Mức |
|---|---|
| Cột ΔT NASA ↔ đa thức sản xuất | ✅ **độc lập thật** — dữ liệu ngoài |
| Phút điểm Sóc NASA ↔ dataset | ✅ **độc lập thật** — dữ liệu ngoài |
| Thời điểm trung khí | ⚠️ **kiểm chéo hiện thực** — chung ERFA và chung đa thức ΔT, vì đó **là** nguồn sản xuất đã đóng băng |

Bằng chứng ngoài cho quy tắc lịch nằm ở **HKO** và **văn bản nhà nước**, không nằm
trong verifier.

---

## I. Kết quả kiểm chứng

```
--- verifier độc lập ---
Sóc tăng đơn điệu · trung khí tăng đơn điệu · 2448 = 12 × 204
khoảng cách Sóc 29,274 .. 29,830 ngày · phủ trọn 1901-01-01..2100-12-31
2474/2474 điểm Sóc NASA có mặt, đúng tới phút
200/200 năm ΔT nằm trong lượng tử của cột NASA   (lệch lớn nhất 30,02 s)
2448/2448 trung khí khớp trong [0, 60) s          (lệch lớn nhất 59,971 s)
0 bất đồng ngày dương lịch Việt Nam
75/75 năm 13 tháng tìm được đúng một tháng thiếu trung khí
tháng 29 ngày ×1189 · tháng 30 ngày ×1344
=> DATASET HỢP LỆ

--- HKO, toàn dải 1901-2100, mô hình @120°Đ ---
2.474 tháng · khớp 2.471 (99,88%)
lệch số tháng 0 · lệch cờ nhuận 0 · 73/73 năm nhuận khớp
3 khác biệt: 1914, 1916, 1920 — đều trước 1929, khi Trung Quốc dùng
giờ mặt trời trung bình Bắc Kinh (UTC+7:45:40) thay vì UTC+8
```

---

## J. Cũ ↔ mới, bằng số

| | |
|---|---:|
| Bản ghi Sóc đổi giá trị | 30 *(đều là đệm nội bộ)* |
| Bản ghi Sóc đổi **ngày Việt Nam** | **0** |
| Bản ghi trung khí đổi giá trị | 1.438 |
| Bản ghi trung khí đổi **ngày Việt Nam** | **3** |
| Mốc bắt đầu tháng âm đổi | **0 / 2.534** |
| Số năm nhuận | 75 → **75** |
| Gán tháng nhuận đổi | **1** *(1938)* |
| Tháng âm đổi nhãn | **2** |
| Ngày đổi nhãn | **59** |

Ba trung khí đổi ngày:

| Trung khí | Cũ | Mới |
|---|---|---|
| 1924-06-21 | 17:00Z → 22/06 | 16:59Z → **21/06** |
| 1938-09-23 | 17:00Z → 24/09 | 16:59Z → **23/09** |
| 2038-07-22 | 17:00Z → 23/07 | 16:59Z → **22/07** |

Chỉ 1938 đổi lịch; hai ca kia rơi giữa tháng.

---

## K. Quét biên đầy đủ — quét **toàn bộ**, không giới hạn ca đã biết

### K.1 — 8 điểm Sóc trong ±120 s quanh 17:00:00Z

| UTC | lệch (s) | ngày VN | nếu `round` | floor≠round |
|---|---:|---|---|---|
| 1944-06-20 17:00:00 | 0,0 | 1944-06-21 | 1944-06-21 | không |
| 1967-07-07 17:00:00 | 0,0 | 1967-07-08 | 1967-07-08 | không |
| 1998-09-20 17:01:00 | +60,0 | 1998-09-21 | 1998-09-21 | không |
| 2054-05-07 17:00:00 | 0,0 | 2054-05-08 | 2054-05-08 | không |
| 2072-12-09 16:59:00 | −60,0 | 2072-12-09 | 2072-12-09 | không |
| 2077-11-15 17:00:00 | 0,0 | 2077-11-16 | 2077-11-16 | không |
| 2079-08-26 17:02:00 | +120,0 | 2079-08-27 | 2079-08-27 | không |
| 2085-10-18 17:00:00 | 0,0 | 2085-10-19 | 2085-10-19 | không |

Không ca nào bị `floor` ảnh hưởng: NASA công bố **đúng phút chẵn**, ta dùng nguyên giá
trị. Bất định còn lại ở đây là **của NASA**, đã đóng băng theo chính sách NASA-first.

### K.2 — 7 trung khí trong ±120 s quanh 17:00:00Z

| UTC | lệch (s) | ΔT (s) | ngày VN | nếu `round` | floor≠round | đổi lịch? |
|---|---:|---:|---|---|---|---|
| 1924-06-21 16:59:18,766 | −41,2 | 23,6 | 1924-06-21 | 1924-06-21 | không | không |
| 1938-01-20 16:58:42,253 | −77,7 | 24,0 | 1938-01-20 | 1938-01-20 | **có** | không |
| **1938-09-23 16:59:27,106** | **−32,9** | 24,1 | 1938-09-23 | 1938-09-23 | không | **CÓ** |
| 1953-06-21 16:59:53,678 | −6,3 | 30,4 | 1953-06-21 | **1953-06-22** | **có** | không |
| 2004-05-20 16:59:11,629 | −48,4 | 64,6 | 2004-05-20 | 2004-05-20 | không | không |
| 2038-07-22 16:59:41,593 | −18,4 | 83,6 | 2038-07-22 | **2038-07-23** | **có** | không |
| 2074-08-22 17:00:14,634 | +14,6 | 145,0 | 2074-08-23 | 2074-08-23 | không | không |

Biên hẹp nhất toàn dải: **6,3 giây** (1953-06-21).

---

## L. Tái lập

Xoá sạch rồi sinh lại **4 lần**, 3 múi giờ và 3 locale — kể cả `tr_TR` vốn hay làm lộ
lỗi chữ hoa/thường:

| TZ | Locale | `.bin` | `.json` |
|---|---|---|---|
| Asia/Ho_Chi_Minh | vi_VN.UTF-8 | `b9f9613a…` | `92dfa7fe…` |
| Asia/Ho_Chi_Minh | vi_VN.UTF-8 | `b9f9613a…` | `92dfa7fe…` |
| Asia/Shanghai | zh_CN.UTF-8 | `b9f9613a…` | `92dfa7fe…` |
| America/Los_Angeles | tr_TR.UTF-8 | `b9f9613a…` | `92dfa7fe…` |

**Giống hệt từng byte cả bốn lần.**

Provenance **không nhúng thời điểm sinh** — cố ý, vì §XI đòi tái lập giống hệt byte.
Nhận dạng phiên bản dùng **sha256 của chính script** (`generator.sha256`,
`deltaTModuleSha256`). Đây là sai khác có chủ đích so với yêu cầu "dataset generation
timestamp" của uỷ quyền; hai điều đó loại trừ nhau.

`tools/build_erfa_bench.sh` mới thêm: trước đây binary ERFA chỉ nằm ở `/tmp` và biến
mất sau mỗi lần dọn — chính chuyện đó đã xảy ra giữa audit này.

---

## M. Điều **không** thay đổi

Không đổi quyền uy nguồn · không thay NASA bằng ERFA cho điểm Sóc · không đưa
`moon98`/Meeus vào sản xuất · không dùng Hồ Ngọc Đức · không scrape lịch online ·
không thêm mạng, dependency, NDK/JNI/C · không sửa UI, Room, navigation · không mở
rộng phạm vi vùng miền lịch sử · không thêm `InsufficientPrecision` · **không
hardcode 1938 hay bất kỳ ngoại lệ nào** — kết quả đúng đến từ pipeline tổng quát.
