#!/usr/bin/env python3
"""
KIỂM CHỨNG ĐỘC LẬP dataset lịch âm. Dev-only, ngoài app/, không vào APK.

=========================== TÍNH ĐỘC LẬP ===========================
Sự cố Phase 3 xảy ra vì parser hỏng → mô hình tham chiếu → dataset → fixture → test
đều dùng CHUNG một sai lầm. File này tồn tại để phá chuỗi đó.

File này KHÔNG import bất kỳ hàm nào của generator. Nó tự viết:
  · parser trang NASA (điểm Sóc và cột ΔT)
  · hiện thực ΔT riêng (bảng hệ số, không phải chuỗi if của generator)
  · driver ERFA riêng, công thức JD riêng
  · parser file .bin riêng
  · suy ra ngày dương lịch Việt Nam riêng
  · quy tắc lịch R1-R4 riêng

ĐỘC LẬP TỚI ĐÂU — nói thẳng:
  ✅ ĐỘC LẬP THẬT  cột ΔT của NASA đối chiếu đa thức sản xuất (dữ liệu ngoài)
  ✅ ĐỘC LẬP THẬT  phút điểm Sóc của NASA đối chiếu dataset (dữ liệu ngoài)
  ⚠️ CHỈ LÀ KIỂM CHÉO HIỆN THỰC  thời điểm trung khí — cùng dùng thư viện ERFA và
     cùng đa thức ΔT, vì đó LÀ nguồn sản xuất đã đóng băng. Hai bên viết tách rời nên
     bắt được lỗi chép sai, KHÔNG chứng minh được thiên văn đúng.
  Chứng cứ ngoài cho quy tắc lịch nằm ở HKO và văn bản nhà nước, không ở file này.
====================================================================
"""
import hashlib
import html
import math
import re
import struct
import subprocess
import sys
import urllib.request
from datetime import datetime, timedelta, timezone

BENCH = "/tmp/erfabuild/erfa_bench"
BIN = "app/src/main/assets/lunar/vn_lunar_v1.bin"
EPOCH_V = datetime(1890, 1, 1, tzinfo=timezone.utc)
VN_TZ = timezone(timedelta(hours=7))
MON = dict(zip("Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec".split(), range(1, 13)))

# ΔT — hệ số Espenak & Meeus (NASA/GSFC) viết dạng BẢNG, tách rời hiện thực của
# generator. Mỗi mục: (cận trên y, gốc t, danh sách hệ số theo luỹ thừa tăng dần).
_DT_TABLE = [
    (1920, 1900, [-2.79, 1.494119, -0.0598939, 0.0061966, -0.000197]),
    (1941, 1920, [21.20, 0.84493, -0.076100, 0.0020936]),
    (1961, 1950, [29.07, 0.407, -1 / 233.0, 1 / 2547.0]),
    (1986, 1975, [45.45, 1.067, -1 / 260.0, -1 / 718.0]),
    (2005, 2000, [63.86, 0.3345, -0.060374, 0.0017275, 0.000651814, 0.00002373599]),
    (2050, 2000, [62.92, 0.32217, 0.005589]),
]


def dt_independent(year, month=7):
    y = year + (month - 0.5) / 12.0
    for hi, origin, co in _DT_TABLE:
        if y < hi:
            t = y - origin
            return sum(c * t ** i for i, c in enumerate(co))
    if y < 2150:
        return -20 + 32 * ((y - 1820) / 100.0) ** 2 - 0.5628 * (2150 - y)
    return -20 + 32 * ((y - 1820) / 100.0) ** 2


