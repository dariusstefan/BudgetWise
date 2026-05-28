package com.example.budgetwise.ui.i18n

import androidx.compose.runtime.compositionLocalOf
import java.util.Locale

enum class AppLanguage(val displayName: String) {
    ENGLISH("English"),
    FRENCH("Français"),
    SPANISH("Español"),
    ROMANIAN("Română"),
    GERMAN("Deutsch")
}

interface AppStrings {
    val locale: Locale

    // Shared
    val cancel: String
    val save: String
    val amount: String
    val category: String
    val date: String
    val income: String
    val expenses: String
    val all: String
    val weeklyLabel: String
    val monthlyLabel: String

    // Navigation
    val navHome: String
    val navHistory: String
    val navRecurring: String
    val navSettings: String

    // Dashboard
    val totalBalance: String
    val incomeChip: String
    val expensesChip: String
    val monthlyBudget: String
    val incoming: String
    val left: String
    val incomingTransactionsHeader: String
    val recentTransactionsHeader: String
    val noTransactionsThisMonth: String
    val setBudgetForThisMonth: String
    val viewAllTransactions: String
    val setMonthlyBudget: String
    val due: String
    val savedCheckmark: String

    // History
    val historyTitle: String
    val noTransactionsForPeriod: String
    val noIncomeThisMonth: String
    val noExpensesThisMonth: String
    val incomeCardLabel: String
    val expensesCardLabel: String

    // Add Transaction
    val addTransactionTitle: String
    val expenseToggle: String
    val incomeToggle: String
    val noteOptional: String
    val saveTransaction: String

    // Recurring
    val recurringTitle: String
    val recurringInLabel: String
    val recurringOutLabel: String
    val tapToView: String
    val addNewRecurring: String
    val labelHint: String
    val frequency: String
    val dayOfWeekLabel: String
    val dayOfMonthLabel: String
    val endDate: String
    val addRecurring: String
    val recurringInSheet: String
    val recurringOutSheet: String
    val noRecurringIncome: String
    val noRecurringExpenses: String
    val dayLabels: List<String>

    // Settings
    val settingsTitle: String
    val budgetDescription: String
    val defaultBudget: String
    val displayCurrency: String
    val darkMode: String
    val language: String
    val selectedPrefix: String

    fun percentSpent(pct: Int): String
    fun dayN(n: Int): String
    fun dayInSubtitle(n: Int): String
}

object EnglishStrings : AppStrings {
    override val locale = Locale.ENGLISH
    override val cancel = "Cancel"
    override val save = "Save"
    override val amount = "Amount"
    override val category = "Category"
    override val date = "Date"
    override val income = "Income"
    override val expenses = "Expenses"
    override val all = "All"
    override val weeklyLabel = "Weekly"
    override val monthlyLabel = "Monthly"

    override val navHome = "Home"
    override val navHistory = "History"
    override val navRecurring = "Recurring"
    override val navSettings = "Settings"

    override val totalBalance = "TOTAL BALANCE"
    override val incomeChip = "↑ INCOME"
    override val expensesChip = "↓ EXPENSES"
    override val monthlyBudget = "Monthly Budget"
    override val incoming = "incoming"
    override val left = "left"
    override val incomingTransactionsHeader = "INCOMING TRANSACTIONS"
    override val recentTransactionsHeader = "RECENT TRANSACTIONS"
    override val noTransactionsThisMonth = "No transactions this month"
    override val setBudgetForThisMonth = "Set budget for this month"
    override val viewAllTransactions = "View all transactions →"
    override val setMonthlyBudget = "Set Monthly Budget"
    override val due = "Due"
    override val savedCheckmark = "Saved ✓"

    override val historyTitle = "History"
    override val noTransactionsForPeriod = "No transactions for this period"
    override val noIncomeThisMonth = "No income this month"
    override val noExpensesThisMonth = "No expenses this month"
    override val incomeCardLabel = "INCOME"
    override val expensesCardLabel = "EXPENSES"

