#!/usr/bin/env python3
"""
ΔT (TT − UT) cho pipeline sinh dataset. Dev-only, ngoài app/, không vào APK.

NGUỒN — ĐÂY LÀ MỘT **MÔ HÌNH**, KHÔNG PHẢI SỐ ĐO
--------------------------------------------------
Espenak & Meeus, "Polynomial Expressions for Delta T (ΔT)", NASA/GSFC Eclipse Web
Site — https://eclipse.gsfc.nasa.gov/SEcat5/deltatpoly.html
Phụ lục của *Five Millennium Canon of Solar Eclipses: -1999 to +3000*
(NASA/TP-2006-214141).

Trang nguồn nói nguyên văn: các đa thức được tạo ra *"using the ΔT values derived
from the historical record and from direct observations ... to simplify the
evaluation of ΔT"*. Tức đây là **đa thức khớp** với số liệu lịch sử và quan trắc,
**không phải bản thân số đo**.

BA LOẠI GIÁ TRỊ — KHÔNG ĐƯỢC GỘP
---------------------------------
  A. ΔT quan trắc / lịch sử   giá trị đo được, nằm ở Table 1 & Table 2 của nguồn.
                              Dự án này KHÔNG dùng trực tiếp.
  B. ΔT theo đa thức          thứ pipeline này dùng cho TOÀN BỘ 1901-2100.
  C. ΔT ngoại suy             với 2050-2150, chính NASA ghi rõ đa thức
                              *"is derived from estimated values"*, mốc 2010
                              (66,9 s) và 2050 (93 s) đều là ngoại suy tuyến tính.

Vì vậy tuyệt đối KHÔNG mô tả kết quả hàm này là "ΔT đo được", "ΔT quan trắc" hay
"ΔT thực tế". Nó là **giá trị mô hình**.

Phạm vi dự án là 1901-2100, nên phần sau 2050 hoàn toàn nằm trong vùng ngoại suy:
Nếp Nhà **không** tuyên bố biết trước lịch sử quay của Trái Đất.

QUAN HỆ VỚI MEEUS — NÓI CHO ĐÚNG
---------------------------------
Trang nguồn ghi rõ tiêu đề *"Five Millennium Canon of Solar Eclipses [Espenak and
Meeus]"*. Vì vậy KHÔNG được nói pipeline này "không dính Meeus".

Điều đúng là: **không dòng mã nguồn nào của Meeus được sao chép hay chạy trong Nếp
Nhà.** Ta dùng một biểu thức đa thức do NASA công bố. Xem
docs/PHASE_3_MEEUS_PROVENANCE.md.

ĐỘ CHÍNH XÁC — ĐÃ KIỂM VỚI CHÍNH TRANG NGUỒN
---------------------------------------------
  ΔT(2010) = 66,92 s   NASA nêu 66,9 s
  ΔT(2050) = 93,00 s   NASA nêu 93 s
  ΔT(2026-08) = 75,46 s   khớp giá trị 75,4 s mà tài liệu nhật thực NASA nêu
Nhảy bậc lớn nhất tại mối nối: 0,0884 s (mốc 1900, ngoài phạm vi dự án).
Trong 1901-2100 nhảy lớn nhất là 0,0501 s tại mốc 2005.

VÌ SAO KHÔNG DÙNG CỘT ΔT CỦA TRANG PHASE CATALOG
------------------------------------------------
Trang phases*.html có cột ΔT nhưng in ở dạng `HHhMMm` — **độ phân giải PHÚT**. Suốt
1901-2000 nó chỉ nhận hai giá trị `00h00m` và `00h01m`. Pipeline này quyết định ngày
âm bằng những khoảng cách cỡ vài giây nên cột đó không đủ phân giải để làm đầu vào.
Nó vẫn hữu ích với vai trò **kiểm chứng thô** trong verify_lunar_dataset.py.
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
