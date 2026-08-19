package com.rogerchang.twsestock.ui.stocklist

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rogerchang.twsestock.R
import com.rogerchang.twsestock.domain.formatChange
import com.rogerchang.twsestock.domain.formatCompactCount
import com.rogerchang.twsestock.domain.formatPrice
import com.rogerchang.twsestock.domain.model.PriceTrend
import com.rogerchang.twsestock.domain.model.Stock
import com.rogerchang.twsestock.domain.model.changeTrend
import com.rogerchang.twsestock.domain.model.closingPriceTrend
import com.rogerchang.twsestock.ui.theme.CompactNumericTextStyle
import com.rogerchang.twsestock.ui.theme.LocalStockColors
import com.rogerchang.twsestock.ui.theme.NumericTextStyle
import com.rogerchang.twsestock.ui.theme.TwseStockTheme
import com.rogerchang.twsestock.ui.theme.colorFor

/**
 * 一檔個股，版型照題目的 mockup：代號名稱在上，六個欄位分兩欄，成交資訊橫排在底下。
 *
 * 只有兩個數字上色，各自對照不同的基準（收盤價比月平均價、漲跌價差比 0）。
 * 其餘一律維持一般文字色——全部都上色就等於沒有重點。
 */
@Composable
fun StockCard(
    stock: Stock,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val closingColor = trendColor(stock.closingPriceTrend(), stock.hasNoTrades)
    val changeColor = trendColor(stock.changeTrend(), stock.hasNoTrades)

    // 這麼大一張卡片，按下去只有 ripple 其實不太感覺得到。
    // 稍微縮一點點，整張卡才像個按鈕。
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.98f else 1f, label = "pressScale")

    Card(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            CardHeader(stock)

            FigureRow(
                leadingLabel = stringResource(R.string.card_opening_price),
                leadingValue = formatPrice(stock.openingPrice),
                trailingLabel = stringResource(R.string.card_closing_price),
                trailingValue = formatPrice(stock.closingPrice),
                trailingColor = closingColor,
            )
            FigureRow(
                leadingLabel = stringResource(R.string.card_highest_price),
                leadingValue = formatPrice(stock.highestPrice),
                trailingLabel = stringResource(R.string.card_lowest_price),
                trailingValue = formatPrice(stock.lowestPrice),
            )
            FigureRow(
                leadingLabel = stringResource(R.string.card_change),
                leadingValue = formatChange(stock.change),
                leadingColor = changeColor,
                trailingLabel = stringResource(R.string.card_monthly_average),
                trailingValue = formatPrice(stock.monthlyAveragePrice),
            )

            TradeTotals(stock)
        }
    }
}

/**
 * 把漲跌轉成顏色，並讓顏色的變化補間。
 *
 * 沒有成交就沒有漲跌可言，一整張破折號的卡片不該染上紅綠；這條規則寫在這裡而不是交給呼叫端，
 * 才不會在某個組裝狀態的地方被漏掉。
 *
 * 會補間是因為刷新是在使用者正在讀的時候落地的。顏色瞬間從綠跳到紅很容易被忽略，
 * 更糟的是會被誤讀成「它本來就是紅的」。
 */
@Composable
private fun trendColor(trend: PriceTrend, hasNoTrades: Boolean): Color {
    val stockColors = LocalStockColors.current
    val target = if (hasNoTrades) stockColors.flat else stockColors.colorFor(trend)
    val animated by animateColorAsState(targetValue = target, label = "trendColor")
    return animated
}

@Composable
private fun CardHeader(stock: Stock) {
    Text(text = stock.code, style = MaterialTheme.typography.labelMedium)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stock.name,
            style = MaterialTheme.typography.headlineSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false),
        )
        if (stock.hasNoTrades) {
            NoTradesBadge(modifier = Modifier.padding(start = 8.dp))
        }
    }
}

/** 兩組「標籤 + 數字」共用一列，各佔一半，整張卡的數字才會對齊成兩欄。 */
@Composable
private fun FigureRow(
    leadingLabel: String,
    leadingValue: String,
    trailingLabel: String,
    trailingValue: String,
    modifier: Modifier = Modifier,
    leadingColor: Color = Color.Unspecified,
    trailingColor: Color = Color.Unspecified,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Figure(leadingLabel, leadingValue, leadingColor, Modifier.weight(1f))
        Figure(trailingLabel, trailingValue, trailingColor, Modifier.weight(1f))
    }
}

@Composable
private fun Figure(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier,
    valueStyle: TextStyle = NumericTextStyle,
) {
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        // 標籤保持它需要的寬度，數字拿走剩下的並靠右——這樣才會對齊成一欄，
        // 也不會因為標籤變長就把價格擠到換行。
        Text(
            text = value,
            style = valueStyle,
            color = valueColor,
            maxLines = 1,
            // 斷成兩行的價格就不是價格了。
            softWrap = false,
            modifier = Modifier
                .weight(1f)
                .padding(start = 6.dp),
        )
    }
}

/** 當日的成交總量。一律不上色，成交量沒有方向可言。 */
@Composable
private fun TradeTotals(stock: Stock) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Figure(
            label = stringResource(R.string.card_transaction),
            value = formatCompactCount(stock.transaction),
            valueColor = Color.Unspecified,
            valueStyle = CompactNumericTextStyle,
            modifier = Modifier.weight(1f),
        )
        Figure(
            label = stringResource(R.string.card_trade_volume),
            value = formatCompactCount(stock.tradeVolume),
            valueColor = Color.Unspecified,
            valueStyle = CompactNumericTextStyle,
            modifier = Modifier.weight(1f),
        )
        Figure(
            label = stringResource(R.string.card_trade_value),
            value = formatCompactCount(stock.tradeValue),
            valueColor = Color.Unspecified,
            valueStyle = CompactNumericTextStyle,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun NoTradesBadge(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.small,
    ) {
        Text(
            text = stringResource(R.string.card_no_trades),
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StockCardPreview() {
    TwseStockTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StockCard(stock = PreviewStocks.rising, onClick = {})
            StockCard(stock = PreviewStocks.untraded, onClick = {})
        }
    }
}
