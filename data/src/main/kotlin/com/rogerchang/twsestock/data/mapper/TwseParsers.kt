package com.rogerchang.twsestock.data.mapper

import java.time.DateTimeException
import java.time.LocalDate

// 三支端點的每個欄位都是字串，數字與日期也不例外。
// 這些函式只做一件事：轉不出來就回傳 null。null 代表「不知道」，永遠不是 0。

/** 民國與西元的差。 */
private const val ROC_ERA_OFFSET = 1911

/** 民國年 + MMDD，現行 payload 是 7 碼（1150814），舊格式有 6 碼。 */
private val ROC_DATE_LENGTHS = 6..7

/** 解析 `"14.79"`、`"-0.0100"`。順手把千分位逗號拿掉，其他 TWSE 端點會帶。 */
internal fun parseDecimal(raw: String?): Double? = raw?.replace(",", "")?.trim()?.toDoubleOrNull()

/** 解析 `"49694719"`。逗號處理同 [parseDecimal]。 */
internal fun parseCount(raw: String?): Long? = raw?.replace(",", "")?.trim()?.toLongOrNull()

/**
 * 民國日期 `"1150814"` → `2026-08-14`。
 *
 * 後四碼固定是月日，前面全是民國年，所以現行的三位年與舊的兩位年不需要各寫一套。
 * 格式不對就回傳 null 而不是拋例外——一列日期壞掉不該賠上整份回應。
 *
 * 長度是比對已知的兩種格式，而不是只擋「太短」。八碼的 `"11150814"` 會被讀成民國 1115 年，
 * 得到一個西元 3026 年的合法日期：照收、錯的、而且無聲無息。
 */
internal fun parseRocDate(raw: String?): LocalDate? {
    val digits = raw?.trim().orEmpty()
    if (digits.length !in ROC_DATE_LENGTHS || !digits.all(Char::isDigit)) return null

    val rocYear = digits.dropLast(4).toInt()
    if (rocYear <= 0) return null
    val month = digits.substring(digits.length - 4, digits.length - 2).toInt()
    val day = digits.takeLast(2).toInt()

    return try {
        LocalDate.of(rocYear + ROC_ERA_OFFSET, month, day)
    } catch (_: DateTimeException) {
        // 13 月、2 月 31 日這類值。
        null
    }
}
