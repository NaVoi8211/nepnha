# Phase 3 Preflight — Lịch âm Việt Nam

> **Trạng thái: NGHIÊN CỨU, CHƯA CÓ CODE.** Không có engine, không đổi schema,
> không nối UI, không thêm dependency. Tài liệu này tồn tại để quyết định *có được
> làm gì* và *làm thế nào* trước khi viết dòng code đầu tiên.
>
> Nghiên cứu ngày 2026-08-25. Mọi kết luận đều kèm nguồn. Chỗ nào chưa có bằng
> chứng thì ghi thẳng **CHƯA XÁC MINH** — không đoán.

---

## 1. License research — kết luận quan trọng nhất

### 1.1 Đã tìm được nguyên văn giấy phép của Hồ Ngọc Đức

Trang gốc `http://www.informatik.uni-leipzig.de/~duc/amlich/` nay **404**. Nhưng
header bản quyền còn nguyên trong các bản sao lưu, ví dụ `js/amlich.js` của
NukeViet 2.0:

```
/* *
 * Copyright 2004 Ho Ngoc Duc [http://come.to/duc]. All Rights Reserved.<p>
 * Permission to use, copy, modify, and redistribute this software and its
 * documentation for personal, non-commercial use is hereby granted provided that
 * this copyright notice appears in all copies.
 */
```

Nguồn: <https://raw.githubusercontent.com/nukeviet/NukeViet-2.0/master/js/amlich.js>
(truy cập 2026-08-25)

### 1.2 Điều này nghĩa là gì

| Câu hỏi | Trả lời |
|---|---|
| Được dùng cho app thương mại? | **KHÔNG.** Giấy phép chỉ cho `personal, non-commercial use` |
| Được sửa đổi? | Có, nhưng vẫn trong phạm vi phi thương mại |
| Được phân phối lại? | Có, kèm nguyên văn thông báo bản quyền, vẫn phi thương mại |
| Được đưa vào APK closed-source? | Chỉ khi app phi thương mại — và "phi thương mại" là khái niệm mơ hồ, rủi ro |
| Cần attribution? | **Có**, bắt buộc: `this copyright notice appears in all copies` |

**KẾT LUẬN: KHÔNG đưa source code của Hồ Ngọc Đức vào Nếp Nhà.**

Kể cả nếu MVP miễn phí, "non-commercial" là ranh giới không rõ ràng; app có mặt trên
store, có thể có bản trả phí sau, có thể mang thương hiệu — đủ để rơi vào vùng xám.
Không đáng đánh đổi.

### 1.3 Bẫy: các thư viện "MIT" là license laundering

Có nhiều thư viện quảng cáo MIT và nói rõ *"based on Hồ Ngọc Đức's algorithm"*:

| Repo | LICENSE khai báo |
|---|---|
| `baolanlequang/VietnameseLunar-android` | `MIT License · Copyright (c) 2022 Lan Le` |
| `teddyvn/VietnameseCalendar` | `MIT License · Copyright (c) 2022 Phuoc Nguyen` |
| `nhatanh2996/LunarCalendar4J` | `MIT License · Copyright (c) 2020 nhatanh2996` |
| `vanng822/amlich` | **Không có file LICENSE** (mặc định: all rights reserved) |

Cả ba repo đầu tự nhận bản quyền **của chính họ** rồi cấp phép MIT cho một tác phẩm
phái sinh từ code chỉ được phép dùng phi thương mại. Người port **không có quyền
relicense** thứ họ không sở hữu. Nhãn MIT ở đó **không làm sạch** giấy phép gốc.

**KẾT LUẬN: không dùng bất kỳ thư viện nào trong nhóm này.**

### 1.4 Phần được phép dùng: bản mô tả THUẬT TOÁN

Hồ Ngọc Đức có một trang riêng mô tả **quy tắc tính lịch**, tách khỏi source code:
*"How to compute the Vietnamese lunar calendar"*.

Nguồn còn truy cập được: <https://www.xemamlich.uhm.vn/calrules_en.html> (2026-08-25)

