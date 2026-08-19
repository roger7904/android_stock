package com.rogerchang.twsestock.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class StockFormatterTest {
    @Test
    fun `價格固定兩位小數`() {
        assertThat(formatPrice(14.7)).isEqualTo("14.70")
        assertThat(formatPrice(1234.567)).isEqualTo("1234.57")
    }

    @Test
    fun `缺值顯示破折號而不是零`() {
        assertThat(formatPrice(null)).isEqualTo(NO_VALUE)
        assertThat(formatChange(null)).isEqualTo(NO_VALUE)
        assertThat(formatCompactCount(null)).isEqualTo(NO_VALUE)
    }

    @Test
    fun `漲跌價差帶方向符號與正負號`() {
        assertThat(formatChange(0.06)).isEqualTo("▲ +0.06")
        assertThat(formatChange(-0.01)).isEqualTo("▼ -0.01")
        assertThat(formatChange(0.0)).isEqualTo("－ 0.00")
    }

    @Test
    fun `四捨五入後歸零就不該還帶方向`() {
        // API 給四位小數，顯示兩位。-0.001 進位後是 0.00，這時再標 ▼ 是騙人的。
        assertThat(formatChange(-0.001)).isEqualTo("－ 0.00")
        assertThat(formatSignedChange(-0.001)).isEqualTo("0.00")
    }

    @Test
    fun `成交量依大小換單位`() {
        assertThat(formatCompactCount(852)).isEqualTo("852")
        assertThat(formatCompactCount(8_912)).isEqualTo("8.9K")
        assertThat(formatCompactCount(49_694_719)).isEqualTo("49.7M")
        assertThat(formatCompactCount(738_788_003)).isEqualTo("738.8M")
        assertThat(formatCompactCount(2_500_000_000)).isEqualTo("2.5B")
    }

    @Test
    fun `進位到整千時往上跳一個單位`() {
        // 999,960 四捨五入是 1000.0K，應該寫成 1.0M。
        assertThat(formatCompactCount(999_960)).isEqualTo("1.0M")
    }
}
