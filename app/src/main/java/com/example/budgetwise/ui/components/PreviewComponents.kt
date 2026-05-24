package com.example.budgetwise.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.budgetwise.data.model.Transaction
import com.example.budgetwise.data.model.TransactionType
import com.example.budgetwise.ui.dashboard.BalanceCard
import com.example.budgetwise.ui.dashboard.BalanceSummary
import com.example.budgetwise.ui.dashboard.BudgetProgress
import com.example.budgetwise.ui.dashboard.TransactionItem
import com.example.budgetwise.ui.theme.BudgetWiseTheme
import com.example.budgetwise.util.EUR_DEFAULT

@Preview(showBackground = true)
@Composable
fun BalanceCardPreview() {
    BudgetWiseTheme {
        BalanceCard(
            summary = BalanceSummary(
                balance = 1250.0,
                income = 2000.0,
                expense = 750.0,
                budget = 1000.0
            ),
            symbol = "€"
        )
    }
}

@Preview(showBackground = true)
@Composable
fun BudgetProgressPreview() {
    BudgetWiseTheme {
        BudgetProgress(expense = 750.0, budget = 1000.0, symbol = "€")
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionItemPreview() {
    BudgetWiseTheme {
        TransactionItem(
            transaction = Transaction(
                amount = 50.0,
                category = "Food",
                date = System.currentTimeMillis(),
                note = "Lunch",
                type = TransactionType.EXPENSE
            ),
            currencyInfo = EUR_DEFAULT
        )
    }
}
