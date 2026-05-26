package com.example.budgetwise.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Frequency {
    WEEKLY, MONTHLY
}

@Entity(tableName = "recurring_transactions")
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val label: String,
    val category: String,
    val frequency: Frequency,
    val type: TransactionType,
    val startDate: Long = System.currentTimeMillis(),
    val dayOfWeek: Int? = null,        // 1=Mon … 7=Sun; used when frequency=WEEKLY
    val dayOfMonth: Int? = null,       // 1–31; used when frequency=MONTHLY
    val endDate: Long? = null,
    val lastProcessedDate: Long? = null
)
