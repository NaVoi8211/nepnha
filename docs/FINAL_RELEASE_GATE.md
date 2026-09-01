# Final Release Gate — Nếp Nhà

Cổng cuối trước khi chủ dự án tự tạo upload keystore, ký AAB và đưa lên Google Play
Closed Testing. Đây là cổng kiểm toán và gia cố, **không phải phase làm tính năng**:
không notification, không AlarmManager, không WorkManager, không cloud, không analytics,
không dependency mới, không đổi `core/lunar`, không đổi dataset, không đổi Room schema.

Ngày kiểm: **28/08/2026**. Thiết bị: **Samsung Galaxy A32 (SM-A325F), Android 13, arm64-v8a**.
Máy build: MacBook Pro 2019 Intel i5, 8 GB RAM, JBR 25, AGP 9.3.1.

---

## KẾT LUẬN — **B. READY FOR SIGNING ONLY**

Mã nguồn và artifact đã đạt. Không còn blocker về **đúng/sai**, **mất dữ liệu**,
**riêng tư/bảo mật** hay **sập app**. Mọi việc còn lại nằm ngoài tầm với của mã nguồn:
một upload keystore và các khai báo trên Play Console — đều thuộc về chủ dự án và được
liệt kê ở cuối tài liệu.

Không chọn **A** vì hai điều kiện Play bắt buộc chưa tồn tại và không thể tự tạo thay
chủ dự án: **keystore phát hành** và **URL chính sách quyền riêng tư công khai**.

---

## 1. Bảng gate

