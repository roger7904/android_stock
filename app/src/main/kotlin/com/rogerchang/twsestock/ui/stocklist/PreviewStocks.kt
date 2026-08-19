package com.rogerchang.twsestock.ui.stocklist

import com.rogerchang.twsestock.domain.model.Stock
import com.rogerchang.twsestock.domain.model.Valuation
import java.time.LocalDate

/** Preview 與畫面測試共用的樣本，挑的是實際資料裡幾種值得盯住的情況。 */
object PreviewStocks {
    private val date = LocalDate.of(2026, 8, 14)

    /** 收盤價高於月平均、漲跌為正，兩處都該是紅字。 */
    val rising = Stock(
        code = "2330",
        name = "台積電",
        date = date,
        tradeVolume = 49_694_719L,
        tradeValue = 58_033_244_174L,
        transaction = 89_123L,
        openingPrice = 1165.0,
        highestPrice = 1180.0,
        lowestPrice = 1160.0,
        closingPrice = 1175.0,
        change = 10.0,
        monthlyAveragePrice = 1150.0,
        valuation = Valuation(peRatio = 27.76, dividendYield = 1.36, pbRatio = 8.1),
        hasNoTrades = false,
    )

    /** 收盤價低於月平均、漲跌為負，兩處都該是綠字。 */
    val falling = rising.copy(
        code = "1101",
        name = "台泥",
        openingPrice = 35.0,
        highestPrice = 35.5,
        lowestPrice = 34.8,
        closingPrice = 35.05,
        change = -0.05,
        monthlyAveragePrice = 35.6,
        tradeVolume = 8_912_345L,
        tradeValue = 312_345_678L,
        transaction = 5_123L,
    )

    /** 交易所未發布本益比的主動式 ETF，dialog 三欄都會是破折號。 */
    val withoutValuation = falling.copy(
        code = "00981A",
        name = "主動統一台股",
        valuation = null,
    )

    /** 當日無成交，價格全部清成缺值。 */
    val untraded = rising.copy(
        code = "9962",
        name = "有益",
        tradeVolume = 0L,
        tradeValue = 0L,
        transaction = 0L,
        openingPrice = null,
        highestPrice = null,
        lowestPrice = null,
        closingPrice = null,
        change = null,
        monthlyAveragePrice = 12.4,
        hasNoTrades = true,
    )

    val all = listOf(rising, falling, withoutValuation, untraded)
}
