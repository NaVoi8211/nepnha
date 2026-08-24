package com.nepnha

import android.app.Application

/**
 * Application entry point + gốc của **manual DI**.
 *
 * Quyết định kiến trúc (Phase 0): KHÔNG dùng Hilt/Dagger/Koin.
 * Toàn bộ dependency được khởi tạo `by lazy` trong [AppContainer]; ViewModel lấy
 * container qua `viewModelFactory`. Lý do: project nhỏ (4 tab), máy dev 8GB RAM —
 * annotation processor của Hilt làm chậm build và tốn RAM daemon mà không đổi lại
 * lợi ích gì ở quy mô này.
 *
 * Nguyên tắc: container CHỈ giữ những gì thật sự dùng chung (database, repository,
 * engine). Không biến nó thành service locator cho mọi thứ.
 */
class NepNhaApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

/**
 * Nơi duy nhất tạo dependency dùng chung.
 *
 * Phase 0: cố ý để trống. Sẽ được điền dần:
 *  - Phase 2: `NepNhaDatabase`, `FamilyRepository`
 *  - Phase 3: `VietnameseLunarCalendar`
 *  - Phase 4: `ContentRepository` (đọc các file JSON trong assets/content)
 *  - Phase 5: `PrayerTemplateEngine`
 *  - Phase 7: `MemorialRepository`, `EventCalculator`
 *  - Phase 8: `AlarmScheduler`
 *  - Phase 9: `AudioPlayer`
 *
 * Mọi thứ đều `by lazy` để app khởi động nhanh và không preload dữ liệu lớn.
 */
class AppContainer(@Suppress("unused") private val app: Application)