| # | Gate | Kết quả | Bằng chứng | Cần sửa? |
|---|---|---|---|---|
| **A. Đúng/sai** |
| A1 | Quy đổi âm lịch 1901–2100 | **PASS** | 156 unit test, trong đó `LunarInvariantTest`, `LunarFixtureTest`, `LunarExternalVectorTest`, `LunarBoundaryRegressionTest` | không |
| A2 | Dataset SHA-256 | **PASS** | `b9f9613a…20f33d` trong repo **và** trong APK release | không |
| A3 | Tháng nhuận | **PASS** | `verify_lunar_dataset.py`: 75 năm nhuận trong dải, mọi năm 13 tháng đều có đúng một tháng thiếu trung khí | không |
| A4 | Tháng nhuận 1938 = tháng 8 | **PASS** | `verify_lunar_dataset.py` in ra `tháng nhuận thuộc năm 1938: [1938-09-24]`, tính từ nguyên tắc R1–R4 chứ **không** hardcode | không |
| A5 | Ca biên | **PASS** | quét biên ±120 s quanh 17:00:00Z: 8 sóc và 7 trung khí sát biên, không cái nào lệch ngày | không |
| A6 | Quy đổi ngày giỗ | **PASS** | `MemorialDateResolverTest`, `MemorialSearchWindowTest`; và trên máy thật: 30/7 âm → 10/9/2026 | không |
| A7 | Ngày thiếu 30 → 29 | **PASS** | trên bản RELEASE: "Tháng 7 âm năm nay không có ngày 30… tính vào ngày 29", đối chiếu độc lập với lưới lịch (11/9 = 1/8 ⇒ tháng 7 chỉ có 29 ngày) | không |
| A8 | Ba policy tháng nhuận | **PASS** | trên bản RELEASE: `LEAP_MONTH_ONLY` → 20/9/2044; `LEAP_MONTH_PREFERRED` → 10/9/2026 kèm **cả hai** lời giải thích độc lập | không |
| A9 | Liên kết `memberId` | **PASS** | đổi tên thành viên ⇒ tên trên ngày giỗ **đi theo** | không |
| A10 | `ON DELETE SET NULL` | **PASS** | xoá thành viên ⇒ ngày giỗ **vẫn còn**, chỉ đứt liên kết | không |
| A11 | Tên hiển thị dự phòng | **PASS** | sau khi xoá thành viên, ngày giỗ rơi về **tên đã lưu** ("Nguyen Van A") chứ không trống | không |
| A12 | "Hôm nay" làm mới qua `ON_RESUME` | **PASS** | `MidnightLifecycleTest` với `DateProvider` giả: ngày dương đổi 10/9 → 11/9 sau STOP/RESUME | không |
| A13 | `daysUntil`: 0 = Hôm nay, 1 = Ngày mai, không âm | **PASS** | `bang_dem_nguoc_dung_o_moi_khoang_cach` kiểm cả ba nhánh **và** khẳng định "Còn 0 ngày"/"Còn 1 ngày" không tồn tại ở bất cứ đâu | không |
| A14 | Ngoài dải hỗ trợ | **PASS** | `LunarBoundaryAndErrorTest`, `CalendarViewModelTest` (`OUT_OF_SUPPORTED_RANGE`) | không |
| A15 | Không nhánh nullable nào hiện ngày sai | **PASS** | `LunarDay` là sealed: `Known`/`Unknown(reason)`, không có ngày nullable; `MemorialResolution` sealed, `Skipped` **không có** ngày dương | không |
| **B. An toàn dữ liệu** |
| B1 | Migration v1→v2, v2→v3, v1→v2→v3 | **PASS** | 4 test trên **file SQLite thật**, gồm ca nhiều bản ghi chứng minh id không bị đánh số lại | không |
| B2 | Xuất/nhập | **PASS** | 25 unit test codec + 11 instrumented + kiểm tay trên bản release | không |
| B3 | Nhập: kiểm tra **toàn bộ** trước transaction | **PASS** | `BackupCodec.decode` trả `Invalid` với **danh sách** lỗi trước khi ViewModel chạm database | không |
| B4 | Nhập hỏng ⇒ database không đổi | **PASS** | `rollback_toan_bo_tren_bo_du_lieu_lon` (lỗi ở bản ghi 100/180) + 4 loại file lỗi trên máy thật | không |
| B5 | Chỉ-thêm, không ghi đè ngầm | **PASS** | `nhap_vao_may_da_co_du_lieu_chi_them_khong_xoa`; màn xem trước nói thẳng | không |
| B6 | Enum xuất ra dùng hằng chuỗi cố định | **PASS** | file xuất từ **bản minify**: `"female"`, `"common_month"`, `"last_valid_day"` | không |
| B7 | R8 không đổi ngữ nghĩa lưu trữ | **PASS** | `check_release_mapping.py` 3 lớp + 4 lỗi tiêm vào đều bị bắt | không |
| B8 | File sao lưu không mất `memberId`/policy/ngày sinh âm/tín chủ | **PASS** | xoá sạch dữ liệu rồi khôi phục: thành viên, ngày sinh âm 26/1/1950, **tín chủ**, ngày giỗ 10/9/2026 "Còn 13 ngày" — khớp hoàn toàn | không |
| B9 | Không log dữ liệu gia đình/ngày giỗ | **PASS** | logcat sau toàn bộ phiên: 0 dòng chứa tên người; dòng duy nhất mang tên `com.nepnha` là thông điệp **GC của ART** | không |
| B10 | `allowBackup=false` | **PASS** | đọc từ manifest đã ghép trong APK | không |
| **C. Release / R8** |
| C1 | `assembleRelease` / `bundleRelease` | **PASS** | clean + `--no-build-cache`, 3 lần đều thành công | không |
| C2 | minify / shrinkResources | **PASS** | `isMinifyEnabled=true`, `isShrinkResources=true`; tài nguyên đã đổi tên (`icon='res/BW.xml'`) | không |
| C3 | `mapping.txt` | **PASS** | sinh ra, **và được đóng gói vào AAB** (`BUNDLE-METADATA/…/proguard.map`) ⇒ Play giải mã được crash | không |
| C4 | Mọi enum lưu xuống database | **PASS** | 10 enum / 28 hằng quét từ nguồn, không cái nào bị đổi tên | không |
| C5 | Mọi `enum.name` qua ranh giới lưu trữ | **PASS** | 13 chỗ, khớp đúng ảnh chụp đã ghi nhận | không |
| C6 | ProGuard rules | **PASS** | rule giữ tên enum còn nguyên (có kiểm tự động) | không |
| C7 | applicationId / versionCode / versionName | **PASS** | `com.nepnha` / `1` / `0.1.0-mvp`, đọc từ artifact | không |
| C8 | Code debug/test không lọt AAB | **PASS** | 2 hàm `@Preview` **và** ngày cứng của chúng không còn trong `classes.dex`; `DebugProbesKt.bin` đã loại từ Phase 7.5 | không |
| C9 | TODO / FIXME | **PASS** | 0 trong `app/src/main` | không |
| C10 | Log / println | **PASS** | 0 trong `app/src/main` | không |
| C11 | Test fixture trong asset production | **PASS** | không có; xem giới hạn về `vn_lunar_v1.json` | không |
| C12 | Secret / keystore trong repo | **PASS** | 0 kết quả; `.gitignore` chặn `keystore.properties`, `*.jks`, `*.keystore` | không |

---

## 2. Manifest và quyền — đọc từ **artifact**, không phải từ source

Toàn bộ `<application>` chỉ có **4 component**, **2 `uses-library`** (đều `required=false`)
và **3 `meta-data`**:

| Component | exported | Ghi chú |
|---|---|---|
| `com.nepnha.MainActivity` | `true` | bắt buộc — chỉ có `MAIN`/`LAUNCHER`, không có `VIEW`/`BROWSABLE`/scheme/host |
| `androidx.startup.InitializationProvider` | `false` | |
| `androidx.room.MultiInstanceInvalidationService` | `false` | |
| `androidx.profileinstaller.ProfileInstallReceiver` | `true` | của AndroidX, **chặn bằng `android:permission="android.permission.DUMP"`** (chỉ hệ thống/shell gọi được) |

### Phân biệt hai loại quyền, đúng như yêu cầu

**Quyền app THỰC SỰ REQUEST** (`<uses-permission>`) — đúng **một**:

