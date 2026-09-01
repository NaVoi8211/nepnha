# Final Pre-Signing Stability Gate

Cổng cuối trước khi đóng băng mã nguồn. Chỉ có hai việc: đóng hai điểm chưa chứng minh
được của `FINAL_RELEASE_GATE.md`, rồi freeze.

Ngày: **31/08/2026**. Thiết bị: **SM-A325F, Android 13**. Không mở Phase 8, không thêm
tính năng, không dependency mới, không tạo keystore, không đụng Play Console.

---

## FINAL STATUS — **A. STABLE / READY FOR SIGNING**

Cả hai điểm treo đã đóng bằng bằng chứng đo được, không phải bằng suy luận.

---

## 1. `CalendarViewModel` — root cause đã xác định

### Điều tra

Đọc mã trước: bốn biến `today`, `view`, `knownMemorials`, `knownMembers` là `var`
**không đồng bộ**, được đọc/ghi cả từ thao tác người dùng lẫn từ **hai collector** trong
`init`, và cả hai collector đều ghi `_state.value`.

Trong app thật điều đó an toàn vì mọi lần chạm đều nằm trên luồng chính: thao tác đến từ
callback Compose (`onSelectDay`, `onNextMonth`, `onPreviousMonth`) và
`LifecycleEventEffect(ON_RESUME)`, còn `viewModelScope` trên Android là
`Dispatchers.Main.immediate`.

Trong **unit test không gọi `Dispatchers.setMain`**, `viewModelScope` của lifecycle 2.11
lùi về một context không dispatcher ⇒ collector chạy trên `Dispatchers.Default`.

### Tái hiện — `CalendarViewModelStressTest`

Chạy đúng điều kiện của lớp đã đỏ (không `setMain`):

| Phép đo | Kết quả |
|---|---|
| Chẩn đoán luồng | luồng test `Test worker`, luồng collector **`DefaultDispatcher-worker-3`** — hai luồng khác nhau |
| Stress điều hướng 2.000 vòng | **ĐỎ ở vòng 95**: `expected 2002-02 but was 2002-03` |
| Stress collector bận rộn 2.000 vòng (39.975 lượt phát) | **6 lần lệch**, giá trị nhận được đúng bằng **tháng khởi tạo** của ViewModel đó |

Chi tiết cuối là bằng chứng quyết định: luồng nền dựng state từ `view` **cũ** — nó chưa
nhìn thấy giá trị mới vì không có rào cản bộ nhớ nào bảo đảm điều đó.

**Kết luận: race có thật, nhưng KHÔNG THỂ xảy ra trong app.** Nó chỉ tồn tại vì test cũ
chạy ViewModel dưới một mô hình luồng mà sản phẩm không bao giờ dùng. Đây chính là lần đỏ
một-lần-rồi-thôi đã ghi nhận trong `FINAL_RELEASE_GATE.md` §8.4 — **nay đã có lời giải**.

### Sửa

| Chỗ | Thay đổi |
|---|---|
| `CalendarViewModelTest` | thêm `Dispatchers.setMain(UnconfinedTestDispatcher())` — test chạy đúng mô hình luồng của app |
| `CalendarViewModel` (production) | **chỉ thêm chú thích** ghi rõ bất biến luồng. Không đổi một dòng hành vi nào |
| `CalendarViewModelStressTest` | chuyển sang chạy dưới mô hình luồng production, trở thành lưới chắn hồi quy |

Không tăng timeout, không thêm `sleep`, không nuốt exception, không retry.

### Sau khi sửa

| Phép đo | Kết quả |
|---|---|
| Stress điều hướng | **2.000 vòng, 0 lỗi** |
| Stress collector bận rộn | **2.000 vòng / 40.000 lượt phát, 0 lệch** |
| Test bất biến (tất định) | collector chạy **cùng luồng** với người gọi ✓ |

### Lưới chắn đã được chứng minh

Tiêm hồi quy thật: đổi `viewModelScope.launch {` thành
`viewModelScope.launch(Dispatchers.IO) {` trong production.

**4 test đỏ ngay**, gồm test bất biến tất định và stress collector. Khôi phục ⇒ xanh lại.
Nghĩa là nếu sau này ai đẩy một collector sang luồng nền, nó bị chặn **trước khi** trở
thành một lần đỏ ngẫu nhiên không ai giải thích được.

