package com.example.budgetwise.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetwise.data.BudgetRepository
import com.example.budgetwise.data.model.MonthBudget
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SettingsViewModel(private val repository: BudgetRepository) : ViewModel() {

    private val monthIdFormatter = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val currentMonthId = monthIdFormatter.format(Date())

    val currentMonthBudget: StateFlow<MonthBudget?> = repository.getBudgetForMonth(currentMonthId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _selectedCurrency = MutableStateFlow("EUR")
    val selectedCurrency: StateFlow<String> = _selectedCurrency

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    fun setBudget(amount: Double) {
        viewModelScope.launch {
            repository.setBudgetForMonth(MonthBudget(currentMonthId, amount))
        }
    }

    fun setCurrency(currency: String) {
        _selectedCurrency.value = currency
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
    }
}
