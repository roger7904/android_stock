package com.rogerchang.twsestock.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

@Composable
fun TwseStockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

        darkTheme -> BrandDarkColors
        else -> BrandLightColors
    }

    // flat 在這裡才決定：「持平」應該是當下這套配色的一般文字色，包含 dynamic color。
    val stockColors = (if (darkTheme) DarkStockColors else LightStockColors)
        .copy(flat = colorScheme.onSurface)

    CompositionLocalProvider(LocalStockColors provides stockColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = TwseTypography,
            content = content,
        )
    }
}
