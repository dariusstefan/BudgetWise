package com.example.budgetwise.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.budgetwise.data.BudgetRepository
import com.example.budgetwise.data.model.Transaction
import com.example.budgetwise.data.model.TransactionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class AddTransactionViewModel(private val repository: BudgetRepository) : ViewModel() {

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
        val amountValue = _amount.value.toDoubleOrNull() ?: return
        viewModelScope.launch {
            repository.insertTransaction(
                Transaction(
                    amount = amountValue,
                    category = _category.value,
                    date = _date.value,
                    note = _note.value,
                    type = _type.value
                )
            )
            onSuccess()
        }
    }
}
