# Phase 5 — Hardening audit trước Phase 6

> Kiểm toán commit `9da8015`. Sửa tối thiểu những gì audit **chứng minh** là hỏng;
> phần còn lại chỉ ghi nhận, không tự mở rộng phạm vi.

## Tóm tắt

| # | Mục | Kết luận |
|---|---|---|
| **A** | Semantics quy đổi ngày giỗ | ⚠️ **2 lỗi** — đã sửa |
| **B** | Cửa sổ dò 25 năm | ⛔ **Lỗi nặng** — đã sửa |
| **C** | Vòng đời "hôm nay" | ⚠️ **Bug**, hoãn sang Phase 6 |
| **E** | Đếm ngược | ✅ PASS, đã thêm test khoá |
| **F** | Chỗ cho `deathYear` sau này | ✅ Không có blocker |
| **G** | Thuật ngữ Nhà / người / ngày giỗ | ⚠️ **Có mơ hồ**, ghi nhận, không sửa |
| **H** | `SourceHygieneTest` | ⚠️ Quá hẹp — đã mở rộng |

---

## B — Cửa sổ dò: lý do cũ **sai**

Bản Phase 5 giới hạn 25 năm âm với lý do *"phủ trọn chu kỳ Meton 19 năm"*. Đo cạn kiệt
**2.160 cấu hình × 200 năm = 432.000 lượt quy đổi** cho thấy lý do đó không đúng:

```
tổng cấu hình        : 2160
có ít nhất 1 lần giỗ : 1978        không có lần nào: 182
khoảng cách lớn nhất : 114 năm âm   (30/4 LEAP_MONTH_ONLY/SKIP)
   gap=114  30/4 ONLY/SKIP      gap=95  30/5 ONLY/SKIP
   gap=76   30/3 ONLY/SKIP      gap=68  30/7 ONLY/SKIP
   gap=57   1/2 và 2/2 ONLY (cả hai policy ngày thiếu)
```

Tháng nhuận mang một **số cụ thể** không lặp theo chu kỳ 19 năm; kết hợp với `SKIP` và
ngày 30 (đòi tháng nhuận đó phải có 30 ngày) thì khoảng cách giãn ra tới hơn một thế kỷ.

**Hệ quả của bản cũ:** ngày giỗ có thật ở năm thứ 26–114 bị báo là *"không có ngày giỗ
nào"*.

**Cách sửa:** bỏ hằng số phỏng đoán. Dò tới **hết phạm vi dữ liệu** (1901–2100). Vẫn
hữu hạn vì phạm vi hữu hạn — nhiều nhất ~200 lượt tra bảng, và trường hợp thường gặp
thoát ngay vòng đầu.

**Test:** `MemorialSearchWindowTest` — chứng minh **0/1978** cấu hình có lần giỗ bị bỏ
sót, **182** cấu hình bất khả thi vẫn trả `null` chứ không bịa ngày, và vòng dò tệ nhất
kết thúc dưới 500 ms.

---

## A — Hai lỗi trong quy đổi

### A1. Hai điều chỉnh đè nhau, người dùng chỉ nghe một nửa

Ngày giỗ **30 tháng 7 nhuận**, năm 2026: không có tháng 7 nhuận (⇒ lùi tháng thường)
**và** tháng 7 thường chỉ có 29 ngày (⇒ lùi ngày). Hai điều chỉnh **độc lập, xảy ra
cùng lúc**, nhưng `AdjustmentReason` là một enum nên nhánh sau ghi đè nhánh trước.

Người dùng chỉ được báo *"tháng này không có ngày 30"* và **không hề biết** app đã tự
chuyển từ tháng nhuận sang tháng thường.

**Sửa:** thay enum bằng hai cờ độc lập `dayWasShortened` và `fellBackToCommonMonth`;
`wasAdjusted` là hợp của hai. Biểu mẫu nay in **cả hai** câu giải thích.

### A2. "Ngoài phạm vi" bị báo nhầm thành "không có tháng nhuận"

`leapMonthOf()` trả `null` cho **cả hai** trường hợp: năm không nhuận, và năm ngoài
phạm vi dữ liệu. Với `LEAP_MONTH_ONLY`, năm ngoài phạm vi bị gán
`Reason.NO_LEAP_MONTH`, và biểu mẫu khuyên người dùng *"hãy chọn Tính vào tháng
thường"* — lời khuyên cho một vấn đề không phải của họ.

**Sửa:** hỏi trước bằng **tháng thường** (luôn tồn tại nếu năm đó có dữ liệu) để tách
bạch hai lý do.

### A3–A5 PASS

Không đường nào ghi `effectiveLunarDay` ngược vào `lunarDay`; `MemorialResolution`
sealed nên `Resolved` luôn có `solarDate`; UI không có nhánh nullable nào hiển thị được
ngày sai.

---

## C — "Hôm nay" chốt lúc tạo ViewModel

**Đây là bug, không phải limitation** — app hiển thị **thông tin sai**, không phải
thiếu thông tin.

| Tình huống | Hành vi hiện tại |
|---|---|
| Mở app qua 00:00 (app còn trong nền) | ViewModel còn sống ⇒ màn Nhà vẫn hiện ngày **hôm qua**, ngày âm hôm qua, đếm ngược lệch 1 |
| Xoay màn hình | ViewModel sống sót ⇒ vẫn sai |
| Activity bị huỷ và dựng lại | ViewModel còn trong `ViewModelStore` ⇒ vẫn sai |
| Tiến trình bị giết rồi mở lại | ViewModel dựng mới ⇒ **đúng** |

Ảnh hưởng nặng nhất: ngày giỗ **đúng hôm nay** sẽ hiện *"Ngày mai"*.

