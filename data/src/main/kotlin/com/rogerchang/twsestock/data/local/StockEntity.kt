package com.rogerchang.twsestock.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 已經合併好的一檔個股。
 *
 * 刻意反正規化成單表：合併規則是商業規則，寫成純函式才能用不碰資料庫的測試覆蓋，
 * 搬進 SQL join 只會讓驗證成本變成一個儀器測試，而且得先把要被濾掉的兩萬多筆權證存進來。
 *
 * 欄位全是基本型別，所以不需要 TypeConverter：日期存 epoch day。
 */
@Entity(tableName = "stocks")
internal data class StockEntity(
    @PrimaryKey val code: String,
    val name: String,
    val tradeDate: Long,
    val tradeVolume: Long?,
    val tradeValue: Long?,
    // 不叫 transaction，那是 SQL 保留字。Room 會自動加引號，但一個不用多想的欄位名更便宜。
    val transactionCount: Long?,
    val openingPrice: Double?,
    val highestPrice: Double?,
    val lowestPrice: Double?,
    val closingPrice: Double?,
    val change: Double?,
    val monthlyAveragePrice: Double?,
    /**
     * 交易所有沒有發布這檔的本益比資訊。
     *
     * 與下面三個數字分開存，是因為它們各自可能缺值——交易所會發出本益比空白、
     * 但殖利率有值的列。少了這個旗標，「整筆沒有」與「三欄剛好都空」在存取後就分不出來了。
     */
    val hasValuation: Boolean,
    val peRatio: Double?,
    val dividendYield: Double?,
    val pbRatio: Double?,
    val hasNoTrades: Boolean,
)
