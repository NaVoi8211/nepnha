# Phase 3A.1 — Dataset & Historical Verification

> **KHÔNG CÓ PRODUCTION CODE.** Không engine, không đổi Room schema, không nối UI,
> không thêm dependency. Nghiên cứu 2026-08-25.
>
> Khác với Phase 3A (đọc điều khoản trên landing page), phase này **tải và mổ xẻ
> chính dữ liệu** mà engine sẽ dựa vào. Mọi con số dưới đây tái lập được bằng
> [`tools/verify_nasa_newmoons.py`](../tools/verify_nasa_newmoons.py).

**KẾT QUẢ: GATE VẪN ⛔ BLOCKED**, và phase này phát hiện **thêm một blocker mới**
mà Phase 3A chưa thấy. Chi tiết §B.3.

---

## A. NASA / GSFC — Moon Phases

### A.1 Đã kiểm chứng bằng dữ liệu thật

| Hạng mục | Kết quả | Cách kiểm |
|---|---|---|
| Độ phủ | ✅ **1901–2100 đủ**, gọn trong 2 file: `phases1901.html` (1901–2000), `phases2001.html` (2001–2100) | Tải và đọc tiêu đề bảng |
| Số điểm Sóc | ✅ **2.474** cho 1901–2100 | Parse theo **vị trí cột**, không theo regex tự do |
| Đối chiếu lý thuyết | ✅ **Khớp chính xác**: 200 × 12,3685 ≈ 2.474 | Nếu parser sai thì con số này đã lệch |
| Format | Text cố định cột trong HTML: `Jan 24  13:07` | Đọc file |
| **Thang thời gian** | **Universal Time (UT)**, có kèm cột ΔT | Nguyên văn: *"the date and time (Universal Time) of all phases of the Moon"* |
| **Độ phân giải** | ⚠️ **Chỉ tới PHÚT** — không có giây | Đọc file |
| Giấy phép | ✅ *"Permission is freely granted to reproduce this data when accompanied by an acknowledgment"* | Nguyên văn trên trang catalog |
| Ghi công bắt buộc | `Moon Phase Predictions by Fred Espenak, NASA/GSFC` | như trên |
| Đóng gói offline | ✅ Khả thi — 2.474 timestamp ≈ vài chục KB | Ước tính |

### A.2 ⚠️ Phát hiện phải nói rõ: NASA tính bằng thuật toán Meeus

Nguyên văn trên trang catalog:

> "The phases of the Moon as well as eclipses are based on Jean Meeus' Astronomical
> Algorithms (Willmann-Bell, Inc., Richmond, 1998). All calculations are by Fred
> Espenak, and he assumes full responsibility for their accuracy."

Nghĩa là: ta đã loại Meeus khỏi implementation, nhưng **dữ liệu NASA lại là kết quả
chạy thuật toán Meeus**.

Phân biệt cần thiết:

| | |
|---|---|
| Cái ta **không** làm | Đọc sách Meeus rồi chép công thức vào code |
| Cái ta **sẽ** làm | Dùng **những con số** mà NASA công bố, kèm giấy phép NASA cấp cho chính những con số đó |

Kết quả chạy một thuật toán là **dữ kiện**, không phải bản sao phần biểu đạt của
cuốn sách; và giấy phép ta dựa vào là của NASA/Espenak cấp cho dữ liệu, không phải
của Willmann-Bell.

> ⚠️ **CHƯA XÁC MINH:** đây là **nhận định của tôi, không phải ý kiến pháp lý**.
> Nếu chủ dự án muốn chắc chắn tuyệt đối cho một sản phẩm thương mại, cần luật sư
> xác nhận. Tôi ghi rõ ở đây để về sau không ai hiểu nhầm là đã "xong khâu pháp lý".

### A.3 "Fake precision" — đã lượng hoá được

Đây là câu trả lời cho mục 8 trong chỉ đạo.

