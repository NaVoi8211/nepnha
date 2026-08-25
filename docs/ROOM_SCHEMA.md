# Room — chỉ cho user-generated data

> Chốt ở Phase 0. **Family + FamilyMember đã implement ở Phase 2** (database version 1,
> schema export tại `app/schemas/`). Memorial thuộc Phase 7.

Room **không** lưu nội dung cố định (nghi lễ, văn khấn, checklist) — xem
[CONTENT_SCHEMA.md](CONTENT_SCHEMA.md).

## Entity

### Family
| Cột | Kiểu | Ghi chú |
|---|---|---|
| `id` | `Long` PK autoGenerate | |
| `name` | `String` | |
| `createdAt` | `Long` | epoch millis |

### FamilyMember
| Cột | Kiểu | Ghi chú |
|---|---|---|
| `id` | `Long` PK autoGenerate | |
| `familyId` | `Long` FK → Family, `onDelete = CASCADE`, có index | |
| `fullName` | `String` | |
| `gender` | `String` | enum lưu dạng tên: `NAM` / `NU` / `KHAC` |
| `solarBirthDate` | `String?` | ISO `yyyy-MM-dd`, nullable (có thể không biết) |
| `lunarBirthDay` | `Int?` | **tách 3 cột thay vì 1 chuỗi** — xem giải thích dưới |
| `lunarBirthMonth` | `Int?` | |
| `lunarBirthYear` | `Int?` | |
| `lunarBirthIsLeapMonth` | `Boolean` | mặc định `false` |
| `lunarBirthSource` | `String?` | `USER_PROVIDED` khi có ngày âm, `null` khi không. Xem giải thích (4) |
| `role` | `String?` | vai trò trong nhà (ông, bà, bố, mẹ…) |
| `note` | `String?` | |

### Memorial
| Cột | Kiểu | Ghi chú |
|---|---|---|
| `id` | `Long` PK autoGenerate | |
| `familyId` | `Long` FK → Family, CASCADE, có index | |
| `personName` | `String` | |
| `relationship` | `String` | |
| `lunarMonth` | `Int` | |
| `lunarDay` | `Int` | **Dữ liệu gốc, bất biến** — giữ đúng con số người dùng khai (kể cả 30 ở tháng thiếu). Ngày thực tế của từng năm là giá trị dẫn xuất, xem [MEMORIAL_RULES.md](MEMORIAL_RULES.md) |
| `isLeapMonth` | `Boolean` | mặc định `false` — người mất có thực sự mất vào tháng nhuận hay không |
| `leapMonthPolicy` | `String` | `COMMON_MONTH_DEFAULT` (mặc định) / `LEAP_MONTH_PREFERRED`. Lưu theo từng Memorial vì tập quán mỗi nhà một khác |
| `solarDeathDate` | `String?` | ISO `yyyy-MM-dd`, nếu biết |
| `note` | `String?` | |

## Giải thích những field thêm so với đặc tả

1. **`lunarBirthDate` tách thành 4 cột** (`day/month/year/isLeapMonth`) thay vì một
   chuỗi. Lý do: ngày âm có tháng nhuận, một chuỗi `"12/07/2026"` không biểu diễn
   được "tháng 7 nhuận". Lưu số nguyên còn cho phép query trực tiếp "ai sinh tháng
   7 âm" mà không phải parse chuỗi.
2. **`Memorial.isLeapMonth`.** Cùng lý do — ngày giỗ có thể rơi vào tháng nhuận.
2b. **`Memorial.leapMonthPolicy`.** Business rule đã chốt (xem
   [MEMORIAL_RULES.md](MEMORIAL_RULES.md)): mặc định giỗ tính theo tháng **thường**,
   nhưng model phải cho phép gia đình chọn tháng nhuận. Thêm cột ngay từ version 1
   để sau này không phải migration.
3. **`gender`/`role` lưu `String`.** Đơn giản, dễ đọc khi debug bằng `adb`; enum
   chuyển đổi ở tầng repository.
4. **`lunarBirthSource`.** Từ Phase 3 sẽ có thêm ngày âm do engine quy đổi ra. Ngày
   âm **người nhà khai** và ngày âm **máy tính ra** là hai thứ khác nhau — trộn lại
   là mất dấu vết dữ liệu gốc. Thêm cột ngay ở version 1 để sau khỏi migration.
   Phase 2 chỉ có duy nhất giá trị `USER_PROVIDED`: chưa có engine thì không đoán.

## Quyết định thiết kế

- **Ngày dương lưu ISO `String`, không lưu epoch.** Đọc được bằng mắt khi dump
  database; múi giờ không bao giờ làm lệch ngày (`LocalDate` không có giờ).
- **Người dùng chính / tín chủ KHÔNG là cột trong FamilyMember**, mà là một
  `primaryMemberId` trong DataStore. Lý do: nếu là cột `isPrimary` thì database cho
  phép trạng thái sai (hai người cùng `true`, hoặc không ai `true`); một giá trị đơn
  ở DataStore thì không thể sai.
- **Không dùng `Flow` thô ra UI** — DAO trả `Flow`, repository map sang model
  domain, ViewModel phơi `StateFlow`.
- **Schema export bật** (`room.schemaLocation = app/schemas`) và `app/schemas/…/1.json`
  **được commit** để còn diff mà viết migration. Hiện là version 1 nên chưa có
  `Migration` nào.
- **Không destructive migration ở release.** Dữ liệu gia phả/ngày giỗ của người dùng
  không có bản sao trên cloud (cố ý), mất là mất thật.