Trang này trình bày quy tắc lịch — tức **phương pháp/sự kiện**, không phải phần
biểu đạt phần mềm. Ta được phép **đọc hiểu rồi tự viết Kotlin**. Đây chính là ranh
giới ở mục 7 trong chỉ đạo Phase 3.

> ⚠️ **CHƯA XÁC MINH:** trang `calrules_en.html` không có tuyên bố bản quyền nào.
> Ta chỉ lấy *quy tắc*, không sao chép câu chữ hay bảng biểu của trang đó.
> Nếu sau này cần trích dẫn nguyên văn, phải xin phép.

---

## 2. Source provenance

| Thành phần | Nguồn | Trạng thái |
|---|---|---|
| Quy tắc lịch âm VN | Hồ Ngọc Đức, *How to compute the Vietnamese lunar calendar* | Dùng được (là phương pháp) |
| Source code `amlich.js` v0.5 (2004) | Hồ Ngọc Đức | ⛔ **Không dùng** — phi thương mại |
| Source code `amlich-aa98.js` | Hồ Ngọc Đức, bản dựa trên Meeus 1998 | ⛔ **CHƯA XÁC MINH** giấy phép — không tìm được file gốc. Mặc định coi như cùng điều kiện ⇒ không dùng |
| *Calendrical Calculations* (Reingold & Dershowitz) | HND ghi rõ *"I thank Edward M. Reingold and Nachum Dershowitz for their permission to use their Calendrical Calculations Software Package"* | ⛔ Code của họ cần **xin phép riêng** — không dùng |
| Meeus, *Astronomical Algorithms* (1998) | Sách in | Công thức thiên văn; xem §3.3 |
| Lịch chính thức VN | Trần Tiến Bình, *Lịch Việt Nam thế kỷ XX–XXI (1901–2100)*, NXB Văn hoá – Thông tin, 2005; biên soạn từ Ban Lịch Nhà nước (Trung tâm Thông tin Tư liệu, Viện Hàn lâm KH&CN VN) | **Oracle chuẩn để đối chiếu** |

---

## 3. Algorithm choice

### 3.1 Quy tắc lịch âm Việt Nam (5 quy tắc)

Theo bản mô tả của Hồ Ngọc Đức:

1. Ngày đầu tháng âm là ngày chứa thời điểm **Sóc** (New Moon).
2. Năm thường 12 tháng âm; năm nhuận 13 tháng.
3. **Đông chí luôn rơi vào tháng 11.**
4. Trong năm nhuận, tháng **không chứa Trung khí** (Principal Term) là tháng nhuận.
   Nếu có hai tháng như vậy, chỉ tháng **đầu tiên sau Đông chí** được coi là nhuận.
5. Mọi tính toán quy về **kinh tuyến 105° Đông**.

Quy tắc 1–4 **giống hệt lịch Trung Quốc**. Khác biệt duy nhất và cốt lõi nằm ở quy
tắc 5: Việt Nam dùng **105°Đ (UTC+7)**, Trung Quốc dùng **120°Đ (UTC+8)**.

Minh hoạ của chính tác giả:

> "If you have New Moon at yyyy-02-18 16:24:45 GMT, then the first day of the
> Vietnamese lunar month is yyyy-02-18, because 16:24:45 GMT is 23:24:45 Hanoi time
> of the same day."

Cùng thời điểm đó ở Bắc Kinh đã sang ngày 19 ⇒ lịch Trung Quốc bắt đầu tháng muộn
hơn một ngày. **Một giờ đồng hồ đủ để lệch cả tháng nhuận** — xem §7.

### 3.2 Hai bài toán thiên văn cần giải

Toàn bộ lịch chỉ cần hai hàm:

1. `newMoonTime(k)` — thời điểm Sóc thứ k (UTC).
2. `sunLongitude(jd)` — hoàng kinh Mặt Trời, để xác định Trung khí và Đông chí.

Độ chính xác cần: **cỡ phút**. Sai một phút chỉ đổi kết quả khi Sóc rơi sát nửa đêm
giờ Hà Nội — hiếm, nhưng chính những ca đó tạo ra khác biệt VN/TQ, nên **không được
dùng công thức quá thô**.

### 3.3 Nguồn công thức

