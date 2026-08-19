package com.rogerchang.twsestock.ui.stocklist

import androidx.compose.runtime.Immutable
import com.rogerchang.twsestock.domain.model.DataError
import com.rogerchang.twsestock.domain.model.SortOption
import com.rogerchang.twsestock.domain.model.Stock
import java.time.LocalDate

/**
 * 畫面上的一切。
 *
 * 排序與關鍵字放在最外層而不是塞進某個「載入中」的狀態裡，
 * 這樣下拉刷新時使用者的搜尋字與排序不會在眼前被清掉。
 */
@Immutable
data class StockListUiState(
    val stocks: List<Stock> = emptyList(),
    /** 資料日期，取自清單第一筆。 */
    val dataDate: LocalDate? = null,
    val query: String = "",
    val sort: SortOption = SortOption.Default,
    /** 還沒有任何資料可畫，顯示骨架卡片。 */
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    /** 最近一次刷新的失敗原因。有快取時只跳 snackbar，沒快取才佔滿整個畫面。 */
    val error: DataError? = null,
    val isSearchVisible: Boolean = false,
    val isSortSheetVisible: Boolean = false,
    /** 被點開的個股，非 null 時顯示本益比 dialog。 */
    val selectedStock: Stock? = null,
)

/**
 * 畫面能做的所有事，寫成資料。
 *
 * 單一入口而不是十幾個 lambda：Preview 與測試只要給一個函式，
 * 新增互動時也不可能忘記把它串進去。
 */
sealed interface StockListAction {
    data class QueryChanged(val query: String) : StockListAction

    data object SearchOpened : StockListAction

    /** 一併清空關鍵字：留著一個看不見的篩選條件只會讓人一頭霧水。 */
    data object SearchClosed : StockListAction

    data object SortSheetOpened : StockListAction

    data object SortSheetDismissed : StockListAction

    data class SortSelected(val option: SortOption) : StockListAction

    data class StockSelected(val code: String) : StockListAction

    data object DetailDismissed : StockListAction

    /** 下拉刷新。 */
    data object Refreshed : StockListAction

    /** 錯誤畫面上的重試。 */
    data object Retried : StockListAction
}