def scrape():
    """Parser riêng cho trang NASA. Trả (điểm Sóc, ΔT theo năm tính bằng giây)."""
    moons, dts = [], {}
    for start in (1901, 2001):
        raw = urllib.request.urlopen(
            f"https://eclipse.gsfc.nasa.gov/phase/phases{start}.html", timeout=90).read()
        txt = html.unescape(re.sub(r"<[^>]+>", "", raw.decode("utf-8", "replace")))
        lines = txt.splitlines()
        hdr = next(i for i, l in enumerate(lines) if "New Moon" in l and "ΔT" in l)
        c0 = lines[hdr].index("New Moon") - 4
        c1 = lines[hdr].index("First Quarter") - 2
        cdt = lines[hdr].index("ΔT") - 4
        year = None
        for l in lines[hdr:]:
            ym = re.match(r"\s*(\d{4})\s", l)
            if ym:
                year = int(ym.group(1))
                seg = l[cdt:] if len(l) > cdt else ""
                dm = re.search(r"(\d{2})h(\d{2})m", seg)
                if dm:
                    dts[year] = int(dm.group(1)) * 3600 + int(dm.group(2)) * 60
            if year is None or len(l) < c0:
                continue
            mm = re.search(r"([A-Z][a-z]{2})\s+(\d{1,2})\s+(\d{2}):(\d{2})", l[c0:c1])
            if mm:
                moons.append(datetime(year, MON[mm.group(1)], int(mm.group(2)),
                                      int(mm.group(3)), int(mm.group(4)),
                                      tzinfo=timezone.utc))
    return moons, dts


def jdn(y, m, d):
    a = (14 - m) // 12
    yy, mm = y + 4800 - a, m + 12 * a - 3
    return d + (153 * mm + 2) // 5 + 365 * yy + yy // 4 - yy // 100 + yy // 400 - 32045


def read_bin(path):
    b = open(path, "rb").read()
    if b[:4] != b"NNLD":
        sys.exit("magic sai")
    ver, fr, to, n_m, n_t = struct.unpack(">HHHII", b[4:18])
    need = 18 + 4 * (n_m + n_t)
    if len(b) != need:
        sys.exit(f"kích thước sai: {len(b)} != {need}")
    o = 18
    mo = list(struct.unpack(f">{n_m}I", b[o:o + 4 * n_m])); o += 4 * n_m
    te = list(struct.unpack(f">{n_t}I", b[o:o + 4 * n_t]))
    return b, ver, fr, to, mo, te


def as_dt(minute):
    return EPOCH_V + timedelta(minutes=minute)


def vn(minute):
    return as_dt(minute).astimezone(VN_TZ).date()