Các công thức chuỗi rút gọn cho Sóc và hoàng kinh Mặt Trời là **sự kiện toán học/
thiên văn**, không phải biểu đạt có bản quyền. Điều bị bảo hộ là *văn bản sách* và
*code cụ thể*.

> ⚠️ **CHƯA XÁC MINH:** điều khoản chính xác mà Jean Meeus đặt ra cho việc dùng
> thuật toán trong sách *Astronomical Algorithms* vào phần mềm thương mại. Tôi
> **chưa đọc được** trang bản quyền của sách. Phải xác minh trước khi implement,
> hoặc chọn nguồn công thức có điều khoản rõ ràng.
>
> Đây là **rủi ro pháp lý còn mở duy nhất** của Phase 3.

### 3.4 Đề xuất

**Tự viết Kotlin từ quy tắc**, không port code của ai. Attribution ghi rõ:

> Quy tắc lịch âm Việt Nam tham khảo mô tả của Hồ Ngọc Đức. Không sử dụng mã nguồn
> của tác giả. Đối chiếu với *Lịch Việt Nam thế kỷ XX–XXI* của Trần Tiến Bình.

---

## 4. Proposed implementation strategy

```
core/lunar/                      ← Kotlin thuần, CẤM import android.*
├── VietnameseLunarCalendar.kt   ← API công khai (interface + impl)
├── LunarDate.kt                 ← domain model canonical
├── LunarError.kt                ← error model
├── VietnamTimeZone.kt           ← múi giờ theo mốc lịch sử (§6)
└── internal/
    ├── Astronomy.kt             ← newMoonTime(k), sunLongitude(jd)
    └── JulianDay.kt             ← Gregorian ↔ Julian Day Number
```

Không bảng tra cứu khổng lồ. Không asset JSON. Không truy cập database.

---

## 5. Supported year range

**Không chọn 1800–2199 chỉ vì thuận tiện.** Bằng chứng thu được:

| Nguồn | Phạm vi |
|---|---|
| `amlich.js` v0.5 của HND (bảng tra cứu TK19/TK20/TK21) | 1800–2199 |
| Trang hướng dẫn của HND | "Chọn một tháng của một năm trong khoảng 1800-2199" |
| Trang lịch của HND | "The historic calendar is reliable for years since 1301" |
| **Trần Tiến Bình, lịch chính thức VN** | **1901–2100** |

Vấn đề: ta bỏ code của HND, nên phạm vi của HND **không tự động áp dụng** cho
implementation của ta. Thứ quyết định phạm vi *bảo đảm* là **phạm vi ta kiểm chứng
được**, mà oracle chuẩn (sách Trần Tiến Bình) chỉ phủ **1901–2100**.

### Đề xuất

| | |
|---|---|
| **Phạm vi BẢO ĐẢM** | **1901–2100** — trùng oracle chính thức, kiểm chứng được |
| Ngoài phạm vi | Ném `LunarError.UnsupportedYear`, **không im lặng ngoại suy** |
| Mở rộng về sau | Chỉ khi tìm được oracle đáng tin cho 1800–1900 và 2101–2199 |

1901–2100 dư sức cho Nếp Nhà: ngày giỗ luôn được quy đổi cho các năm sắp tới, và
người sinh trước 1901 thì nay đã 125 tuổi.

---

## 6. Timezone & historical research

### 6.1 Nguyên tắc bất di bất dịch

Engine **cố định theo domain Việt Nam**, tuyệt đối không đọc timezone của máy.
`TimeZone.getDefault()` không được xuất hiện trong `core/lunar`.

### 6.2 Lịch sử múi giờ Việt Nam

Nguồn: <https://en.wikipedia.org/wiki/Time_in_Vietnam> (2026-08-25)