    override val addTransactionTitle = "Add Transaction"
    override val expenseToggle = "↓ Expense"
    override val incomeToggle = "↑ Income"
    override val noteOptional = "Note (optional)"
    override val saveTransaction = "Save Transaction"

    override val recurringTitle = "Recurring Transactions"
    override val recurringInLabel = "RECURRING IN"
    override val recurringOutLabel = "RECURRING OUT"
    override val tapToView = "tap to view →"
    override val addNewRecurring = "Add New Recurring"
    override val labelHint = "Label (e.g. Rent)"
    override val frequency = "Frequency"
    override val dayOfWeekLabel = "Day of week"
    override val dayOfMonthLabel = "Day of month"
    override val endDate = "End date"
    override val addRecurring = "Add Recurring"
    override val recurringInSheet = "Recurring In"
    override val recurringOutSheet = "Recurring Out"
    override val noRecurringIncome = "No recurring income yet."
    override val noRecurringExpenses = "No recurring expenses yet."
    override val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    override val settingsTitle = "Settings"
    override val budgetDescription = "Applies to the current month and all future months without a specific override."
    override val defaultBudget = "Default Budget"
    override val displayCurrency = "Display Currency"
    override val darkMode = "Dark Mode"
    override val language = "Language"
    override val selectedPrefix = "Selected: "

    override fun percentSpent(pct: Int) = "$pct% spent"
    override fun dayN(n: Int) = "Day $n"
    override fun dayInSubtitle(n: Int) = "day $n"
}

object FrenchStrings : AppStrings {
    override val locale = Locale.FRENCH
    override val cancel = "Annuler"
    override val save = "Sauvegarder"
    override val amount = "Montant"
    override val category = "Catégorie"
    override val date = "Date"
    override val income = "Revenus"
    override val expenses = "Dépenses"
    override val all = "Tout"
    override val weeklyLabel = "Hebdomadaire"
    override val monthlyLabel = "Mensuel"

    override val navHome = "Accueil"
    override val navHistory = "Historique"
    override val navRecurring = "Récurrents"
    override val navSettings = "Paramètres"

    override val totalBalance = "SOLDE TOTAL"
    override val incomeChip = "↑ REVENUS"
    override val expensesChip = "↓ DÉPENSES"
    override val monthlyBudget = "Budget Mensuel"
    override val incoming = "à venir"
    override val left = "restant"
    override val incomingTransactionsHeader = "TRANSACTIONS À VENIR"
    override val recentTransactionsHeader = "TRANSACTIONS RÉCENTES"
    override val noTransactionsThisMonth = "Aucune transaction ce mois"
    override val setBudgetForThisMonth = "Définir le budget du mois"
    override val viewAllTransactions = "Voir toutes les transactions →"
    override val setMonthlyBudget = "Définir le Budget Mensuel"
    override val due = "Prévu le"
    override val savedCheckmark = "Sauvegardé ✓"

    override val historyTitle = "Historique"
    override val noTransactionsForPeriod = "Aucune transaction pour cette période"
    override val noIncomeThisMonth = "Aucun revenu ce mois"
    override val noExpensesThisMonth = "Aucune dépense ce mois"
    override val incomeCardLabel = "REVENUS"
    override val expensesCardLabel = "DÉPENSES"

    override val addTransactionTitle = "Ajouter une Transaction"
    override val expenseToggle = "↓ Dépense"
    override val incomeToggle = "↑ Revenu"
    override val noteOptional = "Note (facultatif)"
    override val saveTransaction = "Sauvegarder"

    override val recurringTitle = "Transactions Récurrentes"
    override val recurringInLabel = "RÉCURRENTS ENTRANTS"
    override val recurringOutLabel = "RÉCURRENTS SORTANTS"
    override val tapToView = "appuyer pour voir →"
    override val addNewRecurring = "Nouveau Récurrent"
    override val labelHint = "Étiquette (ex. Loyer)"
    override val frequency = "Fréquence"
    override val dayOfWeekLabel = "Jour de la semaine"
    override val dayOfMonthLabel = "Jour du mois"
    override val endDate = "Date de fin"
    override val addRecurring = "Ajouter Récurrent"
    override val recurringInSheet = "Récurrents Entrants"
    override val recurringOutSheet = "Récurrents Sortants"
    override val noRecurringIncome = "Aucun revenu récurrent."
    override val noRecurringExpenses = "Aucune dépense récurrente."
    override val dayLabels = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")

