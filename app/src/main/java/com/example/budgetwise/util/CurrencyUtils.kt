package com.example.budgetwise.util

data class CurrencyInfo(val code: String, val rate: Double) {
    val symbol: String
        get() = when (code) {
            "EUR" -> "€"
            "GBP" -> "£"
            "RON" -> "RON "
            else -> "$"
        }
}

val EUR_DEFAULT = CurrencyInfo("EUR", 1.0)
