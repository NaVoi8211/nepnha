# Nội dung cố định — schema (chốt Phase 0, đổ dữ liệu ở Phase 4/5)

**Nguyên tắc: ENGINE trước, CONTENT sau.** Nội dung nghi lễ/văn khấn **không** nằm
trong Room và **không** hard-code trong Kotlin. Chúng là asset:

```
app/src/main/assets/content/rituals.json
app/src/main/assets/content/prayers.json
app/src/main/assets/content/checklists.json
app/src/main/assets/audio/…
```

Lý do: sửa một bài khấn không được kéo theo database migration hay build lại logic.
Sau này có thể ship "content pack" mà không đụng Kotlin.

## 1. rituals.json

```jsonc
{
  "contentVersion": 1,
  "rituals": [
    {
      "id": "ram_thang_7",
      "title": "Rằm tháng 7",
      "description": "Lễ Vu Lan báo hiếu và cúng cô hồn.",
      "category": "LE_TIET",           // SOC_VONG | LE_TIET | GIA_TIEN | GIO
      "lunarMonth": 7,
      "lunarDay": 15,
      "target": "GIA_TIEN",            // GIA_TIEN | THAN_LINH | ONG_TAO | PHAT | CHUNG_SINH
      "performer": "Chủ nhà hoặc người lớn tuổi nhất trong nhà",
      "checklistId": "cl_ram_thang_7",
      "prayerIds": ["pr_ram_thang_7_gia_tien"],
      "note": ""
    }
  ]
}
```

- `lunarMonth` / `lunarDay` là **ngày âm**; ngày dương do `VietnameseLunarCalendar`
  tính ra tại runtime cho từng năm — không lưu sẵn ngày dương.
- Nghi lễ đặc biệt không rơi vào một ngày âm cố định (ví dụ Thanh Minh theo tiết
  khí) sẽ cần một field kiểu `"rule": "SOLAR_TERM:THANH_MINH"`. Sẽ bổ sung khi làm
  Phase 4 — ghi ở đây để không quên.

## 2. checklists.json

```jsonc
{
  "contentVersion": 1,
  "checklists": [
    {
      "id": "cl_ram_thang_7",
      "title": "Lễ vật",
      "items": [
        { "id": "huong",  "label": "Hương",          "optional": false },
        { "id": "hoa",    "label": "Hoa tươi",       "optional": false },
        { "id": "vang_ma","label": "Vàng mã",        "optional": true,
          "note": "Tuỳ tập quán từng gia đình" }
      ]
    }
  ]
}
```

Trạng thái tick của người dùng là **user data** → nếu cần lưu, lưu ở Room/DataStore,
không ghi ngược vào asset.

## 3. prayers.json

```jsonc
{
  "contentVersion": 1,
  "prayers": [
    {
      "id": "pr_ram_thang_7_gia_tien",
      "title": "Văn khấn gia tiên rằm tháng 7",
      "body": "…{{worshipper_name}}… {{current_lunar_date}}…",
      "segments": [
        { "index": 1, "text": "…", "audioAsset": "audio/pr_ram_thang_7_gia_tien/segment_01.mp3", "pauseAfterMillis": 4000 }
      ],
      "metadata": {
        "region": "CHUNG",             // BAC | TRUNG | NAM | CHUNG
        "version": "1.0.0",
        "status": "DRAFT",             // DRAFT | REVIEWED_BY_OWNER
        "source": "Chưa xác minh nguồn",
        "editorialNote": "Bản tham khảo, giữ cấu trúc: kính lạy → xưng danh → nêu lễ → khấn nguyện → tạ"
      }
    }
  ]
}
```

`segments` là tuỳ chọn: bài nào chưa có audio thì bỏ trống, Prayer Reader vẫn chạy
chế độ STATIC và AUTO_SCROLL bình thường.

## 4. Chính sách nội dung — bắt buộc

- **Không** dùng chữ "chuẩn", "chuẩn nhất", "chính thống" ở bất kỳ đâu trong app.
- **Không** bịa nguồn, tên thầy, tên chuyên gia, tên sách nếu chưa xác minh.
  Chưa có nguồn thì `"source": "Chưa xác minh nguồn"` — nói thật.
- `status` mặc định là `DRAFT`; chỉ chủ dự án mới đổi sang `REVIEWED_BY_OWNER`.
- App hiển thị một dòng ghi chú cố định ở màn hình bài khấn:
  *"Nội dung mang tính tham khảo. Mỗi vùng miền và mỗi gia đình có thể có cách khấn khác nhau."*

## 5. Template variables

Chỉ có **một** nơi render: `domain/prayer/PrayerTemplateEngine`. Màn hình không
được tự ghép chuỗi.

| Biến | Nguồn dữ liệu |
|---|---|
| `{{worshipper_name}}` | Thành viên được chọn làm tín chủ |
| `{{worshipper_birth_year}}` | Năm sinh của tín chủ |
| `{{current_solar_date}}` | Ngày dương hôm nay |
| `{{current_lunar_date}}` | Ngày âm hôm nay (từ `VietnameseLunarCalendar`) |
| `{{family_name}}` | `Family.name` |
| `{{deceased_name}}` | Chỉ có khi mở từ một Memorial |
| `{{relationship}}` | Quan hệ với người mất |

Quy tắc xử lý:

- Biến **không có dữ liệu** ⇒ thay bằng một placeholder đọc được (ví dụ `…`) và
  đánh dấu để UI có thể nhắc người dùng bổ sung thông tin. **Không** để lộ chuỗi
  `{{…}}` ra màn hình, **không** crash.
- Biến **không nhận diện được** ⇒ giữ nguyên và log ở debug: đó là lỗi content, phải
  thấy được.
- Engine phải test được độc lập, không cần Android.
