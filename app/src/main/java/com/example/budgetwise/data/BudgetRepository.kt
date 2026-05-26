package com.example.budgetwise.data

import com.example.budgetwise.data.local.TransactionDao
import com.example.budgetwise.data.model.MonthBudget
import com.example.budgetwise.data.model.RecurringTransaction
import com.example.budgetwise.data.model.Transaction
import com.example.budgetwise.data.model.TransactionType
import com.example.budgetwise.data.remote.ExchangeRateService
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

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

    suspend fun seedMay2026IfEmpty() {
        fun date(day: Int): Long = Calendar.getInstance().apply {
            set(2026, Calendar.MAY, day, 12, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val start = Calendar.getInstance().apply { set(2026, Calendar.MAY, 1, 0, 0, 0) }.timeInMillis
        val end   = Calendar.getInstance().apply { set(2026, Calendar.MAY, 31, 23, 59, 59) }.timeInMillis
        if (transactionDao.countTransactionsInRange(start, end) > 0) return

        val transactions = listOf(
            Transaction(amount = 3000.0,  category = "Salary",          date = date(1),  note = "Monthly salary",       type = TransactionType.INCOME),
            Transaction(amount = 700.0,   category = "Housing",          date = date(1),  note = "Rent",                 type = TransactionType.EXPENSE),
            Transaction(amount = 124.5,   category = "Food & Groceries", date = date(3),  note = "Grocery Shopping",     type = TransactionType.EXPENSE),
            Transaction(amount = 37.0,    category = "Entertainment",    date = date(5),  note = "Netflix",              type = TransactionType.EXPENSE),
            Transaction(amount = 85.0,    category = "Utilities",        date = date(7),  note = "Electricity Bill",     type = TransactionType.EXPENSE),
            Transaction(amount = 42.0,    category = "Utilities",        date = date(7),  note = "Water Bill",           type = TransactionType.EXPENSE),
            Transaction(amount = 98.3,    category = "Food & Groceries", date = date(12), note = "Grocery Shopping",     type = TransactionType.EXPENSE),
            Transaction(amount = 65.0,    category = "Transport",        date = date(14), note = "Monthly transit pass", type = TransactionType.EXPENSE),
            Transaction(amount = 500.0,   category = "Salary",          date = date(15), note = "Freelance project",    type = TransactionType.INCOME),
            Transaction(amount = 230.0,   category = "Shopping",         date = date(18), note = "Clothes",              type = TransactionType.EXPENSE),
            Transaction(amount = 110.0,   category = "Food & Groceries", date = date(22), note = "Grocery Shopping",     type = TransactionType.EXPENSE),
            Transaction(amount = 55.0,    category = "Entertainment",    date = date(24), note = "Concert tickets",      type = TransactionType.EXPENSE),
            Transaction(amount = 30.0,    category = "Transport",        date = date(26), note = "Fuel",                 type = TransactionType.EXPENSE),
        )
        transactions.forEach { transactionDao.insertTransaction(it) }
    }
}
