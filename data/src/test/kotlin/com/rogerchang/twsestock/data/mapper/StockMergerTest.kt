package com.rogerchang.twsestock.data.mapper

import com.google.common.truth.Truth.assertThat
import com.rogerchang.twsestock.domain.model.Stock
import org.junit.Test
import java.time.LocalDate

class StockMergerTest {
    private val today = LocalDate.of(2026, 8, 14)
    private val yesterday = today.minusDays(1)

    private fun trade(
        code: String = "2330",
        date: LocalDate = today,
        tradeVolume: Long? = 1_000L,
        closingPrice: Double? = 100.0,
    ) = DailyTrade(
        code = code,
        name = "台積電",
        date = date,
        tradeVolume = tradeVolume,
        tradeValue = 100_000L,
        transaction = 10L,
        openingPrice = 99.0,
        highestPrice = 101.0,
        lowestPrice = 98.0,
        closingPrice = closingPrice,
        change = 0.5,
    )

    @Test
    fun `三支同日時完整合併`() {
        val merged = mergeStocks(
            dailyTrades = listOf(trade()),
            monthlyAverages = listOf(MonthlyAverage("2330", today, 100.0, 95.0)),
            valuations = listOf(ValuationRatio("2330", today, 20.0, 2.0, 5.0)),
        )

        val stock = merged.single()
        assertThat(stock.monthlyAveragePrice).isEqualTo(95.0)
        assertThat(stock.valuation?.peRatio).isEqualTo(20.0)
    }

    @Test
    fun `輔助來源日期落後時不採用`() {
        // 拿昨天的月平均價替今天的收盤價上色，輸出的不是過期數字而是錯的訊號。
        val merged = mergeStocks(
            dailyTrades = listOf(trade()),
            monthlyAverages = listOf(MonthlyAverage("2330", yesterday, 100.0, 95.0)),
            valuations = listOf(ValuationRatio("2330", yesterday, 20.0, 2.0, 5.0)),
        )

        val stock = merged.single()
        assertThat(stock.monthlyAveragePrice).isNull()
        assertThat(stock.valuation).isNull()
    }

    @Test
    fun `交易所沒發布本益比的標的仍然留在清單裡`() {
        val merged = mergeStocks(
            dailyTrades = listOf(trade(code = "00981A")),
            monthlyAverages = emptyList(),
            valuations = emptyList(),
        )

        assertThat(merged.map(Stock::code)).containsExactly("00981A")
        assertThat(merged.single().valuation).isNull()
    }

    @Test
    fun `主檔沒有的代號不會被帶進來`() {
        // 月平均價那支有兩萬多筆權證，只有與主檔的交集才該出現在畫面上。
        val merged = mergeStocks(
            dailyTrades = listOf(trade()),
            monthlyAverages = listOf(
                MonthlyAverage("2330", today, 100.0, 95.0),
                MonthlyAverage("03001T", today, 1.0, 1.2),
            ),
            valuations = emptyList(),
        )

        assertThat(merged.map(Stock::code)).containsExactly("2330")
    }

    @Test
    fun `當日無成交時價格清成缺值`() {
        val merged = mergeStocks(
            dailyTrades = listOf(trade(tradeVolume = 0L, closingPrice = 0.0)),
            monthlyAverages = listOf(MonthlyAverage("2330", today, 50.0, 95.0)),
            valuations = emptyList(),
        )

        val stock = merged.single()
        assertThat(stock.hasNoTrades).isTrue()
        assertThat(stock.openingPrice).isNull()
        // 沒有成交就沒有價格，不該從另一支端點補一個看起來像收盤價的數字進來。
        assertThat(stock.closingPrice).isNull()
        assertThat(stock.tradeVolume).isEqualTo(0L)
    }

    @Test
    fun `有成交但收盤價缺值時退回月平均價那支`() {
        val merged = mergeStocks(
            dailyTrades = listOf(trade(closingPrice = null)),
            monthlyAverages = listOf(MonthlyAverage("2330", today, 88.0, 95.0)),
            valuations = emptyList(),
        )

        assertThat(merged.single().closingPrice).isEqualTo(88.0)
    }
}
