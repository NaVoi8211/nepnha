# Phase 7.5 — Kiểm toán bản ứng viên phát hành

Mục tiêu duy nhất: xác định **có bằng chứng** rằng bản AAB hiện tại đủ an toàn để đưa lên
Google Play, hoặc liệt kê chính xác những gì còn thiếu. Đây là phase kiểm toán và gia cố,
không phải phase làm tính năng: không notification, không AlarmManager, không WorkManager,
không cloud, không đổi giao diện.

## KẾT LUẬN — B. READY FOR SIGNING ONLY

Mã nguồn và artifact đã đạt. **Không còn blocker về đúng/sai, mất dữ liệu, sập app hay
riêng tư.** Phần còn lại nằm ngoài tầm với của mã nguồn: keystore phát hành và các mục
khai báo trên Play Console — đều là việc của chủ dự án, liệt kê ở cuối tài liệu.

Không dùng trạng thái A vì chưa có keystore và chưa có chính sách quyền riêng tư được
công bố; hai thứ đó Play bắt buộc và không thể tự tạo thay chủ dự án.

---

## Định danh bản kiểm

| Hạng mục | Giá trị |
|---|---|
| Commit gốc khi bắt đầu | `e2ae29d` |
| Thiết bị | Samsung Galaxy A32 (SM-A325F), Android 13, arm64-v8a |
| Máy build | MacBook Pro 2019 Intel i5, 8 GB RAM, JBR 25, AGP 9.3.1 |
| `applicationId` | `com.nepnha` |
| `versionCode` / `versionName` | `1` / `0.1.0-mvp` |
| `minSdk` / `targetSdk` / `compileSdk` | 26 / 37 / 37 |
| APK release (chưa ký) SHA-256 | `f9e21db731258444186f9c80ead9afbed0aafe3c6dc74b3b845dd5a55ed8fe90` (1 766 674 B) |
| AAB SHA-256 | `2c638543a15be5cc28cd0c1c4db63bcd8e37fcceac1e1ee487946fb85454d03d` (4 260 795 B) |
| Dataset lịch âm SHA-256 | `b9f9613a0d1974ac82a024a737b8b40cbd1588869881db13c90f4c90a020f33d` |

Hai hash artifact ở trên đến từ một lần `./gradlew clean` rồi build lại **không dùng
build cache**, và trùng khớp từng byte với lần build trước đó từ cùng mã nguồn — bản
release tái lập được trên máy này.

---

## Lỗi tìm được và đã sửa

### 1. Ba đường **âm thầm mất dữ liệu** trong bộ đọc file sao lưu — P1, đã sửa

Hợp đồng Phase 7 cấm "giá trị lạ thì lặng lẽ về mặc định" đối với policy, nhưng ba chỗ
khác trong chính bộ đọc lại làm đúng điều đó:

| Chỗ | Hành vi cũ | Hậu quả với người dùng |
|---|---|---|
| `leapMonth` | so sánh chuỗi `== "true"`, nên `1`, `"yes"`, `"True"` đều thành `false` | người sinh **tháng nhuận** khôi phục xong thành sinh tháng thường |
| `lunarBirthDate` sai kiểu | `runCatching { jsonObject }.getOrNull() ?: return null` | ngày sinh âm **biến mất** không một lời báo |
| `primaryMemberRef` sai kiểu | `intOrNull` trả `null` ⇒ coi như không có | khôi phục xong **mất tín chủ**, không rõ vì sao |

Cả ba giờ báo `WrongType` kèm vị trí trong file. `null` tường minh vẫn được hiểu là
"không có" chứ không phải sai kiểu — có test riêng cho việc đó, vì file do chính app ghi
ra luôn dùng `null`.

Không chỗ nào trong ba chỗ này xảy ra với file do Nếp Nhà tạo. Chúng chỉ bị kích hoạt bởi
file sửa tay hoặc file do công cụ khác sinh ra. Sửa vì hợp đồng đã hứa "hỏng thì nói",
và vì một bản sao lưu đọc sai còn tệ hơn một bản sao lưu đọc không được.

### 2. Câu chú thích sai trong `BackupChecksum` — đã sửa

Chú thích khẳng định thứ tự phần tử trong file "quyết định thứ tự danh sách sau khi nhập".
Sai với ngày giỗ: DAO sắp lại theo `ORDER BY lunarMonth, lunarDay, id`. Phát hiện khi một
test mới so sánh bản xuất lần hai với bộ dữ liệu gốc và lệch thứ tự. Dữ liệu đúng, chỉ có
lời mô tả sai — nhưng một lời mô tả sai trong mã nguồn là cái bẫy cho người sửa sau.

### 3. `DebugProbesKt.bin` nằm trong mọi bản cài — P2, đã loại bỏ

Bảng dò của trình gỡ lỗi coroutine, 1 728 B, chỉ có tác dụng khi cài debug agent. Đã thêm
vào `packaging.resources.excludes`.

### 4. Công cụ kiểm R8 chỉ bảo vệ được thứ người viết còn nhớ — P1 về quy trình, đã viết lại

`tools/check_release_mapping.py` bản cũ giữ một **danh sách viết tay** 9 hằng enum. Thêm một
enum mới lưu xuống database là script vẫn báo xanh. Bản mới có ba lớp:

1. **RULE** — rule `-keepclassmembers enum com.nepnha.**` còn nguyên trong `proguard-rules.pro`.
2. **SITES** — tập 13 chỗ `enum.name` chảy vào/ra Room đúng bằng ảnh chụp đã ghi nhận.
   Phân loại theo **tên cột** (`ENUM_COLUMNS` / `TEXT_COLUMNS`) chứ không đoán vế phải:
   `name = x.name` (tên ngày giỗ) và `gender = m.gender.name` (hằng enum) trông giống hệt
   nhau khi không có kiểu.
