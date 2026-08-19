package com.rogerchang.twsestock.domain

import com.rogerchang.twsestock.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface ThemePreferences {
    fun observeThemeMode(): Flow<ThemeMode>

    suspend fun setThemeMode(mode: ThemeMode)
}
