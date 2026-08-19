package com.rogerchang.twsestock.domain

import com.rogerchang.twsestock.domain.model.Stock
import com.rogerchang.twsestock.domain.model.Valuation
import java.time.LocalDate

internal val TRADE_DATE: LocalDate = LocalDate.of(2026, 8, 14)

/** 測試只在乎少數幾個欄位，其餘給預設值，讓每個測試的意圖留在它改動的那幾行。 */
internal fun stock(
    code: String = "2330",
    name: String = "台積電",
    date: LocalDate = TRADE_DATE,
    openingPrice: Double? = 99.0,
    closingPrice: Double? = 100.0,
    monthlyAveragePrice: Double? = 100.0,
    change: Double? = 0.0,
    valuation: Valuation? = Valuation(peRatio = 20.0, dividendYield = 2.0, pbRatio = 5.0),
    hasNoTrades: Boolean = false,
) = Stock(
    code = code,
    name = name,
    date = date,
    tradeVolume = 1_000L,
    tradeValue = 100_000L,
    transaction = 10L,
    openingPrice = openingPrice,
    highestPrice = 101.0,
    lowestPrice = 98.0,
    closingPrice = closingPrice,
    change = change,
    monthlyAveragePrice = monthlyAveragePrice,
    valuation = valuation,
    hasNoTrades = hasNoTrades,
)