3. **MAPPING** — quét **toàn bộ** 10 enum dưới `com.nepnha` từ nguồn (28 hằng), không cái
   nào bị R8 đổi tên; riêng 4 enum đang nằm trong database thì bắt buộc phải có mặt.

Trong lúc viết, chính công cụ này mắc hai lỗi phải sửa — cả hai đều thuộc loại "báo xanh
trong khi thực ra bỏ sót", tức là loại nguy hiểm nhất với một công cụ an toàn:

* tách hằng enum khỏi thân lớp bằng dấu `;`, mà doc comment tiếng Việt đầy dấu chấm phẩy
  ("giữ đúng nguyên tắc; hệ quả là…") ⇒ mất hằng cuối của `LeapMonthPolicy` và **cả hai**
  hằng của `MissingDayPolicy`. Sửa bằng cách bỏ comment trước khi phân tích;
* nuốt luôn dấu phẩy phân cách ⇒ hai hằng viết chung một dòng
  (`{ DAY_OUT_OF_RANGE, MONTH_OUT_OF_RANGE }`) chỉ nhận được cái đầu. Sửa bằng lookahead.

**Công cụ đã được chứng minh bằng lỗi tiêm vào**, không chỉ bằng lần chạy xanh:

| Lỗi giả | Kết quả |
|---|---|
| đổi `MissingDayPolicy.SKIP -> f` trong `mapping.txt` | ✓ bắt được |
| xoá rule giữ tên enum khỏi `proguard-rules.pro` | ✓ bắt được |
| thêm một chỗ `missingDayPolicy = somethingElse.name` mới | ✓ bắt được |
| thêm một cột lạ nhận `.name` (`coMoiToanhToanh = zodiacSign.name`) | ✓ bắt được, đòi phân loại |
| khôi phục nguyên trạng | ✓ xanh trở lại |

### 5. Lint error có sẵn từ Phase 4 — đã sửa ở Phase 7

`StateFlow.value` đọc trong composition ở `LunarIntegrationTest`. Ghi lại ở đây cho đủ mạch.

---

## Ba lần test đỏ, không lần nào sửa test cho qua

| Test | Nguyên nhân thật | Xử lý |
|---|---|---|
| `bo_du_lieu_lon_nhap_dung_va_do_thoi_gian` (350 ≠ 50) | **lỗi test**: hàm đo lặp block 7 lần, mà `importAdditive` theo hợp đồng là CHỈ-THÊM nên 7 lần = 7 bộ dữ liệu. Đúng như thiết kế. | đo import bằng 5 database sạch riêng biệt, lấy trung vị |
| cùng test, lệch thứ tự ngày giỗ | **lỗi test + chú thích sai trong production**: DAO sắp ngày giỗ theo ngày âm | so sánh ngày giỗ theo tập hợp; sửa chú thích ở `BackupChecksum` |
| `bang_dem_nguoc_dung_o_moi_khoang_cach` (thiếu `home_countdown_4`) | **lỗi test**: màn Nhà cố ý chỉ hiện 3 ngày giỗ gần nhất (`HomeViewModel.take(3)`) | dùng đúng 3 mốc 0/1/9 ngày, phủ đủ cả ba nhánh |

Ngoài ra một lần `minifyReleaseWithR8 FAILED` xảy ra **một lần duy nhất** rồi không tái
hiện: build sạch không cache sau đó thành công trong 2 phút 26. Không giữ được log chẩn
đoán. Xếp loại **môi trường** (máy 8 GB, Gradle daemon 1536 MB + Kotlin daemon 1024 MB +
R8 chạy cùng lúc), không phải lỗi mã nguồn. Ghi lại vì nếu tái diễn thì đây là manh mối
đầu tiên.

---

## Ma trận test

| Bộ | Số lượng | Kết quả |
|---|---|---|
| Unit (JVM) | **156** | 156 pass, 0 fail |
| Instrumented (SM-A325F, Android 13) | **70** | 70 pass, 0 fail |
| Lint (`lintDebug`) | — | **0 error**, 23 warning (đều là gợi ý nâng phiên bản / plural / tài nguyên chưa dùng) |
| `tools/check_release_mapping.py` | 3 lớp | pass, và bắt được cả 4 lỗi tiêm vào |

Test **mới thêm trong Phase 7.5** (13 cái):

*Unit — `BackupCodecTest` (+7)*
`bien duoi va bien tren cua ngay va thang am deu bi chan` · `file bi cat cut bi tu choi` ·
`moc thoi gian sai dinh dang khong chan viec nhap` ·
`leapMonth sai kieu bi bao loi chu khong am tham thanh false` ·
`lunarBirthDate sai kieu bi bao loi chu khong am tham bien mat` ·
`primaryMemberRef sai kieu bi bao loi` · `null tuong minh khong bi coi la sai kieu`

*Instrumented (+6)*
`nhap_vao_database_hoan_toan_trong_tai_hien_du_moi_thu` ·
`rollback_toan_bo_tren_bo_du_lieu_lon` · `bo_du_lieu_lon_nhap_dung_va_do_thoi_gian` ·
`nang_cap_v2_len_v3_nhieu_ban_ghi_khong_gan_nham_va_khong_danh_so_lai` ·
`bang_dem_nguoc_dung_o_moi_khoang_cach` · `dung_lai_activity_van_hien_dung_ngay_va_du_lieu`

---

## §II — Xuất / nhập: mất dữ liệu và tính nguyên tử