    override val settingsTitle = "Paramètres"
    override val budgetDescription = "S'applique au mois en cours et à tous les mois futurs sans dérogation spécifique."
    override val defaultBudget = "Budget par défaut"
    override val displayCurrency = "Devise"
    override val darkMode = "Mode Sombre"
    override val language = "Langue"
    override val selectedPrefix = "Sélectionné : "

    override fun percentSpent(pct: Int) = "$pct% dépensé"
    override fun dayN(n: Int) = "Jour $n"
    override fun dayInSubtitle(n: Int) = "jour $n"
}

object SpanishStrings : AppStrings {
    override val locale = Locale("es")
    override val cancel = "Cancelar"
    override val save = "Guardar"
    override val amount = "Monto"
    override val category = "Categoría"
    override val date = "Fecha"
    override val income = "Ingresos"
    override val expenses = "Gastos"
    override val all = "Todo"
    override val weeklyLabel = "Semanal"
    override val monthlyLabel = "Mensual"

    override val navHome = "Inicio"
    override val navHistory = "Historial"
    override val navRecurring = "Recurrentes"
    override val navSettings = "Ajustes"

    override val totalBalance = "SALDO TOTAL"
    override val incomeChip = "↑ INGRESOS"
    override val expensesChip = "↓ GASTOS"
    override val monthlyBudget = "Presupuesto Mensual"
    override val incoming = "pendiente"
    override val left = "restante"
    override val incomingTransactionsHeader = "TRANSACCIONES PENDIENTES"
    override val recentTransactionsHeader = "TRANSACCIONES RECIENTES"
    override val noTransactionsThisMonth = "Sin transacciones este mes"
    override val setBudgetForThisMonth = "Establecer presupuesto del mes"
    override val viewAllTransactions = "Ver todas las transacciones →"
    override val setMonthlyBudget = "Establecer Presupuesto Mensual"
    override val due = "Vence el"
    override val savedCheckmark = "Guardado ✓"

    override val historyTitle = "Historial"
    override val noTransactionsForPeriod = "Sin transacciones para este período"
    override val noIncomeThisMonth = "Sin ingresos este mes"
    override val noExpensesThisMonth = "Sin gastos este mes"
    override val incomeCardLabel = "INGRESOS"
    override val expensesCardLabel = "GASTOS"

    override val addTransactionTitle = "Añadir Transacción"
    override val expenseToggle = "↓ Gasto"
    override val incomeToggle = "↑ Ingreso"
    override val noteOptional = "Nota (opcional)"
    override val saveTransaction = "Guardar"

    override val recurringTitle = "Transacciones Recurrentes"
    override val recurringInLabel = "RECURRENTES ENTRADAS"
    override val recurringOutLabel = "RECURRENTES SALIDAS"
    override val tapToView = "toca para ver →"
    override val addNewRecurring = "Nuevo Recurrente"
    override val labelHint = "Etiqueta (ej. Alquiler)"
    override val frequency = "Frecuencia"
    override val dayOfWeekLabel = "Día de la semana"
    override val dayOfMonthLabel = "Día del mes"
    override val endDate = "Fecha de fin"
    override val addRecurring = "Añadir Recurrente"
    override val recurringInSheet = "Recurrentes Entradas"
    override val recurringOutSheet = "Recurrentes Salidas"
    override val noRecurringIncome = "Sin ingresos recurrentes."
    override val noRecurringExpenses = "Sin gastos recurrentes."
    override val dayLabels = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")

    override val settingsTitle = "Ajustes"
    override val budgetDescription = "Se aplica al mes actual y a todos los meses futuros sin una anulación específica."
    override val defaultBudget = "Presupuesto por defecto"
    override val displayCurrency = "Moneda"
    override val darkMode = "Modo Oscuro"
    override val language = "Idioma"
    override val selectedPrefix = "Seleccionado: "

