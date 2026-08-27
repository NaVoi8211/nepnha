# Phase 7 — Xuất / nhập dữ liệu cục bộ

> **Hợp đồng chốt trước khi viết code.** Mọi quyết định dưới đây được ghi ra trước,
> rồi mới hiện thực đúng như vậy.
>
> Lý do tính năng này tồn tại: `PHASE_6_RELEASE.md §Gate 4.D` đã quyết định giữ
> `allowBackup="false"` để dữ liệu gia đình **không bao giờ** rời khỏi máy qua đám mây.
> Cái giá là đổi điện thoại thì mất sạch. Xuất/nhập cục bộ là câu trả lời đúng cho cái
> giá đó, và là **điều kiện tiên quyết trước khi phát hành công khai**.

---

## A. Xuất những gì

| Nhóm | Trường |
|---|---|
| Gia đình | tên |
| Thành viên | họ tên · giới tính · ngày sinh dương · ngày sinh âm (ngày/tháng/năm/nhuận/nguồn) · vai trò · ghi chú |
| Ngày giỗ | tên · ngày âm · tháng âm · quy tắc tháng nhuận · quy tắc ngày thiếu · ghi chú · liên kết tới thành viên |
| Tuỳ chọn | tín chủ (trỏ tới một thành viên trong chính file) |

## B. **Không** xuất

| Không xuất | Vì sao |
|---|---|
| `id` của Room | Là chi tiết lưu trữ nội bộ. File chỉ dùng **tham chiếu nội bộ file** để giữ quan hệ |
| `createdAt` / `updatedAt` | Không phải dữ liệu người dùng nhập. Thứ tự danh sách được giữ bằng **thứ tự phần tử trong file** |
| Bất cứ thứ gì của lịch âm | Dataset đã đóng băng và nằm trong APK. Không có lý do gì để nhét vào bản sao lưu |
| Đường dẫn file, thông tin thiết bị, số nhận dạng | Không cần cho việc khôi phục, và là dữ liệu người dùng không hề yêu cầu |

## C. `formatVersion`

**`formatVersion = 1`.** Số nguyên, tăng khi và chỉ khi có thay đổi **phá vỡ tương thích**.

## D. Độc lập với Room như thế nào

Định dạng được **hợp đồng này** định nghĩa, không suy ra từ schema.

* Tên trường trong file là tên **miền nghiệp vụ**, không phải tên cột.
* Enum ghi bằng **hằng chuỗi tường minh** khai báo trong module sao lưu — **không**
  dùng `enum.name`. Phase 6 đã dạy đúng bài này: R8 đổi tên hằng enum và dữ liệu ghi
  bởi bản build trước thành không đọc được. Định dạng file trên đĩa **không được**
  phụ thuộc vào tên ký hiệu trong mã.
* Đổi schema Room ⇒ đổi lớp ánh xạ, **không** đổi định dạng file.

## E. Chiến lược ID khi nhập

* File dùng `ref` — số nguyên **chỉ có nghĩa trong chính file đó**, để nối ngày giỗ với
  thành viên.
* Nhập **luôn tạo bản ghi mới** với id mới do Room sinh.
* Trong một giao dịch, giữ bảng ánh xạ `ref cũ → id mới`; ngày giỗ được gắn `memberId`
  theo bảng đó.
* **Không** dùng id trong file làm id trong máy.

## F. Trùng lặp

**Chỉ THÊM. Không gộp, không ghi đè, không xoá.**

Tuyệt đối **không** upsert theo tên: hai người trùng tên là **hai người**. Máy không có
cách nào biết "Nguyễn Văn A" trong file có phải "Nguyễn Văn A" trên máy hay không, và
đoán sai ở đây là trộn lẫn hai người trong gia phả — hỏng nặng hơn nhiều so với việc để
người dùng tự xoá bản trùng.

Hệ quả phải nói thẳng trong giao diện: nhập cùng một file hai lần sẽ có hai bản.

## G. Trường lạ / phiên bản tương lai