Ranh giới ngày ở Việt Nam hiện nay là 00:00 UTC+7, tức **17:00:00 UTC**. Dữ liệu chỉ
có tới phút ⇒ giá trị thật nằm trong khoảng ±30 giây quanh con số công bố. Khi điểm
Sóc rơi **đúng 17:00 UT**, không thể biết chắc tháng âm bắt đầu ngày nào.

Quét toàn bộ 2.474 điểm Sóc:

| Lệch khỏi ranh giới | Số điểm Sóc |
|---|---|
| **0 phút (đúng nửa đêm VN)** | **5** |
| 1 phút | 2 |
| 2 phút | 1 |
| 3 phút | 4 |

**Năm ca không xác định được** (`17:00 UT` chẵn):

| Ngày (UT) | Giờ VN | Ghi chú |
|---|---|---|
| **1944-06-20** | 00:00 | |
| **1967-07-07** | 00:00 | ⚠️ Rơi đúng vào giai đoạn múi giờ miền Bắc đang tranh cãi — xem §C |
| **2054-05-07** | 00:00 | |
| **2077-11-15** | 00:00 | |
| **2085-10-18** | 00:00 | |

Kết luận: **dữ liệu NASA ở độ phân giải phút KHÔNG đủ cho 5 ngày này.** Engine
không được âm thầm chọn một ngày rồi trả về như thể chắc chắn. Ba cách xử lý:

1. Thêm `LunarError.InsufficientPrecision` — đúng tinh thần "không giả vờ chính xác".
2. Tra riêng 5 ca này từ oracle có thẩm quyền (sách Trần Tiến Bình) rồi ghi cứng
   như ngoại lệ **có nguồn**.
3. Tìm nguồn Sóc có độ phân giải giây.

Không chọn cách nào ở phase này. Đây là dữ liệu để chủ dự án quyết.

---

## B. Hong Kong Observatory

### B.1 Bảng đối chiếu Dương–Âm — kiểm chứng bằng file thật

| Hạng mục | Kết quả |
|---|---|
| Độ phủ | ✅ **1901–2100**, nguyên văn: *"Please use mouse to choose a year (1901 to 2100)"* |
| Format | ✅ **Có bản text thuần**, một file/năm: `T1901e.txt` … `T2100e.txt` (PDF cũng có nhưng khó parse) |
| Cấu trúc | Cột cố định: `1985/01/21     12th Lunar month    Monday` |
| Nội dung thêm | ✅ Có cột **tiết khí** — nhưng chỉ **NGÀY**, không có giờ |
| Múi giờ | UTC+8 (giờ Hồng Kông) — đây là **lịch Trung Quốc** |
| Giấy phép | ✅ data.gov.hk TOU cho phép **thương mại**, kèm attribution |
| Đóng gói offline | ✅ ~26 KB/năm ở dạng thô; trích riêng ngày đầu tháng thì rất nhỏ |

### B.2 ✅ Xác nhận chéo THẬT SỰ đầu tiên của dự án

Tải `T1985e.txt` và đọc các ngày mùng 1 âm của lịch Trung Quốc năm 1985:

```
1985/01/21     12th Lunar month    Monday
1985/02/20     1st Lunar month     Wednesday      ← Tết Trung Quốc
1985/03/21     2nd Lunar month     Thursday
```

| | Tết Ất Sửu 1985 | Nhánh nguồn |
|---|---|---|
| **Việt Nam** | **21/01/1985** | A — Ban Lịch Nhà nước qua báo Thanh Niên |
| **Trung Quốc** | **20/02/1985** | B — **HKO, dữ liệu gốc, tự tải và đọc** |

Hai **nhánh độc lập**, lệch đúng **một tháng**. Đây là test vector VN≠TQ đầu tiên
đạt chuẩn đối chiếu chéo thật — không phải hai website cùng chạy code HND.

Chi tiết đắt giá: HKO cho thấy **tháng 12 âm của Trung Quốc bắt đầu đúng ngày
21/01/1985** — chính ngày Việt Nam bắt đầu **tháng 1**. Lịch Việt Nam đi trước lịch
Trung Quốc trọn một tháng, thấy được bằng mắt trên dữ liệu gốc.

