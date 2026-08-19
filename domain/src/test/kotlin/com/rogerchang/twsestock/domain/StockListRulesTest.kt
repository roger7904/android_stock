package com.rogerchang.twsestock.domain

import com.google.common.truth.Truth.assertThat
import com.rogerchang.twsestock.domain.model.SortOption
import com.rogerchang.twsestock.domain.model.Stock
import org.junit.Test

class StockListRulesTest {
    private val listings = listOf(
        stock(code = "2330", name = "台積電"),
        stock(code = "00400A", name = "野村臺灣趨勢動能主動式"),
        stock(code = "2891C", name = "中信金乙特"),
        stock(code = "1101", name = "台泥"),
    )

    @Test
    fun `預設降序，代號以字串比較`() {
        val sorted = sortStocks(listings, SortOption.CODE_DESC).map(Stock::code)

        assertThat(sorted).containsExactly("2891C", "2330", "1101", "00400A").inOrder()
    }

    @Test
    fun `升序與交易所排列一致`() {
        val sorted = sortStocks(listings, SortOption.CODE_ASC).map(Stock::code)

        assertThat(sorted).containsExactly("00400A", "1101", "2330", "2891C").inOrder()
    }

    @Test
    fun `代號比對開頭，名稱比對包含`() {
        assertThat(filterStocks(listings, "2330").map(Stock::code)).containsExactly("2330")
        assertThat(filterStocks(listings, "台積").map(Stock::code)).containsExactly("2330")
        // 代號只比開頭，所以中間含 330 的不該命中。
        assertThat(filterStocks(listings, "330")).isEmpty()
    }

    @Test
    fun `搜尋忽略大小寫`() {
        assertThat(filterStocks(listings, "00400a").map(Stock::code)).containsExactly("00400A")
    }

    @Test
    fun `空白關鍵字回傳完整清單`() {
        assertThat(filterStocks(listings, "   ")).hasSize(listings.size)
    }
}