> Nói cho đúng phạm vi: đây là **đã tìm ra và đóng** nguyên nhân, chứ không phải
> "chứng minh không còn race nào". Bằng chứng là 4.000 vòng có kiểm soát cộng một test
> tất định chốt bất biến, không phải một phép chứng minh hình thức.

---

## 2. Activity recreation / rotation — **PASS**

`RotationStateTest` chạy trên **`MainActivity` thật**. Điều này quan trọng: `setContent`
gọi từ test gắn vào Activity **cũ**, nên sau `recreate()` phải gọi lại bằng tay và ta đang
kiểm một đường mà app thật không đi. `MainActivity` tự gọi `setContent` trong `onCreate`,
nên vòng đời chạy đúng như khi người dùng nghiêng máy.

| Test | Khẳng định **giá trị nghiệp vụ** |
|---|---|
| `man_lich_giu_nguyen_thang_va_ngay_dang_chon_sau_khi_dung_lai` | tiêu đề tháng và **toàn bộ chữ trên thẻ ngày đang chọn** khớp từng ký tự trước/sau |
| `form_ngay_gio_chua_luu_khong_bi_mat_sau_khi_dung_lai` | chữ người dùng đã gõ mà **chưa lưu** còn nguyên |
| `ngay_tren_man_nha_van_dung_sau_khi_dung_lai` | ngày dương/âm giữ nguyên **và** đúng bằng `VietnameseDateFormatter.fullDate(LocalDate.now())` — không chỉ "không đổi", vì "không đổi" cũng đúng khi cả hai lần đều sai |
| `ngay_gio_giu_gia_tri_nghiep_vu_va_khong_nhan_ban_sau_khi_dung_lai` | đếm ngược đúng chữ **"Ngày mai"**, cả dòng ngày giỗ khớp từng ký tự, và **số bản ghi trong database không đổi** ⇒ dựng lại không nhân bản |
| `xoay_ngang_roi_doc_that_su_giu_nguyen_thang_va_ngay_dang_chon` | **xoay thật** qua `requestedOrientation` LANDSCAPE → PORTRAIT, không phải `recreate()` mô phỏng |

Không test nào dùng `assertIsDisplayed()` làm bằng chứng duy nhất.

**5/5 pass trên SM-A325F.** Ngày giỗ do test gieo được xoá ở `@After` dù test đỏ hay xanh.

### Hai lỗi trong chính bộ test này, tìm ra và sửa

1. **`waitForTag` dùng cây semantics ĐÃ GỘP.** `home_countdown_<id>` nằm trong một hàng
   `clickable`, mà `clickable` gộp semantics của con vào cha — trên cây đã gộp, tag bên
   trong **không tồn tại**. Chờ nó là chờ mãi, và lỗi hiện ra dưới dạng "hết giờ 10 giây"
   chứ không phải "tìm sai chỗ". Tìm ra bằng cách in cả cây semantics ra logcat, thấy
   `home_countdown_1` = `Ngày mai` vẫn nằm đó. Sửa: luôn `useUnmergedTree = true`.
2. **Đọc ô nhập bằng cách gom cả cây con** nhặt luôn nhãn của ô ("Tên người mất"). Tách
   `fieldTextOf` chỉ đọc `EditableText`.

---

## 3. Ma trận test

| Test | Kết quả |
|---|---|
| Unit (`clean`, `--no-build-cache`) | **160 test / 21 lớp — 0 fail** |
| Instrumented (SM-A325F) | **75 test / 15 lớp — 0 fail** |
| Lint (`clean lintDebug`) | **0 error**, 23 warning |
| `verify_lunar_dataset.py` | **DATASET HỢP LỆ** |
| `test_generator.py` | **TẤT CẢ ĐỀU QUA** |
| `assembleRelease` (`clean`) | **BUILD SUCCESSFUL** |
| `bundleRelease` (`clean`) | **BUILD SUCCESSFUL** |
| `check_release_mapping.py` | **PASS** — 13 chỗ, 10 enum / 28 hằng giữ nguyên tên |
| 16 KB page size | **PASS** — 8/8 `.so` căn chỉnh, `zipalign -P 16` *Verification successful* |

So với cổng trước: unit +4 (stress), instrumented +2 (xoay máy).

---

