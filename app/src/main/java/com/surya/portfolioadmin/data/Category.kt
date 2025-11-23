package com.surya.portfolioadmin.data

import java.util.Date

data class Category(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "",
    val gridSize: String = "medium",
    // --- ADD THIS LINE ---
    val order: Any? = 0, // Temporarily accept any type
    val createdAt: Date = Date()
)
{
    fun getOrderAsInt(): Int {
        return when (order) {
            is Number -> (order as Number).toInt()
            is String -> order.toIntOrNull() ?: 0
            else -> 0
        }
    }
}