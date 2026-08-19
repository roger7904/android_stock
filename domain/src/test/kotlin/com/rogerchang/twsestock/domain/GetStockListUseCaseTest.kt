package com.rogerchang.twsestock.domain

import com.google.common.truth.Truth.assertThat
import com.rogerchang.twsestock.domain.model.DataError
import com.rogerchang.twsestock.domain.model.SortOption
import com.rogerchang.twsestock.domain.model.Stock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetStockListUseCaseTest {
    private val stocks = MutableStateFlow(
        listOf(
            stock(code = "1101", name = "台泥"),
            stock(code = "2330", name = "台積電"),
        ),
    )
    private val repository = object : StockRepository {
        var refreshCount = 0
            private set

        override fun observeStocks(): Flow<List<Stock>> = stocks

        override suspend fun refresh(): DataError? {
            refreshCount++
            return null
        }
    }
    private val getStockList = GetStockListUseCase(repository, Dispatchers.Unconfined)

    @Test
    fun `同時套用排序與關鍵字`() = runTest {
        val result = getStockList(
            sort = MutableStateFlow(SortOption.CODE_ASC),
            query = MutableStateFlow("台"),
        ).first()

        assertThat(result.map(Stock::code)).containsExactly("1101", "2330").inOrder()
    }

    @Test
    fun `切換排序不會重新請求 API`() = runTest {
        val sort = MutableStateFlow(SortOption.CODE_DESC)
        val flow = getStockList(sort, MutableStateFlow(""))

        assertThat(flow.first().map(Stock::code)).containsExactly("2330", "1101").inOrder()
        sort.value = SortOption.CODE_ASC
        assertThat(flow.first().map(Stock::code)).containsExactly("1101", "2330").inOrder()

        assertThat(repository.refreshCount).isEqualTo(0)
    }
}
