#!/usr/bin/env python3
"""
Kiểm chứng ĐỘC LẬP dataset sản xuất. Dev-only, ngoài app/.

Đọc lại file nhị phân bằng một parser VIẾT RIÊNG (không dùng lại code sinh), rồi:
  1. Kiểm checksum, magic, header, tính đơn điệu, khoảng cách hợp lý
  2. Đối chiếu thời điểm Sóc với chính catalog NASA (nguồn gốc)
  3. Đối chiếu thời điểm trung khí với HKO 2026-2028 (nguồn NGOÀI, khác nhánh)
"""
import hashlib, json, os, re, struct, sys, urllib.request
from datetime import datetime, timedelta, timezone

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "benchmark_erfa_astronomy"))
from compare_with_nasa import fetch, parse                # noqa: E402

ASSET = os.path.join(HERE, "..", "app", "src", "main", "assets", "lunar")
EPOCH = datetime(1890, 1, 1, tzinfo=timezone.utc)
HKO_TERMS = "https://www.hko.gov.hk/en/gts/astronomy/data/files/24SolarTerms_{}.xml"


def read_dataset(path):
    blob = open(path, "rb").read()
    assert blob[:4] == b"NNLD", "magic sai"
    ver, y0, y1, nm, nt = struct.unpack(">HHHII", blob[4:18])
    off = 18
    moons = list(struct.unpack(f">{nm}I", blob[off:off + 4*nm])); off += 4*nm
    terms = list(struct.unpack(f">{nt}I", blob[off:off + 4*nt])); off += 4*nt
    assert off == len(blob), f"thừa {len(blob)-off} byte"
    return blob, ver, y0, y1, moons, terms


def dt(minutes):
    return EPOCH + timedelta(minutes=minutes)


def main():
    blob, ver, y0, y1, moons, terms = read_dataset(os.path.join(ASSET, "vn_lunar_v1.bin"))
    prov = json.load(open(os.path.join(ASSET, "vn_lunar_v1.json"), encoding="utf-8"))
    fails = 0

    def check(label, ok, detail=""):
        nonlocal fails
        print(f"  [{'OK ' if ok else 'FAIL'}] {label}{('  ' + detail) if detail else ''}")
        if not ok: fails += 1

    print("1. Cấu trúc và checksum")
    check("magic + header", (ver, y0, y1) == (1, 1901, 2100), f"v{ver} {y0}-{y1}")
    check("sha256 khớp provenance", hashlib.sha256(blob).hexdigest() == prov["sha256"])
    check("số lượng khớp provenance",
          len(moons) == prov["newMoonCount"] and len(terms) == prov["principalTermCount"])
    check("Sóc tăng đơn điệu", all(b > a for a, b in zip(moons, moons[1:])))
    check("trung khí tăng đơn điệu", all(b > a for a, b in zip(terms, terms[1:])))
    gm = [b - a for a, b in zip(moons, moons[1:])]
    check("khoảng Sóc trong [29.2, 29.9] ngày",
          all(29.2*1440 <= g <= 29.9*1440 for g in gm),
          f"min={min(gm)/1440:.3f} max={max(gm)/1440:.3f}")
    gt = [b - a for a, b in zip(terms, terms[1:])]
    check("khoảng trung khí trong [29, 32] ngày",
          all(29*1440 <= g <= 32*1440 for g in gt),
          f"min={min(gt)/1440:.3f} max={max(gt)/1440:.3f}")
    check("phủ hết phạm vi công bố",
          dt(moons[0]).year < 1901 and dt(moons[-1]).year > 2100,
          f"{dt(moons[0]).date()} .. {dt(moons[-1]).date()}")

    print("\n2. Đối chiếu Sóc với catalog NASA (nguồn gốc)")
    nasa, _ = [], {}
    for s in (1901, 2001):
        m, _d = parse(fetch(s)); nasa += m
    ds = {dt(v).replace(second=0, microsecond=0) for v in moons}
    missing = [x for x in nasa if x.replace(second=0, microsecond=0) not in ds]
    check(f"toàn bộ {len(nasa)} mốc NASA có trong dataset", not missing,
          f"thiếu {len(missing)}" if missing else "")

    print("\n3. Đối chiếu trung khí với HKO 2026-2028 (nguồn NGOÀI, khác nhánh)")
    worst = 0.0; n = 0
    for y in (2026, 2027, 2028):
        req = urllib.request.Request(HKO_TERMS.format(y), headers={"User-Agent": "Mozilla/5.0"})
        xml = urllib.request.urlopen(req, timeout=60).read().decode("utf-8", "replace")
        hko = [datetime(y, int(mo), int(d), int(h), int(mi),
                        tzinfo=timezone(timedelta(hours=8)))
               for mo, d, h, mi in re.findall(
                   r"<M>(\d+)</M><D>(\d+)</D><hm>(\d+):(\d+)</hm>", xml)]
        # HKO liệt kê cả 24 tiết khí; dataset chỉ có 12 trung khí (bội số 30 độ),
        # là các mục ở vị trí lẻ (chỉ số 1, 3, 5, ...) kể từ Tiểu Hàn (285 độ).
        hko_principal = hko[1::2]
        mine = [dt(v) for v in terms if dt(v).year == y]
        if len(mine) != len(hko_principal):
            check(f"{y}: số trung khí", False, f"hko={len(hko_principal)} ta={len(mine)}")
            continue
        for a, b in zip(hko_principal, mine):
            worst = max(worst, abs((b - a.astimezone(timezone.utc)).total_seconds())); n += 1
    check(f"{n} trung khí khớp HKO trong 60 giây", worst <= 60, f"|lệch| max = {worst:.0f}s")

    print(f"\n=> {'DATASET HỢP LỆ' if fails == 0 else f'{fails} LỖI'}")
    return 1 if fails else 0


if __name__ == "__main__":
    sys.exit(main())
