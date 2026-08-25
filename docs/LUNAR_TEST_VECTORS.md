# Test vectors — lịch âm Việt Nam

> Bảng oracle cho Phase 3. **Chưa dùng để code.** Mỗi dòng phải có nguồn.
> Nghiên cứu 2026-08-25.

## Metadata bắt buộc cho mỗi vector

Theo mục 12 của chỉ đạo Phase 3A. Không lưu trần trụi `2026-02-17 -> 01/01/2026`.

```yaml
- input:                 2026-02-17          # ngày dương
  expected:              01/01/2026          # ngày âm
  leap_month:            false
  input_source:          —                   # rỗng nếu input do ta chọn
  expected_value_source: Thông báo 9441/TB-BNV (Bộ Nội vụ)
  source_tier:           1
  independence:          Nhánh A (nhà nước) — độc lập với HND
  license_status:        Văn bản pháp quy
  verified:              true
  verification_date:     2026-08-25
  notes:                 Tết Bính Ngọ
```

`verified: false` ⇒ **không được dùng làm authoritative assertion**, chỉ để ghi nhận.

**Data source ≠ Oracle.** NASA Moon Phases là *data source* của engine ⇒ **tuyệt đối
không** được dùng làm `expected_value_source`. Kiểm engine phải bằng nhánh khác.

---

## Phân hạng mức xác nhận (Phase 3A.4)

Một vector chỉ được dùng làm **expected value** khi đạt **V1**.

| Mức | Nguồn | Dùng làm gì |
|---|---|---|
| **V1** Independent Tier 1 | Trần Tiến Bình · văn bản nhà nước | ✅ **expected value** |
| **V2** Independent astronomical | NASA · HKO · ERFA | ✅ cho thiên văn · ❌ cho quy tắc lịch VN |
| **V3** Vietnamese-calendar implementation | lichviet.app, các lịch VN khác | ⚠️ **chỉ cảnh báo sai lệch** |
| **V4** Online black-box | Nguồn ancestry UNKNOWN | ⚠️ chỉ phát hiện sai lệch |
| **V5** Unresolved | Nguồn mâu thuẫn | ⛔ điều tra, không chọn bên |

> ⛔ **V3/V4 đồng ý KHÔNG BAO GIỜ nâng thành "verified".** Nhiều website cùng cho một
> kết quả chỉ chứng minh chúng cùng một implementation. Xem
> [LUNAR_ONLINE_ORACLE_PROVENANCE.md](LUNAR_ONLINE_ORACLE_PROVENANCE.md).
>
> ⛔ **Không commit dữ liệu lấy từ lichviet.app** — điều khoản của họ cấm sao chép/
> tái tạo/phân phối "dữ liệu" nếu không có văn bản cho phép.

---

## Quy ước trạng thái

| Ký hiệu | Nghĩa |
|---|---|
| ✅ **ĐÃ ĐỐI CHIẾU CHÉO** | Ít nhất hai nguồn **độc lập** khớp nhau |
| 🟡 **MỘT NGUỒN** | Chỉ một nguồn — dùng được nhưng phải xác nhận lại bằng sách |
| ⛔ **CHƯA XÁC MINH** | Chưa có nguồn đạt chuẩn — **không được đưa vào test** |

> ⚠️ Cảnh báo phương pháp: phần lớn website lịch âm Việt Nam chạy **cùng một
> implementation** của Hồ Ngọc Đức. Chúng khớp nhau vì cùng một code, không phải vì
> cùng đúng. Đối chiếu độc lập thật sự phải dùng **sách in**:
> Trần Tiến Bình, *Lịch Việt Nam thế kỷ XX–XXI (1901–2100)*, NXB Văn hoá – Thông tin,
> 2005 — biên soạn từ Ban Lịch Nhà nước.

---

## A. Tết Nguyên Đán

