package com.rogerchang.twsestock.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
internal interface StockDao {
    /** 冷啟動時立刻發出空清單，之後每次寫入都會再推一次，畫面因此可以先畫快取。 */
    @Query("SELECT * FROM stocks")
    fun observeStocks(): Flow<List<StockEntity>>

    /**
     * 整批換掉。
     *
     * 每次刷新都是抓全部，沒有增量可做；整批換掉也順便清掉已下市的標的，
     * 單純 upsert 會讓它們永遠留在清單裡。兩個語句在同一個 transaction 內，
     * 讀取端不會看到「刪完還沒寫入」的空窗。
     */
    @Transaction
    suspend fun replaceAll(stocks: List<StockEntity>) {
        deleteAll()
        insertAll(stocks)
    }

    @Query("DELETE FROM stocks")
    suspend fun deleteAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(stocks: List<StockEntity>)
}
