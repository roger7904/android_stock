package com.rogerchang.twsestock.data

import com.rogerchang.twsestock.domain.model.DataError
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 把傳輸層的例外翻成畫面能直接對應文案的型別。
 *
 * 這是分層的邊界：IOException、HttpException 是 OkHttp 與 Retrofit 的詞彙，
 * 在這裡翻譯完，domain 才能維持純 JVM，使用端也才能用窮盡的 `when` 而不是猜例外種類。
 */
internal fun Throwable.toDataError(): DataError = when (this) {
    // 由具體排到一般，這幾個都是 IOException。
    is SocketTimeoutException -> DataError.Timeout

    // OkHttp 的 callTimeout 丟的不是 SocketTimeoutException，而是裸的
    // InterruptedIOException("timeout")。少了這行，最常見的逾時會掉到下面的
    // IOException 分支，變成告訴使用者「沒有網路」。
    // 只認這個訊息而不是整個型別：執行緒在讀取中被中斷時 JDK 也丟同一個型別，那不是逾時。
    is InterruptedIOException -> if (message == "timeout") DataError.Timeout else DataError.NoNetwork

    is UnknownHostException -> DataError.NoNetwork
    is HttpException -> DataError.Http(code())
    is SerializationException -> DataError.Malformed
    is IOException -> DataError.NoNetwork
    else -> DataError.Unknown
}
