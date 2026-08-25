#!/usr/bin/env python3
"""
Quét TOÀN BỘ điểm Sóc 1901-2100 bằng ERFA, đo khoảng cách tới ranh giới ngày Việt Nam
(17:00:00 UTC) và đối chiếu ngày âm suy ra với NASA.

Câu hỏi cần trả lời: trong 200 năm, có bao nhiêu ngày mà sai số phương pháp đủ để
làm ĐỔI NGÀY ÂM. Không phải "lệch bao nhiêu giây" mà "lệch đó có đổi ngày không".

NASA là oracle độc lập, không phải input. Không hiệu chỉnh ERFA cho khớp NASA.
"""
import subprocess, sys
sys.path.insert(0, __file__.rsplit('/', 1)[0])
from compare_with_nasa import fetch, parse, jd_tt_guess, vn_margin_seconds, vn_date
from datetime import datetime, timedelta, timezone

BENCH = sys.argv[1] if len(sys.argv) > 1 else "/tmp/erfabuild/erfa_bench"
# Sai số tổng hợp đáng ngờ: moon98 worst-case 18.3" (~36 giây) + NASA làm tròn phút (±30 giây)
RISK_THRESHOLD_S = 70


def jd_to_datetime(jd_tt, delta_t):
    jd_ut = jd_tt - delta_t / 86400.0
    z = jd_ut + 0.5
    day = int(z); frac = z - day
    if day >= 2299161:
        alpha = int((day - 1867216.25) / 36524.25)
        day += 1 + alpha - alpha // 4
    b = day + 1524; c = int((b - 122.1) / 365.25)
    d = int(365.25 * c); e = int((b - d) / 30.6001)
    dd = b - d - int(30.6001 * e)
    mm = e - 1 if e < 14 else e - 13
    yy = c - 4716 if mm > 2 else c - 4715
    secs = frac * 86400.0
    return datetime(yy, mm, dd, tzinfo=timezone.utc) + timedelta(seconds=secs)


def main():
    moons, dts = [], {}
    for start in (1901, 2001):
        m, d = parse(fetch(start)); moons += m; dts.update(d)
    print(f"NASA: {len(moons)} điểm Sóc 1901-2100")

    payload = "".join(f"{dts.get(nm.year,69)} {jd_tt_guess(nm, dts.get(nm.year,69)):.9f}\n"
                      for nm in moons)
    out = subprocess.run([BENCH, "batch"], input=payload, capture_output=True,
                         text=True, check=True).stdout.strip().splitlines()
    print(f"ERFA: {len(out)} nghiệm tính được")

    risky, disagree, worst = [], [], 0.0
    for nm, line in zip(moons, out):
        jd, dt = (float(x) for x in line.split())
        est = jd_to_datetime(jd, dt)
        diff = abs((est - nm).total_seconds())
        worst = max(worst, diff)
        margin = vn_margin_seconds(est)
        if margin < RISK_THRESHOLD_S:
            risky.append((nm, est, margin))
        if vn_date(est) != vn_date(nm):
            disagree.append((nm, est, margin))

    print(f"\n|lệch| lớn nhất ERFA vs NASA: {worst:.1f} giây")
    print(f"\nĐiểm Sóc có margin < {RISK_THRESHOLD_S}s tới ranh giới ngày VN: {len(risky)}"
          f"  ({len(risky)/len(moons)*100:.2f}% của {len(moons)})")
    for nm, est, m in risky:
        print(f"   NASA {nm:%Y-%m-%d %H:%M}   ERFA {est:%Y-%m-%d %H:%M:%S}   margin {m:6.1f}s")
    print(f"\nSố ca ERFA và NASA cho NGÀY ÂM KHÁC NHAU: {len(disagree)}")
    for nm, est, m in disagree:
        print(f"   NASA {nm:%Y-%m-%d %H:%M} -> {vn_date(nm)}   "
              f"ERFA {est:%Y-%m-%d %H:%M:%S} -> {vn_date(est)}   margin {m:.1f}s")


if __name__ == "__main__":
    main()
