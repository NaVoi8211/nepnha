# Test vectors — lịch âm Việt Nam

> Bảng oracle cho Phase 3. **Chưa dùng để code.** Mỗi dòng phải có nguồn.
> Nghiên cứu 2026-08-25.

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
| **Bính Ngọ 2026** | **17/02/2026** | 🟡 | Wikipedia *Tết* |
| Đinh Mùi 2027 | 06/02/2027 | 🟡 | Wikipedia *Tết* |
| Mậu Thân 2028 | 26/01/2028 | 🟡 | Wikipedia *Tết* |
| Kỷ Dậu 2029 | 13/02/2029 | 🟡 | Wikipedia *Tết* |
| **Canh Tuất 2030** | **02/02/2030** | ✅ | Wikipedia *Tết* **+** HND *calrules_en* (hai nguồn độc lập) |
| Tân Hợi 2031 | 23/01/2031 | 🟡 | Wikipedia *Tết* |

Wikipedia còn có tới 2043; sẽ bổ sung sau khi đối chiếu sách.

---

## B. Việt Nam KHÁC Trung Quốc — nhóm test quan trọng nhất

Đây là bằng chứng engine không phải lịch Trung Quốc đổi tên.

| Năm | Việt Nam | Trung Quốc | Lệch | Trạng thái | Nguồn |
|---|---|---|---|---|---|
| **1985** | Tết **21/01/1985** | Tết **20/02/1985** | **1 tháng** | ✅ | Thanh Niên **+** Wikipedia EN *Tết* ("a month before China") |
| 1984 | **không** nhuận | nhuận **tháng 10** | tháng nhuận | 🟡 | Hànộimới (dẫn Ban Lịch Nhà nước) |
| 1987 | nhuận **tháng 7** | nhuận **tháng 6** | tháng nhuận | 🟡 | Hànộimới |
| 2006 | khác nhau **25/06 → 24/07** | | 1 ngày | 🟡 | Tuổi Trẻ |
| **2007** | Tết **17/02/2007** | Tết **18/02/2007** | 1 ngày | ✅ | HND *calrules_en* **+** Tuổi Trẻ (lệch 17/02 → 18/03) |
| 2008 | khác nhau **27/11 → 26/12** | | 1 ngày | 🟡 | Tuổi Trẻ |
| **2030** | Tết **02/02/2030** | Tết **03/02/2030** | 1 ngày | ✅ | HND *calrules_en* **+** Wikipedia *Tết* |
| 2053 | Tết **18/02/2053** | Tết **19/02/2053** | 1 ngày | 🟡 | HND *calrules_en* |

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

## E. Ngày 29 / 30 âm

⛔ **CHƯA XÁC MINH — chưa có nguồn nào cho biết tháng âm cụ thể nào có 29 hay 30 ngày.**

Cần từ sách, tối thiểu:

| Cần | Vì sao |
|---|---|
| Một tháng âm **có** ngày 30 | `daysInLunarMonth` phải trả 30 |
| Một tháng âm **chỉ có 29** ngày | `toSolar(30/…)` phải trả `NonexistentLunarDate(lastValidDay = 29)` |
| Cùng tháng đó ở một năm **khác** lại có 30 | Chứng minh không hard-code |

Đây là nhóm test nối thẳng với `MemorialRule.missingDayPolicy` ở
[MEMORIAL_RULES.md](MEMORIAL_RULES.md).

---

## F. Rằm, Mùng 1, Giao thừa

⛔ **CHƯA XÁC MINH.**

Về nguyên tắc suy ra được từ Tết (mùng 1 tháng 1) — nhưng **suy ra không phải là
kiểm chứng**. Rằm tháng Giêng = Tết + 14 ngày chỉ đúng nếu tháng Giêng đủ ngày; đó
lại chính là thứ ta đang cần kiểm.

Phải tra trực tiếp từ sách:

- Rằm tháng Giêng, Rằm tháng 7, Trung Thu (15/8 âm) của vài năm.
- Giao thừa = ngày dương liền trước mùng 1 Tết (suy được, nhưng vẫn nên đối chiếu).
- Ông Công Ông Táo 23/12 âm, Tất niên 30 hoặc 29/12 âm — chú ý đúng nhóm E.

---

## G. Năm biên và chuyển năm

⛔ **CHƯA XÁC MINH.**

| Cần | Kỳ vọng |
|---|---|
| 01/01/1901 (biên dưới) | Có kết quả, không lỗi |
| 31/12/2100 (biên trên) | Có kết quả, không lỗi |
| 31/12/1900 | `UnsupportedYear` |
| 01/01/2101 | `UnsupportedYear` |
| 31/12 ↔ 01/01 quanh giao thừa dương | Năm âm **không** đổi tại 01/01 dương |
| Ngày trước và sau mùng 1 Tết | Năm âm đổi đúng **tại Tết** |

---

## H. Round-trip (không cần oracle)

Nhóm này tự kiểm, không cần nguồn ngoài — nhưng chỉ chứng minh **nhất quán**, không
chứng minh **đúng**. Đừng nhầm hai thứ đó.

| Test | Phạm vi |
|---|---|
| `toSolar(toLunar(d)) == d` | mọi ngày dương 1990–2050 (≈22.000 ngày) |
| `toLunar(toSolar(l)) == l` | mọi ngày âm hợp lệ trong dải đó, **gồm cả tháng nhuận** |
| Đổi `TimeZone.setDefault()` sang Tokyo/UTC/New York | Kết quả **không đổi** |

---

## Việc cần làm trước khi code

1. ⛔ Có **sách Trần Tiến Bình** trong tay.
2. ⛔ Điền các ô CHƯA XÁC MINH ở mục **D, E, F, G**.
3. ⛔ Nâng các dòng 🟡 ở mục A và B lên ✅ bằng cách đối chiếu sách.
4. ⛔ Chốt phương án Bắc/Nam 1955–1975 rồi mới viết test mục C.

**Chưa xong 4 việc này thì bảng test chưa đủ tư cách làm oracle**, và engine viết ra
sẽ không có gì để chứng minh là đúng.
