package com.rogerchang.twsestock.data.mapper

import com.google.common.truth.Truth.assertThat
import com.rogerchang.twsestock.data.remote.DailyTradeDto
import com.rogerchang.twsestock.data.remote.TwseJson
import com.rogerchang.twsestock.data.remote.ValuationRatioDto
import com.rogerchang.twsestock.domain.model.Stock
import com.rogerchang.twsestock.domain.model.Valuation
import kotlinx.serialization.json.Json
import org.junit.Test
import java.time.LocalDate

class StockMappersTest {
    @Test
    fun `解析真實回應的形狀`() {
        // 題目給的 model 沒有列出 Date，但實際回應每一列都有。
        val payload = """
            [
              {
                "Date": "1150814",
                "Code": "2330",
                "Name": "台積電",
                "TradeVolume": "49694719",
                "TradeValue": "58033244174",
                "OpeningPrice": "1165.00",
                "HighestPrice": "1180.00",
                "LowestPrice": "1160.00",
                "ClosingPrice": "1175.00",
                "Change": "10.0000",
                "Transaction": "89123"
              }
            ]
        """.trimIndent()

        val trades = TwseJson.decodeFromString<List<DailyTradeDto>>(payload).toDailyTrades()

        val trade = trades.single()
        assertThat(trade.code).isEqualTo("2330")
        assertThat(trade.date).isEqualTo(LocalDate.of(2026, 8, 14))
        assertThat(trade.closingPrice).isEqualTo(1175.0)
        assertThat(trade.change).isEqualTo(10.0)
    }

    @Test
    fun `多出來的欄位不會讓整份回應解析失敗`() {
        // 釘住 ignoreUnknownKeys 的必要性：交易所日後加一個欄位，已安裝的版本不該就此壞掉。
        val payload = """[{"Date":"1150814","Code":"2330","Name":"台積電","NewColumn":"x"}]"""

        assertThat(TwseJson.decodeFromString<List<DailyTradeDto>>(payload)).hasSize(1)
        assertThat(runCatching { Json.decodeFromString<List<DailyTradeDto>>(payload) }.isFailure).isTrue()
    }

    @Test
    fun `本益比空字串轉成缺值而不是零`() {
        val payload = """
            [{"Date":"1150814","Code":"9958","Name":"世紀鋼","PEratio":"","DividendYield":"","PBratio":"2.5"}]
        """.trimIndent()

        val ratio = TwseJson.decodeFromString<List<ValuationRatioDto>>(payload).toValuationRatios().single()

        assertThat(ratio.peRatio).isNull()
        assertThat(ratio.dividendYield).isNull()
        assertThat(ratio.pbRatio).isEqualTo(2.5)
    }

    @Test
    fun `沒有代號或日期的列直接丟掉`() {
        val dtos = listOf(
            DailyTradeDto(date = "1150814", code = "  ", name = "壞掉"),
            DailyTradeDto(date = "壞日期", code = "2330", name = "台積電"),
            DailyTradeDto(date = "1150814", code = "1101", name = "台泥"),
        )

        // 一列壞掉不該賠上整批。
        assertThat(dtos.toDailyTrades().map { it.code }).containsExactly("1101")
    }

    @Test
    fun `存回資料庫再讀出來的內容一致`() {
        val stock = Stock(
            code = "2330",
            name = "台積電",
            date = LocalDate.of(2026, 8, 14),
            tradeVolume = 49_694_719L,
            tradeValue = 58_033_244_174L,
            transaction = 89_123L,
            openingPrice = 1165.0,
            highestPrice = 1180.0,
            lowestPrice = 1160.0,
            closingPrice = 1175.0,
            change = 10.0,
            monthlyAveragePrice = 1150.0,
            valuation = Valuation(peRatio = 27.76, dividendYield = null, pbRatio = 8.1),
            hasNoTrades = false,
        )

        assertThat(stock.toEntity().toStock()).isEqualTo(stock)
    }

    @Test
    fun `交易所未發布與三欄剛好都空是兩件事`() {
        val unpublished = Stock(
            code = "00981A", name = "主動統一台股", date = LocalDate.of(2026, 8, 14),
            tradeVolume = 1L, tradeValue = 1L, transaction = 1L,
            openingPrice = 1.0, highestPrice = 1.0, lowestPrice = 1.0, closingPrice = 1.0,
            change = 0.0, monthlyAveragePrice = 1.0, valuation = null, hasNoTrades = false,
        )
        val blank = unpublished.copy(valuation = Valuation(null, null, null))

        assertThat(unpublished.toEntity().toStock().valuation).isNull()
        assertThat(blank.toEntity().toStock().valuation).isNotNull()
    }
}
