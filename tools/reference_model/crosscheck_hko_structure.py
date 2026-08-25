#!/usr/bin/env python3
"""
KIỂM CHỨNG CẤU TRÚC — reference model @120E vs bảng lịch Trung Quốc của HKO.

Cơ sở: quy tắc R1-R5 dùng CHUNG giữa lịch Việt Nam và lịch Trung Quốc. Khác biệt
duy nhất là kinh tuyến (R6). Nên nếu mô hình chạy ở 120E tái lập đúng bảng HKO thì
R1-R5 và tầng thiên văn ĐÃ ĐƯỢC KIỂM CHỨNG, đối chiếu một đài thiên văn quốc gia
dùng số liệu HM Nautical Almanac Office (khác nhánh Meeus).

GIỚI HẠN: điều này KHÔNG chứng minh lịch chính thức Việt Nam = R1-R5 tại 105E.
Mắt xích đó dựa vào QĐ 121-CP điều 1 và các ca VN≠TQ.

Dev-only. Ngoài app/. Không đưa dữ liệu HKO vào APK.
Nguồn: HKO qua data.gov.hk (cho phép dùng thương mại, kèm attribution).
"""
import re, sys, os, time, urllib.request
from datetime import date
sys.path.insert(0, os.path.dirname(__file__))
from build_inputs import load_nasa, erfa_new_moons, erfa_solar_terms   # noqa: E402
from vn_lunar_reference import VietnameseLunarReference                # noqa: E402

HKO = "https://www.hko.gov.hk/en/gts/time/calendar/text/files/T{}e.txt"
ORD = {'1st':1,'2nd':2,'3rd':3,'4th':4,'5th':5,'6th':6,
       '7th':7,'8th':8,'9th':9,'10th':10,'11th':11,'12th':12}


def hko_month_starts(year):
    """[(date, month_no, is_leap)] — HKO đánh dấu nhuận bằng số tháng LẶP LẠI."""
    req = urllib.request.Request(HKO.format(year), headers={"User-Agent": "Mozilla/5.0"})
    time.sleep(0.15)   # lịch sự với máy chủ HKO, không dồn dập
    with urllib.request.urlopen(req, timeout=60) as r:
        txt = r.read().decode("utf-8", "replace")
    out, prev_num = [], None
    for line in txt.splitlines():
        m = re.match(r"\s*(\d{4})/(\d{1,2})/(\d{1,2})\s+(\w+)\s+Lunar\s+Month", line, re.I)
        if not m:
            continue
        y, mo, d, ordinal = int(m.group(1)), int(m.group(2)), int(m.group(3)), m.group(4).lower()
        if ordinal not in ORD:
            continue
        num = ORD[ordinal]
        # Nhuận = trùng số với tháng LIỀN TRƯỚC. Không dùng "đã thấy trong năm":
        # tháng 12 có thể xuất hiện hai lần trong một năm dương mà thuộc HAI năm âm
        # khác nhau — bản parser đầu tiên nhầm chỗ này thành 7 ca nhuận giả.
        is_leap = (num == prev_num)
        prev_num = num
        out.append((date(y, mo, d), num, is_leap))
    return out


def main():
    if len(sys.argv) == 3 and sys.argv[1] == "--range":
        a, b = sys.argv[2].split("-"); years = list(range(int(a), int(b) + 1))
    else:
        years = [int(y) for y in sys.argv[1:]] or (
        list(range(1901, 1911)) + [1925, 1937, 1944, 1955, 1960, 1967, 1968, 1975, 1976,
                                   1984, 1985, 1987, 1990, 2000, 2006, 2007, 2008, 2023,
                                   2025, 2026, 2028, 2030, 2033, 2050, 2053, 2075,
                                   2097, 2098, 2099, 2100])
    print(f"Đang tải {len(years)} năm dữ liệu HKO...", flush=True)
    nasa, dts = load_nasa()
    moons = erfa_new_moons(nasa, dts)
    terms = erfa_solar_terms(dts)
    cn = VietnameseLunarReference(moons, terms, 8.0)      # 120 độ Đông

    tot = ok = 0
    bad_start = bad_num = bad_leap = 0
    leap_years_checked = leap_ok = 0
    len29 = len30 = 0
    problems = []

    for y in years:
        try:
            ref = hko_month_starts(y)
        except Exception as e:
            problems.append((y, f"không tải được HKO: {e}")); continue
        mine = cn.month_starts_in_year(y)
        if len(ref) != len(mine):
            problems.append((y, f"số tháng khác: HKO={len(ref)} model={len(mine)}"))
        for (rd, rn, rl), (md, mn, ml) in zip(ref, mine):
            tot += 1
            good = True
            if rd != md: bad_start += 1; good = False
            if rn != mn: bad_num += 1; good = False
            if rl != ml: bad_leap += 1; good = False
            if good: ok += 1
            else: problems.append((y, f"HKO {rd} th{rn}{'N' if rl else ''} != model {md} th{mn}{'N' if ml else ''}"))
        if any(l for _, _, l in ref):
            leap_years_checked += 1
            if [ (n,l) for _,n,l in ref ] == [ (n,l) for _,n,l in mine ]:
                leap_ok += 1
        # độ dài tháng: đo trên chuỗi mốc của model
        for a, b in zip(mine, mine[1:]):
            n = (b[0] - a[0]).days
            if n == 29: len29 += 1
            elif n == 30: len30 += 1

    print(f"Kiểm chứng cấu trúc: reference model @120E  vs  HKO (lịch Trung Quốc)")
    print(f"  số năm đối chiếu      : {len(years)}")
    print(f"  số tháng đối chiếu    : {tot}")
    print(f"  khớp hoàn toàn        : {ok}  ({ok/tot*100:.2f}%)" if tot else "")
    print(f"  lệch ngày bắt đầu     : {bad_start}")
    print(f"  lệch số tháng         : {bad_num}")
    print(f"  lệch cờ nhuận         : {bad_leap}")
    print(f"  năm nhuận đối chiếu   : {leap_years_checked}  (khớp {leap_ok})")
    print(f"  tháng 29 ngày quan sát: {len29}   tháng 30 ngày: {len30}")
    if problems:
        print(f"\n  {len(problems)} khác biệt:")
        for y, p in problems[:25]:
            print(f"    {y}: {p}")


if __name__ == "__main__":
    main()
