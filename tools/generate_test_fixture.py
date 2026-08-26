#!/usr/bin/env python3
"""
Sinh fixture cross-implementation cho engine lịch âm. Dev-only, ngoài app/.

CHUỖI BẰNG CHỨNG — đọc kỹ trước khi tin fixture này
---------------------------------------------------
    NASA/GSFC (điểm Sóc)  +  ERFA (trung khí)  +  ΔT Espenak/NASA
              └─ mô hình tham chiếu Python, quy tắc R1-R6 viết ĐỘC LẬP
                       └─ fixture này
                              └─ engine Kotlin phải tái lập đúng

Fixture KHÔNG đọc app/src/main/assets/lunar/vn_lunar_v1.bin. Nó đi thẳng từ dữ liệu
nguồn qua một hiện thực quy tắc KHÁC với hiện thực Kotlin. Vì vậy nó bắt được lỗi
chuyển ngữ sang Kotlin VÀ lỗi đọc/dựng chỉ số từ dataset.

Nó vẫn KHÔNG phải oracle độc lập về thiên văn: hai bên dùng chung nguồn NASA/ERFA đã
đóng băng. Bằng chứng ngoài nằm ở HKO và văn bản nhà nước, không ở đây.

Trước audit cuối Phase 3, file này sinh fixture TỪ CHÍNH dataset — nghĩa là chỉ kiểm
được việc đọc file, không kiểm được quy tắc. Đã sửa.
"""
import os
import sys
from datetime import date, timedelta

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(HERE, "reference_model"))
sys.path.insert(0, os.path.join(HERE, "benchmark_erfa_astronomy"))
sys.path.insert(0, HERE)
from build_inputs import load_nasa, erfa_solar_terms          # noqa: E402
from vn_lunar_reference import VietnameseLunarReference       # noqa: E402

OUT = os.path.join(HERE, "..", "app", "src", "test", "resources", "lunar_fixture_v1.tsv")
STEP = 37
SYNODIC = 29.530588853
PAD = 30


def main():
    nasa, dts = load_nasa()
    # Điểm Sóc lấy THẲNG từ NASA — đúng chính sách NASA-first của production.
    # Đệm hai đầu bằng chu kỳ trung bình, giống generator.
    moons = ([nasa[0] - timedelta(days=SYNODIC * k) for k in range(PAD, 0, -1)]
             + list(nasa)
             + [nasa[-1] + timedelta(days=SYNODIC * k) for k in range(1, PAD + 1)])
    terms = erfa_solar_terms(dts)
    ref = VietnameseLunarReference(moons, terms, 7.0)

    rows, leap = [], 0
    d, end = date(1901, 1, 1), date(2100, 12, 31)
    while d <= end:
        l = ref.to_lunar(d)
        leap += 1 if l.is_leap_month else 0
        rows.append(f"{d.isoformat()}\t{l.day}\t{l.month}\t{l.year}\t{1 if l.is_leap_month else 0}")
        d += timedelta(days=STEP)

    os.makedirs(os.path.dirname(OUT), exist_ok=True)
    with open(OUT, "w", encoding="utf-8") as f:
        f.write("# Fixture cross-implementation cho engine lịch âm Nếp Nhà\n")
        f.write("# KHÔNG phải oracle độc lập về thiên văn — xem đầu tools/generate_test_fixture.py\n")
        f.write("# Sinh bằng mô hình tham chiếu Python đi thẳng từ NASA + ERFA,\n")
        f.write("# KHÔNG đọc vn_lunar_v1.bin. Kiểm được quy tắc, không chỉ việc đọc file.\n")
        f.write(f"# bước lấy mẫu: {STEP} ngày, phạm vi 1901-2100\n")
        f.write("# gregorian\tlunarDay\tlunarMonth\tlunarYear\tisLeapMonth\n")
        f.write("\n".join(rows) + "\n")
    print(f"✓ {len(rows)} vector, {leap} vector rơi vào tháng nhuận -> {OUT}")


if __name__ == "__main__":
    main()
