package com.example.budgetwise.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetwise.data.BudgetRepository
import com.example.budgetwise.data.model.MonthBudget
import com.example.budgetwise.data.model.Transaction
import com.example.budgetwise.data.model.TransactionType
import com.example.budgetwise.data.preferences.PreferencesRepository
import com.example.budgetwise.util.CurrencyInfo
import com.example.budgetwise.util.EUR_DEFAULT
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val repository: BudgetRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance())
    val selectedMonth: StateFlow<Calendar> = _selectedMonth

    private val monthFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private val monthIdFormatter = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    private val _currencyInfo = MutableStateFlow(EUR_DEFAULT)
    val currencyInfo: StateFlow<CurrencyInfo> = _currencyInfo

    init {
        viewModelScope.launch {
            preferencesRepository.selectedCurrency.collect { currency ->
                val rate = if (currency == "EUR") {
                    1.0
                } else {
                    try {
                        val response = repository.getExchangeRates("EUR")
                        response.rates[currency] ?: 1.0
                    } catch (e: Exception) {
                        1.0
                    }
                }
                _currencyInfo.value = CurrencyInfo(currency, rate)
            }
        }
    }

    val selectedMonthText: StateFlow<String> = combine(_selectedMonth) { (calendar) ->
        monthFormatter.format(calendar.time)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private val transactionsForSelectedMonth: StateFlow<List<Transaction>> = _selectedMonth.flatMapLatest { calendar ->
        repository.allTransactions.combine(MutableStateFlow(calendar)) { transactions, cal ->
            transactions.filter { transaction ->
                val transCal = Calendar.getInstance().apply { timeInMillis = transaction.date }
                transCal.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                        transCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val monthBudget: StateFlow<MonthBudget?> = _selectedMonth.flatMapLatest { calendar ->
        repository.getBudgetForMonth(monthIdFormatter.format(calendar.time))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val balanceSummary = combine(
        transactionsForSelectedMonth,
        monthBudget,
        repository.allRecurringTransactions,
        _selectedMonth
    ) { transactions, budget, recurring, calendar ->
        val endOfMonth = Calendar.getInstance().apply {
            set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1)
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }.timeInMillis

        val transIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val transExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        val activeRecurring = recurring.filter { it.startDate <= endOfMonth }
        val recurIncome = activeRecurring.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val recurExpense = activeRecurring.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        val totalIncome = transIncome + recurIncome
        val totalExpense = transExpense + recurExpense

        BalanceSummary(
            balance = totalIncome - totalExpense,
            income = totalIncome,
            expense = totalExpense,
            budget = budget?.budgetAmount ?: 0.0
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BalanceSummary())

    val recentTransactions = transactionsForSelectedMonth.combine(MutableStateFlow(5)) { transactions, limit ->
        transactions.take(limit)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun nextMonth() {
        val next = _selectedMonth.value.clone() as Calendar
        next.add(Calendar.MONTH, 1)
        _selectedMonth.value = next
    }

    fun previousMonth() {
        val prev = _selectedMonth.value.clone() as Calendar
        prev.add(Calendar.MONTH, -1)
        _selectedMonth.value = prev
    }
}

data class BalanceSummary(
    val balance: Double = 0.0,
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val budget: Double = 0.0
)