## 4. Artifact

Đo trên bản build sạch tại commit cuối. Xem phần cuối tài liệu để biết hash chính xác của
commit nào — AGP nhúng git SHA vào `META-INF/version-control-info.textproto` nên **hash
đổi theo commit** dù kích thước không đổi (đã giải thích ở `FINAL_RELEASE_GATE.md` §6).

| | |
|---|---|
| APK release | 1 766 674 B |
| AAB | 4 260 795 B |
| Dataset SHA-256 | `b9f9613a0d1974ac82a024a737b8b40cbd1588869881db13c90f4c90a020f33d` — trong repo **và** trong APK |
| `applicationId` / `versionCode` / `versionName` | `com.nepnha` / `1` / `0.1.0-mvp` |
| `minSdk` / `targetSdk` | 26 / **37** |
| `uses-permission` | đúng một: `com.nepnha.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` (mức chữ ký, của chính app) |
| Hàng rào component | `android.permission.DUMP` trên `ProfileInstallReceiver` — **không phải quyền app xin** |
| INTERNET | **không có** |
| `<queries>` / deep link / foreground service / `debuggable` / `testOnly` | 0 / 0 / 0 / 0 / 0 |
| Component exported | `MainActivity` (LAUNCHER, bắt buộc) và `ProfileInstallReceiver` (chặn bằng DUMP). Provider và Service của AndroidX đều `exported=false` |
| `mapping.txt` | 37 MB, **có trong AAB** (`BUNDLE-METADATA/…/proguard.map`) |
| Entry debug/test trong APK | 0 |
| Chuỗi giống secret trong APK | 0 |
| File keystore trong repo | 0 |

---

## 5. Offline — kiểm toàn bộ trong **chế độ máy bay**

Bật máy bay **trước khi cài**, giữ suốt phiên.

| Bước | Kết quả |
|---|---|
| Cài bản release | ✅ |
| Mở app | 31/8/2026 → **19 tháng 7 năm Bính Ngọ** |
| Home | ✅ |
| Calendar + lật tháng | Tháng 8 → Tháng 9 ✅ |
| Tạo ngày giỗ 15/8 âm | → **25/9/2026**, đối chiếu độc lập với lưới lịch (1/8 âm = 11/9 ⇒ 15/8 = 25/9) ✅ |
| Xuất | "Đã xuất 0 thành viên và 1 ngày giỗ", file có checksum và policy đúng |
| Xoá sạch dữ liệu app | ✅ |
| Nhập lại | "Đã thêm 0 thành viên và 1 ngày giỗ" |
| Restart tiến trình | dữ liệu và ngày giỗ khôi phục đúng: "Cu To Offline", "Còn 25 ngày", 25/9/2026 |
| Crash / ANR / StrictMode | **không có** |

**Không có phụ thuộc mạng nào.** Trong logcat có vài `UnknownHostException`, nhưng chúng
thuộc **app khác**: PID 1447 là `system_server` (`play.google.com`) và PID 9146 là tiến
trình của Google Play Services dùng OkHttp. PID của Nếp Nhà trong phiên là 8619 / 11888 /
12179. Nếp Nhà **không có** OkHttp trong cây phụ thuộc và **0** chuỗi mạng trong dex.

---

## 6. Privacy-policy specification

Chỉ là đặc tả để chủ dự án tự viết chính sách. Mọi mục dưới đây rút ra từ artifact thật.

