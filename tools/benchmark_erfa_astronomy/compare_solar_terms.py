#!/usr/bin/env python3
"""
So sánh thời điểm 24 tiết khí tính bằng ERFA với bảng công bố của Đài Thiên văn
Hồng Kông (HKO). KHÔNG phải code của app.

HKO là ORACLE ĐỘC LẬP (nhánh B), không phải input. HKO chỉ công bố kèm giờ cho
3 năm — vừa đủ để kiểm chứng pipeline, không đủ làm nguồn dữ liệu 1901-2100.

24 tiết khí nằm ở các bội số 15° của hoàng kinh Mặt Trời; mục đầu tiên của năm
(Tiểu Hàn) ở 285°.

Giờ HKO là giờ Hồng Kông = UTC+8, độ phân giải phút.
Ranh giới ngày âm Việt Nam là 00:00 UTC+7 = 17:00:00 UTC.

Nguồn: HKO qua data.gov.hk (cho phép dùng thương mại, kèm attribution).
"""
import re
import subprocess
import sys
import urllib.request
from datetime import datetime, timedelta, timezone

BENCH = sys.argv[1] if len(sys.argv) > 1 else "/tmp/erfabuild/erfa_bench"
HKO = "https://www.hko.gov.hk/en/gts/astronomy/data/files/24SolarTerms_{}.xml"
DELTA_T = {2026: 72.0, 2027: 72.0, 2028: 72.0}   # giây, từ cột ΔT của catalog NASA
NAMES = ["Tiểu Hàn", "Đại Hàn", "Lập Xuân", "Vũ Thuỷ", "Kinh Trập", "Xuân Phân",
         "Thanh Minh", "Cốc Vũ", "Lập Hạ", "Tiểu Mãn", "Mang Chủng", "Hạ Chí",
         "Tiểu Thử", "Đại Thử", "Lập Thu", "Xử Thử", "Bạch Lộ", "Thu Phân",
         "Hàn Lộ", "Sương Giáng", "Lập Đông", "Tiểu Tuyết", "Đại Tuyết", "Đông Chí"]


def hko_terms(year):
    with urllib.request.urlopen(HKO.format(year), timeout=60) as r:
        xml = r.read().decode("utf-8", "replace")
    out = []
    for mm, dd, hh, mi in re.findall(r"<M>(\d+)</M><D>(\d+)</D><hm>(\d+):(\d+)</hm>", xml):
        out.append(datetime(year, int(mm), int(dd), int(hh), int(mi),
                            tzinfo=timezone(timedelta(hours=8))))
    return out


def jd_from_datetime_utc(dt):
    a = (14 - dt.month) // 12
    y = dt.year + 4800 - a
    m = dt.month + 12 * a - 3
    jdn = dt.day + (153 * m + 2) // 5 + 365 * y + y // 4 - y // 100 + y // 400 - 32045
    return jdn + (dt.hour - 12) / 24 + dt.minute / 1440 + dt.second / 86400


def erfa_term(delta_t, deg, guess_jd_tt):
    out = subprocess.run([BENCH, "sunlong", str(delta_t), f"{deg:.6f}", f"{guess_jd_tt:.9f}"],
                         capture_output=True, text=True, check=True).stdout
    g = [int(x) for x in re.search(
        r"UT=(\d{4})-(\d{2})-(\d{2}) (\d{2}):(\d{2}):(\d{2})\.(\d{3})", out).groups()]
    return datetime(g[0], g[1], g[2], g[3], g[4], g[5], g[6] * 1000, tzinfo=timezone.utc)


def vn_margin_s(dt_utc):
    b = dt_utc.replace(hour=17, minute=0, second=0, microsecond=0)
    return min(abs((dt_utc - b).total_seconds()),
               abs((dt_utc - (b - timedelta(days=1))).total_seconds()),
               abs((dt_utc - (b + timedelta(days=1))).total_seconds()))


def main():
    print(f"{'Tiết khí':<12} {'HKO (giờ HK)':<18} {'ERFA (giờ HK)':<21} {'lệch':>8} "
          f"{'margin VN':>10}  kết luận")
    print("-" * 88)
    worst, n, borderline = 0.0, 0, 0
    for year in (2026, 2027, 2028):
        dt = DELTA_T[year]
        for i, hko in enumerate(hko_terms(year)):
            deg = (285 + 15 * i) % 360
            guess = jd_from_datetime_utc(hko.astimezone(timezone.utc)) + dt / 86400.0
            est_utc = erfa_term(dt, deg, guess)
            est_hk = est_utc.astimezone(timezone(timedelta(hours=8)))
            diff = (est_hk - hko).total_seconds()
            worst = max(worst, abs(diff)); n += 1
            margin = vn_margin_s(est_utc)
            same_vn_day = (est_utc + timedelta(hours=7)).date() == \
                          (hko.astimezone(timezone.utc) + timedelta(hours=7)).date()
            if not same_vn_day:
                verdict = "FAIL"
            elif margin < 120:
                verdict = "BORDERLINE"; borderline += 1
            else:
                verdict = "PASS"
            if verdict != "PASS" or abs(diff) > 45:
                print(f"{NAMES[i]:<12} {hko:%Y-%m-%d %H:%M}      "
                      f"{est_hk:%Y-%m-%d %H:%M:%S}   {diff:+6.0f}s {margin/60:8.1f}p  {verdict}")
    print("-" * 88)
    print(f"Đã đối chiếu {n} tiết khí (2026-2028)")
    print(f"  |lệch| lớn nhất so với HKO: {worst:.0f} giây")
    print(f"  số ca BORDERLINE (margin < 2 phút tới nửa đêm VN): {borderline}")


if __name__ == "__main__":
    main()
