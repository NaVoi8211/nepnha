#!/usr/bin/env python3
"""Chuẩn bị đầu vào thiên văn cho reference model. Dev-only, ngoài app/."""
import subprocess, sys, os
import os
import sys
from datetime import datetime, timedelta, timezone

# ΔT: dùng CHUNG module đã sửa ở audit cuối Phase 3. Trước đây file này lấy ΔT từ
# cột phân giải PHÚT của trang NASA, khiến mô hình tham chiếu mắc ĐÚNG lỗi mà
# dataset mắc — nên phép đối chiếu HKO không thể phát hiện ra.
# Xem docs/PHASE_3_DATASET_CORRECTION.md §E.
sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), ".."))
from deltat import delta_t_seconds                                     # noqa: E402
sys.path.insert(0, os.path.join(os.path.dirname(__file__), '..', 'benchmark_erfa_astronomy'))
from compare_with_nasa import fetch, parse, jd_tt_guess          # noqa: E402
from scan_all_newmoons import jd_to_datetime                     # noqa: E402

BENCH = os.environ.get("ERFA_BENCH", "/tmp/erfabuild/erfa_bench")
SUN_DEG_PER_DAY = 0.98564736


def jd_of(y, m, d):
    a = (14 - m) // 12; yy = y + 4800 - a; mm = m + 12 * a - 3
    return d + (153*mm+2)//5 + 365*yy + yy//4 - yy//100 + yy//400 - 32045 - 0.5


def load_nasa():
    moons, dts = [], {}
    for s in (1901, 2001):
        m, d = parse(fetch(s)); moons += m; dts.update(d)
    return moons, dts


def erfa_new_moons(nasa_moons, dts, pad_lunations=30):
    """Tính Sóc bằng ERFA, có ĐỆM thêm ở hai đầu.

    Vì tháng 11 âm được neo vào Đông chí, các tháng đầu 1901 và cuối 2100 thuộc về
    khoảng neo nằm ngoài dải NASA. Không đệm thì mô hình bỏ trống hai đầu — kiểm
    chứng cấu trúc với HKO đã phát hiện đúng lỗi này (1901: 1/12 tháng, 2100: 11/13).
    """
    SYN = 29.530588853
    guesses = [(delta_t_seconds(nm.year, nm.month),
                jd_tt_guess(nm, delta_t_seconds(nm.year, nm.month))) for nm in nasa_moons]
    first_dt, first_jd = guesses[0]
    last_dt, last_jd = guesses[-1]
    pre = [(first_dt, first_jd - SYN * k) for k in range(pad_lunations, 0, -1)]
    post = [(last_dt, last_jd + SYN * k) for k in range(1, pad_lunations + 1)]
    guesses = pre + guesses + post
    payload = "".join(f"{dt} {g:.9f}\n" for dt, g in guesses)
    out = subprocess.run([BENCH, "batch"], input=payload, capture_output=True,
                         text=True, check=True).stdout.split()
    return [jd_to_datetime(float(j), float(t)) for j, t in zip(out[0::2], out[1::2])]


def erfa_solar_terms(dts, y0=1899, y1=2103):
    """Tiết khí, sinh DƯ ở hai đầu.

    Tháng 11 âm được neo vào Đông chí, và một khoảng neo cần Đông chí ở CẢ HAI đầu.
    Không sinh dư thì các tháng cuối 2100 không được gán — kiểm chứng HKO đã bắt
    đúng lỗi này (2100: 11/13 tháng).
    """
    jobs = []
    for y in range(y0, y1):
        eq = jd_of(y, 3, 20)
        for deg in range(0, 360, 15):
            jobs.append((delta_t_seconds(y), deg, eq + deg / SUN_DEG_PER_DAY))
    payload = "".join(f"{dt} {deg} {g:.9f}\n" for dt, deg, g in jobs)
    out = subprocess.run([BENCH, "sunbatch"], input=payload, capture_output=True,
                         text=True, check=True).stdout.split()
    terms = []
    for (dt, deg, _), i in zip(jobs, range(0, len(out), 3)):
        terms.append((deg, jd_to_datetime(float(out[i]), float(out[i+1]))))
    return sorted(terms, key=lambda t: t[1])
