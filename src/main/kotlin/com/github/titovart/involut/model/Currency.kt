package com.github.titovart.involut.model

enum class Currency(val code: String) {

    RUR("RUR"),
    USD("USD"),
    GBT("GBT");

    companion object Factory {
        fun valueOf(code: String): Currency {
            val upperedCode = code.toUpperCase()
            return values().firstOrNull { status -> status.code == upperedCode }
                ?: throw IllegalArgumentException("Currency code=$code isn't supported.")
        }
    }
}
