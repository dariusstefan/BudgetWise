package com.example.budgetwise.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.Dialog
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
import com.example.budgetwise.util.CurrencyInfo
import com.example.budgetwise.util.EUR_DEFAULT
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
    val currencyInfo by viewModel.currencyInfo.collectAsState()

    var showBudgetDialog by remember { mutableStateOf(false) }

    if (showBudgetDialog) {
        BudgetDialog(
            currentBudget = if (summary.budget > 0) "%.2f".format(summary.budget * currencyInfo.rate) else "",
            symbol = currencyInfo.symbol,
            onConfirm = { amount ->
                viewModel.setBudget(amount)
                showBudgetDialog = false
            },
            onDismiss = { showBudgetDialog = false }
        )
    }

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
                BalanceCard(summary, currencyInfo)
            }

            item {
                if (summary.budget > 0) {
                    BudgetProgress(
                        expense = summary.expense,
                        budget = summary.budget,
                        currencyInfo = currencyInfo,
                        onEditClick = { showBudgetDialog = true }
                    )
                } else {
                    OutlinedButton(
                        onClick = { showBudgetDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set budget for this month")
                    }
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
                TransactionItem(transaction, currencyInfo)
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
fun BalanceCard(summary: BalanceSummary, currencyInfo: CurrencyInfo = EUR_DEFAULT) {
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
                text = "%s%.2f".format(currencyInfo.symbol, summary.balance * currencyInfo.rate),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                SummaryItem("Income", summary.income, IncomeGreen, currencyInfo)
                SummaryItem("Expenses", summary.expense, ExpenseRed, currencyInfo)
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, amount: Double, color: Color, currencyInfo: CurrencyInfo = EUR_DEFAULT) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall)
        Text(
            text = "%s%.2f".format(currencyInfo.symbol, amount * currencyInfo.rate),
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BudgetProgress(
    expense: Double,
    budget: Double,
    currencyInfo: CurrencyInfo = EUR_DEFAULT,
    onEditClick: (() -> Unit)? = null
) {
    val progress = (expense / budget).toFloat().coerceIn(0f, 1f)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Monthly Budget", style = MaterialTheme.typography.labelMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "%s%.0f / %s%.0f".format(currencyInfo.symbol, expense * currencyInfo.rate, currencyInfo.symbol, budget * currencyInfo.rate),
                    style = MaterialTheme.typography.labelMedium
                )
                if (onEditClick != null) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(20.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit budget", modifier = Modifier.size(14.dp))
                    }
                }
            }
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
fun TransactionItem(transaction: Transaction, currencyInfo: CurrencyInfo = EUR_DEFAULT) {
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
            text = (if (transaction.type == TransactionType.INCOME) "+" else "-") +
                    "%s%.2f".format(currencyInfo.symbol, transaction.amount * currencyInfo.rate),
            color = if (transaction.type == TransactionType.INCOME) IncomeGreen else ExpenseRed,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BudgetDialog(
    currentBudget: String,
    symbol: String,
    onConfirm: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf(currentBudget) }
    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Set Monthly Budget", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Amount") },
                    prefix = { Text(symbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { input.toDoubleOrNull()?.let(onConfirm) },
                        enabled = input.toDoubleOrNull() != null
                    ) { Text("Save") }
                }
            }
        }
    }
}