    override fun percentSpent(pct: Int) = "$pct% gastado"
    override fun dayN(n: Int) = "Día $n"
    override fun dayInSubtitle(n: Int) = "día $n"
}

object RomanianStrings : AppStrings {
    override val locale = Locale("ro")
    override val cancel = "Anulare"
    override val save = "Salvare"
    override val amount = "Sumă"
    override val category = "Categorie"
    override val date = "Dată"
    override val income = "Venituri"
    override val expenses = "Cheltuieli"
    override val all = "Toate"
    override val weeklyLabel = "Săptămânal"
    override val monthlyLabel = "Lunar"

    override val navHome = "Acasă"
    override val navHistory = "Istoric"
    override val navRecurring = "Recurente"
    override val navSettings = "Setări"

    override val totalBalance = "SOLD TOTAL"
    override val incomeChip = "↑ VENITURI"
    override val expensesChip = "↓ CHELTUIELI"
    override val monthlyBudget = "Buget Lunar"
    override val incoming = "urmează"
    override val left = "rămas"
    override val incomingTransactionsHeader = "TRANZACȚII VIITOARE"
    override val recentTransactionsHeader = "TRANZACȚII RECENTE"
    override val noTransactionsThisMonth = "Nicio tranzacție luna aceasta"
    override val setBudgetForThisMonth = "Setează buget pentru luna aceasta"
    override val viewAllTransactions = "Vezi toate tranzacțiile →"
    override val setMonthlyBudget = "Setează Buget Lunar"
    override val due = "Scadent pe"
    override val savedCheckmark = "Salvat ✓"

    override val historyTitle = "Istoric"
    override val noTransactionsForPeriod = "Nicio tranzacție pentru această perioadă"
    override val noIncomeThisMonth = "Niciun venit luna aceasta"
    override val noExpensesThisMonth = "Nicio cheltuială luna aceasta"
    override val incomeCardLabel = "VENITURI"
    override val expensesCardLabel = "CHELTUIELI"

    override val addTransactionTitle = "Adaugă Tranzacție"
    override val expenseToggle = "↓ Cheltuială"
    override val incomeToggle = "↑ Venit"
    override val noteOptional = "Notă (opțional)"
    override val saveTransaction = "Salvează"

    override val recurringTitle = "Tranzacții Recurente"
    override val recurringInLabel = "RECURENTE INTRĂRI"
    override val recurringOutLabel = "RECURENTE IEȘIRI"
    override val tapToView = "atinge pentru a vedea →"
    override val addNewRecurring = "Adaugă Recurent"
    override val labelHint = "Etichetă (ex. Chirie)"
    override val frequency = "Frecvență"
    override val dayOfWeekLabel = "Ziua săptămânii"
    override val dayOfMonthLabel = "Ziua lunii"
    override val endDate = "Dată de sfârșit"
    override val addRecurring = "Adaugă Recurent"
    override val recurringInSheet = "Recurente Intrări"
    override val recurringOutSheet = "Recurente Ieșiri"
    override val noRecurringIncome = "Niciun venit recurent."
    override val noRecurringExpenses = "Nicio cheltuială recurentă."
    override val dayLabels = listOf("Lun", "Mar", "Mie", "Joi", "Vin", "Sâm", "Dum")

    override val settingsTitle = "Setări"
    override val budgetDescription = "Se aplică lunii curente și tuturor lunilor viitoare fără o suprascriere specifică."
    override val defaultBudget = "Buget implicit"
    override val displayCurrency = "Monedă afișată"
    override val darkMode = "Mod Întunecat"
    override val language = "Limbă"
    override val selectedPrefix = "Selectat: "

    override fun percentSpent(pct: Int) = "$pct% cheltuit"
    override fun dayN(n: Int) = "Ziua $n"
    override fun dayInSubtitle(n: Int) = "ziua $n"
}

