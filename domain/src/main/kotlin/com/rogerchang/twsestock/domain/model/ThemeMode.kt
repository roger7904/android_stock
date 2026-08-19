package com.rogerchang.twsestock.domain.model

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
    ;

    companion object {
        val Default: ThemeMode = SYSTEM
    }
}
