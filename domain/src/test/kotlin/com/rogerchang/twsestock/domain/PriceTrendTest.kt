package com.rogerchang.twsestock.domain

import com.google.common.truth.Truth.assertThat
import com.rogerchang.twsestock.domain.model.PriceTrend
import com.rogerchang.twsestock.domain.model.changeTrend
import com.rogerchang.twsestock.domain.model.closingPriceTrend
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
    fun `漲跌價差以零為基準`() {
        assertThat(stock(change = 0.06).changeTrend()).isEqualTo(PriceTrend.RISE)
        assertThat(stock(change = -0.01).changeTrend()).isEqualTo(PriceTrend.FALL)
        assertThat(stock(change = 0.0).changeTrend()).isEqualTo(PriceTrend.FLAT)
        assertThat(stock(change = null).changeTrend()).isEqualTo(PriceTrend.UNKNOWN)
    }
}
