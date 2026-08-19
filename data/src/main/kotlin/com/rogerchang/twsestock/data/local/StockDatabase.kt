package com.rogerchang.twsestock.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [StockEntity::class], version = 1, exportSchema = false)
internal abstract class StockDatabase : RoomDatabase() {
    abstract fun stockDao(): StockDao

    companion object {
        fun create(context: Context): StockDatabase =
            Room.databaseBuilder(context, StockDatabase::class.java, "twse-stock.db")
                // 這張表是純快取，沒有使用者資料，寫 migration 沒有任何收益。
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
    }
}
