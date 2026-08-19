package com.rogerchang.twsestock.ui.stocklist

import com.google.common.truth.Truth.assertThat
import com.rogerchang.twsestock.MainDispatcherRule
import com.rogerchang.twsestock.domain.GetStockListUseCase
import com.rogerchang.twsestock.domain.model.DataError
import com.rogerchang.twsestock.domain.model.SortOption
import com.rogerchang.twsestock.domain.model.Stock
import com.rogerchang.twsestock.domain.model.ThemeMode
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockListViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository = FakeStockRepository()
    private val themePreferences = FakeThemePreferences()

    private fun viewModel() = StockListViewModel(
        repository = repository,
        themePreferences = themePreferences,
        getStockList = GetStockListUseCase(repository, mainDispatcherRule.dispatcher),
    )

    /** uiState 是 WhileSubscribed，沒有訂閱者就不會產生新值。 */
    private fun TestScope.subscribe(viewModel: StockListViewModel) {
        backgroundScope.launch { viewModel.uiState.collect() }
        advanceUntilIdle()
    }

    @Test
    fun `冷啟動自動刷新並顯示清單`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        subscribe(viewModel)

        val state = viewModel.uiState.value
        assertThat(repository.refreshCount).isEqualTo(1)
        assertThat(state.isLoading).isFalse()
        assertThat(state.stocks).isNotEmpty()
        assertThat(state.dataDate).isNotNull()
    }

    @Test
    fun `沒有快取又刷新失敗就是整頁錯誤`() = runTest(mainDispatcherRule.dispatcher) {
        repository.error = DataError.NoNetwork
        val viewModel = viewModel()
        subscribe(viewModel)

        val state = viewModel.uiState.value
        assertThat(state.stocks).isEmpty()
        assertThat(state.error).isEqualTo(DataError.NoNetwork)
        assertThat(state.isLoading).isFalse()
    }

    @Test
    fun `有快取時刷新失敗仍然保留清單`() = runTest(mainDispatcherRule.dispatcher) {
        // 有東西可看就不該把畫面換成一頁錯誤訊息，畫面會改用 snackbar 提示。
        repository.seed()
        repository.error = DataError.Timeout
        val viewModel = viewModel()
        subscribe(viewModel)

        val state = viewModel.uiState.value
        assertThat(state.stocks).isNotEmpty()
        assertThat(state.error).isEqualTo(DataError.Timeout)
    }

    @Test
    fun `重試會再打一次 API`() = runTest(mainDispatcherRule.dispatcher) {
        repository.error = DataError.NoNetwork
        val viewModel = viewModel()
        subscribe(viewModel)

        repository.error = null
        viewModel.onAction(StockListAction.Retried)
        advanceUntilIdle()

        assertThat(repository.refreshCount).isEqualTo(2)
        assertThat(viewModel.uiState.value.error).isNull()
        assertThat(viewModel.uiState.value.stocks).isNotEmpty()
    }

    @Test
    fun `預設降序，切換排序不會重打 API`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        subscribe(viewModel)

        assertThat(viewModel.uiState.value.sort).isEqualTo(SortOption.CODE_DESC)
        val descending = viewModel.uiState.value.stocks.map(Stock::code)

        viewModel.onAction(StockListAction.SortSelected(SortOption.CODE_ASC))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.stocks.map(Stock::code)).isEqualTo(descending.reversed())
        assertThat(viewModel.uiState.value.isSortSheetVisible).isFalse()
        assertThat(repository.refreshCount).isEqualTo(1)
    }

    @Test
    fun `搜尋只保留符合的股票`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        subscribe(viewModel)

        viewModel.onAction(StockListAction.QueryChanged("台積"))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.stocks.map(Stock::code)).containsExactly("2330")
    }

    @Test
    fun `連續輸入時只有最後一次生效`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        subscribe(viewModel)

        viewModel.onAction(StockListAction.QueryChanged("台"))
        advanceTimeBy(100)
        viewModel.onAction(StockListAction.QueryChanged("台積"))
        advanceUntilIdle()

        // 中間那次「台」如果生效過，清單會先閃一次兩筆結果。
        assertThat(viewModel.uiState.value.stocks.map(Stock::code)).containsExactly("2330")
    }

    @Test
    fun `關閉搜尋會一併清掉關鍵字`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        subscribe(viewModel)
        viewModel.onAction(StockListAction.SearchOpened)
        viewModel.onAction(StockListAction.QueryChanged("台積"))
        advanceUntilIdle()

        viewModel.onAction(StockListAction.SearchClosed)
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.query).isEmpty()
        assertThat(viewModel.uiState.value.stocks).hasSize(PreviewStocks.all.size)
    }

    @Test
    fun `切換主題會寫進偏好並反映在狀態上`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        subscribe(viewModel)
        assertThat(viewModel.uiState.value.themeMode).isEqualTo(ThemeMode.SYSTEM)

        viewModel.onAction(StockListAction.ThemeModeSelected(ThemeMode.DARK))
        advanceUntilIdle()

        assertThat(viewModel.uiState.value.themeMode).isEqualTo(ThemeMode.DARK)
        assertThat(viewModel.uiState.value.isThemeMenuVisible).isFalse()
    }

    @Test
    fun `點選卡片後帶出對應的個股`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = viewModel()
        subscribe(viewModel)

        viewModel.onAction(StockListAction.StockSelected("2330"))
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.selectedStock?.code).isEqualTo("2330")

        viewModel.onAction(StockListAction.DetailDismissed)
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.selectedStock).isNull()
    }
}
