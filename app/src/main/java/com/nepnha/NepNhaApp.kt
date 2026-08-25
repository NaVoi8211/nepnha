package com.nepnha

import android.app.Application
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.nepnha.data.db.NepNhaDatabase
import com.nepnha.data.prefs.SettingsRepository
import com.nepnha.data.repository.FamilyOverviewSource
import com.nepnha.data.repository.FamilyRepository
import com.nepnha.data.repository.MemberRepository
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
) {
    val familyOverview = FamilyOverviewSource(familyRepository, memberRepository, settingsRepository)

    companion object {
        fun create(context: Context): AppContainer {
            val database = NepNhaDatabase.create(context)
            val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
                scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
                produceFile = { context.applicationContext.preferencesDataStoreFile("settings") },
            )
            val settings = SettingsRepository(dataStore)
            return AppContainer(
                familyRepository = FamilyRepository(database.familyDao()),
                memberRepository = MemberRepository(database.memberDao(), settings),
                settingsRepository = settings,
            )
        }
    }
}
