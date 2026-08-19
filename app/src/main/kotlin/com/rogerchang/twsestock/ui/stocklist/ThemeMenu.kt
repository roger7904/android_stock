package com.rogerchang.twsestock.ui.stocklist

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.rogerchang.twsestock.R
import com.rogerchang.twsestock.domain.model.ThemeMode

/**
 * 淺色／深色／跟隨系統。
 *
 * 「跟隨系統」是預設，也是多數人該用的；另外兩個存在的理由是——
 * 想比較兩種配色的人沒辦法靠「跟隨系統」比較，而看這個 App 的人應該不必離開它才能看到深色。
 */
@Composable
fun ThemeMenu(
    selected: ThemeMode,
    isExpanded: Boolean,
    onAction: (StockListAction) -> Unit,
) {
    Box {
        IconButton(onClick = { onAction(StockListAction.ThemeMenuOpened) }) {
            Icon(
                painter = painterResource(R.drawable.ic_theme),
                contentDescription = stringResource(R.string.theme_open),
            )
        }
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { onAction(StockListAction.ThemeMenuDismissed) },
        ) {
            ThemeMode.entries.forEach { mode ->
                DropdownMenuItem(
                    text = { Text(text = stringResource(mode.labelRes())) },
                    onClick = { onAction(StockListAction.ThemeModeSelected(mode)) },
                    trailingIcon = {
                        if (mode == selected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = stringResource(R.string.sort_selected),
                                tint = MaterialTheme.colorScheme.primary,
                            )
                        }
                    },
                )
            }
        }
    }
}

private fun ThemeMode.labelRes(): Int = when (this) {
    ThemeMode.SYSTEM -> R.string.theme_system
    ThemeMode.LIGHT -> R.string.theme_light
    ThemeMode.DARK -> R.string.theme_dark
}