| # | Mục | Sự thật đo được |
|---|---|---|
| 1 | Dữ liệu lưu trên máy | `families(name)`; `members(fullName, gender, ngày sinh dương, 4 trường ngày sinh âm, nguồn, vai trò, ghi chú)`; `memorials(name, memberId, lunarDay, lunarMonth, 2 policy, ghi chú)`; + id/timestamp. DataStore: đúng một khoá `primary_member_id`. Tất cả trong sandbox riêng của app |
| 2 | Dữ liệu xuất ra | Chỉ khi người dùng tự bấm và tự chọn nơi lưu qua bộ chọn file của Android. Nội dung đúng bằng hợp đồng `PHASE_7_EXPORT_IMPORT.md`. **Không** chứa id nội bộ, **không** chứa dữ liệu lịch đã tính, **không** chứa thông tin thiết bị. **Văn bản thường, KHÔNG mã hoá** |
| 3 | Dữ liệu nhập vào | Chỉ khi người dùng tự chọn file; có màn xem trước và phải xác nhận trước khi ghi |
| 4 | Dữ liệu rời khỏi thiết bị | **Không có**, trừ đúng một đường: file người dùng **tự xuất** rồi tự mang đi |
| 5 | SDK có khả năng mạng | **Không có SDK mạng nào**: 0 dòng khớp `okhttp\|retrofit\|volley\|firebase\|gms\|analytics` trong cây phụ thuộc release; 0 chuỗi mạng trong dex. App **không khai báo `INTERNET`** nên về mặt kỹ thuật không thể gửi gì |
| 6 | Analytics / crash reporting | **Không có** |
| 7 | Tài khoản | **Không có** ⇒ yêu cầu "xoá tài khoản" của Play **không áp dụng** |
| 8 | Quảng cáo | **Không có** |
| 9 | Xoá dữ liệu | Gỡ app hoặc "Xoá dữ liệu" là xoá hết. Không có bản sao ở nơi nào khác |
| 10 | Lưu giữ | Dữ liệu tồn tại trên máy tới khi người dùng tự xoá. Không có thời hạn phía máy chủ vì không có máy chủ |

### Câu KHÔNG được viết

Đừng viết *"the app never communicates with any external service"*. Phân biệt ba mức:

| Mức | Ở Nếp Nhà |
|---|---|
| App **chủ động gửi dữ liệu người dùng** | **Không** — không có quyền `INTERNET`, không có client mạng |
| **OS/framework truy cập tài nguyên hệ thống** | **Có**: `androidx.emoji2` hỏi nhà cung cấp font của hệ thống một font emoji lúc khởi động. Đó là IPC sang app khác, chạy bằng quyền của app đó; nội dung là **chuỗi truy vấn font**, không có dữ liệu người dùng |
| **SDK có khả năng mạng nhưng không dùng để gửi dữ liệu người dùng** | **Không có SDK nào như vậy** |

Câu an toàn và đúng sự thật: *"Nếp Nhà không xin quyền truy cập mạng và không gửi dữ liệu
của bạn đi đâu. Dữ liệu chỉ rời khỏi máy khi chính bạn xuất ra một file và tự mang đi."*

---

## 7. Play readiness — chỉ xác minh, không đổi code

Tra nguồn chính thức đúng ngày 31/08/2026.

| Yêu cầu | Trạng thái |
|---|---|
| **Target API** | Từ **hôm nay 31/08/2026**, app mới phải target **API 36 trở lên**; đây là **mức tối thiểu**, target cao hơn được chấp nhận. Nếp Nhà target **37** ⇒ **đạt**. (Có thể xin gia hạn tới 01/11/2026 — không cần dùng.) |
| **16 KB page size** | **đạt** — 8/8 `.so`, `p_align 0x4000`, `extractNativeLibs=false` |
| **AAB** | **đạt** |
| Privacy policy | ❌ chưa có URL công khai — **bắt buộc kể cả với app không thu thập dữ liệu** |
| Data Safety | ❌ chưa khai (đặc tả ở §6) |
| Content rating | ❌ chưa làm |
| Target audience | ❌ chưa khai |
| Ads declaration | ❌ chưa khai (thực tế: không có quảng cáo) |
| App access | ❌ chưa khai (thực tế: không cần đăng nhập) |
| Developer verification | ❌ tài khoản cá nhân cần giấy tờ tuỳ thân; tài khoản cá nhân **mới** còn phải xác minh có thiết bị Android thật qua app Play Console. D-U-N-S **không** áp dụng cho tài khoản cá nhân |
| Closed testing | ❌ nếu tài khoản cá nhân tạo **sau 13/11/2023**: **≥12 người kiểm thử, opt-in liên tục 14 ngày**, rồi mới *Apply for production*. **Đáp ứng 12×14 KHÔNG tự động bảo đảm được duyệt production** — Google vẫn xét đơn |
| Store listing | ❌ icon 512×512 PNG 32-bit; feature graphic 1024×500; ≥2 ảnh chụp (nên ≥4 ảnh ≥1080px); mô tả ngắn ≤80 ký tự |

---

## 8. Signing — không tạo gì

Đã kiểm, **không thay đổi cấu hình** (không cần):

