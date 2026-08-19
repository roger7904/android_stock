package com.rogerchang.twsestock.data.mapper

import com.rogerchang.twsestock.data.local.StockEntity
import com.rogerchang.twsestock.data.remote.DailyTradeDto
import com.rogerchang.twsestock.data.remote.MonthlyAverageDto
import com.rogerchang.twsestock.data.remote.ValuationRatioDto
import com.rogerchang.twsestock.domain.model.Stock
import com.rogerchang.twsestock.domain.model.Valuation
import java.time.LocalDate

// DTO → 中間 model。
//
// 只有兩種情況會丟掉整列：認不出是哪一檔（沒有代號），或是放不進時間軸（日期解析不出來）。
// 兩者都是關鍵欄位——代號是合併的 join key，日期是防止跨日拼接的依據。
// 其他欄位缺值都允許，缺值本來就會顯示成「–」。
// 丟掉一列而不是讓整批失敗，是因為呼叫端對 all-or-nothing 的失敗無能為力。

private fun code(raw: String?): String? = raw?.trim()?.takeIf { it.isNotEmpty() }

internal fun List<DailyTradeDto>.toDailyTrades(): List<DailyTrade> = mapNotNull { dto ->
    val code = code(dto.code) ?: return@mapNotNull null
    val date = parseRocDate(dto.date) ?: return@mapNotNull null

    DailyTrade(
        code = code,
        // 名稱空白還過得去，代號才是識別，卡片也是以代號開頭。
        name = dto.name?.trim().orEmpty(),
        date = date,
        tradeVolume = parseCount(dto.tradeVolume),
        tradeValue = parseCount(dto.tradeValue),
        transaction = parseCount(dto.transaction),
        openingPrice = parseDecimal(dto.openingPrice),
        highestPrice = parseDecimal(dto.highestPrice),
        lowestPrice = parseDecimal(dto.lowestPrice),
        closingPrice = parseDecimal(dto.closingPrice),
        change = parseDecimal(dto.change),
    )
}

internal fun List<MonthlyAverageDto>.toMonthlyAverages(): List<MonthlyAverage> = mapNotNull { dto ->
    val code = code(dto.code) ?: return@mapNotNull null
    val date = parseRocDate(dto.date) ?: return@mapNotNull null

    MonthlyAverage(
        code = code,
        date = date,
        closingPrice = parseDecimal(dto.closingPrice),
        monthlyAveragePrice = parseDecimal(dto.monthlyAveragePrice),
    )
}

internal fun List<ValuationRatioDto>.toValuationRatios(): List<ValuationRatio> = mapNotNull { dto ->
    val code = code(dto.code) ?: return@mapNotNull null
    val date = parseRocDate(dto.date) ?: return@mapNotNull null

    ValuationRatio(
        code = code,
        date = date,
        peRatio = parseDecimal(dto.peRatio),
        dividendYield = parseDecimal(dto.dividendYield),
        pbRatio = parseDecimal(dto.pbRatio),
    )
}

internal fun Stock.toEntity(): StockEntity = StockEntity(
    code = code,
    name = name,
    tradeDate = date.toEpochDay(),
    tradeVolume = tradeVolume,
    tradeValue = tradeValue,
    transactionCount = transaction,
    openingPrice = openingPrice,
    highestPrice = highestPrice,
    lowestPrice = lowestPrice,
    closingPrice = closingPrice,
    change = change,
    monthlyAveragePrice = monthlyAveragePrice,
    hasValuation = valuation != null,
    peRatio = valuation?.peRatio,
    dividendYield = valuation?.dividendYield,
    pbRatio = valuation?.pbRatio,
    hasNoTrades = hasNoTrades,
)

internal fun StockEntity.toStock(): Stock = Stock(
    code = code,
    name = name,
    date = LocalDate.ofEpochDay(tradeDate),
    tradeVolume = tradeVolume,
    tradeValue = tradeValue,
    transaction = transactionCount,
    openingPrice = openingPrice,
    highestPrice = highestPrice,
    lowestPrice = lowestPrice,
    closingPrice = closingPrice,
    change = change,
    monthlyAveragePrice = monthlyAveragePrice,
    valuation = if (hasValuation) Valuation(peRatio, dividendYield, pbRatio) else null,
    hasNoTrades = hasNoTrades,
)
