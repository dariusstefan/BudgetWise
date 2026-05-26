package com.example.budgetwise.ui.recurring

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetwise.data.BudgetRepository
import com.example.budgetwise.data.model.Frequency
import com.example.budgetwise.data.model.RecurringTransaction
import com.example.budgetwise.data.model.TransactionType
import com.example.budgetwise.data.preferences.PreferencesRepository
import com.example.budgetwise.util.CurrencyInfo
import com.example.budgetwise.util.EUR_DEFAULT
import com.example.budgetwise.util.InputSanitizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecurringViewModel(
    private val repository: BudgetRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val recurringTransactions: StateFlow<List<RecurringTransaction>> = repository.allRecurringTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount

    private val _label = MutableStateFlow("")
    val label: StateFlow<String> = _label

    private val _category = MutableStateFlow("Others")
    val category: StateFlow<String> = _category

    private val _frequency = MutableStateFlow(Frequency.MONTHLY)
    val frequency: StateFlow<Frequency> = _frequency

    private val _type = MutableStateFlow(TransactionType.EXPENSE)
    val type: StateFlow<TransactionType> = _type

    private val _dayOfWeek = MutableStateFlow(1) // 1=Mon … 7=Sun
    val dayOfWeek: StateFlow<Int> = _dayOfWeek

    private val _dayOfMonth = MutableStateFlow(1) // 1–31
    val dayOfMonth: StateFlow<Int> = _dayOfMonth

    private val _hasEndDate = MutableStateFlow(false)
    val hasEndDate: StateFlow<Boolean> = _hasEndDate

    private val _endDate = MutableStateFlow(
        System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000
    )
    val endDate: StateFlow<Long> = _endDate

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

    fun onAmountChange(value: String) { _amount.value = value }
    fun onLabelChange(value: String) { _label.value = value }
    fun onCategoryChange(value: String) { _category.value = value }
    fun onFrequencyChange(value: Frequency) { _frequency.value = value }
    fun onTypeChange(value: TransactionType) { _type.value = value }
    fun onDayOfWeekChange(day: Int) { _dayOfWeek.value = day }
    fun onDayOfMonthChange(day: Int) { _dayOfMonth.value = day }
    fun onHasEndDateChange(value: Boolean) { _hasEndDate.value = value }
    fun onEndDateChange(value: Long) { _endDate.value = value }

    fun addRecurring() {
        val amountValue = InputSanitizer.sanitizeAmount(_amount.value) ?: return
        if (!InputSanitizer.isValidLabel(_label.value)) return
        val rate = _currencyInfo.value.rate
        val startOfMonth = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }.timeInMillis
        viewModelScope.launch {
            repository.insertRecurringTransaction(
                RecurringTransaction(
                    amount = amountValue / rate,
                    label = InputSanitizer.sanitizeLabel(_label.value),
                    category = InputSanitizer.sanitizeLabel(_category.value),
                    frequency = _frequency.value,
                    type = _type.value,
                    startDate = startOfMonth,
                    dayOfWeek = if (_frequency.value == Frequency.WEEKLY) _dayOfWeek.value else null,
                    dayOfMonth = if (_frequency.value == Frequency.MONTHLY) _dayOfMonth.value else null,
                    endDate = if (_hasEndDate.value) _endDate.value else null
                )
            )
            _amount.value = ""
            _label.value = ""
            _hasEndDate.value = false
            _endDate.value = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000
            repository.processRecurringTransactions()
        }
    }

    fun deleteRecurring(recurringTransaction: RecurringTransaction) {
        viewModelScope.launch {
            repository.deleteRecurringTransaction(recurringTransaction)
        }
    }
}
