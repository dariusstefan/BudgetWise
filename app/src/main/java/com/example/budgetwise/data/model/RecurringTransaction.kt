package com.example.budgetwise.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class Frequency {
    DAILY, WEEKLY, MONTHLY
}

@Entity(tableName = "recurring_transactions")
data class RecurringTransaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val label: String,
    val category: String,
    val frequency: Frequency,
    val type: TransactionType,
    val startDate: Long = System.currentTimeMillis()
)
