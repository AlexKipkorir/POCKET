package com.example.pocket.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.platform.LocalContext
import com.example.pocket.ui.theme.Typography

private val Crimson = Color(0xFF990000)
private val white = Color(0xFFFFFFFF)

val crimson = Color(0xFFB71C1C) // deep red


private val PocketColorScheme = darkColorScheme(
    primary = Crimson,
    onPrimary = White,
    background = White,
    onBackground = Crimson
)

@Composable
fun PocketTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PocketColorScheme,
        typography = Typography(),
        content = content
    )
}