### B.3 ⛔ BLOCKER MỚI: HKO không có giờ tiết khí cho 1901–2100

Phase 3A đề xuất "Hướng 1": lấy điểm Sóc từ NASA + **thời điểm tiết khí từ HKO**,
rồi tự viết code áp 5 quy tắc. Kiểm tra thực tế cho thấy hướng này **không chạy
được như đã hình dung**:

Trang *Date and Time of the 24 Solar Terms* của HKO sinh danh sách năm bằng
JavaScript:

```javascript
Cyyyy = Now.getYear()+1900;
for (var y=Cyyyy; y<Cyyyy+3; y++)
```

⇒ **Chỉ có năm hiện tại và 2 năm kế tiếp.** Không phải 1901–2100.

Còn bảng đối chiếu 1901–2100 tuy có cột tiết khí nhưng **chỉ ghi ngày, không ghi
giờ**, và ngày đó là **ngày theo giờ Hồng Kông (UTC+8)**.

Vì sao điều này giết chết Hướng 1: quy tắc tháng nhuận cần biết trung khí rơi vào
ngày nào **theo giờ Việt Nam**. Một trung khí xảy ra lúc 00:30 giờ HK là ngày hôm
trước theo giờ VN. Không có giờ thì **không suy ra được** — và tỉ lệ trung khí rơi
vào khung 00:00–01:00 giờ HK là ~1/24, tức khoảng **200 trong số ~4.800 trung khí**
của 1901–2100 sẽ mơ hồ. Quá nhiều để bỏ qua.

**Kết luận: vẫn phải tính hoàng kinh Mặt Trời**, tức vẫn cần một nguồn công thức có
provenance rõ ràng. Meeus đã loại. USNO chưa xác minh được (§B.4). Đây là blocker
kỹ thuật **mới**, chưa có lời giải.

### B.4 USNO — vẫn không truy cập được

`aa.usno.navy.mil` không phân giải DNS từ máy này (`ENOTFOUND`), thử cả hai đường
dẫn. Chưa đọc được điều khoản lẫn bảng sai số ⇒ **CHƯA XÁC MINH**, không được đưa
vào kế hoạch.

Tài liệu thứ cấp nói độ chính xác ~0,01° ≈ **~15 phút thời gian** — nếu đúng thì
với quy tắc trung khí, con số này còn tệ hơn độ phân giải phút của NASA.

---

## C. Múi giờ lịch sử — phát hiện MÂU THUẪN, không giải quyết được

### C.1 Bảng bằng chứng

| Giai đoạn | Miền Bắc | Miền Nam | Nguồn | Tin cậy |
|---|---|---|---|---|
| 1945-09-02 → 1947-03-31 | UTC+7 | — | vi.wikipedia | Medium |
| 1947-04-01 → 1954 | UTC+8 (vùng Pháp kiểm soát) | — | vi.wikipedia | Medium |
| 1954-10 → 1967 | ⚠️ **TRANH CÃI** | — | xem §C.2 | ⛔ **Low** |
| Geneva 1954 → 1955-06-30 | — | UTC+8 | en/vi.wikipedia | Medium |
| 1955-07-01 → 1959-12-31 | — | UTC+7 | en/vi.wikipedia | Medium |
| **1960-01-01 → 1975-06-12** | — | **UTC+8** | Tiền Phong (dẫn Ban Lịch Nhà nước) + Wikipedia | **High** — hai nhánh |
| **1968-01-01 → 1975-06-12** | **UTC+7** | — | QĐ 121/CP + Tiền Phong + Wikipedia | **High** |
| **1975-06-13 → nay** | **UTC+7 toàn quốc** | | Tiền Phong + Wikipedia | **High** |

### C.2 ⚠️ Mâu thuẫn trực tiếp: miền Bắc 1954–1967

Hai phía nói ngược nhau:

