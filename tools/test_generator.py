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


print("--- A. ΔT khớp giá trị công bố của nguồn ---")
print("    Sai thì: hàm ΔT bị đổi/chép sai hệ số ⇒ mọi trung khí lệch vài chục giây.")
# Dung sai lấy từ chính nguồn: đây là đa thức KHỚP, không phải nội suy bảng, nên
# sai lệch so với giá trị công bố chỉ đến từ làm tròn khi công bố. ±1,5 s là rộng rãi.
TOL = 1.5
for year, want in ((1938, 24.05), (2026, 75.41), (2100, 204.02)):
    got = delta_t_seconds(year)
    check(f"ΔT({year}) ≈ {want} s", abs(got - want) <= TOL, f"nhận {got:.2f} s")

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
print("\n--- B2. Đa thức liên tục tại các mối nối ---")
print("    Sai thì: chép nhầm một hệ số ⇒ nhảy bậc lớn ngay tại mối nối.")
worst_seam, seam_at = 0.0, None
for seam in (1920, 1941, 1961, 1986, 2005, 2050):
    lo = delta_t_seconds(seam - 1, 12)
    hi = delta_t_seconds(seam, 1)
    if abs(hi - lo) > worst_seam:
        worst_seam, seam_at = abs(hi - lo), seam
# Ngưỡng 0,25 s đặt theo giá trị ĐO ĐƯỢC: mối nối xấu nhất là 2050 với 0,1205 s,
# do đoạn 2050-2150 dùng dạng hàm khác. Chép nhầm một hệ số sẽ tạo nhảy bậc cỡ giây
# tới phút, nên ngưỡng này vẫn bắt được lỗi thật. Biên an toàn nhỏ nhất của dataset
# là 6,3 s (trung khí 1953) nên 0,12 s không thể đổi ngày nào.
check("mọi mối nối liên tục trong 0,25 s", worst_seam < 0.25,
      f"nhảy lớn nhất {worst_seam:.4f} s tại {seam_at}")

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
    ("16:59:29.9", base.replace(second=29, microsecond=900000)),
    ("16:59:30.0", base.replace(second=30)),
    ("16:59:59.9", base.replace(second=59, microsecond=900000)),
    ("17:00:00.0", base.replace(hour=17, minute=0, second=0)),
    ("17:00:00.1", base.replace(hour=17, minute=0, second=0, microsecond=100000)),
    ("17:00:30.5", base.replace(hour=17, minute=0, second=30, microsecond=500000)),
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
