package com.example.budgetwise.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

enum class TransactionType {
    INCOME, EXPENSE
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val category: String,
    val date: Long, // Using timestamp
    val note: String,
    val type: TransactionType
)
