package com.surya.portfolioadmin.data

import java.util.Date

data class Category(
    val id: String = "",
    val name: String = "",
    val imageUrl: String = "", // Deprecated: No longer used in UI
    val gridSize: String = "medium",
    val order: Any? = 0,
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