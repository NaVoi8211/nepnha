# Mô hình thời gian lịch sử Việt Nam

> Phase 3A.2. Nhãn: **FACT** · **INFERENCE** · **HYPOTHESIS** · **VERIFIED** /
> **NOT VERIFIED** / **INDEPENDENTLY VERIFIED**.
>
> **Trạng thái: ⛔ CHƯA GIẢI QUYẾT cho giai đoạn 1954–1967.**

---

## 1. Tách hai khái niệm — theo yêu cầu mục 12 của chỉ đạo

Không được coi `giờ dân sự chính thức = múi giờ dùng để tính lịch` là tiên đề. Hai
cột riêng:

| Giai đoạn | Giờ dân sự chính thức | Múi giờ **thực sự dùng để tính lịch** | Bằng chứng | Nhãn |
|---|---|---|---|---|
| trước 1906-07-01 | UTC+07:06:40 | ⛔ không rõ | vi/en.wikipedia | FACT (giờ dân sự) |
| 1906-07-01 → 1911-04-30 | UTC+07:06:30 | ⛔ không rõ | như trên | FACT |
| 1911-05-01 → 1942-12-30 | UTC+07:00 | ⛔ không rõ | như trên | FACT |
| 1942-12-31 → 1945-03-13 | UTC+08:00 | ⛔ không rõ | như trên | FACT |
| 1945-03-14 → 1945-09-01 | UTC+09:00 (giờ Tokyo) | ⛔ không rõ | như trên | FACT |
| **1954 → 1967 — miền BẮC** | ⚠️ **TRANH CÃI** (§2) | ⛔ **KHÔNG BIẾT** | mâu thuẫn | **BLOCKED** |
| 1954 → 1955-06-30 — miền Nam | UTC+08:00 | ⛔ không rõ | wikipedia | FACT |
| 1955-07-01 → 1959-12-31 — miền Nam | UTC+07:00 | ⛔ không rõ | wikipedia | FACT |
| **1960-01-01 → 1975-06-12 — miền Nam** | **UTC+08:00** | 🟡 suy ra là +8 | Tiền Phong (Ban Lịch NN) + wikipedia | FACT (dân sự) / INFERENCE (lịch) |
| **1968-01-01 → 1975-06-12 — miền Bắc** | **UTC+07:00** | ✅ **UTC+07:00** | QĐ 121-CP | **FACT** (§3) |
| **1975-06-13 → nay** | **UTC+07:00** toàn quốc | ✅ **UTC+07:00** | như trên | FACT / INFERENCE |

Điểm đáng chú ý: **cột thứ ba gần như trống trước 1968.** Ta biết khá rõ giờ dân sự,
nhưng gần như không có bằng chứng trực tiếp nào về *tập quán tính lịch*.

---

## 2. ⚠️ Mâu thuẫn chưa giải: miền Bắc 1954–1967

| Phía | Khẳng định | Nguồn | Nhãn |
|---|---|---|---|
| **P1** | Miền Bắc dùng **UTC+7 từ 1945/1954**; QĐ 121-CP chỉ **"khẳng định"** lại | Tiền Phong dẫn **Trịnh Tiến Điều, Trưởng ban Lịch Nhà nước**; vi.wikipedia | FACT rằng nguồn nói vậy · **NOT VERIFIED** rằng đúng |
| **P2** | Miền Bắc dùng **UTC+8 đến 1967** | Trang lịch Hồ Ngọc Đức: *"GMT+480 min was used … in North Vietnam before 7/8/1967"*; nhiều tài liệu phổ thông | FACT rằng nguồn nói vậy · **NOT VERIFIED** rằng đúng |

**HYPOTHESIS (KHÔNG dùng làm quyết định sản phẩm):** P2 hợp lý hơn, vì ban hành một
quyết định để không thay đổi gì thì bất thường.

**STATUS: NOT VERIFIED.** Theo đúng chỉ đạo mục 11, giả thuyết này **không được**
biến thành hành vi của engine.

### FACT MỚI (Phase 3A.4) — tiêu đề của 121-CP

**FACT.** Tiêu đề chính thức của Quyết định 121-CP là **"về việc tính lịch và quản lý
lịch của Nhà nước"** — *không phải* một quyết định thuần về giờ dân sự.

