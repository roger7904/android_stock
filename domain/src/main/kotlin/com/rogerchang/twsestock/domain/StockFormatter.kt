package com.rogerchang.twsestock.domain

import java.util.Locale
import kotlin.math.roundToLong

// 數字轉字串的規則放在 domain，因為它們是純函式、用測試釘住的成本極低，
// 而四捨五入、正負號、單位階梯正是容易悄悄跑掉的東西。
//
// 全部用 Locale.ROOT：跟隨裝置語系的話，小數點符號甚至數字字形都會被改掉，
// 台股股價在阿拉伯語系裝置上會變成東阿拉伯數字。

/** 缺值一律顯示這個符號（en dash）。 */
const val NO_VALUE = "–"

private val COMPACT_UNITS = listOf("K", "M", "B")

/** 價格固定兩位小數。 */
fun formatPrice(value: Double?): String =
    value?.let { String.format(Locale.ROOT, "%.2f", it) } ?: NO_VALUE

/** 本益比、殖利率、股價淨值比，格式與價格相同。 */
fun formatRatio(value: Double?): String = formatPrice(value)

/**
 * 帶正負號的漲跌價差：`+0.06`、`-0.01`、`0.00`。
 *
 * 加 0.0 是為了把 -0.001 四捨五入後的 -0.0 收斂成 0.0，否則會印出 `-0.00`。
 */
fun formatSignedChange(value: Double?): String {
    if (value == null) return NO_VALUE
    val rounded = roundTo(value, scale = 100.0)
    val sign = if (rounded > 0) "+" else ""
    return sign + String.format(Locale.ROOT, "%.2f", rounded + 0.0)
}

/**
 * 卡片上顯示的漲跌價差，前面帶方向符號：`▲ +0.06`、`▼ -0.01`、`－ 0.00`。
 *
 * 符號不是裝飾，是 WCAG 1.4.1 的要求——紅綠正好是紅綠色盲會抹平的那一組。
 * 由 formatter 產生而不是交給畫面拼，才能用測試守住，畫面也漏不掉。
 * 方向依四捨五入後的值決定，所以 -0.001 顯示 `－ 0.00` 而不是 `▼ 0.00`。
 */
fun formatChange(value: Double?): String {
    if (value == null) return NO_VALUE
    val rounded = roundTo(value, scale = 100.0)
    val arrow = when {
        rounded > 0 -> "▲"
        rounded < 0 -> "▼"
        else -> "－"
    }
    return "$arrow ${formatSignedChange(value)}"
}

/**
 * 成交筆數／股數／金額縮寫成 K/M/B，一位小數：`8.9K`、`49.7M`、`738.8M`。
 * 一千以下直接印原值，`852` 本來就夠短，寫成 `0.9K` 又長又不準。
 */
fun formatCompactCount(value: Long?): String {
    if (value == null) return NO_VALUE
    if (value < 1_000) return value.toString()

    var scaled = value / 1_000.0
    var unit = 0
    // 先四捨五入再比，999,960 才不會變成彆扭的 1000.0K。
    while (roundTo(scaled, scale = 10.0) >= 1_000 && unit < COMPACT_UNITS.lastIndex) {
        scaled /= 1_000
        unit++
    }
    return String.format(Locale.ROOT, "%.1f%s", roundTo(scaled, scale = 10.0), COMPACT_UNITS[unit])
}

private fun roundTo(value: Double, scale: Double): Double = (value * scale).roundToLong() / scale
