package com.rogerchang.twsestock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.rogerchang.twsestock.ui.stocklist.StockListRoute
import com.rogerchang.twsestock.ui.theme.TwseStockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TwseStockTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StockListRoute()
                }
            }
        }
    }
}
