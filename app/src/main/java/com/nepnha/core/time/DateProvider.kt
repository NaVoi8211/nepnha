package com.nepnha.core.time

import java.time.LocalDate

/**
 * Nguồn duy nhất trả lời "hôm nay là ngày nào".
 *
 * Tồn tại vì hai lý do, cả hai đều là bài học từ audit trước:
 *
 * 1. **Đúng.** Trước đây mỗi ViewModel tự gọi `LocalDate.now()` một lần lúc khởi tạo
 *    rồi giữ mãi. App mở qua nửa đêm là màn Nhà vẫn hiện ngày hôm qua, và ngày giỗ
 *    đúng hôm nay bị hiện thành "Ngày mai". Đó là **thông tin sai**, không phải thiếu.
 *
 * 2. **Test được.** Có một chỗ duy nhất để tiêm ngày giả thì test mô phỏng được việc
 *    qua nửa đêm mà không phải chờ đồng hồ thật.
 *
 * Cố ý **không** phải một bộ đếm: không hẹn giờ nền, không `AlarmManager`, không
 * `WorkManager`. Ngày được đọc lại khi màn hình quay lại tiền cảnh — xem
 * `LifecycleEventEffect(ON_RESUME)` ở `NepNhaShell`.
 */
fun interface DateProvider {
    fun today(): LocalDate

    companion object {
        /** Nguồn thật: đồng hồ và múi giờ của thiết bị. */
        val System: DateProvider = DateProvider { LocalDate.now() }
    }
}
