package com.rogerchang.twsestock.data

import com.rogerchang.twsestock.data.local.StockDao
import com.rogerchang.twsestock.data.local.StockEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/** replaceAll 是 DAO 的預設實作，這裡只補上它呼叫的三個方法。 */
internal class FakeStockDao : StockDao {
    private val rows = MutableStateFlow<List<StockEntity>>(emptyList())

    override fun observeStocks(): Flow<List<StockEntity>> = rows

    override suspend fun deleteAll() {
        rows.value = emptyList()
    }

    override suspend fun insertAll(stocks: List<StockEntity>) {
        rows.value = rows.value + stocks
    }
}
