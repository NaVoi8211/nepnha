#!/usr/bin/env python3
"""
Kiểm chứng phía VIỆT NAM của reference model. Dev-only, ngoài app/.

Gồm:
  1. Vector V1 từ văn bản nhà nước (oracle Tier 1 duy nhất hiện có)
  2. Các ca Việt Nam khác Trung Quốc — so model@105E với model@120E và với HKO
  3. Độ nhạy nguồn Sóc: NASA (phân giải phút) vs ERFA (phân giải giây)
  4. Tháng nhuận, tháng 29/30 ngày trên toàn dải
  5. Ngày văn hoá, biên, round-trip
"""
import sys, os
from datetime import date, timedelta
sys.path.insert(0, os.path.dirname(__file__))
from build_inputs import load_nasa, erfa_new_moons, erfa_solar_terms
from vn_lunar_reference import VietnameseLunarReference, LunarDate

# --- V1: văn bản nhà nước. Đây là oracle Tier 1 DUY NHẤT hiện có. ---
V1 = [
    (date(2025, 1, 25), (26, 12, 2024), "6150/TB-BLĐTBXH: 26 tháng Chạp Giáp Thìn"),
    (date(2025, 1, 29), (1, 1, 2025),   "6150/TB-BLĐTBXH: mùng 1 Tết Ất Tỵ"),
    (date(2025, 2, 2),  (5, 1, 2025),   "6150/TB-BLĐTBXH: mùng 5 tháng Giêng"),
    (date(2026, 2, 14), (27, 12, 2025), "9441/TB-BNV: 27 tháng Chạp Ất Tỵ"),
    (date(2026, 2, 17), (1, 1, 2026),   "9441/TB-BNV: mùng 1 Tết Bính Ngọ"),
    (date(2026, 2, 22), (6, 1, 2026),   "9441/TB-BNV: mùng 6 Tết"),
    # Ban Lịch Nhà nước qua báo Thanh Niên
    (date(1985, 1, 21), (1, 1, 1985),   "Thanh Niên/Ban Lịch NN: Tết Ất Sửu"),
]

DIVERGENCE_YEARS = [1984, 1985, 1987, 2006, 2007, 2008, 2030, 2053]


def main():
    nasa, dts = load_nasa()
    erfa = erfa_new_moons(nasa, dts)
    terms = erfa_solar_terms(dts)
    vn = VietnameseLunarReference(erfa, terms, 7.0)
    cn = VietnameseLunarReference(erfa, terms, 8.0)

    print("=" * 78)
    print("1. VECTOR V1 — văn bản nhà nước (oracle Tier 1)")
    ok = 0
    for g, (d, m, y), src in V1:
        l = vn.to_lunar(g)
        good = (l.day, l.month, l.year) == (d, m, y) and not l.is_leap_month
        ok += good
        print(f"   {'PASS' if good else 'FAIL'}  {g}  model={l}  kỳ vọng={d}/{m}/{y}   {src}")
    print(f"   => {ok}/{len(V1)}")

    print("=" * 78)
    print("2. VIỆT NAM ≠ TRUNG QUỐC — số ngày lệch trong từng năm")
    for y in DIVERGENCE_YEARS:
        diff = sum(1 for n in range((date(y, 12, 31) - date(y, 1, 1)).days + 1)
                   if str(vn.to_lunar(date(y, 1, 1) + timedelta(days=n)))
                   != str(cn.to_lunar(date(y, 1, 1) + timedelta(days=n))))
        vt = vn.leap_month_of(y); ct = cn.leap_month_of(y)
        tet_vn = vn.to_solar(LunarDate(1, 1, y)); tet_cn = cn.to_solar(LunarDate(1, 1, y))
        mark = "  <-- KHÁC" if diff else ""
        print(f"   {y}: {diff:3d} ngày lệch | Tết VN {tet_vn} · TQ {tet_cn} | "
              f"nhuận VN {vt} · TQ {ct}{mark}")

    print("=" * 78)
    print("3. ĐỘ NHẠY NGUỒN SÓC — NASA (phút) vs ERFA (giây), cùng quy tắc @105E")
    vn_nasa = VietnameseLunarReference(nasa, terms, 7.0)
    # Mô hình NASA không có đệm hai đầu nên dải hẹp hơn; chỉ so phần CHUNG.
    d0, d1 = date(1902, 1, 1), date(2100, 1, 1)
    n = (d1 - d0).days
    diffs, compared = [], 0
    for k in range(n):
        g = d0 + timedelta(days=k)
        try:
            a, b = str(vn.to_lunar(g)), str(vn_nasa.to_lunar(g))
        except ValueError:
            continue
        compared += 1
        if a != b:
            diffs.append(g)
    print(f"   So sánh được {compared} ngày. Ngày âm KHÁC nhau: {len(diffs)}"
          f"  ({len(diffs)/compared*100:.4f}%)")
    if diffs:
        runs, cur = [], [diffs[0]]
        for a, b in zip(diffs, diffs[1:]):
            (cur.append(b) if (b - a).days == 1 else (runs.append(cur), cur := [b]))
        runs.append(cur)
        for r in runs:
            print(f"     {r[0]} .. {r[-1]}  ({len(r)} ngày)  "
                  f"ERFA={vn.to_lunar(r[0])} NASA={vn_nasa.to_lunar(r[0])}")

    print("=" * 78)
    print("4. THÁNG NHUẬN & ĐỘ DÀI THÁNG — toàn dải 1901-2100 (mô hình @105E)")
    leaps = {y: vn.leap_month_of(y) for y in range(1901, 2101)}
    nleap = sum(1 for v in leaps.values() if v)
    from collections import Counter
    cnt = Counter(v for v in leaps.values() if v)
    print(f"   Năm nhuận: {nleap}/200   phân bố tháng nhuận: {dict(sorted(cnt.items()))}")
    lens = Counter(vn.month_length(i) for i in sorted(vn.month_meta) if i + 1 < len(vn.month_starts))
    print(f"   Độ dài tháng âm: {dict(sorted(lens.items()))}")
    print(f"   monthsInLunarYear: {Counter(vn.months_in_year(y) for y in range(1902, 2100))}")

    print("=" * 78)
    print("5. NGÀY VĂN HOÁ (mô hình, chưa có V1) + BIÊN + ROUND-TRIP")
    for y in (2026, 2027):
        for (d, m, name) in [(15,1,"Rằm tháng Giêng"), (5,5,"Tết Đoan Ngọ"),
                             (15,7,"Rằm tháng Bảy"), (15,8,"Trung Thu"),
                             (23,12,"Ông Công Ông Táo")]:
            try: print(f"   {y} {name:18} -> {vn.to_solar(LunarDate(d, m, y))}")
            except ValueError as e: print(f"   {y} {name:18} -> {e}")
    print(f"   Biên: 1901-01-01 -> {vn.to_lunar(date(1901,1,1))} | "
          f"2100-12-31 -> {vn.to_lunar(date(2100,12,31))}")
    bad = 0
    for k in range(0, (date(2100,12,31)-date(1901,1,1)).days, 7):
        g = date(1901,1,1) + timedelta(days=k)
        if vn.to_solar(vn.to_lunar(g)) != g: bad += 1
    print(f"   Round-trip G->L->G mỗi 7 ngày, 1901-2100: {bad} lỗi")


if __name__ == "__main__":
    main()