| Yêu cầu | Bằng chứng |
|---|---|
| **A.** nhập vào database **hoàn toàn trống** | `nhap_vao_database_hoan_toan_trong_tai_hien_du_moi_thu`: khẳng định trước khi nhập là chưa có gia đình và chưa có tín chủ, sau khi nhập kiểm từng trường — tên gia đình lấy theo file, dấu tiếng Việt (`Nguyễn Văn A`, `Trưởng nam`, `ghi chú`), ngày sinh dương, đủ 4 trường ngày sinh âm, người không có ngày sinh âm vẫn không có, quan hệ `memberId` trỏ đúng người với id mới, hai policy không mặc định, tín chủ được áp dụng |
| **B.** nhập lần hai | `nhap_vao_may_da_co_du_lieu_chi_them_khong_xoa` + kiểm tay trên máy thật: 3 → 6 thành viên, tín chủ cũ không đổi, không bản ghi nào bị ghi đè |
| **C.** file lỗi (15 loại) | bảng riêng bên dưới |
| **D.** nguyên tử trên bộ **lớn** | `rollback_toan_bo_tren_bo_du_lieu_lon`: 60 thành viên + 120 ngày giỗ, bản ghi **thứ 100** hỏng ⇒ ném sau khi 99 cái trước đã ghi. Sau đó số thành viên, số ngày giỗ và **từng trường** của dữ liệu cũ y nguyên |
| **E.** kỳ vọng idempotency | Không idempotent, **có chủ ý**. Màn xem trước nói thẳng: *"Nhập cùng một file hai lần sẽ tạo ra hai bản — Nếp Nhà không tự gộp hai người trùng tên, vì không có cách nào biết chắc đó là một người."* Không có đường gộp ngầm nào |
| **F.** bộ dữ liệu lớn | 50/100 và 200/500, đo trên máy thật — xem §IX |

### C — bảng file lỗi

| Trường hợp | Chỗ kiểm | Kết quả |
|---|---|---|
| checksum sai (lật 1 chữ số, JSON vẫn hợp lệ) | unit + máy thật | từ chối: "File có dấu hiệu bị hỏng…" |
| JSON hỏng | unit + máy thật | từ chối: "File này không đúng định dạng Nếp Nhà." |
| file bị **cắt cụt** (25 %, 50 %, 75 %, 95 %) | unit | cả 4 mức đều bị từ chối |
| file rỗng | unit | `EmptyFile` |
| `formatVersion` không hỗ trợ (99) | unit + máy thật | "File được tạo bởi phiên bản Nếp Nhà mới hơn…" |
| thiếu `formatVersion` | unit | `MissingFormatVersion` |
| thiếu trường bắt buộc | unit | `MissingField` kèm vị trí |
| **trường thừa** | unit + máy thật | bỏ qua, không làm hỏng việc nhập (hợp đồng §G cho phép) |
| enum không hợp lệ | unit + máy thật | `BadEnum`, **không** về mặc định |
| `lunarDay` 0 và 31 | unit | cả hai bị chặn; 1 và 30 vẫn lọt |
| `lunarMonth` 0 và 13 | unit | cả hai bị chặn; 1 và 12 vẫn lọt |
| `ref` không tồn tại (`memberRef`, `primaryMemberRef`) | unit | `DanglingReference` |
| `ref` trùng lặp | unit | `DuplicateRef` |
| **ID trùng lặp** | — | không áp dụng: định dạng **không chứa id của Room**, chỉ có `ref` cục bộ trong file. Đây là lựa chọn của hợp đồng §E, không phải chỗ bỏ sót |
| timestamp không hợp lệ | unit | `exportedAt` là siêu dữ liệu, sai định dạng **không** chặn việc khôi phục — có test ghi rõ quyết định này |
| chuỗi quá dài | unit | `TooLong` kèm giới hạn |
| nhiều lỗi cùng lúc | unit + ảnh chụp máy thật | gom lại báo một lần, mỗi lỗi kèm vị trí |

---

## §III — Riêng tư và bảo mật của file xuất

Nội dung file xuất từ bản **release** trên máy thật, kiểm bằng mắt:

```json
{ "formatVersion": 1, "exportedAt": "…", "appVersionName": "0.1.0-mvp", "checksum": "sha256:…",
  "data": { "familyName": …, "primaryMemberRef": 1,
            "members":   [ { ref, fullName, gender, solarBirthDate, lunarBirthDate, role, note } ],
            "memorials": [ { name, memberRef, lunarDay, lunarMonth,
                             leapMonthPolicy, missingDayPolicy, note } ] } }
```

| Kiểm | Kết quả |
|---|---|
| có dữ liệu ngoài hợp đồng không | không |
| có id nội bộ của Room không | không — chỉ `ref` 1..n cục bộ trong file |
| có dữ liệu lịch **đã tính** dư thừa không | không — chỉ ngày âm người dùng nhập |
| có log/debug data không | không |
| có secret/token không | không (quét chuỗi trong APK: 0 kết quả) |
| có gửi mạng không | không thể — APK **không khai báo `INTERNET`** |
| có ghi file tạm ngoài ý muốn không | không — chỉ ghi đúng `Uri` người dùng chọn qua SAF |
| có log tên người / ngày giỗ không | **không** |

Về mục cuối, nói cho chính xác: sau toàn bộ phiên kiểm tay, `logcat` có **đúng một** dòng
chứa tên người — và dòng đó do `AccessibilityNodeInfoDumper` sinh ra, tức là công cụ
`uiautomator dump` của **người kiểm**, không phải app. Lọc riêng những dòng do Nếp Nhà
phát ra thì **bằng không**: toàn bộ dòng liên quan đến `com.nepnha` đều đến từ hệ thống
Android (ActivityManager, SGM, Layer, GC). Mã nguồn production cũng không có `println`,
`Log.*` hay `printStackTrace` nào.

**Không tuyên bố** file được mã hoá, và **không tuyên bố** checksum chống được sửa có chủ
ý. Checksum chỉ phát hiện hỏng ngoài ý muốn. Màn Cài đặt nói thẳng với người dùng:
*"File xuất ra ở dạng văn bản thường, KHÔNG mã hoá — ai mở được file là đọc được nội dung."*

