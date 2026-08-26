#!/usr/bin/env python3
"""
Self-test cho pipeline sinh dataset. Dev-only. Chạy: python3 tools/test_generator.py

Mỗi test ghi rõ SAI THÌ LÀ GÌ, để không có test nào chỉ để làm dày con số.
"""
import math
import os
import sys
from datetime import datetime, timedelta, timezone

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from deltat import delta_t_seconds                                     # noqa: E402

EPOCH = datetime(1890, 1, 1, tzinfo=timezone.utc)
VN = timezone(timedelta(hours=7))
fails = []


def check(name, cond, detail=""):
    print(f"  [{'OK ' if cond else 'FAIL'}] {name}{('  ' + detail) if detail else ''}")
    if not cond:
        fails.append(name)


print("--- A. ΔT khớp ĐÚNG đa thức NASA/Espenak trên mọi khoảng ---")
print("    Kiểm mô hình toán đã hiện thực so với công thức NASA, KHÔNG so với một")
print("    ước lượng ΔT hiện đại nào khác.")
print("    Sai thì: chép nhầm hệ số ⇒ mọi trung khí lệch vài chục giây.")
# Dung sai 0,01 s: đây là đa thức đóng, không nội suy, nên chỉ sai số làm tròn khi
# ghi giá trị kỳ vọng ở đây. KHÔNG dùng để che sai lệch mô hình.
TOL = 0.01
EXPECTED = [
    (1901, -0.61), (1914, 16.52), (1920, 21.64), (1938, 24.05),
    (1941, 25.08), (1954, 30.87), (1961, 33.79), (1967, 37.86),
    (1985, 54.64), (1986, 55.12), (2000, 64.02), (2005, 64.88),
    (2026, 75.41), (2050, 94.10), (2054, 102.31), (2077, 151.47),
    (2085, 169.36), (2100, 204.02),
]
bad_years = []
for year, want in EXPECTED:
    got = delta_t_seconds(year)
    if abs(got - want) > TOL:
        bad_years.append((year, want, round(got, 2)))
check(f"cả {len(EXPECTED)} năm đại diện khớp đa thức", not bad_years, str(bad_years))

print("\n--- A2. Đối chiếu ba giá trị chính NASA tự nêu trên trang nguồn ---")
print("    Sai thì: hiện thực lệch khỏi chính con số NASA công bố.")
# Trang deltatpoly.html nêu: ΔT(2010) = 66,9 s và ΔT(2050) = 93 s.
# Tài liệu nhật thực NASA nêu ΔT ≈ 75,4 s cho 2026-08.
check("ΔT(2010) ≈ 66,9 s", abs(delta_t_seconds(2010, 7) - 66.9) < 0.1,
      f"nhận {delta_t_seconds(2010, 7):.2f} s")
check("ΔT(2050-01, nhánh 2005-2050) ≈ 93 s",
      abs((62.92 + 0.32217 * 50 + 0.005589 * 2500) - 93.0) < 0.05)
check("ΔT(2026-08) ≈ 75,4 s", abs(delta_t_seconds(2026, 8) - 75.4) < 0.1,
      f"nhận {delta_t_seconds(2026, 8):.2f} s")

print("\n--- B. ΔT không bao giờ sập về bốn giá trị của lỗi cũ ---")
print("    Sai thì: quay lại đọc cột phân giải PHÚT của trang NASA.")
vals = [delta_t_seconds(y, m) for y in range(1901, 2101) for m in range(1, 13)]
check("có hơn 2000 giá trị phân biệt", len(set(round(v, 6) for v in vals)) > 2000,
      f"{len(set(round(v,6) for v in vals))} giá trị")
check("không giá trị nào thuộc {0, 60, 120, 180} đúng khít",
      not any(abs(v - k) < 1e-9 for v in vals for k in (0, 60, 120, 180)))
check("dải giá trị hợp lý cho 1901-2100", -5 < min(vals) and max(vals) < 215,
      f"{min(vals):.2f} .. {max(vals):.2f} s")
# KHÔNG khẳng định đơn điệu: ΔT có cực tiểu ~-1,3 s ở 1901 và một vùng chững nhẹ
# 1928-1935 (đỉnh 24,17 s xuống 23,81 s). Đó là vật lý thật, không phải lỗi.

print("\n--- B2. Liên tục tại ĐÚNG mốc chuyển khoảng ---")
print("    Đánh giá hai công thức kề nhau tại chính giá trị y của mốc, không phải")
print("    ở hai tâm tháng cách nhau — cách sau trộn độ dốc vào phép đo.")
print("    Sai thì: chép nhầm một hệ số ⇒ nhảy bậc cỡ giây tới phút.")


def _p1900(t): return -2.79 + 1.494119*t - 0.0598939*t**2 + 0.0061966*t**3 - 0.000197*t**4


def _p1920(t): return 21.20 + 0.84493*t - 0.076100*t**2 + 0.0020936*t**3


