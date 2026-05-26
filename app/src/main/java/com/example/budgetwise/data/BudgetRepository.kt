package com.example.budgetwise.data

import com.example.budgetwise.data.local.TransactionDao
import com.example.budgetwise.data.model.Frequency
import com.example.budgetwise.data.model.MonthBudget
import com.example.budgetwise.data.model.RecurringTransaction
import com.example.budgetwise.data.model.Transaction
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

    suspend fun processRecurringTransactions() {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
        }

        for (recurring in transactionDao.getAllRecurringTransactionsOnce()) {
            val endCal = recurring.endDate?.let { Calendar.getInstance().apply { timeInMillis = it } }
            val effectiveEnd = if (endCal != null && endCal.before(now)) endCal else now

            // fromCal: midnight of the day after lastProcessed, or midnight of startDate
            val fromCal = Calendar.getInstance().apply {
                timeInMillis = recurring.lastProcessedDate ?: recurring.startDate
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                if (recurring.lastProcessedDate != null) add(Calendar.DAY_OF_MONTH, 1)
            }

            if (fromCal.after(effectiveEnd)) continue

            val dueDates = mutableListOf<Long>()

            when (recurring.frequency) {
                Frequency.MONTHLY -> {
                    val dom = recurring.dayOfMonth ?: 1
                    val cursor = Calendar.getInstance().apply {
                        set(fromCal.get(Calendar.YEAR), fromCal.get(Calendar.MONTH), 1, 12, 0, 0)
                        set(Calendar.MILLISECOND, 0)
                        val max = getActualMaximum(Calendar.DAY_OF_MONTH)
                        set(Calendar.DAY_OF_MONTH, minOf(dom, max))
                        if (before(fromCal)) add(Calendar.MONTH, 1)
                    }
                    while (!cursor.after(effectiveEnd)) {
                        val max = cursor.getActualMaximum(Calendar.DAY_OF_MONTH)
                        cursor.set(Calendar.DAY_OF_MONTH, minOf(dom, max))
                        if (!cursor.after(effectiveEnd)) dueDates.add(cursor.timeInMillis)
                        cursor.add(Calendar.MONTH, 1)
                    }
                }
                Frequency.WEEKLY -> {
                    // our 1=Mon…7=Sun → Calendar 2=Mon…1=Sun
                    val targetDow = recurring.dayOfWeek ?: 1
                    val calDow = if (targetDow == 7) Calendar.SUNDAY else targetDow + 1
                    val cursor = Calendar.getInstance().apply {
                        timeInMillis = fromCal.timeInMillis
                        while (get(Calendar.DAY_OF_WEEK) != calDow) add(Calendar.DAY_OF_MONTH, 1)
                        set(Calendar.HOUR_OF_DAY, 12); set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    }
                    while (!cursor.after(effectiveEnd)) {
                        dueDates.add(cursor.timeInMillis)
                        cursor.add(Calendar.DAY_OF_MONTH, 7)
                    }
                }
            }

            var lastProcessed: Long? = null
            for (dueDate in dueDates) {
                transactionDao.insertTransaction(
                    Transaction(
                        amount = recurring.amount,
                        category = recurring.category,
                        date = dueDate,
                        note = recurring.label,
                        type = recurring.type
                    )
                )
                lastProcessed = dueDate
            }
            if (lastProcessed != null) {
                transactionDao.updateRecurringLastProcessed(recurring.id, lastProcessed)
            }

            // Delete if end date is set and its month has fully passed
            if (recurring.endDate != null) {
                val endMonth = Calendar.getInstance().apply {
                    timeInMillis = recurring.endDate
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                    add(Calendar.MONTH, 1)
                }
                val startOfCurrentMonth = Calendar.getInstance().apply {
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                }
                if (!startOfCurrentMonth.before(endMonth)) {
                    transactionDao.deleteRecurringTransaction(recurring)
                }
            }
        }
    }

}