---

## §IV — Artifact phát hành

Build từ cây làm việc sạch: `./gradlew clean` → `assembleRelease` → `bundleRelease`,
chạy lại với `--no-build-cache` để chắc chắn R8 thật sự chạy.

| Kiểm | Kết quả |
|---|---|
| AAB tồn tại | ✓ 4 260 795 B, có `base/` + `BUNDLE-METADATA/` |
| APK release tồn tại | ✓ 1 766 674 B (`app-release-unsigned.apk`) |
| `applicationId` | `com.nepnha` |
| `versionCode` / `versionName` | 1 / `0.1.0-mvp` |
| `minSdk` / `targetSdk` | 26 / 37 |
| ABI | `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64` — 2 thư viện native của AndroidX (`libandroidx.graphics.path.so`, `libdatastore_shared_counter.so`). AAB tách theo thiết bị nên người dùng chỉ tải phần của mình |
| R8 / minify | ✓ bật, `mapping.txt` sinh ra và **được đóng gói vào AAB** (`BUNDLE-METADATA/…/proguard.map`) nên Play giải mã được crash |
| Resource shrinking | ✓ bật (tài nguyên đã đổi tên: `icon='res/BW.xml'`) |
| test fixture trong APK | không |
| asset chỉ dành cho test | không — nhưng xem "giới hạn" bên dưới về `vn_lunar_v1.json` |
| debug-only code | `DebugProbesKt.bin` **đã loại bỏ** trong phase này; không còn gì khác |
| quyền `INTERNET` | **không có** |
| SDK analytics / crash / ads / mạng | **không có** — toàn bộ 16 dependency đều là AndroidX/Kotlin, 0 dòng khớp `firebase\|crashlytics\|analytics\|admob\|gms\|okhttp\|retrofit\|sentry\|…` |
| secret trong APK | không (quét `strings`) |
| keystore trong repo | không, và `.gitignore` đã chặn `keystore.properties`, `*.jks`, `*.keystore` |
| TODO / FIXME / HACK / XXX | không có trong `app/src/main` |
| `println` / `Log.*` / `printStackTrace` | không có trong `app/src/main` |
| cleartext / network security config | không có, và không thể có ý nghĩa gì khi không có `INTERNET` |
| dataset trong APK | SHA-256 **trùng khớp** `b9f9613a…20f33d` |

### Quyền và component trong manifest đã ghép

Chỉ **một** quyền, do AndroidX tự thêm:

```
permission      com.nepnha.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION  protectionLevel=signature
uses-permission com.nepnha.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION
```

Đây là quyền **mức chữ ký, riêng của app**, `androidx.core` sinh ra để đăng ký
`BroadcastReceiver` không-exported khi `targetSdk ≥ 33`. Không app nào khác dùng được nó.

| Component | exported | Ghi chú |
|---|---|---|
| `com.nepnha.MainActivity` | `true` | bắt buộc — là LAUNCHER |
| `androidx.startup.InitializationProvider` | `false` | |
| `androidx.room.MultiInstanceInvalidationService` | `false` | |
| `androidx.profileinstaller.ProfileInstallReceiver` | `true` | của AndroidX, chặn bằng `android:permission="android.permission.DUMP"` (chỉ hệ thống/shell gọi được) |

---

## §V — Rủi ro riêng của bản minify

Quét toàn bộ `app/src/main` theo từng loại nguy cơ:

| Nguy cơ | Có trong Nếp Nhà? | Đường đi trong mã | Hành vi ở bản release | Chốt chặn |
|---|---|---|---|---|
| `enum.name` lưu xuống database | **CÓ** | 13 chỗ trong `MemorialRepository`, `MemberRepository`, `BackupRepository`, `Gender.fromStorage` | tên hằng giữ nguyên | rule ProGuard + `check_release_mapping.py` (3 lớp, đã tiêm lỗi để chứng minh) |
| reflection | 1 chỗ | `Room.databaseBuilder(…, NepNhaDatabase::class.java, …)` | Room tự sinh `_Impl` và mang theo rule giữ của riêng nó | chạy thật trên bản release, có test migration |
| `Class.forName` | không | — | — | — |
| tên lớp bị serialize | không | `BackupCodec` **không** dùng `@Serializable`, chỉ dùng `JsonObject` viết tay với hằng chuỗi cố định | file xuất từ bản release chứa `"female"`, `"leap_month_only"`, `"skip"` — nguyên vẹn | kiểm bằng mắt trên file thật + test unit `enum tren file la hang chuoi co dinh khong phai ten ky hieu` |
| Gson / Moshi | không dùng | — | — | — |
| entity / DAO của Room | có | `@Entity(tableName = …)`, tên cột do KSP chốt lúc biên dịch | SQL sinh sẵn, R8 đổi tên trường Kotlin không ảnh hưởng | 3 test migration chạy trên file SQLite thật |
| chuỗi route điều hướng | có | `TopLevelDestination("home", …)`, `Routes.MEMBER_EDITOR` | **hằng chuỗi tường minh**, không phái sinh từ `enum.name` | đọc mã xác nhận |
| khoá SavedState | không có khoá tự đặt | `rememberSaveable` dùng khoá theo vị trí, cố định lúc biên dịch | | test dựng lại Activity |
| khoá DataStore | có | `longPreferencesKey("primary_member_id")` | hằng chuỗi tường minh | |
| content description | không phụ thuộc reflection | dùng `stringResource` | | |
| tên component trong manifest | có | `.NepNhaApp`, `.MainActivity` | AGP tự sinh rule giữ | cài và chạy bản release trên máy thật |
| rule ProGuard bị xoá | — | — | — | `check_release_mapping.py` lớp RULE |

