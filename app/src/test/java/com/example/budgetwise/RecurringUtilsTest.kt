package com.example.budgetwise

import com.example.budgetwise.data.model.Frequency
import com.example.budgetwise.data.model.RecurringTransaction
import com.example.budgetwise.data.model.TransactionType
import com.example.budgetwise.util.nextDueDate
import com.example.budgetwise.util.weeklyOccurrencesInMonth
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class RecurringUtilsTest {

    private fun calendarOf(year: Int, month: Int, day: Int): Long =
        Calendar.getInstance().apply {
            set(year, month, day, 10, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun monthlyRecurring(dayOfMonth: Int, endDate: Long? = null) = RecurringTransaction(
        amount = 100.0, label = "Test", category = "Others",
        frequency = Frequency.MONTHLY, type = TransactionType.EXPENSE,
        dayOfMonth = dayOfMonth, endDate = endDate
    )

    private fun weeklyRecurring(dayOfWeek: Int, endDate: Long? = null) = RecurringTransaction(
        amount = 100.0, label = "Test", category = "Others",
        frequency = Frequency.WEEKLY, type = TransactionType.EXPENSE,
        dayOfWeek = dayOfWeek, endDate = endDate
    )

    // --- MONTHLY ---

    @Test
    fun `monthly due date falls in same month when day not yet passed`() {
        // Today = May 10, due day = 20 → next due = May 20
        val after = calendarOf(2026, Calendar.MAY, 10)
        val result = nextDueDate(monthlyRecurring(20), after)!!
        val cal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals(Calendar.MAY, cal.get(Calendar.MONTH))
        assertEquals(20, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `monthly due date advances to next month when day already passed`() {
        // Today = May 25, due day = 1 → next due = June 1
        val after = calendarOf(2026, Calendar.MAY, 25)
        val result = nextDueDate(monthlyRecurring(1), after)!!
        val cal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals(Calendar.JUNE, cal.get(Calendar.MONTH))
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun `monthly returns null when next due is after end date`() {
        val after = calendarOf(2026, Calendar.MAY, 25)
        val endDate = calendarOf(2026, Calendar.MAY, 31)
        // Due day = 1, so next is June 1, which is after end date
        assertNull(nextDueDate(monthlyRecurring(1, endDate), after))
    }

    @Test
    fun `monthly returns result when next due is before end date`() {
        val after = calendarOf(2026, Calendar.MAY, 10)
        val endDate = calendarOf(2026, Calendar.MAY, 31)
        assertNotNull(nextDueDate(monthlyRecurring(20, endDate), after))
    }

    // --- WEEKLY ---

    @Test
    fun `weekly returns next occurrence of given weekday`() {
        // May 27, 2026 is a Wednesday (4). Next Monday (1) should be June 1.
        val after = calendarOf(2026, Calendar.MAY, 27)
        val result = nextDueDate(weeklyRecurring(1), after)!! // Monday
        val cal = Calendar.getInstance().apply { timeInMillis = result }
        assertEquals(Calendar.MONDAY, cal.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun `weekly returns null when next occurrence is after end date`() {
        val after = calendarOf(2026, Calendar.MAY, 27)
        val endDate = calendarOf(2026, Calendar.MAY, 28) // too soon
        assertNull(nextDueDate(weeklyRecurring(1, endDate), after)) // next Monday is June 1
    }

    // --- weeklyOccurrencesInMonth ---

    @Test
    fun `monday occurs 4 or 5 times in a month`() {
        val count = weeklyOccurrencesInMonth(1, 2026, Calendar.MAY)
        assertTrue(count == 4 || count == 5)
    }

    @Test
    fun `weekly occurrences are always between 4 and 5`() {
        for (dow in 1..7) {
            val count = weeklyOccurrencesInMonth(dow, 2026, Calendar.MAY)
            assertTrue("dow=$dow count=$count", count in 4..5)
        }
    }

    @Test
    fun `total weekly occurrences across all days equals days in month`() {
        val total = (1..7).sumOf { weeklyOccurrencesInMonth(it, 2026, Calendar.MAY) }
        assertEquals(31, total) // May has 31 days
    }
}
