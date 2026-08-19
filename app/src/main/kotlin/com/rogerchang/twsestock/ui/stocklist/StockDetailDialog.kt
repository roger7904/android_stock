package com.rogerchang.twsestock.ui.stocklist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.rogerchang.twsestock.R
import com.rogerchang.twsestock.domain.formatRatio
import com.rogerchang.twsestock.domain.model.Stock
import com.rogerchang.twsestock.ui.theme.NumericTextStyle

/**
 * 點卡片後跳出的本益比資訊。
 *
 * 用 AlertDialog 是照題目的字面（「跳 alert」）。裡面只放題目要的三個數字：
 * 其他欄位卡片上都有了，重複一次只會把這三個數字埋掉。
 */
@Composable
fun StockDetailDialog(
    stock: Stock,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
        title = { Text(text = stringResource(R.string.detail_title, stock.code, stock.name)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DetailRow(stringResource(R.string.detail_pe_ratio), formatRatio(stock.valuation?.peRatio))
                DetailRow(stringResource(R.string.detail_dividend_yield), formatRatio(stock.valuation?.dividendYield))
                DetailRow(stringResource(R.string.detail_pb_ratio), formatRatio(stock.valuation?.pbRatio))

                if (stock.valuation == null) {
                    // 約 295 檔交易所根本不發布。三個沒有說明的破折號看起來像 App 壞了。
                    Text(
                        text = stringResource(R.string.detail_unpublished),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.detail_close))
            }
        },
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    // 合併成一個節點：否則讀螢幕會把「本益比」與「27.76」念成兩件不相干的事。
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Text(text = value, style = NumericTextStyle)
    }
}
