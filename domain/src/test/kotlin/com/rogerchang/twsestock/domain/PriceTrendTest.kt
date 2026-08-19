package com.rogerchang.twsestock.domain

import com.google.common.truth.Truth.assertThat
import com.rogerchang.twsestock.domain.model.PriceTrend
import com.rogerchang.twsestock.domain.model.changeTrend
import com.rogerchang.twsestock.domain.model.closingPriceTrend
import com.rogerchang.twsestock.domain.model.openingPriceTrend
import org.junit.Test

class PriceTrendTest {
    @Test
    fun `收盤價高於月平均價為漲、低於為跌`() {
        assertThat(stock(closingPrice = 101.0, monthlyAveragePrice = 100.0).closingPriceTrend())
            .isEqualTo(PriceTrend.RISE)
        assertThat(stock(closingPrice = 99.0, monthlyAveragePrice = 100.0).closingPriceTrend())
            .isEqualTo(PriceTrend.FALL)
        assertThat(stock(closingPrice = 100.0, monthlyAveragePrice = 100.0).closingPriceTrend())
            .isEqualTo(PriceTrend.FLAT)
    }

    @Test
    fun `月平均價缺值時不上色`() {
        assertThat(stock(closingPrice = 101.0, monthlyAveragePrice = null).closingPriceTrend())
            .isEqualTo(PriceTrend.UNKNOWN)
    }

    @Test
    fun `開盤價以當日收盤價為基準`() {
        assertThat(stock(openingPrice = 101.0, closingPrice = 100.0).openingPriceTrend())
            .isEqualTo(PriceTrend.RISE)
        assertThat(stock(openingPrice = 99.0, closingPrice = 100.0).openingPriceTrend())
            .isEqualTo(PriceTrend.FALL)
    }

    @Test
    fun `開高走低時開盤紅與漲跌綠會同時出現`() {
        // 兩個數字各自對照自己的基準，這兩個基準本來就會不一致。這是對的，不是矛盾。
        val openedHighDriftedDown = stock(openingPrice = 101.0, closingPrice = 100.0, change = -0.5)

        assertThat(openedHighDriftedDown.openingPriceTrend()).isEqualTo(PriceTrend.RISE)
        assertThat(openedHighDriftedDown.changeTrend()).isEqualTo(PriceTrend.FALL)
    }

    @Test
    fun `漲跌價差以零為基準`() {
        assertThat(stock(change = 0.06).changeTrend()).isEqualTo(PriceTrend.RISE)
        assertThat(stock(change = -0.01).changeTrend()).isEqualTo(PriceTrend.FALL)
        assertThat(stock(change = 0.0).changeTrend()).isEqualTo(PriceTrend.FLAT)
        assertThat(stock(change = null).changeTrend()).isEqualTo(PriceTrend.UNKNOWN)
    }
}
