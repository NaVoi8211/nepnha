#!/usr/bin/env python3
"""
REFERENCE MODEL — KHÔNG PHẢI ENGINE CỦA APP.

Đây là mô hình đối chiếu phía DEVELOPMENT, viết bằng Python, nằm ngoài app/.
Nó KHÔNG được build vào APK, KHÔNG phải production code, và KHÔNG phải
`VietnameseLunarCalendar` của Phase 3.

Mục đích duy nhất: có một mô hình để ĐO, phục vụ Gate O1-O10 của Phase 3A.5.
Không có nó thì không thể đánh giá "engine khớp bao nhiêu %" với bất kỳ nguồn nào.

QUY TẮC LỊCH (provenance: Aslaksen, The Mathematics of the Chinese Calendar, NUS;
Explanatory Supplement to the Astronomical Almanac — ĐỘC LẬP với Hồ Ngọc Đức):
  R1  Ngày chứa thời điểm Sóc là mùng 1; ngày lấy trọn.
  R2  Mỗi điểm Sóc mở một tháng âm.
  R3  Đông chí (hoàng kinh Mặt Trời 270 độ) luôn nằm trong tháng 11.
  R4  Giữa hai tháng 11 liên tiếp mà có 13 tháng thì là năm nhuận; tháng nhuận là
      tháng ĐẦU TIÊN không chứa trung khí.
  R5  Trung khí = hoàng kinh Mặt Trời ở bội số 30 độ.
  R6  Việt Nam quy chiếu theo kinh tuyến 105 độ Đông (múi giờ 7) — Quyết định 121-CP
      điều 1. Trung Quốc dùng 120 độ Đông. ĐÂY LÀ KHÁC BIỆT DUY NHẤT giữa hai lịch.

Nguồn dữ liệu thiên văn:
  Sóc     — NASA/GSFC Six Millennium Catalog ("Moon Phase Predictions by Fred
            Espenak, NASA/GSFC"), hoặc ERFA (dev-only) để so sánh độ nhạy.
  Tiết khí— sinh bằng ERFA eraEpv00/eraEqec06/eraNut06a (nhánh KHÔNG dính Meeus),
            đã đối chiếu HKO 72/72 trong 38 giây.
"""
from __future__ import annotations

import bisect
from dataclasses import dataclass
from datetime import date, datetime, timedelta, timezone


@dataclass(frozen=True)
class LunarDate:
    day: int
    month: int
    year: int
    is_leap_month: bool = False

    def __str__(self) -> str:
        return f"{self.day}/{self.month}{'N' if self.is_leap_month else ''}/{self.year}"


