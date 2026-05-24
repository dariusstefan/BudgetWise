package com.example.budgetwise.ui

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object History : Screen("history")
    object AddTransaction : Screen("add_transaction")
    object Recurring : Screen("recurring")
    object Settings : Screen("settings")
}
