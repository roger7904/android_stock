package com.rogerchang.twsestock

import android.app.Application
import com.rogerchang.twsestock.data.di.dataModule
import com.rogerchang.twsestock.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class StockApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@StockApplication)
            modules(dataModule(enableNetworkLogging = BuildConfig.DEBUG), appModule)
        }
    }
}
