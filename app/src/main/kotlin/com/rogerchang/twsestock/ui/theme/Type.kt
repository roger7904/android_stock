package com.rogerchang.twsestock.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

internal val TwseTypography = Typography()

/**
 * 給所有會被拿來互相比較的數字用。
 *
 * 等寬是重點：比例字體的 1 比 0 窄，一整排價格會隨著數值變動左右晃動。
 * 靠右對齊則是因為最重要的是最後一位。
 */
val NumericTextStyle = TextStyle(
    fontFamily = FontFamily.Monospace,
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    textAlign = TextAlign.End,
)

/** 卡片最下面那排成交資訊用的小一號版本。 */
val CompactNumericTextStyle = NumericTextStyle.copy(fontSize = 12.sp)
