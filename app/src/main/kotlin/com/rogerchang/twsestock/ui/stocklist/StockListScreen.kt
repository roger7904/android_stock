package com.rogerchang.twsestock.ui.stocklist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rogerchang.twsestock.R
import com.rogerchang.twsestock.ui.theme.TwseStockTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/** 一張卡片至少要這麼寬才不會被擠壞；螢幕夠寬就自動多排一欄。 */
private val MIN_CARD_WIDTH = 320.dp

private val DateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd")

/**
 * 清單畫面本身沒有任何依賴：狀態從 [state] 進來、事件從 [onAction] 出去，
 * 所以 Preview、測試與正式 App 驅動它的方式完全一樣。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockListScreen(
    state: StockListUiState,
    onAction: (StockListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    CacheNoticeEffect(state, snackbarHostState, onAction)

    Scaffold(
        modifier = modifier,
        topBar = { StockListTopBar(state, onAction) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { onAction(StockListAction.Refreshed) },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            StockListContent(state, onAction)
        }
    }

    if (state.isSortSheetVisible) {
        SortBottomSheet(
            selected = state.sort,
            onSelect = { onAction(StockListAction.SortSelected(it)) },
            onDismiss = { onAction(StockListAction.SortSheetDismissed) },
        )
    }

    state.selectedStock?.let { stock ->
        StockDetailDialog(stock = stock, onDismiss = { onAction(StockListAction.DetailDismissed) })
    }
}

/**
 * 清單此刻是哪一種樣子。
 *
 * 順序有意義：有資料就先畫資料，錯誤只在「連快取都沒有」時才佔滿整個畫面——
 * 有東西可看的時候，把它換成一頁錯誤訊息並不划算。
 */
@Composable
private fun StockListContent(
    state: StockListUiState,
    onAction: (StockListAction) -> Unit,
) {
    when {
        state.stocks.isNotEmpty() -> StockGrid(state, onAction)
        state.isLoading -> LoadingState()
        state.error != null -> ErrorState(
            error = state.error,
            onRetry = { onAction(StockListAction.Retried) },
        )

        state.query.isNotBlank() -> NoResultsState(
            query = state.query,
            onClearSearch = { onAction(StockListAction.QueryChanged("")) },
        )

        else -> EmptyState()
    }
}

@Composable
private fun StockGrid(
    state: StockListUiState,
    onAction: (StockListAction) -> Unit,
) {
    LazyVerticalGrid(
        // 橫向或平板上自動變成兩、三欄。一張卡片橫跨整個橫向畫面，
        // 只會變成中間一大片空白、價格被丟到兩端。
        columns = GridCells.Adaptive(MIN_CARD_WIDTH),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = state.stocks, key = { it.code }) { stock ->
            StockCard(
                stock = stock,
                onClick = { onAction(StockListAction.StockSelected(stock.code)) },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StockListTopBar(
    state: StockListUiState,
    onAction: (StockListAction) -> Unit,
) {
    // 用 Column 包起來，不是並排兩個。Scaffold 的 topBar slot 會把子項從同一個原點排版、
    // 只把最高的那個當成 bar 的高度，並排會讓兩者疊在一起。
    Column {
        TopAppBar(
            title = {
                Column {
                    Text(
                        text = stringResource(R.string.app_name),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    state.dataDate?.let { date ->
                        Text(
                            text = stringResource(R.string.data_date, date.format(DateFormatter)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            },
            actions = {
                IconButton(
                    onClick = {
                        onAction(
                            if (state.isSearchVisible) StockListAction.SearchClosed else StockListAction.SearchOpened,
                        )
                    },
                ) {
                    Icon(
                        imageVector = if (state.isSearchVisible) Icons.Default.Clear else Icons.Default.Search,
                        contentDescription = stringResource(
                            if (state.isSearchVisible) R.string.search_close else R.string.search_open,
                        ),
                    )
                }
                IconButton(onClick = { onAction(StockListAction.SortSheetOpened) }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_sort),
                        contentDescription = stringResource(R.string.sort_open),
                    )
                }
            },
        )

        // 平常收起來：清單有一千多筆，多一排卡片比一個沒人在用的搜尋框值錢。
        AnimatedVisibility(visible = state.isSearchVisible) {
            SearchField(state.query, onAction)
        }
    }
}

@Composable
private fun SearchField(
    query: String,
    onAction: (StockListAction) -> Unit,
) {
    OutlinedTextField(
        value = query,
        onValueChange = { onAction(StockListAction.QueryChanged(it)) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        singleLine = true,
        placeholder = { Text(text = stringResource(R.string.search_hint)) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onAction(StockListAction.QueryChanged("")) }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = stringResource(R.string.search_clear),
                    )
                }
            }
        },
    )
}

/**
 * 刷新失敗但手上還有快取時的提示。
 *
 * 有東西可看就不該把畫面換掉，但也不能讓人以為看到的是最新的。
 * snackbar 附重試，按下去真的會重試——一個只會關掉自己的「重試」比沒有更糟。
 */
@Composable
private fun CacheNoticeEffect(
    state: StockListUiState,
    snackbarHostState: SnackbarHostState,
    onAction: (StockListAction) -> Unit,
) {
    val message = stringResource(R.string.error_showing_cache)
    val retryLabel = stringResource(R.string.error_retry)
    val hasCachedError = state.error != null && state.stocks.isNotEmpty()

    LaunchedEffect(hasCachedError) {
        if (!hasCachedError) return@LaunchedEffect

        val result = snackbarHostState.showSnackbar(message = message, actionLabel = retryLabel)
        if (result == SnackbarResult.ActionPerformed) {
            onAction(StockListAction.Retried)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StockListScreenPreview() {
    TwseStockTheme {
        StockListScreen(
            state = StockListUiState(
                stocks = PreviewStocks.all,
                dataDate = LocalDate.of(2026, 8, 14),
                isLoading = false,
            ),
            onAction = {},
        )
    }
}
