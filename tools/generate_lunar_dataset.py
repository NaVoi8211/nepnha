#!/usr/bin/env python3
"""
Sinh dataset thiên văn cho engine lịch âm của Nếp Nhà. Dev-only, ngoài app/.

QUAN TRỌNG: dataset chỉ chứa DỮ KIỆN THIÊN VĂN (thời điểm Sóc, thời điểm trung khí).
Nó KHÔNG chứa lịch đã tính sẵn. Quy tắc lịch nằm trong Kotlin. Đây là điều kiện để
tránh circular validation — xem docs/PHASE_3_IMPLEMENTATION_READINESS.md §Q9.

Nguồn:
  Sóc      NASA/GSFC Six Millennium Catalog of the Phases of the Moon
           "Moon Phase Predictions by Fred Espenak, NASA/GSFC"
           KHÔNG dùng eraMoon98 — xem readiness §Q2.
  Trung khí ERFA eraEpv00 + eraEqec06 + eraNut06a (BSD-3, nhánh KHÔNG dính Meeus)
  ΔT       Espenak & Meeus, "Polynomial Expressions for Delta T", NASA/GSFC.
           Xem tools/deltat.py. KHÔNG dùng cột ΔT của trang phases*.html vì cột đó
           chỉ có độ phân giải PHÚT (xem docs/PHASE_3_DATASET_CORRECTION.md §B).

LƯỢNG TỬ HOÁ: floor, KHÔNG phải round. Ranh giới ngày dương lịch Việt Nam nằm đúng
tại một mốc phút (17:00:00Z = 00:00 UTC+7), nên phút CHỨA một thời điểm luôn nằm trọn
về một phía của mốc đó. floor giữ nguyên ngày; round thì không. Xem §C của tài liệu
trên.

Đầu ra:
  app/src/main/assets/lunar/vn_lunar_v1.bin
  app/src/main/assets/lunar/vn_lunar_v1.json   (provenance, không đọc lúc chạy)
"""
import hashlib
import json
import math
import os
import struct
import subprocess
import sys
from datetime import datetime, timedelta, timezone

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "benchmark_erfa_astronomy"))
from compare_with_nasa import fetch, parse                  # noqa: E402
from scan_all_newmoons import jd_to_datetime                # noqa: E402
from deltat import delta_t_seconds                          # noqa: E402

BENCH = os.environ.get("ERFA_BENCH", "/tmp/erfabuild/erfa_bench")
OUT_DIR = os.path.join(HERE, "..", "app", "src", "main", "assets", "lunar")
MAGIC = b"NNLD"
VERSION = 1
FROM_YEAR, TO_YEAR = 1901, 2100
EPOCH = datetime(1890, 1, 1, tzinfo=timezone.utc)     # gốc đếm phút
# Đặt trước 1900 vì phần đệm lùi ~30 tuần trăng đưa mốc đầu tiên về ~1898.
SUN_DEG_PER_DAY = 0.98564736
PAD_LUNATIONS = 30                                     # đệm nội bộ, KHÔNG mở rộng phạm vi
PRINCIPAL_DEGREES = list(range(0, 360, 30))            # 12 trung khí


def minutes(dt):
    """Phút UTC CHỨA thời điểm dt. floor, không round — xem docstring đầu file."""
    return math.floor((dt - EPOCH).total_seconds() / 60.0)


def jd_of(y, mo, d):
    a = (14 - mo) // 12; yy = y + 4800 - a; mm = mo + 12 * a - 3
    return d + (153*mm+2)//5 + 365*yy + yy//4 - yy//100 + yy//400 - 32045 - 0.5


def new_moons_from_nasa(nasa, dts):
    """Dùng THẲNG giá trị NASA (phân giải phút). Đệm hai đầu bằng chu kỳ trung bình."""
    SYN = 29.530588853
    out = list(nasa)
    first, last = nasa[0], nasa[-1]
    out = ([first - timedelta(days=SYN * k) for k in range(PAD_LUNATIONS, 0, -1)]
           + out
           + [last + timedelta(days=SYN * k) for k in range(1, PAD_LUNATIONS + 1)])
    return out


def jd_to_month(jd):
    """Tháng dương lịch xấp xỉ của một JD — chỉ để chọn điểm đánh giá ΔT."""
    z = int(jd + 0.5); a = z
    if z >= 2299161:
        al = int((z - 1867216.25) / 36524.25)
        a = z + 1 + al - al // 4
    b = a + 1524; c = int((b - 122.1) / 365.25)
    d = int(365.25 * c); e = int((b - d) / 30.6001)
    return e - 1 if e < 14 else e - 13


