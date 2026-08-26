# Phase 4 — Nối lịch âm vào sản phẩm

> Chỉ **product integration**. Engine, dataset, thuật toán và API của Phase 3 **không
> đổi một dòng nào**.

```
sha256 dataset  b9f9613a0d1974ac82a024a737b8b40cbd1588869881db13c90f4c90a020f33d   (không đổi)
```

## Dòng dữ liệu

```
assets/lunar/vn_lunar_v1.bin
        │  AppContainer.loadLunarCalendar(context)   ← nạp một lần lúc khởi động
        ▼
LunarDataset.parse ─► VietnameseLunarCalendar        ← core/lunar, ĐÓNG BĂNG
        ▼
LunarCalendarService                                  ← domain/calendar, MỚI
        │  quy LunarResult/LunarError về LunarDay.Known | LunarDay.Unknown(lý do)
        ▼
HomeViewModel · CalendarViewModel                     ← tính ở đây, không ở Composable
        ▼
HomeScreen · CalendarScreen                           ← chỉ vẽ, không tính
```

**Giao diện không tự tính gì cả** — kể cả tháng nhuận và can chi. Không Composable nào
gọi `LocalDate.now()` để suy ra lịch; ngày "hôm nay" đi vào ViewModel qua tham số nên
test chốt được ngày cố định.

## Vì sao có `LunarCalendarService`

UI **không được** tự xử lý `LunarResult`/`LunarError`. Để mỗi màn hình tự `when` trên
sealed error thì sớm muộn có chỗ quên một nhánh và hiện số sai. Service quy về đúng
hai trạng thái: **biết** hoặc **không biết kèm lý do**.

Service **không** chứa quy tắc lịch (nằm ở `core/lunar`) và **không** chứa quy tắc
ngày giỗ (nằm ở `domain/event/MemorialRule`, tính ở phase sau).

## Màn Nhà

```
Thứ Tư
26 tháng 8, 2026
Âm lịch
14 tháng 7 năm Bính Ngọ
```

Ngày âm và can chi gộp **một dòng** — giữ header đủ thấp để nút "Thiết lập gia đình"
không bị đẩy khỏi khung nhìn trên máy nhỏ như A32.

## Màn Lịch

Lưới tháng bắt đầu **Thứ Hai**, mỗi ô có ngày dương lớn và ngày âm nhỏ bên dưới. Mùng
1 âm hiện dạng `1/7` và được tô màu nhấn để mắt bắt được nhịp tháng âm khi lướt. Hôm
nay có viền, ngày đang chọn có nền. Dưới lưới là thẻ chi tiết đầy đủ.

Lật tháng giữ nguyên ngày đang chọn và **kẹp về ngày cuối tháng** khi tháng mới ngắn
hơn — đang ở 31/01 bấm sang tháng 2 mà không kẹp thì `DateTimeException`.

Lưới dựng bằng `Column`+`Row` chứ không `LazyVerticalGrid`: cả tháng chỉ 35–42 ô và
màn hình đã nằm trong `verticalScroll`; lồng lazy grid vào scroll là nguồn lỗi đo
chiều cao kinh điển.

## Tháng nhuận

Chữ **"nhuận"** không bao giờ bị bỏ. Tháng 7 nhuận là **lần xuất hiện thứ hai của
tháng 7**, không phải tháng 8 — hiển thị sai chỗ này là làm sai ngày giỗ của người ta.

Ca kiểm chuẩn là **1938**: tháng 8 thường bắt đầu 25/08, tháng 8 **nhuận** bắt đầu
24/09, tháng 9 bắt đầu 23/10. Có test cả ở JVM lẫn trên thiết bị.

## Khi không tra được

| Tình huống | Người dùng thấy |
|---|---|
| Ngoài 1901–2100 | "Ngày âm chỉ có trong khoảng 1901–2100" |
| Không nạp được dataset | "Lịch âm tạm thời chưa dùng được" |

Không bịa số, và **không crash**: hỏng asset thì app vẫn mở được. Trường hợp này chỉ
xảy ra khi đóng gói sai, vì checksum dataset đã bị khoá trong unit test.

## Hiệu năng

Không tối ưu sớm. Không cache, không coroutine, không worker nền. Một tháng là 28–31
lượt tra bảng; đo trên A32 là 365 lượt trong ~30 ms.

## Giới hạn của riêng Phase 4

| # | Giới hạn |
|---|---|
| **P1** | Ngày "hôm nay" chốt **một lần** lúc tạo ViewModel. App mở qua nửa đêm chưa tự đổi ngày — chưa cần bộ đếm cho tới khi có nhắc việc theo giờ |
| **P2** | Can chi chỉ có **theo năm**. API Phase 3 chỉ cung cấp `sexagenaryYear`; can chi ngày và tháng cần mở rộng API nên **không** làm ở phase này |
| **P3** | Chưa có ngày lễ, ngày giỗ, sự kiện trên lưới — thuộc phase sau |

Giới hạn L1–L6 của Phase 3 vẫn nguyên hiệu lực, xem
[PHASE_3_FINAL_HANDOFF.md](PHASE_3_FINAL_HANDOFF.md).
