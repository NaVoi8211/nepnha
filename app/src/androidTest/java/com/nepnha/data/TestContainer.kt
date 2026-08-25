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

    val container = AppContainer(familyRepository, memberRepository, settingsRepository)

    fun close() {
        database.close()
        prefsFile.delete()
    }
}