def principal_terms():
    jobs = []
    for y in range(FROM_YEAR - 2, TO_YEAR + 3):
        eq = jd_of(y, 3, 20)
        for deg in PRINCIPAL_DEGREES:
            guess = eq + deg / SUN_DEG_PER_DAY
            jobs.append((delta_t_seconds(y, jd_to_month(guess)), deg, guess))
    payload = "".join(f"{dt:.6f} {deg} {g:.9f}\n" for dt, deg, g in jobs)
    out = subprocess.run([BENCH, "sunbatch"], input=payload, capture_output=True,
                         text=True, check=True).stdout.split()
    terms = []
    for (dt, deg, _), i in zip(jobs, range(0, len(out), 3)):
        terms.append(jd_to_datetime(float(out[i]), float(out[i + 1])))
    return sorted(terms), [dt for dt, _, _ in jobs]


def file_sha(name):
    with open(os.path.join(HERE, name), "rb") as f:
        return hashlib.sha256(f.read()).hexdigest()


def near_boundary(dt):
    """Khoảng cách (giây) tới mốc 17:00:00Z gần nhất. Âm = trước mốc."""
    ref = dt.replace(hour=17, minute=0, second=0, microsecond=0)
    best = None
    for cand in (ref - timedelta(days=1), ref, ref + timedelta(days=1)):
        d = (dt - cand).total_seconds()
        if best is None or abs(d) < abs(best):
            best = d
    return best


def vn_date(dt):
    return (dt + timedelta(hours=7)).date().isoformat()


