package com.surya.portfolioadmin.data

import java.util.Date

data class Project(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "",
    // This field now holds the ID of the category document
    val category: String = "",
    val layout: String = "regular",
    val lastUpdated: Date = Date()
)