#!/usr/bin/env python3
"""
Công cụ kiểm chứng dữ liệu — KHÔNG phải code của app.

Nằm ngoài `app/`, không được build vào APK, không phải production code.
Mục đích: bất kỳ ai cũng tái lập được các con số trong
`docs/PHASE_3A1_DATASET_VERIFICATION.md` thay vì phải tin lời tôi.

Việc nó làm:
  1. Tải Six Millennium Catalog of the Phases of the Moon (NASA/GSFC, Fred Espenak)
  2. Đếm số điểm Sóc 1901–2100, đối chiếu với kỳ vọng lý thuyết
  3. Liệt kê các điểm Sóc rơi sát 00:00 giờ Việt Nam — nhóm "fake precision"

Nguồn dữ liệu: https://eclipse.gsfc.nasa.gov/phase/phasecat.html
Ghi công bắt buộc khi dùng lại dữ liệu:
    "Moon Phase Predictions by Fred Espenak, NASA/GSFC"

Cách chạy:  python3 tools/verify_nasa_newmoons.py
"""
import html
import re
import urllib.request

BASE = "https://eclipse.gsfc.nasa.gov/phase/phases{}.html"
MONTHS = {m: i + 1 for i, m in enumerate(
    "Jan Feb Mar Apr May Jun Jul Aug Sep Oct Nov Dec".split())}

# Ranh giới ngày ở Việt Nam hiện nay: 00:00 UTC+7 == 17:00:00 UTC.
VN_DAY_BOUNDARY_UTC_MINUTES = 17 * 60


def fetch(year_start: int) -> str:
    with urllib.request.urlopen(BASE.format(year_start), timeout=60) as r:
        return r.read().decode("utf-8", "replace")


def parse_new_moons(page: str):
    """Đọc riêng cột New Moon bằng vị trí ký tự — cột khác dễ bị nhận nhầm."""
    text = html.unescape(re.sub(r"<[^>]+>", "", page))
    lines = text.splitlines()
    header = next(i for i, l in enumerate(lines)
                  if "New Moon" in l and "First Quarter" in l)
    lo = lines[header].index("New Moon") - 4
    hi = lines[header].index("First Quarter") - 2

    result, year = [], None
    for line in lines[header:]:
        m_year = re.match(r"\s*(\d{4})\s", line)
        if m_year:
            year = int(m_year.group(1))
        if year is None or len(line) < lo:
            continue
        m = re.search(r"([A-Z][a-z]{2})\s+(\d{1,2})\s+(\d{2}):(\d{2})", line[lo:hi])
        if m:
            result.append((year, MONTHS[m.group(1)], int(m.group(2)),
                           int(m.group(3)), int(m.group(4))))
    return result


def minutes_from_vn_boundary(hour: int, minute: int) -> int:
    delta = abs(hour * 60 + minute - VN_DAY_BOUNDARY_UTC_MINUTES)
    return min(delta, 1440 - delta)


def main() -> None:
    new_moons = []
    for start in (1901, 2001):
        page = fetch(start)
        chunk = parse_new_moons(page)
        print(f"phases{start}.html: {len(chunk)} điểm Sóc, "
              f"{chunk[0][0]}–{chunk[-1][0]}")
        new_moons += chunk

    expected = round(200 * 12.3685)  # 200 năm × số tuần trăng trung bình mỗi năm
    print(f"\nTổng 1901–2100: {len(new_moons)} (kỳ vọng lý thuyết ≈ {expected})")

    print("\nĐiểm Sóc sát 00:00 giờ Việt Nam — dữ liệu chỉ chính xác tới PHÚT "
          "nên các ca này không xác định chắc chắn được ngày:")
    for year, month, day, hour, minute in new_moons:
        delta = minutes_from_vn_boundary(hour, minute)
        if delta <= 3:
            mark = "  <-- ĐÚNG NỬA ĐÊM, KHÔNG XÁC ĐỊNH ĐƯỢC" if delta == 0 else ""
            print(f"  {year:4d}-{month:02d}-{day:02d}  {hour:02d}:{minute:02d} UT"
                  f"  lệch {delta} phút{mark}")


if __name__ == "__main__":
    main()