| Phía | Khẳng định | Nguồn |
|---|---|---|
| **P1** | Miền Bắc dùng **UTC+7 từ 1945/1954**; QĐ 121/CP chỉ **"khẳng định"** lại | Tiền Phong dẫn **Trịnh Tiến Điều, Trưởng ban Lịch Nhà nước**; vi.wikipedia |
| **P2** | Miền Bắc dùng **UTC+8 đến 1967**, đổi sang UTC+7 từ 1968 | Trang lịch của Hồ Ngọc Đức (*"GMT+480 min ... in North Vietnam before 7/8/1967"*); nhiều tài liệu phổ thông VN |

Điểm chung đã chắc chắn: **Quyết định 121/CP**, Thủ tướng Phạm Văn Đồng **ký
08/8/1967**, **hiệu lực 01/01/1968**, xác định Việt Nam ở **múi giờ thứ 7**. Được
tái khẳng định bởi Quyết định 134/2002/QĐ-TTg ngày 14/10/2002.

Nhưng **121/CP *đổi* múi giờ hay chỉ *khẳng định lại*** thì hai nguồn nói khác nhau.
Về mặt logic P2 nghe hợp lý hơn (ra một quyết định để không đổi gì thì lạ), nhưng
**"nghe hợp lý" không phải bằng chứng** — và nguồn của P1 lại chính là Trưởng ban
Lịch Nhà nước.

### C.3 Vì sao chuyện này quan trọng thật sự

Nếu P2 đúng thì lịch âm chính thức của Việt Nam cho **miền Bắc giai đoạn 1954–1967**
phải tính theo UTC+8 — và mọi phần mềm tính theo UTC+7 cho giai đoạn đó đều **sai**.
Nếu P1 đúng thì ngược lại, chính implementation của HND (mà cả Việt Nam đang dùng)
đang sai ở giai đoạn đó.

Không thể có chuyện cả hai cùng đúng. Người dùng bị ảnh hưởng là **những cụ sinh ở
miền Bắc trong khoảng 1954–1967** — nhóm người có ngày giỗ đang được cúng ngay hôm nay.

Trùng hợp đáng chú ý: điểm Sóc `1967-07-07 17:00 UT` — một trong 5 ca "đúng nửa đêm"
ở §A.3 — rơi đúng vào giai đoạn tranh cãi này. Theo UTC+7 nó là nửa đêm; theo UTC+8
nó là 01:00 sáng hôm sau, không mơ hồ chút nào.

**Không kết luận. Cần sách Trần Tiến Bình hoặc chính văn 121/CP.**

---

## D. Oracle Tier 1 từ văn bản nhà nước

Đã tìm thêm được văn bản của những năm trước. Mỗi văn bản cho **nhiều cặp
dương↔âm**, không chỉ ngày mùng 1.

| Ngày dương | Ngày âm | Văn bản |
|---|---|---|
| 25/01/2025 | 26 tháng Chạp Giáp Thìn | **6150/TB-BLĐTBXH** (03/12/2024) |
| 29/01/2025 | **Mùng 1 Tết Ất Tỵ** | như trên |
| 02/02/2025 | Mùng 5 tháng Giêng Ất Tỵ | như trên |
| 14/02/2026 | 27 tháng Chạp Ất Tỵ | **9441/TB-BNV** |
| 17/02/2026 | **Mùng 1 Tết Bính Ngọ** | như trên |
| 22/02/2026 | Mùng 6 Tết Bính Ngọ | như trên |

Đây là **quyết định hành chính**, không phải kết quả phần mềm ⇒ nhánh A, độc lập với
HND. Có thể truy ngược nhiều năm nữa nếu cần.

> ⚠️ Giới hạn thật: chỉ phủ vùng quanh Tết (khoảng 26 tháng Chạp → mùng 6). **Không
> giúp gì** cho Rằm tháng 7, Trung Thu, tháng nhuận, hay số ngày của tháng âm — tức
> là **không mở được G6, G7, G10**.

---

## E. Oracle matrix