* `.gitignore` chặn `keystore.properties`, `*.jks`, `*.keystore` ✓
* 0 file keystore trong repo ✓
* 0 secret trong repo và trong APK ✓
* `signingConfigs.release` đọc từ `keystore.properties`, thiếu khoá thì **fail ngay lúc
  cấu hình** kèm tên khoá, **không bao giờ in giá trị** ✓
* Không có file thì bản release vẫn build được nhưng **không ký** — cố ý ✓

Không chạy `keytool`, không tạo `.jks`/`.keystore`/password file.

---

## 9. Freeze

| Hạng mục | Trạng thái |
|---|---|
| `core/lunar` | **0 thay đổi** — commit cuối chạm vào là `eb0f550` (Phase 3) |
| Dataset | **0 thay đổi** — commit cuối chạm vào là `f12edbb`; SHA `b9f9613a…20f33d` không đổi |
| Room schema | không đổi, không thêm migration |
| Dependency | `libs.versions.toml` không đổi |
| `app/src/main` | **1 file thay đổi: `CalendarViewModel.kt`, +15 dòng, TOÀN BỘ là chú thích** ghi bất biến luồng. Không đổi một dòng hành vi nào |

---

## 10. Rủi ro còn lại

1. **Chưa có keystore phát hành** — mọi APK trong tài liệu này ký bằng `debug.keystore`,
   chỉ để cài thử.
2. **Bất biến luồng của `CalendarViewModel` được bảo vệ bằng test, không bằng kiểu.** Có
   một test tất định chốt nó và đã chứng minh bắt được hồi quy, nhưng trình biên dịch
   không ép được. Đổi kiến trúc để ép sẽ là refactor lớn — cố ý không làm trong cổng này.
3. **Chỉ kiểm trên một thiết bị**: SM-A325F, Android 13, arm64-v8a.
4. **Chưa có người dùng thật nào** ngoài chủ dự án.
5. **Đổi ngày hệ thống trên máy thật** vẫn NOT VERIFIED (máy không root) — đã chứng minh ở
   tầng tự động bằng `MidnightLifecycleTest` với `DateProvider` giả.
6. **Hash artifact đổi theo commit** — không phải lỗi, nhưng phải nhớ khi đối chiếu.
7. Các giới hạn đã ghi ở `FINAL_RELEASE_GATE.md` §14 vẫn còn nguyên giá trị.

---

## 10b. Lịch sử git đã được viết lại (01/09/2026)

Sau khi cổng này đóng, toàn bộ **29 commit** được viết lại để đổi email tác giả và
committer từ email công ty sang email cá nhân của chủ dự án. Repo **chưa từng được push**
nên không ảnh hưởng tới ai.

Điều đó có nghĩa:

* **Mọi commit SHA đã đổi.** Các SHA trích dẫn trong tài liệu này và trong
  `FINAL_RELEASE_GATE.md` đã được cập nhật sang giá trị mới;
* **Nội dung không đổi một byte nào** — đã chứng minh bằng cách so `HEAD^{tree}` trước và
  sau: cùng là `9e936ab9c927fabea2039fa6ab658afa7bd46b69`;
* **Hash artifact ghi trong các tài liệu cũ không còn tái lập được**, vì AGP nhúng git SHA
  vào `META-INF/version-control-info.textproto`. Đây là hệ quả đã lường trước, không phải
  lỗi. Muốn có hash hiện hành thì build lại tại commit hiện tại.

## 11. Việc tiếp theo — của chủ dự án

**DỪNG CODE.** Nguồn đã đóng băng.

1. Tạo upload keystore (`keytool -genkeypair`), tự giữ mật khẩu.
2. Sao lưu keystore ở hai nơi. Mất là mất quyền cập nhật app.
3. Tạo `keystore.properties` ở gốc dự án (đã được `.gitignore` chặn).
4. `./gradlew clean bundleRelease` rồi kiểm chữ ký bằng `jarsigner -verify -verbose -certs`.
5. Viết và đăng chính sách quyền riêng tư theo đặc tả §6.
6. Tạo app trên Play Console, bật Play App Signing, điền các khai báo ở §7.
7. Xác minh danh tính nhà phát triển.
8. Tải AAB lên, chạy Closed Testing.

**Đừng gửi mật khẩu hay keystore cho ai.**
