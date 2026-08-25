# reference_model — mô hình đối chiếu, KHÔNG PHẢI ENGINE CỦA APP

Python, nằm ngoài `app/`, **không bao giờ** được build vào APK. Đây **không phải**
`VietnameseLunarCalendar` của Phase 3 — engine production sẽ viết bằng Kotlin thuần.

Tồn tại vì Gate O1–O10 của Phase 3A.5 đòi so sánh "kết quả engine" với các nguồn, mà
lúc đó chưa có engine nào để so.

## Nội dung

| File | Việc |
|---|---|
| `vn_lunar_reference.py` | Quy tắc lịch R1–R6. `utc_offset_hours` = 7 (VN) hoặc 8 (TQ) |
| `build_inputs.py` | Nạp Sóc từ NASA, tính lại bằng ERFA, sinh tiết khí bằng ERFA |
| `crosscheck_hko_structure.py` | Chạy @120°Đ, so với bảng lịch Trung Quốc của HKO |
| `validate_vietnam.py` | Vector V1, ca VN≠TQ, độ nhạy nguồn Sóc, nhuận, 29/30, biên, round-trip |

## Chạy

```bash
# cần liberfa.a + erfa_bench — xem tools/benchmark_erfa_astronomy/README.md
python3 tools/reference_model/crosscheck_hko_structure.py              # 40 năm mẫu
python3 tools/reference_model/crosscheck_hko_structure.py --range 1901-2100
python3 tools/reference_model/validate_vietnam.py
```

## Provenance quy tắc

R1–R5 từ Aslaksen, *The Mathematics of the Chinese Calendar* (NUS) và *Explanatory
Supplement to the Astronomical Almanac* — **độc lập với Hồ Ngọc Đức**.
R6 (kinh tuyến 105°Đ) từ **Quyết định 121-CP điều 1**.

## Nguồn dữ liệu

- **NASA/GSFC** — *Moon Phase Predictions by Fred Espenak, NASA/GSFC*
- **ERFA** — BSD-3-Clause, phái sinh có phép từ IAU SOFA (dev-only)
- **HKO** qua data.gov.hk — cho phép thương mại kèm attribution (chỉ để đối chiếu)

## Hai bug mà kiểm chứng cấu trúc đã bắt được

1. Đánh số tháng nhuận lệch 1 — tháng nhuận mang số của tháng **liền trước**.
2. Thiếu tháng ở hai đầu dải — cần **dữ liệu đệm ngoài phạm vi bảo đảm**.

Engine Kotlin ở Phase 3 phải có test riêng cho cả hai.
Xem [`docs/LUNAR_ONLINE_ANOMALIES.md`](../../docs/LUNAR_ONLINE_ANOMALIES.md).
