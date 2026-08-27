# Phase 6 — Product hardening / Release readiness

> Năm cổng, làm và nghiệm thu từng cổng một. Engine lịch, dataset và API Phase 3 **không
> đổi một dòng nào**.

```
sha256 dataset  b9f9613a0d1974ac82a024a737b8b40cbd1588869881db13c90f4c90a020f33d  (không đổi)
```

## Gate 1 — Ngày giỗ ↔ thành viên

Liên kết **tuỳ chọn**: `memorials.memberId` nullable.

| | Quy tắc |
|---|---|
| Hiển thị | Có liên kết **và** thành viên còn ⇒ **tên hiện tại của thành viên**. Còn lại ⇒ `Memorial.name` |
| `name` | **Không bao giờ bị xoá.** Khi liên kết, tên thành viên được **chụp lại** vào `name` |
| Đổi tên thành viên | Ngày giỗ đổi theo ngay — đó chính là lý do liên kết tồn tại |
| Xoá thành viên | `ON DELETE SET NULL`. Liên kết đứt, **ngày giỗ còn nguyên**, quay về tên đã chụp |
| Migration | Bản ghi cũ để `memberId = NULL`. **Không đoán** ngày giỗ cũ ứng với ai — đoán sai còn tệ hơn để trống |

SQLite không ALTER TABLE thêm được khoá ngoại nên v2→v3 phải dựng bảng mới rồi chép
sang — đúng chỗ dễ mất dữ liệu nhất, nên có test dựng file v2 thật rồi mở bằng Room v3
và đối chiếu **từng cột**.

## Gate 2 — "Hôm nay" qua nửa đêm

Lỗi cũ (`PHASE_5_AUDIT.md §C`): "hôm nay" chốt lúc tạo ViewModel ⇒ app mở qua 00:00 vẫn
hiện ngày hôm qua, ngày giỗ **đúng hôm nay** bị hiện thành *"Ngày mai"*.

```
DateProvider (fun interface)  ← nguồn duy nhất, tiêm qua AppContainer
        ▼
ViewModel giữ "hôm nay" là STATE, không phải hằng số
        ▼
LifecycleEventEffect(ON_RESUME) → đọc lại khi màn hình quay lại tiền cảnh
```

**Không** bộ đếm · **không** AlarmManager/WorkManager · **không** việc chạy nền ·
**không** wake lock · **không** dependency mới (`lifecycle-runtime-compose` đã có sẵn).

Test tiêm ngày giả nên **không chờ đồng hồ thật một giây nào**.

## Gate 3 — Đo hiệu năng: **không tối ưu gì cả**

SM-A325F · Android 13 · arm64-v8a. Trung vị 21 lần sau khi làm nóng.

| Phép đo | Lần đầu | Ổn định | Ngưỡng |
|---|---:|---:|---:|
| Nạp dataset + dựng engine | 29,6 ms | 29,7 ms | < 300 ms |
| Dựng lưới 1 tháng | ~81 ms | **2,27 ms** | < 16 ms |
| 365 lượt chuyển đổi | — | 20,3 ms | < 200 ms |
| Quy đổi 1 ngày giỗ | — | **0,16 ms** | < 16 ms |
| 10 / 50 / 100 ngày giỗ | — | 1,22 / 5,13 / 9,00 ms | tuyến tính |
| Marker lịch, 50 ngày giỗ | — | 1,17 ms | < 16 ms |
| Quy đổi **tệ nhất** (dò hết 1901–2100) | — | **0,13 ms** | < 300 ms |
| Cold start (release) | 1,06–1,62 s | | |
| Warm start (release) | 102–179 ms | | |

**Bài học về phương pháp đo:** bản đo đầu chỉ làm nóng 3 vòng và cho ra *84 ms cho một
tháng* trong khi *365 ngày chỉ 29 ms* — con số vô lý đó là **lỗi phương pháp**, không
phải lỗi sản phẩm. Làm nóng đủ thì mới so được. Suýt nữa đã "tối ưu" một thứ không hỏng.

**Kết luận: không thêm cache, không memoize, không index, không đổi kiến trúc.** Mọi
thao tác thường gặp đều dưới một khung hình 60 Hz. Nỗi lo "dò hết 200 năm" hoá ra tốn
0,13 ms.

## Gate 4 — Release

