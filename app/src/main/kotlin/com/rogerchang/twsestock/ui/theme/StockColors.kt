package com.rogerchang.twsestock.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.rogerchang.twsestock.domain.model.PriceTrend

/**
 * 漲、跌、持平的顏色。
 *
 * Material 的 color scheme 沒有這種角色：primary / error / tertiary 沒有一個代表「這個數字漲了」，
 * 而借 error 來表示下跌比不上色更糟——那是在說出事了，但市場只是動了。
 *
 * 紅漲綠跌是台股慣例，與歐美相反。搞反不會看起來像 bug，會看起來像市場走了反方向。
 */
@Immutable
data class StockColors(
    val rise: Color,
    val fall: Color,
    /** 持平或無從比較，用一般文字色，不是 disabled 灰。 */
    val flat: Color,
)

fun StockColors.colorFor(trend: PriceTrend): Color = when (trend) {
    PriceTrend.RISE -> rise
    PriceTrend.FALL -> fall
    PriceTrend.FLAT, PriceTrend.UNKNOWN -> flat
}

/**
 * 刻意不從 dynamic color 推導。使用者的桌布很可能產生一個綠色的 primary，
 * 讓桌布決定「漲」長什麼樣子並不合理。
 *
 * 兩色對卡片背景都在 4.5:1 以上，而且刻意讓明度差很多（紅明顯比綠深），
 * 轉成灰階後仍分得出來——收盤價只有顏色一個訊號，這個明度差就是最後的保險。
 */
internal val LightStockColors = StockColors(
    rise = Color(0xFF7F1D1D),
    fall = Color(0xFF15703A),
    flat = Color.Unspecified,
)

/** 深色主題另外挑過，不是把淺色那組拿來用——淺色的紅在深色卡片上只有 1.6:1。 */
internal val DarkStockColors = StockColors(
    rise = Color(0xFFFF8A80),
    fall = Color(0xFF7BE0A0),
    flat = Color.Unspecified,
)

/** 只有整個主題換掉時才會變，逐一追蹤讀取反而更貴，所以用 static。 */
val LocalStockColors = staticCompositionLocalOf { LightStockColors }
