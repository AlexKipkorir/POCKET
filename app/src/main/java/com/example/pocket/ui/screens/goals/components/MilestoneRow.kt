package com.example.pocket.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pocket.model.Milestone
import com.example.pocket.ui.theme.PocketType
import com.example.pocket.ui.theme.Secondary
import com.example.pocket.ui.theme.Spacing

@Composable
fun MilestoneRow(
    milestone: Milestone,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(
                    if (milestone.isReached) Secondary.copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.surfaceContainerHighest
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (milestone.isReached) Icons.Filled.Check else Icons.Filled.Schedule,
                contentDescription = null,
                tint = if (milestone.isReached) Secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.padding(start = Spacing.md).weight(1f)) {
            Column(modifier = Modifier.padding(bottom = if (isLast) 0.dp else Spacing.base)) {
                Text(
                    text = milestone.label,
                    fontWeight = FontWeight.Bold,
                    color = if (milestone.isReached) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = milestone.dateOrStatus,
                    style = PocketType.timeIndicator,
                    color = if (milestone.isReached)
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.primary,
                    fontWeight = if (milestone.isReached) FontWeight.Normal else FontWeight.Bold
                )
            }
            if (!isLast) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = Spacing.xs),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f)
                )
            }
        }
    }
}