package com.example.budgetwise.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.budgetwise.ui.i18n.AppLanguage
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesRepository(private val context: Context) {

    companion object {
        val CURRENCY_KEY = stringPreferencesKey("selected_currency")
        val DARK_MODE_KEY = booleanPreferencesKey("is_dark_mode")
        val DEFAULT_BUDGET_KEY = doublePreferencesKey("default_budget")
        val DEFAULT_BUDGET_SET_AT_KEY = androidx.datastore.preferences.core.longPreferencesKey("default_budget_set_at")
        val LANGUAGE_KEY = stringPreferencesKey("selected_language")
    }

    val selectedCurrency: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[CURRENCY_KEY] ?: "EUR" }

    val isDarkMode: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[DARK_MODE_KEY] ?: false }

    val selectedLanguage: Flow<AppLanguage> = context.dataStore.data
        .map { prefs ->
            prefs[LANGUAGE_KEY]?.let { runCatching { AppLanguage.valueOf(it) }.getOrNull() }
                ?: AppLanguage.ENGLISH
        }

    suspend fun setSelectedLanguage(language: AppLanguage) {
        context.dataStore.edit { prefs -> prefs[LANGUAGE_KEY] = language.name }
    }

    suspend fun setSelectedCurrency(currency: String) {
        context.dataStore.edit { prefs -> prefs[CURRENCY_KEY] = currency }
    }

    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[DARK_MODE_KEY] = enabled }
    }

    val defaultBudget: Flow<Double> = context.dataStore.data
        .map { prefs -> prefs[DEFAULT_BUDGET_KEY] ?: 0.0 }

    val defaultBudgetSetAt: Flow<Long> = context.dataStore.data
        .map { prefs -> prefs[DEFAULT_BUDGET_SET_AT_KEY] ?: 0L }

    suspend fun setDefaultBudget(amount: Double) {
        context.dataStore.edit { prefs ->
            prefs[DEFAULT_BUDGET_KEY] = amount
            if ((prefs[DEFAULT_BUDGET_SET_AT_KEY] ?: 0L) == 0L) {
                prefs[DEFAULT_BUDGET_SET_AT_KEY] = System.currentTimeMillis()
            }
        }
    }
}
