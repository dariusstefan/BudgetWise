package com.example.budgetwise.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.budgetwise.data.model.MonthBudget
import com.example.budgetwise.data.model.RecurringTransaction
import com.example.budgetwise.data.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT COUNT(*) FROM transactions WHERE date >= :start AND date <= :end")
    suspend fun countTransactionsInRange(start: Long, end: Long): Int

    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)

    @Query("SELECT * FROM recurring_transactions")
    fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>>

    @Insert
    suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransaction)

    @Delete
    suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction)

    @Query("SELECT * FROM month_budgets WHERE monthId = :monthId")
    fun getBudgetForMonth(monthId: String): Flow<MonthBudget?>

    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun setBudgetForMonth(monthBudget: MonthBudget)
}
