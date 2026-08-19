package com.rogerchang.twsestock.ui.stocklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rogerchang.twsestock.R
import com.rogerchang.twsestock.domain.model.DataError

private const val SKELETON_COUNT = 6

@Composable
fun LoadingState(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(SKELETON_COUNT) { StockCardSkeleton() }
    }
}

/** 沒有快取而且刷新失敗，是唯一會佔滿整個畫面的錯誤。 */
@Composable
fun ErrorState(
    error: DataError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CenteredMessage(
        title = stringResource(R.string.error_title),
        body = error.describe(),
        modifier = modifier,
    ) {
        Button(onClick = onRetry) {
            Text(text = stringResource(R.string.error_retry))
        }
    }
}

/** 刷新成功但交易所沒發資料。 */
@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    CenteredMessage(
        title = stringResource(R.string.empty_title),
        body = stringResource(R.string.empty_body),
        modifier = modifier,
    )
}

/**
 * 搜尋不到。
 *
 * 文案刻意與 [EmptyState] 不同：「交易所沒發資料」與「你搜的東西不存在」
 * 要做的下一步不一樣，講錯會讓人跑去檢查網路，而其實只是打錯字。
 */
@Composable
fun NoResultsState(
    query: String,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    CenteredMessage(
        title = stringResource(R.string.no_results_title, query),
        body = stringResource(R.string.no_results_body),
        modifier = modifier,
    ) {
        TextButton(onClick = onClearSearch) {
            Text(text = stringResource(R.string.no_results_clear))
        }
    }
}

@Composable
private fun CenteredMessage(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
        if (action != null) {
            Column(modifier = Modifier.padding(top = 16.dp)) { action() }
        }
    }
}

/**
 * 一種失敗一句話。
 *
 * sealed 的 [DataError] 讓這個 when 具備窮盡性：新增一種錯誤時編譯器會要求補上文案，
 * 而不是讓它掉進「發生錯誤」了事。
 */
@Composable
fun DataError.describe(): String = when (this) {
    DataError.NoNetwork -> stringResource(R.string.error_no_network)
    DataError.Timeout -> stringResource(R.string.error_timeout)
    is DataError.Http -> stringResource(R.string.error_http, code)
    DataError.Malformed -> stringResource(R.string.error_malformed)
    DataError.Unknown -> stringResource(R.string.error_unknown)
}