| Giai đoạn | Offset |
|---|---|
| Trước 1906-07-01 | UTC+07:06:40 (giờ mặt trời địa phương) |
| 1906-07-01 → 1911-04-30 | UTC+07:06:30 |
| 1911-05-01 → 1942-12-30 | UTC+07:00 |
| 1942-12-31 → 1945-03-13 | UTC+08:00 |
| 1945-03-14 → 1945-09-01 | UTC+09:00 (giờ Tokyo) |
| **Miền Bắc** 1945-09-02 → 1947-03-31 | UTC+07:00 |
| **Miền Bắc** 1947-04-01 → Geneva 1954 | Không thống nhất (+7 vùng yên, +8 vùng bị đánh phá) |
| **Miền Bắc** 1968-01-01 → 1975-06-12 | UTC+07:00 |
| **Miền Nam** Geneva 1954 → 1955-06-30 | UTC+08:00 |
| **Miền Nam** 1955-07-01 → 1959-12-31 | UTC+07:00 |
| **Miền Nam** 1960-01-01 → 1975-06-12 | UTC+08:00 |
| Toàn quốc 1975-06-13 → nay | UTC+07:00 |

### 6.3 Mốc 1967/1968 — đã giải quyết được mâu thuẫn

Hai nguồn thoạt nhìn mâu thuẫn:

- Wikipedia: miền Bắc dùng UTC+7 **từ 1968-01-01**.
- Trang của HND: *"GMT+480 min was used … in North Vietnam before 7/8/1967"*.

Báo Hànộimới (dẫn Ban Lịch Nhà nước) hoà giải được:

> "Quyết định số 121/CP" do Thủ tướng Phạm Văn Đồng ký ngày **8/8/1967**, **có hiệu
> lực từ 1968**.

Nguồn: <https://hanoimoi.vn/lich-viet-nam-hieu-the-nao-cho-dung-105092.html>

⇒ Ngày **ký** là 8/8/1967, ngày **hiệu lực** là 1/1/1968. HND ghi mốc theo ngày văn
bản; Wikipedia ghi theo ngày hiệu lực. Không thực sự mâu thuẫn.

> ⚠️ **CHƯA XÁC MINH:** HND ghi `7/8/1967` chứ không phải `8/8/1967`. Lệch một ngày,
> hoặc là khác cách viết ngày/tháng. Cần đọc chính văn Quyết định 121/CP.

### 6.4 VẤN ĐỀ NGHIỆP VỤ CHƯA CÓ LỜI GIẢI — cần chủ dự án quyết

Từ **1955 đến 1975, Việt Nam tồn tại HAI lịch âm khác nhau** vì hai miền dùng hai
múi giờ. Chính HND cũng hiển thị cả hai:

> "Between 1955 and 1975 the additional lunar day is the one according to the
> calendar used in South Vietnam."

Ví dụ nổi tiếng nhất: **Tết Mậu Thân 1968**, miền Bắc ăn Tết **29/01/1968**, miền Nam
**30/01/1968** (nguồn: Wikipedia tiếng Việt, *Sự kiện Tết Mậu Thân*).

**Hệ quả trực tiếp cho Nếp Nhà:** một người sinh hoặc mất ở Sài Gòn năm 1968 có ngày
âm **khác** người sinh cùng ngày dương ở Hà Nội. Đây không phải chi tiết học thuật —
đúng đối tượng người dùng của app.

Ba phương án:

| | Phương án | Đánh đổi |
|---|---|---|
| A | Luôn dùng múi giờ **chính thức của nhà nước** theo từng thời kỳ (giống sách Trần Tiến Bình) | Nhất quán với lịch chính thức; nhưng gia đình miền Nam có ký ức khác |
| B | Cho người dùng chọn **Bắc/Nam** cho các ngày trong 1955–1975 | Đúng hơn về mặt gia đình; thêm một field + một câu hỏi khó hiểu trong UI |
| C | Chỉ **bảo đảm từ 1976** trở đi, trước đó cảnh báo | Đơn giản nhất, nhưng bỏ rơi ngày giỗ của các cụ |

**Đề xuất: A cho MVP** (một lịch, theo nhà nước), **ghi rõ trong app**, và để B vào
roadmap. Nhưng đây là **quyết định của chủ dự án, không phải của tôi** — nó động tới
ký ức gia đình, không chỉ code.

---

## 7. Vietnamese vs Chinese difference

Đây là bằng chứng engine không phải "lịch Trung Quốc đổi tên".

