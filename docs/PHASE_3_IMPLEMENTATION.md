# Phase 3 — Vietnamese Lunar Calendar Engine

> ⚠️ Dataset đã được sửa sau audit cuối — xem
> **[PHASE_3_DATASET_CORRECTION.md](PHASE_3_DATASET_CORRECTION.md)**.
>
> Domain engine + dataset + tests. **Không nối UI, không đụng Room, không thêm
> dependency.**

## Đã làm

| Thành phần | Nội dung |
|---|---|
| **Dataset** | `assets/lunar/vn_lunar_v1.bin` — 19.946 B, 2.534 điểm Sóc + 2.448 trung khí |
| **Engine** | `core/lunar/` — 4 file Kotlin thuần, **không** import `android.*` |
| **Tools** | `generate_lunar_dataset.py` · `verify_lunar_dataset.py` · `generate_test_fixture.py` |
| **Test** | **38 unit test lịch âm** + 4 instrumented test |
| **Dependency mới** | **0** |
| **Thay đổi Room/UI** | **0** |

## Kiến trúc

```
[ MÁY DEV — không vào APK ]
NASA catalog ─┐
              ├─► generate_lunar_dataset.py ─► vn_lunar_v1.bin (+ .json provenance)
ERFA (BSD-3) ─┘                                        │
HKO, văn bản nhà nước ─► kiểm chứng độc lập           │
                                                       ▼
[ APK — Kotlin thuần ]
LunarDataset.parse(bytes) ─► VietnameseLunarCalendar ─► LunarResult<LunarDate>
```

**Không** C · **không** NDK · **không** JNI · **không** native lib · **không** mạng ·
**không** tính toán thiên văn lúc chạy.

## Quy tắc đã hiện thực

R1 ngày chứa Sóc là mùng 1 (lấy trọn ngày) · R2 mỗi Sóc mở một tháng · R3 Đông chí
nằm trong tháng 11 · R4 13 tháng giữa hai tháng 11 ⇒ năm nhuận, tháng nhuận là tháng
**đầu tiên không chứa trung khí** và mang **số của tháng liền trước** · R5 trung khí ở
bội số 30° · R6 quy chiếu **105°Đ (UTC+7)**.

Provenance R1–R5: Aslaksen (NUS) + *Explanatory Supplement* — **độc lập với Hồ Ngọc
Đức**. R6: **QĐ 121-CP điều 1**.

## Bốn tầng kiểm thử

| Tầng | Nội dung | Chứng minh được gì |
|---|---|---|
| **1 — vector ngoài** | 7 vector văn bản nhà nước · Tết 1985 · Tết 2007/2030/2053 · tháng nhuận 1984/1987 theo Ban Lịch NN · can chi | ✅ **engine ĐÚNG** |
| **2 — bất biến** | 9 test trên **toàn bộ 73.049 ngày**: song ánh, round-trip hai chiều, độ dài tháng 29/30, tháng liên tục, nhuận nhất quán | Engine **nhất quán**, không chứng minh đúng |
| **3 — cross-implementation** | 1.975 vector fixture sinh từ chính dataset | Bắt lỗi **chuyển ngữ sang Kotlin** |
| **4 — thiết bị thật** | Nạp asset qua `AssetManager`, vector nhà nước, hiệu năng, timezone | Đóng gói APK đúng |

Chuỗi bằng chứng của tầng 3: **HKO (2.474/2.474 tháng, nguồn ngoài)** → mô hình tham
chiếu → fixture → Kotlin. Bằng chứng độc lập nằm ở **đầu** chuỗi.

## Kết quả

```
:app:assembleDebug              BUILD SUCCESSFUL, 0 lỗi 0 cảnh báo
:app:testDebugUnitTest          59/59 pass  (38 lịch âm + 21 cũ)
core/lunar import android.*     0
APK chứa assets/lunar/          ✓ 19.946 B
```

## Giới hạn đã công bố

| # | Giới hạn |
|---|---|
| **L1** | `OfficialVietnam` dùng **UTC+7 hồi tố toàn dải**. **Không** tuyên bố phản ánh đúng tập quán lịch đương thời mọi thời kỳ, đặc biệt **miền Bắc 1954–1967** |
| **L2** | `HistoricalRegion.NORTH/SOUTH` **chưa có** — roadmap, không tạo API hứa suông |
| **L3** | 6 tháng sát ranh giới ngày (0,24 %) chọn theo NASA, **chưa có oracle Việt Nam phân xử** |
| **L4** | Tháng nhuận và độ dài 29/30 mới ở mức **engineering cross-check** với HKO, chưa có oracle Việt Nam đầy đủ |
| **L5** | Câu hỏi chính sách `moon98`/Meeus **vẫn UNRESOLVED** — nhưng **đã loại khỏi đường sản xuất** |

## Phase sau

Nối engine vào Home và màn Lịch; ngày giỗ dùng `NonexistentLunarDate.lastValidDay`
với `MemorialRule.missingDayPolicy`. **Engine không bao giờ tự lùi 30 → 29.**
