package com.example.pocket.ui.screens.goals.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocket.model.Goal
import com.example.pocket.ui.theme.PocketType
import com.example.pocket.ui.theme.PrimaryRed
import com.example.pocket.ui.theme.Secondary
import com.example.pocket.ui.theme.Spacing

@Composable
fun ActiveGoalRow(
    goal: Goal,
    barHeight: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val progressColor = when {
        goal.progressPercent >= 75 -> Color(0xFF22C55E)
        goal.progressPercent >= 50 -> Color(0xFFF59E0B)
        else -> PrimaryRed
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                CategoryLabelWithIcon(goal.category, goal.categoryIcon)
                Text(
                    text = goal.title,
                    style = PocketType.goalTitleMobile,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp
                )
            }
            Text(
                text = "${goal.progressPercent}%",
                style = PocketType.progressDisplayMobile,
                color = progressColor,
                fontSize = 18.sp
            )
        }

        Spacer(Modifier.height(Spacing.base))

        LinearProgressIndicator(
            progress = { goal.progressPercent / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.xs)
                .height(barHeight)
                .clip(RoundedCornerShape(50)),
            color = progressColor,
            trackColor = onSurfaceVariant.copy(alpha = 0.1f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                AmountLine(goal.currentAmount, goal.targetAmount, goal.currency)
                if (goal.nextMilestoneLabel != null) {
                    Text(
                        text = goal.nextMilestoneLabel!!,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRed,
                    )
                } else if (goal.statusLabel != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Secondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = " ${goal.statusLabel}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Secondary,
                        )
                    }
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = goal.monthsRemaining?.let {
                        " $it month${if (it == 1) "" else "s"} remaining"
                    } ?: " No deadline",
                    style = PocketType.timeIndicator,
                    color = onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }
    }
}

@Composable
fun CompletedGoalRow(
    goal: Goal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                CategoryLabelWithIcon(goal.category, goal.categoryIcon)
                Text(
                    text = goal.title,
                    style = PocketType.goalTitleMobile,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontSize = 16.sp
                )
            }
            Text(
                text = "${goal.progressPercent}%",
                style = PocketType.progressDisplayMobile,
                color = Secondary.copy(alpha = 0.5f),
                fontSize = 18.sp
            )
        }

        LinearProgressIndicator(
            progress = { 1f },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.xs)
                .height(4.dp)
                .clip(RoundedCornerShape(50)),
            color = Secondary.copy(alpha = 0.5f),
            trackColor = Secondary.copy(alpha = 0.1f),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "${goal.currency} ${"%,d".format(goal.currentAmount)} SAVED".uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Text(
                text = goal.statusLabel?.uppercase() ?: "COMPLETED",
                style = PocketType.timeIndicator,
                fontWeight = FontWeight.Bold,
                color = Secondary.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun CategoryLabelWithIcon(category: String, iconName: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = iconFor(iconName),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = " $category",
            style = PocketType.categoryLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun AmountLine(current: Int, target: Int, currency: String) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val muted = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    val text = androidx.compose.ui.text.buildAnnotatedString {
        withStyle(androidx.compose.ui.text.SpanStyle(color = onSurface, fontWeight = FontWeight.Bold)) {
            append("$currency ${"%,d".format(current)} / ")
        }
        withStyle(androidx.compose.ui.text.SpanStyle(color = muted, fontWeight = FontWeight.Bold)) {
            append("$currency ${"%,d".format(target)}")
        }
    }
    Text(text = text, fontSize = 14.sp)
}