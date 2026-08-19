package com.rogerchang.twsestock.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// 忠實對應 API 回傳的形狀：每個欄位都是 String，包含數字與日期。
//
// 全部 nullable 並給預設值不是防禦性的雜訊。kotlinx.serialization 遇到缺少的
// 非 optional 欄位會讓整份文件解析失敗，一列壞掉會賠上另外 1,377 列；
// 宣告成 optional 才能讓 mapper 只丟掉那一列。
// 字串怎麼轉型、空字串代表什麼，一律是 mapper 的事，這裡不做任何解讀。

/** 上市個股日成交資訊 `/exchangeReport/STOCK_DAY_ALL`。 */
@Serializable
internal data class DailyTradeDto(
    @SerialName("Date") val date: String? = null,
    @SerialName("Code") val code: String? = null,
    @SerialName("Name") val name: String? = null,
    @SerialName("TradeVolume") val tradeVolume: String? = null,
    @SerialName("TradeValue") val tradeValue: String? = null,
    @SerialName("OpeningPrice") val openingPrice: String? = null,
    @SerialName("HighestPrice") val highestPrice: String? = null,
    @SerialName("LowestPrice") val lowestPrice: String? = null,
    @SerialName("ClosingPrice") val closingPrice: String? = null,
    @SerialName("Change") val change: String? = null,
    @SerialName("Transaction") val transaction: String? = null,
)

/** 上市個股日收盤價及月平均價 `/exchangeReport/STOCK_DAY_AVG_ALL`。 */
@Serializable
internal data class MonthlyAverageDto(
    @SerialName("Date") val date: String? = null,
    @SerialName("Code") val code: String? = null,
    @SerialName("Name") val name: String? = null,
    @SerialName("ClosingPrice") val closingPrice: String? = null,
    @SerialName("MonthlyAveragePrice") val monthlyAveragePrice: String? = null,
)

/** 上市個股日本益比、殖利率及股價淨值比 `/exchangeReport/BWIBBU_ALL`。 */
@Serializable
internal data class ValuationRatioDto(
    @SerialName("Date") val date: String? = null,
    @SerialName("Code") val code: String? = null,
    @SerialName("Name") val name: String? = null,
    @SerialName("PEratio") val peRatio: String? = null,
    @SerialName("DividendYield") val dividendYield: String? = null,
    @SerialName("PBratio") val pbRatio: String? = null,
)