Về việc "ưu tiên đổi kiến trúc sang wire value thay vì chỉ giữ tên bằng ProGuard": định
dạng **file sao lưu** đã dùng hằng chuỗi độc lập từ Phase 7 và không phụ thuộc `enum.name`.
Còn lại là **cột trong database cục bộ**. Đổi sang wire value ở đó đòi một migration
v3→v4 viết lại 4 cột trên dữ liệu người dùng đang có — rủi ro lớn hơn hẳn cái nó phòng,
trong khi rule giữ tên đã được chứng minh là có hiệu lực và có công cụ canh. Giữ nguyên,
đúng tinh thần "KHÔNG refactor lớn nếu không cần thiết".

---

## §VI — Room và migration

Chạy trên **file SQLite thật**, dựng đúng DDL của từng phiên bản đã phát hành rồi mở bằng
Room bản hiện tại (Room tự đối chiếu `identity_hash`, lệch một cột là ném).

| Đường đi | Test | Kiểm được gì |
|---|---|---|
| v1 → v2 → v3 (một mạch) | `nang_cap_v1_len_v2_giu_nguyen_du_lieu_cu` | gia đình và thành viên cũ còn nguyên; bảng `memorials` dùng được ngay; cột `memberId` của v3 có mặt và để trống |
| v2 → v3 | `nang_cap_v2_len_v3_giu_nguyen_ngay_gio` | từng cột cũ giữ nguyên từng giá trị, kể cả `createdAt`/`updatedAt` |
| v2 → v3, **nhiều bản ghi** *(mới)* | `nang_cap_v2_len_v3_nhieu_ban_ghi_khong_gan_nham_va_khong_danh_so_lai` | 4 ngày giỗ + 1 thành viên: tra theo **id cũ** vẫn ra đúng tên ⇒ migration không đánh số lại; không dòng nào thừa/thiếu; `memberId` để trống chứ không đoán bừa; khoá ngoại mới là khoá ngoại **thật** |
| `ON DELETE SET NULL` | `xoa_thanh_vien_chi_lam_dut_lien_ket_khong_xoa_ngay_gio` + kiểm tay trên máy | xoá thành viên chỉ làm đứt liên kết, ngày giỗ và tên đã lưu vẫn còn |

`memberId` nullable đúng; schema export `1.json`/`2.json`/`3.json` được commit; hash schema
ổn định (Room ném ngay lúc mở nếu lệch, và cả 3 test migration đều mở được).

Không thêm migration mới.

---

## §VII — "Hôm nay" và vòng đời

| Yêu cầu | Trạng thái | Bằng chứng |
|---|---|---|
| mở trước nửa đêm → đổi ngày → resume → tiêu đề đổi | ✓ | `qua_nua_dem_thi_ngay_gio_hom_nay_hien_dung_chu_Hom_nay`: ngày dương trên đầu màn Nhà đổi từ `10 tháng 9, 2026` sang `11 tháng 9, 2026` |
| `0 → "Hôm nay"` | ✓ | cùng test, và `bang_dem_nguoc_dung_o_moi_khoang_cach` |
| `1 → "Ngày mai"` | ✓ | `bang_dem_nguoc_dung_o_moi_khoang_cach` |
| `>1 → "Còn N ngày"` | ✓ | cùng test, mốc 9 ngày |
| **không bao giờ** "Còn 0 ngày" | ✓ | cùng test khẳng định cả `"Còn 0 ngày"` lẫn `"Còn 1 ngày"` không tồn tại ở bất cứ đâu trên màn hình |
| ngày giỗ hôm nay không hiện "Ngày mai" | ✓ | `assertTextEquals("Hôm nay")`, so khớp **chính xác** chứ không phải chứa |
| **Activity dựng lại thật sự** | ✓ | `dung_lai_activity_van_hien_dung_ngay_va_du_lieu` dùng `scenario.recreate()` — huỷ hẳn Activity cũ, không phải STOP/RESUME cùng một instance. Sau khi dựng lại, ngày, đếm ngược và tên ngày giỗ đều đúng |
| xoay máy thật | ✓ | kiểm tay trên bản release: xoay ngang → dọc, dữ liệu nguyên vẹn, bố cục không vỡ, không crash; và ở Phase 7, xoay giữa lúc đang mở màn xem trước nhập thì hộp thoại giữ nguyên và bấm Huỷ vẫn không ghi gì |

Không có mục nào phải ghi NOT VERIFIED ở §VII. Không thêm bộ đếm, `AlarmManager` hay việc
chạy nền nào để làm những test này xanh.

---

## §VIII — Kiểm tay trên SM-A325F, bản RELEASE đã ký và cài thật

APK release ký bằng `~/.android/debug.keystore` **chỉ để cài thử** (chưa có keystore phát
hành — xem §XI), cài bằng `adb install`, dữ liệu xoá sạch trước khi bắt đầu.

