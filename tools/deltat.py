#!/usr/bin/env python3
"""
ΔT (TT − UT) cho pipeline sinh dataset. Dev-only, ngoài app/, không vào APK.

NGUỒN
-----
Espenak & Meeus, "Polynomial Expressions for Delta T", NASA/GSFC Eclipse Web Site
(eclipse.gsfc.nasa.gov/SEcat5/deltatpoly.html), phụ lục của *Five Millennium Canon
of Solar Eclipses: -1999 to +3000* (NASA/TP-2006-214141).

Đây là **cùng một nguồn NASA/GSFC** đã dùng cho điểm Sóc, nên chính sách NASA-first
không thay đổi.

VÌ SAO KHÔNG DÙNG CỘT ΔT CỦA TRANG PHASE CATALOG
------------------------------------------------
Trang phases*.html có cột ΔT nhưng in ở dạng `HHhMMm` — **độ phân giải PHÚT**. Suốt
1901–2000 nó chỉ nhận hai giá trị `00h00m` và `00h01m`. Pipeline này quyết định ngày
âm bằng những khoảng cách cỡ vài giây, nên cột đó không đủ phân giải để dùng làm đầu
vào. Nó vẫn hữu ích với vai trò **kiểm chứng thô** trong verify_lunar_dataset.py —
xem hàm cross-check ở đó.

ĐỘ CHÍNH XÁC ĐÃ BIẾT — GIỚI HẠN CÔNG BỐ
----------------------------------------
Đa thức đoạn 2005–2050 là **ngoại suy lập năm 2006**. Tốc độ quay của Trái Đất từ đó
nhanh hơn dự báo, nên với thập niên 2020 đa thức cho ~75 s trong khi giá trị quan
trắc là ~69 s — lệch khoảng 6 giây. Đây là giới hạn của chính nguồn, không phải lỗi
hiện thực. Ảnh hưởng lên lịch đã được ĐO, xem docs/PHASE_3_DATASET_CORRECTION.md §D.
"""

def delta_t_seconds(year: int, month: int = 7) -> float:
    """ΔT = TT − UT, tính bằng giây, theo đa thức Espenak & Meeus (NASA/GSFC).

    Tham số `year`/`month` là ngày dương lịch của sự kiện. Biến của đa thức là
    y = year + (month − 0.5) / 12, đúng như nguồn định nghĩa.
    """
    y = year + (month - 0.5) / 12.0

    if y < 1900:
        t = (y - 1860) / 1.0
        return (7.62 + 0.5737 * t - 0.251754 * t**2 + 0.01680668 * t**3
                - 0.0004473624 * t**4 + t**5 / 233174.0)
    if y < 1920:
        t = y - 1900
        return (-2.79 + 1.494119 * t - 0.0598939 * t**2 + 0.0061966 * t**3
                - 0.000197 * t**4)
    if y < 1941:
        t = y - 1920
        return 21.20 + 0.84493 * t - 0.076100 * t**2 + 0.0020936 * t**3
    if y < 1961:
        t = y - 1950
        return 29.07 + 0.407 * t - t**2 / 233.0 + t**3 / 2547.0
    if y < 1986:
        t = y - 1975
        return 45.45 + 1.067 * t - t**2 / 260.0 - t**3 / 718.0
    if y < 2005:
        t = y - 2000
        return (63.86 + 0.3345 * t - 0.060374 * t**2 + 0.0017275 * t**3
                + 0.000651814 * t**4 + 0.00002373599 * t**5)
    if y < 2050:
        t = y - 2000
        return 62.92 + 0.32217 * t + 0.005589 * t**2
    if y < 2150:
        return -20 + 32 * ((y - 1820) / 100.0)**2 - 0.5628 * (2150 - y)
    t = (y - 1820) / 100.0
    return -20 + 32 * t**2


if __name__ == "__main__":
    for y in (1901, 1914, 1920, 1938, 1944, 1954, 1967, 1985,
              2000, 2026, 2054, 2077, 2085, 2100):
        print(f"{y}  ΔT = {delta_t_seconds(y):8.2f} s")
