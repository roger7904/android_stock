package com.rogerchang.twsestock.ui.stocklist

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rogerchang.twsestock.R

private const val PULSE_MIN_ALPHA = 0.30f
private const val PULSE_MAX_ALPHA = 0.85f
private const val PULSE_DURATION_MILLIS = 900

/**
 * 第一次載入時的佔位卡片。
 *
 * 比例與真的卡片一致，資料進來時清單不會跳動。轉圈圈只說得出「請等」，
 * 骨架還說得出「等的是什麼、大概有多少」。
 *
 * 呼吸是單純的 alpha 動畫而不是掃光漸層——掃光要自訂 brush 與 draw modifier，
 * 而「正在做事」這件事，呼吸已經表達得夠清楚。
 */
@Composable
fun StockCardSkeleton(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = PULSE_MIN_ALPHA,
        targetValue = PULSE_MAX_ALPHA,
        animationSpec = infiniteRepeatable(
            animation = tween(PULSE_DURATION_MILLIS, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "alpha",
    )
    val loadingLabel = stringResource(R.string.card_loading)

    Card(
        modifier = modifier
            .fillMaxWidth()
            // 對讀螢幕來說是一個「載入中」節點，不是十幾個沒有名字的方塊。
            .clearAndSetSemantics { contentDescription = loadingLabel },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Block(alpha = alpha, height = 12.dp, width = 48.dp)
            Block(alpha = alpha, height = 24.dp, width = 140.dp)

            repeat(3) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Block(alpha = alpha, height = 16.dp, modifier = Modifier.weight(1f))
                    Block(alpha = alpha, height = 16.dp, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun Block(
    alpha: Float,
    height: Dp,
    modifier: Modifier = Modifier,
    width: Dp? = null,
) {
    // 先混色成不透明再畫，而不是疊一層半透明——一張畫面上有十幾個方塊，
    // 這樣可以少一層 overdraw。
    val color = lerp(
        start = MaterialTheme.colorScheme.surfaceContainer,
        stop = MaterialTheme.colorScheme.onSurfaceVariant,
        fraction = alpha * 0.35f,
    )

    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier)
            .height(height)
            .background(color = color, shape = MaterialTheme.shapes.extraSmall),
    )
}
