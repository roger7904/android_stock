package com.rogerchang.twsestock.data

import com.google.common.truth.Truth.assertThat
import com.rogerchang.twsestock.domain.model.DataError
import kotlinx.serialization.SerializationException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.io.InterruptedIOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class DataErrorsTest {
    @Test
    fun `逾時與斷線分開回報`() {
        assertThat(SocketTimeoutException().toDataError()).isEqualTo(DataError.Timeout)
        assertThat(UnknownHostException().toDataError()).isEqualTo(DataError.NoNetwork)
        assertThat(IOException().toDataError()).isEqualTo(DataError.NoNetwork)
    }

    @Test
    fun `callTimeout 的例外要被認出來是逾時`() {
        // OkHttp 的 callTimeout 丟的是裸的 InterruptedIOException("timeout")，
        // 不是 SocketTimeoutException。認錯的話最常見的逾時會顯示成「沒有網路」。
        assertThat(InterruptedIOException("timeout").toDataError()).isEqualTo(DataError.Timeout)
        // 同一個型別也用在執行緒被中斷，那不是逾時。
        assertThat(InterruptedIOException("interrupted").toDataError()).isEqualTo(DataError.NoNetwork)
    }

    @Test
    fun `HTTP 錯誤帶上狀態碼`() {
        val response = Response.error<Unit>(503, "".toResponseBody("application/json".toMediaType()))

        assertThat(HttpException(response).toDataError()).isEqualTo(DataError.Http(503))
    }

    @Test
    fun `解析失敗與未知錯誤分開`() {
        assertThat(SerializationException("bad").toDataError()).isEqualTo(DataError.Malformed)
        assertThat(IllegalStateException().toDataError()).isEqualTo(DataError.Unknown)
    }
}
