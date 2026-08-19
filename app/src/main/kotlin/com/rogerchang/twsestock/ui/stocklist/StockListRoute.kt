package com.rogerchang.twsestock.ui.stocklist

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

/** 把 ViewModel 綁到無狀態的畫面上。分開是為了讓畫面本身能被 Preview 與測試直接驅動。 */
@Composable
fun StockListRoute(viewModel: StockListViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    StockListScreen(state = state, onAction = viewModel::onAction)
}
