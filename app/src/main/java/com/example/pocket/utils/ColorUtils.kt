package com.example.pocket.utils

import androidx.compose.ui.graphics.Color

fun getColorForCategory(name: String): Color {
    return when (name.lowercase()) {
        "food" -> Color(0xFFF06292)
        "rent" -> Color(0xFF9575CD)
        "transport" -> Color(0xFF4DB6AC)
        "entertainment" -> Color(0xFFFFB74D)
        "others" -> Color(0xFFA1887F)
        else -> Color.LightGray
    }
}