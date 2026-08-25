# Quyết định kiến trúc: astronomical backend

> Phase 3A.3. **Đề xuất, chưa phải quyết định cuối** — còn chờ chủ dự án trả lời một
> câu hỏi chính sách (§4).

---

## 1. Quyết định đề xuất

> **ERFA (chỉ ngoài máy dev) → dataset thiên văn tái lập được → quy tắc lịch Việt Nam
> bằng Kotlin thuần → Room/UI**

App **không** chứa C, **không** NDK, **không** JNI, **không** thư viện thiên văn.
App chỉ đọc một bảng dữ liệu đã kiểm chứng và áp 5 quy tắc lịch.

---

## 2. Vì sao — bằng chứng, không phải sở thích

| Câu hỏi | Đã đo được | Ở đâu |
|---|---|---|
| ERFA có đủ chính xác cho điểm Sóc? | trung vị 18,9 s, max 78,9 s trên 2.474 mốc/200 năm | [3A.3 §6](PHASE_3A3_ASTRONOMICAL_BENCHMARK.md) |
| ERFA có đủ chính xác cho tiết khí? | max 33 s trên 72/72 mốc, **0** ca sát biên | §7 |
| Sai số đó có làm đổi ngày âm không? | **6/2.474 = 0,24%** có margin < 79 s | §6 |
| ΔY lịch sử có phá không? | ΔT sai 5 s ⇒ **0** ngày đổi | §8 |
| Có cần chuỗi UTC→TAI→TT không? | **Không** — chỉ cần `UT = TT − ΔT` | §8.1 |
| Giấy phép có sạch không? | BSD-3-Clause, đã đọc nguyên văn | §4 |

---

## 3. Ranh giới trách nhiệm

| Tầng | Ai làm | Ở đâu |
|---|---|---|
| Ephemeris | ERFA (BSD-3) | **ngoài app**, máy dev |
| ΔT | NASA catalog | **ngoài app** |
| Sinh dataset | script của ta | `tools/`, có checksum |
| Đối chiếu độc lập | NASA · HKO · sách Trần Tiến Bình | **ngoài app** |
| Quy tắc lịch Việt Nam | **ta viết, Kotlin thuần** | `app/core/lunar` |
| Múi giờ lịch sử | **ta viết**, theo `CalendarContext` | `app/core/lunar` |
| Quy tắc ngày giỗ | **ta viết** | `app/domain/event` — **không** nằm trong engine |

**Nguyên tắc giữ nguyên từ Phase 3A:** engine trả `NonexistentLunarDate`, tuyệt đối
không tự sửa 30 → 29. Đó là `MemorialRule`, tầng khác.

---

## 4. ⚠️ Câu hỏi chính sách còn treo

`eraMoon98` là **implementation thuật toán Meeus** (ERFA ghi rõ nguồn: *Astronomical
Algorithms*, 2nd ed., Willmann-Bell 1998, p337). Dự án đã cấm Meeus.

| Nếu bạn trả lời | Nhánh Mặt Trăng | Hệ quả |
|---|---|---|
| **Cho phép** (code BSD-3 do SOFA viết, ta không chạm sách) | `eraMoon98` sinh bảng điểm Sóc | Độ phân giải giây; vẫn không giải được 5 ca sát biên |
| **Không cho phép** | Dùng **thẳng dữ liệu NASA** (2.474 mốc) | Độ phân giải phút; 5 ca sát biên **chắc chắn** cần oracle thứ ba. *(Lưu ý: NASA cũng dựa trên Meeus — cùng vấn đề, ở dạng dữ liệu)* |
| **Không cho phép, và cũng không muốn NASA** | Tìm ELP/MPP02 hoặc JPL ephemeris | **UNKNOWN** — chưa nghiên cứu giấy phép |

**Nhánh Mặt Trời (`epv00`) không dính Meeus** — làm được bất kể câu trả lời.

---

## 5. Nghĩa vụ giấy phép phải thực hiện

Nếu dùng ERFA để sinh dataset:

- [ ] Ghi trong `docs/` và mục "Giới thiệu" của app: *"Dữ liệu thiên văn được tạo
      bằng ERFA, thư viện phái sinh từ IAU SOFA"* — đúng khuyến nghị của ERFA là
      ghi *"a library derived from SOFA, rather than SOFA itself"*.
- [ ] Giữ nguyên văn bản quyền + disclaimer BSD-3 của ERFA trong `tools/`.
- [ ] **Không** dùng tên SOFA Board / IAU để quảng bá Nếp Nhà.
- [ ] Ghi công NASA: *"Moon Phase Predictions by Fred Espenak, NASA/GSFC"*.
- [ ] Ghi công HKO nếu dùng dữ liệu của họ trong app (hiện chỉ dùng để đối chiếu
      ngoài app ⇒ chưa phát sinh nghĩa vụ).

---

## 6. Rủi ro còn lại của kiến trúc này

| Rủi ro | Mức | Xử lý |
|---|---|---|
| Bảng dữ liệu sai mà không ai phát hiện | **Cao** | Đối chiếu sách Trần Tiến Bình; ship checksum; script tái lập được |
| 5 ca sát biên | Trung bình | Chưa quyết — **không** hardcode, **không** giả vờ chính xác |
| Phạm vi cứng 1901–2100 | Thấp | Ngoài phạm vi trả `UnsupportedYear`; muốn mở rộng phải sinh lại bảng **và** có oracle mới |
| Không ai kiểm được pipeline sinh dữ liệu | Trung bình | Script nằm trong `tools/`, input là URL công khai, chạy lại ra đúng số |
| `moon98` không được IAU chứng thực | Thấp | Header ghi rõ *"Not IAU-endorsed"*; đã đo sai số thực nghiệm |

---

## 7. Trạng thái

**CHƯA ĐƯỢC IMPLEMENT.** Benchmark chỉ chứng minh *phương tiện* đủ tốt. Nó **không**
chứng minh lịch ta tính ra đúng lịch Việt Nam — việc đó cần oracle Tier 1, mà
G5/G6/G7/G9/G10 vẫn đang BLOCKED.
