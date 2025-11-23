package com.surya.portfolioadmin.ui.theme


import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color


// --- Purple Theme ---

val PurplePrimaryDark = Color(0xFFB39DDB)

val PurplePrimaryLight = Color(0xFF6200EE)


// --- Orange Theme ---

val OrangePrimaryDark = Color(0xFFFB923C)

val OrangePrimaryLight = Color(0xFFF97316)


// --- Blue Theme ---

val BluePrimaryDark = Color(0xFF90CAF9)

val BluePrimaryLight = Color(0xFF2196F3)


// --- Green Theme ---

val GreenPrimaryDark = Color(0xFFA5D6A7)

val GreenPrimaryLight = Color(0xFF4CAF50)


// --- Universal Colors ---

val AppBackgroundDark = Color(0xFF121212)

val AppSurfaceDark = Color(0xFF1E1E1E)

val AppOnBackgroundDark = Color(0xFFE6E1E5)

val AppOnSurfaceDark = Color(0xFFE6E1E5)


val AppBackgroundLight = Color(0xFFF5F5F5)

val AppSurfaceLight = Color(0xFFFFFFFF)

val AppOnBackgroundLight = Color(0xFF000000)

val AppOnSurfaceLight = Color(0xFF000000)


// Add these new colors at the top with your other color definitions
val BluePrimary = Color(0xFF3B82F6)
val BlueSecondary = Color(0xFF60A5FA)
val GreenPrimary = Color(0xFF10B981)
val GreenSecondary = Color(0xFF34D399)

// Add these new ColorScheme objects at the bottom of the file
val BlueColorScheme = darkColorScheme(
    primary = BluePrimary,
    secondary = BlueSecondary,
    tertiary = BlueSecondary
    /* Other colors can be defined here */
)

val GreenColorScheme = darkColorScheme(
    primary = GreenPrimary,
    secondary = GreenSecondary,
    tertiary = GreenSecondary
    /* Other colors can be defined here */
)