| # | Việc | Kết quả |
|---|---|---|
| 1 | mở lần đầu | app trống, không crash |
| 2 | màn Nhà | 27/8/2026 · 15 tháng 7 năm Bính Ngọ |
| 3 | màn Lịch | lưới tháng 8/2026, ngày âm dưới mỗi ô, mốc sang tháng `1/7` đánh dấu đỏ ở 13/8, hôm nay được khoanh |
| 4 | tạo thành viên | ✓ |
| 5 | đổi tên thành viên | ✓ (lần đầu tưởng hỏng — hoá ra script kiểm bấm trượt nút Lưu; làm lại từng bước thì đúng) |
| 6 | tạo ngày giỗ | ✓ |
| 7 | policy không mặc định | chọn `SKIP` ⇒ bản xem trước đổi từ "10/9/2026, đã điều chỉnh" sang "31/8/2027" — policy có hiệu lực thật |
| 8 | sửa ngày giỗ | mở lại đúng policy đã lưu, đổi qua lại thì bản xem trước đổi theo ⇒ enum đi trọn vòng Room trên bản minify |
| 9 | xoá thành viên | ✓ còn 0 thành viên |
| 10 | `ON DELETE SET NULL` | ngày giỗ **vẫn còn**, giữ tên đã lưu, không bị xoá theo |
| 11–13 | đóng · force-stop · mở lại | dữ liệu nguyên vẹn |
| 14 | xoay màn hình | ngang và dọc đều đúng, không vỡ bố cục, không crash |
| 15 | nền → tiền cảnh | đúng ngày, đúng dữ liệu |
| 16 | đổi ngày giả | ở tầng tự động (§VII) |
| 17 | xuất | "Đã xuất 1 thành viên và 1 ngày giỗ"; file có `"missingDayPolicy": "skip"` và `"memberRef": null` (đúng, vì thành viên đã bị xoá) |
| 18 | **xoá sạch dữ liệu app** | `pm clear`, app về trắng |
| 19 | nhập lại file vừa xuất | màn xem trước hiện thêm dòng *"Người đứng khấn trong file sẽ được áp dụng, vì máy này chưa chọn ai."* — đúng nhánh máy trống |
| 20 | kiểm tra phục hồi | thành viên, **tín chủ**, ngày giỗ với policy `SKIP` → 31/8/2027, "Còn 369 ngày" — khớp hoàn toàn trạng thái trước khi xoá |
| 21 | 4 loại file lỗi | cắt cụt → "không đúng định dạng"; checksum sai → "có dấu hiệu bị hỏng"; `formatVersion` 99 → "phiên bản mới hơn"; nhiều lỗi giá trị → báo **cả hai** lỗi kèm vị trí `memorials[0].lunarDay` và `memorials[0].leapMonthPolicy` |
| 22 | database sau 4 lần nhập hỏng | không đổi một dòng nào |
| 23 | chế độ máy bay | bật, force-stop, mở lại: cả 4 màn đều dùng được, lịch âm vẫn tính đúng, xuất/nhập vẫn chạy |
| 24 | crash / ANR | **không có** `FATAL EXCEPTION`, `ANR in`, `StrictMode` hay `SQLiteException` nào trong suốt phiên |

Mục 19–20 là kịch bản thật của cả tính năng: mất máy, cầm file sao lưu, lấy lại gia đình.
Nó chạy được trên bản đã minify.

---

## §IX — Hiệu năng, đo trước rồi mới kết luận

Tất cả đo trên **SM-A325F, bản RELEASE**. Không tối ưu gì trong phase này.

### Xuất / nhập (đo trong instrumented test, trung vị 5 lần, mỗi lần một database sạch)

| Bộ dữ liệu | Kích thước file | encode | decode | **import** trung vị | import xấu nhất | export |
|---|---|---|---|---|---|---|
| 50 thành viên / 100 ngày giỗ | 40 KB | 45 ms | 41 ms | **235 ms** | 404 ms | 79 ms |
| 200 thành viên / 500 ngày giỗ | 184 KB | 70 ms | 69 ms | **751 ms** | 836 ms | 96 ms |

Khoảng 1 ms cho mỗi bản ghi. Chạy ngoài luồng chính, có thanh tiến trình. Lần đo đầu cho
1 400 ms là **sai phương pháp** — nó gộp cả thời gian Room tạo schema; sửa bằng cách chạm
vào database một lần trước khi bấm giờ.

Con số đo qua giao diện trên máy (≈2,7 giây cho 50/100) **không phải benchmark**: mỗi vòng
polling của tôi gọi `uiautomator dump` mất 0,5–0,8 giây. Chỉ dùng để kết luận: giao diện
không đứng hình, hộp thoại kết quả hiện ra bình thường.

### Khởi động nguội (`am start -W`, `TotalTime`, 7 lần)

| Trạng thái | Trung vị | Nhanh nhất | Chậm nhất |
|---|---|---|---|
| app rỗng | **914 ms** | 863 ms | 1 265 ms |
| 50 thành viên / 100 ngày giỗ | **910 ms** | 863 ms | 1 212 ms |

Lượng dữ liệu **không** ảnh hưởng thời gian khởi động. Lần chạy đầu tiên luôn chậm hơn
(1,2 s) — bình thường, do nạp trang lần đầu.

### Khung hình

| Thao tác | Tổng khung | Janky | p50 | p90 | p95 |
|---|---|---|---|---|---|
| cuộn màn Nhà (6 lần vuốt) | 211 | **2,84 %** | 10 ms | 15 ms | 15 ms |
| lật tháng ở màn Lịch (12 lần, tốc độ người dùng ~2/giây) | 35 | 71 % | **31 ms** | 61 ms | 69 ms |

Hai con số này phải đọc cùng nhau. Cuộn — thao tác liên tục, nơi jank thật sự làm khó chịu
— rất mượt. Lật tháng tốn ~31 ms **cho một khung hình duy nhất mỗi lần bấm**: người dùng
thấy hơi khựng một nhịp, không phải đứng hình. GPU chỉ tốn 5–6 ms ở mọi phân vị, nên chi
phí nằm ở việc dựng lại lưới tháng trên luồng UI, không phải ở việc vẽ.

Lần đo đầu ở màn Lịch cho 77 % janky kèm `Number High input latency: 25` — phần lớn là do
`adb shell input tap` bơm sự kiện, không phải app. Nhưng đo lại ở tốc độ người dùng thật
vẫn ra p50 31 ms, nên **đây là chi phí thật** chứ không phải nhiễu, và tôi ghi lại đúng
như vậy.

**Kết luận: không tối ưu.** 31 ms cho một lần bấm nằm dưới ngưỡng "tức thì" 100 ms, và
theo nguyên tắc đã chốt thì không được tối ưu khi chưa có vấn đề đo được. Ghi lại số ở
đây để lần sau ai muốn động vào còn có mốc so sánh.