| Năm âm | Ngày dương (mùng 1 Tết) | Trạng thái | Nguồn |
|---|---|---|---|
| Canh Tý 2020 | 25/01/2020 | 🟡 | Wikipedia *Tết* |
| Tân Sửu 2021 | 12/02/2021 | 🟡 | Wikipedia *Tết* |
| Nhâm Dần 2022 | 01/02/2022 | 🟡 | Wikipedia *Tết* |
| Quý Mão 2023 | 22/01/2023 | 🟡 | Wikipedia *Tết* |
| Giáp Thìn 2024 | 10/02/2024 | 🟡 | Wikipedia *Tết* |
| Ất Tỵ 2025 | 29/01/2025 | 🟡 | Wikipedia *Tết* |
| **Ất Tỵ 2025** | **29/01/2025** | ✅ **TIER 1** | **6150/TB-BLĐTBXH** (03/12/2024) — văn bản nhà nước, nhánh A |
| **Bính Ngọ 2026** | **17/02/2026** | ✅ **TIER 1** | **Thông báo 9441/TB-BNV (Bộ Nội vụ)** — văn bản nhà nước, nhánh A |
| Đinh Mùi 2027 | 06/02/2027 | 🟡 | Wikipedia *Tết* |
| Mậu Thân 2028 | 26/01/2028 | 🟡 | Wikipedia *Tết* |
| Kỷ Dậu 2029 | 13/02/2029 | 🟡 | Wikipedia *Tết* |
| **Canh Tuất 2030** | **02/02/2030** | 🟡 *(hạ từ ✅)* | Wikipedia *Tết* + HND *calrules_en* — **có thể cùng nhánh**, xem cảnh báo trên |
| Tân Hợi 2031 | 23/01/2031 | 🟡 | Wikipedia *Tết* |

Wikipedia còn có tới 2043; sẽ bổ sung sau khi đối chiếu sách.

### A.0 Vector từ văn bản nhà nước — đã xác minh (Phase 3A.1)

| Ngày dương | Ngày âm | Văn bản | verified |
|---|---|---|---|
| 25/01/2025 | 26 tháng Chạp Giáp Thìn | 6150/TB-BLĐTBXH | ✅ |
| **29/01/2025** | **Mùng 1 Tết Ất Tỵ** | 6150/TB-BLĐTBXH | ✅ |
| 02/02/2025 | Mùng 5 tháng Giêng Ất Tỵ | 6150/TB-BLĐTBXH | ✅ |
| 14/02/2026 | 27 tháng Chạp Ất Tỵ | 9441/TB-BNV | ✅ |
| **17/02/2026** | **Mùng 1 Tết Bính Ngọ** | 9441/TB-BNV | ✅ |
| 22/02/2026 | Mùng 6 Tết Bính Ngọ | 9441/TB-BNV | ✅ |

Sáu cặp dương↔âm Tier 1, nhánh A. Đây là **nhóm vector đầu tiên của dự án đạt
`verified: true`**.

---

### A.1 Nguồn Tier 1 mới: văn bản nhà nước công bố nghỉ Tết

Mỗi năm Chính phủ/Bộ ra văn bản ấn định lịch nghỉ Tết, **ghi kèm cả ngày âm lẫn ngày
dương**. Đây là quyết định hành chính, **không phải kết quả của phần mềm nào** ⇒
nhánh A, độc lập hoàn toàn với HND.

| Ngày âm | Ngày dương | Văn bản |
|---|---|---|
| 27 tháng Chạp Ất Tỵ | 14/02/2026 | Thông báo 9441/TB-BNV (Bộ Nội vụ) |
| **Mùng 1 Tết Bính Ngọ** | **17/02/2026** | như trên |
| Mùng 6 Tết Bính Ngọ | 22/02/2026 | như trên |

⛔ **CẦN LÀM:** truy các văn bản tương ứng của những năm trước để dựng chuỗi Tier 1
nhiều năm. Chưa làm ⇒ hiện chỉ có 2026.

> Giới hạn: chỉ phủ vùng quanh Tết. Không giúp gì cho Rằm tháng 7, Trung Thu, tháng
> nhuận hay số ngày của tháng âm.

---

## B. Việt Nam KHÁC Trung Quốc — nhóm test quan trọng nhất

Đây là bằng chứng engine không phải lịch Trung Quốc đổi tên.

