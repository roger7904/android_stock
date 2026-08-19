package com.rogerchang.twsestock.domain.model

/**
 * 更新失敗的原因，用畫面能直接對應文案的方式表達。
 *
 * 不用 Throwable：sealed interface 讓 `when` 具備窮盡性，新增一種錯誤時
 * 編譯器會要求每個使用端補上處理；也讓 IOException 這類傳輸層型別
 * 不會流進 domain。轉換只發生在 data 層的邊界。
 */
sealed interface DataError {
    data object NoNetwork : DataError

    data object Timeout : DataError

    data class Http(val code: Int) : DataError

    data object Malformed : DataError

    data object Unknown : DataError
}
