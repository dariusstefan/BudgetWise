package com.example.budgetwise.ui.add

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.budgetwise.data.model.TransactionType
import com.example.budgetwise.ui.theme.ExpenseRed
import com.example.budgetwise.ui.theme.IncomeGreen
import com.example.budgetwise.ui.theme.IncomeSurface
import com.example.budgetwise.ui.theme.ExpenseSurface
import com.example.budgetwise.util.categoryEmoji
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
    val currencyInfo by viewModel.currencyInfo.collectAsState()

    var showCategoryMenu by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val categories = listOf("Food & Groceries", "Housing", "Salary", "Entertainment", "Transport", "Shopping", "Utilities", "Others")
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    val isExpense = type == TransactionType.EXPENSE
    val amountColor = if (isExpense) ExpenseRed else IncomeGreen
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val selectedExpenseBg = if (isDark) MaterialTheme.colorScheme.background else ExpenseSurface
    val selectedIncomeBg = if (isDark) MaterialTheme.colorScheme.background else IncomeSurface

    if (showDatePicker) {
        DisposableEffect(Unit) {
            val calendar = Calendar.getInstance().apply { timeInMillis = date }
            val dialog = DatePickerDialog(
                context,
                com.example.budgetwise.R.style.DatePickerTheme,
                { _, year, month, day ->
                    val cal = Calendar.getInstance()
                    cal.set(year, month, day)
                    viewModel.onDateChange(cal.timeInMillis)
                    showDatePicker = false
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            dialog.setOnDismissListener { showDatePicker = false }
            dialog.show()
            onDispose { dialog.dismiss() }
        }
    }

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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Custom segmented toggle
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
                        text = "↓ Expense",
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
                        text = "↑ Income",
                        color = if (!isExpense) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (!isExpense) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
            }

            // Amount section with label + large BasicTextField + underline
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
                Spacer(modifier = Modifier.height(12.dp))
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

            // Category
            Box {
                OutlinedTextField(
                    value = "${categoryEmoji(category)} $category",
                    onValueChange = {},
                    label = { Text("Category") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Text("▼") }
                )
                Box(modifier = Modifier.matchParentSize().clickable { focusManager.clearFocus(); showCategoryMenu = true })
                DropdownMenu(expanded = showCategoryMenu, onDismissRequest = { showCategoryMenu = false }) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text("${categoryEmoji(cat)} $cat") },
                            onClick = { viewModel.onCategoryChange(cat); showCategoryMenu = false }
                        )
                    }
                }
            }

            // Note
            OutlinedTextField(
                value = note,
                onValueChange = { viewModel.onNoteChange(it) },
                label = { Text("Note (optional)") },
                placeholder = { Text("e.g. Weekly groceries", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                modifier = Modifier.fillMaxWidth()
            )

            // Date
            val dateFormatter = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault())
            Box {
                OutlinedTextField(
                    value = dateFormatter.format(Date(date)),
                    onValueChange = {},
                    label = { Text("Date") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pick date")
                    }
                )
                Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { viewModel.saveTransaction { onNavigateBack() } },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                enabled = amount.isNotEmpty()
            ) {
                Text("Save Transaction", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}
