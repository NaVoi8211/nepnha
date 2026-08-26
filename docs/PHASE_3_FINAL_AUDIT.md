# Phase 3 — Final audit

> Kiểm toán trước khi chuyển Phase 4. Nhãn: **PASS** · **FAIL** · **BLOCKER**.
>
> ⚠️ Đánh giá kỹ thuật/provenance. **Không phải tư vấn pháp lý.**

> ✅ **BLOCKER ĐÃ XỬ LÝ** — xem
> [PHASE_3_DATASET_CORRECTION.md](PHASE_3_DATASET_CORRECTION.md). Nguyên nhân thật
> khác chẩn đoán ban đầu ở §B: regex đọc **đúng** cột, nhưng cột ΔT của NASA chỉ có
> **độ phân giải phút**.

# ⛔ KẾT LUẬN LÚC AUDIT: **NOT READY** — 1 blocker

Mười lăm trên mười sáu cổng PASS. Cổng **G3/G8** FAIL vì một lỗi đo được, không phải
vì thiếu nghiên cứu. Blocker có phạm vi hẹp, nguyên nhân đã xác định chính xác, và
cách sửa nằm trọn trong `tools/`.

---

## A. Bảng cổng

| Gate | Status | Bằng chứng |
|---|---|---|
| **G1** Dataset reproducible | ✅ PASS | Sinh lại dưới `TZ=America/Los_Angeles`, `LC_ALL=tr_TR.UTF-8` → `.bin` **giống hệt từng byte**, sha256 không đổi. Sau khi vá A1, `.json` cũng giống hệt |
| **G2** NASA-first frozen | ✅ PASS | 6 ca sát biên đều chọn NASA; không hardcode ngoại lệ; không vote; giá trị ERFA giữ trong provenance |
| **G3** ERFA principal-term pipeline | ⛔ **FAIL** | **ΔT bị trích sai** — xem §B |
| **G4** 105°Đ frozen | ✅ PASS | `VN_OFFSET_MINUTES = 7*60`, hằng số duy nhất, không đọc `ZoneId.systemDefault()` |
| **G5** API frozen | ✅ PASS | 6 hàm, `LunarResult` sealed, không `null` che nghiệp vụ |
| **G6** Boundary verified | ✅ PASS | 11 test biên; nhãn năm âm 1900 ở đầu 1901 hợp lệ |
| **G7** Leap-month semantics | ✅ PASS | Tháng nhuận mang số tháng liền trước — có test riêng |
| **G8** Ca 1938 | ⛔ **FAIL** | **Có ảnh hưởng R4** — xem §B |
| **G9** Historical limitation | ✅ PASS | Không có câu nào tuyên bố UTC+7 là cách VN từng tính mọi thời kỳ |
| **G10** Meeus limitation | ✅ PASS | `sunbatch` chỉ gọi `eraEpv00`/`eraEqec06`/`eraNut06a`; `moon98` không nằm trên đường sinh dataset |
| **G11** Online oracle policy | ✅ PASS | Không scrape; không dữ liệu online nào trong APK |
| **G12** Unit tests | ✅ PASS | 53/53 |
| **G13** Instrumented | ✅ PASS | 30/30 trên SM‑A325F |
| **G14** Performance | ✅ PASS | 365 ngày trong 51 ms / ngưỡng 500 ms |
| **G15** APK sạch | ✅ PASS *(có đính chính)* | Không INTERNET, không dependency mới, không native của Nếp Nhà. **Đính chính:** APK **có** 8 file `.so` — `androidx.graphics.path` và `datastore_shared_counter`, đến từ AndroidX của Phase 1/2, **không** từ engine lịch và **không** từ ERFA |
| **G16** Git clean | ✅ PASS | Working tree sạch sau commit |

---

## B. ⛔ BLOCKER — ΔT bị trích sai, làm lệch tháng nhuận năm 1938

### B.1 Cơ chế

`compare_with_nasa.parse()` trích ΔT bằng regex `(\d{2})h(\d{2})m` rồi quy ra giây.
Regex này **không khớp cột ΔT** của trang NASA. Kết quả: ΔT chỉ nhận đúng bốn giá trị.

```
phân bố ΔT mà script sinh ra:  60s ×95   0s ×51   120s ×30   180s ×24
```