**INFERENCE.** Đây đúng là văn bản cần tìm: nó nói về **tính lịch**, chạm thẳng vào
cột "múi giờ dùng để tính lịch" đang để trống ở §1.

**BLOCKED.** Toàn văn vẫn không đọc được: `thuvienphapluat.vn` trả **HTTP 403** cho
cả hai bản ghi (18212 và 18929) kể cả khi gửi User-Agent trình duyệt;
`hethongphapluat.com` trả body rỗng.

### 🔴 FACT MỚI (Phase 3A.5) — cấu trúc điều khoản của 121-CP

| Điều | Nội dung |
|---|---|
| **1** | Việt Nam nằm trọn **múi giờ thứ 7**; giờ chính thức là giờ múi 7 |
| **2** | **Dương lịch** là lịch chính thức |
| **3** | **"Việc sửa đổi cách tính âm lịch"** cần thiết để chính xác và **phù hợp với giờ chính thức** |
| **4** | Giao **Nha Khí tượng** tính **âm lịch thống nhất cả nước**, quản lý việc lịch |
| **6** | UB Khoa học Kỹ thuật NN, Bộ Nội vụ, VN Thông tấn xã, Nha Khí tượng thi hành |

**FACT.** Tồn tại **Thông tư 01-VLĐC (1967)** của Nha Khí tượng *"giải thích và hướng
dẫn thi hành Quyết định 121-CP"*.

> ⚠️ **NOT INDEPENDENTLY VERIFIED.** Không đọc được toàn văn: `thuvienphapluat.vn`
> (18212, 18929, cả bản mobile) và `luatminhkhue.vn` đều **HTTP 403** kể cả với
> User-Agent trình duyệt. Nội dung trên từ **trích xuất của công cụ tìm kiếm**.

**INFERENCE.** Điều 3 nói **"sửa đổi cách tính âm lịch"** — một quyết định sửa đổi chỉ
có nghĩa nếu cách tính trước đó **khác**. Nghiêng mạnh về **P2**.

**KHÔNG chuyển G9 sang PASS**, vì: (1) chưa đọc được văn bản gốc; (2) điều 3 nói *có
sửa đổi*, **không nói** trước đó là múi giờ nào; (3) chỉ nói về miền Bắc.

**HYPOTHESIS.** **Thông tư 01-VLĐC** có thể là văn bản quan trọng nhất chưa đọc được —
một thông tư hướng dẫn *cách tính lịch* nhiều khả năng nêu **phương pháp**, tức có thể
là nguồn V1 cho chính các quy tắc lịch.

### Đã tìm thêm nhưng chưa đủ

- ✅ Xác định được số hiệu, ngày ký, ngày hiệu lực của 121-CP.
- ❌ **Chưa đọc được toàn văn 121-CP** từ nguồn pháp quy gốc.
- ❌ **Chưa tìm được tài liệu nào trước 1968** nói về tập quán tính lịch của miền Bắc.
- ❌ **Chưa tiếp cận được lịch nhà nước in trong giai đoạn 1954–1967** — đây mới là
  bằng chứng trực tiếp nhất: cầm quyển lịch năm 1960 lên là biết ngay họ tính theo
  múi giờ nào.

---

## 3. Điều đã chắc chắn về 121-CP

**FACT.** Quyết định số **121-CP**, Hội đồng Chính phủ Việt Nam Dân chủ Cộng hoà,
Thủ tướng **Phạm Văn Đồng ký ngày 08/8/1967**, **hiệu lực từ 01/01/1968**.

**FACT.** Điều 1 xác định Việt Nam **nằm trọn trong múi giờ thứ 7**, giờ chính thức
là giờ múi thứ 7, kinh tuyến trung tâm **105° Đông**.

**FACT.** Được tái khẳng định bởi Quyết định **134/2002/QĐ-TTg** ngày 14/10/2002.

**FACT (theo nguồn thứ cấp) — quan trọng:** có nguồn tóm tắt 121-CP nói rằng quyết
định này cũng xác định **âm lịch dùng trong nước được tính theo giờ chính thức đó**.

> **NOT INDEPENDENTLY VERIFIED.** Tôi đọc điều này từ một trang tóm tắt văn bản, chưa
> phải từ chính văn. Nếu đúng, đây chính là **mắt xích nối cột 2 và cột 3** trong
> bảng §1 — nhưng chỉ **từ 1968 trở đi**, vẫn không nói gì về 1954–1967.