```
com.nepnha.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION   protectionLevel = signature
```

Đây là quyền **do chính app tự khai báo, mức chữ ký**, `androidx.core` sinh ra để đăng ký
`BroadcastReceiver` không-exported khi `targetSdk ≥ 33`. Không app nào khác dùng được nó.

**Quyền chỉ xuất hiện như hàng rào của component** (`android:permission` trên `<receiver>`),
**không** phải app xin: `android.permission.DUMP`.

Bằng chứng ở mức artifact: giải mã protobuf manifest trong **AAB**, chuỗi
`android.permission.*` **duy nhất** trong toàn bộ file là `android.permission.DUMP`, và
nó không nằm trong một `<uses-permission>` nào.

| Hạng mục | Kết quả |
|---|---|
| INTERNET | **không có** |
| Foreground service | 0 (`foregroundServiceType` không xuất hiện) |
| Notification (`POST_NOTIFICATIONS`) | không có |
| Exact alarm | không có |
| Storage | không có — dùng SAF nên không cần |
| Location / Contacts / Camera / Microphone | không có |
| Accessibility service | 0 |
| `<queries>` (package visibility) | 0 phần tử |
| App links / deep links | 0 (`scheme`, `host`, `BROWSABLE` đều không xuất hiện) |
| `allowBackup` | `false` |
| `dataExtractionRules` / `fullBackupContent` / `backupAgent` | không khai báo (vô nghĩa khi `allowBackup=false`) |
| `debuggable` / `testOnly` | không có |
| `extractNativeLibs` | `false` (cần cho căn chỉnh 16 KB) |

---

## 3. Target API — kiểm ba nguồn độc lập

| Nguồn | minSdk | targetSdk |
|---|---|---|
| `app/build.gradle.kts` | 26 | **37** |
| Manifest đã ghép trong APK (`aapt2 dump xmltree`) | 26 | **37** |
| Manifest protobuf trong AAB (giải mã trực tiếp) | attr `0x0101020c` = 26 | attr `0x01010270` = **37** |

Google Play từ **31/08/2026** yêu cầu app mới target **API 36+**. Target 37 **hợp lệ và
cao hơn yêu cầu**.

Đã kiểm thêm một điều dễ bị bỏ qua: **API 37 là SDK chính thức, không phải bản xem trước**
— `source.properties` của platform ghi `AndroidVersion.CodeName=` rỗng,
`PreviewSdkInt=0`, `BetaVersion=` rỗng, `IsBaseSdk=true`. Play **không nhận** AAB build
bằng SDK xem trước, nên điểm này phải kiểm chứ không được suy đoán.

**Không hạ target xuống 36.** Không phát hiện rủi ro tương thích nào của API 37 trong
phạm vi kiểm (app chạy đủ chức năng trên Android 13/API 33 là thiết bị thấp hơn target
rất nhiều, và không dùng API nào bị đổi hành vi).

---

## 4. 16 KB page size — **PASS**

Không tin báo cáo cũ; đo lại từ đầu trên **cả APK lẫn AAB**.

| Kiểm | Kết quả |
|---|---|
| `zipalign -c -P 16 -v 4` trên APK | **Verification successful**, cả 8 `.so` báo `(OK)` |
| ELF `p_align` của mọi `PT_LOAD` | **16/16 file** (8 trong APK + 8 trong AAB) đều `0x4000` |
| `extractNativeLibs` | `false` |

Thư viện native chỉ có hai, đều của AndroidX, không phải mã của Nếp Nhà:
`libandroidx.graphics.path.so` (Compose) và `libdatastore_shared_counter.so` (DataStore),
mỗi cái cho 4 ABI. AAB tách theo thiết bị nên người dùng chỉ tải phần của mình.

Không có dependency nào không đạt.

---

## 5. Riêng tư — mô tả chính xác để viết Privacy Policy

### Dữ liệu app LƯU (sandbox riêng của app, không app khác đọc được)

| Bảng | Trường |
|---|---|
| `families` | `name`, `createdAt`, `updatedAt` |
| `members` | `fullName`, `gender`, `solarBirthDate`, `lunarBirthDay/Month/Year`, `lunarBirthIsLeapMonth`, `lunarBirthSource`, `role`, `note`, timestamps |
| `memorials` | `name`, `memberId`, `lunarDay`, `lunarMonth`, `leapMonthPolicy`, `missingDayPolicy`, `note`, timestamps |

DataStore: đúng **một** khoá — `primary_member_id` (một số).

### Dữ liệu app XUẤT ra file

Chỉ khi người dùng **tự bấm** "Xuất dữ liệu" và **tự chọn** nơi lưu qua bộ chọn file của
Android. Nội dung đúng bằng hợp đồng `docs/PHASE_7_EXPORT_IMPORT.md`: tên gia đình, tín
chủ, thành viên, ngày giỗ. **Không** chứa id nội bộ của Room, **không** chứa dữ liệu lịch
đã tính, **không** chứa thông tin thiết bị.

### Dữ liệu app NHẬP từ file

