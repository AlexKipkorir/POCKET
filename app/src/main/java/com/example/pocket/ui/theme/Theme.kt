package com.example.pocket.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

//---------------------------------------------------------------
// COLORS
//---------------------------------------------------------------
val PrimaryRed = Color(0xFFDB143C)
val Primary = Color(0xFFDB143C)
val Secondary = Color(0xFF006E2F)
val BackgroundLight = Color(0xFFF8F6F6)
val BackgroundDark = Color(0xFF211114)
val CardDark = Color(0xFF2D1A1D)
val TextLight = Color(0xFF181112)
val TextMuted = Color(0xFF896169)

//---------------------------------------------------------------
// THEME
//---------------------------------------------------------------
@Composable
fun PocketTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        androidx.compose.material3.darkColorScheme(
            primary = PrimaryRed,
            background = BackgroundDark,
            surface = CardDark,
            onBackground = Color.White,
            onSurface = Color.White,
            surfaceVariant = Color(0xFF3A2528)
        )
    } else {
        androidx.compose.material3.lightColorScheme(
            primary = PrimaryRed,
            background = BackgroundLight,
            surface = Color.White,
            onBackground = TextLight,
            onSurface = TextLight,
            surfaceVariant = Color(0xFFF5F0F1)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = Shapes,
        content = content
    )
}