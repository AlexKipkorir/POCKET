package com.example.pocket.ui.screens.goals.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.pocket.ui.theme.Spacing

@Composable
fun StepProgressDashes(
    step: Int,
    total: Int,
    modifier: Modifier = Modifier,
    dashWidth: Dp = 48.dp,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
        repeat(total) { index ->
            Spacer(
                modifier = Modifier
                    .width(dashWidth)
                    .height(4.dp)
                    .background(
                        color = if (index < step) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(50),
                    ),
            )
        }
    }
}