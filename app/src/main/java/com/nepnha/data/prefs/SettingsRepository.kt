package com.nepnha.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Cài đặt cục bộ.
 *
 * DataStore CHỈ giữ tuỳ chọn của người dùng. Sự tồn tại của dữ liệu (gia đình,
 * thành viên) là việc của Room — nếu để cả hai cùng nói thì sẽ có ngày DataStore
 * bảo "đã khởi tạo" trong khi Room rỗng.
 *
 * **Tín chủ** nằm ở đây chứ không phải một cột `isPrimary` trong bảng members: cột
 * đó cho phép trạng thái sai (hai người cùng `true`), một giá trị đơn thì không thể.
 */
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    /** `null` = **chưa chọn**. Không bao giờ tự suy ra người nào. */
    val primaryMemberId: Flow<Long?> = dataStore.data.map { prefs ->
        prefs[KEY_PRIMARY_MEMBER_ID]
    }

    suspend fun setPrimaryMemberId(id: Long?) {
        dataStore.edit { prefs ->
            if (id == null) prefs.remove(KEY_PRIMARY_MEMBER_ID) else prefs[KEY_PRIMARY_MEMBER_ID] = id
        }
    }

    private companion object {
        val KEY_PRIMARY_MEMBER_ID = longPreferencesKey("primary_member_id")
    }
}
