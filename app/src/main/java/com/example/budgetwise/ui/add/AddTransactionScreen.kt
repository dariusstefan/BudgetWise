package com.example.budgetwise.ui.add

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.budgetwise.data.model.TransactionType
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: AddTransactionViewModel,
    onNavigateBack: () -> Unit
) {
    val amount by viewModel.amount.collectAsState()
    val note by viewModel.note.collectAsState()
    val category by viewModel.category.collectAsState()
    val type by viewModel.type.collectAsState()
    val date by viewModel.date.collectAsState()

    var showCategoryMenu by remember { mutableStateOf(false) }
    val categories = listOf("Food", "Rent", "Salary", "Entertainment", "Transport", "Shopping", "Others")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Transaction") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type Selection
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(
                    selected = type == TransactionType.EXPENSE,
                    onClick = { viewModel.onTypeChange(TransactionType.EXPENSE) }
                )
                Text("Expense")
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(
                    selected = type == TransactionType.INCOME,
                    onClick = { viewModel.onTypeChange(TransactionType.INCOME) }
                )
                Text("Income")
            }

            // Amount
            OutlinedTextField(
                value = amount,
                onValueChange = { viewModel.onAmountChange(it) },
                label = { Text("Amount") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            // Category
            Box {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    label = { Text("Category") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showCategoryMenu = true }) {
                            Text("▼")
                        }
                    }
                )
                DropdownMenu(
                    expanded = showCategoryMenu,
                    onDismissRequest = { showCategoryMenu = false }
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat) },
                            onClick = {
                                viewModel.onCategoryChange(cat)
                                showCategoryMenu = false
                            }
                        )
                    }
                }
            }

            // Date (Simple display for now)
            val dateFormatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            OutlinedTextField(
                value = dateFormatter.format(Date(date)),
                onValueChange = {},
                label = { Text("Date") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Note
            OutlinedTextField(
                value = note,
                onValueChange = { viewModel.onNoteChange(it) },
                label = { Text("Note (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.saveTransaction { onNavigateBack() } },
                modifier = Modifier.fillMaxWidth(),
                enabled = amount.isNotEmpty()
            ) {
                Text("Save Transaction")
            }
        }
    }
}
