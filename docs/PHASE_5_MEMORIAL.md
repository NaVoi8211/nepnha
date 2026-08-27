# Phase 5 — Ngày giỗ và sự kiện

> Tầng nghiệp vụ ngày giỗ. Engine lịch âm, dataset và API của Phase 3 **không đổi một
> dòng nào**.

```
sha256 dataset  b9f9613a0d1974ac82a024a737b8b40cbd1588869881db13c90f4c90a020f33d   (không đổi)
```

## Ranh giới trách nhiệm

```
core/lunar            "tháng đó bao nhiêu ngày?"  "năm đó nhuận tháng mấy?"
   │                  "ngày âm này là ngày dương nào?"          ĐÓNG BĂNG
   ▼
domain/calendar       LunarCalendarService — quy LunarResult về hai trạng thái
   ▼
domain/event          MemorialDateResolver — NƠI DUY NHẤT quyết định
   │                  · tháng thường hay tháng nhuận
   │                  · mùng 30 trong tháng thiếu thì làm gì
   │                  · năm nào bị bỏ qua
   ▼
data/db + repository  ngày âm GỐC, không bao giờ ghi đè
   ▼
ViewModel             quy đổi xong xuôi rồi mới đưa ra
   ▼
UI                    chỉ vẽ
```

Engine **không bao giờ tự lùi 30 về 29**. Nó trả `NonexistentLunarDate(lastValidDay)`
và dừng ở đó.

## Mô hình dữ liệu

Bảng `memorials` lưu **ngày âm gốc**: `lunarDay`, `lunarMonth`, `leapMonthPolicy`,
`missingDayPolicy`, `note`.

Hai điều cố ý **không** lưu:

| Không lưu | Vì sao |
|---|---|
| Ngày dương | Đổi mỗi năm và phụ thuộc quy tắc. Lưu là đóng băng một năm cụ thể vào dữ liệu và mất ý định ban đầu |
| Cột `isLeapMonth` riêng | Tính nhuận đã nằm trong `leapMonthPolicy`. Hai nguồn sự thật cho cùng một điều là mở đường cho mâu thuẫn |

Policy lưu **theo từng ngày giỗ**, không phải cài đặt toàn app — mỗi người mất trong
nhà có thể theo một tập quán khác nhau. Chốt từ Phase 0.

## Quy tắc tháng nhuận

| Policy | Nghĩa |
|---|---|
| `COMMON_MONTH_DEFAULT` | Mặc định. Luôn tháng thường, kể cả năm có tháng nhuận cùng số |
| `LEAP_MONTH_PREFERRED` | Năm có tháng nhuận thì giỗ tháng nhuận; năm không có thì **lùi về tháng thường và báo rõ** |
| `LEAP_MONTH_ONLY` | Chỉ tháng nhuận. Năm không có ⇒ `Skipped(NO_LEAP_MONTH)`, không tự lùi |

**Vì sao giữ cả ba.** `LEAP_MONTH_PREFERRED` đã có từ Phase 0 và là phương án dùng
được trong thực tế: tháng nhuận mang một số cụ thể chỉ quay lại sau nhiều chục năm,
nên nếu không có đường lùi thì gia đình **không có ngày giỗ nào** trong hầu hết các
năm. `LEAP_MONTH_ONLY` là phương án nghiêm ngặt mà Phase 5 yêu cầu. Hai thứ khác nhau
thật, không phải một thứ đặt hai tên.

## Quy tắc ngày thiếu

| Policy | Nghĩa |
|---|---|
| `LAST_VALID_DAY_OF_MONTH` | Mặc định. Dùng ngày cuối tháng, **kèm `wasAdjusted = true`** |
| `SKIP` | Bỏ qua năm đó — `Skipped(MISSING_DAY)` |

Trong cả hai trường hợp, `Memorial.lunarDay` trong Room **vẫn là 30 vĩnh viễn**. 29
chỉ là `effectiveLunarDay` của một năm cụ thể.

## Kết quả quy đổi

```kotlin
sealed interface MemorialResolution {
    data class Resolved(val date: ResolvedMemorialDate)
    data class Skipped(val lunarYear: Int, val reason: Reason)
    enum class Reason { MISSING_DAY, NO_LEAP_MONTH, OUT_OF_SUPPORTED_RANGE }
}
```

