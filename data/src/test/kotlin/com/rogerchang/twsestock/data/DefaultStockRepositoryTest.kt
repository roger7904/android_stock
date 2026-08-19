package com.rogerchang.twsestock.data

import com.google.common.truth.Truth.assertThat
import com.rogerchang.twsestock.data.remote.DailyTradeDto
import com.rogerchang.twsestock.data.remote.MonthlyAverageDto
import com.rogerchang.twsestock.data.remote.TwseApi
import com.rogerchang.twsestock.data.remote.ValuationRatioDto
import com.rogerchang.twsestock.domain.model.DataError
import com.rogerchang.twsestock.domain.model.Stock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test
import java.io.IOException
import java.net.SocketTimeoutException

class DefaultStockRepositoryTest {
    private val dao = FakeStockDao()

    private fun repository(api: TwseApi) = DefaultStockRepository(api, dao, Dispatchers.Unconfined)

    @Test
    fun `刷新成功後清單從快取推出來`() = runTest {
        val repository = repository(FakeTwseApi())

        assertThat(repository.refresh()).isNull()

        val stocks = repository.observeStocks().first()
        assertThat(stocks.map(Stock::code)).containsExactly("2330", "1101")
        assertThat(stocks.first { it.code == "2330" }.monthlyAveragePrice).isEqualTo(1150.0)
        assertThat(stocks.first { it.code == "2330" }.valuation?.peRatio).isEqualTo(27.76)
    }

    @Test
    fun `主檔失敗時回報錯誤且不動快取`() = runTest {
        repository(FakeTwseApi()).refresh()
        val before = dao.observeStocks().first()

        val error = repository(FakeTwseApi(dailyTradeFailure = SocketTimeoutException())).refresh()

        assertThat(error).isEqualTo(DataError.Timeout)
        assertThat(dao.observeStocks().first()).isEqualTo(before)
    }

    @Test
    fun `輔助來源失敗時清單照常，只是欄位缺值`() = runTest {
        val repository = repository(FakeTwseApi(valuationFailure = IOException()))

        assertThat(repository.refresh()).isNull()

        val stocks = repository.observeStocks().first()
        assertThat(stocks).hasSize(2)
        assertThat(stocks.all { it.valuation == null }).isTrue()
        // 月平均價那支沒有掛，這欄要還在。
        assertThat(stocks.first { it.code == "2330" }.monthlyAveragePrice).isEqualTo(1150.0)
    }

    @Test
    fun `重新刷新會換掉整批資料`() = runTest {
        val repository = repository(FakeTwseApi())
        repository.refresh()
        repository.refresh()

        // 整批換掉而不是 upsert，已下市的標的才不會永遠留著。
        assertThat(repository.observeStocks().first()).hasSize(2)
    }
}

private class FakeTwseApi(
    private val dailyTradeFailure: Throwable? = null,
    private val valuationFailure: Throwable? = null,
) : TwseApi {
    override suspend fun dailyTrades(): List<DailyTradeDto> {
        dailyTradeFailure?.let { throw it }
        return listOf(
            DailyTradeDto(
                date = "1150814", code = "2330", name = "台積電",
                tradeVolume = "49694719", tradeValue = "58033244174",
                openingPrice = "1165.00", highestPrice = "1180.00", lowestPrice = "1160.00",
                closingPrice = "1175.00", change = "10.0000", transaction = "89123",
            ),
            DailyTradeDto(
                date = "1150814", code = "1101", name = "台泥",
                tradeVolume = "8912345", tradeValue = "312345678",
                openingPrice = "35.00", highestPrice = "35.50", lowestPrice = "34.80",
                closingPrice = "35.05", change = "-0.0500", transaction = "5123",
            ),
        )
    }

    override suspend fun monthlyAverages(): List<MonthlyAverageDto> = listOf(
        MonthlyAverageDto(date = "1150814", code = "2330", closingPrice = "1175.00", monthlyAveragePrice = "1150.00"),
        MonthlyAverageDto(date = "1150814", code = "1101", closingPrice = "35.05", monthlyAveragePrice = "35.60"),
    )

    override suspend fun valuationRatios(): List<ValuationRatioDto> {
        valuationFailure?.let { throw it }
        return listOf(
            ValuationRatioDto(
                date = "1150814", code = "2330", peRatio = "27.76", dividendYield = "1.36", pbRatio = "8.10",
            ),
        )
    }
}
