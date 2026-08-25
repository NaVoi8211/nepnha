# Provenance của dataset lịch âm

> `app/src/main/assets/lunar/vn_lunar_v1.bin` — **19.946 byte**
> `sha256 a9b36e14e9efc455a8341e631b9402a81775ec55aa1152eec8c7816dc02f3ba0`
>
> ⚠️ Đánh giá kỹ thuật/provenance. **Không phải tư vấn pháp lý.**

## Dataset chứa gì — và cố ý KHÔNG chứa gì

| Có | Không có |
|---|---|
| 2.534 thời điểm **Sóc** | ❌ lịch đã tính sẵn |
| 2.448 thời điểm **trung khí** | ❌ ngày âm |
| | ❌ tháng nhuận |

**Vì sao tách như vậy:** quy tắc lịch nằm trong Kotlin, dữ liệu nằm trong asset. Nếu
dataset chứa sẵn lịch thì test Kotlin sẽ chỉ so lại chính thứ đã sinh ra nó — vòng
tròn. Tách ra thì kỳ vọng trong test đến được từ nguồn thứ ba.

## Ba loại nguồn — không được gộp

| Loại | Nguồn | Điều khoản | Ghi công |
|---|---|---|---|
| **DATA SOURCE** — Sóc | **NASA/GSFC Six Millennium Catalog of the Phases of the Moon** | *"Permission is freely granted to reproduce this data when accompanied by an acknowledgment"* + chính sách bản quyền NASA | **Bắt buộc:** `Moon Phase Predictions by Fred Espenak, NASA/GSFC` |
| **DATA SOURCE** — trung khí | Sinh bằng **ERFA** `eraEpv00` + `eraEqec06` + `eraNut06a`, chạy ở **máy dev** | BSD-3-Clause (NumFOCUS; phái sinh **có phép** của IAU SOFA) | *"Dữ liệu tiết khí tạo bằng ERFA, thư viện phái sinh từ IAU SOFA"* |
| **DATA SOURCE** — ΔT | Cột ΔT của chính catalog NASA | như trên | như trên |
| **ALGORITHM SOURCE** | Aslaksen, *The Mathematics of the Chinese Calendar* (NUS); *Explanatory Supplement to the Astronomical Almanac*; kinh tuyến 105°Đ theo **QĐ 121-CP điều 1** | — | Trích dẫn |
| **VALIDATION SOURCE** | HKO (data.gov.hk) · văn bản nhà nước 6150/TB-BLĐTBXH, 9441/TB-BNV | HKO: thương mại OK + attribution | Không vào APK |

## 🔑 `eraMoon98` KHÔNG nằm trên đường sản xuất

**FACT.** Quét toàn bộ 251 file ERFA: `moon98.c` là **file duy nhất** nhắc tới Meeus
và là ephemeris Mặt Trăng duy nhất trong ERFA.

**FACT.** Pipeline sinh dataset **không gọi `moon98`**. Điểm Sóc lấy **thẳng từ dữ
liệu NASA**. `moon98` chỉ tồn tại trong `tools/benchmark_erfa_astronomy/` để đối
chiếu ở máy dev.

⇒ Câu hỏi chính sách về `moon98` **vẫn UNRESOLVED**, nhưng **không còn nằm trên đường
sản xuất**.

**LIMITATION còn lại:** dữ liệu NASA *"based on Meeus"*. Ta dựa vào **permission trực
tiếp của chính bên công bố**. Phần còn lại là **LEGAL-UNKNOWN** ở lớp thuật toán.

## Định dạng

Nhị phân, big-endian, chỉ số nguyên — không dấu phẩy động, không phụ thuộc endian máy.

```
0   4B   "NNLD"      10  u32  newMoonCount
4   u16  version     14  u32  principalTermCount
6   u16  1901        18  u32[] Sóc — phút UTC từ 1890-01-01T00:00Z
8   u16  2100        ..  u32[] trung khí — cùng đơn vị, 12 mốc/chu kỳ từ 0°
```

**Đệm:** dataset chứa mốc vượt ngoài 1901–2100 (30 tuần trăng và 2 chu kỳ trung khí
mỗi đầu). Cần vì tháng 11 âm neo vào Đông chí. **Đệm là dữ liệu tính toán nội bộ,
KHÔNG mở rộng phạm vi công bố.**

**Lượng tử hoá:** lưu theo **phút** ⇒ sai số ±30 giây. Khi đối chiếu với nguồn cũng
làm tròn phút (HKO), chênh lệch quan sát được có thể tới 60 giây mà không phải sai số
thật.

## Sáu tháng sát ranh giới ngày

Chọn **NASA** cho tất cả, theo chính sách NASA-first. Giá trị ERFA **được giữ lại**
làm bằng chứng, không bị xoá.

| Ngày (UT) | NASA | ERFA | Cách 17:00 UTC | Ngày âm NASA | Ngày âm ERFA |
|---|---|---|---|---|---|
| 1944-06-20 | 17:00 | 16:59:52 | 7,9 s | 30/4**N** | 1/5 |
| 1967-07-07 | 17:00 | 16:59:41 | 18,6 s | 30/5 | 1/6 |
| 2054-05-07 | 17:00 | 17:00:06 | 6,5 s | *(cùng)* | *(cùng)* |
| 2072-12-09 | 16:59 | 16:59:09 | 50,3 s | *(cùng)* | *(cùng)* |
| 2077-11-15 | 17:00 | 16:59:25 | 34,7 s | 30/9 | 1/10 |
| 2085-10-18 | 17:00 | 16:59:22 | 37,5 s | 30/8 | 1/9 |

**Chọn NASA thay ERFA làm đổi đúng 4 tháng âm** trong 200 năm — 120/72.319 ngày =
**0,166 %**. Đây là **astronomical uncertainty**, **không phải engine bug**.

Cờ `PRECISION_SENSITIVE` chỉ nằm trong file provenance JSON. **Không** vào binary,
**không** lộ ra API, **không** được dùng để đổi kết quả.

## Một trung khí sát ranh giới

7/2.400 trung khí nằm trong ±2 phút quanh nửa đêm giờ VN. Chỉ **một** ca rơi đúng
ranh giới tháng và do đó về lý thuyết có thể ảnh hưởng quy tắc tháng nhuận:

**1938-09-23 17:00Z → ngày VN 1938-09-24, đúng mùng 1.** Đánh dấu
`PRECISION_SENSITIVE_TERM`. Sáu ca còn lại nằm giữa tháng, không ảnh hưởng.

## Tái sinh và kiểm chứng

```bash
python3 tools/generate_lunar_dataset.py      # sinh lại, phải ra cùng sha256
python3 tools/verify_lunar_dataset.py        # kiểm độc lập bằng parser riêng
```

`verify_lunar_dataset.py` dùng **parser viết riêng**, không dùng lại code sinh, và
đối chiếu ngược với catalog NASA cùng bảng tiết khí HKO.

Test Kotlin `LunarDatasetTest` **khoá sha256**: đổi dataset mà quên cập nhật ⇒ test đỏ.
