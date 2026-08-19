package com.rogerchang.twsestock.data.mapper

import com.rogerchang.twsestock.domain.model.Stock
import com.rogerchang.twsestock.domain.model.Valuation
import java.time.LocalDate

/** 三支端點各自解析後的中間形狀，只在合併時用到。 */
internal data class DailyTrade(
    val code: String,
    val name: String,
    val date: LocalDate,
    val tradeVolume: Long?,
    val tradeValue: Long?,
    val transaction: Long?,
    val openingPrice: Double?,
    val highestPrice: Double?,
    val lowestPrice: Double?,
    val closingPrice: Double?,
    val change: Double?,
)

internal data class MonthlyAverage(
    val code: String,
    val date: LocalDate,
    val closingPrice: Double?,
    val monthlyAveragePrice: Double?,
)

internal data class ValuationRatio(
    val code: String,
    val date: LocalDate,
    val peRatio: Double?,
    val dividendYield: Double?,
    val pbRatio: Double?,
)

/**
 * 以日成交資訊為主檔，另外兩支做 left join。
 *
 * 主檔選 STOCK_DAY_ALL 是因為卡片上八個成交欄位全部來自它，它天然就是版型骨架。
 * 另外兩個選擇都會掉資料：以月平均價為主會灌進兩萬多筆沒有成交欄位的權證，
 * 以本益比為主則會無聲少掉 295 檔交易所不發布本益比的標的。
 *
 * **join key 是 (code, date)，不是只有 code。** 三支端點各自發布、不保證同步，
 * 只用 code 合併會把昨天的月平均價接到今天的收盤價上——而那組比較正是紅綠配色的依據，
 * 結果不是一個過期的數字，是一個錯的訊號。日期對不上就當作沒有這筆，欄位顯示「–」。
 */
internal fun mergeStocks(
    dailyTrades: List<DailyTrade>,
    monthlyAverages: List<MonthlyAverage>,
    valuations: List<ValuationRatio>,
): List<Stock> {
    val averageByKey = monthlyAverages.associateBy { it.code to it.date }
    val valuationByKey = valuations.associateBy { it.code to it.date }

    return dailyTrades.map { trade ->
        trade.toStock(
            average = averageByKey[trade.code to trade.date],
            valuation = valuationByKey[trade.code to trade.date],
        )
    }
}

private fun DailyTrade.toStock(average: MonthlyAverage?, valuation: ValuationRatio?): Stock {
    val hasNoTrades = tradeVolume == 0L

    return Stock(
        code = code,
        name = name,
        date = date,
        // 成交股數／金額／筆數是 0 是有意義的事實；價格是 0 就不是價格，下面會清掉。
        tradeVolume = tradeVolume,
        tradeValue = tradeValue,
        transaction = transaction,
        openingPrice = openingPrice.unlessUntraded(hasNoTrades),
        highestPrice = highestPrice.unlessUntraded(hasNoTrades),
        lowestPrice = lowestPrice.unlessUntraded(hasNoTrades),
        // 收盤價以主檔為準，才會與開高低同源、內部一致；
        // 只有在「有成交但收盤價缺值」時才退回月平均價那支的收盤價。
        closingPrice = if (hasNoTrades) null else closingPrice?.takeIf { it != 0.0 } ?: average?.closingPrice,
        change = change.unlessUntraded(hasNoTrades),
        monthlyAveragePrice = average?.monthlyAveragePrice,
        valuation = valuation?.let { Valuation(it.peRatio, it.dividendYield, it.pbRatio) },
        hasNoTrades = hasNoTrades,
    )
}

/** 當日無成交的列，開高低收都是 "0.00"，印出來會讓人以為股價歸零。 */
private fun Double?.unlessUntraded(hasNoTrades: Boolean): Double? = if (hasNoTrades) null else this
