package com.rogerchang.twsestock.ui.stocklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rogerchang.twsestock.domain.GetStockListUseCase
import com.rogerchang.twsestock.domain.StockRepository
import com.rogerchang.twsestock.domain.ThemePreferences
import com.rogerchang.twsestock.domain.model.DataError
import com.rogerchang.twsestock.domain.model.SortOption
import com.rogerchang.twsestock.domain.model.Stock
import com.rogerchang.twsestock.domain.model.ThemeMode
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SEARCH_DEBOUNCE_MILLIS = 300L
private const val SUBSCRIPTION_TIMEOUT_MILLIS = 5_000L

@OptIn(FlowPreview::class)
class StockListViewModel(
    private val repository: StockRepository,
    private val themePreferences: ThemePreferences,
    getStockList: GetStockListUseCase,
) : ViewModel() {

    private val query = MutableStateFlow("")
    private val sort = MutableStateFlow(SortOption.Default)
    private val screen = MutableStateFlow(ScreenState())

    /**
     * 空字串不等待，其餘等 300 毫秒。
     *
     * 一視同仁地 debounce 會連第一次發射都延後，冷啟動時會多盯著骨架三分之一秒；
     * 清除搜尋也不該讓人等才看到完整清單。
     */
    private val debouncedQuery = query
        .debounce { keyword -> if (keyword.isBlank()) 0L else SEARCH_DEBOUNCE_MILLIS }
        .distinctUntilChanged()

    val uiState: StateFlow<StockListUiState> =
        combine(
            getStockList(sort, debouncedQuery),
            query,
            sort,
            screen,
            themePreferences.observeThemeMode(),
        ) { stocks, keyword, sortOption, screenState, themeMode ->
            toUiState(stocks, keyword, sortOption, screenState, themeMode)
        }.stateIn(
            scope = viewModelScope,
            // 轉向時不重跑，畫面真的離開了才收掉。
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MILLIS),
            initialValue = StockListUiState(),
        )

    init {
        refresh()
    }

    fun onAction(action: StockListAction) {
        when (action) {
            is StockListAction.QueryChanged -> query.value = action.query

            StockListAction.SearchOpened -> screen.update { it.copy(isSearchVisible = true) }

            StockListAction.SearchClosed -> {
                query.value = ""
                screen.update { it.copy(isSearchVisible = false) }
            }

            StockListAction.SortSheetOpened -> screen.update { it.copy(isSortSheetVisible = true) }

            StockListAction.SortSheetDismissed -> screen.update { it.copy(isSortSheetVisible = false) }

            is StockListAction.SortSelected -> {
                sort.value = action.option
                screen.update { it.copy(isSortSheetVisible = false) }
            }

            StockListAction.ThemeMenuOpened -> screen.update { it.copy(isThemeMenuVisible = true) }

            StockListAction.ThemeMenuDismissed -> screen.update { it.copy(isThemeMenuVisible = false) }

            is StockListAction.ThemeModeSelected -> {
                screen.update { it.copy(isThemeMenuVisible = false) }
                viewModelScope.launch { themePreferences.setThemeMode(action.mode) }
            }

            is StockListAction.StockSelected -> screen.update { it.copy(selectedCode = action.code) }

            StockListAction.DetailDismissed -> screen.update { it.copy(selectedCode = null) }

            StockListAction.Refreshed, StockListAction.Retried -> refresh()
        }
    }

    private fun refresh() {
        // 下拉刷新很容易連按兩次，第二趟只會跟第一趟搶著寫同一批資料。
        if (screen.value.isRefreshing) return

        viewModelScope.launch {
            screen.update { it.copy(isRefreshing = true) }
            val error = repository.refresh()
            screen.update { it.copy(isRefreshing = false, hasRefreshed = true, error = error) }
        }
    }

    private fun toUiState(
        stocks: List<Stock>,
        keyword: String,
        sortOption: SortOption,
        screenState: ScreenState,
        themeMode: ThemeMode,
    ): StockListUiState {
        val isLoading = !screenState.hasRefreshed && stocks.isEmpty()

        return StockListUiState(
            stocks = stocks,
            dataDate = stocks.firstOrNull()?.date,
            query = keyword,
            sort = sortOption,
            isLoading = isLoading,
            // 第一次載入畫的是骨架，這時再轉一個刷新圈圈是同一件事等兩次。
            isRefreshing = screenState.isRefreshing && !isLoading,
            error = screenState.error,
            isSearchVisible = screenState.isSearchVisible,
            isSortSheetVisible = screenState.isSortSheetVisible,
            themeMode = themeMode,
            isThemeMenuVisible = screenState.isThemeMenuVisible,
            // 存代號而不是整個 Stock：刷新後才不會停在一份已經過期的副本上。
            selectedStock = screenState.selectedCode?.let { code -> stocks.firstOrNull { it.code == code } },
        )
    }

    private data class ScreenState(
        val isRefreshing: Boolean = false,
        /** 第一次刷新是否已經結束，用來分辨「還在載入」與「真的沒有資料」。 */
        val hasRefreshed: Boolean = false,
        val error: DataError? = null,
        val isSearchVisible: Boolean = false,
        val isSortSheetVisible: Boolean = false,
        val isThemeMenuVisible: Boolean = false,
        val selectedCode: String? = null,
    )
}
