package com.nepnha

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.nepnha.core.lunar.LunarDataset
import com.nepnha.core.time.DateProvider
import com.nepnha.core.lunar.VietnameseLunarCalendar
import com.nepnha.data.db.NepNhaDatabase
import com.nepnha.data.prefs.SettingsRepository
import com.nepnha.data.repository.BackupRepository
import com.nepnha.data.repository.FamilyOverviewSource
import com.nepnha.data.repository.FamilyRepository
import com.nepnha.data.repository.MemberRepository
import com.nepnha.data.repository.MemorialRepository
import com.nepnha.data.repository.MemorialSource
import com.nepnha.domain.calendar.LunarCalendarService
import com.nepnha.domain.event.MemorialDateResolver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Application entry point + gốc của **manual DI**.
 *
 * Không Hilt/Dagger/Koin: app nhỏ, máy dev 8GB RAM, annotation processor chỉ làm
 * chậm build mà không đổi lại lợi ích gì ở quy mô này.
 */
class NepNhaApp : Application() {

    lateinit var container: AppContainer
        private set

    /**
     * Scope cho đúng một việc: khởi tạo gia đình mặc định lúc mở app lần đầu.
     * Không phải worker nền, không chạy định kỳ.
     */
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer.create(this)
        appScope.launch {
            container.familyRepository.ensureDefaultFamily(getString(R.string.family_default_name))
        }
    }
}

/**
 * Nơi duy nhất tạo dependency dùng chung.
 *
 * Nhận repository qua constructor (thay vì tự dựng bên trong) để test đo được: bản
 * test dựng container trên Room in-memory và DataStore tạm, không cần hook đặc biệt
 * nào trong code production.
 */
class AppContainer(
    val familyRepository: FamilyRepository,
    val memberRepository: MemberRepository,
    val settingsRepository: SettingsRepository,
    val lunarCalendar: LunarCalendarService,
    val memorialRepository: MemorialRepository,
    /**
     * Xuất/nhập cục bộ.
     *
     * Bắt buộc trong constructor chứ không phải `var` gán sau: bản đầu tiên để nó là
     * `var … = null` và bản test dựng container quên gán, nên nút "Xuất dữ liệu" **im
     * lặng không làm gì** — không lỗi, không thông báo. Kiểu không-null làm trình biên
     * dịch bắt lỗi đó thay cho người đọc.
     */
    val backupRepository: BackupRepository,
    /** Nguồn "hôm nay" duy nhất. Test tiêm bản giả để mô phỏng qua nửa đêm. */
    val dateProvider: DateProvider = DateProvider.System,
) {
    val familyOverview = FamilyOverviewSource(familyRepository, memberRepository, settingsRepository)
    val memorials = MemorialSource(familyRepository, memorialRepository)

    /** Quy tắc ngày giỗ sống ở domain, dùng chung cho Nhà, Lịch và màn danh sách. */
    val memorialResolver = MemorialDateResolver(lunarCalendar)

    companion object {

        /** Đường dẫn asset của dataset lịch âm. Xem `docs/LUNAR_DATASET_PROVENANCE.md`. */
        const val LUNAR_ASSET = "lunar/vn_lunar_v1.bin"

        /**
         * Nạp engine lịch âm từ asset.
         *
         * Dataset ~20 KB, dựng chỉ số một lần rồi bất biến; đo trên A32 là 365 lượt
         * chuyển đổi trong ~30 ms, nên nạp thẳng lúc khởi động là an toàn — không
         * cần lazy, không cần đẩy sang luồng nền.
         *
         * Hỏng asset thì trả về service không có engine thay vì ném: app vẫn mở
         * được và nói thật là lịch âm chưa dùng được. Trường hợp này chỉ xảy ra khi
         * đóng gói sai, vì checksum dataset đã bị khoá trong unit test.
         */
        fun loadLunarCalendar(context: Context): LunarCalendarService {
            val engine = runCatching {
                val bytes = context.assets.open(LUNAR_ASSET).use { it.readBytes() }
                VietnameseLunarCalendar.create(LunarDataset.parse(bytes))
            }.getOrNull()
            return LunarCalendarService(engine)
        }

        fun create(context: Context): AppContainer {
            val database = NepNhaDatabase.create(context)
            val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
                scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
                produceFile = { context.applicationContext.preferencesDataStoreFile("settings") },
            )
            val settings = SettingsRepository(dataStore)
            val familyRepository = FamilyRepository(database.familyDao())
            return AppContainer(
                familyRepository = familyRepository,
                memberRepository = MemberRepository(database.memberDao(), settings),
                settingsRepository = settings,
                lunarCalendar = loadLunarCalendar(context),
                memorialRepository = MemorialRepository(database.memorialDao()),
                backupRepository = BackupRepository(database, familyRepository, settings),
            )
        }
    }
}