Chỉ khi người dùng tự chọn file. Có màn xem trước và phải xác nhận trước khi ghi.

### Những gì app **KHÔNG** làm

| | |
|---|---|
| Gửi qua mạng | **Không thể** — APK không khai báo `INTERNET`. Không có HTTP client, không có URL endpoint nào trong dex |
| Analytics | không có SDK nào (0 dòng khớp `firebase\|crashlytics\|analytics\|admob\|gms\|okhttp\|retrofit\|sentry\|…` trong cây phụ thuộc release) |
| Crash telemetry | không có |
| AI / cloud | không có |
| Tài khoản | không có |
| Ghi ra ngoài sandbox | chỉ qua `Uri` người dùng chọn; không `Environment.*`, không `getExternalFilesDir` |
| Ghi log nội dung | không — mã production không có `Log.*`, `println`, `printStackTrace` nào |

### Điều **KHÔNG được** viết quá lên trong policy

Không viết *"we never collect any data under any circumstances"* hay *"app never contacts
anything"*. Căn cứ vào artifact, có hai điểm phải nói cho đúng:

1. **`androidx.emoji2`** (do Compose kéo vào) chạy `EmojiCompatInitializer` lúc khởi động
   và hỏi **nhà cung cấp font của hệ thống** một font emoji. Đó là IPC sang app khác trên
   máy, chạy bằng quyền của app đó; nội dung yêu cầu là **chuỗi truy vấn font**, không có
   dữ liệu người dùng. Nếu máy không có nhà cung cấp font, có sẵn nhánh
   *"EmojiCompat font provider not available on this device."* Nếp Nhà **tự nó** không gửi
   được gì vì không có quyền `INTERNET`.
2. **Artifact có nhúng git commit SHA** (`META-INF/version-control-info.textproto`, do AGP
   sinh). Đó là metadata của người phát triển, không phải dữ liệu người dùng.

### Câu chữ đề xuất cho Data safety trên Play

* Thu thập dữ liệu: **Không**.
* Chia sẻ dữ liệu: **Không**.
* Dữ liệu được mã hoá khi truyền: **không áp dụng** — app không truyền dữ liệu đi đâu.
* Người dùng có thể yêu cầu xoá dữ liệu: **Có** — gỡ app hoặc Xoá dữ liệu là xoá hết; app
  không có bản sao ở nơi nào khác.
* Có tài khoản: **Không** ⇒ yêu cầu "xoá tài khoản" của Play **không áp dụng**.
* Quảng cáo: **Không**.

Nói thẳng trong policy rằng **file sao lưu do người dùng tự xuất là văn bản thường, KHÔNG
mã hoá** — màn Cài đặt trong app đã nói đúng như vậy.

---

## 6. Tái lập bản build — và một đính chính

Build sạch (`clean` + `--no-build-cache`) **ba lần** hôm nay, tất cả tại commit `70198da`:

| Lần | APK SHA-256 | AAB SHA-256 |
|---|---|---|
| 1 | `1505fbbf…a87a21cb` | `d1277a92…3a17d452` |
| 2 | `1505fbbf…a87a21cb` | `d1277a92…3a17d452` |
| 3 | `1505fbbf…a87a21cb` | `d1277a92…3a17d452` |

`mapping.txt` của lần 1 và lần 2 cũng **giống hệt** (`c9ef11d8…`) ⇒ R8 đặt tên có tất định.

### Đính chính báo cáo Phase 7.5

`docs/PHASE_7_5_RELEASE_AUDIT.md` ghi hash `f9e21db7…` / `2c638543…` và khẳng định bản
release "tái lập được". **Bằng chứng khi đó không hợp lệ**: tôi so hai lần build mà một
lần được Gradle build cache phục hồi, tức không phải hai lần dựng độc lập.

Kết luận đúng, đã truy ra nguyên nhân chính xác: AGP nhúng git SHA vào
`META-INF/version-control-info.textproto`, và file đó chứa đúng
`revision: "70198daf45ff…"`. Vì SHA-1 dài cố định nên **kích thước artifact không đổi
(1 766 674 B / 4 260 795 B) mà hash đổi theo commit**.

> **Cùng commit ⇒ artifact byte-identical. Khác commit ⇒ hash đổi theo thiết kế.**

Hệ quả thực tế: hash trong tài liệu này chỉ đúng cho commit `70198da`. Sau bất kỳ commit
nào — kể cả commit thêm chính tài liệu này — phải build lại để có hash mới. Đây **không**
phải lỗi và không cần sửa cho việc phát hành lên Play.

---

## 7. Ma trận test cuối

Mọi lệnh chạy từ trạng thái `clean`, và các bộ test chạy với `--no-build-cache` để bảo đảm
test **thực sự thực thi** chứ không phải kết quả được cache phục hồi.