---

## §X — Sẵn sàng nộp Google Play

Tra theo tài liệu chính thức, tháng 8/2026.

### Đã đạt trong mã nguồn

| Yêu cầu | Trạng thái |
|---|---|
| **Target API level** — từ 31/8/2026 app mới và bản cập nhật phải nhắm **API 36+** | ✓ **API 37**, cao hơn yêu cầu. SDK 37 là bản **chính thức** (`AndroidVersion.CodeName=` rỗng, `PreviewSdkInt=0`, `IsBaseSdk=true`) — Play không nhận AAB build bằng SDK xem trước, nên điểm này đã được kiểm chứ không phải đoán |
| **Định dạng AAB** | ✓ `app-release.aab` hợp lệ, có `base/` và `BUNDLE-METADATA/` |
| **Hỗ trợ trang nhớ 16 KB** (bắt buộc từ 1/11/2025 với app có mã native nhắm Android 15+) | ✓ **đã kiểm chứng chứ không suy đoán**: `zipalign -c -P 16` báo *Verification successful* cho cả 8 file `.so`, và đọc trực tiếp header ELF của hai thư viện 64-bit thì mọi `PT_LOAD` đều có `p_align = 0x4000` |
| `applicationId` | ✓ `com.nepnha` |
| `versionCode` / `versionName` | ✓ 1 / `0.1.0-mvp` |
| Quyền nhạy cảm (sức khoẻ, tài chính, SMS, vị trí…) | ✓ không xin quyền nào |
| Quyền ảnh/video | ✓ không xin — app dùng SAF, không cần quyền lưu trữ |
| Quyền thông báo | ✓ không xin — app chưa có thông báo, nên không phải khai báo gì |
| Yêu cầu **xoá tài khoản** | ✓ không áp dụng — app không có tài khoản. Dữ liệu chỉ nằm trên máy, xoá app là xoá hết |
| Quảng cáo | ✓ không có SDK quảng cáo nào |
| `mapping.txt` để giải mã crash | ✓ đã nằm sẵn trong AAB |

### Chưa đạt — đều là việc phải làm ngoài mã nguồn

| Yêu cầu | Loại | Ghi chú |
|---|---|---|
| **Keystore phát hành** | việc thủ công của chủ dự án | chưa tồn tại. Cấu hình đã sẵn sàng đọc, xem §XI |
| **Chính sách quyền riêng tư** ở một URL công khai | việc thủ công + blocker Play | **bắt buộc kể cả với app không thu thập gì**: Play nói rõ *"Even developers with apps that do not collect any user data must complete this form and provide a link to their privacy policy."* Trớ trêu là một app không có `INTERNET` vẫn phải có một trang web |
| **Biểu mẫu Data safety** | Play Console | phải điền dù khai "không thu thập dữ liệu". Theo hành vi thật: không thu thập, không chia sẻ, không truyền đi đâu, không có tài khoản. Nói được như vậy vì app **không thể** gửi mạng — không có quyền `INTERNET` |
| **Content rating** | Play Console | làm bảng câu hỏi |
| **App access** | Play Console | khai "không cần đăng nhập" — đúng với thực tế |
| **Target audience / trẻ em** | Play Console | cần quyết định; app hướng tới người trưởng thành lo việc thờ cúng trong nhà |
| **Ads declaration** | Play Console | khai "không có quảng cáo" |
| **Kiểm thử khép kín 12 người × 14 ngày** | Play Console, **có thể là blocker lớn** | áp dụng cho **tài khoản cá nhân tạo sau 13/11/2023**. 12 người kiểm thử phải **liên tục** tham gia đủ 14 ngày, ai rời ra là đếm lại. **Chủ dự án phải tự xác nhận** tài khoản của mình thuộc diện nào — tôi không có cách nào biết |
| Store listing, ảnh chụp màn hình, feature graphic, thông tin liên hệ | Play Console | chưa chuẩn bị |
| Biểu tượng app | ⚠ | có `ic_launcher` chạy được, nhưng lint nhắc thiếu thẻ `monochrome` (biểu tượng đơn sắc cho Android 13+). Không phải blocker nộp bài, chỉ là biểu tượng trông kém tinh tế ở chế độ theme động |

Không mục nào ở bảng dưới được đánh dấu PASS, vì tôi **không có bằng chứng** — chúng nằm
trong Play Console.

---

## §XI — Ký bản phát hành

**Không tạo keystore.** Keystore là tài sản của chủ dự án và phải do chủ dự án tạo, giữ và
sao lưu. Không có mật khẩu giả, không có keystore giả nào được sinh ra trong phase này.

Đã chuẩn bị sẵn:

* `app/build.gradle.kts` có `signingConfigs.release` đọc từ `keystore.properties` ở gốc dự
  án. Thiếu khoá nào thì **fail ngay lúc cấu hình** kèm tên khoá còn thiếu, và **không bao
  giờ in giá trị**;
* không có file thì bản release vẫn build được nhưng **không được ký** (`app-release-unsigned.apk`)
  — cố ý, để không lặng lẽ ký bằng debug key rồi để chủ dự án tải một file vô dụng lên Play;
* `.gitignore` đã chặn `keystore.properties`, `*.jks`, `*.keystore`;
* không hardcode mật khẩu ở đâu, không log mật khẩu ở đâu;
* bật cả `enableV1Signing` và `enableV2Signing`.

### Các bước chủ dự án phải tự làm

**Đừng gửi mật khẩu hay file keystore cho tôi, cho AI nào, hay qua chat/email.**

1. **Tạo upload keystore** (chạy một lần, tự đặt mật khẩu mạnh):
   ```
   keytool -genkeypair -v \
     -keystore ~/nepnha-upload.jks -alias nepnha-upload \
     -keyalg RSA -keysize 4096 -validity 10000
   ```