| Tình huống | Xử lý |
|---|---|
| Trường **lạ** trong một `formatVersion` đã hỗ trợ | **Bỏ qua**, không lỗi. Tương thích tiến |
| `formatVersion` **lớn hơn** bản hỗ trợ | **Từ chối** kèm thông báo rõ. Không đoán |
| `formatVersion` **nhỏ hơn** | Hôm nay chỉ có v1 nên chưa phát sinh. Khi có v2 sẽ thêm hàm nâng cấp v1→v2 chạy trước khi kiểm tra |
| Thiếu `formatVersion` | **Từ chối**. Không mặc định là v1 |

## H. Tính nguyên tử

```
file → đọc → phân tích → KIỂM TRA TOÀN BỘ → xem trước → người dùng xác nhận
     → MỘT giao dịch Room → commit
```

* Phân tích hoặc kiểm tra hỏng ⇒ **database không bị chạm tới một dòng nào**.
* Giao dịch hỏng giữa chừng ⇒ **rollback toàn bộ**.
* **Không có** nhập một phần.

## I. Quy tắc kiểm tra

| # | Quy tắc |
|---|---|
| 1 | JSON hợp lệ, đối tượng gốc là một object |
| 2 | `formatVersion` có mặt, là số nguyên, ≤ bản hỗ trợ |
| 3 | Mỗi thành viên có `ref` là số nguyên, **duy nhất** trong file |
| 4 | `fullName` không rỗng sau khi trim, ≤ 200 ký tự |
| 5 | `gender` thuộc tập hằng đã biết |
| 6 | Ngày sinh dương, nếu có, là ISO `yyyy-MM-dd` **có thật** |
| 7 | Ngày sinh âm, nếu có, đủ ngày/tháng/năm; ngày 1..30, tháng 1..12, năm 1900..2100 |
| 8 | Ngày giỗ: `lunarDay` 1..30, `lunarMonth` 1..12 |
| 9 | `leapMonthPolicy` và `missingDayPolicy` thuộc tập hằng đã biết |
| 10 | `memberRef` của ngày giỗ, nếu có, **phải trỏ tới một `ref` có thật trong file** |
| 11 | `primaryMemberRef`, nếu có, phải trỏ tới một `ref` có thật |
| 12 | `name` của ngày giỗ không rỗng, ≤ 200 ký tự; `note`/`role` ≤ 1000 ký tự |
| 13 | `checksum` khớp — xem §K |

Mọi lỗi được **gom lại và báo cùng lúc**, không dừng ở lỗi đầu tiên: người dùng cần
thấy toàn bộ vấn đề của file chứ không phải sửa từng vòng.

## J. Riêng tư và an toàn

* File sao lưu là **văn bản thuần, KHÔNG mã hoá**. Giao diện nói đúng điều đó.
* Dùng **Storage Access Framework** — người dùng tự chọn chỗ lưu và chọn file để đọc.
  App chỉ chạm đúng file người dùng chỉ định.
* **Không** xin quyền lưu trữ. **Không** INTERNET. **Không** tự gửi đi đâu.
* **Không** ghi log nội dung sao lưu, tên người, hay ngày giỗ.
* Nếu người dùng chọn Google Drive trong bộ chọn file của Android, đó là hành vi của
  **Android và người dùng**, không phải app tích hợp đám mây.
* Phase 7 **không** thêm mã hoá: nó sẽ kéo theo quản lý mật khẩu/khoá, và mất mật khẩu
  đồng nghĩa mất sạch bản sao lưu. Nếu sau này thấy cần thì phải bàn đánh đổi trước.

## K. Checksum

**Có** — `checksum` là SHA-256 của một **chuỗi chuẩn hoá** dựng từ chính các giá trị
nghiệp vụ (cùng một hàm dùng cho cả xuất lẫn nhập), không phải của văn bản JSON thô.
Nhờ vậy nó miễn nhiễm với khác biệt về định dạng/thứ tự khoá.

Nói cho đúng phạm vi:

* ✅ Bắt được **hỏng dữ liệu ngoài ý muốn** — một chữ số bị lật trong `lunarDay` mà
  JSON vẫn phân tích được thì checksum không khớp và app từ chối nhập.
* ❌ **Không phải mã hoá.** File vẫn đọc được bằng mắt thường.
* ❌ **Không chống sửa có chủ ý.** Ai sửa file cũng tính lại được checksum.

## L. Nâng cấp giữa các `formatVersion`