| Năm | Việt Nam | Trung Quốc | Lệch | Trạng thái | Nguồn |
|---|---|---|---|---|---|
| **1985** | Tết **21/01/1985** | Tết **20/02/1985** | **1 tháng** | ✅ **TIER 1** | VN: Thanh Niên (nhánh A) · TQ: **HKO `T1985e.txt` đã tải, đọc trực tiếp** (nhánh B). Hai nhánh độc lập |
| 1984 | **không** nhuận | nhuận **tháng 10** | tháng nhuận | 🟡 | Hànộimới (dẫn Ban Lịch Nhà nước) |
| 1987 | nhuận **tháng 7** | nhuận **tháng 6** | tháng nhuận | 🟡 | Hànộimới |
| 2006 | khác nhau **25/06 → 24/07** | | 1 ngày | 🟡 | Tuổi Trẻ |
| **2007** | Tết **17/02/2007** | Tết **18/02/2007** | 1 ngày | 🟡 | HND *calrules_en* + Tuổi Trẻ — **phía TQ cần lấy từ HKO** |
| 2008 | khác nhau **27/11 → 26/12** | | 1 ngày | 🟡 | Tuổi Trẻ |
| **2030** | Tết **02/02/2030** | Tết **03/02/2030** | 1 ngày | 🟡 *(hạ từ ✅)* | HND + Wikipedia, **có thể cùng nhánh** |
| 2053 | Tết **18/02/2053** | Tết **19/02/2053** | 1 ngày | 🟡 | HND *calrules_en* |

### B.1 Phía Trung Quốc phải lấy từ HKO, không lấy từ nguồn VN

Đài Thiên văn Hồng Kông (HKO) công bố **bảng đối chiếu Dương–Âm 1901–2100** trên
data.gov.hk, điều khoản **cho phép dùng thương mại** kèm attribution. Đây là nhánh B
— độc lập hoàn toàn với cả HND lẫn Ban Lịch Nhà nước.

⛔ **CẦN LÀM:** tải bảng HKO cho 1984, 1985, 1987, 2006, 2007, 2008, 2030, 2053 và
điền cột "Trung Quốc". Khi đó cột VN (nhánh A) và cột TQ (nhánh B) tạo thành đối
chiếu chéo **thật sự độc lập**, và nhóm test này lên Tier 1.

**1985 là ca xương sống của cả bộ test.** Lệch cả một tháng chứ không phải một ngày:
nếu engine trả 20/02/1985 thì nó đang là lịch Trung Quốc, không cần bàn thêm.

---

## C. Hai lịch trong một nước — 1955 đến 1975

| Sự kiện | Miền Bắc | Miền Nam | Trạng thái | Nguồn |
|---|---|---|---|---|
| **Tết Mậu Thân 1968** | **29/01/1968** | **30/01/1968** | 🟡 | Wikipedia tiếng Việt, *Sự kiện Tết Mậu Thân* |

Nguyên nhân: miền Bắc UTC+7 (Quyết định 121/CP hiệu lực 1968), miền Nam UTC+8.

Test này chỉ viết được **sau khi** chủ dự án chốt phương án A/B/C ở §6.4 của
[PHASE_3_PREFLIGHT.md](PHASE_3_PREFLIGHT.md).

---

## D. Tháng nhuận

| Năm | Tháng nhuận | Trạng thái | Nguồn |
|---|---|---|---|
| 2023 | tháng **2** | 🟡 | Nguồn phổ thông VN — **cần xác nhận bằng sách** |
| 2025 | tháng **6** | 🟡 | Nguồn phổ thông VN — **cần xác nhận bằng sách** |
| 2028 | tháng **5** | 🟡 | Nguồn phổ thông VN — **cần xác nhận bằng sách** |
| 1985 | có nhuận | 🟡 | Nguồn phổ thông VN |
| 1987 | tháng **7** | 🟡 | Hànộimới |
| 1984 | **không nhuận** | 🟡 | Hànộimới |

Các năm âm lịch nhuận 2023–2050 (nguồn phổ thông, ⛔ **chưa đủ chuẩn để test**):
2023, 2025, 2028, 2031, 2033, 2036, 2039, 2042, 2044, 2047, 2050.

> Quy tắc dân gian "chia năm cho 19, dư 0/3/6/9/11/14/17 thì nhuận" **không phải**
> định nghĩa của lịch âm. Định nghĩa thật là quy tắc Trung khí (§3.1). Chu kỳ Meton
> chỉ gần đúng. **Không được dùng nó trong engine, cũng không dùng để sinh expected
> value.**

