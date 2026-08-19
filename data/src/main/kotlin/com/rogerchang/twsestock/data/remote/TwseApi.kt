package com.rogerchang.twsestock.data.remote

import retrofit2.http.GET

/**
 * 題目指定的三支端點。
 *
 * 失敗時直接拋例外（IOException / HttpException / SerializationException），
 * 轉成 [com.rogerchang.twsestock.domain.model.DataError] 是 repository 的事——
 * 只有它知道一次失敗對使用者代表什麼。
 */
internal interface TwseApi {
    /** 日成交資訊，約 1,378 筆，是清單的主檔。 */
    @GET("exchangeReport/STOCK_DAY_ALL")
    suspend fun dailyTrades(): List<DailyTradeDto>

    /** 收盤價與月平均價，約 26,000 筆，其中兩萬多筆是權證。 */
    @GET("exchangeReport/STOCK_DAY_AVG_ALL")
    suspend fun monthlyAverages(): List<MonthlyAverageDto>

    /** 本益比、殖利率、股價淨值比，約 1,083 筆，有 295 檔交易所不發布。 */
    @GET("exchangeReport/BWIBBU_ALL")
    suspend fun valuationRatios(): List<ValuationRatioDto>
}