class VietnameseLunarReference:
    """Mô hình tham chiếu. `utc_offset_hours` = 7 cho Việt Nam, 8 cho Trung Quốc."""

    def __init__(self, new_moons_utc, solar_terms_utc, utc_offset_hours: float = 7.0):
        """new_moons_utc: list[datetime] tăng dần. solar_terms_utc: list[(deg, datetime)]."""
        self.offset = timedelta(hours=utc_offset_hours)
        self.month_starts = sorted({(nm + self.offset).date() for nm in new_moons_utc})
        # Chỉ giữ trung khí (bội số 30 độ); ghi lại ngày địa phương của chúng.
        self.principal_terms = sorted(
            ((t + self.offset).date(), int(round(deg)))
            for deg, t in solar_terms_utc if int(round(deg)) % 30 == 0
        )
        self._term_days = [d for d, _ in self.principal_terms]
        self._solstice_days = sorted(d for d, deg in self.principal_terms if deg == 270)
        self._build()

    # ---------- dựng cấu trúc tháng ----------

    def _month_index(self, d: date) -> int:
        i = bisect.bisect_right(self.month_starts, d) - 1
        if i < 0:
            raise ValueError(f"{d} nằm trước mốc Sóc đầu tiên")
        return i

    def _has_principal_term(self, i: int) -> bool:
        """Tháng thứ i có chứa trung khí nào không."""
        lo = self.month_starts[i]
        hi = self.month_starts[i + 1]
        j = bisect.bisect_left(self._term_days, lo)
        return j < len(self._term_days) and self._term_days[j] < hi

    def _build(self) -> None:
        """Gán (số tháng, có nhuận không, năm âm) cho từng tháng."""
        self.month_meta: dict[int, tuple[int, bool, int]] = {}

        # R3: tháng chứa Đông chí là tháng 11.
        # Chỉ xét các Đông chí nằm TRONG dải Sóc đã có — tiết khí được sinh dư một
        # chu kỳ ở hai đầu nên có thể rơi ra ngoài.
        lo, hi = self.month_starts[0], self.month_starts[-1]
        m11 = sorted({self._month_index(d) for d in self._solstice_days if lo <= d < hi})

        for a, b in zip(m11, m11[1:]):
            span = b - a                       # số tháng giữa hai tháng 11 liên tiếp
            leap_at = None
            if span == 13:                     # R4: năm nhuận
                for k in range(a + 1, b):
                    if not self._has_principal_term(k):
                        leap_at = k
                        break
                if leap_at is None:            # phòng hờ: không tìm thấy
                    leap_at = b - 1

            # Năm âm: tháng 11 của năm Y bắt đầu trong năm dương Y.
            #
            # Quy ước đánh số tháng nhuận: tháng nhuận mang SỐ CỦA THÁNG LIỀN TRƯỚC
            # (nhuận tháng 2 = lần xuất hiện thứ hai của tháng 2), không phải số kế
            # tiếp. Bản đầu tiên của mô hình này đánh số sai lệch 1 — kiểm chứng cấu
            # trúc với HKO đã bắt được, xem docs/LUNAR_ONLINE_ANOMALIES.md.
            lunar_year = self.month_starts[a].year
            num, prev = 11, None
            for i in range(a, b):
                if i == leap_at and prev is not None:
                    self.month_meta[i] = (prev[0], True, prev[1])
                else:
                    self.month_meta[i] = (num, False, lunar_year)
                    prev = (num, lunar_year)
                    num += 1
                    if num == 13:
                        num = 1
                        lunar_year += 1

    # ---------- API ----------

    def to_lunar(self, g: date) -> LunarDate:
        i = self._month_index(g)
        if i not in self.month_meta:
            raise ValueError(f"{g} ngoài phạm vi đã dựng")
        num, is_leap, ly = self.month_meta[i]
        return LunarDate((g - self.month_starts[i]).days + 1, num, ly, is_leap)

    def to_solar(self, l: LunarDate) -> date:
        for i, (num, is_leap, ly) in self.month_meta.items():
            if num == l.month and is_leap == l.is_leap_month and ly == l.year:
                length = (self.month_starts[i + 1] - self.month_starts[i]).days
                if l.day > length:
                    raise ValueError(f"NONEXISTENT: tháng chỉ có {length} ngày")
                return self.month_starts[i] + timedelta(days=l.day - 1)
        raise ValueError(f"Không tìm thấy tháng âm {l}")

    def month_length(self, i: int) -> int:
        return (self.month_starts[i + 1] - self.month_starts[i]).days

    def leap_month_of(self, lunar_year: int):
        for i, (num, is_leap, ly) in self.month_meta.items():
            if ly == lunar_year and is_leap:
                return num
        return None

    def months_in_year(self, lunar_year: int) -> int:
        return sum(1 for _, _, ly in self.month_meta.values() if ly == lunar_year)

    def month_starts_in_year(self, gregorian_year: int):
        """[(ngày dương bắt đầu, số tháng, có nhuận)] cho các tháng mở trong năm đó."""
        out = []
        for i, (num, is_leap, _) in sorted(self.month_meta.items()):
            if self.month_starts[i].year == gregorian_year:
                out.append((self.month_starts[i], num, is_leap))
        return out
