package com.surya.portfolioadmin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.surya.portfolioadmin.navigation.AppNavigation
import com.surya.portfolioadmin.ui.theme.PortfolioAdminTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Get SharedPreferences to save user's theme choices
            val prefs = remember { getSharedPreferences("app_theme_prefs", MODE_PRIVATE) }

            // State for the selected color (e.g., Purple, Blue)
            var currentColorKey by remember {
                mutableStateOf(prefs.getString("theme_color", "Purple") ?: "Purple")
            }

            // State for light/dark theme toggle
            var isDarkTheme by remember {
                mutableStateOf(prefs.getBoolean("is_dark_theme", true)) // Default to dark
            }

            PortfolioAdminTheme(
                darkTheme = isDarkTheme,
                colorKey = currentColorKey
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        isDarkTheme = isDarkTheme,
                        onThemeChange = { newColorKey ->
                            prefs.edit().putString("theme_color", newColorKey).apply()
                            currentColorKey = newColorKey
                        },
                        onThemeToggle = {
                            val newThemeState = !isDarkTheme
                            prefs.edit().putBoolean("is_dark_theme", newThemeState).apply()
                            isDarkTheme = newThemeState
                        }
                    )
                }
            }
        }
    }
}
