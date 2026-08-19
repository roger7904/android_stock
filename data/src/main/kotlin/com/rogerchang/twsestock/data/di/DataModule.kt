package com.rogerchang.twsestock.data.di

import com.rogerchang.twsestock.data.DefaultStockRepository
import com.rogerchang.twsestock.data.local.StockDatabase
import com.rogerchang.twsestock.data.local.ThemeDataStore
import com.rogerchang.twsestock.data.local.settingsDataStore
import com.rogerchang.twsestock.data.remote.createOkHttpClient
import com.rogerchang.twsestock.data.remote.createTwseApi
import com.rogerchang.twsestock.domain.StockRepository
import com.rogerchang.twsestock.domain.ThemePreferences
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

/**
 * @param enableNetworkLogging 由 `:app` 傳入 `BuildConfig.DEBUG`。
 *   這個模組不讀 BuildConfig，是為了讓「要不要印 log」成為呼叫端的決定而不是編譯期的巧合。
 */
fun dataModule(enableNetworkLogging: Boolean) = module {
    single { createOkHttpClient(enableNetworkLogging) }
    single { createTwseApi(get()) }

    single { StockDatabase.create(androidContext()) }
    single { get<StockDatabase>().stockDao() }

    single<StockRepository> { DefaultStockRepository(api = get(), dao = get()) }
    single<ThemePreferences> { ThemeDataStore(androidContext().settingsDataStore) }
}
