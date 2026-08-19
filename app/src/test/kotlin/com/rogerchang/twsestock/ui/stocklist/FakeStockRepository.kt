package com.rogerchang.twsestock.ui.stocklist

import com.rogerchang.twsestock.domain.StockRepository
import com.rogerchang.twsestock.domain.model.DataError
import com.rogerchang.twsestock.domain.model.Stock
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeStockRepository : StockRepository {
    private val cache = MutableStateFlow<List<Stock>>(emptyList())

    /** 設成非 null 時，refresh 會失敗且不動快取。 */
    var error: DataError? = null

    var refreshCount = 0
        private set

    override fun observeStocks(): Flow<List<Stock>> = cache

    override suspend fun refresh(): DataError? {
        refreshCount++
        error?.let { return it }
        cache.value = PreviewStocks.all
        return null
    }

    /** 模擬已經有快取的冷啟動。 */
    fun seed(stocks: List<Stock> = PreviewStocks.all) {
        cache.value = stocks
    }
}
