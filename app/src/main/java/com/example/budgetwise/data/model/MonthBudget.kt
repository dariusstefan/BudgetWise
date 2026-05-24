package com.example.budgetwise.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "month_budgets")
data class MonthBudget(
    @PrimaryKey val monthId: String, // Format "YYYY-MM"
    val budgetAmount: Double
)
