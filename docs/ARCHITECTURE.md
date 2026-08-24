# Kiến trúc — Nếp Nhà

Chốt ở Phase 0. Mọi thay đổi phải cập nhật file này kèm lý do.

## 1. Tầng

```
Compose UI  (ui/)
    ↓  state: StateFlow   ↑  event: hàm gọi lên ViewModel
ViewModel   (ui/<feature>/)
    ↓
Repository  (data/repository/)
    ↓
Room (user data)  |  Assets JSON (fixed content)  |  DataStore (settings)
```

**Không có tầng UseCase mặc định.** Chỉ tạo class trong `domain/` khi một logic được
dùng ở ≥2 nơi hoặc phức tạp đủ để cần test riêng. Ba class chắc chắn sẽ tồn tại vì
đúng tiêu chí đó:

| Class | Package | Lý do tồn tại |
|---|---|---|
| `VietnameseLunarCalendar` | `core/lunar` | Thuật toán thuần, phải test độc lập, không được dính Android |
| `PrayerTemplateEngine` | `domain/prayer` | Nơi **duy nhất** render `{{biến}}` — chống copy/paste vào từng màn hình |
| `EventCalculator` | `domain/event` | Gộp nghi lễ cố định + ngày giỗ → timeline; dùng bởi Home, Lịch và Notification |

## 2. Dependency Injection — thủ công

`NepNhaApp` giữ một `AppContainer`; ViewModel nhận dependency qua `viewModelFactory`.

Không Hilt/Dagger/Koin. Lý do: app 4 tab, máy dev Intel 8GB RAM — annotation processor
của Hilt làm chậm mọi lần build và tốn thêm RAM daemon, đổi lại không có lợi ích thực
ở quy mô này. Nếu project phình lên nhiều module thì xem xét lại (ghi vào ROADMAP).

Không tự viết DI framework. `AppContainer` chỉ là các `val ... by lazy`.

## 3. Module

**Single Gradle module `:app`.** Trên máy 4 core / 8GB, mỗi module thêm là thêm một
lần configuration + classpath. Engine lịch âm vẫn testable nhanh vì nó là Kotlin thuần
trong `src/test` (JVM, không cần thiết bị).

Chỉ tách module nếu có lý do kỹ thuật thật (ví dụ build quá chậm đo được, hoặc cần
chia sẻ engine cho một app khác) — không tách vì "cho đẹp".

## 4. Cấu trúc package

```
com.nepnha
├── NepNhaApp.kt        Application + AppContainer (manual DI)
├── MainActivity.kt
├── core/lunar/         Lịch âm VN — Kotlin thuần, KHÔNG import android.*
├── core/time/          Tiện ích ngày tháng (java.time)
├── data/db/            Room: entity, DAO, database
├── data/assets/        Đọc & parse JSON trong assets/content
├── data/prefs/         DataStore settings
├── data/repository/    Repository
├── domain/model/       Model nghiệp vụ (Ritual, Prayer, UpcomingEvent…)
├── domain/prayer/      PrayerTemplateEngine
├── domain/event/       EventCalculator
├── notification/       AlarmScheduler, receiver
├── audio/              AudioPlayer (abstraction) + impl MediaPlayer
└── ui/                 theme, navigation, home, calendar, family, settings,
                        ritual, prayer
```

Ràng buộc kiểm tra được: **`core/lunar` không được import `android.*`.**

## 5. Cấu hình build

| Thiết lập | Giá trị | Lý do |
|---|---|---|
| AGP | 9.3.1 | Đã có trong cache máy dev |
| Gradle | 9.7.0 | Đã có trong cache; chạy tốt với JBR 25 |
| Kotlin | 2.3.21 | **AGP 9 có Kotlin built-in** — KHÔNG apply `org.jetbrains.kotlin.android` nữa (AGP 9 báo lỗi nếu apply) |
| compileSdk / targetSdk | 37 | Platform duy nhất mới có sẵn (API 36 không được cài) |
| minSdk | 26 | `java.time` native ⇒ **không cần core library desugaring**; Notification Channel là API 26 |
| JVM target | 17 | Biên dịch bằng JDK 25, target 17 |
| jvmToolchain | **không dùng** | Máy chỉ có JDK 25; khai toolchain 17 sẽ buộc Gradle tải JDK ⇒ cần mạng |
| buildFeatures | `compose=true`, tắt `buildConfig/aidl/shaders/resValues` | Ít task hơn ⇒ build nhanh hơn |
| Gradle daemon | `-Xmx1536m`, Kotlin daemon `-Xmx1024m`, `parallel=false` | Giữ tổng RAM dưới ~2.5GB để máy 8GB không swap |
| configuration-cache | bật | Rút ngắn vòng lặp build/debug |

### Xử lý API mới hơn minSdk 26

Bắt buộc `Build.VERSION.SDK_INT >= …` cho: `POST_NOTIFICATIONS` (33),
`canScheduleExactAlarms()` (31), `PendingIntent.FLAG_IMMUTABLE` (23 — luôn dùng),
hành vi foreground/exact alarm mới của API 34+. Lint sẽ bắt các trường hợp còn lại.

## 6. Quyền & offline

`AndroidManifest.xml` Phase 0: **không xin bất kỳ quyền nào.**

Không khai báo `android.permission.INTERNET` — đây là hàng rào cứng, không phải quy
ước: thiếu quyền thì mọi lời gọi mạng crash ngay lúc dev, nên không thể có hidden
network dependency. Kiểm chứng đã chạy ở Phase 0:

```
aapt2 dump permissions app-debug.apk
→ chỉ có com.nepnha.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION (AGP tự chèn, cục bộ)
```

`android:allowBackup="false"` — mặc định `true` sẽ đẩy dữ liệu người dùng lên Google
Backup, tức là **cloud**. Ràng buộc MVP là không cloud, nên tắt. Đổi lại: cài lại máy
là mất dữ liệu ⇒ ROADMAP có mục "export/import file cục bộ".

Quyền sẽ thêm ở Phase 8 (notification): `POST_NOTIFICATIONS`,
`RECEIVE_BOOT_COMPLETED`, `SCHEDULE_EXACT_ALARM`. Không dùng `USE_EXACT_ALARM`
(chính sách Google Play chỉ cho app đồng hồ/lịch).

## 7. Dependency — và những gì cố ý không dùng

Runtime hiện tại: core-ktx, activity-compose, Compose BOM (ui, ui-graphics,
tooling-preview, material3), lifecycle (runtime-ktx / runtime-compose /
viewmodel-compose), navigation-compose, room (runtime + ktx + compiler qua KSP),
datastore-preferences, kotlinx-serialization-json.

| Cố ý KHÔNG dùng | Thay bằng | Lý do |
|---|---|---|
| Media3 / ExoPlayer | `android.media.MediaPlayer` | Chỉ phát file ngắn cục bộ, tuần tự; ExoPlayer thừa dung lượng và RAM |
| WorkManager | `AlarmManager` + `BroadcastReceiver` | Cần đúng ngày, không cần job nền liên tục |
| Hilt / Dagger / Koin | `AppContainer` | Build time + RAM |
| Gson / Moshi | kotlinx-serialization | Trùng chức năng, đã có compiler plugin |
| Coil / Glide | drawable cục bộ | MVP không có ảnh remote |
| Bất kỳ SDK mạng/analytics/crash | — | Vi phạm hard constraint |

Thêm dependency mới ⇒ phải ghi lý do vào bảng này.