| Nhóm test | Nguồn | Độc lập? | License đã xác minh? | Trạng thái |
|---|---|---|---|---|
| **Tết** | Văn bản nhà nước (6150/TB-BLĐTBXH, 9441/TB-BNV) | ✅ Nhánh A | ✅ văn bản pháp quy | ✅ **PASS** (2025, 2026) |
| **VN ≠ TQ (1985)** | VN: Thanh Niên/Ban Lịch NN · TQ: **HKO T1985e.txt** | ✅ **A vs B** | ✅ data.gov.hk cho phép thương mại | ✅ **PASS** |
| VN ≠ TQ (1984, 1987, 2006, 2007, 2008, 2030, 2053) | TQ: HKO (tải được) · VN: **thiếu nguồn Tier 1** | 🟡 một nửa | ✅ phía HKO | 🟡 **MỘT PHẦN** |
| **Tháng nhuận** | Chỉ nguồn phổ thông | ❌ nghi cùng nhánh HND | — | ⛔ **BLOCKED** |
| **29/30 ngày** | **Không có nguồn nào** | — | — | ⛔ **BLOCKED** |
| **Biên 1901 / 2100** | Cần Trần Tiến Bình | — | — | ⛔ **BLOCKED** |
| Rằm / Trung Thu / Đoan Ngọ | Cần Trần Tiến Bình | — | — | ⛔ **BLOCKED** |
| **Múi giờ lịch sử** | Tiền Phong (Ban Lịch NN) vs HND | ⚠️ **hai nguồn mâu thuẫn** | — | ⛔ **BLOCKED** (§C.2) |
| Input không hợp lệ | Tự định nghĩa | không cần | — | ✅ **PASS** |

### Data source ≠ Oracle — bảng phân vai

Theo yêu cầu mục 3 của chỉ đạo:

| Nguồn | Vai trò **Data source** (engine ăn vào) | Vai trò **Oracle** (kiểm engine) |
|---|---|---|
| NASA Moon Phases | ✅ điểm Sóc | ❌ **không** — nếu vừa là input vừa là expected thì không kiểm được gì |
| HKO — bảng 1901–2100 | ❌ (là lịch **Trung Quốc**) | ✅ oracle cho **phía TQ** trong test VN≠TQ |
| HKO — giờ tiết khí | ✗ **không dùng được** — chỉ 3 năm (§B.3) | ❌ |
| Văn bản nhà nước | ❌ | ✅ oracle Tier 1 cho ngày Tết |
| Trần Tiến Bình | ❌ | ✅ oracle Tier 1 — **chưa có** |

Điểm mấu chốt: NASA là **data source**, nên **tuyệt đối không** được dùng NASA làm
expected value để kiểm engine. Kiểm engine phải bằng nhánh A (nhà nước / sách).

---

## F. Ảnh hưởng tới hướng kỹ thuật

Phase 3A đề xuất hai hướng. Sau khi mổ dữ liệu thật:

| Hướng | Điểm Sóc | Trung khí | Kết luận |
|---|---|---|---|
| **1 — thuần dữ liệu** | ✅ NASA, 1901–2100, giấy phép rõ | ⛔ **Không có nguồn nào phủ 1901–2100** | **KHÔNG KHẢ THI như đã hình dung** |
| **2 — thuần công thức** | cần nguồn công thức | cần nguồn công thức | ⛔ Meeus loại, USNO chưa xác minh |
| **3 — lai (mới)** | ✅ NASA (dữ liệu) | ✳️ tự tính hoàng kinh Mặt Trời từ nguồn công thức **chưa xác định** | Khả dĩ nhất, nhưng **vẫn hở đúng một mắt xích** |

Mắt xích còn hở của cả ba hướng là **một nguồn công thức hoàng kinh Mặt Trời có
provenance rõ ràng**. Chưa có nó thì chưa có engine.

Cần nghiên cứu tiếp ở phase sau (chưa làm ở đây, không đoán):

- USNO qua đường mạng khác.
- IAU SOFA — thư viện chuẩn quốc tế, có giấy phép riêng cần đọc kỹ.
- VSOP87 — dữ liệu công bố trong tạp chí thiên văn học.
- Hỏi xin phép Willmann-Bell cho Meeus (đường chính thống, mất thời gian).
