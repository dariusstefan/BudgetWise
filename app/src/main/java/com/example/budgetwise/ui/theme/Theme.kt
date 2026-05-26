package com.example.budgetwise.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    secondary = GreenGrey40,
    tertiary = Teal40,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF1B4332),
    secondaryContainer = Color(0xFFD7EDDA),
    onSecondaryContainer = Color(0xFF1B4332),
    tertiaryContainer = Color(0xFFB2DFDB),
    onTertiaryContainer = Color(0xFF004D40)
)

private val DarkColorSchemeUpdated = darkColorScheme(
    primary = Green80,
    secondary = GreenGrey80,
    tertiary = Teal80,
    primaryContainer = Color(0xFF1B4332),
    onPrimaryContainer = Color(0xFFD1FAE5),
    secondaryContainer = Color(0xFF2E5C38),
    onSecondaryContainer = Color(0xFFB7DFBC),
    tertiaryContainer = Color(0xFF00574B),
    onTertiaryContainer = Color(0xFFB2DFDB)
)

@Composable
fun BudgetWiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorSchemeUpdated else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
