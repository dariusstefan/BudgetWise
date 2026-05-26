package com.example.budgetwise.util

import com.example.budgetwise.data.model.Frequency
import com.example.budgetwise.data.model.RecurringTransaction
import java.util.Calendar

/**
 * Returns the next due timestamp for [recurring] strictly after [afterMillis],
 * or null if no future occurrence exists within the recurring's end date.
 */
fun nextDueDate(recurring: RecurringTransaction, afterMillis: Long): Long? {
    val endMillis = recurring.endDate
    val afterCal = Calendar.getInstance().apply {
        timeInMillis = afterMillis
        set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59); set(Calendar.MILLISECOND, 999)
    }
    return when (recurring.frequency) {
        Frequency.MONTHLY -> {
            val dom = recurring.dayOfMonth ?: 1
            val cursor = Calendar.getInstance().apply {
                set(afterCal.get(Calendar.YEAR), afterCal.get(Calendar.MONTH), 1, 12, 0, 0)
                set(Calendar.MILLISECOND, 0)
                set(Calendar.DAY_OF_MONTH, minOf(dom, getActualMaximum(Calendar.DAY_OF_MONTH)))
                if (!after(afterCal)) add(Calendar.MONTH, 1)
            }
            cursor.set(Calendar.DAY_OF_MONTH, minOf(dom, cursor.getActualMaximum(Calendar.DAY_OF_MONTH)))
            if (endMillis != null && cursor.timeInMillis > endMillis) null else cursor.timeInMillis
        }
        Frequency.WEEKLY -> {
            val calDow = recurring.dayOfWeek?.let { if (it == 7) Calendar.SUNDAY else it + 1 } ?: Calendar.MONDAY
            val cursor = Calendar.getInstance().apply {
                timeInMillis = afterMillis
                add(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 12); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
                while (get(Calendar.DAY_OF_WEEK) != calDow) add(Calendar.DAY_OF_MONTH, 1)
            }
            if (endMillis != null && cursor.timeInMillis > endMillis) null else cursor.timeInMillis
        }
    }
}

/** Counts how many times [dayOfWeek] (1=Mon…7=Sun) occurs in the given month. */
fun weeklyOccurrencesInMonth(dayOfWeek: Int, year: Int, month: Int): Int {
    val calDow = if (dayOfWeek == 7) Calendar.SUNDAY else dayOfWeek + 1
    val cal = Calendar.getInstance().apply { set(year, month, 1) }
    return (1..cal.getActualMaximum(Calendar.DAY_OF_MONTH)).count { day ->
        cal.set(Calendar.DAY_OF_MONTH, day)
        cal.get(Calendar.DAY_OF_WEEK) == calDow
    }
}
