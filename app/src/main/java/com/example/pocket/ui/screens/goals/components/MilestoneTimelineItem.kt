package com.example.pocket.ui.screens.goals.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.unit.sp
import com.example.pocket.model.DraftMilestone
import com.example.pocket.model.MilestoneState
import com.example.pocket.ui.theme.PocketType
import com.example.pocket.ui.theme.Spacing

@Composable
fun MilestoneTimelineItem(
    milestone: DraftMilestone,
    showConnector: Boolean,
    showDivider: Boolean,
    onEditClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val primary = MaterialTheme.colorScheme.primary
    val dimmed = milestone.state == MilestoneState.UPCOMING || milestone.state == MilestoneState.FINAL

    Row(modifier = modifier.fillMaxWidth()) {
        // Dot + connector column
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(40.dp)) {
            MilestoneDot(milestone.state)
            if (showConnector) {
                Box(
                    modifier = Modifier
                        .padding(top = Spacing.base)
                        .width(2.dp)
                        .height(40.dp)
                        .background(
                            if (milestone.state == MilestoneState.COMPLETED) primary
                            else onSurfaceVariant.copy(alpha = 0.05f),
                        ),
                )
            }
        }

        Column(modifier = Modifier.weight(1f).padding(bottom = Spacing.md)) {
            Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    Text(
                        text = if (milestone.state == MilestoneState.FINAL) "GOAL COMPLETE" else "${milestone.percent}% MILESTONE",
                        style = PocketType.categoryLabel,
                        color = when (milestone.state) {
                            MilestoneState.COMPLETED -> primary
                            MilestoneState.CURRENT -> onSurfaceVariant
                            else -> onSurfaceVariant.copy(alpha = 0.5f)
                        },
                    )
                    Text(
                        text = milestone.amountLabel,
                        style = PocketType.goalTitleMobile,
                        fontSize = 16.sp,
                        color = if (dimmed) onSurface.copy(alpha = 0.5f) else onSurface,
                    )
                }
                when (milestone.state) {
                    MilestoneState.COMPLETED -> Icon(
                        Icons.Filled.DragIndicator,
                        contentDescription = null,
                        tint = onSurfaceVariant.copy(alpha = 0.4f),
                    )
                    MilestoneState.CURRENT, MilestoneState.UPCOMING -> Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit milestone",
                        tint = onSurfaceVariant.copy(alpha = 0.4f),
                    )
                    MilestoneState.FINAL -> {}
                }
            }
            if (milestone.description.isNotEmpty()) {
                Text(
                    text = milestone.description,
                    fontSize = 14.sp,
                    color = if (dimmed) onSurfaceVariant.copy(alpha = 0.5f) else onSurfaceVariant,
                    modifier = Modifier.padding(top = Spacing.base),
                )
            }
            if (showDivider) {
                HorizontalDivider(
                    modifier = Modifier.padding(top = Spacing.md),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                )
            }
        }
    }
}

@Composable
private fun MilestoneDot(state: MilestoneState) {
    val primary = MaterialTheme.colorScheme.primary
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val surface = MaterialTheme.colorScheme.surface

    when (state) {
        MilestoneState.COMPLETED -> Box(
            modifier = Modifier.size(24.dp).clip(CircleShape).background(primary),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(14.dp)
            )
        }
        MilestoneState.CURRENT -> Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(surface)
                .border(2.dp, primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(primary))
        }
        MilestoneState.UPCOMING -> Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(surface)
                .border(2.dp, outlineVariant, CircleShape),
        )
        MilestoneState.FINAL -> Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(surface)
                .border(2.dp, outlineVariant, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = outlineVariant,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}