### 7.1 Các năm khác nhau đã có nguồn

| Năm | Việt Nam | Trung Quốc | Nguồn |
|---|---|---|---|
| **1985** | Tết **21/01/1985** | Tết **20/02/1985** | Thanh Niên; Wikipedia (EN) *Tết* |
| 1984 | **không** có tháng nhuận | nhuận **tháng 10** | Hànộimới |
| 1987 | nhuận **tháng 7** | nhuận **tháng 6** | Hànộimới |
| 2006 | lệch nhau 25/06 → 24/07 | | Tuổi Trẻ |
| **2007** | Tết **17/02/2007** | Tết **18/02/2007** | HND *calrules*; Tuổi Trẻ (lệch 17/02 → 18/03) |
| 2008 | lệch nhau 27/11 → 26/12 | | Tuổi Trẻ |
| **2030** | Tết **02/02/2030** | Tết **03/02/2030** | HND *calrules*; **đối chiếu chéo**: Wikipedia *Tết* ghi Tuất = 2 February 2030 ✅ |
| **2053** | Tết **18/02/2053** | Tết **19/02/2053** | HND *calrules* |

Nguyên văn từ HND:

> "In 2007 the Vietnamese New Year is on 17/02/2007, the Chinese one on 18/02/2007.
> In 2030 the dates are 02/02/2030 and 03/02/2030, and in 2053 they are 18/02/2053
> and 19/02/2053."

**1985 là ca mạnh nhất**: lệch **cả một tháng**, không phải một ngày. Nếu engine trả
20/02/1985 cho Tết Ất Sửu thì nó là lịch Trung Quốc.

### 7.2 Cảnh báo có thật ở Việt Nam

Báo Tuổi Trẻ, bài *"Lịch sai do dịch sách Trung Quốc"*: nhiều sách lịch vạn niên bán
trên thị trường **dịch thẳng từ nguồn Trung Quốc** nên sai lịch Việt Nam.
Nguồn: <https://tuoitre.vn/lich-sai-do-dich-sach-trung-quoc-151667.htm>

⇒ **Không phải cứ lịch in ra là dùng làm oracle được.** Xem §8.

---

## 8. Oracle sources

### 8.1 Vấn đề nghiêm trọng: hầu hết nguồn online KHÔNG độc lập

Phần lớn website lịch âm Việt Nam đều chạy cùng một implementation của Hồ Ngọc Đức.
Chúng khớp nhau vì **cùng một code**, không phải vì cùng đúng. Lấy chúng làm
"đối chiếu chéo" là tự lừa mình.

### 8.2 Xếp hạng nguồn

| Hạng | Nguồn | Ghi chú |
|---|---|---|
| ⭐ A | **Trần Tiến Bình, _Lịch Việt Nam thế kỷ XX–XXI (1901–2100)_**, NXB Văn hoá – Thông tin 2005 | Từ Ban Lịch Nhà nước. **Sách in ⇒ độc lập với mọi code.** Oracle chuẩn |
| B | Wikipedia (bảng ngày Tết 2020–2043) | Độc lập với HND; đã dùng đối chiếu chéo 2030 ✅ |
| B | Báo chí dẫn Ban Lịch Nhà nước (Hànộimới, Tuổi Trẻ, Thanh Niên) | Tốt cho các ca đặc biệt 1984/1985/1987/2006–2008 |
| C | Trang `calrules_en.html` của HND | Chính tác giả, nhưng **cùng nguồn** với implementation ⇒ không tính là đối chiếu độc lập |
| ⛔ D | Các web lịch vạn niên Việt Nam | Gần như chắc chắn là HND. **Không dùng làm oracle** |
| ⛔ D | Sách lịch dịch từ Trung Quốc | Đã được báo chí chỉ ra là sai |

### 8.3 Việc cần chủ dự án làm

Mua/mượn **sách Trần Tiến Bình** rồi đối chiếu tay bảng vector ở
[LUNAR_TEST_VECTORS.md](LUNAR_TEST_VECTORS.md). Phase 2 đã dạy: 26 test xanh vẫn để
lọt một bug thật. Ở đây còn nguy hiểm hơn — test có thể **xanh mà sai**, vì cả code
lẫn expected value đều bắt nguồn từ cùng một chỗ.