### Nhóm test bắt buộc quanh tháng nhuận

Không chỉ kiểm `isLeapMonth == true` một lần. Phải phân biệt được **ba tháng liên
tiếp**, lấy 2025 (nhuận tháng 6) làm ví dụ:

| Cần phân biệt | Kỳ vọng |
|---|---|
| 15 tháng 6 **thường** 2025 | một ngày dương X |
| 15 tháng 6 **nhuận** 2025 | một ngày dương Y ≠ X, và Y − X ≈ 29–30 ngày |
| 15 tháng 7 thường 2025 | ngày dương Z > Y |

⛔ Ba giá trị X, Y, Z **CHƯA XÁC MINH** — phải tra sách rồi mới điền.

---

## E. Âm → Dương (nhóm C2 của đặc tả Phase 3A)

⛔ **CHƯA XÁC MINH — chưa có nguồn Tier 1 nào cho nhóm này.**

Cần từ sách Trần Tiến Bình, tối thiểu mỗi loại một vector:

| Loại | Input | Kỳ vọng |
|---|---|---|
| Ngày 1 | (1, m, y, thường) | ngày dương |
| Ngày 15 | (15, m, y, thường) | ngày dương |
| Ngày 29 | (29, m, y) ở tháng **thiếu** | ngày dương — ngày cuối tháng |
| Ngày 30 | (30, m, y) ở tháng **đủ** | ngày dương — ngày cuối tháng |
| **Ngày 30 ở tháng thiếu** | (30, m, y) mà tháng đó chỉ 29 ngày | **`NonexistentLunarDate(lastValidDay = 29)`** |
| Tháng nhuận | (15, m, y, **nhuận**) | ngày dương ≠ ngày của tháng thường |
| Tháng nhuận không tồn tại | (15, m, y, nhuận) ở năm thường | **`NoSuchLeapMonth`** |

Nhóm này nối thẳng với `MemorialRule.missingDayPolicy` ở [MEMORIAL_RULES.md](MEMORIAL_RULES.md):
engine trả lỗi, `EventCalculator` mới được phép lùi 30→29.

---

## F. Dương → Âm cho các ngày lễ MVP (nhóm C1)

⛔ **CHƯA XÁC MINH.**

Cần cho ít nhất 3 năm khác nhau: Rằm tháng Giêng (15/1 âm) · Tết Hàn Thực (3/3 âm) ·
Tết Đoan Ngọ (5/5 âm) · Rằm tháng 7 (15/7 âm) · Trung Thu (15/8 âm) · Ông Công Ông
Táo (23/12 âm) · **ngày cuối năm âm** (29 hoặc 30 tháng Chạp — chú ý nhóm E) ·
**mùng 1 Tết** (đã có, nhánh A).

> **Không được suy** Rằm tháng Giêng = Tết + 14 ngày. Suy ra không phải kiểm chứng,
> và nó chỉ đúng nếu tháng Giêng đủ ngày — mà đó chính là thứ đang cần kiểm.

---

## G. Biên và chuyển năm (nhóm C6)

⛔ **CHƯA XÁC MINH.**

| Input | Kỳ vọng | Nguồn cần |
|---|---|---|
| 01/01/1901 | Tính được | Sách (1901 là năm đầu sách phủ) |
| 31/12/1902 | Tính được | Sách |
| 01/01/2099 | Tính được | Sách |
| 31/12/2100 | Tính được | Sách (2100 là năm cuối sách phủ) |
| 31/12/1900 | **`UnsupportedYear`** | Không cần oracle |
| 01/01/2101 | **`UnsupportedYear`** | Không cần oracle |
| 31/12 ↔ 01/01 dương | Năm âm **không** đổi tại 01/01 dương | Sách |
| Ngày trước / sau mùng 1 Tết | Năm âm đổi **đúng tại Tết** | Nhánh A |

Câu hỏi thiết kế chưa quyết: ngày dương đầu 1901 thuộc **năm âm 1900**. Trả kết quả
hay `UnsupportedYear`? Cần bảng biên rồi mới định nghĩa.

---

## H. Input không hợp lệ (nhóm C7) — không cần oracle ngoài

