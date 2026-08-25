#!/usr/bin/env python3
"""
Nếp Nhà — so sánh điểm Sóc tính bằng ERFA với NASA Six Millennium Catalog.

KHÔNG phải code của app. Chạy ngoài app/, không vào APK.

NASA đóng vai ORACLE ĐỘC LẬP, không phải input. Tuyệt đối không hiệu chỉnh ERFA
cho khớp NASA — chỉ đo chênh lệch.

Ranh giới ngày âm ở Việt Nam (từ 1968) là 00:00 UTC+7 = 17:00:00 UTC. Điều thực sự
quan trọng không phải "lệch bao nhiêu giây" mà là "lệch đó có đủ để đổi NGÀY không".

Nguồn dữ liệu:
  NASA/GSFC — Moon Phase Predictions by Fred Espenak, NASA/GSFC
  ERFA      — BSD-3-Clause, phái sinh có phép từ IAU SOFA
"""
import html
import re
import subprocess
import sys
import urllib.request
from datetime import datetime, timedelta, timezone

BENCH = sys.argv[1] if len(sys.argv) > 1 else "/tmp/erfabuild/erfa_bench"
MONTHS = {m: i + 1 for i, m in enumerate(
    "Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec".split())}
VN_BOUNDARY_UTC_HOUR = 17          # 00:00 giờ VN = 17:00 UTC


def fetch(year_start):
    url = f"https://eclipse.gsfc.nasa.gov/phase/phases{year_start}.html"
    with urllib.request.urlopen(url, timeout=90) as r:
        return html.unescape(re.sub(r"<[^>]+>", "", r.read().decode("utf-8", "replace")))


def parse(text):
    """Trả về (new_moons, delta_t_by_year). Đọc theo VỊ TRÍ CỘT."""
    lines = text.splitlines()
    h = next(i for i, l in enumerate(lines) if "New Moon" in l and "First Quarter" in l)
    lo = lines[h].index("New Moon") - 4
    hi = lines[h].index("First Quarter") - 2
    dt_col = lines[h].index("T", lines[h].index("New Moon")) if False else None

    moons, dts, year = [], {}, None
    for l in lines[h:]:
        my = re.match(r"\s*(\d{4})\s", l)
        if my:
            year = int(my.group(1))
            mdt = re.search(r"(\d{2})h(\d{2})m", l)
            if mdt:
                dts[year] = int(mdt.group(1)) * 3600 + int(mdt.group(2)) * 60
        if year is None or len(l) < lo:
            continue
        m = re.search(r"([A-Z][a-z]{2})\s+(\d{1,2})\s+(\d{2}):(\d{2})", l[lo:hi])
        if m:
            moons.append(datetime(year, MONTHS[m.group(1)], int(m.group(2)),
                                  int(m.group(3)), int(m.group(4)), tzinfo=timezone.utc))
    return moons, dts


def jd_tt_guess(dt_utc, delta_t):
    """JD(TT) xấp xỉ từ thời điểm UT của NASA."""
    a = (14 - dt_utc.month) // 12
    y = dt_utc.year + 4800 - a
    m = dt_utc.month + 12 * a - 3
    jdn = (dt_utc.day + (153 * m + 2) // 5 + 365 * y + y // 4 - y // 100 + y // 400 - 32045)
    frac = (dt_utc.hour - 12) / 24 + dt_utc.minute / 1440 + dt_utc.second / 86400
    return jdn + frac + delta_t / 86400.0


def run_erfa(delta_t, guess):
    out = subprocess.run([BENCH, "newmoon", str(delta_t), f"{guess:.9f}"],
                         capture_output=True, text=True, check=True).stdout
    m = re.search(r"UT=(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})\.(\d{3})", out)
    g = [int(x) for x in m.groups()]
    return datetime(g[0], g[1], g[2], g[3], g[4], g[5], g[6] * 1000, tzinfo=timezone.utc)


def vn_margin_seconds(dt_utc):
    """Khoảng cách tới ranh giới ngày VN gần nhất (17:00:00 UTC), tính bằng giây."""
    b = dt_utc.replace(hour=VN_BOUNDARY_UTC_HOUR, minute=0, second=0, microsecond=0)
    return min(abs((dt_utc - b).total_seconds()),
               abs((dt_utc - (b - timedelta(days=1))).total_seconds()),
               abs((dt_utc - (b + timedelta(days=1))).total_seconds()))


def vn_date(dt_utc):
    return (dt_utc + timedelta(hours=7)).date()


def classify(diff_s, margin_s, same_day):
    if not same_day:
        return "FAIL"
    if margin_s < 120:
        return "BORDERLINE"
    return "PASS"


def main():
    years = [1944, 1950, 1967, 1985, 1987, 2007, 2026, 2030, 2054, 2077, 2085, 2099]
    text = {}
    for start in (1901, 2001):
        text[start] = fetch(start)
    moons, dts = [], {}
    for start in (1901, 2001):
        m, d = parse(text[start])
        moons += m
        dts.update(d)

    print(f"{'NASA (UT)':<20} {'ERFA (UT)':<24} {'lệch':>9} {'margin VN':>11} "
          f"{'ngày VN':>12} {'':>10}")
    print("-" * 92)
    stats = {"PASS": 0, "BORDERLINE": 0, "FAIL": 0}
    worst = 0.0
    for nm in moons:
        if nm.year not in years:
            continue
        dt = dts.get(nm.year, 69)
        est = run_erfa(dt, jd_tt_guess(nm, dt))
        diff = (est - nm).total_seconds()
        worst = max(worst, abs(diff))
        margin = vn_margin_seconds(est)
        same = vn_date(est) == vn_date(nm)
        verdict = classify(diff, margin, same)
        stats[verdict] += 1
        if verdict != "PASS" or abs(diff) > 60:
            print(f"{nm:%Y-%m-%d %H:%M}     {est:%Y-%m-%d %H:%M:%S.%f}  "
                  f"{diff:+8.1f}s {margin/60:9.1f}p  {vn_date(est)!s:>12}  {verdict}")
    print("-" * 92)
    total = sum(stats.values())
    print(f"Tổng {total} điểm Sóc trong {len(years)} năm khảo sát")
    print(f"  PASS={stats['PASS']}  BORDERLINE={stats['BORDERLINE']}  FAIL={stats['FAIL']}")
    print(f"  |lệch| lớn nhất so với NASA: {worst:.1f} giây")


if __name__ == "__main__":
    main()
