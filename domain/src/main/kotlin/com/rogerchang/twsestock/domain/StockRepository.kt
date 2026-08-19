package com.rogerchang.twsestock.domain

import com.rogerchang.twsestock.domain.model.DataError
import com.rogerchang.twsestock.domain.model.Stock
import kotlinx.coroutines.flow.Flow

/**
 * 介面宣告在 domain、實作放在 data，依賴方向因此是 data → domain。
 * 這是 domain 能維持純 JVM module 的前提。
 */
interface StockRepository {
    /** 快取內容，冷啟動時先發出空清單，之後每次寫入都會再發一次。 */
    fun observeStocks(): Flow<List<Stock>>

    /** 抓三支 API、合併、寫回快取。成功回傳 null，失敗回傳原因。 */
    suspend fun refresh(): DataError?
}
