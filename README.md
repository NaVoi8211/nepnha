# Nếp Nhà

Trợ lý nghi lễ gia đình Việt. **Offline-first, không backend, không tài khoản.**

> Đến đúng ngày → biết cần làm gì → chuẩn bị đúng → có đúng bài khấn → đọc/khấn dễ dàng.

## Trạng thái

**Phase 2 hoàn thành** — app shell + Family/Member lưu bằng Room.
**Phase 3A.1 hoàn thành** — đã kiểm chứng dữ liệu NASA/HKO. Engine lịch âm **vẫn chưa được phép viết** (BLOCKED).

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
| [docs/LUNAR_ORACLE_PROVENANCE.md](docs/LUNAR_ORACLE_PROVENANCE.md) | Phân tích độc lập của các nguồn oracle |
| [docs/LUNAR_TEST_VECTORS.md](docs/LUNAR_TEST_VECTORS.md) | Bảng test vector lịch âm kèm nguồn |
| [docs/CONTENT_SCHEMA.md](docs/CONTENT_SCHEMA.md) | Schema `rituals/prayers/checklists` + template variables |
| [docs/ROOM_SCHEMA.md](docs/ROOM_SCHEMA.md) | Entity Room cho user data |
| [docs/MEMORIAL_RULES.md](docs/MEMORIAL_RULES.md) | Quy tắc nghiệp vụ ngày giỗ: tháng nhuận & ngày 30 |
| [docs/AUDIO.md](docs/AUDIO.md) | Chiến lược audio theo segment cho "Khấn cùng tôi" |
| [ROADMAP.md](ROADMAP.md) | Các phase + danh sách CỐ Ý KHÔNG làm trong MVP |
