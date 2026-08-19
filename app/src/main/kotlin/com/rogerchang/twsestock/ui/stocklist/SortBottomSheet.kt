package com.rogerchang.twsestock.ui.stocklist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.rogerchang.twsestock.R
import com.rogerchang.twsestock.domain.model.SortOption

/**
 * 題目指定的兩種排序，就只有這兩種。
 *
 * mockup 上沒有選取狀態，這裡補了一個：一個不肯說出目前用哪種排序的選單，
 * 會讓人打開來看還是得用猜的。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SortBottomSheet(
    selected: SortOption,
    onSelect: (SortOption) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        modifier = modifier,
    ) {
        Column(modifier = Modifier.navigationBarsPadding()) {
            Text(
                text = stringResource(R.string.sort_title),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            )
            SortOption.entries.forEach { option ->
                SortOptionRow(
                    option = option,
                    isSelected = option == selected,
                    onSelect = { onSelect(option) },
                )
            }
        }
    }
}

@Composable
private fun SortOptionRow(
    option: SortOption,
    isSelected: Boolean,
    onSelect: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // 用 Role.RadioButton 而不是單純的 clickable：這樣讀螢幕才知道這幾項是互斥的、
            // 以及現在選的是哪一個，勾勾只有眼睛看得到。
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onSelect)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(option.labelRes()),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
        )
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = stringResource(R.string.sort_selected),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun SortOption.labelRes(): Int = when (this) {
    SortOption.CODE_DESC -> R.string.sort_code_desc
    SortOption.CODE_ASC -> R.string.sort_code_asc
}
