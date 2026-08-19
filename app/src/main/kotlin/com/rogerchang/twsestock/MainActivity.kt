package com.rogerchang.twsestock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rogerchang.twsestock.domain.ThemePreferences
import com.rogerchang.twsestock.domain.model.ThemeMode
import com.rogerchang.twsestock.ui.stocklist.StockListRoute
import com.rogerchang.twsestock.ui.theme.TwseStockTheme
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    // 主題在 setContent 外層就要決定，比 ViewModel 早一步，所以直接注入偏好。
    // 兩邊看的是同一條 Flow，不會各自說一套。
    private val themePreferences: ThemePreferences by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by themePreferences.observeThemeMode()
                .collectAsStateWithLifecycle(initialValue = ThemeMode.Default)

            TwseStockTheme(darkTheme = themeMode.isDark()) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StockListRoute()
                }
            }
        }
    }
}

@Composable
private fun ThemeMode.isDark(): Boolean = when (this) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}
