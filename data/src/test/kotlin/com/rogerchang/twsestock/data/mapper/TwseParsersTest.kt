package com.rogerchang.twsestock.data.mapper

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.time.LocalDate

class TwseParsersTest {
    @Test
    fun `民國年轉西元`() {
        assertThat(parseRocDate("1150814")).isEqualTo(LocalDate.of(2026, 8, 14))
        assertThat(parseRocDate("991231")).isEqualTo(LocalDate.of(2010, 12, 31))
    }

    @Test
    fun `長度不對的日期一律拒絕`() {
        // 八碼會被讀成民國 1115 年，得到一個西元 3026 年的合法日期，而且沒有任何徵兆。
        assertThat(parseRocDate("11150814")).isNull()
        assertThat(parseRocDate("11508")).isNull()
        assertThat(parseRocDate("")).isNull()
        assertThat(parseRocDate(null)).isNull()
    }

    @Test
    fun `不存在的日期回傳 null 而不是拋例外`() {
        assertThat(parseRocDate("1151315")).isNull()
        assertThat(parseRocDate("1150231")).isNull()
        assertThat(parseRocDate("115081a")).isNull()
    }

    @Test
    fun `空字串解析成 null 而不是零`() {
        assertThat(parseDecimal("")).isNull()
        assertThat(parseCount("")).isNull()
        assertThat(parseDecimal("14.79")).isEqualTo(14.79)
        assertThat(parseCount("49694719")).isEqualTo(49_694_719L)
    }

    @Test
    fun `容忍千分位逗號`() {
        assertThat(parseCount("49,694,719")).isEqualTo(49_694_719L)
        assertThat(parseDecimal("1,234.56")).isEqualTo(1234.56)
    }
}
