package com.example.pocket.ui.screens.goals.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocket.ui.theme.PocketType
import com.example.pocket.ui.theme.Spacing
import com.example.pocket.ui.theme.PrimaryRed

/**
 * A single entry in the horizontally scrollable "Quick Stats Strip"
 */
@Composable
fun StatItem(
    label: String,
    value: String,
    valueColor: Color,
    suffix: String? = null,
    minWidth: androidx.compose.ui.unit.Dp = 120.dp,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.base),
        modifier = modifier
            .width(minWidth)
            .padding(vertical = Spacing.xs)
            .padding(end = Spacing.md)
    ) {
        Text(
            text = label,
            style = PocketType.categoryLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = PocketType.progressDisplayMobile,
            color = valueColor
        )
        if (suffix != null) {
            Text(
                text = suffix,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}