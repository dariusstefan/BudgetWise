package com.example.budgetwise.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetwise.data.BudgetRepository
import com.example.budgetwise.data.model.Transaction
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
import java.util.Calendar

class AddTransactionViewModel(
    private val repository: BudgetRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

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

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount

    private val _note = MutableStateFlow("")
    val note: StateFlow<String> = _note

    private val _category = MutableStateFlow("Food")
    val category: StateFlow<String> = _category

    private val _type = MutableStateFlow(TransactionType.EXPENSE)
    val type: StateFlow<TransactionType> = _type

    private val _date = MutableStateFlow(Calendar.getInstance().timeInMillis)
    val date: StateFlow<Long> = _date

    fun onAmountChange(value: String) { _amount.value = value }
    fun onNoteChange(value: String) { _note.value = value }
    fun onCategoryChange(value: String) { _category.value = value }
    fun onTypeChange(value: TransactionType) { _type.value = value }
    fun onDateChange(value: Long) { _date.value = value }

    fun saveTransaction(onSuccess: () -> Unit) {
        val amountValue = InputSanitizer.sanitizeAmount(_amount.value) ?: return
        val rate = _currencyInfo.value.rate
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    amount = amountValue / rate,
                    category = InputSanitizer.sanitizeLabel(_category.value),
                    date = _date.value,
                    note = InputSanitizer.sanitizeLabel(_note.value),
                    type = _type.value
                )
            )
            onSuccess()
        }
    }
}
