package com.example.budgetwise.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.budgetwise.data.model.Transaction
import com.example.budgetwise.data.model.TransactionType
import com.example.budgetwise.ui.theme.ExpenseRed
import com.example.budgetwise.ui.theme.IncomeGreen
import com.example.budgetwise.util.CurrencyInfo
import com.example.budgetwise.util.EUR_DEFAULT
import com.example.budgetwise.util.categoryEmoji
import com.example.budgetwise.util.categoryIconColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onAddTransaction: () -> Unit,
    onViewAllTransactions: () -> Unit = {}
) {
    val selectedMonthText by viewModel.selectedMonthText.collectAsState()
    val summary by viewModel.balanceSummary.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val incomingTransactions by viewModel.incomingTransactions.collectAsState()
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
            TopAppBar(
                title = { Text("BudgetWise", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = {}) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
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

            item { BalanceCard(summary, currencyInfo) }

            item {
                val incomingExpense = incomingTransactions
                    .filter { it.type == TransactionType.EXPENSE }
                    .sumOf { it.amount } // base currency — BudgetProgress applies the rate
                when {
                    summary.budget > 0 -> BudgetProgress(
                        expense = summary.expense,
                        incomingExpense = incomingExpense,
                        budget = summary.budget,
                        currencyInfo = currencyInfo,
                        onEditClick = { showBudgetDialog = true }
                    )
                    summary.hasTransactions || summary.hasExplicitBudget -> OutlinedButton(
                        onClick = { showBudgetDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Set budget for this month")
                    }
                }
            }

            if (incomingTransactions.isNotEmpty()) {
                item {
                    Text(
                        text = "INCOMING TRANSACTIONS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        incomingTransactions.forEachIndexed { index, incoming ->
                            IncomingTransactionRow(incoming, currencyInfo)
                            if (index < incomingTransactions.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "RECENT TRANSACTIONS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    if (recentTransactions.isEmpty()) {
                        Text(
                            text = "No transactions this month",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        recentTransactions.forEachIndexed { index, transaction ->
                            TransactionRow(transaction, currencyInfo)
                            if (index < recentTransactions.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        TextButton(
                            onClick = onViewAllTransactions,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "View all transactions →",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthPicker(monthText: String, onPrevious: () -> Unit, onNext: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Month")
        }
        Text(text = monthText, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        IconButton(onClick = onNext) {
            Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
        }
    }
}

@Composable
fun BalanceCard(summary: BalanceSummary, currencyInfo: CurrencyInfo = EUR_DEFAULT) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "TOTAL BALANCE",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "%s%.2f".format(currencyInfo.symbol, summary.balance * currencyInfo.rate),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryChip("↑ INCOME", summary.income, currencyInfo, Modifier.weight(1f))
                SummaryChip("↓ EXPENSES", summary.expense, currencyInfo, Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun SummaryChip(label: String, amount: Double, currencyInfo: CurrencyInfo, modifier: Modifier = Modifier) {
    val arrow = label.first().toString()
    val title = label.drop(2)
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = arrow,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                    letterSpacing = 0.5.sp
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "%s%.0f".format(currencyInfo.symbol, amount * currencyInfo.rate),
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun BudgetProgress(
    expense: Double,
    incomingExpense: Double = 0.0,
    budget: Double,
    currencyInfo: CurrencyInfo = EUR_DEFAULT,
    onEditClick: (() -> Unit)? = null
) {
    val spentFraction = (expense / budget).toFloat().coerceIn(0f, 1f)
    val incomingFraction = ((incomingExpense / budget).toFloat()).coerceIn(0f, 1f - spentFraction)
    val pct = (spentFraction * 100).toInt()
    val remaining = ((budget - expense - incomingExpense) * currencyInfo.rate).coerceAtLeast(0.0)
    val overBudget = (expense + incomingExpense) > budget
    val spentColor = if (spentFraction > 0.9f || overBudget) ExpenseRed else MaterialTheme.colorScheme.primary
    val incomingColor = Color(0xFF26C6DA) // fixed teal, visible in both themes

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Budget",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "%s%.0f / %s%.0f".format(
                            currencyInfo.symbol, expense * currencyInfo.rate,
                            currencyInfo.symbol, budget * currencyInfo.rate
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (onEditClick != null) {
                        Spacer(modifier = Modifier.width(4.dp))
                        IconButton(onClick = onEditClick, modifier = Modifier.size(20.dp)) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit budget",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            // Two-segment bar: spent + incoming
            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                val totalWidth = maxWidth
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (spentFraction > 0f) {
                        Box(
                            modifier = Modifier
                                .width(totalWidth * spentFraction)
                                .fillMaxHeight()
                                .background(spentColor)
                        )
                    }
                    if (incomingFraction > 0f) {
                        Box(
                            modifier = Modifier
                                .width(totalWidth * incomingFraction)
                                .fillMaxHeight()
                                .background(incomingColor)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "$pct% spent",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (incomingExpense > 0.0) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(incomingColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${currencyInfo.symbol}${"%.0f".format(incomingExpense * currencyInfo.rate)} incoming",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "${currencyInfo.symbol}${"%.0f".format(remaining)} left",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun TransactionRow(transaction: Transaction, currencyInfo: CurrencyInfo = EUR_DEFAULT) {
    val shortDateFormatter = SimpleDateFormat("MMM d", Locale.getDefault())
    val isIncome = transaction.type == TransactionType.INCOME
    val amountColor = if (isIncome) IncomeGreen else ExpenseRed
    val title = if (transaction.note.isNotBlank()) transaction.note else transaction.category
    val subtitle = "${transaction.category} · ${shortDateFormatter.format(Date(transaction.date))}"

    Row(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = categoryEmoji(transaction.category),
            fontSize = 28.sp,
            modifier = Modifier.size(44.dp).wrapContentSize()
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = amountColor.copy(alpha = 0.1f)
        ) {
            Text(
                text = (if (isIncome) "+ " else "– ") +
                        "%s%.2f".format(currencyInfo.symbol, transaction.amount * currencyInfo.rate),
                color = amountColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
}

@Composable
fun IncomingTransactionRow(incoming: IncomingTransaction, currencyInfo: CurrencyInfo = EUR_DEFAULT) {
    val dueDateFormatter = SimpleDateFormat("MMM d", Locale.getDefault())
    val isIncome = incoming.type == TransactionType.INCOME
    val amountColor = if (isIncome) IncomeGreen else ExpenseRed

    Row(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = categoryEmoji(incoming.category),
            fontSize = 28.sp,
            modifier = Modifier.size(44.dp).wrapContentSize()
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = incoming.label,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Due ${dueDateFormatter.format(Date(incoming.dueDate))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Surface(
            shape = RoundedCornerShape(50),
            color = amountColor.copy(alpha = 0.1f)
        ) {
            Text(
                text = (if (isIncome) "+ " else "– ") +
                        "%s%.2f".format(currencyInfo.symbol, incoming.amount * currencyInfo.rate),
                color = amountColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun TransactionItem(transaction: Transaction, currencyInfo: CurrencyInfo = EUR_DEFAULT) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        TransactionRow(transaction, currencyInfo)
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
