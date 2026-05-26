package com.example.budgetwise.ui.recurring

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetwise.R
import com.example.budgetwise.data.model.Frequency
import com.example.budgetwise.data.model.RecurringTransaction
import com.example.budgetwise.data.model.TransactionType
import com.example.budgetwise.ui.theme.ExpenseRed
import com.example.budgetwise.ui.theme.ExpenseSurface
import com.example.budgetwise.ui.theme.IncomeGreen
import com.example.budgetwise.ui.theme.IncomeSurface
import com.example.budgetwise.util.CurrencyInfo
import com.example.budgetwise.util.EUR_DEFAULT
import com.example.budgetwise.util.categoryEmoji
import java.text.SimpleDateFormat
import java.util.*

private val DAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
private val dateFormatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())

private fun monthlyMultiplier(recurring: RecurringTransaction): Int {
    if (recurring.frequency == Frequency.MONTHLY) return 1
    val dow = recurring.dayOfWeek ?: 1
    val calDow = if (dow == 7) Calendar.SUNDAY else dow + 1
    val now = Calendar.getInstance()
    val cal = Calendar.getInstance().apply {
        set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 1)
    }
    return (1..cal.getActualMaximum(Calendar.DAY_OF_MONTH)).count { day ->
        cal.set(Calendar.DAY_OF_MONTH, day)
        cal.get(Calendar.DAY_OF_WEEK) == calDow
    }
}

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
    val category by viewModel.category.collectAsState()
    val dayOfWeek by viewModel.dayOfWeek.collectAsState()
    val dayOfMonth by viewModel.dayOfMonth.collectAsState()
    val hasEndDate by viewModel.hasEndDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showFrequencyMenu by remember { mutableStateOf(false) }
    var showDayOfMonthMenu by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current

    val isExpense = type == TransactionType.EXPENSE
    val amountColor = if (isExpense) ExpenseRed else IncomeGreen
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val selectedExpenseBg = if (isDark) MaterialTheme.colorScheme.background else ExpenseSurface
    val selectedIncomeBg = if (isDark) MaterialTheme.colorScheme.background else IncomeSurface

    val totalIn = recurringTransactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount * monthlyMultiplier(it) * currencyInfo.rate }
    val totalOut = recurringTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount * monthlyMultiplier(it) * currencyInfo.rate }

    var sheetType by remember { mutableStateOf<TransactionType?>(null) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (showEndDatePicker) {
        DisposableEffect(Unit) {
            val calendar = Calendar.getInstance().apply { timeInMillis = endDate }
            val dialog = DatePickerDialog(
                context,
                R.style.DatePickerTheme,
                { _, year, month, day ->
                    val cal = Calendar.getInstance()
                    cal.set(year, month, day)
                    viewModel.onEndDateChange(cal.timeInMillis)
                    showEndDatePicker = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dialog.setOnDismissListener { showEndDatePicker = false }
            dialog.show()
            onDispose { dialog.dismiss() }
        }
    }

    if (sheetType != null) {
        ModalBottomSheet(
            onDismissRequest = { sheetType = null },
            sheetState = sheetState
        ) {
            val sheetList = recurringTransactions.filter { it.type == sheetType }
            val sheetTitle = if (sheetType == TransactionType.INCOME) "Recurring In" else "Recurring Out"
            Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 32.dp)) {
                Text(
                    sheetTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                if (sheetList.isEmpty()) {
                    Text(
                        "No recurring ${if (sheetType == TransactionType.INCOME) "income" else "expenses"} yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 24.dp)
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(sheetList, key = { it.id }) { recurring ->
                            SwipeToDeleteRecurringItem(
                                recurring = recurring,
                                currencyInfo = currencyInfo,
                                onDelete = { viewModel.deleteRecurring(recurring) }
                            )
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Recurring Transactions") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
                .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus() }) },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RecurringSummaryCard(
                    label = "RECURRING IN",
                    amount = totalIn,
                    symbol = currencyInfo.symbol,
                    color = IncomeGreen,
                    modifier = Modifier.weight(1f),
                    onClick = { sheetType = TransactionType.INCOME }
                )
                RecurringSummaryCard(
                    label = "RECURRING OUT",
                    amount = totalOut,
                    symbol = currencyInfo.symbol,
                    color = ExpenseRed,
                    modifier = Modifier.weight(1f),
                    onClick = { sheetType = TransactionType.EXPENSE }
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.background
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Add New Recurring",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Type toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(if (isExpense) selectedExpenseBg else Color.Transparent)
                                .clickable { viewModel.onTypeChange(TransactionType.EXPENSE) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "↓ Expense",
                                color = if (isExpense) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (isExpense) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                        VerticalDivider(color = MaterialTheme.colorScheme.outline)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(if (!isExpense) selectedIncomeBg else Color.Transparent)
                                .clickable { viewModel.onTypeChange(TransactionType.INCOME) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "↑ Income",
                                color = if (!isExpense) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (!isExpense) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }

                    // Amount
                    var amountFocused by remember { mutableStateOf(false) }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "AMOUNT (${currencyInfo.code})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = currencyInfo.symbol.trim(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = amountColor,
                                modifier = Modifier.padding(end = 4.dp, bottom = 8.dp)
                            )
                            BasicTextField(
                                value = amount,
                                onValueChange = { viewModel.onAmountChange(it) },
                                modifier = Modifier.onFocusChanged { amountFocused = it.isFocused },
                                textStyle = TextStyle(
                                    fontSize = 48.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = amountColor,
                                    textAlign = TextAlign.Center
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                decorationBox = { innerTextField ->
                                    Box(contentAlignment = Alignment.Center) {
                                        if (amount.isEmpty()) {
                                            Text(
                                                "0.00",
                                                fontSize = 48.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = amountColor.copy(alpha = 0.3f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                        if (amount.isNotEmpty() && amountFocused) {
                            Spacer(modifier = Modifier.height(4.dp))
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(0.7f),
                                color = amountColor,
                                thickness = 2.dp
                            )
                        }
                    }

                    // Label
                    OutlinedTextField(
                        value = label,
                        onValueChange = { viewModel.onLabelChange(it) },
                        label = { Text("Label (e.g. Rent)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // Category
                    val categories = listOf("Food & Groceries", "Housing", "Salary", "Entertainment", "Transport", "Shopping", "Utilities", "Others")
                    Box {
                        OutlinedTextField(
                            value = "${categoryEmoji(category)} $category",
                            onValueChange = {},
                            label = { Text("Category") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { Text("▼") }
                        )
                        Box(modifier = Modifier
                            .matchParentSize()
                            .clickable { focusManager.clearFocus(); showCategoryMenu = true })
                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false }
                        ) {
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text("${categoryEmoji(cat)} $cat") },
                                    onClick = { viewModel.onCategoryChange(cat); showCategoryMenu = false }
                                )
                            }
                        }
                    }

                    // Frequency
                    Box {
                        OutlinedTextField(
                            value = frequency.name.lowercase().replaceFirstChar { it.uppercase() },
                            onValueChange = {},
                            label = { Text("Frequency") },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth(),
                            trailingIcon = { Text("▼") }
                        )
                        Box(modifier = Modifier
                            .matchParentSize()
                            .clickable { focusManager.clearFocus(); showFrequencyMenu = true })
                        DropdownMenu(
                            expanded = showFrequencyMenu,
                            onDismissRequest = { showFrequencyMenu = false }
                        ) {
                            Frequency.entries.forEach { freq ->
                                DropdownMenuItem(
                                    text = { Text(freq.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                    onClick = { viewModel.onFrequencyChange(freq); showFrequencyMenu = false }
                                )
                            }
                        }
                    }

                    // Day picker — depends on frequency
                    if (frequency == Frequency.WEEKLY) {
                        Column {
                            Text(
                                "Day of week",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                DAY_LABELS.forEachIndexed { index, dayLabel ->
                                    val dayNum = index + 1
                                    val selected = dayOfWeek == dayNum
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (selected) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable { viewModel.onDayOfWeekChange(dayNum) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            dayLabel,
                                            fontSize = 11.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (selected) MaterialTheme.colorScheme.onPrimary
                                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        Box {
                            OutlinedTextField(
                                value = "Day $dayOfMonth",
                                onValueChange = {},
                                label = { Text("Day of month") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = { Text("▼") }
                            )
                            Box(modifier = Modifier
                                .matchParentSize()
                                .clickable { focusManager.clearFocus(); showDayOfMonthMenu = true })
                            DropdownMenu(
                                expanded = showDayOfMonthMenu,
                                onDismissRequest = { showDayOfMonthMenu = false }
                            ) {
                                (1..31).forEach { day ->
                                    DropdownMenuItem(
                                        text = { Text("Day $day") },
                                        onClick = { viewModel.onDayOfMonthChange(day); showDayOfMonthMenu = false }
                                    )
                                }
                            }
                        }
                    }

                    // End date
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("End date", style = MaterialTheme.typography.bodyMedium)
                        Switch(
                            checked = hasEndDate,
                            onCheckedChange = { viewModel.onHasEndDateChange(it) }
                        )
                    }
                    if (hasEndDate) {
                        Box {
                            OutlinedTextField(
                                value = dateFormatter.format(Date(endDate)),
                                onValueChange = {},
                                label = { Text("End date") },
                                readOnly = true,
                                modifier = Modifier.fillMaxWidth(),
                                trailingIcon = {
                                    Text("📅", fontSize = 16.sp)
                                }
                            )
                            Box(modifier = Modifier
                                .matchParentSize()
                                .clickable { showEndDatePicker = true })
                        }
                    }

                    Button(
                        onClick = { viewModel.addRecurring() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        enabled = amount.isNotEmpty() && label.isNotEmpty()
                    ) {
                        Text("Add Recurring", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun RecurringSummaryCard(
    label: String,
    amount: Double,
    symbol: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = modifier
            .border(1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.medium)
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.8f),
                letterSpacing = 1.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "%s%.2f".format(symbol, amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "tap to view →",
                style = MaterialTheme.typography.labelSmall,
                color = color.copy(alpha = 0.5f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteRecurringItem(
    recurring: RecurringTransaction,
    currencyInfo: CurrencyInfo = EUR_DEFAULT,
    onDelete: () -> Unit
) {
    val state = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) { onDelete(); true } else false
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
                    .background(color = ExpenseRed, shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White,
                    modifier = Modifier.padding(end = 20.dp)
                )
            }
        }
    ) {
        RecurringItem(recurring = recurring, currencyInfo = currencyInfo, onDelete = onDelete)
    }
}

@Composable
fun RecurringItem(recurring: RecurringTransaction, currencyInfo: CurrencyInfo = EUR_DEFAULT, onDelete: () -> Unit) {
    val subtitle = buildString {
        when (recurring.frequency) {
            Frequency.WEEKLY -> {
                append("Weekly")
                recurring.dayOfWeek?.let { append(" (${DAY_LABELS[it - 1]})") }
            }
            Frequency.MONTHLY -> {
                append("Monthly")
                recurring.dayOfMonth?.let { append(" (day $it)") }
            }
        }
        append(" • ${recurring.category}")
        recurring.endDate?.let {
            append(" · until ${SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(it))}")
        }
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🗓️",
                fontSize = 28.sp,
                modifier = Modifier.size(44.dp).wrapContentSize()
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = recurring.label, fontWeight = FontWeight.Bold)
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Surface(
                shape = RoundedCornerShape(50),
                color = (if (recurring.type == TransactionType.INCOME) IncomeGreen else ExpenseRed).copy(alpha = 0.1f)
            ) {
                Text(
                    text = (if (recurring.type == TransactionType.INCOME) "+ " else "– ") +
                            "%s%.2f".format(currencyInfo.symbol, recurring.amount * currencyInfo.rate),
                    color = if (recurring.type == TransactionType.INCOME) IncomeGreen else ExpenseRed,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }
    }
}
