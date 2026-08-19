package com.rogerchang.twsestock.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.rogerchang.twsestock.domain.ThemePreferences
import com.rogerchang.twsestock.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

internal val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

private val ThemeModeKey = stringPreferencesKey("theme_mode")

internal class ThemeDataStore(
    private val dataStore: DataStore<Preferences>,
) : ThemePreferences {
    override fun observeThemeMode(): Flow<ThemeMode> = dataStore.data
        // 偏好檔壞掉不該把整個畫面一起帶走。退回預設值只損失一個設定，讓它拋出則是損失整個 App。
        .catch { throwable -> if (throwable is IOException) emit(emptyPreferences()) else throw throwable }
        // 存名稱不存 ordinal：有人重排 enum 時 ordinal 會無聲改變意義，
        // 而症狀會是使用者的主題莫名其妙自己換了。
        .map { preferences ->
            ThemeMode.entries.firstOrNull { it.name == preferences[ThemeModeKey] } ?: ThemeMode.Default
        }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { preferences -> preferences[ThemeModeKey] = mode.name }
    }
}
