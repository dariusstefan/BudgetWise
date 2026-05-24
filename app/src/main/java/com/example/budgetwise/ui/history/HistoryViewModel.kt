package com.example.budgetwise.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetwise.data.BudgetRepository
import com.example.budgetwise.data.model.Transaction
import com.example.budgetwise.data.model.TransactionType
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class HistoryViewModel(private val repository: BudgetRepository) : ViewModel() {

    private val _filterType = MutableStateFlow<TransactionType?>(null)
    val filterType: StateFlow<TransactionType?> = _filterType

    private val _selectedMonth = MutableStateFlow(Calendar.getInstance())
    val selectedMonth: StateFlow<Calendar> = _selectedMonth

    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        repository.allTransactions,
        _filterType,
        _selectedMonth
    ) { transactions, type, cal ->
        transactions.filter { transaction ->
            val transCal = Calendar.getInstance().apply { timeInMillis = transaction.date }
            val monthMatch = transCal.get(Calendar.MONTH) == cal.get(Calendar.MONTH) &&
                    transCal.get(Calendar.YEAR) == cal.get(Calendar.YEAR)
            val typeMatch = type == null || transaction.type == type
            monthMatch && typeMatch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilterType(type: TransactionType?) { _filterType.value = type }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

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
