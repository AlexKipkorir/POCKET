package com.example.pocket.utils

import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object ScreenConfig {
    enum class ScreenType {
        Compact, Medium, Expanded
    }

    data class WindowSize(
        val width: Dp,
        val height: Dp,
        val screenType: ScreenType
    )
}

@Composable
fun rememberWindowSize(): ScreenConfig.WindowSize {
    val configuration = LocalConfiguration.current

    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    val screenType = when {
        screenWidth < 600.dp -> ScreenConfig.ScreenType.Compact
        screenWidth < 840.dp -> ScreenConfig.ScreenType.Medium
        else -> ScreenConfig.ScreenType.Expanded
    }

    return ScreenConfig.WindowSize(screenWidth, screenHeight, screenType)
}

@Composable
fun calculateResponsivePadding(): Dp {
    val windowSize = rememberWindowSize()
    return when (windowSize.screenType) {
        ScreenConfig.ScreenType.Compact -> 16.dp
        ScreenConfig.ScreenType.Medium -> 24.dp
        ScreenConfig.ScreenType.Expanded -> 32.dp
    }
}

@Composable
fun calculateCardWidth(): Dp {
    val windowSize = rememberWindowSize()
    return when (windowSize.screenType) {
        ScreenConfig.ScreenType.Compact -> 340.dp
        ScreenConfig.ScreenType.Medium -> 400.dp
        ScreenConfig.ScreenType.Expanded -> 480.dp
    }
}

@Composable
fun responsiveCardModifier(): Modifier {
    val cardWidth = calculateCardWidth()
    return Modifier.widthIn(max = cardWidth)
}