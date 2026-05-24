package com.example.budgetwise.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val currency by viewModel.selectedCurrency.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val currencyInfo by viewModel.currencyInfo.collectAsState()
    val defaultBudget by viewModel.defaultBudget.collectAsState()

    var budgetInput by remember { mutableStateOf("") }
    var budgetSaved by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(defaultBudget, currencyInfo) {
        budgetInput = if (defaultBudget > 0) "%.2f".format(defaultBudget * currencyInfo.rate) else ""
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
            Text(
                "Applies to the current month and all future months without a specific override.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = { budgetInput = it },
                    label = { Text("Default Budget") },
                    prefix = { Text(currencyInfo.symbol) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val budgetChanged = budgetInput.toDoubleOrNull()?.let {
                    "%.2f".format(it) != "%.2f".format(defaultBudget * currencyInfo.rate)
                } ?: false
                Button(
                    onClick = {
                        budgetInput.toDoubleOrNull()?.let {
                            viewModel.setDefaultBudget(it)
                            scope.launch {
                                budgetSaved = true
                                delay(1500)
                                budgetSaved = false
                            }
                        }
                    },
                    enabled = budgetChanged
                ) {
                    Text(if (budgetSaved) "Saved ✓" else "Save")
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