| Lệnh | Kết quả |
|---|---|
| `./gradlew clean assembleDebug` | BUILD SUCCESSFUL — `app-debug.apk` 13 654 166 B |
| `./gradlew clean testDebugUnitTest` | **156 test / 19 lớp — 0 fail** (chạy lại 3 lần liên tiếp đều 156/0) |
| `./gradlew connectedDebugAndroidTest` | **73 test / 14 lớp — 0 fail** (SM-A325F, 2 lần liên tiếp) |
| `./gradlew clean assembleRelease` | BUILD SUCCESSFUL — 1 766 674 B |
| `./gradlew clean bundleRelease` | BUILD SUCCESSFUL — 4 260 795 B |
| `tools/verify_lunar_dataset.py` | **DATASET HỢP LỆ** |
| `tools/test_generator.py` | **TẤT CẢ ĐỀU QUA** |
| `tools/check_release_mapping.py` | **PASS** — 3 lớp kiểm, và bắt được cả 4 lỗi tiêm vào |
| `./gradlew clean lintDebug` | **0 error**, 23 warning |

23 warning của lint, không cái nào là blocker: 8 `UnusedResources`, 5 `PluralsCandidate`,
2 `AndroidGradlePluginVersion`, 2 `NewerVersionAvailable`, 2 `MonochromeLauncherIcon`,
1 `RedundantLabel`, 1 `GradleDependency`, 1 `DataExtractionRules`, 1 `ObsoleteSdkInt`.

### Test mới thêm trong cổng này (3)

`RotationStateTest` — chạy trên **`MainActivity` thật**, không phải `ComponentActivity` +
`rule.setContent`. Khác biệt quan trọng: `setContent` gọi từ test gắn vào Activity **cũ**,
nên sau `recreate()` phải gọi lại bằng tay, và khi đó ta đang kiểm một đường mà app thật
không đi. `MainActivity` tự gọi `setContent` trong `onCreate`, nên `scenario.recreate()`
chạy đúng dòng đời xoay máy thật.

* `man_lich_giu_nguyen_thang_va_ngay_dang_chon_sau_khi_dung_lai`
* `form_ngay_gio_chua_luu_khong_bi_mat_sau_khi_dung_lai`
* `ngay_tren_man_nha_van_dung_sau_khi_dung_lai`

---

## 8. Lỗi tìm được trong cổng này

### 8.1 Hai lỗi trong hạ tầng test — đã sửa

**(a) Một test giao diện kết thúc giữa chừng, làm chết lớp test kế tiếp.**
`BackupFlowTest.nhap_du_lieu_phai_xem_truoc_roi_moi_ghi` chờ database đổi rồi kết thúc.
Nhưng Room phát tín hiệu thay đổi **trước** khi ViewModel kịp đặt `busy = false`, nên test
dừng lại đúng lúc `LinearProgressIndicator` — một animation **vô hạn** — còn đang quay.
Composition tiếp tục xin khung hình, main looper không bao giờ idle, và **lớp test chạy
sau** chết với `AppNotIdleException` — một lỗi trông như thể nằm ở chỗ hoàn toàn khác.

Sửa: chờ đúng tín hiệu **người dùng nhìn thấy** — hộp thoại "Đã thêm…" — rồi mới khẳng
định. Chặt hơn bản cũ, vì giờ nó kiểm luôn cả việc app có báo cho người dùng biết đã xong.

**(b) `TestEnvironment.close()` không huỷ scope của DataStore.**
Scope được tạo ngay trong lời gọi `PreferenceDataStoreFactory.create` và không giữ tham
chiếu, nên mỗi test để lại một `CoroutineScope` sống mãi kèm coroutine theo dõi file. Một
bộ test dài tích lại hàng chục cái — vô hình cho tới khi máy bắt đầu chậm. Sửa: giữ tham
chiếu và `cancel()` trong `close()`.

**Cả hai đều là lỗi trong test, không phải lỗi sản phẩm.** Thanh tiến trình quay khi đang
bận là hành vi đúng.

### 8.2 Ba lần test đỏ khi viết test xoay máy — không lần nào sửa test cho xanh

| Lỗi | Nguyên nhân | Xử lý |
|---|---|---|
| `calendar_selected` đọc ra chuỗi rỗng | tag gắn trên một `InfoCard` — container **không gộp semantics**, nên node đó không có thuộc tính `Text` nào. Nếu không phát hiện, phép so sánh thành "rỗng bằng rỗng": một test **luôn xanh mà chẳng chứng minh gì** | gom text đệ quy xuống cây con |
| `memorial_name` so sánh hụt | cách gom đệ quy nhặt luôn nhãn ô ("Tên người mất") | tách hàm riêng chỉ đọc `EditableText` của ô nhập |
| 4 test báo "No compose hierarchies found" | **lỗi của người kiểm**: tôi cho máy ngủ để hạ nhiệt, `KEYCODE_WAKEUP` không mở được khoá màn hình | `wm dismiss-keyguard` trước khi chạy |

### 8.3 Nhiễu từ thiết bị — ghi lại để không bị hiểu nhầm là lỗi sản phẩm

