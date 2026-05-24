package com.example.budgetwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.budgetwise.data.BudgetRepository
import com.example.budgetwise.data.local.AppDatabase
import com.example.budgetwise.data.remote.ExchangeRateService
import com.example.budgetwise.ui.BudgetViewModelFactory
import com.example.budgetwise.ui.Screen
import com.example.budgetwise.ui.add.AddTransactionScreen
import com.example.budgetwise.ui.dashboard.DashboardScreen
import com.example.budgetwise.ui.history.HistoryScreen
import com.example.budgetwise.ui.recurring.RecurringScreen
import com.example.budgetwise.ui.settings.SettingsScreen
import com.example.budgetwise.ui.theme.BudgetWiseTheme
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val database = AppDatabase.getDatabase(this)
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.frankfurter.app/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val exchangeRateService = retrofit.create(ExchangeRateService::class.java)
        val repository = BudgetRepository(database.transactionDao(), exchangeRateService)
        val factory = BudgetViewModelFactory(repository)

        setContent {
            BudgetWiseTheme {
                MainApp(factory)
            }
        }
    }
}

@Composable
fun MainApp(factory: BudgetViewModelFactory) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        Triple(Screen.Dashboard, "Home", Icons.Default.Home),
        Triple(Screen.History, "History", Icons.Default.History),
        Triple(Screen.Recurring, "Recurring", Icons.Default.Repeat),
        Triple(Screen.Settings, "Settings", Icons.Default.Settings)
    )

    Scaffold(
        bottomBar = {
            if (currentDestination?.route != Screen.AddTransaction.route) {
                NavigationBar {
                    items.forEach { (screen, label, icon) ->
                        NavigationBarItem(
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.Dashboard.route,
            Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel(factory = factory),
                    onAddTransaction = { navController.navigate(Screen.AddTransaction.route) }
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(viewModel = viewModel(factory = factory))
            }
            composable(Screen.Recurring.route) {
                RecurringScreen(viewModel = viewModel(factory = factory))
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel(factory = factory))
            }
            composable(Screen.AddTransaction.route) {
                AddTransactionScreen(
                    viewModel = viewModel(factory = factory),
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
