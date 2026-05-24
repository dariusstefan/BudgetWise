package com.example.budgetwise.data

import com.example.budgetwise.data.local.TransactionDao
import com.example.budgetwise.data.model.MonthBudget
import com.example.budgetwise.data.model.RecurringTransaction
import com.example.budgetwise.data.model.Transaction
import com.example.budgetwise.data.remote.ExchangeRateService
import kotlinx.coroutines.flow.Flow

class BudgetRepository(
    private val transactionDao: TransactionDao,
    private val exchangeRateService: ExchangeRateService
) {
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()
    val allRecurringTransactions: Flow<List<RecurringTransaction>> = transactionDao.getAllRecurringTransactions()

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransaction) {
        transactionDao.insertRecurringTransaction(recurringTransaction)
    }

    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction) {
        transactionDao.deleteRecurringTransaction(recurringTransaction)
    }

    fun getBudgetForMonth(monthId: String): Flow<MonthBudget?> {
        return transactionDao.getBudgetForMonth(monthId)
    }

    suspend fun setBudgetForMonth(monthBudget: MonthBudget) {
        transactionDao.setBudgetForMonth(monthBudget)
    }

    suspend fun getExchangeRates(base: String) = exchangeRateService.getLatestRates(base)
}
