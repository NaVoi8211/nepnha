#!/usr/bin/env python3
"""
Sinh bảng 24 tiết khí 1901-2100 bằng ERFA. KHÔNG phải code của app.

Chỉ dùng nhánh Mặt Trời (eraEpv00, eraEqec06, eraNut06a) — nhánh này KHÔNG dính
Meeus. eraMoon98 không được gọi ở đây.

Đầu ra là dữ liệu ứng viên để đóng gói thành asset; script này chỉ sinh và KIỂM TRA
TÍNH TOÀN VẸN, chưa đưa vào app.

ΔT lấy theo năm từ catalog NASA (cùng giấy phép với dữ liệu Sóc). Nội suy: dùng
nguyên giá trị của năm chứa thời điểm — xem docs/PHASE_3A_NEXT_GATE.md §H.
"""
import subprocess
import sys
from datetime import datetime, timedelta, timezone

sys.path.insert(0, __file__.rsplit("/", 1)[0])
from compare_with_nasa import fetch, parse                      # noqa: E402
from scan_all_newmoons import jd_to_datetime                    # noqa: E402

BENCH = sys.argv[1] if len(sys.argv) > 1 else "/tmp/erfabuild/erfa_bench"
YEARS = range(1901, 2101)
# Sinh dư một chu kỳ ở hai đầu: chu kỳ bắt đầu từ Xuân phân nên các tiết khí
# tháng 1-3 của năm Y thuộc chu kỳ của năm Y-1. Không có dòng này thì 1901
# thiếu 5 mốc đầu năm — chính kiểm tra [5] đã bắt được lỗi đó.
GEN_YEARS = range(1900, 2101)
TERMS = list(range(0, 360, 15))          # 24 tiết khí, mỗi 15 độ hoàng kinh
SUN_DEG_PER_DAY = 0.98564736


def jd_of(y, m, d):
    a = (14 - m) // 12
    yy = y + 4800 - a
    mm = m + 12 * a - 3
    return d + (153 * mm + 2) // 5 + 365 * yy + yy // 4 - yy // 100 + yy // 400 - 32045 - 0.5


def main():
    _, delta_t = parse(fetch(1901))
    _, dt2 = parse(fetch(2001))
    delta_t.update(dt2)

    jobs = []
    for y in GEN_YEARS:
        equinox = jd_of(y, 3, 20)                 # hoàng kinh 0 độ ~ 20/3
        for deg in TERMS:
            jobs.append((delta_t.get(y, 69.0), deg, equinox + deg / SUN_DEG_PER_DAY))

    payload = "".join(f"{dt} {deg} {g:.9f}\n" for dt, deg, g in jobs)
    out = subprocess.run([BENCH, "sunbatch"], input=payload,
                         capture_output=True, text=True, check=True).stdout.split()

    rows = []
    for (dt, deg, _), i in zip(jobs, range(0, len(out), 3)):
        jd, dt_used, lon = float(out[i]), float(out[i + 1]), float(out[i + 2])
        rows.append((deg, jd, dt_used, lon, jd_to_datetime(jd, dt_used)))

    print(f"Đã tính {len(rows)} mốc tiết khí\n")

    # ---------- KIỂM TRA TOÀN VẸN (mục F.6) ----------
    fails = 0

    bad_lon = [r for r in rows if min(abs(r[3] - r[0]), 360 - abs(r[3] - r[0])) > 1e-4]
    print(f"[1] Hoàng kinh sai lệch > 0.0001 độ: {len(bad_lon)}")
    fails += len(bad_lon)

    rows.sort(key=lambda r: r[1])
    non_mono = sum(1 for a, b in zip(rows, rows[1:]) if b[1] <= a[1])
    print(f"[2] Timestamp không tăng đơn điệu: {non_mono}")
    fails += non_mono

    gaps = [b[1] - a[1] for a, b in zip(rows, rows[1:])]
    bad_gap = [g for g in gaps if not (13.0 < g < 17.0)]
    print(f"[3] Khoảng cách hai tiết khí ngoài [13,17] ngày: {len(bad_gap)}"
          f"   (min={min(gaps):.3f}  max={max(gaps):.3f})")
    fails += len(bad_gap)

    jds = [r[1] for r in rows]
    dups = len(jds) - len(set(round(j, 6) for j in jds))
    print(f"[4] Mốc trùng nhau: {dups}")
    fails += dups

    in_range = [r for r in rows if 1901 <= r[4].year <= 2100]
    per_year = {}
    for r in in_range:
        per_year[r[4].year] = per_year.get(r[4].year, 0) + 1
    wrong = {y: n for y, n in per_year.items() if n != 24}
    print(f"[5] Năm không có đúng 24 tiết khí: {len(wrong)}"
          + (f"  {dict(list(wrong.items())[:5])}" if wrong else ""))
    fails += len(wrong)

    missing = sorted(set(YEARS) - set(per_year))
    print(f"[6] Năm thiếu hoàn toàn: {len(missing)}")
    fails += len(missing)

    print(f"\n=> {'✅ TOÀN VẸN' if fails == 0 else f'❌ {fails} lỗi'}")

    # ---------- Ước lượng kích thước asset ----------
    epoch = datetime(1901, 1, 1, tzinfo=timezone.utc)
    minutes = [int(round((r[4] - epoch).total_seconds() / 60)) for r in in_range]
    deltas = [b - a for a, b in zip(minutes, minutes[1:])]
    print(f"\nKích thước ước tính (mã hoá hiệu số phút, varint):")
    print(f"  số mốc trong 1901-2100: {len(in_range)}")
    print(f"  hiệu số phút: min={min(deltas)} max={max(deltas)} "
          f"=> vừa 2 byte/mốc")
    print(f"  thô 4 byte/mốc : {len(in_range)*4/1024:.1f} KB")
    print(f"  hiệu số 2 byte : {len(in_range)*2/1024:.1f} KB")

    # ---------- Đối chiếu bảng đã sinh với oracle HKO ----------
    print("\nĐối chiếu bảng vừa sinh với HKO (oracle độc lập, nhánh B):")
    try:
        from compare_solar_terms import hko_terms
        worst, n = 0.0, 0
        for y in (2026, 2027, 2028):
            hko = hko_terms(y)
            mine = sorted(r[4] for r in in_range if r[4].year == y)
            if len(hko) != len(mine):
                print(f"  {y}: SỐ MỐC KHÁC NHAU hko={len(hko)} ta={len(mine)}")
                continue
            for h, m in zip(hko, mine):
                d = abs((m - h.astimezone(timezone.utc)).total_seconds())
                worst = max(worst, d); n += 1
        print(f"  {n} mốc đối chiếu, |lệch| lớn nhất = {worst:.0f} giây")
        print(f"  (HKO làm tròn phút => sai số +-30 giây là của chính oracle)")
    except Exception as e:
        print(f"  không đối chiếu được: {e}")

    print(f"\nMẫu 3 mốc đầu:")
    for r in rows[:3]:
        print(f"  {r[0]:3.0f} độ  ->  {r[4]:%Y-%m-%d %H:%M:%S} UT")


if __name__ == "__main__":
    main()