---

## 9. Test vector table

Xem [LUNAR_TEST_VECTORS.md](LUNAR_TEST_VECTORS.md).

---

## 10. Bidirectional test strategy

```
Chiều xuôi:  Gregorian → Lunar         so với oracle
Chiều ngược: Lunar     → Gregorian     so với oracle
Round-trip A: g → toLunar → toSolar == g     (mọi ngày trong nhiều năm liên tiếp)
Round-trip B: l → toSolar → toLunar == l     (mọi ngày âm hợp lệ, gồm tháng nhuận)
```

Round-trip A nên chạy **toàn bộ** ngày của một dải năm (ví dụ 1990–2050 ≈ 22.000
ngày) — rẻ, và bắt được lệch biên mà vài chục vector không thấy.

Round-trip B **bắt buộc phủ tháng nhuận**: `(15, 6, 2025, leap=true)` và
`(15, 6, 2025, leap=false)` phải cho **hai ngày dương khác nhau**.

---

## 11. Invalid date strategy — ranh giới với business rule

Đây là điểm quan trọng nhất về mặt kiến trúc.

```
Ngày 30 tháng 8 âm, năm đó tháng 8 chỉ có 29 ngày

  VietnameseLunarCalendar  →  LunarError.NonexistentLunarDate
                              (KHÔNG tự sửa 30 → 29)

  EventCalculator (Phase 7) →  áp MemorialRule.missingDayPolicy
                               = LAST_VALID_DAY_OF_MONTH
                            →  dùng ngày 29, đánh dấu wasAdjusted
```

Engine **không biết** khái niệm ngày giỗ. Nó chỉ trả lời "ngày này không tồn tại".
Việc *có được phép lùi về 29 hay không* là quy tắc nghiệp vụ đã chốt ở
[MEMORIAL_RULES.md](MEMORIAL_RULES.md), và nằm ở tầng khác.

Trộn hai thứ này lại sẽ khiến không thể phân biệt "lịch sai" với "gia đình chọn
tính theo ngày cuối tháng".

---

## 12. Error model

```kotlin
sealed interface LunarError {
    /** Năm ngoài phạm vi bảo đảm (1901–2100). */
    data class UnsupportedYear(val year: Int, val supported: IntRange) : LunarError

    /** Ngày dương không tồn tại (31/02, 29/02 năm không nhuận…). */
    data class InvalidGregorianDate(val year: Int, val month: Int, val day: Int) : LunarError

    /** Giá trị ngày âm sai khoảng (tháng 13, ngày 31…). */
    data class InvalidLunarDate(val date: LunarDate) : LunarError

    /** Giá trị hợp lệ về khoảng, nhưng năm đó KHÔNG có ngày này. */
    data class NonexistentLunarDate(val date: LunarDate, val lastValidDay: Int) : LunarError

    /** Năm đó không có tháng nhuận này. */
    data class NoSuchLeapMonth(val year: Int, val month: Int) : LunarError
}
```

Nguyên tắc: **không dùng `null` để che lỗi nghiệp vụ.** `null` chỉ có nghĩa "không
có giá trị"; "không tính được" phải là một lỗi có tên và có dữ liệu kèm theo —
`NonexistentLunarDate` mang sẵn `lastValidDay` để `EventCalculator` áp policy mà
không phải hỏi lại engine.

---

## 13. API proposal

```kotlin
package com.nepnha.core.lunar

/** Model canonical DUY NHẤT cho ngày âm. Không nơi nào tự ghép day/month/isLeap. */
data class LunarDate(
    val day: Int,
    val month: Int,
    val year: Int,
    val isLeapMonth: Boolean = false,
)

interface VietnameseLunarCalendar {

    val supportedYears: IntRange           // 1901..2100

    fun toLunar(solar: LocalDate): LunarResult<LunarDate>

    fun toSolar(lunar: LunarDate): LunarResult<LocalDate>

    /** Số ngày của một tháng âm: 29 hoặc 30. Cần cho missingDayPolicy. */
    fun daysInLunarMonth(year: Int, month: Int, isLeapMonth: Boolean): LunarResult<Int>

    /** Tháng nhuận của năm âm, hoặc null nếu năm thường. */
    fun leapMonthOf(lunarYear: Int): LunarResult<Int?>

    /** Can Chi của năm âm, ví dụ "Bính Ngọ". */
    fun sexagenaryYear(lunarYear: Int): LunarResult<String>
}

sealed interface LunarResult<out T> {
    data class Success<T>(val value: T) : LunarResult<T>
    data class Failure(val error: LunarError) : LunarResult<Nothing>
}
```

