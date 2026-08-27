# Nếp Nhà

Trợ lý nghi lễ gia đình Việt. **Offline-first, không backend, không tài khoản.**

> Đến đúng ngày → biết cần làm gì → chuẩn bị đúng → có đúng bài khấn → đọc/khấn dễ dàng.

## Trạng thái

**Phase 2 hoàn thành** — app shell + Family/Member lưu bằng Room.
**Phase 5 hoàn thành** — ngày giỗ theo lịch âm: tạo, quy đổi, hiện trên Nhà và Lịch. Trước đó: **Phase 4** — lịch âm hiển thị trên màn Nhà và màn Lịch. Trước đó: **Phase 3** — engine lịch âm Việt Nam (Kotlin thuần, offline). Trước đó: **Phase 3A.5** — bảng tiết khí 1901–2100 đã sinh & kiểm toàn vẹn. Engine lịch âm **vẫn chưa được phép viết** (BLOCKED).

## Build

Máy này chưa có JDK trên PATH; dùng JBR đi kèm Android Studio:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
./gradlew :app:assembleDebug
./gradlew :app:testDebugUnitTest
```

Cài lên máy thật (Samsung Galaxy A32 là thiết bị test chính):

```bash
~/Library/Android/sdk/platform-tools/adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Tài liệu

