package com.surya.portfolioadmin.ui.theme

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

@Composable
fun PortfolioAdminTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // We will pass the selected color key as a parameter
    colorKey: String = "Purple",
    content: @Composable () -> Unit
) {
    val colorScheme = when (colorKey) {
        "Orange" -> if (darkTheme) darkColorScheme(primary = OrangePrimaryDark) else lightColorScheme(primary = OrangePrimaryLight)
        "Blue" -> if (darkTheme) darkColorScheme(primary = BluePrimaryDark) else lightColorScheme(primary = BluePrimaryLight)
        "Green" -> if (darkTheme) darkColorScheme(primary = GreenPrimaryDark) else lightColorScheme(primary = GreenPrimaryLight)
        else -> if (darkTheme) darkColorScheme(primary = PurplePrimaryDark) else lightColorScheme(primary = PurplePrimaryLight)
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme.copy(
            background = if (darkTheme) AppBackgroundDark else AppBackgroundLight,
            surface = if (darkTheme) AppSurfaceDark else AppSurfaceLight,
            onBackground = if (darkTheme) AppOnBackgroundDark else AppOnBackgroundLight,
            onSurface = if (darkTheme) AppOnSurfaceDark else AppOnSurfaceLight
        ),
        typography = Typography,
        content = content
    )
}