object GermanStrings : AppStrings {
    override val locale = Locale.GERMAN
    override val cancel = "Abbrechen"
    override val save = "Speichern"
    override val amount = "Betrag"
    override val category = "Kategorie"
    override val date = "Datum"
    override val income = "Einnahmen"
    override val expenses = "Ausgaben"
    override val all = "Alle"
    override val weeklyLabel = "Wöchentlich"
    override val monthlyLabel = "Monatlich"

    override val navHome = "Start"
    override val navHistory = "Verlauf"
    override val navRecurring = "Wiederkehrend"
    override val navSettings = "Einstellungen"

    override val totalBalance = "GESAMTSALDO"
    override val incomeChip = "↑ EINNAHMEN"
    override val expensesChip = "↓ AUSGABEN"
    override val monthlyBudget = "Monatsbudget"
    override val incoming = "anstehend"
    override val left = "übrig"
    override val incomingTransactionsHeader = "ANSTEHENDE BUCHUNGEN"
    override val recentTransactionsHeader = "LETZTE BUCHUNGEN"
    override val noTransactionsThisMonth = "Keine Buchungen diesen Monat"
    override val setBudgetForThisMonth = "Budget für diesen Monat festlegen"
    override val viewAllTransactions = "Alle Buchungen anzeigen →"
    override val setMonthlyBudget = "Monatsbudget festlegen"
    override val due = "Fällig am"
    override val savedCheckmark = "Gespeichert ✓"

    override val historyTitle = "Verlauf"
    override val noTransactionsForPeriod = "Keine Buchungen für diesen Zeitraum"
    override val noIncomeThisMonth = "Keine Einnahmen diesen Monat"
    override val noExpensesThisMonth = "Keine Ausgaben diesen Monat"
    override val incomeCardLabel = "EINNAHMEN"
    override val expensesCardLabel = "AUSGABEN"

    override val addTransactionTitle = "Buchung hinzufügen"
    override val expenseToggle = "↓ Ausgabe"
    override val incomeToggle = "↑ Einnahme"
    override val noteOptional = "Notiz (optional)"
    override val saveTransaction = "Speichern"

    override val recurringTitle = "Wiederkehrende Buchungen"
    override val recurringInLabel = "WIEDERKEHREND EINGEHEND"
    override val recurringOutLabel = "WIEDERKEHREND AUSGEHEND"
    override val tapToView = "tippen zum Anzeigen →"
    override val addNewRecurring = "Neues Wiederkehrendes"
    override val labelHint = "Bezeichnung (z.B. Miete)"
    override val frequency = "Häufigkeit"
    override val dayOfWeekLabel = "Wochentag"
    override val dayOfMonthLabel = "Tag des Monats"
    override val endDate = "Enddatum"
    override val addRecurring = "Hinzufügen"
    override val recurringInSheet = "Eingehend Wiederkehrend"
    override val recurringOutSheet = "Ausgehend Wiederkehrend"
    override val noRecurringIncome = "Keine wiederkehrenden Einnahmen."
    override val noRecurringExpenses = "Keine wiederkehrenden Ausgaben."
    override val dayLabels = listOf("Mo", "Di", "Mi", "Do", "Fr", "Sa", "So")

    override val settingsTitle = "Einstellungen"
    override val budgetDescription = "Gilt für den aktuellen Monat und alle zukünftigen Monate ohne spezifische Überschreibung."
    override val defaultBudget = "Standardbudget"
    override val displayCurrency = "Anzeigewährung"
    override val darkMode = "Dunkler Modus"
    override val language = "Sprache"
    override val selectedPrefix = "Ausgewählt: "

    override fun percentSpent(pct: Int) = "$pct% ausgegeben"
    override fun dayN(n: Int) = "Tag $n"
    override fun dayInSubtitle(n: Int) = "Tag $n"
}

fun appStringsFor(language: AppLanguage): AppStrings = when (language) {
    AppLanguage.ENGLISH -> EnglishStrings
    AppLanguage.FRENCH -> FrenchStrings
    AppLanguage.SPANISH -> SpanishStrings
    AppLanguage.ROMANIAN -> RomanianStrings
    AppLanguage.GERMAN -> GermanStrings
}

val LocalAppStrings = compositionLocalOf<AppStrings> { EnglishStrings }
