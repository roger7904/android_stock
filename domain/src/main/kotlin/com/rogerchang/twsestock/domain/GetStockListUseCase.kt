package com.rogerchang.twsestock.domain

import com.rogerchang.twsestock.domain.model.SortOption
import com.rogerchang.twsestock.domain.model.Stock
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn

/**
 * 畫面要的清單：快取內容 + 當前關鍵字 + 當前排序。
 *
 * 把三者組合在這裡而不是 ViewModel，是為了讓排序與搜尋不會碰到網路——
 * 兩者都從既有快取重新推導，切換時不會發出任何請求。
 */
class GetStockListUseCase(
    private val repository: StockRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {
    operator fun invoke(sort: Flow<SortOption>, query: Flow<String>): Flow<List<Stock>> =
        combine(repository.observeStocks(), sort, query) { stocks, option, keyword ->
            // 先篩再排，排一個變短的清單一定比較省。
            sortStocks(filterStocks(stocks, keyword), option)
        }.flowOn(dispatcher)
}
