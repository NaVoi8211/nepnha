package com.nepnha.audio

/**
 * Abstraction cho local audio của tính năng "KHẤN CÙNG TÔI".
 *
 * MVP dùng `android.media.MediaPlayer` (không Media3/ExoPlayer): audio chỉ là các
 * file ngắn nằm trong `assets/audio/`, phát tuần tự, không streaming, không network.
 * Interface này tồn tại để sau này có thể thay bằng Media3 mà **không phải sửa
 * UI hay domain logic** — không nhằm mục đích nào khác, và cố ý giữ nhỏ.
 *
 * Phase 0 chỉ ĐỊNH NGHĨA hợp đồng. Implementation `AndroidMediaPlayerAudioPlayer`
 * thuộc Phase 9.
 */
interface AudioPlayer {

    /** Phát một asset trong `assets/audio/`. [onCompleted] gọi khi đoạn kết thúc. */
    fun play(assetPath: String, onCompleted: () -> Unit)

    fun pause()

    fun resume()

    /** Dừng hẳn và giải phóng tài nguyên native. Bắt buộc gọi khi rời màn hình. */
    fun release()
}

/**
 * Một đoạn của bài khấn có hướng dẫn.
 *
 * Audio được chia theo SEGMENT chứ không phải một file dài duy nhất, để dựng được
 * nhịp: app đọc một đoạn → im lặng [pauseAfterMillis] cho người dùng đọc theo →
 * đoạn tiếp theo. Chia nhỏ cũng có nghĩa mỗi lúc chỉ có một file ngắn được nạp,
 * không load toàn bộ audio vào RAM (quan trọng với Galaxy A32).
 *
 * Ví dụ đặt tên asset: `audio/prayer_mung_mot/segment_01.mp3`
 */
data class PrayerAudioSegment(
    val index: Int,
    val assetPath: String,
    /** Đoạn text tương ứng, để highlight đồng bộ khi phát. */
    val text: String,
    /** Khoảng lặng sau đoạn này, cho người dùng đọc theo. */
    val pauseAfterMillis: Long,
)
