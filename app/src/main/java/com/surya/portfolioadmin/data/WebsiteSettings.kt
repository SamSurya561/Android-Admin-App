package com.surya.portfolioadmin.data

data class WebsiteSettings(
    // --- General ---
    val cvUrl: String = "",
    val heroImageUrl: String = "",
    val aboutImageUrl: String = "",
    val avatarImageUrl: String = "",
    val accentColor: String = "#474af0",

    // --- Background Configuration ---
    val backgroundType: String = "solid", // "solid", "gradient", "image"

    // LIGHT MODE PREFS
    val lightBackgroundColor: String = "#FFFFFF",
    val lightGradientStart: String = "#FFFFFF",
    val lightGradientEnd: String = "#F0F0F0",
    val lightBackgroundImageUrl: String = "",

    // DARK MODE PREFS
    val darkBackgroundColor: String = "#121212",
    val darkGradientStart: String = "#121212",
    val darkGradientEnd: String = "#000000",
    val darkBackgroundImageUrl: String = "",

    // --- Skills ---
    val skills: List<Skill> = emptyList(),

    // Deprecated fields (keeping them to avoid crashes with old data)
    val backgroundImageUrl: String = "",
    val solidBackgroundColor: String = "",
    val gradientStartColor: String = "",
    val gradientEndColor: String = ""
)