def main():
    blob, ver, fr, to, mo, te = read_bin(BIN)
    sha = hashlib.sha256(blob).hexdigest()
    ok = True

    def check(label, cond, detail=""):
        nonlocal ok
        print(f"  [{'OK ' if cond else 'FAIL'}] {label}{('  ' + detail) if detail else ''}")
        ok = ok and cond

    print("=" * 66)
    print("KIỂM CHỨNG ĐỘC LẬP DATASET LỊCH ÂM")
    print("=" * 66)
    print(f"\nsha256 {sha}\nsize   {len(blob):,} B   version {ver}   phạm vi {fr}-{to}")
    print(f"Sóc {len(mo)}   trung khí {len(te)}")

    print("\n--- 1. Cấu trúc file (parser riêng) ---")
    check("Sóc tăng đơn điệu", all(b > a for a, b in zip(mo, mo[1:])))
    check("trung khí tăng đơn điệu", all(b > a for a, b in zip(te, te[1:])))
    check("số trung khí chia hết 12", len(te) % 12 == 0, f"{len(te)} = 12 × {len(te)//12}")
    gaps = [b - a for a, b in zip(mo, mo[1:])]
    check("khoảng cách Sóc trong [29,0 ; 30,0] ngày",
          all(29.0 * 1440 <= g <= 30.0 * 1440 for g in gaps),
          f"min {min(gaps)/1440:.3f}d max {max(gaps)/1440:.3f}d")
    check("phủ trọn 1901-01-01..2100-12-31",
          vn(mo[0]) < datetime(1901, 1, 1).date() and vn(mo[-1]) > datetime(2100, 12, 31).date())

    print("\n--- 2. ✅ ĐỘC LẬP: đối chiếu dữ liệu NASA vừa tải ---")
    nasa_moons, nasa_dts = scrape()
    check("số điểm Sóc NASA 1901-2100", len(nasa_moons) == 2474, f"{len(nasa_moons)}")
    ds = {as_dt(m).strftime("%Y-%m-%dT%H:%MZ") for m in mo}
    miss = [x for x in nasa_moons if x.strftime("%Y-%m-%dT%H:%MZ") not in ds]
    check("mọi điểm Sóc NASA đều có trong dataset, đúng tới phút",
          not miss, f"thiếu {len(miss)}")

    print("\n--- 3. ✅ ĐỘC LẬP: đa thức ΔT vs cột ΔT của NASA ---")
    # Quan hệ đã xác định bằng thực nghiệm: cột NASA = round(ΔT / 60) phút. Các năm
    # cột nhảy bậc là 1952, 2047, 2077 — đúng chỗ đa thức cắt 30 s, 90 s, 150 s.
    # Đây là xác nhận độc lập rằng hiện thực ΔY của ta khớp ΔT của chính NASA, trong
    # giới hạn phân giải mà NASA công bố.
    print("      cột NASA = round(ΔT/60) phút; bậc nhảy 1952/2047/2077 khớp đúng chỗ")
    print("      đa thức cắt 30/90/150 s ⇒ dung sai của nguồn là ±30 s")
    TIE = 0.10   # 2046: đa thức 90,02 s nằm sát điểm hoà 90 s, NASA làm tròn xuống
    worst, bad = 0.0, []
    for y, col in sorted(nasa_dts.items()):
        if not (fr <= y <= to):
            continue
        d = dt_independent(y) - col
        worst = max(worst, abs(d))
        if abs(d) > 30.0 + TIE:
            bad.append((y, round(dt_independent(y), 2), col))
    check(f"toàn bộ {len([y for y in nasa_dts if fr<=y<=to])} năm nằm trong lượng tử của cột",
          not bad, f"lệch lớn nhất {worst:.2f} s")
    for y, p, c in bad[:10]:
        print(f"        {y}: đa thức {p} s vs cột NASA {c} s")

    print("\n--- 4. ⚠️ KIỂM CHÉO HIỆN THỰC: tính lại trung khí bằng driver riêng ---")
    jobs = []
    for y in range(fr - 2, to + 3):
        eq = jdn(y, 3, 20) - 0.5
        for k in range(12):
            g = eq + (k * 30) / 0.98564736
            z = int(g + 0.5)
            al = int((z - 1867216.25) / 36524.25)
            a2 = z + 1 + al - al // 4
            bb = a2 + 1524
            cc = int((bb - 122.1) / 365.25)
            dd = int(365.25 * cc)
            ee = int((bb - dd) / 30.6001)
            mth = ee - 1 if ee < 14 else ee - 13
            jobs.append((dt_independent(y, mth), k * 30, g))
    pay = "".join(f"{a:.6f} {b} {c:.9f}\n" for a, b, c in jobs)
    res = subprocess.run([BENCH, "sunbatch"], input=pay, capture_output=True,
                         text=True, check=True).stdout.split()
    # sunbatch in "jdTT deltaT longitude". Đổi sang UT rồi sang lịch bằng mốc Unix
    # (JD 2440587,5 = 1970-01-01T00:00Z) — tuyến đường khác hẳn phép phân rã lịch
    # kiểu Meeus mà generator dùng.
    UNIX_JD = 2440587.5
    UNIX_T0 = datetime(1970, 1, 1, tzinfo=timezone.utc)
    mine = []
    for i in range(0, len(res), 3):
        jd_tt, dt_s = float(res[i]), float(res[i + 1])
        mine.append(UNIX_T0 + timedelta(days=jd_tt - dt_s / 86400.0 - UNIX_JD))
    mine.sort()
    check("cùng số lượng trung khí", len(mine) == len(te))
    diffs = [abs((mine[i] - as_dt(te[i])).total_seconds()) for i in range(min(len(mine), len(te)))]
    check("mọi trung khí khớp trong [0,60) s của lượng tử hoá floor",
          all(0 <= d < 60 for d in diffs), f"lệch lớn nhất {max(diffs):.3f} s")
    civil = [i for i in range(len(mine)) if mine[i].astimezone(VN_TZ).date() != vn(te[i])]
    check("0 bất đồng NGÀY DƯƠNG LỊCH VIỆT NAM", not civil, f"{len(civil)} ca")
    for i in civil[:10]:
        print(f"        {mine[i]}  độc lập {mine[i].astimezone(VN_TZ).date()}  "
              f"dataset {vn(te[i])}")

    print("\n--- 5. floor có thực sự bảo toàn ngày Việt Nam không ---")
    viol = []
    for i, m in enumerate(te):
        exact = mine[i] if i < len(mine) else None
        if exact and exact.astimezone(VN_TZ).date() != vn(m):
            viol.append(i)
    check("không thời điểm nào bị lượng tử hoá đẩy qua ranh giới ngày", not viol)
    rnd = sum(1 for i, m in enumerate(te)
              if i < len(mine)
              and (EPOCH_V + timedelta(minutes=int(round(
                  (mine[i] - EPOCH_V).total_seconds() / 60.0)))).astimezone(VN_TZ).date()
              != mine[i].astimezone(VN_TZ).date())
    print(f"      đối chứng: nếu dùng round thay floor thì {rnd} trung khí sẽ SAI ngày")

    print("\n--- 6. Quy tắc lịch R1-R4, hiện thực riêng ---")
    starts = [vn(m) for m in mo]
    tdays = [vn(t) for t in te]
    tdeg = [(i % 12) * 30 for i in range(len(te))]
    ws = [tdays[i] for i in range(len(te)) if tdeg[i] == 270]

    def midx(day):
        lo = 0
        for i, s in enumerate(starts):
            if s <= day:
                lo = i
            else:
                break
        return lo

    leaps, thirteens = {}, 0
    for k in range(len(ws) - 1):
        a, b = midx(ws[k]), midx(ws[k + 1])
        if b - a == 13:
            thirteens += 1
            for j in range(a, b):
                if not any(starts[j] <= x < starts[j + 1] for x in tdays):
                    leaps[starts[a]] = starts[j]
                    break
    check("mọi năm 13 tháng đều tìm được đúng một tháng thiếu trung khí",
          len(leaps) == thirteens, f"{len(leaps)}/{thirteens}")
    lens = [(starts[i + 1] - starts[i]).days for i in range(len(starts) - 1)]
    check("mọi tháng âm dài 29 hoặc 30 ngày", set(lens) <= {29, 30},
          f"29 ngày ×{lens.count(29)}, 30 ngày ×{lens.count(30)}")
    print(f"      số năm nhuận trong dải: {len(leaps)}")
    k1938 = [v for k, v in leaps.items() if v.year == 1938]
    print(f"      tháng nhuận thuộc năm 1938: {k1938}")

    print("\n--- 7. ✅ Quét biên ±120 s quanh 17:00:00Z ---")

    def off(d):
        r = d.replace(hour=17, minute=0, second=0, microsecond=0)
        return min(((d - c).total_seconds() for c in
                    (r - timedelta(days=1), r, r + timedelta(days=1))), key=abs)

    nmb = [m for m in mo if abs(off(as_dt(m))) <= 120]
    ptb = [i for i in range(len(mine)) if abs(off(mine[i])) <= 120]
    print(f"      Sóc sát biên       : {len(nmb)}")
    print(f"      trung khí sát biên : {len(ptb)}")

    print("\n" + "=" * 66)
    print("KẾT QUẢ: " + ("DATASET HỢP LỆ" if ok else "⛔ CÓ LỖI"))
    print("=" * 66)
    return 0 if ok else 1


if __name__ == "__main__":
    sys.exit(main())
