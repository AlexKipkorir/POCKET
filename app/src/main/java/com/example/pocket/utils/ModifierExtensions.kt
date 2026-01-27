package com.example.pocket.utils

import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun Modifier.responsiveCardModifier(): Modifier {
    val screenWidthDp = LocalConfiguration.current.screenWidthDp

    return when {
        screenWidthDp < 360 -> this
            .fillMaxWidth()
            .padding(12.dp)

        screenWidthDp < 600 -> this
            .fillMaxWidth()
            .padding(16.dp)

        else -> this
            .widthIn(max = 420.dp)
            .padding(20.dp)
    }
}
