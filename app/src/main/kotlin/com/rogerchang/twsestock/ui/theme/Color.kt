package com.rogerchang.twsestock.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Android 12 以下沒有 dynamic color 可用，就落到這組品牌配色。
// 只覆寫 accent 家族（primary / secondary / tertiary），surface 沿用 Material baseline——
// baseline 的 surface 本來就是這個色調，重寫一遍等於手算 tonal palette 再繞回原點。
// primary 與 launcher icon 同色，App 在桌面與第一個畫面之間不會換張臉。

internal val BrandLightColors = lightColorScheme(
    primary = Color(0xFF4A3F6B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE4DEF7),
    onPrimaryContainer = Color(0xFF1B1330),
    secondary = Color(0xFF5D5A6B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE5E0F0),
    onSecondaryContainer = Color(0xFF1A1826),
    tertiary = Color(0xFF74546A),
    onTertiary = Color(0xFFFFFFFF),
)

internal val BrandDarkColors = darkColorScheme(
    primary = Color(0xFFC9BEEC),
    onPrimary = Color(0xFF302747),
    primaryContainer = Color(0xFF413760),
    onPrimaryContainer = Color(0xFFE5DEFF),
    secondary = Color(0xFFC8C3D6),
    onSecondary = Color(0xFF2F2C3C),
    secondaryContainer = Color(0xFF454253),
    onSecondaryContainer = Color(0xFFE5E0F0),
    tertiary = Color(0xFFDEBAD1),
    onTertiary = Color(0xFF412638),
)
