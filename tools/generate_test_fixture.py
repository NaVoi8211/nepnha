#!/usr/bin/env python3
"""
Sinh fixture kiểm thử cho engine Kotlin. Dev-only.

CHUỖI BẰNG CHỨNG — đọc kỹ trước khi tin fixture này:

  HKO (nguồn NGOÀI, nhánh HM Nautical Almanac Office)
      └─ đã đối chiếu 2.474/2.474 tháng với reference model @120E  (Phase 3A.5)
           └─ reference model, cùng quy tắc, đổi sang @105E
                └─ fixture này
                     └─ engine Kotlin phải tái lập

Fixture KHÔNG phải oracle độc lập. Nó là **cross-implementation check**: nó bắt lỗi
chuyển ngữ khi viết lại quy tắc sang Kotlin. Bằng chứng độc lập nằm ở tầng HKO và ở
các vector V1 từ văn bản nhà nước, được test riêng.

Fixture đọc CHÍNH dataset nhị phân mà app dùng, nên mọi khác biệt là lỗi Kotlin,
không phải khác biệt nguồn dữ liệu.
"""
import bisect, os, struct, sys
from datetime import datetime, timedelta, timezone, date

HERE = os.path.dirname(os.path.abspath(__file__))
BIN = os.path.join(HERE, "..", "app", "src", "main", "assets", "lunar", "vn_lunar_v1.bin")
OUT = os.path.join(HERE, "..", "app", "src", "test", "resources", "lunar_fixture_v1.tsv")
EPOCH = date(1890, 1, 1)
VN_OFFSET_MIN = 7 * 60
SAMPLE_STEP_DAYS = 37          # số nguyên tố ⇒ trải đều mọi vị trí trong tháng âm


def load():
    b = open(BIN, "rb").read()
    ver, y0, y1, nm, nt = struct.unpack(">HHHII", b[4:18]); off = 18
    moons = struct.unpack(f">{nm}I", b[off:off + 4*nm]); off += 4*nm
    terms = struct.unpack(f">{nt}I", b[off:off + 4*nt])
    return y0, y1, list(moons), list(terms)


def local_day(minutes):
    return (minutes + VN_OFFSET_MIN) // 1440


def build(moons, terms):
    starts = sorted(set(local_day(m) for m in moons))
    tdays = [local_day(t) for t in terms]
    idx = lambda d: bisect.bisect_right(starts, d) - 1

    def has_term(m):
        lo, hi = starts[m], starts[m + 1]
        i = bisect.bisect_left(tdays, lo)
        return i < len(tdays) and tdays[i] < hi

    m11 = []
    for i in range(len(tdays)):
        if i % 12 != 9:                     # 270 độ = Đông chí
            continue
        d = tdays[i]
        if d < starts[0] or d >= starts[-1]:
            continue
        k = idx(d)
        if not m11 or m11[-1] != k:
            m11.append(k)

    meta = {}
    for a, b in zip(m11, m11[1:]):
        leap_at = -1
        if b - a == 13:
            for m in range(a + 1, b):
                if not has_term(m):
                    leap_at = m; break
            if leap_at < 0:
                leap_at = b - 1
        num, ly, prev = 11, (EPOCH + timedelta(days=starts[a])).year, None
        for m in range(a, b):
            if m == leap_at and prev:
                meta[m] = (prev[0], True, prev[1])
            else:
                meta[m] = (num, False, ly)
                prev = (num, ly)
                num += 1
                if num == 13:
                    num, ly = 1, ly + 1
    return starts, meta, idx


def main():
    y0, y1, moons, terms = load()
    starts, meta, idx = build(moons, terms)

    rows = []
    d = date(y0, 1, 1)
    end = date(y1, 12, 31)
    while d <= end:
        k = idx((d - EPOCH).days)
        if k in meta:
            num, leap, ly = meta[k]
            day = (d - EPOCH).days - starts[k] + 1
            rows.append(f"{d.isoformat()}\t{day}\t{num}\t{ly}\t{'1' if leap else '0'}")
        d += timedelta(days=SAMPLE_STEP_DAYS)

    with open(OUT, "w", encoding="utf-8") as f:
        f.write("# Fixture cross-implementation cho engine lịch âm Nếp Nhà\n")
        f.write("# KHÔNG phải oracle độc lập — xem chú thích đầu tools/generate_test_fixture.py\n")
        f.write("# Sinh từ chính app/src/main/assets/lunar/vn_lunar_v1.bin\n")
        f.write(f"# bước lấy mẫu: {SAMPLE_STEP_DAYS} ngày, phạm vi {y0}-{y1}\n")
        f.write("# gregorian\tlunarDay\tlunarMonth\tlunarYear\tisLeapMonth\n")
        f.write("\n".join(rows) + "\n")
    print(f"Đã ghi {OUT}\n  {len(rows)} vector, {y0}-{y1}, bước {SAMPLE_STEP_DAYS} ngày")
    leap_rows = sum(1 for r in rows if r.endswith("\t1"))
    print(f"  trong đó {leap_rows} vector rơi vào tháng nhuận")


if __name__ == "__main__":
    main()