| | |
|---|---|
| APK release | **1,62 MB** (debug 13,4 MB) — R8 + shrinkResources |
| AAB | **3,87 MB**, build thành công |
| Quyền | chỉ `com.nepnha.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION` (AndroidX tự thêm). **KHÔNG INTERNET** |
| `android.permission.DUMP` | chỉ là **rào bảo vệ** trên receiver của `androidx.profileinstaller` (`android:permission=`), không phải quyền app xin |
| applicationId | `com.nepnha` · versionCode 1 · versionName `0.1.0-mvp` · targetSdk 37 · minSdk 26 |
| Nhãn | "Nếp Nhà" |
| Icon | adaptive `mipmap-anydpi-v26`; minSdk 26 nên mọi máy hỗ trợ đều dùng được |
| Asset lịch trong release | có, 19.946 B |
| File test lọt vào release | **0** |
| Ký | **CHƯA có release keystore** — xem dưới |

### Ký — việc còn lại của chủ dự án

Dự án **chưa có** signing config, và audit này **không tạo** keystore nào. Để kiểm thử
trên máy thật, APK release được ký bằng **khoá debug của SDK** ở ngoài Gradle:

```bash
apksigner sign --ks ~/.android/debug.keystore --ks-pass pass:android \
  --key-pass pass:android --ks-key-alias androiddebugkey \
  --out release-signed.apk app-release-unsigned.apk
```

Bản ký kiểu này **chỉ để kiểm thử**, không dùng để phát hành. Trước khi lên Play cần:
tạo upload keystore, cấu hình `signingConfigs` đọc từ biến môi trường hoặc
`keystore.properties` **không commit**, và bật Play App Signing.

## Gate 4.D — Quyết định sao lưu

**Hiện trạng:** `android:allowBackup="false"` (chốt từ Phase 0), không có
`dataExtractionRules`.

| | |
|---|---|
| Nghĩa là | Android Auto Backup **không** chạy. Dữ liệu Room và DataStore **không** rời khỏi máy |
| Rủi ro thật | Người dùng đổi điện thoại là **mất toàn bộ** gia phả và ngày giỗ, và app **chưa có** xuất/nhập |
| Nếu bật `allowBackup=true` | Dữ liệu gia đình sẽ được tải lên Google Drive của người dùng — **trái thẳng** lời hứa cốt lõi "mọi thứ nằm trên máy bạn" đang in trong màn Cài đặt |
| `dataExtractionRules` (API 31+) | Cho phép tắt sao lưu đám mây mà vẫn cho chuyển máy-sang-máy. Nhưng minSdk là **26**: trên API 26–30 chỉ có `allowBackup`, bật lên là bật cả đám mây |

**Quyết định: GIỮ `allowBackup="false"`.** Lời hứa riêng tư là tính năng cốt lõi của
sản phẩm, không phải thứ đánh đổi được. Rủi ro mất dữ liệu là **thật** và cách xử lý
đúng là **xuất/nhập cục bộ**, không phải mở cửa cho đám mây.

⚠️ **Xuất/nhập cục bộ là điều kiện tiên quyết trước khi phát hành công khai.** Đề xuất
làm ở Phase 7 (P0), trước cả thông báo.

## Bug tìm được ở Gate 4 — chỉ xuất hiện ở bản release

**R8 đổi tên hằng enum**: `LEAP_MONTH_ONLY → g`, `SKIP → f`, `MALE → …`

`MemorialRepository`/`MemberRepository` ghi `enum.name` thành chuỗi trong Room rồi đọc
lại bằng so sánh tên. Trong **cùng một bản build** thì vẫn khớp — nên **toàn bộ test
trên bản debug không bao giờ bắt được**. Nhưng bản build sau có thể gán chữ cái khác:
người dùng chọn "chỉ tháng nhuận", cập nhật app, và lựa chọn **âm thầm** quay về mặc
định. Không thông báo, không cách nào biết.

Cùng rủi ro với `gender` và `lunarBirthSource`.

**Sửa:** rule ProGuard giữ nguyên tên hằng cho mọi enum trong `com.nepnha.**`.
**Chặn tái phát:** `tools/check_release_mapping.py` đọc `mapping.txt` và bắt lỗi nếu
bất kỳ hằng nào bị đổi tên. Đã kiểm ngược bằng mapping giả — script thoát mã 1 đúng như
mong đợi, không phải cái sàng thủng.

**Xác minh trên máy thật:** tạo ngày giỗ với policy không mặc định trên **bản release**,
force-stop, mở lại ⇒ cả ba lựa chọn còn nguyên.
