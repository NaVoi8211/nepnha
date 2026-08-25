# Roadmap — Nếp Nhà

## Phase

| Phase | Nội dung | Trạng thái |
|---|---|---|
| 0 | Audit + architecture + dependency setup | ✅ **Xong** |
| 1 | App shell + navigation + theme + Home placeholder | ✅ **Xong** |
| 2 | Room + Family + Member | ✅ **Xong** |
| 3 | Vietnamese lunar calendar engine + tests | 🔬 Preflight xong — chờ duyệt để implement |
| 4 | Ritual content + local JSON + ritual engine | |
| 5 | Prayer template engine + Prayer Reader | |
| 6 | Auto-scroll | |
| 7 | Memorial system | |
| 8 | Local notifications | |
| 9 | Local audio + "Khấn cùng tôi" | |
| 10 | Offline audit + performance + test trên Samsung A32 | |
| 11 | Polish + bug fixing + MVP release build | |

Quy tắc: không chuyển phase khi Definition of Done của phase hiện tại chưa đạt.

## CỐ Ý KHÔNG làm trong MVP

Ghi nhận để không bị "tiện tay làm luôn":

- Social, chia sẻ, bình luận
- Tài khoản, đăng nhập, profile online
- Cloud sync, backup online, đa thiết bị
- Payment, subscription, in-app purchase
- Quảng cáo
- AI, chatbot, sinh văn khấn tự động, TTS/voice cloning
- Tử vi, phong thuỷ, bói toán, xem ngày tốt xấu, sao hạn
- Gia phả đầy đủ (nhiều đời, sơ đồ cây)
- Thương mại (bán lễ vật, đặt mâm cúng)
- Analytics, crash reporting, remote config

## Ứng viên sau MVP (chưa cam kết)

- **Export/import dữ liệu ra file cục bộ.** Cần thiết vì `allowBackup="false"` +
  không cloud ⇒ đổi máy là mất dữ liệu. Đây là món nợ kỹ thuật có ý thức.
- Content pack bổ sung nghi lễ theo vùng miền (Bắc / Trung / Nam).
- Widget màn hình chính "hôm nay là ngày gì".
- Nhắc theo giờ tuỳ chỉnh cho từng nghi lễ.
- Chế độ chữ siêu lớn cho người cao tuổi (nếu Material 3 mặc định chưa đủ).
