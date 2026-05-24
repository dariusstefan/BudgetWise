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

    private val _category = MutableStateFlow("Rent")
    val category: StateFlow<String> = _category

    private val _frequency = MutableStateFlow(Frequency.MONTHLY)
    val frequency: StateFlow<Frequency> = _frequency

    private val _type = MutableStateFlow(TransactionType.EXPENSE)
    val type: StateFlow<TransactionType> = _type

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

    fun addRecurring() {
        val amountValue = _amount.value.toDoubleOrNull() ?: return
        val rate = _currencyInfo.value.rate
        viewModelScope.launch {
            repository.insertRecurringTransaction(
                RecurringTransaction(
                    amount = amountValue / rate,
                    label = _label.value,
                    category = _category.value,
                    frequency = _frequency.value,
                    type = _type.value
                )
            )
            _amount.value = ""
            _label.value = ""
        }
    }

    fun deleteRecurring(recurringTransaction: RecurringTransaction) {
        viewModelScope.launch {
            repository.deleteRecurringTransaction(recurringTransaction)
        }
    }
}
