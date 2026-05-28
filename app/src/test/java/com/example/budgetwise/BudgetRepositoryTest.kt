package com.example.budgetwise

import com.example.budgetwise.data.BudgetRepository
import com.example.budgetwise.data.local.TransactionDao
import com.example.budgetwise.data.model.Frequency
import com.example.budgetwise.data.model.MonthBudget
import com.example.budgetwise.data.model.RecurringTransaction
import com.example.budgetwise.data.model.Transaction
import com.example.budgetwise.data.model.TransactionType
import com.example.budgetwise.data.remote.ExchangeRateResponse
import com.example.budgetwise.data.remote.ExchangeRateService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class BudgetRepositoryTest {

    private lateinit var fakeDao: FakeTransactionDao
    private lateinit var repository: BudgetRepository

    @Before
    fun setup() {
        fakeDao = FakeTransactionDao()
        repository = BudgetRepository(fakeDao, FakeExchangeRateService())
    }

    // --- insert ---

    @Test
    fun `insertTransaction appears in allTransactions flow`() = runTest {
        repository.insertTransaction(tx(amount = 50.0))
        val result = repository.allTransactions.first()
        assertEquals(1, result.size)
        assertEquals(50.0, result[0].amount, 0.0001)
    }

    @Test
    fun `inserting multiple transactions all appear in flow`() = runTest {
        repository.insertTransaction(tx(amount = 10.0))
        repository.insertTransaction(tx(amount = 20.0))
        repository.insertTransaction(tx(amount = 30.0))
        assertEquals(3, repository.allTransactions.first().size)
    }

    @Test
    fun `insertTransaction preserves category and type`() = runTest {
        repository.insertTransaction(tx(category = "Rent", type = TransactionType.EXPENSE))
        val result = repository.allTransactions.first().first()
        assertEquals("Rent", result.category)
        assertEquals(TransactionType.EXPENSE, result.type)
    }

    @Test
    fun `insertTransaction preserves income type`() = runTest {
        repository.insertTransaction(tx(type = TransactionType.INCOME))
        val result = repository.allTransactions.first().first()
        assertEquals(TransactionType.INCOME, result.type)
    }

    // --- delete ---

    @Test
    fun `deleteTransaction removes it from flow`() = runTest {
        repository.insertTransaction(tx(amount = 99.0))
        val inserted = repository.allTransactions.first().first()
        repository.deleteTransaction(inserted)
        assertTrue(repository.allTransactions.first().isEmpty())
    }

    @Test
    fun `deleteTransaction leaves other transactions intact`() = runTest {
        repository.insertTransaction(tx(amount = 10.0))
        repository.insertTransaction(tx(amount = 20.0))
        val toDelete = repository.allTransactions.first().first { it.amount == 10.0 }
        repository.deleteTransaction(toDelete)
        val remaining = repository.allTransactions.first()
        assertEquals(1, remaining.size)
        assertEquals(20.0, remaining[0].amount, 0.0001)
    }

    @Test
    fun `deleteTransaction with unknown id leaves list unchanged`() = runTest {
        repository.insertTransaction(tx(amount = 42.0))
        repository.deleteTransaction(tx(id = 999L, amount = 1.0))
        assertEquals(1, repository.allTransactions.first().size)
    }

    @Test
    fun `flow is empty after inserting and deleting the only transaction`() = runTest {
        repository.insertTransaction(tx(amount = 5.0))
        val t = repository.allTransactions.first().first()
        repository.deleteTransaction(t)
        assertTrue(repository.allTransactions.first().isEmpty())
    }

    // --- budget ---

    @Test
    fun `setBudgetForMonth stores and retrieves budget`() = runTest {
        repository.setBudgetForMonth(MonthBudget("2026-05", 1500.0))
        val result = repository.getBudgetForMonth("2026-05").first()
        assertNotNull(result)
        assertEquals(1500.0, result!!.budgetAmount, 0.0001)
    }

    @Test
    fun `getBudgetForMonth returns null for unknown month`() = runTest {
        val result = repository.getBudgetForMonth("2099-01").first()
        assertNull(result)
    }

    @Test
    fun `setBudgetForMonth overwrites budget for the same month`() = runTest {
        repository.setBudgetForMonth(MonthBudget("2026-05", 1000.0))
        repository.setBudgetForMonth(MonthBudget("2026-05", 2500.0))
        val result = repository.getBudgetForMonth("2026-05").first()
        assertEquals(2500.0, result!!.budgetAmount, 0.0001)
    }

    @Test
    fun `budgets for different months are independent`() = runTest {
        repository.setBudgetForMonth(MonthBudget("2026-04", 800.0))
        repository.setBudgetForMonth(MonthBudget("2026-05", 1200.0))
        assertEquals(800.0, repository.getBudgetForMonth("2026-04").first()!!.budgetAmount, 0.0001)
        assertEquals(1200.0, repository.getBudgetForMonth("2026-05").first()!!.budgetAmount, 0.0001)
    }

    // --- processRecurringTransactions ---

    @Test
    fun `processRecurringTransactions inserts one monthly transaction for current month`() = runTest {
        // startDate = 1st of this month, dayOfMonth = 1
        // The 1st has always passed by today, so exactly 1 occurrence is due.
        fakeDao.insertRecurringTransaction(monthlyRecurring(dayOfMonth = 1, startDate = firstOfCurrentMonth()))
        repository.processRecurringTransactions()
        assertTrue(repository.allTransactions.first().isNotEmpty())
    }

    @Test
    fun `processRecurringTransactions inserts one weekly transaction for current week`() = runTest {
        // startDate = most recent Monday, dayOfWeek = 1 (Monday)
        // That Monday has always passed by today, so exactly 1 occurrence is due.
        fakeDao.insertRecurringTransaction(weeklyRecurring(dayOfWeek = 1, startDate = mostRecentMonday()))
        repository.processRecurringTransactions()
        assertTrue(repository.allTransactions.first().isNotEmpty())
    }

    @Test
    fun `processRecurringTransactions updates lastProcessedDate after inserting`() = runTest {
        fakeDao.insertRecurringTransaction(monthlyRecurring(dayOfMonth = 1, startDate = firstOfCurrentMonth()))
        repository.processRecurringTransactions()
        val updated = fakeDao.getAllRecurringTransactionsOnce().first()
        assertNotNull(updated.lastProcessedDate)
    }

    @Test
    fun `processRecurringTransactions does not re-insert already processed transactions`() = runTest {
        // lastProcessedDate = today → fromCal is tomorrow → nothing is due yet
        val todayNoon = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        fakeDao.insertRecurringTransaction(
            monthlyRecurring(dayOfMonth = 1, startDate = firstOfCurrentMonth(), lastProcessedDate = todayNoon)
        )
        repository.processRecurringTransactions()
        assertTrue(repository.allTransactions.first().isEmpty())
    }

    @Test
    fun `processRecurringTransactions inserts correct amount`() = runTest {
        fakeDao.insertRecurringTransaction(
            monthlyRecurring(dayOfMonth = 1, startDate = firstOfCurrentMonth(), amount = 250.0)
        )
        repository.processRecurringTransactions()
        val transactions = repository.allTransactions.first()
        assertTrue(transactions.isNotEmpty())
        assertEquals(250.0, transactions.first().amount, 0.0001)
    }

    @Test
    fun `processRecurringTransactions skips future-only recurring`() = runTest {
        // startDate = 1st of next month → nothing is due yet
        val nextMonthFirst = Calendar.getInstance().apply {
            add(Calendar.MONTH, 1)
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        fakeDao.insertRecurringTransaction(monthlyRecurring(dayOfMonth = 1, startDate = nextMonthFirst))
        repository.processRecurringTransactions()
        assertTrue(repository.allTransactions.first().isEmpty())
    }

    // --- helpers ---

    private fun tx(
        id: Long = 0L,
        amount: Double = 100.0,
        category: String = "Test",
        type: TransactionType = TransactionType.EXPENSE
    ) = Transaction(
        id = id, amount = amount, category = category,
        date = System.currentTimeMillis(), note = "", type = type
    )

    private fun monthlyRecurring(
        dayOfMonth: Int,
        startDate: Long,
        amount: Double = 50.0,
        lastProcessedDate: Long? = null,
        endDate: Long? = null
    ) = RecurringTransaction(
        amount = amount, label = "Test", category = "Others",
        frequency = Frequency.MONTHLY, type = TransactionType.EXPENSE,
        dayOfMonth = dayOfMonth, startDate = startDate,
        lastProcessedDate = lastProcessedDate, endDate = endDate
    )

    private fun weeklyRecurring(dayOfWeek: Int, startDate: Long) = RecurringTransaction(
        amount = 50.0, label = "Test", category = "Others",
        frequency = Frequency.WEEKLY, type = TransactionType.EXPENSE,
        dayOfWeek = dayOfWeek, startDate = startDate
    )

    private fun firstOfCurrentMonth(): Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun mostRecentMonday(): Long = Calendar.getInstance().apply {
        while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) add(Calendar.DAY_OF_MONTH, -1)
        set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

// ─── Fakes ────────────────────────────────────────────────────────────────────

class FakeTransactionDao : TransactionDao {

    private val transactions = mutableListOf<Transaction>()
    private val recurringTransactions = mutableListOf<RecurringTransaction>()
    private val budgets = mutableMapOf<String, MonthBudget>()
    private var nextId = 1L

    private val _txFlow = MutableStateFlow<List<Transaction>>(emptyList())
    private val _recurringFlow = MutableStateFlow<List<RecurringTransaction>>(emptyList())

    override fun getAllTransactions(): Flow<List<Transaction>> = _txFlow

    override suspend fun insertTransaction(transaction: Transaction) {
        val id = if (transaction.id == 0L) nextId++ else transaction.id
        transactions.add(transaction.copy(id = id))
        _txFlow.value = transactions.sortedByDescending { it.date }
    }

    override suspend fun deleteTransaction(transaction: Transaction) {
        transactions.removeAll { it.id == transaction.id }
        _txFlow.value = transactions.sortedByDescending { it.date }
    }

    override fun getAllRecurringTransactions(): Flow<List<RecurringTransaction>> = _recurringFlow

    override suspend fun getAllRecurringTransactionsOnce(): List<RecurringTransaction> =
        recurringTransactions.toList()

    override suspend fun insertRecurringTransaction(recurringTransaction: RecurringTransaction) {
        val id = if (recurringTransaction.id == 0L) nextId++ else recurringTransaction.id
        recurringTransactions.add(recurringTransaction.copy(id = id))
        _recurringFlow.value = recurringTransactions.toList()
    }

    override suspend fun deleteRecurringTransaction(recurringTransaction: RecurringTransaction) {
        recurringTransactions.removeAll { it.id == recurringTransaction.id }
        _recurringFlow.value = recurringTransactions.toList()
    }

    override suspend fun updateRecurringLastProcessed(id: Long, date: Long) {
        val idx = recurringTransactions.indexOfFirst { it.id == id }
        if (idx >= 0) {
            recurringTransactions[idx] = recurringTransactions[idx].copy(lastProcessedDate = date)
            _recurringFlow.value = recurringTransactions.toList()
        }
    }

    override fun getBudgetForMonth(monthId: String): Flow<MonthBudget?> =
        MutableStateFlow(budgets[monthId])

    override suspend fun setBudgetForMonth(monthBudget: MonthBudget) {
        budgets[monthBudget.monthId] = monthBudget
    }
}

class FakeExchangeRateService : ExchangeRateService {
    override suspend fun getLatestRates(base: String) = ExchangeRateResponse(
        amount = 1.0, base = base, date = "2026-01-01",
        rates = mapOf("USD" to 1.08, "GBP" to 0.85, "RON" to 4.97)
    )
}
