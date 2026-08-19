package com.rogerchang.twsestock.domain

import com.rogerchang.twsestock.domain.model.SortOption
import com.rogerchang.twsestock.domain.model.Stock
import java.util.Locale

/**
 * 依代號排序。
 *
 * 代號一律當字串比，不要 toInt()：1,095 檔是四位數字，但 `2891C`、`00400A` 也是合法代號，
 * 兩者都會讓 toInt() 直接拋例外。字串序剛好就是交易所的排列順序
 * （字元集只有 0-9A-Z，所以 00400A < 1101 < 2891C），也就不需要 Collator。
 *
 * 降序用 compareByDescending 而不是把升序 reversed()，reversed() 不是穩定排序。
 */
fun sortStocks(stocks: List<Stock>, option: SortOption): List<Stock> = when (option) {
    SortOption.CODE_ASC -> stocks.sortedWith(compareBy(Stock::code))
    SortOption.CODE_DESC -> stocks.sortedWith(compareByDescending(Stock::code))
}

/**
 * 依關鍵字篩選：代號比對開頭、名稱比對包含。
 *
 * 兩邊規則不同是刻意的——沒有人會想找「代號中間含 330」的股票，
 * 但很多人只記得公司名稱中間那兩個字。
 */
fun filterStocks(stocks: List<Stock>, query: String): List<Stock> {
    val keyword = query.trim()
    if (keyword.isEmpty()) return stocks

    // 用 Locale.ROOT 而不是裝置語系：土耳其語會把 'I' 轉成無點的 'ı'，
    // 在土耳其語裝置上就再也搜不到 TI 開頭的代號。
    val normalized = keyword.lowercase(Locale.ROOT)
    return stocks.filter { stock ->
        stock.code.lowercase(Locale.ROOT).startsWith(normalized) ||
            stock.name.lowercase(Locale.ROOT).contains(normalized)
    }
}
