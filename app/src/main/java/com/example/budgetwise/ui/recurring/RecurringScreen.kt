package com.example.budgetwise.ui.recurring

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.budgetwise.data.model.Frequency
import com.example.budgetwise.data.model.RecurringTransaction
import com.example.budgetwise.data.model.TransactionType
import com.example.budgetwise.ui.theme.ExpenseRed
import com.example.budgetwise.ui.theme.IncomeGreen
import com.example.budgetwise.util.CurrencyInfo
import com.example.budgetwise.util.EUR_DEFAULT

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
    viewModel: RecurringViewModel
) {
    val recurringTransactions by viewModel.recurringTransactions.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val label by viewModel.label.collectAsState()
    val frequency by viewModel.frequency.collectAsState()
    val type by viewModel.type.collectAsState()
    val currencyInfo by viewModel.currencyInfo.collectAsState()

    var showFrequencyMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Recurring Transactions") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Add New Recurring", style = MaterialTheme.typography.titleSmall)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = type == TransactionType.EXPENSE, onClick = { viewModel.onTypeChange(TransactionType.EXPENSE) })
                        Text("Expense")
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(selected = type == TransactionType.INCOME, onClick = { viewModel.onTypeChange(TransactionType.INCOME) })
                        Text("Income")
                    }

                    OutlinedTextField(
                        value = label,
                        onValueChange = { viewModel.onLabelChange(it) },
                        label = { Text("Label (e.g. Rent)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { viewModel.onAmountChange(it) },
                            label = { Text("Amount") },
                            prefix = { Text(currencyInfo.symbol) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            OutlinedButton(onClick = { showFrequencyMenu = true }, modifier = Modifier.fillMaxWidth()) {
                                Text(frequency.name)
                            }
                            DropdownMenu(expanded = showFrequencyMenu, onDismissRequest = { showFrequencyMenu = false }) {
                                Frequency.entries.forEach { freq ->
                                    DropdownMenuItem(text = { Text(freq.name) }, onClick = {
                                        viewModel.onFrequencyChange(freq)
                                        showFrequencyMenu = false
                                    })
                                }
                            }
                        }
                    }

                    Button(onClick = { viewModel.addRecurring() }, modifier = Modifier.fillMaxWidth(), enabled = amount.isNotEmpty() && label.isNotEmpty()) {
                        Text("Add")
                    }
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(recurringTransactions) { recurring ->
                    RecurringItem(recurring = recurring, currencyInfo = currencyInfo, onDelete = { viewModel.deleteRecurring(recurring) })
                }
            }
        }
    }
}

@Composable
fun RecurringItem(recurring: RecurringTransaction, currencyInfo: CurrencyInfo = EUR_DEFAULT, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = recurring.label, fontWeight = FontWeight.Bold)
                Text(text = "${recurring.frequency.name} • ${recurring.category}", style = MaterialTheme.typography.bodySmall)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = (if (recurring.type == TransactionType.INCOME) "+" else "-") +
                            "%s%.2f".format(currencyInfo.symbol, recurring.amount * currencyInfo.rate),
                    color = if (recurring.type == TransactionType.INCOME) IncomeGreen else ExpenseRed,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
