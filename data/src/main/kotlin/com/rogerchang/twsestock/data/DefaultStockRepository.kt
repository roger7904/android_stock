package com.rogerchang.twsestock.data

import com.rogerchang.twsestock.data.local.StockDao
import com.rogerchang.twsestock.data.mapper.mergeStocks
import com.rogerchang.twsestock.data.mapper.toDailyTrades
import com.rogerchang.twsestock.data.mapper.toEntity
import com.rogerchang.twsestock.data.mapper.toMonthlyAverages
import com.rogerchang.twsestock.data.mapper.toStock
import com.rogerchang.twsestock.data.mapper.toValuationRatios
import com.rogerchang.twsestock.data.remote.TwseApi
import com.rogerchang.twsestock.domain.StockRepository
import com.rogerchang.twsestock.domain.model.DataError
import com.rogerchang.twsestock.domain.model.Stock
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * 資料庫是唯一的真實來源，網路只往資料庫寫。
 *
 * 沒有人直接讀取回應：[observeStocks] 從快取發、[refresh] 往快取寫，
 * 所以刷新成功與冷啟動走的是同一條路徑，而不是多一條只有網路正常時才會用到的支線。
 *
 * dispatcher 預設 Default 而不是 IO：Retrofit 與 Room 各自有自己的執行緒池，
 * 這裡唯一真正吃 CPU 的是解析約 27,000 個字串再 join，那屬於 Default。
 */
internal class DefaultStockRepository(
    private val api: TwseApi,
    private val dao: StockDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : StockRepository {

    override fun observeStocks(): Flow<List<Stock>> =
        dao.observeStocks().map { entities -> entities.map { it.toStock() } }

    override suspend fun refresh(): DataError? = withContext(dispatcher) {
        val (trades, averages, valuations) = coroutineScope {
            val tradesAsync = async { fetch { api.dailyTrades() } }
            val averagesAsync = async { fetch { api.monthlyAverages() } }
            val valuationsAsync = async { fetch { api.valuationRatios() } }
            Triple(tradesAsync.await(), averagesAsync.await(), valuationsAsync.await())
        }

        // 主檔拿不到就沒有清單可以顯示，只能回報失敗。
        // 另外兩支只是補欄位，掛掉就讓那些欄位維持缺值（畫面顯示「–」），清單照常。
        val tradeRows = trades.getOrElse { return@withContext it.toDataError() }

        val stocks = mergeStocks(
            dailyTrades = tradeRows.toDailyTrades(),
            monthlyAverages = averages.getOrDefault(emptyList()).toMonthlyAverages(),
            valuations = valuations.getOrDefault(emptyList()).toValuationRatios(),
        )
        dao.replaceAll(stocks.map { it.toEntity() })
        null
    }

    /**
     * 三支請求各自捕捉自己的失敗，一支掛掉不會連坐另外兩支。
     *
     * 不用 runCatching：它是 `catch (e: Throwable)`，會連 CancellationException 一起吞掉，
     * 使用者離開畫面之後這裡還會傻傻跑完剩下的工作。
     */
    private suspend fun <T> fetch(request: suspend () -> T): Result<T> = try {
        Result.success(request())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        Result.failure(throwable)
    }
}
