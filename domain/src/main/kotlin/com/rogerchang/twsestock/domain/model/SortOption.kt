package com.rogerchang.twsestock.domain.model

/** 排序 bottom sheet 提供的選項，只有股票代號，預設降序。 */
enum class SortOption {
    CODE_DESC,
    CODE_ASC,
    ;

    companion object {
        val Default: SortOption = CODE_DESC
    }
}