| File | Nội dung |
|---|---|
| [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) | Kiến trúc, manual DI, build config, quyết định & lý do |
| [docs/LUNAR_CALENDAR.md](docs/LUNAR_CALENDAR.md) | Chiến lược lịch âm Việt Nam + kế hoạch test (Phase 3) |
| [docs/PHASE_3_PREFLIGHT.md](docs/PHASE_3_PREFLIGHT.md) | **Nghiên cứu license/thuật toán/oracle trước khi viết engine** |
| [docs/PHASE_3A_ORACLE_GATE.md](docs/PHASE_3A_ORACLE_GATE.md) | **Cổng provenance/license — quyết định có được viết engine hay chưa** |
| [docs/PHASE_3A1_DATASET_VERIFICATION.md](docs/PHASE_3A1_DATASET_VERIFICATION.md) | Kiểm chứng dữ liệu NASA/HKO và múi giờ lịch sử bằng dữ liệu thật |
| [docs/PHASE_3A2_ASTRONOMICAL_PREFLIGHT.md](docs/PHASE_3A2_ASTRONOMICAL_PREFLIGHT.md) | Giấy phép & khả thi kỹ thuật của nguồn thiên văn (SOFA/ERFA/NASA) |
| [docs/PHASE_3_MEEUS_PROVENANCE.md](docs/PHASE_3_MEEUS_PROVENANCE.md) | **Quan hệ với Meeus — nêu đúng phạm vi** |
| [docs/PHASE_5_AUDIT.md](docs/PHASE_5_AUDIT.md) | **Audit hardening trước Phase 6** |
| [docs/PHASE_5_MEMORIAL.md](docs/PHASE_5_MEMORIAL.md) | **Ngày giỗ: quy tắc, quy đổi, lưu trữ** |
| [docs/PHASE_4_INTEGRATION.md](docs/PHASE_4_INTEGRATION.md) | **Nối lịch âm vào Home và màn Lịch** |
| [docs/PHASE_3_FINAL_HANDOFF.md](docs/PHASE_3_FINAL_HANDOFF.md) | **Bàn giao Phase 3 → Phase 4** |
| [docs/PHASE_3_DATASET_CORRECTION.md](docs/PHASE_3_DATASET_CORRECTION.md) | **Sửa dataset: ΔT + floor, pháp y ca 1938** |
| [docs/PHASE_3_FINAL_AUDIT.md](docs/PHASE_3_FINAL_AUDIT.md) | **Kiểm toán cuối Phase 3 — 1 blocker ΔT** |
| [docs/PHASE_3_IMPLEMENTATION.md](docs/PHASE_3_IMPLEMENTATION.md) | **Engine lịch âm — kiến trúc, kiểm thử, giới hạn** |
| [docs/LUNAR_API.md](docs/LUNAR_API.md) | **Hợp đồng API lịch âm** |
| [docs/LUNAR_DATASET_PROVENANCE.md](docs/LUNAR_DATASET_PROVENANCE.md) | **Provenance dataset thiên văn** |
| [docs/PHASE_3_IMPLEMENTATION_READINESS.md](docs/PHASE_3_IMPLEMENTATION_READINESS.md) | Freeze trước khi code |
| [docs/PHASE_3A5_ONLINE_CROSSCHECK.md](docs/PHASE_3A5_ONLINE_CROSSCHECK.md) | **Đối chiếu đa nguồn + Gate O1–O10 + quyết định CONDITIONAL GO** |
| [docs/LUNAR_ONLINE_CROSSCHECK_MATRIX.md](docs/LUNAR_ONLINE_CROSSCHECK_MATRIX.md) | Ma trận đối chiếu chi tiết |
| [docs/LUNAR_ONLINE_ANOMALIES.md](docs/LUNAR_ONLINE_ANOMALIES.md) | Nhật ký bất thường và phân loại discrepancy |
| [docs/PHASE_3A5_FINAL_PROVENANCE_GATE.md](docs/PHASE_3A5_FINAL_PROVENANCE_GATE.md) | **FINAL GATE — provenance, NASA-first, gate matrix G1–G16** |
| [docs/PHASE_3A5_ORACLE_CONSOLIDATION.md](docs/PHASE_3A5_ORACLE_CONSOLIDATION.md) | **Hợp nhất oracle, đính chính provenance, tiêu chí PASS khách quan** |
| [docs/PHASE_3A4_ONLINE_ORACLE.md](docs/PHASE_3A4_ONLINE_ORACLE.md) | **Chiến lược oracle trực tuyến + composite oracle 5 mức** |
| [docs/LUNAR_ONLINE_ORACLE_PROVENANCE.md](docs/LUNAR_ONLINE_ORACLE_PROVENANCE.md) | Provenance các lịch Việt Nam trực tuyến |
| [docs/PHASE_3A_NEXT_GATE.md](docs/PHASE_3A_NEXT_GATE.md) | Gate report + decision memo nhánh Mặt Trăng |
| [docs/PHASE_3A3_ASTRONOMICAL_BENCHMARK.md](docs/PHASE_3A3_ASTRONOMICAL_BENCHMARK.md) | Benchmark ERFA vs NASA/HKO — số đo thật |
| [docs/ASTRONOMICAL_BACKEND_DECISION.md](docs/ASTRONOMICAL_BACKEND_DECISION.md) | Quyết định kiến trúc backend thiên văn |
| [docs/ASTRONOMICAL_PROVENANCE.md](docs/ASTRONOMICAL_PROVENANCE.md) | Provenance từng nguồn thiên văn |
| [docs/HISTORICAL_TIME_MODEL.md](docs/HISTORICAL_TIME_MODEL.md) | Giờ dân sự vs múi giờ tính lịch qua các thời kỳ |
| [docs/LUNAR_ORACLE_PROVENANCE.md](docs/LUNAR_ORACLE_PROVENANCE.md) | Phân tích độc lập của các nguồn oracle |
| [docs/LUNAR_TEST_VECTORS.md](docs/LUNAR_TEST_VECTORS.md) | Bảng test vector lịch âm kèm nguồn |
| [docs/CONTENT_SCHEMA.md](docs/CONTENT_SCHEMA.md) | Schema `rituals/prayers/checklists` + template variables |
| [docs/ROOM_SCHEMA.md](docs/ROOM_SCHEMA.md) | Entity Room cho user data |
| [docs/MEMORIAL_RULES.md](docs/MEMORIAL_RULES.md) | Quy tắc nghiệp vụ ngày giỗ: tháng nhuận & ngày 30 |
| [docs/AUDIO.md](docs/AUDIO.md) | Chiến lược audio theo segment cho "Khấn cùng tôi" |
| [ROADMAP.md](ROADMAP.md) | Các phase + danh sách CỐ Ý KHÔNG làm trong MVP |
