package com.surya.portfolioadmin.data

// Represents the entire settings document in Firestore
data class WebsiteSettings(
    val backgroundImageUrl: String = "",
    val aboutImageUrl: String = "",
    val cvUrl: String = "",
    val skills: List<Skill> = emptyList()
)