Ghi chú:

- `isLeapMonth` **nằm trong** `LunarDate`, không tồn tại như tham số rời rạc ở nơi
  khác. Đúng yêu cầu mục 9.
- Không hàm nào nhận `TimeZone`. Múi giờ là **thuộc tính của lịch Việt Nam**, do
  `VietnamTimeZone` quyết định theo mốc lịch sử (§6.2), không phải tuỳ chọn của
  người gọi.
- `LocalDate` là `java.time.LocalDate` — có sẵn native từ minSdk 26, không cần
  desugaring, không phải Android API.

---

## 14. Performance considerations

Mỗi lần đổi ngày chỉ cần: vài phép lượng giác + một vòng lặp ngắn tìm Sóc/Trung khí.
**O(1)** trên thực tế.

- Không truy vấn database.
- Không đọc asset.
- Không coroutine — hàm thuần, gọi thẳng trên main thread cũng vô hại.
- Cân nhắc cache trong một phiên: màn Lịch đổi 30 ngày cùng tháng sẽ tính lại Sóc
  nhiều lần. Chỉ tối ưu **sau khi đo**, không tối ưu sớm.
- Ưu tiên tuyệt đối: **đúng trước, nhanh sau.**

---

## 15. Risks

| # | Rủi ro | Mức | Xử lý |
|---|---|---|---|
| R1 | Dùng nhầm code phi thương mại của HND | **Cao** | Đã chặn: tự viết từ quy tắc, không port |
| R2 | Dùng thư viện "MIT" thực chất là port của HND | **Cao** | Đã chặn: §1.3, không dùng thư viện nào |
| R3 | Điều khoản dùng thuật toán Meeus **chưa xác minh** | **Trung bình** | Phải làm rõ trước khi code — rủi ro pháp lý còn mở duy nhất |
| R4 | Không có oracle độc lập ⇒ test xanh mà sai | **Cao** | Bắt buộc đối chiếu sách Trần Tiến Bình bằng tay |
| R5 | Hai lịch miền Bắc/Nam 1955–1975 | **Trung bình** | Cần chủ dự án chọn phương án A/B/C (§6.4) |
| R6 | Sai số thiên văn ở ca Sóc sát nửa đêm | **Trung bình** | Test riêng 1985/2007/2030 — đúng những ca đó |
| R7 | Engine tự ý sửa ngày 30→29 | Trung bình | Đã chặn bằng thiết kế: §11 |
| R8 | Timezone của máy lọt vào kết quả | Trung bình | Cấm `TimeZone.getDefault()` trong `core/lunar` + test đổi default TZ |

---

## 16. Recommendation

**Được phép làm Phase 3, với 3 điều kiện tiên quyết:**

1. **Xác minh điều khoản Meeus (R3)** — hoặc chọn nguồn công thức có điều khoản rõ.
   Chưa xong thì chưa viết `Astronomy.kt`.
2. **Chủ dự án chốt phương án Bắc/Nam 1955–1975** (§6.4). Đề xuất A.
3. **Có sách Trần Tiến Bình trong tay** trước khi chốt bảng test vector.

Còn lại đã sẵn sàng:

- Không dùng code của ai ⇒ R1, R2 đóng.
- Phạm vi bảo đảm **1901–2100**, ngoài ra báo lỗi rõ ràng.
- API, error model, ranh giới engine/business rule đã thiết kế.
- Đã có các ca VN≠TQ có nguồn để chứng minh engine đúng là lịch Việt Nam.

**Không thay đổi database. Không thêm dependency. Không nối UI.**
