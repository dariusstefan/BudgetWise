package com.example.budgetwise.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetwise.data.model.Transaction
import com.example.budgetwise.data.model.TransactionType
import com.example.budgetwise.ui.theme.ExpenseRed
import com.example.budgetwise.ui.theme.IncomeGreen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onAddTransaction: () -> Unit
) {
    val selectedMonthText by viewModel.selectedMonthText.collectAsState()
    val summary by viewModel.balanceSummary.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("BudgetWise") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MonthPicker(
                    monthText = selectedMonthText,
                    onPrevious = { viewModel.previousMonth() },
                    onNext = { viewModel.nextMonth() }
                )
            }

            item {
                BalanceCard(summary)
            }

            if (summary.budget > 0) {
                item {
                    BudgetProgress(summary.expense, summary.budget)
                }
            }

            item {
                Text(
                    text = "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(recentTransactions) { transaction ->
                TransactionItem(transaction)
            }
        }
    }
}

@Composable
fun MonthPicker(
    monthText: String,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
        }
        Text(text = monthText, fontSize = 18.sp, fontWeight = FontWeight.Medium)
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
        }
    }
}

@Composable
fun BalanceCard(summary: BalanceSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Current Balance", style = MaterialTheme.typography.labelLarge)
            Text(
                text = "$%.2f".format(summary.balance),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SummaryItem("Income", summary.income, IncomeGreen)
                SummaryItem("Expenses", summary.expense, ExpenseRed)
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, amount: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Text(
            text = "$%.2f".format(amount),
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BudgetProgress(expense: Double, budget: Double) {
    val progress = (expense / budget).toFloat().coerceIn(0f, 1f)
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Monthly Budget", style = MaterialTheme.typography.labelMedium)
            Text(text = "$%.0f / $%.0f".format(expense, budget), style = MaterialTheme.typography.labelMedium)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth(),
            color = if (progress > 0.9f) ExpenseRed else MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun TransactionItem(transaction: Transaction) {
    val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = transaction.category, fontWeight = FontWeight.SemiBold)
            Text(text = dateFormatter.format(Date(transaction.date)), style = MaterialTheme.typography.bodySmall)
        }
        Text(
            text = (if (transaction.type == TransactionType.INCOME) "+" else "-") + "$%.2f".format(transaction.amount),
            color = if (transaction.type == TransactionType.INCOME) IncomeGreen else ExpenseRed,
            fontWeight = FontWeight.Bold
        )
    }
}