Hôm nay chỉ có v1 nên chưa có hàm nâng cấp nào. Chính sách: khi lên v2, importer giữ
khả năng đọc v1 bằng một hàm nâng cấp chạy **trước** bước kiểm tra, và bộ test phải có
một file v1 thật để chống hồi quy.

---

## Bố cục file

```json
{
  "formatVersion": 1,
  "exportedAt": "2026-08-27T10:30:00Z",
  "appVersionName": "0.1.0-mvp",
  "checksum": "sha256:…",
  "data": {
    "familyName": "Gia đình tôi",
    "primaryMemberRef": 1,
    "members": [
      {
        "ref": 1,
        "fullName": "Nguyễn Văn A",
        "gender": "male",
        "solarBirthDate": "1950-03-14",
        "lunarBirthDate": { "day": 26, "month": 1, "year": 1950, "leapMonth": false },
        "role": "Trưởng nam",
        "note": null
      }
    ],
    "memorials": [
      {
        "name": "Cụ ông Nguyễn Văn A",
        "memberRef": 1,
        "lunarDay": 30,
        "lunarMonth": 7,
        "leapMonthPolicy": "common_month",
        "missingDayPolicy": "last_valid_day",
        "note": null
      }
    ]
  }
}
```

Hằng chuỗi cho enum — **cố định vĩnh viễn**, không được đổi theo tên ký hiệu trong mã:

| Miền | Hằng trên file |
|---|---|
| `Gender` | `male` · `female` · `unspecified` |
| `LunarBirthDate.Source` | `user_provided` |
| `LeapMonthPolicy` | `common_month` · `leap_month_preferred` · `leap_month_only` |
| `MissingDayPolicy` | `last_valid_day` · `skip` |

## Tín chủ khi nhập

`primaryMemberRef` chỉ được áp dụng khi **máy hiện chưa chọn tín chủ**. Đã có rồi thì
giữ nguyên lựa chọn hiện tại và nói rõ trong màn xem trước. Nhập dữ liệu không được
âm thầm đổi một lựa chọn mà người dùng đã tự tay làm.

## Kết quả kiểm chứng

Đo trên **Samsung Galaxy A32 (SM-A325F, Android 13, arm64-v8a)** — không phải emulator.

### Test tự động

| Bộ | Số test | Kết quả |
|---|---|---|
| Unit (JVM) | 149 (18 của `BackupCodecTest`) | 149 pass |
| Instrumented (A32) | 64 (8 `BackupRepositoryTest` + 5 `BackupFlowTest`) | 64 pass |

### Kiểm chứng thủ công trên bản RELEASE đã minify

Bản debug **không** chứng minh được gì về R8: hợp đồng §D nói định dạng không được
phụ thuộc tên ký hiệu, và chỉ bản release mới kiểm được điều đó. Các bước dưới đây
chạy trên APK release (R8 + resource shrinking) đã ký, cài bằng `adb install`.

