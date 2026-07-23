package com.example.pocket.ui.screens.goals.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.outlined.Flight
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Verified
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Lookup for Material Symbols name -> ImageVector
 * The model layer stores plain strings (e.g., "flight", "home", "verified")
 */
fun iconFor(name: String): ImageVector = when (name) {
    "flight" -> Icons.Outlined.Flight
    "home" -> Icons.Outlined.Home
    "verified" -> Icons.Outlined.Verified
    else -> Icons.Outlined.Home
}