**Không sửa ở audit này** — cần một tín hiệu đổi ngày (`ACTION_DATE_CHANGED` hoặc kiểm
lại khi `onResume`), là thay đổi vòng đời chứ không phải sửa một dòng.

**Acceptance criteria cho Phase 6:**
1. Đưa app ra nền, chỉnh đồng hồ máy qua 00:00, mở lại ⇒ màn Nhà hiện ngày mới.
2. Ngày giỗ rơi đúng ngày mới hiện *"Hôm nay"*.
3. Không có bộ đếm chạy nền, không thức tỉnh định kỳ.
4. Test đo được: tiêm nguồn ngày, phát tín hiệu đổi ngày, khẳng định state đổi.

---

## E — Đếm ngược: PASS

`0 → "Hôm nay"`, `1 → "Ngày mai"`, còn lại `"Còn X ngày"`. Không có đường nào ra
*"Còn 0 ngày"*. `daysUntil` không bao giờ âm vì `nextOccurrence` chỉ trả ngày ≥ hôm nay.
Đã thêm test khoá cả hai tính chất.

---

## F — Chỗ cho `deathYear`: không có blocker

Thêm sau này chỉ cần một cột `deathLunarYear INTEGER` **nullable** (migration v2→v3
thuần thêm cột, `null` = chưa biết, không cần backfill). "Giỗ lần thứ N" =
`resolved.lunarYear − deathLunarYear`, tính được hoàn toàn từ dữ liệu đã có.

**Không triển khai ở Phase 5.**

---

## G — Thuật ngữ: có một mơ hồ thật

App đang có **hai khái niệm "người" không liên quan nhau**:

| | Ở đâu | Là gì |
|---|---|---|
| `FamilyMember` | tab Gia đình | người **đang sống** trong nhà, có ngày sinh, có tín chủ |
| `Memorial.name` | Ngày giỗ | chuỗi tự do, **không** nối với thành viên nào |

Người dùng thêm "Cụ ông Nguyễn Văn A" ở tab Gia đình rồi lại gõ đúng tên đó khi tạo
ngày giỗ sẽ có **hai bản ghi rời nhau**, và app không biết đó là một người.

Ngoài ra "Nhà" (tab) và "Gia đình" (tab) là hai từ gần nghĩa cho hai thứ khác nhau —
"Nhà" là màn hình hôm nay, "Gia đình" là danh sách người.

**Không sửa ở audit này** — đây là quyết định sản phẩm, không phải lỗi kỹ thuật. Đề
xuất tối thiểu cho Phase 6: thêm `memberId` **nullable** vào `memorials` để nối tuỳ
chọn, giữ `name` làm chỗ dựa khi người mất không có trong danh sách thành viên.

---

## D — Audit trực quan trên máy thật: **1 lỗi**

Chạy ở **720×1600 / 320dpi** (ép bằng `wm size` + `wm density`) — hẹp hơn A32 thật,
đúng loại máy phổ thông mà người dùng lớn tuổi hay dùng.

### D1. Tên dài bóp nát phần đếm ngược

Với tên người mất dài (rất thường gặp: *"Cụ cố Nguyễn Văn An - Thượng thư Bộ Lễ thời
Nguyễn"*), hàng tiêu đề của thẻ ngày giỗ không có ràng buộc chiều rộng nào. Tên chiếm
hết chỗ, đẩy **"Còn 14 ngày"** xuống một cột rộng **một ký tự**, xuống dòng theo từng
chữ cái, và thẻ phình cao gần 700 px toàn khoảng trắng.

**Test tự động không bắt được** — node văn bản vẫn tồn tại và vẫn "displayed". Chỉ mắt
người nhìn ảnh chụp mới thấy. Đây đúng là lý do §D tồn tại.

Có ở **cả hai** chỗ: `MemorialListScreen.MemorialRow` và `HomeScreen.UpcomingRow`.

**Sửa:** tên nhận `Modifier.weight(1f)`, `maxLines = 2`, cắt bằng dấu ba chấm; phần
đếm ngược `maxLines = 1`, `softWrap = false`, chừa khoảng cách trái.

### Các mục còn lại: PASS

| Mục | Kết quả ở 720×1600 |
|---|---|
| Tiếng Việt dài, xuống dòng | ✅ mọi thẻ xuống dòng sạch, không tràn ngang |
| Biểu mẫu | ✅ ô Tên cuộn ngang khi tên dài, không vỡ bố cục |
| Trạng thái lỗi | ✅ ba lỗi hiện cùng lúc, ô tô đỏ, chữ tiếng Việt thường |
| Xem trước điều chỉnh | ✅ hiện đúng 10/09/2026 kèm lời giải thích |
| Ngày đã điều chỉnh | ✅ nhãn đỏ "Đã điều chỉnh ngày" ở cả danh sách lẫn màn Nhà |
| Lịch + marker | ✅ chấm đỏ đúng ngày, chọn ngày hiện thẻ |
| Cuộn, back, vùng bấm | ✅ |
| contentDescription ô lịch | ✅ có kèm ", có ngày giỗ" |
| Chế độ máy bay | ✅ toàn bộ luồng chạy bình thường |

---

## H — `SourceHygieneTest` mở rộng

Bản cũ chỉ bắt đúng một mẫu. Nay quét cả `.kt` lẫn `.xml` cho bảy loại dấu vết mà
trình biên dịch **không** bắt được: escaping hỏng, dấu xung đột merge, `TODO()`,
`printStackTrace()`, `System.out.print`. Thêm test kiểm `strings.xml` không có tham số
`%s`/`%d` thiếu chỉ số, và một test **tự kiểm** rằng bộ quy tắc thật sự khớp được mẫu
lỗi đã từng lọt.
