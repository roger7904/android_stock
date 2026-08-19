package com.rogerchang.twsestock.domain.model

/**
 * 數值與它的基準相比的結果，決定顯示什麼顏色。
 *
 * 台股慣例紅漲綠跌，與歐美市場相反。[FLAT] 與 [UNKNOWN] 都用中性色，
 * 但兩者不合併：「持平」與「沒有基準可比」是不同的事實。
 */
enum class PriceTrend {
    RISE,
    FALL,
    FLAT,
    UNKNOWN,
}

/** 沒有基準就不猜。顏色本身就是訊號，上錯色比不上色更糟。 */
fun resolveTrend(value: Double?, reference: Double?): PriceTrend = when {
    value == null || reference == null -> PriceTrend.UNKNOWN
    value > reference -> PriceTrend.RISE
    value < reference -> PriceTrend.FALL
    else -> PriceTrend.FLAT
}

/** 收盤價對月平均價。 */
fun Stock.closingPriceTrend(): PriceTrend = resolveTrend(closingPrice, monthlyAveragePrice)

/** 漲跌價差對 0。 */
fun Stock.changeTrend(): PriceTrend = resolveTrend(change, reference = 0.0)
