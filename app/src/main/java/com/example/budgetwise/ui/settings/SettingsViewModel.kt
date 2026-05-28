package com.example.budgetwise.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetwise.data.BudgetRepository
import com.example.budgetwise.data.preferences.PreferencesRepository
import com.example.budgetwise.ui.i18n.AppLanguage
import com.example.budgetwise.util.CurrencyInfo
import com.example.budgetwise.util.EUR_DEFAULT
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: BudgetRepository,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    val selectedCurrency: StateFlow<String> = preferencesRepository.selectedCurrency
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "EUR")

    val isDarkMode: StateFlow<Boolean> = preferencesRepository.isDarkMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val selectedLanguage: StateFlow<AppLanguage> = preferencesRepository.selectedLanguage
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppLanguage.ENGLISH)

    private val _currencyInfo = MutableStateFlow(EUR_DEFAULT)
    val currencyInfo: StateFlow<CurrencyInfo> = _currencyInfo

    val defaultBudget: StateFlow<Double> = preferencesRepository.defaultBudget
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

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

    fun setDefaultBudget(amount: Double) {
        val rate = _currencyInfo.value.rate
        viewModelScope.launch {
            preferencesRepository.setDefaultBudget(amount / rate)
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

    fun setLanguage(language: AppLanguage) {
        viewModelScope.launch {
            preferencesRepository.setSelectedLanguage(language)
        }
    }
}
