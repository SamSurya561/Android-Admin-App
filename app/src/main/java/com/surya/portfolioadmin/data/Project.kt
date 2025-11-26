package com.surya.portfolioadmin.data

import java.util.Date

data class Project(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val imageUrl: String = "", // Cover Image
    val images: List<String> = emptyList(), // Gallery Images
    val category: String = "",
    val layout: String = "regular",
    val lastUpdated: Date = Date()
)