Trong quá trình điều tra, A32 leo lên **Thermal Status 3 (SEVERE, 39 °C)**. Ở mức đó
Android bóp CPU/GPU rất mạnh: cùng một bộ test chạy **10 phút 35** thay vì **2 phút 46**,
và Espresso vượt ngưỡng idle 60 giây. Sau khi máy nguội về Status 0, bộ test đầy đủ chạy
**73/73 trong 2 phút 41**, hai lần liên tiếp.

### 8.4 Một lỗi **CHƯA XÁC ĐỊNH NGUYÊN NHÂN** — P2

`CalendarViewModelTest > chon ngay o thang khac thi luoi doi theo` **fail đúng một lần**
trong một lần chạy đầy đủ bộ unit test.

Điều biết chắc:

* chạy riêng lớp đó: **PASS**;
* chạy lại toàn bộ bộ unit test **3 lần liên tiếp**: 156/156, 0 fail (468 lượt thực thi);
* **thông báo lỗi đã bị mất**: lệnh `clean lintDebug` chạy ngay sau đó xoá
  `app/build/test-results` trước khi tôi kịp đọc. Đây là **sai sót của người kiểm**.

Vì không có thông báo lỗi và không tái hiện được, **không kết luận nguyên nhân**. Một giả
thuyết chưa kiểm chứng được: `CalendarViewModelTest` nằm chung file với
`CalendarMemorialMarkerTest`, và lớp sau gọi `Dispatchers.setMain` / `resetMain` — tức là
sửa trạng thái **toàn cục của JVM** mà cả hai lớp cùng chia sẻ. Không sửa gì theo giả
thuyết chưa chứng minh.

Nếu tái diễn: chạy `./gradlew --no-build-cache --rerun-tasks testDebugUnitTest` và **đọc
`app/build/test-results/testDebugUnitTest/*.xml` NGAY**, trước bất kỳ lệnh `clean` nào.

---

## 9. Kiểm tay trên SM-A325F, bản RELEASE

APK release ký bằng `~/.android/debug.keystore` **chỉ để cài thử** (chưa có keystore phát
hành). Cài mới hoàn toàn, dữ liệu trống.

| # | Việc | Kết quả |
|---|---|---|
| 1 | cài mới | ✅ |
| 2 | mở app | ✅ không crash |
| 3 | ngày âm hôm nay | 28/8/2026 → **16 tháng 7 năm Bính Ngọ** |
| 4 | mở Lịch | ✅ lưới tháng 8/2026 |
| 5 | lật tháng | tiến 2 → Tháng 10; lùi 1 → Tháng 9 ✅ |
| 6 | chọn ngày | 10/9/2026 → **29 tháng 7 âm**; ô 11/9 đánh dấu **1/8** ⇒ tháng 7 âm chỉ có 29 ngày |
| 7 | tạo thành viên | ✅ |
| 8 | tạo ngày giỗ 30/7 âm | ✅ |
| 9 | liên kết với thành viên | ✅ |
| 10 | đổi tên thành viên | ✅ |
| 11 | tên hiển thị đúng | ngày giỗ **đi theo** tên mới ✅ |
| 12 | xoá thành viên | ✅ còn 0 thành viên |
| 13 | ngày giỗ vẫn tồn tại | ✅ và rơi về **tên đã lưu** |
| 14 | policy tháng nhuận | `LEAP_MONTH_ONLY` → 20/9/2044; `LEAP_MONTH_PREFERRED` → 10/9/2026 kèm **cả hai** lời giải thích |
| 15 | policy ngày 30→29 | → 10/9/2026, khớp với ngày đã chọn độc lập ở bước 6 |
| 16 | xuất | "Đã xuất 1 thành viên và 1 ngày giỗ"; file giữ `"lunarDay": 30` (dữ liệu gốc, **không** phải 29 đã quy đổi) |
| 17 | xoá dữ liệu app | ✅ về trắng |
| 18 | nhập | xem trước hiện đúng dòng *"Người đứng khấn trong file sẽ được áp dụng, vì máy này chưa chọn ai."* |
| 19 | dữ liệu được phục hồi | thành viên + ngày sinh âm 26/1/1950 + **tín chủ** + ngày giỗ 10/9/2026 "Còn 13 ngày" kèm nhãn điều chỉnh — khớp hoàn toàn |
| 20 | restart tiến trình | ✅ |
| 21 | chế độ máy bay | ✅ cả 4 màn dùng được, lịch âm vẫn đúng |
| 22 | resume Activity | ✅ |
| 23 | "Hôm nay" sau khi đổi ngày trên máy | **NOT VERIFIED** — xem dưới |
| 24 | crash / ANR | **không có** `FATAL EXCEPTION`, `ANR in`, `SQLiteException`, `StrictMode` nào |

Ngoài kịch bản, một xác nhận phụ: nhập ngày sinh âm thiếu tháng/năm bị chặn đúng lúc lưu
với thông báo *"Hãy nhập đủ ngày, tháng và năm sinh âm lịch, hoặc để trống cả ba."*

### Bước 23 — NOT VERIFIED, và vì sao