✅ **Đặc tả xong.** Đây là nhóm duy nhất tự định nghĩa được.

| Input | Kỳ vọng |
|---|---|
| 31/02/2026 dương | `InvalidGregorianDate` |
| 29/02/2025 dương (năm không nhuận) | `InvalidGregorianDate` |
| Ngày âm 0 | `InvalidLunarDate(DAY_OUT_OF_RANGE)` |
| Ngày âm 31 | `InvalidLunarDate(DAY_OUT_OF_RANGE)` |
| Tháng âm 0 | `InvalidLunarDate(MONTH_OUT_OF_RANGE)` |
| Tháng âm 13 | `InvalidLunarDate(MONTH_OUT_OF_RANGE)` |
| Tháng nhuận ở năm thường | `NoSuchLeapMonth` |
| Ngày 30 ở tháng chỉ 29 ngày | `NonexistentLunarDate(lastValidDay = 29)` |
| Năm 1900 / 2101 | `UnsupportedYear` |

---

## H2. Fake precision — 5 ca dữ liệu KHÔNG quyết định được (Phase 3A.1)

Điểm Sóc NASA chỉ chính xác tới **phút**. Ranh giới ngày VN là 17:00:00 UTC. Năm
điểm Sóc rơi **đúng 17:00 UT**, tức giá trị thật nằm hai bên nửa đêm:

| Ngày (UT) | Giờ VN | Yêu cầu với engine |
|---|---|---|
| 1944-06-20 17:00 | 00:00 | ⛔ **CHƯA QUYẾT** hành vi |
| *(1967 xem dòng dưới — đã có thêm dữ kiện V3)* | | |
| 1967-07-07 17:00 | 00:00 | ⛔ trùng giai đoạn múi giờ tranh cãi. **3A.4:** NASA ⇒ Sóc 08/07 · ERFA ⇒ 07/07 · lichviet.app ⇒ 08/07 (V3, ancestry UNKNOWN ⇒ **không đếm phiếu**). Cần V1 |
| 2054-05-07 17:00 | 00:00 | ⛔ |
| 2077-11-15 17:00 | 00:00 | ⛔ |
| 2085-10-18 17:00 | 00:00 | ⛔ |

Engine **không được** âm thầm chọn một ngày rồi trả về như thể chắc chắn. Ba lựa
chọn ở [PHASE_3A1_DATASET_VERIFICATION.md §A.3](PHASE_3A1_DATASET_VERIFICATION.md).

Tái lập: `python3 tools/verify_nasa_newmoons.py`

---

## I. Round-trip — chỉ chứng minh NHẤT QUÁN, không chứng minh ĐÚNG

Nhóm này không cần nguồn ngoài. **Đừng nhầm hai thứ đó.** Code sai một cách nhất
quán vẫn pass toàn bộ nhóm này.

| Test | Phạm vi |
|---|---|
| `toSolar(toLunar(d)) == d` | mọi ngày dương 1901–2100 (~73.000 ngày) |
| `toLunar(toSolar(l)) == l` | mọi ngày âm hợp lệ, **gồm tháng nhuận** |
| Đổi `TimeZone.setDefault()` sang Tokyo / UTC / New York | Kết quả **không đổi** |
| Gọi song song từ nhiều thread | Kết quả không đổi (API là hàm thuần) |

---

## Việc cần làm trước khi code — cổng chặn

| # | Việc | Ai | Mở khoá |
|---|---|---|---|
| 1 | **Có sách Trần Tiến Bình** | Chủ dự án | E, F, G, D — **đường găng** |
| 2 | Tải bảng HKO, điền cột Trung Quốc | Tôi (khi được duyệt) | B lên Tier 1 |
| 3 | Truy văn bản nghỉ Tết các năm trước | Tôi (khi được duyệt) | A lên Tier 1 nhiều năm |
| 4 | Chốt phương án Bắc/Nam A/B/C | **Chủ dự án** | C |

**Chưa xong thì bảng này chưa đủ tư cách làm oracle**, và engine viết ra sẽ không có
gì chứng minh là đúng. Xem cổng đầy đủ ở [PHASE_3A_ORACLE_GATE.md §G](PHASE_3A_ORACLE_GATE.md).
