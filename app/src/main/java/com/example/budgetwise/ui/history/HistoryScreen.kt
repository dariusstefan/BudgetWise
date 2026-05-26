package com.example.budgetwise.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetwise.data.model.Transaction
import com.example.budgetwise.data.model.TransactionType
import com.example.budgetwise.ui.dashboard.MonthPicker
import com.example.budgetwise.ui.dashboard.TransactionItem
import com.example.budgetwise.ui.theme.ExpenseRed
import com.example.budgetwise.ui.theme.ExpenseSurface
import com.example.budgetwise.ui.theme.IncomeGreen
import com.example.budgetwise.ui.theme.IncomeSurface
import com.example.budgetwise.util.CurrencyInfo
import com.example.budgetwise.util.EUR_DEFAULT
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val transactions by viewModel.filteredTransactions.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val currencyInfo by viewModel.currencyInfo.collectAsState()
    val monthIncome by viewModel.monthIncome.collectAsState()
    val monthExpense by viewModel.monthExpense.collectAsState()

    val monthFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val groupKeyFormatter = SimpleDateFormat("MMMM d", Locale.getDefault())

    val grouped = transactions
        .sortedByDescending { it.date }
        .groupBy { groupKeyFormatter.format(Date(it.date)).uppercase() }
        .entries
        .toList()

    Scaffold(
        topBar = { TopAppBar(title = { Text("History") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MonthPicker(
                monthText = monthFormatter.format(selectedMonth.time),
                onPrevious = { viewModel.previousMonth() },
                onNext = { viewModel.nextMonth() }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterType == null,
                    onClick = { viewModel.setFilterType(null) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = filterType == TransactionType.INCOME,
                    onClick = { viewModel.setFilterType(TransactionType.INCOME) },
                    label = { Text("Income") }
                )
                FilterChip(
                    selected = filterType == TransactionType.EXPENSE,
                    onClick = { viewModel.setFilterType(TransactionType.EXPENSE) },
                    label = { Text("Expenses") }
                )
            }

            // Monthly summary cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MonthlySummaryCard(
                    label = "INCOME",
                    amount = monthIncome * currencyInfo.rate,
                    symbol = currencyInfo.symbol,
                    amountColor = IncomeGreen,
                    backgroundColor = MaterialTheme.colorScheme.background,
                    modifier = Modifier.weight(1f)
                )
                MonthlySummaryCard(
                    label = "EXPENSES",
                    amount = monthExpense * currencyInfo.rate,
                    symbol = currencyInfo.symbol,
                    amountColor = ExpenseRed,
                    backgroundColor = MaterialTheme.colorScheme.background,
                    modifier = Modifier.weight(1f)
                )
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                grouped.forEach { (dateLabel, dayTransactions) ->
                    item(key = dateLabel) {
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(dayTransactions, key = { it.id }) { transaction ->
                        SwipeToDeleteTransactionItem(
                            transaction = transaction,
                            currencyInfo = currencyInfo,
                            onDelete = { viewModel.deleteTransaction(transaction) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlySummaryCard(
    label: String,
    amount: Double,
    symbol: String,
    amountColor: androidx.compose.ui.graphics.Color,
    backgroundColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium),
        shape = MaterialTheme.shapes.medium,
        color = backgroundColor
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = amountColor.copy(alpha = 0.8f),
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "%s%.0f".format(symbol, amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteTransactionItem(
    transaction: Transaction,
    currencyInfo: CurrencyInfo = EUR_DEFAULT,
    onDelete: () -> Unit
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else false
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.5f }
    )

    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 4.dp)
                    .background(
                        color = ExpenseRed,
                        shape = MaterialTheme.shapes.medium
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        }
    ) {
        TransactionItem(transaction, currencyInfo)
    }
}