> ⚠️ **ĐÃ BỊ THAY THẾ — hai điểm trong mục này về sau xác định là sai:**
> **(1)** *"Regex không khớp cột ΔT"* — regex đọc **đúng** cột; vấn đề là bản thân cột
> chỉ có **độ phân giải phút**.
> **(2)** Cột *"ΔT thực tế"* trong bảng dưới lấy giá trị quan trắc hiện đại làm chuẩn.
> Sai chuẩn: đường sản xuất dùng **mô hình đa thức NASA/Espenak**, và với 2026 mô hình
> cho **≈ 75,4 s** — đúng bằng con số NASA nêu, **không phải lỗi**.
> Giữ nguyên văn bản gốc để không viết lại lịch sử. Xem
> [PHASE_3_DATASET_CORRECTION.md](PHASE_3_DATASET_CORRECTION.md) và
> [PHASE_3_MEEUS_PROVENANCE.md](PHASE_3_MEEUS_PROVENANCE.md).

| Năm | ΔT script dùng | ΔT tham chiếu dùng lúc audit | Sai |
|---|---:|---:|---:|
| 1920 | 0 s | 21,2 s | −21 s |
| **1938** | **0 s** | **23,9 s** | **−24 s** |
| 1970 | 60 s | 40,2 s | +20 s |
| 2026 | 60 s | ~~69,5 s~~ ⚠️ | — |
| 2100 | 180 s | 202,8 s | −23 s |

Điểm Sóc **không bị ảnh hưởng** — chúng lấy thẳng từ NASA, không qua ΔT. Lỗi này chỉ
chạm tới trung khí.

### B.2 Bán kính ảnh hưởng — đo, không đoán

Sinh lại toàn bộ 2.448 trung khí với ΔT đúng (đa thức Espenak/Meeus của chính NASA)
rồi so với dataset đã ship:

```
lệch thời điểm        : trung bình 22,4 s, lớn nhất 166,8 s
trung khí ĐỔI NGÀY VN : 3 / 2448
số năm nhuận          : 75 ↔ 75   (không đổi)
THÁNG NHUẬN KHÁC NHAU : 1 năm âm
```

| Trung khí | Dataset đã ship | Giá trị đúng | Đổi ngày VN? |
|---|---|---|---|
| 1924‑06‑21 | 17:00:00Z → 22/06 | 16:59:18,8Z → 21/06 | có, **không** đổi lịch |
| **1938‑09‑23** | **17:00:00Z → 24/09** | **16:59:27,1Z → 23/09** | có, **ĐỔI LỊCH** |
| 2038‑07‑22 | 17:00:00Z → 23/07 | 16:59:41,6Z → 22/07 | có, **không** đổi lịch |

### B.3 Vì sao ca 1938 làm đổi lịch

Năm âm mở bằng tháng 11 ngày **1937‑12‑03** có **13 tháng** ⇒ là năm nhuận, nên R4
được kích hoạt và "tháng đầu tiên không chứa trung khí" trở thành yếu tố quyết định.

Trung khí 180° (Thu phân) rơi **đúng ngày mùng 1** của tháng bắt đầu 1938‑09‑24. Một
phút xê dịch là nó nhảy sang tháng trước:

| Thời điểm | Tháng thiếu trung khí ⇒ tháng nhuận |
|---|---|
| ≥ 17:00:00Z (dataset đã ship) | tháng bắt đầu **1938‑08‑25** |
| ≤ 16:59:59Z (giá trị đúng) | tháng bắt đầu **1938‑09‑24** |

Kiểm nhiễu loạn ±120 phút xác nhận điểm lật nằm đúng giữa −1 và 0 phút.

⇒ **Dataset đang ship đặt tháng nhuận năm 1938 sai một tháng.**

### B.4 Lỗi thứ hai, độc lập: làm tròn phút

Generator lưu `int(round(minutes))`. Với ΔT sai, giá trị thật 16:59:51,2 → làm tròn
thành **17:00** ⇒ đổi ngày VN. Đây là lỗi thứ hai và nó tồn tại **kể cả sau khi sửa
ΔT**: với ΔT đúng vẫn còn **3 trung khí nằm trong ±30 giây** quanh ranh giới ngày.

```
1953-06-21 16:59:53,7Z   cách  6,3 s   round → 17:00 → SAI ngày
2074-08-22 17:00:14,6Z   cách 14,6 s   round → 17:00 → đúng ngày
2038-07-22 16:59:41,6Z   cách 18,4 s   round → 17:00 → SAI ngày
```