Không đổi được đồng hồ hệ thống của máy: `adb shell date` trả
`cannot set date: Operation not permitted` (máy **không root**). Cách duy nhất còn lại là
vào Cài đặt đổi giờ bằng tay — **cố ý không làm**, vì đây là điện thoại thật đang dùng của
chủ dự án và đổi ngày hệ thống có thể làm rối các app khác.

Đã chứng minh được ở mức tự động, **chạy trên chính máy này**:
`MidnightLifecycleTest.qua_nua_dem_thi_ngay_gio_hom_nay_hien_dung_chu_Hom_nay` tiêm
`DateProvider` giả, cho ngày nhảy qua nửa đêm, rồi kiểm ô đếm ngược đổi từ "Ngày mai" sang
**đúng chữ "Hôm nay"** và tiêu đề ngày dương đổi theo. Bản release dùng đúng đường mã đó
với `DateProvider.System`; R8 không đổi được logic ấy. Nhưng **đó là suy luận, không phải
phép đo**, nên bước này ghi NOT VERIFIED.

---

## 10. Hiệu năng — đo, không tối ưu

Trên SM-A325F, **bản RELEASE**, máy ở **Thermal Status 0**.

| Thao tác | Số đo |
|---|---|
| Khởi động nguội (7 lần, `am start -W`) | trung vị **863 ms**, nhanh nhất 803 ms, chậm nhất 1 282 ms |
| Khởi động nguội với 50 thành viên / 100 ngày giỗ (Phase 7.5) | trung vị 910 ms — **lượng dữ liệu không ảnh hưởng** |
| Cuộn màn Nhà (396 khung hình) | p50 **17 ms**, p90 19 ms, p95 20 ms, janky 5,8 %; GPU p50 11 ms |
| Lật tháng màn Lịch (319 khung hình, ~2 lần/giây) | p50 **25 ms**, p90 27 ms, p95 61 ms, janky 77 %; GPU p50 16 ms |
| Xuất/nhập 50 thành viên / 100 ngày giỗ (40 KB) | encode 44 ms · decode 47 ms · **import trung vị 340 ms** · export 125 ms |
| Xuất/nhập 200 thành viên / 500 ngày giỗ (184 KB) | encode 83 ms · decode 61 ms · **import trung vị 1 020 ms** · export 95 ms |
| 365 lượt quy đổi âm lịch | ~30 ms (Phase 6, `PerformanceBenchmarkTest` vẫn nằm trong bộ 73 test) |

Đọc đúng hai con số khung hình: **cuộn** — thao tác liên tục, nơi jank thật sự gây khó
chịu — chạy p50 17 ms, sát ngưỡng 16,7 ms của màn 60 Hz. **Lật tháng** tốn ~25 ms cho một
khung hình mỗi lần bấm, tức người dùng thấy hơi khựng một nhịp chứ không đứng hình.

**Kết luận: không tối ưu gì.** Mọi thao tác đều dưới ngưỡng hợp lý, và nguyên tắc đã chốt
là không tối ưu khi chưa có vấn đề đo được. Ghi số ở đây làm mốc cho lần sau.

---

## 11. Thành phần đóng băng — xác nhận

| Hạng mục | Bằng chứng |
|---|---|
| `core/lunar` | commit cuối cùng chạm vào là `eb0f550` (Phase 3). **0 thay đổi** kể từ đó |
| Dataset | commit cuối cùng chạm vào là `f12edbb`. SHA-256 `b9f9613a…20f33d`, **không** regenerate |
| Dataset trong APK | cùng hash `b9f9613a…20f33d` |
| Thuật toán lịch âm / ΔT / quy tắc nhuận | không đụng |
| API lịch âm Phase 3 | không đụng |
| Room schema | không đổi, **không thêm migration** |
| Dependency | `gradle/libs.versions.toml` không đổi một dòng |
| Quyền INTERNET | vẫn không có |
| Notification / AlarmManager / WorkManager / cloud / sync / deathYear | không thêm gì |

Mọi thay đổi trong cổng này nằm ở `app/src/androidTest/` và `docs/` — **không chạm một
dòng nào** trong `app/src/main`.

---

## 12. Blocker

### P0 — chặn nộp bài, **đều nằm ngoài mã nguồn**

| # | Blocker | Vì sao |
|---|---|---|
| 1 | Chưa có upload keystore | Play cần AAB đã ký. Keystore là tài sản của chủ dự án |
| 2 | Chưa có URL chính sách quyền riêng tư công khai | Play **bắt buộc kể cả với app không thu thập dữ liệu** — muốn hoàn thành biểu mẫu Data safety thì phải có link |
| 3 | Chưa điền Data safety / Content rating / App access / Target audience / Ads declaration | chỉ làm được trên Play Console |
| 4 | Chưa xác nhận tài khoản có thuộc diện **kiểm thử khép kín 12 người × 14 ngày** | áp dụng cho tài khoản **cá nhân tạo sau 13/11/2023**; chỉ chủ dự án biết |
| 5 | Chưa xác minh danh tính nhà phát triển | tài khoản cá nhân cần giấy tờ tuỳ thân; tài khoản cá nhân **mới** còn phải xác minh có thiết bị Android thật qua app Play Console. D-U-N-S **không** áp dụng (chỉ dành cho tài khoản tổ chức) |
| 6 | Chưa có store listing, ảnh chụp màn hình, feature graphic, thông tin liên hệ | icon 512×512 PNG 32-bit; feature graphic 1024×500; tối thiểu 2 ảnh chụp, nên ≥4 ảnh ≥1080px để đủ điều kiện được đề xuất; mô tả ngắn ≤80 ký tự |

