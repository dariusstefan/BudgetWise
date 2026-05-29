# 💰 BudgetWise — Android Budget Tracking App
### Android Course — Final Project Presentation (FPP)

---

## 📌 Project Idea

**BudgetWise** is a personal finance tracking application for Android, built with Kotlin and Jetpack Compose. It allows users to log income and expense transactions, organize them by category, and get a live overview of their financial balance. Exchange rates are fetched from a public REST API so users can view their balance in a currency of their choice, with preferences persisted across sessions.

---

## 🎯 Main Functionalities

| # | Feature | Details |
|---|---------|---------|
| 1 | **Add Transactions** | Log income or expense entries with amount, category, date and optional note |
| 2 | **Transaction History** | Scrollable list of all transactions, filterable by type (income / expense) and by month; swipe to delete |
| 3 | **Dashboard Overview** | Summary card showing balance, income, expenses and budget for the selected month |
| 4 | **Month Picker** | Arrow-based month selector on both Dashboard and History screens |
| 5 | **Monthly Budget** | Set a spending budget per month; a segmented progress bar shows spent vs. incoming, turns red above 90% |
| 6 | **Recurring Transactions** | Define recurring income or expenses (salary, rent, etc.) with frequency (daily / weekly / monthly); recurring entries are automatically projected into the transaction history at each due date in the selected month |
| 7 | **Incoming Transactions** | Dashboard section listing upcoming recurring entries due in the current month |
| 8 | **Category Management** | 7 predefined categories with emoji icons (Food, Rent, Salary, Transport, Entertainment, Health, Other) |
| 9 | **Currency Conversion** | Live exchange rates via Frankfurter API; all amounts — balance, income, expenses, budget — are converted and displayed in the selected currency |
| 10 | **Settings Screen** | Choose display currency (EUR / USD / GBP / RON / JPY), toggle dark/light theme, select language; all preferences are persisted via DataStore |
| 11 | **Multi-language Support** | Full UI translation in 5 languages: English, French, Spanish, Romanian, German |
| 12 | **Input Sanitization** | All user-entered text is stripped of HTML/script tags and trimmed before saving |
| 13 | **Local Database** | Transactions, recurring entries and monthly budgets are persisted locally using Room (SQLite) |
| 14 | **Unit Tests** | Test suite covering BudgetRepository, CurrencyUtils, RecurringUtils, InputSanitizer and CategoryEmoji |

---

## 🏗️ Architecture & Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose (bottom nav bar) |
| State | ViewModel + StateFlow / Flow |
| Local DB | Room (SQLite) with KSP |
| Preferences | DataStore (Preferences) |
| Networking | Retrofit 2 + Gson |
| Exchange Rates | [Frankfurter API](https://api.frankfurter.app) |
| Testing | JUnit 4 + kotlinx-coroutines-test |

---

## 🗺️ Screens, Navigation Flow & UI Elements

![Navigation Flow](img/plantuml-budgetwise.png)

### Screen Previews

![BudgetWise Screens](img/screens-preview.png)

### Interactive Mockup

Serve this folder locally:

```bash
npx serve .
```

Then visit `http://localhost:3000`.

---

## 🧪 Unit Tests

Tests are located in `app/src/test/java/com/example/budgetwise/`:

| Test Class | What it covers |
|------------|---------------|
| `BudgetRepositoryTest` | Monthly balance calculation, budget retrieval, recurring projection |
| `CurrencyInfoTest` | Currency symbol lookup, conversion factor application |
| `RecurringUtilsTest` | Weekly multiplier (4×/5×), monthly due-date generation |
| `InputSanitizerTest` | HTML stripping, script tag removal, whitespace trimming |
| `CategoryEmojiTest` | Emoji mapping for all 7 categories |

---

*BudgetWise — Android Course Project · May 2026*
