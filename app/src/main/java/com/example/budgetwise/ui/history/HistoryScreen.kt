package com.example.budgetwise.ui.history

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
import com.example.budgetwise.data.model.Transaction
import com.example.budgetwise.data.model.TransactionType
import com.example.budgetwise.ui.dashboard.MonthPicker
import com.example.budgetwise.ui.dashboard.TransactionItem
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel
) {
    val transactions by viewModel.filteredTransactions.collectAsState()
    val filterType by viewModel.filterType.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()

    val monthFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Transaction History") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    label = { Text("Expense") }
                )
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(transactions) { transaction ->
                    SwipeToDeleteTransactionItem(
                        transaction = transaction,
                        onDelete = { viewModel.deleteTransaction(transaction) }
                    )
                }
            }
        }
    }
}

@Composable
fun SwipeToDeleteTransactionItem(
    transaction: Transaction,
    onDelete: () -> Unit
) {
    // Simple Row with delete button for now instead of complex SwipeToDismiss
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            TransactionItem(transaction)
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
        }
    }
}
