#!/bin/bash
# Dựng lại erfa_bench — công cụ dev, KHÔNG vào APK.
# Trước đây binary chỉ nằm ở /tmp nên biến mất sau mỗi lần dọn /tmp; audit cuối
# Phase 3 gặp đúng chuyện đó. Script này khiến việc dựng lại là một lệnh.
set -euo pipefail
OUT="${ERFA_BUILD_DIR:-/tmp/erfabuild}"
SRC="$(cd "$(dirname "$0")" && pwd)/benchmark_erfa_astronomy"
mkdir -p "$OUT" && cd "$OUT"
if [ ! -d erfa-master ]; then
  curl -sL -o erfa.tar.gz https://github.com/liberfa/erfa/archive/refs/heads/master.tar.gz
  tar xzf erfa.tar.gz
fi
cd erfa-master/src
for f in *.c; do cc -O2 -I. -c "$f" -o "$OUT/${f%.c}.o" 2>/dev/null || true; done
rm -f "$OUT/erfaversion.o" "$OUT/t_erfa_c.o" "$OUT/t_erfa_c_extra.o"
ar rcs "$OUT/liberfa.a" "$OUT"/*.o
cc -O2 -I. "$SRC/erfa_bench.c" "$OUT/liberfa.a" -lm -o "$OUT/erfa_bench"
echo "✓ $OUT/erfa_bench"
