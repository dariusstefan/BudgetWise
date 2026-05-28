package com.example.budgetwise.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetwise.data.BudgetRepository
import com.example.budgetwise.data.model.MonthBudget
import com.example.budgetwise.data.model.Transaction
import com.example.budgetwise.data.model.TransactionType
import com.example.budgetwise.data.preferences.PreferencesRepository
import com.example.budgetwise.util.nextDueDate
import com.example.budgetwise.util.CurrencyInfo
import com.example.budgetwise.util.EUR_DEFAULT
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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

    private val monthIdFormatter = SimpleDateFormat("yyyy-MM", Locale.US)

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

    private val defaultBudget = preferencesRepository.defaultBudget
    private val defaultBudgetSetAt = preferencesRepository.defaultBudgetSetAt

    private val defaultBudgetInfo = combine(defaultBudget, defaultBudgetSetAt) { amount, setAt ->
        Pair(amount, setAt)
    }

    val balanceSummary = combine(
        transactionsForSelectedMonth,
        monthBudget,
        _selectedMonth,
        defaultBudgetInfo
    ) { transactions, budget, calendar, (defBudget, budgetSetAt) ->
        val startOfMonth = Calendar.getInstance().apply {
            set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val budgetSetMonth = if (budgetSetAt > 0L) {
            Calendar.getInstance().apply {
                timeInMillis = budgetSetAt
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        } else Long.MAX_VALUE
        val effectiveDefaultBudget = if (startOfMonth >= budgetSetMonth) defBudget else 0.0

        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        BalanceSummary(
            balance = totalIncome - totalExpense,
            income = totalIncome,
            expense = totalExpense,
            budget = budget?.budgetAmount ?: effectiveDefaultBudget,
            hasExplicitBudget = budget != null,
            hasTransactions = transactions.isNotEmpty()
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), BalanceSummary())

    val recentTransactions = transactionsForSelectedMonth.combine(MutableStateFlow(5)) { transactions, limit ->
        transactions.take(limit)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomingTransactions: StateFlow<List<IncomingTransaction>> = combine(
        repository.allRecurringTransactions,
        _selectedMonth
    ) { recurrings, calendar ->
        val now = System.currentTimeMillis()
        val startOfMonth = Calendar.getInstance().apply {
            set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val endOfMonth = Calendar.getInstance().apply {
            set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), 1)
            add(Calendar.MONTH, 1)
            add(Calendar.MILLISECOND, -1)
        }.timeInMillis

        // Start scanning from today or from start of month, whichever is later
        val scanFrom = maxOf(now, startOfMonth - 1)

        recurrings.flatMap { recurring ->
            val dueDates = mutableListOf<Long>()
            var after = scanFrom
            while (true) {
                val next = nextDueDate(recurring, after) ?: break
                if (next > endOfMonth) break
                dueDates.add(next)
                after = next // nextDueDate uses strictly-after logic, so this advances
            }
            dueDates.map { dueDate ->
                IncomingTransaction(
                    label = recurring.label,
                    category = recurring.category,
                    amount = recurring.amount,
                    type = recurring.type,
                    dueDate = dueDate
                )
            }
        }.sortedBy { it.dueDate }
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

    fun setBudget(amount: Double) {
        val rate = _currencyInfo.value.rate
        val monthId = monthIdFormatter.format(_selectedMonth.value.time)
        viewModelScope.launch {
            repository.setBudgetForMonth(MonthBudget(monthId, amount / rate))
        }
    }
}

data class IncomingTransaction(
    val label: String,
    val category: String,
    val amount: Double,
    val type: TransactionType,
    val dueDate: Long
)

data class BalanceSummary(
    val balance: Double = 0.0,
    val income: Double = 0.0,
    val expense: Double = 0.0,
    val budget: Double = 0.0,
    val hasExplicitBudget: Boolean = false,
    val hasTransactions: Boolean = false
)