def main():
    nasa = []
    for start in (1901, 2001):
        m, _ = parse(fetch(start)); nasa += m
    print(f"NASA: {len(nasa)} điểm Sóc 1901-2100")

    moons = new_moons_from_nasa(nasa, None)
    terms, dt_used = principal_terms()
    print(f"Sóc (kèm đệm {PAD_LUNATIONS} tuần trăng mỗi đầu): {len(moons)}")
    print(f"Trung khí (12/năm, kèm đệm 2 năm mỗi đầu): {len(terms)}")

    mm = [minutes(x) for x in moons]
    tt = [minutes(x) for x in terms]
    assert all(b > a for a, b in zip(mm, mm[1:])), "Sóc không tăng đơn điệu"
    assert all(b > a for a, b in zip(tt, tt[1:])), "Trung khí không tăng đơn điệu"
    assert all(0 <= v < 2**32 for v in mm + tt), "tràn u32"

    blob = (MAGIC + struct.pack(">HHHII", VERSION, FROM_YEAR, TO_YEAR, len(mm), len(tt))
            + b"".join(struct.pack(">I", v) for v in mm)
            + b"".join(struct.pack(">I", v) for v in tt))

    # --- §VI quét biên: TOÀN BỘ sự kiện, không giới hạn ở các ca đã biết ---
    nm_boundary, pt_boundary = [], []
    for x in moons:
        d = near_boundary(x)
        if abs(d) <= 120:
            fl, rn = math.floor((x - EPOCH).total_seconds() / 60.0), \
                     int(round((x - EPOCH).total_seconds() / 60.0))
            nm_boundary.append({
                "utc": x.strftime("%Y-%m-%dT%H:%M:%SZ"),
                "offsetFromBoundarySeconds": round(d, 3),
                "sourceResolution": "phút (NASA công bố)",
                "quantisedMinute": fl,
                "vnDate": vn_date(EPOCH + timedelta(minutes=fl)),
                "vnDateIfRound": vn_date(EPOCH + timedelta(minutes=rn)),
                "floorDiffersFromRound": fl != rn,
            })
    for x, dtv in zip(terms, [None] * len(terms)):
        d = near_boundary(x)
        if abs(d) <= 120:
            fl, rn = math.floor((x - EPOCH).total_seconds() / 60.0), \
                     int(round((x - EPOCH).total_seconds() / 60.0))
            pt_boundary.append({
                "utc": x.strftime("%Y-%m-%dT%H:%M:%S.%f")[:-3] + "Z",
                "offsetFromBoundarySeconds": round(d, 3),
                "deltaTSecondsUsed": round(delta_t_seconds(x.year, x.month), 3),
                "quantisedMinute": fl,
                "vnDate": vn_date(EPOCH + timedelta(minutes=fl)),
                "vnDateIfRound": vn_date(EPOCH + timedelta(minutes=rn)),
                "floorDiffersFromRound": fl != rn,
            })

    os.makedirs(OUT_DIR, exist_ok=True)
    bin_path = os.path.join(OUT_DIR, "vn_lunar_v1.bin")
    with open(bin_path, "wb") as f:
        f.write(blob)
    digest = hashlib.sha256(blob).hexdigest()

    provenance = {
        "schemaVersion": VERSION,
        "supportedGregorianRange": [FROM_YEAR, TO_YEAR],
        "note": "Chỉ chứa dữ kiện thiên văn. Quy tắc lịch nằm trong Kotlin.",
        "epochUtc": "1890-01-01T00:00:00Z",
        "unit": "phút UTC kể từ epoch, u32 big-endian",
        "quantisation": {
            "rule": "floor",
            "why": "Ranh giới ngày dương lịch Việt Nam nằm đúng tại một mốc phút "
                   "(17:00:00Z = 00:00 UTC+7). Phút CHỨA một thời điểm luôn nằm trọn "
                   "về một phía của mốc, nên floor không bao giờ đổi ngày Việt Nam; "
                   "round thì có thể.",
            "residualErrorSeconds": "[0, 60) — luôn lùi về trước, không bao giờ vượt biên",
        },
        "internalPaddingLunations": PAD_LUNATIONS,
        "paddingIsNotSupportedRange": True,
        "newMoonCount": len(mm),
        "principalTermCount": len(tt),
        "sha256": digest,
        "sizeBytes": len(blob),
        "generator": {
            "file": "tools/generate_lunar_dataset.py",
            "sha256": file_sha("generate_lunar_dataset.py"),
            "deltaTModule": "tools/deltat.py",
            "deltaTModuleSha256": file_sha("deltat.py"),
            "noWallClockByDesign": "Không nhúng thời điểm sinh, để việc sinh lại cho ra "
                                   "file giống hệt từng byte. Nhận dạng phiên bản dùng "
                                   "sha256 của chính script.",
        },
        "sources": {
            "newMoon": {
                "role": "DATA SOURCE",
                "name": "NASA/GSFC Six Millennium Catalog of the Phases of the Moon",
                "url": "https://eclipse.gsfc.nasa.gov/phase/phasecat.html",
                "attribution": "Moon Phase Predictions by Fred Espenak, NASA/GSFC",
                "resolution": "1 minute (UT) — dùng nguyên giá trị NASA công bố",
                "note": "Không dùng eraMoon98. Xem readiness Q2.",
            },
            "principalTerms": {
                "role": "DATA SOURCE",
                "name": "Sinh bằng ERFA eraEpv00 + eraEqec06 + eraNut06a",
                "license": "BSD-3-Clause (ERFA, NumFOCUS; phái sinh có phép từ IAU SOFA)",
                "attribution": "Dữ liệu tiết khí tạo bằng ERFA, thư viện phái sinh từ IAU SOFA",
                "meeusFree": True,
            },
            "deltaT": {
                "role": "DATA SOURCE",
                "name": 'Espenak & Meeus, "Polynomial Expressions for Delta T", NASA/GSFC',
                "url": "https://eclipse.gsfc.nasa.gov/SEcat5/deltatpoly.html",
                "publication": "phụ lục Five Millennium Canon of Solar Eclipses "
                               "(NASA/TP-2006-214141)",
                "variable": "y = year + (month - 0.5) / 12",
                "notUsed": "Cột ΔT của phases*.html — chỉ có độ phân giải PHÚT, "
                           "không đủ cho pipeline quyết định ở mức giây.",
                "knownLimitation": "Đoạn 2005-2050 là ngoại suy lập năm 2006; với thập "
                                   "niên 2020 đa thức cho ~75 s so với quan trắc ~69 s. "
                                   "Ảnh hưởng lên lịch đã được đo — xem "
                                   "docs/PHASE_3_DATASET_CORRECTION.md.",
                "rangeSeconds": [round(min(dt_used), 2), round(max(dt_used), 2)],
                "distinctValues": len(set(round(v, 6) for v in dt_used)),
            },
            "calendarRules": {
                "role": "ALGORITHM SOURCE",
                "rulesR1toR5": "Aslaksen, The Mathematics of the Chinese Calendar (NUS); "
                               "Explanatory Supplement to the Astronomical Almanac",
                "ruleR6meridian": "105°E — Quyết định 121-CP điều 1",
                "independentOfHoNgocDuc": True,
            },
            "validation": {
                "role": "VALIDATION SOURCE",
                "hko": "Bảng Dương-Âm 1901-2100 (lịch Trung Quốc) — chỉ đối chiếu, không vào APK",
                "government": "6150/TB-BLĐTBXH, 9441/TB-BNV",
                "nasaDeltaTColumn": "Cột ΔT phân giải phút của phases*.html — dùng làm "
                                    "kiểm chứng thô cho đa thức, trong verify_lunar_dataset.py",
            },
        },
        "boundaryScan": {
            "note": "Quét TOÀN BỘ sự kiện, không giới hạn ở các ca đã biết. Ranh giới "
                    "ngày Việt Nam = 17:00:00Z.",
            "windowSeconds": 120,
            "newMoonsNearBoundary": nm_boundary,
            "principalTermsNearBoundary": pt_boundary,
        },
    }
    with open(os.path.join(OUT_DIR, "vn_lunar_v1.json"), "w", encoding="utf-8") as f:
        json.dump(provenance, f, ensure_ascii=False, indent=2)
        f.write("\n")

    print(f"\nĐã ghi {bin_path}")
    print(f"  kích thước : {len(blob):,} byte")
    print(f"  sha256     : {digest}")
    print(f"  ΔT         : {min(dt_used):.2f} .. {max(dt_used):.2f} s, "
          f"{len(set(round(v,6) for v in dt_used))} giá trị phân biệt")
    print(f"  sát biên   : {len(nm_boundary)} Sóc, {len(pt_boundary)} trung khí")


if __name__ == "__main__":
    main()
