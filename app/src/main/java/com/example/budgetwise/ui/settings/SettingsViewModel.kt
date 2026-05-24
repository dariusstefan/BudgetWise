package com.example.budgetwise.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetwise.data.BudgetRepository
import com.example.budgetwise.data.model.MonthBudget
import com.example.budgetwise.data.preferences.PreferencesRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class SettingsViewModel(
    private val repository: BudgetRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    private val monthIdFormatter = SimpleDateFormat("yyyy-MM", Locale.getDefault())
    private val currentMonthId = monthIdFormatter.format(Date())

    val currentMonthBudget: StateFlow<MonthBudget?> = repository.getBudgetForMonth(currentMonthId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val selectedCurrency: StateFlow<String> = preferencesRepository.selectedCurrency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "EUR")

    val isDarkMode: StateFlow<Boolean> = preferencesRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun setBudget(amount: Double) {
        viewModelScope.launch {
            repository.setBudgetForMonth(MonthBudget(currentMonthId, amount))
        }
    }

    fun setCurrency(currency: String) {
        viewModelScope.launch {
            preferencesRepository.setSelectedCurrency(currency)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setDarkMode(enabled)
        }
    }
}