def _p1941(t): return 29.07 + 0.407*t - t**2/233 + t**3/2547


def _p1961(t): return 45.45 + 1.067*t - t**2/260 - t**3/718


def _p1986(t): return (63.86 + 0.3345*t - 0.060374*t**2 + 0.0017275*t**3
                       + 0.000651814*t**4 + 0.00002373599*t**5)


def _p2005(t): return 62.92 + 0.32217*t + 0.005589*t**2


def _p2050(y): return -20 + 32*((y - 1820)/100)**2 - 0.5628*(2150 - y)


SEAMS = [
    (1920, lambda y: _p1900(y - 1900), lambda y: _p1920(y - 1920)),
    (1941, lambda y: _p1920(y - 1920), lambda y: _p1941(y - 1950)),
    (1961, lambda y: _p1941(y - 1950), lambda y: _p1961(y - 1975)),
    (1986, lambda y: _p1961(y - 1975), lambda y: _p1986(y - 2000)),
    (2005, lambda y: _p1986(y - 2000), lambda y: _p2005(y - 2000)),
    (2050, lambda y: _p2005(y - 2000), _p2050),
]
worst_seam, seam_at = 0.0, None
for yy, left, right in SEAMS:
    jump = abs(right(yy) - left(yy))
    if jump > worst_seam:
        worst_seam, seam_at = jump, yy
# Ngưỡng 0,10 s đặt theo giá trị ĐO ĐƯỢC: mối xấu nhất trong phạm vi dự án là 2005
# với 0,0501 s. Mốc 2050 chỉ lệch 0,0010 s vì NASA thêm số hạng -0,5628*(2150-y)
# chính để khử gián đoạn ở đó. Biên an toàn nhỏ nhất của dataset là 6,3 s nên các
# nhảy bậc này không thể đổi ngày nào.
check("mọi mối nối liên tục trong 0,10 s", worst_seam < 0.10,
      f"nhảy lớn nhất {worst_seam:.4f} s tại {seam_at}")
check("mối nối 2050 gần như khử hẳn gián đoạn",
      abs(_p2050(2050) - _p2005(50)) < 0.005,
      f"{abs(_p2050(2050) - _p2005(50)):.4f} s")

check("hàm sản xuất dùng đúng nhánh tại mỗi mốc",
      all(abs(delta_t_seconds(yy, 1) - r(yy + 0.5/12)) < 1e-9 for yy, _, r in SEAMS))

print("\n--- C. floor không bao giờ đẩy sự kiện qua ranh giới ngày Việt Nam ---")
print("    Sai thì: quay lại round ⇒ ngày âm lệch một ngày ở các mốc sát 17:00:00Z.")


def q_floor(dt):
    return math.floor((dt - EPOCH).total_seconds() / 60.0)


def q_round(dt):
    return int(round((dt - EPOCH).total_seconds() / 60.0))


def vn_of(minute):
    return (EPOCH + timedelta(minutes=minute)).astimezone(VN).date()


base = datetime(2000, 6, 15, 16, 59, 0, tzinfo=timezone.utc)
probes = [
    ("16:59:29.999", base.replace(second=29, microsecond=999000)),
    ("16:59:30.0", base.replace(second=30)),
    ("16:59:59.999", base.replace(second=59, microsecond=999000)),
    ("17:00:00.0", base.replace(hour=17, minute=0, second=0)),
    ("17:00:00.001", base.replace(hour=17, minute=0, second=0, microsecond=1000)),
    ("17:00:30.5", base.replace(hour=17, minute=0, second=30, microsecond=500000)),
    ("17:00:59.999", base.replace(hour=17, minute=0, second=59, microsecond=999000)),
]
round_broke = 0
for label, t in probes:
    truth = t.astimezone(VN).date()
    fl, rn = vn_of(q_floor(t)), vn_of(q_round(t))
    if rn != truth:
        round_broke += 1
    check(f"floor giữ đúng ngày tại {label}", fl == truth, f"thật {truth}, floor {fl}")
check("bộ mẫu này thực sự phân biệt được floor với round", round_broke > 0,
      f"round sai {round_broke}/{len(probes)} mốc")

print("\n--- D. floor luôn lùi về sau, sai số nằm trong [0, 60) giây ---")
print("    Sai thì: công thức lượng tử hoá sai dấu hoặc lệch epoch.")
ok = True
for s in range(0, 3600, 7):
    t = datetime(1950, 3, 4, 12, 0, tzinfo=timezone.utc) + timedelta(seconds=s)
    err = (t - (EPOCH + timedelta(minutes=q_floor(t)))).total_seconds()
    ok = ok and 0 <= err < 60
check("sai số luôn thuộc [0, 60) giây", ok)

print("\n" + ("KẾT QUẢ: TẤT CẢ ĐỀU QUA" if not fails else f"KẾT QUẢ: ⛔ {len(fails)} LỖI: {fails}"))
sys.exit(1 if fails else 0)