Dùng sealed thay vì nhồi một trường `status` vào `ResolvedMemorialDate`: một năm bị bỏ
qua thì **không có** ngày dương, nên để trường đó nullable là mời gọi lỗi. Kiểu ở đây
bảo đảm có `Resolved` thì chắc chắn có `solarDate`.

## Ngày giỗ kế tiếp

1. Bắt đầu từ năm âm của **hôm nay**.
2. Quy đổi; nếu ngày dương chưa qua thì đó là lần kế tiếp. **Ngày giỗ của đúng hôm nay
   vẫn tính là sắp tới** — hôm nay là ngày phải làm cỗ, không phải ngày đã lỡ.
3. Nếu đã qua hoặc bị policy bỏ qua thì dò năm sau.
4. Dò tới **hết phạm vi dữ liệu** (1901–2100), không dùng cửa sổ cố định. Bản đầu giới
   hạn 25 năm với lý do "phủ chu kỳ Meton"; đo cạn kiệt cho thấy khoảng cách thật giữa
   hai lần giỗ liên tiếp lên tới **114 năm âm** — xem
   [PHASE_5_AUDIT.md §B](PHASE_5_AUDIT.md). Vòng lặp vẫn hữu hạn vì phạm vi hữu hạn.
5. Không tìm được thì trả `null`; **đó là trạng thái hợp lệ mà UI phải nói ra**, không
   phải lỗi và không được giấu ngày giỗ đi.

Mọi so sánh "đã qua hay chưa" dùng **ngày dương**, không dùng khoảng cách âm lịch.

## Giao diện

Không thuật ngữ kỹ thuật nào lọt ra màn hình. Người dùng thấy hai câu hỏi thường:

```
☐ Ngày này thuộc tháng nhuận
     └─ Năm không có tháng nhuận thì
        ○ Tính vào tháng thường          → LEAP_MONTH_PREFERRED
        ○ Bỏ qua năm đó                  → LEAP_MONTH_ONLY

Nếu tháng đó chỉ có 29 ngày
  ○ Giỗ vào ngày cuối tháng              → LAST_VALID_DAY_OF_MONTH
  ○ Bỏ qua năm đó                        → SKIP
```

Biểu mẫu **xem trước** lần giỗ kế tiếp ngay trong lúc gõ. Nếu ngày 30 phải lùi về 29,
người dùng biết **trước khi lưu**:

> Tháng 7 âm năm nay không có ngày 30. Nếp Nhà tính ngày giỗ vào ngày 29 — ngày cuối
> tháng, theo lựa chọn của bạn.

Trên màn Lịch, ngày có giỗ mang một chấm đỏ nhỏ; chọn ngày đó thì hiện danh sách.
Nhiều người mất cùng ngày đều được giữ. Màn Nhà hiện ba ngày giỗ gần nhất.

## Nâng cấp cơ sở dữ liệu

v1 → v2 chỉ **thêm** bảng `memorials`; `families` và `members` không bị đụng, nên gia
phả người dùng nhập ở Phase 2 còn nguyên. `MemorialMigrationTest` dựng một file SQLite
v1 thật rồi mở bằng Room v2 và kiểm cả dữ liệu cũ lẫn bảng mới.

Cố ý **không** thêm `androidx.room:room-testing`: Room tự đối chiếu schema khi mở nên
dựng thẳng file v1 đã đủ chặt.

## Không thuộc Phase 5

Thông báo, nhắc trước, sao lưu, xuất/nhập, đồng bộ, chia sẻ. Phase 5B trở đi.

## Giới hạn của riêng Phase 5

| # | Giới hạn |
|---|---|
| **M1** | Chưa lưu **năm mất**, nên chưa hiện được "giỗ năm thứ N" |
| **M2** | `LEAP_MONTH_ONLY` cho một tháng chưa từng nhuận trong 1901–2100 (182/2160 cấu hình) thì **không có** lần giỗ nào — UI nói rõ chứ không giấu |
| **M3** | Ngày "hôm nay" chốt một lần lúc tạo ViewModel, như Phase 4 (giới hạn P1) |
| **M4** | Chưa có tìm kiếm/lọc trong danh sách ngày giỗ |
