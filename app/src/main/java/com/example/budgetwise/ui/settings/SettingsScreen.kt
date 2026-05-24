package com.example.budgetwise.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.budgetwise.util.CurrencyInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val budget by viewModel.currentMonthBudget.collectAsState()
    val currency by viewModel.selectedCurrency.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val currencySymbol = CurrencyInfo(currency, 1.0).symbol

    var budgetInput by remember { mutableStateOf("") }
    LaunchedEffect(budget) {
        budgetInput = budget?.budgetAmount?.toString() ?: ""
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Settings") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Monthly Budget", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it },
                    label = { Text("Set Budget") },
                    prefix = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    budgetInput.toDoubleOrNull()?.let { viewModel.setBudget(it) }
                }) {
                    Text("Save")
                }
            }

            HorizontalDivider()

            Text("Display Currency", style = MaterialTheme.typography.titleMedium)
            var showCurrencyMenu by remember { mutableStateOf(false) }
            val currencies = listOf("EUR", "USD", "GBP", "RON")
            Box {
                OutlinedButton(onClick = { showCurrencyMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Selected: $currency")
                }
                DropdownMenu(expanded = showCurrencyMenu, onDismissRequest = { showCurrencyMenu = false }) {
                    currencies.forEach { curr ->
                        DropdownMenuItem(
                            text = { Text(curr) },
                            onClick = {
                                viewModel.setCurrency(curr)
                                showCurrencyMenu = false
                            }
                        )
                    }
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark Mode", style = MaterialTheme.typography.titleMedium)
                Switch(checked = isDarkMode, onCheckedChange = { viewModel.setDarkMode(it) })
            }
        }
    }
}
