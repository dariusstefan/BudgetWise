package com.example.budgetwise.util

import androidx.compose.ui.graphics.Color

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

fun categoryEmoji(category: String): String = when {
    category.contains("food", ignoreCase = true) || category.contains("grocer", ignoreCase = true) -> "🛒"
    category.contains("rent", ignoreCase = true) || category.contains("housing", ignoreCase = true) -> "🏠"
    category.contains("salary", ignoreCase = true) -> "💼"
    category.contains("entertainment", ignoreCase = true) -> "🎬"
    category.contains("transport", ignoreCase = true) -> "🚗"
    category.contains("shopping", ignoreCase = true) -> "🛍️"
    category.contains("utilities", ignoreCase = true) || category.contains("electric", ignoreCase = true) || category.contains("bill", ignoreCase = true) -> "⚡"
    category.contains("water", ignoreCase = true) -> "💧"
    else -> "💰"
}

fun categoryIconColor(category: String): Color = when {
    category.contains("food", ignoreCase = true) || category.contains("grocer", ignoreCase = true) -> Color(0xFFE8F5E9)
    category.contains("rent", ignoreCase = true) || category.contains("housing", ignoreCase = true) -> Color(0xFFE3F2FD)
    category.contains("salary", ignoreCase = true) -> Color(0xFFF3E5F5)
    category.contains("entertainment", ignoreCase = true) -> Color(0xFFEDE7F6)
    category.contains("transport", ignoreCase = true) -> Color(0xFFFFF3E0)
    category.contains("shopping", ignoreCase = true) -> Color(0xFFFCE4EC)
    category.contains("utilities", ignoreCase = true) || category.contains("electric", ignoreCase = true) || category.contains("bill", ignoreCase = true) -> Color(0xFFFFFDE7)
    category.contains("water", ignoreCase = true) -> Color(0xFFE0F7FA)
    else -> Color(0xFFF5F5F5)
}