### P1 — **không có**

Không còn blocker nào về đúng/sai, mất dữ liệu, riêng tư/bảo mật hay sập app trong mã nguồn.

### P2 — không chặn phát hành

| # | Việc |
|---|---|
| 1 | Lỗi một lần chưa rõ nguyên nhân ở `CalendarViewModelTest` (§8.4) |
| 2 | `assets/lunar/vn_lunar_v1.json` (10 KB) được đóng gói nhưng app không đọc — file provenance, giữ hay bỏ là quyết định của chủ dự án |
| 3 | Biểu tượng app thiếu thẻ `monochrome` (lint warning) |
| 4 | Cân nhắc đổi `versionName` từ `0.1.0-mvp` sang thứ hợp với cửa hàng hơn |

---

## 13. Việc chủ dự án phải tự làm

**Không gửi mật khẩu hay file keystore cho tôi, cho AI nào, hay qua chat/email.**

Cấu hình đã sẵn sàng: `app/build.gradle.kts` có `signingConfigs.release` đọc từ
`keystore.properties`, thiếu khoá nào thì **fail ngay lúc cấu hình** kèm tên khoá còn
thiếu và **không bao giờ in giá trị**; không có file thì bản release vẫn build được nhưng
**không được ký** — cố ý, để không lặng lẽ ký bằng debug key. `.gitignore` đã chặn
`keystore.properties`, `*.jks`, `*.keystore`.

1. **Tạo upload keystore** (một lần, tự đặt mật khẩu mạnh):
   ```
   keytool -genkeypair -v \
     -keystore ~/nepnha-upload.jks -alias nepnha-upload \
     -keyalg RSA -keysize 4096 -validity 10000
   ```
2. **Sao lưu an toàn.** Mất upload key là mất quyền cập nhật app. Cất `.jks` và mật khẩu ở
   hai nơi khác nhau. **Không** commit, **không** để trong thư mục dự án.
3. **Tạo `keystore.properties` ở gốc dự án**:
   ```
   storeFile=/Users/os/nepnha-upload.jks
   storePassword=…
   keyAlias=nepnha-upload
   keyPassword=…
   ```
4. **Build AAB đã ký**: `./gradlew clean bundleRelease`
5. **Kiểm chữ ký**: `jarsigner -verify -verbose -certs app/build/outputs/bundle/release/app-release.aab`
   — chứng chỉ phải là của bạn, **không** phải `CN=Android Debug`.
6. **Viết và đăng chính sách quyền riêng tư** ở một URL công khai, dựa trên §5 của tài liệu này.
7. **Tạo app trên Play Console**, bật **Play App Signing** (mặc định bật cho app mới),
   điền store listing, Data safety, Content rating, App access, Target audience, Ads declaration.
8. **Xác minh danh tính nhà phát triển** nếu chưa.
9. **Tải AAB lên**, chạy **Closed Testing**, và nếu tài khoản thuộc diện bắt buộc thì hoàn
   thành 12 người × 14 ngày liên tục trước khi *Apply for production*.

---

## 14. Giới hạn còn lại — nói thẳng

1. **Chưa có keystore phát hành.** Mọi APK release trong tài liệu này ký bằng
   `debug.keystore`, **chỉ để cài thử**.
2. **Bước 23 NOT VERIFIED** (đổi ngày hệ thống trên máy thật) — lý do ở §9.
3. **Một lỗi test chưa rõ nguyên nhân** (§8.4), không tái hiện được sau 468 lượt thực thi.
4. **Chỉ kiểm trên một thiết bị**: SM-A325F, Android 13, arm64-v8a. Không có bằng chứng
   trên tablet, màn hình gập, Android 14/15/16/17 hay thiết bị x86.
5. **Chưa có người dùng thật nào** ngoài chủ dự án dùng app này.
6. **Hash artifact đổi theo commit** (§6) — không phải lỗi, nhưng phải nhớ khi đối chiếu.
7. **Nhập cùng một file hai lần là nhân đôi** — có chủ ý, có cảnh báo ở màn xem trước.
8. **Checksum không chống sửa có chủ ý**, và **file sao lưu không mã hoá** — cả tài liệu
   lẫn màn Cài đặt đều nói thẳng.
9. **Lật tháng ở màn Lịch tốn ~25 ms một khung hình** — đã đo, dưới ngưỡng, cố ý không tối ưu.
