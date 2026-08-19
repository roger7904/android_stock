package com.rogerchang.twsestock.ui.stocklist

import android.app.Application
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.google.common.truth.Truth.assertThat
import com.rogerchang.twsestock.domain.model.DataError
import com.rogerchang.twsestock.domain.model.ThemeMode
import com.rogerchang.twsestock.ui.theme.TwseStockTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * 畫面本身，用一個 state 物件驅動。
 *
 * 用 Robolectric 在 JVM 上跑而不是開模擬器：秒級回饋，也不需要額外的裝置。
 * 因為畫面是「狀態進、動作出」，這些測試驅動它的方式與 App 完全一樣。
 */
@RunWith(RobolectricTestRunner::class)
// 用一般的 Application：這個畫面不需要 DI，掛真的 Application 只會讓每個測試重複 startKoin。
@Config(application = Application::class, qualifiers = "w411dp-h891dp")
class StockListScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val actions = mutableListOf<StockListAction>()

    private fun show(state: StockListUiState) {
        composeRule.setContent {
            TwseStockTheme {
                StockListScreen(state = state, onAction = actions::add)
            }
        }
    }

    @Test
    fun `卡片顯示代號、名稱與格式化後的數字`() {
        show(StockListUiState(stocks = listOf(PreviewStocks.rising), isLoading = false))

        composeRule.onNodeWithText("2330").assertIsDisplayed()
        composeRule.onNodeWithText("台積電").assertIsDisplayed()
        composeRule.onNodeWithText("1175.00").assertIsDisplayed()
        composeRule.onNodeWithText("▲ +10.00").assertIsDisplayed()
    }

    @Test
    fun `當日無成交的卡片顯示 badge 且價格是破折號`() {
        show(StockListUiState(stocks = listOf(PreviewStocks.untraded), isLoading = false))

        composeRule.onNodeWithText("當日無成交").assertIsDisplayed()
        // 印 0.00 會讓人以為股價歸零。
        composeRule.onNodeWithText("0.00").assertDoesNotExist()
    }

    @Test
    fun `點卡片會送出選取事件`() {
        show(StockListUiState(stocks = listOf(PreviewStocks.rising), isLoading = false))

        composeRule.onNodeWithText("台積電").performClick()

        assertThat(actions).containsExactly(StockListAction.StockSelected("2330"))
    }

    @Test
    fun `選到個股時顯示三個估值數字`() {
        show(
            StockListUiState(
                stocks = listOf(PreviewStocks.rising),
                selectedStock = PreviewStocks.rising,
                isLoading = false,
            ),
        )

        composeRule.onNodeWithText("本益比").assertIsDisplayed()
        composeRule.onNodeWithText("27.76").assertIsDisplayed()
        composeRule.onNodeWithText("殖利率(%)").assertIsDisplayed()
        composeRule.onNodeWithText("股價淨值比").assertIsDisplayed()
    }

    @Test
    fun `交易所未發布本益比時附上說明`() {
        show(
            StockListUiState(
                stocks = listOf(PreviewStocks.withoutValuation),
                selectedStock = PreviewStocks.withoutValuation,
                isLoading = false,
            ),
        )

        composeRule.onNodeWithText("交易所未提供此標的之本益比資訊。").assertIsDisplayed()
    }

    @Test
    fun `沒有快取的錯誤畫面可以重試`() {
        show(StockListUiState(isLoading = false, error = DataError.NoNetwork))

        composeRule.onNodeWithText("目前沒有網路連線。").assertIsDisplayed()
        composeRule.onNodeWithText("重試").performClick()

        assertThat(actions).containsExactly(StockListAction.Retried)
    }

    @Test
    fun `搜尋不到時的文案與沒有資料不同`() {
        show(StockListUiState(isLoading = false, query = "zzz", isSearchVisible = true))

        composeRule.onNodeWithText("找不到符合「zzz」的股票").assertIsDisplayed()
    }

    @Test
    fun `右上排序圖示會開啟 bottom sheet`() {
        show(StockListUiState(stocks = listOf(PreviewStocks.rising), isLoading = false))

        composeRule.onNodeWithContentDescription("排序方式").performClick()

        assertThat(actions).containsExactly(StockListAction.SortSheetOpened)
    }

    @Test
    fun `主題選單列出三種模式`() {
        show(
            StockListUiState(
                stocks = listOf(PreviewStocks.rising),
                isLoading = false,
                isThemeMenuVisible = true,
            ),
        )

        composeRule.onNodeWithText("跟隨系統").assertIsDisplayed()
        composeRule.onNodeWithText("淺色").assertIsDisplayed()
        composeRule.onNodeWithText("深色").performClick()

        assertThat(actions).containsExactly(StockListAction.ThemeModeSelected(ThemeMode.DARK))
    }

    @Test
    fun `bottom sheet 標示目前選中的排序`() {
        show(
            StockListUiState(
                stocks = listOf(PreviewStocks.rising),
                isLoading = false,
                isSortSheetVisible = true,
            ),
        )

        composeRule.onNodeWithText("依股票代號降序").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("已選取").assertIsDisplayed()
    }
}
