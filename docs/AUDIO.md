# Audio "Khấn cùng tôi" (chốt Phase 0, implement Phase 9)

## Ràng buộc

- Audio **cục bộ**, đóng gói trong `assets/audio/`. Không streaming, không network.
- **Không** AI voice, **không** clone giọng người thật, **không** cloud TTS.
- **Không** dùng `android.speech.TextToSpeech`: giọng tiếng Việt phụ thuộc voice data
  đã tải và có thể fallback ra mạng ⇒ vi phạm yêu cầu Airplane Mode.

## Chia theo SEGMENT, không phải một file dài

```
assets/audio/pr_mung_mot_gia_tien/segment_01.mp3
assets/audio/pr_mung_mot_gia_tien/segment_02.mp3
assets/audio/pr_mung_mot_gia_tien/segment_03.mp3
```

Nhịp mong muốn:

```
app đọc segment_01 → im lặng (pauseAfterMillis) → người dùng đọc theo
                   → app đọc segment_02 → …
```

Chia nhỏ còn có nghĩa: mỗi lúc chỉ một file ngắn được nạp — **không load toàn bộ
audio vào RAM**, quan trọng với Galaxy A32.

Metadata segment nằm trong `prayers.json` (`segments[]`), không hard-code trong Kotlin.

## Abstraction

`com.nepnha.audio.AudioPlayer` (đã tạo ở Phase 0) + `PrayerAudioSegment`.

MVP chỉ có một implementation: `AndroidMediaPlayerAudioPlayer` dùng
`android.media.MediaPlayer` với `AssetFileDescriptor`. Interface tồn tại **chỉ** để
sau này thay bằng Media3 mà không phải sửa UI/domain — không mở rộng thêm gì khác.

Yêu cầu implementation (Phase 9):

- `release()` bắt buộc gọi khi rời màn hình (`DisposableEffect`) — `MediaPlayer` giữ
  codec native, rò rỉ là thấy ngay trên A32.
- Tạo `MediaPlayer` cho từng segment rồi giải phóng, không giữ nhiều instance.
- Phát audio phải tôn trọng nút im lặng và audio focus ở mức tối thiểu.
- Không có audio ⇒ chỉ ẩn nút "Khấn cùng tôi", các chế độ đọc khác vẫn chạy.

## Phạm vi MVP

**1 bài khấn có audio là đủ** để kiểm chứng luồng. Recording do chủ dự án cung cấp.
Chưa cần ở Phase 0.
