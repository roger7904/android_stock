package com.rogerchang.twsestock.ui.stocklist

import com.rogerchang.twsestock.domain.ThemePreferences
import com.rogerchang.twsestock.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeThemePreferences : ThemePreferences {
    private val mode = MutableStateFlow(ThemeMode.Default)

    override fun observeThemeMode(): Flow<ThemeMode> = mode

    override suspend fun setThemeMode(mode: ThemeMode) {
        this.mode.value = mode
    }
}
