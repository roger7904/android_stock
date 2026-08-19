package com.rogerchang.twsestock.domain.model

import java.time.LocalDate

/**
 * 一檔個股當日的完整資訊，由三支 API 合併而成。
 *
 * 所有數值都是 nullable，因為 API 回傳的是字串，解析不出來代表「不知道」而不是 0。
 * 這個區別很重要：本益比有兩百多檔是空字串，填 0 會變成一個看起來合理的假數字。
 */
data class Stock(
    val code: String,
    val name: String,
    /** 資料日期，取自 STOCK_DAY_ALL。 */
    val date: LocalDate,
    val tradeVolume: Long?,
    val tradeValue: Long?,
    val transaction: Long?,
    val openingPrice: Double?,
    val highestPrice: Double?,
    val lowestPrice: Double?,
    val closingPrice: Double?,
    val change: Double?,
    /** 月平均價來源缺這檔或日期對不上時為 null。 */
    val monthlyAveragePrice: Double?,
    /** 交易所未發布本益比資訊的標的（約 295 檔）為 null。 */
    val valuation: Valuation?,
    /** 當日成交股數為 0。這種列的開高低收都是 "0.00"，合併時已清成 null。 */
    val hasNoTrades: Boolean,
)

/** 點卡片後 dialog 顯示的三個數字，各自可能缺值。 */
data class Valuation(
    val peRatio: Double?,
    val dividendYield: Double?,
    val pbRatio: Double?,
)
