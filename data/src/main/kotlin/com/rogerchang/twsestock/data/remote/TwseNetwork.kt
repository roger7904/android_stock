package com.rogerchang.twsestock.data.remote

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://openapi.twse.com.tw/v1/"
private const val TIMEOUT_SECONDS = 30L

/**
 * 題目給的 model 沒有列出 `Date`，但三支 API 每一列都會回傳它，所以 DTO 補上了這個欄位。
 * 再開 `ignoreUnknownKeys`，是為了讓交易所日後多加一欄時，已安裝的版本不會就此解析失敗。
 */
internal val TwseJson = Json { ignoreUnknownKeys = true }

internal fun createOkHttpClient(enableLogging: Boolean): OkHttpClient = OkHttpClient.Builder()
    .apply {
        // callTimeout 管的是整趟請求（DNS、連線、轉址、讀完 body）。
        // 只設 read timeout 擋不住慢慢吐位元組的回應，它會一直把 read timeout 重置掉。
        callTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)

        if (enableLogging) {
            // HEADERS 而不是 BODY：最大的回應是 3 MB 的 JSON，印出來 logcat 就不能用了。
            addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.HEADERS })
        }
    }
    .build()

internal fun createTwseApi(client: OkHttpClient): TwseApi = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(client)
    .addConverterFactory(TwseJson.asConverterFactory("application/json".toMediaType()))
    .build()
    .create(TwseApi::class.java)
