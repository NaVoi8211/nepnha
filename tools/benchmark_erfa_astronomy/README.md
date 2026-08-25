# benchmark_erfa_astronomy

Công cụ đo, **KHÔNG phải code của app**. Nằm ngoài `app/`, không bao giờ được build
vào APK. Không có tham chiếu nào tới `tools/` trong build script.

Mục đích: trả lời bằng số đo — ERFA có đủ chính xác để xác định **ngày âm Việt Nam**
1901–2100 hay không. Kết quả: [`docs/PHASE_3A3_ASTRONOMICAL_BENCHMARK.md`](../../docs/PHASE_3A3_ASTRONOMICAL_BENCHMARK.md).

## Chuẩn bị

```bash
curl -sL -o erfa.tar.gz https://github.com/liberfa/erfa/archive/refs/heads/master.tar.gz
tar xzf erfa.tar.gz && cd erfa-master/src
for f in *.c; do cc -O2 -I. -c "$f" -o "/tmp/erfabuild/${f%.c}.o"; done   # erfaversion.c sẽ lỗi, bỏ qua
ar rcs /tmp/erfabuild/liberfa.a /tmp/erfabuild/*.o
cc -O2 -I. ../../erfa_bench.c /tmp/erfabuild/liberfa.a -lm -o /tmp/erfabuild/erfa_bench
```

## Chạy

```bash
python3 compare_with_nasa.py      # Sóc, các năm bắt buộc, so NASA
python3 scan_all_newmoons.py      # quét cả 2.474 điểm Sóc 1901–2100
python3 compare_solar_terms.py    # 72 tiết khí 2026–2028, so HKO
```

## Nguyên tắc

- NASA và HKO là **oracle độc lập**, không phải input. **Không** hiệu chỉnh ERFA cho
  khớp chúng — chỉ đo chênh lệch.
- Điều quan trọng không phải "lệch bao nhiêu giây" mà **"lệch đó có đổi NGÀY ÂM
  không"**. Ranh giới ngày âm Việt Nam: 00:00 UTC+7 = **17:00:00 UTC**.

## Nguồn và giấy phép

| | |
|---|---|
| ERFA | BSD-3-Clause, NumFOCUS Foundation, phái sinh **có phép** từ IAU SOFA |
| NASA | *"Moon Phase Predictions by Fred Espenak, NASA/GSFC"* |
| HKO | data.gov.hk Terms of Use — cho phép thương mại, kèm attribution |

⚠️ `eraMoon98` là implementation **thuật toán Meeus** — vấn đề chính sách chưa quyết,
xem [PHASE_3A3 §4.2](../../docs/PHASE_3A3_ASTRONOMICAL_BENCHMARK.md).