**INFERENCE.** Từ 01/01/1968 trở đi, giờ dân sự và múi giờ tính lịch của miền Bắc
(và từ 13/6/1975 là toàn quốc) **trùng nhau ở UTC+7**. Đây là suy luận có cơ sở
vững, không phải giả thuyết.

---

## 4. Bằng chứng gián tiếp — Tết Mậu Thân 1968

**FACT.** Miền Bắc ăn Tết **29/01/1968**, miền Nam **30/01/1968** (Wikipedia tiếng
Việt, *Sự kiện Tết Mậu Thân*).

**INFERENCE.** Nhất quán với miền Bắc UTC+7 / miền Nam UTC+8 **tại tháng 01/1968** —
tức là **sau** khi 121-CP có hiệu lực. Sự kiện này **không nói gì** về 1954–1967.

**FACT (Phase 3A.1).** Điểm Sóc `1967-07-07 17:00 UT` là một trong 5 ca "đúng nửa
đêm giờ VN" — và rơi **đúng vào giai đoạn tranh cãi**. Theo UTC+7 nó là 00:00; theo
UTC+8 nó là 01:00 sáng hôm sau, hoàn toàn không mơ hồ.

**INFERENCE.** Nếu tìm được lịch nhà nước in cho tháng 7/1967 và biết tháng âm bắt
đầu ngày nào, ta có thể **suy ngược ra múi giờ họ đã dùng**. Đây là một manh mối
thực sự sắc bén cho việc tra cứu sau này.

---

## 5. Ảnh hưởng tới `CalendarContext`

Thiết kế ở Phase 3A vẫn giữ nguyên, **chưa freeze**:

```kotlin
sealed interface CalendarContext {
    data object OfficialVietnam : CalendarContext
    data class HistoricalRegion(val region: VietnamRegion) : CalendarContext
}
enum class VietnamRegion { NORTH, SOUTH }
```

Trạng thái từng giai đoạn:

| Giai đoạn | Engine làm được gì |
|---|---|
| 1975-06-13 → 2100 | ✅ UTC+7, không mơ hồ |
| 1968-01-01 → 1975-06-12 | ✅ Bắc +7 / Nam +8 — cần chọn `HistoricalRegion` |
| 1954 → 1967-12-31 | ⛔ **KHÔNG XÁC ĐỊNH ĐƯỢC** — chưa biết dùng offset nào |
| 1901 → 1954 | 🟡 biết giờ dân sự, **không biết tập quán tính lịch** |

**Đề xuất (chưa quyết, chờ chủ dự án):** trong khi chưa có bằng chứng, engine **không
được im lặng chọn bừa** cho vùng 1901–1967. Hai khả năng:

- **(a)** Trả lỗi `AmbiguousHistoricalRegion` / `UnverifiedHistoricalPeriod`.
- **(b)** Thu hẹp phạm vi bảo đảm xuống **1968–2100**, dưới mốc đó là "tham khảo",
  có cảnh báo trên UI.

Cả hai đều **trung thực hơn** việc âm thầm áp UTC+7 cho năm 1960.

> ⚠️ Nhắc lại tác động thật: người sinh ở miền Bắc trong 1954–1967 nay khoảng 59–72
> tuổi. Ngày giỗ của cha mẹ họ đang được cúng ngay hôm nay. Đây không phải chi tiết
> học thuật.

---

## 6. Việc cần làm để mở G9

| # | Cần | Vì sao đủ sức mở khoá |
|---|---|---|
| 1 | **Toàn văn 121-CP** từ nguồn pháp quy | Xem quyết định *đổi* hay *khẳng định lại*, và có nói gì về âm lịch không |
| 2 | **Lịch nhà nước in giai đoạn 1954–1967** | Bằng chứng trực tiếp nhất về tập quán tính lịch |
| 3 | **Sách Trần Tiến Bình** | Tác giả từ Ban Lịch Nhà nước, nhiều khả năng bàn thẳng vấn đề này |
| 4 | Đối chiếu tháng âm quanh **07/1967** | Suy ngược ra offset thực dùng (§4) |

Chưa có ít nhất một trong bốn thứ trên thì **G9 vẫn BLOCKED**.
