package com.nepnha.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.nepnha.AppContainer
import com.nepnha.data.db.NepNhaDatabase
import com.nepnha.data.prefs.SettingsRepository
import com.nepnha.data.repository.FamilyRepository
import com.nepnha.data.repository.MemberRepository
import com.nepnha.data.repository.MemorialRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import java.io.File

/**
 * Dựng một [AppContainer] hoàn toàn cô lập cho test: Room in-memory + một file
 * DataStore riêng cho mỗi lần chạy.
 *
 * Nhờ `NepNhaShell` nhận container qua tham số, test dựng được nguyên app mà không
 * cần bất kỳ hook test nào trong code production, và không đụng tới dữ liệu thật
 * trên máy.
 */
class TestEnvironment(context: Context) {

    val database: NepNhaDatabase = Room
        .inMemoryDatabaseBuilder(context, NepNhaDatabase::class.java)
        .allowMainThreadQueries()
        .build()

    private val prefsFile = File(context.cacheDir, "test_${System.nanoTime()}.preferences_pb")

    private val dataStore: DataStore<Preferences> = PreferenceDataStoreFactory.create(
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        produceFile = { prefsFile },
    )

    val settingsRepository = SettingsRepository(dataStore)
    val familyRepository = FamilyRepository(database.familyDao())
    val memberRepository = MemberRepository(database.memberDao(), settingsRepository)
    val memorialRepository = MemorialRepository(database.memorialDao())
    val backupRepository = com.nepnha.data.repository.BackupRepository(
        database, familyRepository, settingsRepository,
    )

    // Nạp dataset lịch âm THẬT từ asset: test giao diện phải nhìn thấy đúng thứ
    // người dùng nhìn thấy, không phải một engine giả.
    /**
     * Ngày giả, đổi được giữa chừng để mô phỏng qua nửa đêm mà không phải chờ đồng
     * hồ thật. `@Volatile` vì `ON_RESUME` đọc nó trên luồng chính còn test ghi từ
     * luồng của mình.
     */
    @Volatile
    var fakeToday: java.time.LocalDate = java.time.LocalDate.now()

    val container = AppContainer(
        familyRepository,
        memberRepository,
        settingsRepository,
        AppContainer.loadLunarCalendar(context),
        memorialRepository,
        backupRepository,
        dateProvider = com.nepnha.core.time.DateProvider { fakeToday },
    )

    fun close() {
        database.close()
        prefsFile.delete()
    }
}