| # | Việc | Kết quả |
|---|---|---|
| 1 | Nhập liệu qua UI: 1 thành viên (nữ, sinh âm 30/6/1950 **nhuận**, vai trò), đặt tín chủ, 1 ngày giỗ 30/7 âm với **cả hai policy đều không mặc định** (`leap_month_only` + `skip`) | ✅ |
| 2 | Xuất qua SAF vào `Download/` | "Đã xuất 1 thành viên và 1 ngày giỗ." |
| 3 | Kéo file về máy, đọc bằng mắt | `"gender": "female"`, `"leapMonthPolicy": "leap_month_only"`, `"missingDayPolicy": "skip"` — **hằng chuỗi nguyên vẹn, không bị R8 đổi thành `a`/`b`** |
| 4 | Tính lại checksum bằng một bản cài đặt **Python độc lập** dựng từ hợp đồng | Trùng khớp từng ký tự với checksum trong file |
| 5 | Nhập một file dựng sẵn có dấu tiếng Việt (`Trần Thị Bưởi`, `Lê Văn Đức`, ghi chú `ắ ằ ẵ ọ ữ`) + một khoá lạ `unknownFutureField` | "Đã thêm 2 thành viên và 2 ngày giỗ." — dấu hiển thị đúng, khoá lạ bị bỏ qua |
| 6 | Tín chủ trong file (`ref` 2) khi máy **đã có** tín chủ | Không bị đổi — vẫn là người cũ (§Tín chủ khi nhập) |
| 7 | `force-stop` rồi mở lại | 3 thành viên, 3 ngày giỗ còn nguyên; resolver tính đúng 5/2/2027 kèm cờ "Đã điều chỉnh ngày" |
| 8 | **Xoay màn hình** khi đang mở màn xem trước | Dialog và số liệu giữ nguyên; bấm Huỷ sau khi xoay → **không ghi gì** (vẫn 3 thành viên) |
| 9 | Bật **chế độ máy bay**, xuất lại rồi nhập lại chính file đó | "Đã xuất 3…" → "Đã thêm 3…" → 6 thành viên. Đúng ngữ nghĩa CHỈ-THÊM |
| 10 | File xuất lần 2 | `memberRef` đã ánh xạ lại đúng sang `ref` mới; giữ đủ cả ba tổ hợp policy |
| 11 | Lật **một chữ số** `lunarDay: 30 → 20` (JSON vẫn hợp lệ) rồi nhập | "File có dấu hiệu bị hỏng…" — từ chối, database không đổi (vẫn 6 thành viên) |
| 12 | `logcat` sau toàn bộ thao tác | Không có tên người, ngày giỗ hay nội dung file nào bị ghi ra (§J) |

### Thành phần đóng băng — xác nhận không đụng tới

| Hạng mục | Trạng thái |
|---|---|
| `core/lunar` | 0 thay đổi (`git status` sạch) |
| `assets/lunar/vn_lunar_v1.bin` | SHA-256 `b9f9613a…20f33d` — không đổi |
| Quyền INTERNET | Không khai báo; app chạy đủ chức năng trong chế độ máy bay |
| Dependency | Không thêm gì — `libs.versions.toml` và `build.gradle.kts` không đổi |
| Enum lưu xuống DB qua R8 | `tools/check_release_mapping.py`: 9/9 hằng giữ nguyên tên |

## Một lỗi đã tìm ra và đã sửa

`AppContainer.backupRepository` ban đầu là `var … = null` gán sau khi dựng. Bản
`TestEnvironment` quên gán, và `SettingsViewModel.export()` có nhánh
`?: return` — nên nút "Xuất dữ liệu" **im lặng không làm gì**: không lỗi, không thông
báo, không log. Bốn test giao diện phát hiện ra nó, nhưng chỉ dưới dạng "chờ 5 giây
không thấy dialog", tức là triệu chứng chứ không phải nguyên nhân.

Sửa bằng cách bỏ hẳn kiểu nullable: `backupRepository` thành **tham số constructor bắt
buộc**. Trình biên dịch giờ bắt lỗi đó thay cho người đọc, và cả hai nhánh
`?: return` không còn tồn tại để im lặng nữa. Đây là **lỗi production**, không phải
lỗi test — bản test chỉ là nơi nó lộ ra.

## Giới hạn còn lại — nói thẳng

1. **Tên gia đình trong file không ghi đè tên hiện có.** Nhập một file có
   `familyName: "Nhà họ Nguyễn"` vào máy đang là "Gia đình tôi" thì tên **giữ nguyên**
   là "Gia đình tôi". Đúng với nguyên tắc chỉ-thêm-không-ghi-đè, nhưng có thể trái với
   trông đợi của người dùng khi khôi phục sang máy mới. `familyName` chỉ được dùng khi
   máy chưa có gia đình nào.
2. **Nhập hai lần là nhân đôi.** Không có gộp trùng, và màn xem trước có nói rõ. Đây là
   lựa chọn có chủ ý (§F): không có cách nào biết chắc hai người trùng tên là một người.
3. **Checksum không chống sửa có chủ ý.** Ai sửa file cũng tính lại được — nó chỉ bắt
   hỏng ngoài ý muốn.
4. **File không mã hoá.** Có nói thẳng trong màn Cài đặt.
5. **Chưa có keystore phát hành.** APK release trong đợt kiểm này ký bằng
   `debug.keystore` chỉ để cài thử — xem `docs/PHASE_6_RELEASE.md`.