2. **Sao lưu an toàn.** Mất upload key là mất quyền cập nhật app. Cất `.jks` và mật khẩu ở
   hai nơi khác nhau (trình quản lý mật khẩu + một bản offline). **Không** commit, **không**
   để trong thư mục dự án.
3. **Tạo `keystore.properties` ở gốc dự án** (`.gitignore` đã chặn sẵn):
   ```
   storeFile=/Users/os/nepnha-upload.jks
   storePassword=…
   keyAlias=nepnha-upload
   keyPassword=…
   ```
4. **Build AAB đã ký**: `./gradlew clean bundleRelease`
   → `app/build/outputs/bundle/release/app-release.aab`
5. **Kiểm chữ ký**:
   ```
   $ANDROID_HOME/build-tools/36.0.0/apksigner verify --print-certs \
     app/build/outputs/apk/release/app-release.apk
   ```
   (với AAB thì dùng `jarsigner -verify -verbose -certs app-release.aab`)
   Chứng chỉ phải là của bạn, **không** phải `CN=Android Debug`.
6. **Tạo app trên Play Console**, điền store listing, Data safety, content rating, app
   access, target audience, ads declaration, và **đăng chính sách quyền riêng tư** ở một
   URL công khai.
7. **Bật Play App Signing** khi tạo app (mặc định đã bật cho app mới). Khoá ở bước 1 trở
   thành *upload key*; Google giữ *app signing key*.
8. **Tải AAB lên**, chạy kiểm thử khép kín nếu tài khoản thuộc diện bắt buộc, rồi
   *Apply for production*.

---

## Giới hạn còn lại — nói thẳng

1. **Chưa có keystore phát hành.** Mọi APK release trong tài liệu này ký bằng
   `debug.keystore` **chỉ để cài thử trên máy của người kiểm**. Không dùng để phát hành.
2. **`assets/lunar/vn_lunar_v1.json` (10 KB) được đóng gói nhưng app không đọc.** Đây là
   file provenance của dataset, nằm cạnh file `.bin`. Không chứa gì nhạy cảm và không phải
   asset của test. Giữ lại là chọn minh bạch, bỏ đi tiết kiệm 10 KB — để chủ dự án quyết,
   không tự ý đổi trong một phase kiểm toán.
3. **Tên gia đình trong file sao lưu không ghi đè tên hiện có.** Chỉ dùng khi máy chưa có
   gia đình nào. Đúng nguyên tắc chỉ-thêm, nhưng có thể trái trông đợi khi khôi phục sang
   máy mới đã lỡ tạo gia đình trước.
4. **Nhập cùng một file hai lần là nhân đôi.** Có chủ ý, có cảnh báo ở màn xem trước.
5. **Checksum không chống sửa có chủ ý**, và **file không mã hoá**. Cả tài liệu lẫn màn
   Cài đặt đều nói thẳng.
6. **Lật tháng ở màn Lịch tốn ~31 ms một khung hình.** Đã đo, dưới ngưỡng, cố ý không tối ưu.
7. **Biểu tượng app thiếu thẻ `monochrome`.** Lint warning, không phải blocker.
8. **Một lần `minifyReleaseWithR8` fail không tái hiện được**, nghi do RAM trên máy 8 GB.
   Không có log giữ lại.
9. **Chỉ kiểm trên một thiết bị** (SM-A325F, Android 13, arm64-v8a). Không có bằng chứng
   trên tablet, màn hình gập, Android 14/15/16/17 hay thiết bị x86.
10. **Chưa kiểm thử với người dùng thật.** Chưa ai ngoài chủ dự án dùng app này.

---

## Blocker

### P0 — chặn việc nộp bài, **không** nằm trong mã nguồn

| # | Blocker | Ai làm |
|---|---|---|
| 1 | Chưa có upload keystore | chủ dự án |
| 2 | Chưa có URL chính sách quyền riêng tư công khai | chủ dự án |
| 3 | Chưa điền Data safety, content rating, app access, target audience, ads declaration | chủ dự án, trên Play Console |
| 4 | Chưa xác nhận tài khoản có thuộc diện kiểm thử khép kín 12 người × 14 ngày hay không | chủ dự án |
| 5 | Chưa có store listing, ảnh chụp màn hình, thông tin liên hệ | chủ dự án |

### P1 — không có

Không còn blocker nào về đúng/sai, mất dữ liệu, sập app hay riêng tư trong mã nguồn.

### P2 — tuỳ chọn, không chặn phát hành

| # | Việc |
|---|---|
| 1 | Quyết định có bỏ `vn_lunar_v1.json` khỏi APK không |
| 2 | Thêm thẻ `monochrome` cho biểu tượng |
| 3 | Cân nhắc đổi `versionName` từ `0.1.0-mvp` sang thứ gì đó hợp với cửa hàng hơn |

---

## Thành phần đóng băng — xác nhận không đụng tới

| Hạng mục | Bằng chứng |
|---|---|
| `core/lunar` | 0 thay đổi (`git status` sạch) |
| `assets/lunar/vn_lunar_v1.bin` | SHA-256 `b9f9613a…20f33d`, và **file trong APK cũng đúng hash đó** |
| Thuật toán lịch âm, mô hình ΔT, quy tắc tháng nhuận | không đụng |
| API lịch âm Phase 3 | không đụng |
| Ngữ nghĩa `MemorialDateResolver` | không đụng |
| Room schema | không đổi, không thêm migration |
| Dependency | `gradle/libs.versions.toml` **không đổi một dòng nào** |
| Quyền `INTERNET` | vẫn không có |
| Notification / AlarmManager / WorkManager / cloud / sync / deathYear | không thêm gì |
