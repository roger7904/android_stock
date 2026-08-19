package com.rogerchang.twsestock.di

import com.rogerchang.twsestock.domain.GetStockListUseCase
import com.rogerchang.twsestock.ui.stocklist.StockListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    factory { GetStockListUseCase(get()) }
    viewModel { StockListViewModel(repository = get(), getStockList = get()) }
}
