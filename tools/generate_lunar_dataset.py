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
  ΔT       cột ΔT của chính catalog NASA

Đầu ra:
  app/src/main/assets/lunar/vn_lunar_v1.bin
  app/src/main/assets/lunar/vn_lunar_v1.json   (provenance, không đọc lúc chạy)
"""
import hashlib
import json
import os
import struct
import subprocess
import sys
from datetime import datetime, timedelta, timezone

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "benchmark_erfa_astronomy"))
from compare_with_nasa import fetch, parse, jd_tt_guess     # noqa: E402
from scan_all_newmoons import jd_to_datetime                # noqa: E402

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
    m = (dt - EPOCH).total_seconds() / 60.0
    return int(round(m))


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


def principal_terms(dts):
    jobs = []
    for y in range(FROM_YEAR - 2, TO_YEAR + 3):
        eq = jd_of(y, 3, 20)
        for deg in PRINCIPAL_DEGREES:
            jobs.append((dts.get(y, 69.0), deg, eq + deg / SUN_DEG_PER_DAY))
    payload = "".join(f"{dt} {deg} {g:.9f}\n" for dt, deg, g in jobs)
    out = subprocess.run([BENCH, "sunbatch"], input=payload, capture_output=True,
                         text=True, check=True).stdout.split()
    terms = []
    for (dt, deg, _), i in zip(jobs, range(0, len(out), 3)):
        terms.append(jd_to_datetime(float(out[i]), float(out[i + 1])))
    return sorted(terms)


def main():
    nasa, dts = load = None, None
    nasa, dts = [], {}
    for s in (1901, 2001):
        m, d = parse(fetch(s)); nasa += m; dts.update(d)
    print(f"NASA: {len(nasa)} điểm Sóc 1901-2100")

    moons = new_moons_from_nasa(nasa, dts)
    terms = principal_terms(dts)
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
        "internalPaddingLunations": PAD_LUNATIONS,
        "paddingIsNotSupportedRange": True,
        "newMoonCount": len(mm),
        "principalTermCount": len(tt),
        "sha256": digest,
        "sizeBytes": len(blob),
        "sources": {
            "newMoon": {
                "role": "DATA SOURCE",
                "name": "NASA/GSFC Six Millennium Catalog of the Phases of the Moon",
                "url": "https://eclipse.gsfc.nasa.gov/phase/phasecat.html",
                "attribution": "Moon Phase Predictions by Fred Espenak, NASA/GSFC",
                "resolution": "1 minute (UT)",
                "note": "Không dùng eraMoon98. Xem readiness Q2.",
            },
            "principalTerms": {
                "role": "DATA SOURCE",
                "name": "Sinh bằng ERFA eraEpv00 + eraEqec06 + eraNut06a",
                "license": "BSD-3-Clause (ERFA, NumFOCUS; phái sinh có phép từ IAU SOFA)",
                "attribution": "Dữ liệu tiết khí tạo bằng ERFA, thư viện phái sinh từ IAU SOFA",
                "meeusFree": True,
                "validatedAgainst": "HKO 24 solar terms 2026-2028, |lệch| <= 38 s",
            },
            "deltaT": {"role": "DATA SOURCE", "name": "cột ΔT của catalog NASA"},
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
            },
        },
        "precisionSensitive": [
            {"utcDate": "1944-06-20", "nasaUt": "17:00", "erfaUt": "16:59:52",
             "marginSeconds": 7.9, "chosen": "NASA", "flag": "PRECISION_SENSITIVE"},
            {"utcDate": "1967-07-07", "nasaUt": "17:00", "erfaUt": "16:59:41",
             "marginSeconds": 18.6, "chosen": "NASA", "flag": "PRECISION_SENSITIVE"},
            {"utcDate": "2054-05-07", "nasaUt": "17:00", "erfaUt": "17:00:06",
             "marginSeconds": 6.5, "chosen": "NASA", "flag": "PRECISION_SENSITIVE"},
            {"utcDate": "2072-12-09", "nasaUt": "16:59", "erfaUt": "16:59:09",
             "marginSeconds": 50.3, "chosen": "NASA", "flag": "PRECISION_SENSITIVE"},
            {"utcDate": "2077-11-15", "nasaUt": "17:00", "erfaUt": "16:59:25",
             "marginSeconds": 34.7, "chosen": "NASA", "flag": "PRECISION_SENSITIVE"},
            {"utcDate": "2085-10-18", "nasaUt": "17:00", "erfaUt": "16:59:22",
             "marginSeconds": 37.5, "chosen": "NASA", "flag": "PRECISION_SENSITIVE"},
        ],
        "precisionNote": "Cờ này KHÔNG được dùng để đổi kết quả và KHÔNG lộ ra API. "
                         "Chọn NASA thay ERFA làm đổi đúng 4 tháng âm trong 200 năm.",
        # Khối dưới đây trước kia được thêm tay vào JSON sau khi sinh, khiến file
        # không tái lập được từ script (audit A1). Đưa vào đây để `python3
        # tools/generate_lunar_dataset.py` sinh ra ĐÚNG file đã commit.
        # ⚠️ Danh sách này được suy ra từ bảng trung khí HIỆN TẠI. Xem
        # docs/PHASE_3_FINAL_AUDIT.md §B — bảng đó có lỗi ΔT, nên khối này phải
        # được sinh lại cùng lúc khi blocker được xử lý.
        "precisionSensitiveTerms": {
            "note": "Trung khí sát 00:00 giờ VN. Chỉ ca nằm ĐÚNG ranh giới tháng mới "
                    "có thể ảnh hưởng quy tắc tháng nhuận.",
            "withinTwoMinutesOfLocalMidnight": 7,
            "totalPrincipalTermsInRange": 2400,
            "couldChangeContainingMonth": [
                {"utc": "1938-09-23T17:00Z", "vnDate": "1938-09-24",
                 "reason": "rơi đúng ngày đầu tháng âm", "flag": "PRECISION_SENSITIVE_TERM"},
            ],
            "othersNoEffect": ["1924-06-21", "1938-01-20", "1953-06-21",
                               "2004-05-20", "2038-07-22", "2074-08-22"],
        },
        "quantisation": "Dataset lưu PHÚT nên có lượng tử hoá ±30 giây. Khi đối chiếu "
                        "với nguồn cũng làm tròn phút (HKO), chênh lệch quan sát được "
                        "có thể tới 60 giây mà không phải sai số thật.",
    }
    with open(os.path.join(OUT_DIR, "vn_lunar_v1.json"), "w", encoding="utf-8") as f:
        json.dump(provenance, f, ensure_ascii=False, indent=2)
        f.write("\n")

    print(f"\nĐã ghi {bin_path}")
    print(f"  kích thước : {len(blob):,} byte")
    print(f"  sha256     : {digest}")


if __name__ == "__main__":
    main()
