package com.example.budgetwise.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.budgetwise.data.BudgetRepository
import com.example.budgetwise.ui.add.AddTransactionViewModel
import com.example.budgetwise.ui.dashboard.DashboardViewModel
import com.example.budgetwise.ui.history.HistoryViewModel
import com.example.budgetwise.ui.recurring.RecurringViewModel
import com.example.budgetwise.ui.settings.SettingsViewModel

class BudgetViewModelFactory(private val repository: BudgetRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(DashboardViewModel::class.java) -> DashboardViewModel(repository) as T
            modelClass.isAssignableFrom(HistoryViewModel::class.java) -> HistoryViewModel(repository) as T
            modelClass.isAssignableFrom(AddTransactionViewModel::class.java) -> AddTransactionViewModel(repository) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> SettingsViewModel(repository) as T
            modelClass.isAssignableFrom(RecurringViewModel::class.java) -> RecurringViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