**Cách sửa đúng là `floor` thay vì `round`.** Chứng minh: ranh giới ngày VN nằm đúng
tại một mốc phút (17:00:00Z). Phút chứa một thời điểm luôn nằm trọn về một phía của
mốc đó, nên lấy `floor` **không bao giờ** làm đổi ngày, còn `round` thì có.

### B.5 Vì sao các tầng kiểm thử hiện có không bắt được

| Tầng | Vì sao trượt |
|---|---|
| Đối chiếu HKO 2.474/2.474 | Mô hình tham chiếu dùng **chung** hàm `parse()` lỗi ⇒ cùng sai một kiểu. Hơn nữa HKO ở 120°Đ có ranh giới ngày 16:00Z, nên ba ca sát 17:00Z không nằm gần biên của Trung Quốc |
| Vector nhà nước | Đều thuộc 1984–2053, không chạm 1938 |
| Bất biến toán học | 1938 vẫn đúng song ánh, vẫn 13 tháng, vẫn có đúng một tháng nhuận — chỉ **sai chỗ**. Bất biến không phát hiện được |
| Fixture | Sinh từ chính dataset lỗi |

Đây đúng là điều tài liệu Phase 3 đã cảnh báo: tầng 2 và tầng 3 **không** chứng minh
lịch đúng. Lần này chính lỗ hổng đó hiện ra bằng một ca cụ thể.

### B.6 Cách sửa đề xuất — **chưa thực hiện, chờ quyết định**

Sửa nằm trọn trong `tools/`, **không đổi production authority**: NASA vẫn là nguồn
điểm Sóc, ERFA vẫn là nguồn trung khí, 105°Đ không đổi, API không đổi, Kotlin không
đổi.

1. Thay hàm trích ΔT hỏng bằng đa thức ΔT của NASA/Espenak (hoặc trích đúng cột).
2. Đổi `round` → `floor` khi lượng tử hoá về phút.
3. Sinh lại `.bin` + `.json`; **sha256 sẽ đổi** — cập nhật ở `LunarDatasetTest`,
   `LUNAR_DATASET_PROVENANCE.md`, `PHASE_3_IMPLEMENTATION.md`.
4. Sinh lại `precisionSensitiveTerms` từ bảng đúng.
5. Sinh lại fixture, thêm test hồi quy T5 khoá tháng nhuận 1938.
6. Chạy lại toàn bộ unit + instrumented.

Tác động dự kiến lên người dùng: **1 năm âm trong 200 năm** (1938) đổi nhãn tháng.

---

## C. Finding đã xử lý trong audit này

### A1 — provenance JSON không tái lập được ✅ đã sửa

`vn_lunar_v1.json` đã commit chứa hai khối (`precisionSensitiveTerms`, `quantisation`)
**thêm tay sau khi sinh**. Chạy lại generator sẽ xoá mất chúng. Đã đưa hai khối vào
`generate_lunar_dataset.py`; nay sinh lại cho ra **cả hai file giống hệt bản đã
commit**. Không byte nào của asset thay đổi.

### T2 — mở rộng phủ timezone ✅ đã làm

Bổ sung `Asia/Ho_Chi_Minh` (UTC+7), `Asia/Shanghai` (UTC+8), `America/Los_Angeles`
theo yêu cầu §XIII. Trước đó thiếu cả ba.

---

## D. Trạng thái T1–T5

| | Test | Trạng thái |
|---|---|---|
| **T1** | Đánh số tháng nhuận | ✅ đã có |
| **T2** | Độc lập timezone | ✅ đã mở rộng trong audit này |
| **T3** | Checksum dataset | ✅ đã có |
| **T4** | Biên 1901/2100 | ✅ đã có |
| **T5** | Ca 1938 | ⏸ **hoãn** — viết bây giờ sẽ khoá cứng giá trị đã đo được là sai |

---

## E. Điều audit này **không** làm

- Không đổi production authority.
- Không sửa dataset để test xanh — dataset **chưa** bị sửa.
- Không đụng Room, UI, navigation, dependency, permission.
- Không mở lại nghiên cứu oracle, không thu thập thêm dữ liệu online.
- Không kết luận pháp lý.
