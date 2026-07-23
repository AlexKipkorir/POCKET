package com.example.pocket.ui.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocket.model.Goal
import com.example.pocket.model.SampleGoals
import com.example.pocket.ui.components.MilestoneRow
import com.example.pocket.ui.screens.goals.components.CircularProgressRing
import com.example.pocket.ui.theme.PocketType
import com.example.pocket.ui.theme.Radius
import com.example.pocket.ui.theme.Secondary
import com.example.pocket.ui.theme.Spacing
import kotlinx.coroutines.launch

/** A single row in the "Recent Contributions" list. */
data class GoalContribution(
    val id: String,
    val title: String,
    val dateLabel: String,
    val amount: Long,
    val icon: ImageVector
)

/** Falls back to a representative sample so the screen still renders meaningfully without wired data. */
private fun sampleContributions(currency: String): List<GoalContribution> = listOf(
    GoalContribution("1", "Monthly Top-up", "Today", 5000, Icons.Filled.AddCircle),
    GoalContribution("2", "Interest Earned", "Jan 15", 420, Icons.AutoMirrored.Filled.TrendingUp),
    GoalContribution("3", "New Year Kickstart", "Jan 1", 12000, Icons.Filled.Savings)
)

private fun formatAbbreviated(amount: Long): String =
    if (amount >= 1000) "${amount / 1000}k" else amount.toString()

@Composable
fun GoalDetailScreen(
    goal: Goal,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onAddContribution: () -> Unit,
    onUpdateTarget: () -> Unit,
    onPauseGoal: () -> Unit,
    onViewAllHistory: () -> Unit = {},
    contributions: List<GoalContribution> = remember(goal.currency) { sampleContributions(goal.currency) }
) {
    val progressColor = when {
        goal.progressPercent >= 75 -> Color(0xFF22C55E)
        goal.progressPercent >= 50 -> Color(0xFFF59E0B)
        else -> com.example.pocket.ui.theme.PrimaryRed
    }

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val milestonesBringIntoView = remember { BringIntoViewRequester() }
    var showOverflowMenu by remember { mutableStateOf(false) }

    // Next 25%-increment milestone, computed from real goal data rather than hardcoded.
    val nextThresholdPercent = (((goal.progressPercent / 25) + 1) * 25).coerceAtMost(100)
    val amountToNextThreshold = ((goal.targetAmount * nextThresholdPercent / 100) - goal.currentAmount)
        .coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f)
                    )
                    .padding(horizontal = Spacing.marginMobile, vertical = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = goal.title,
                    style = PocketType.goalTitleMobile,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = Spacing.xs)
                )
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit goal",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Box {
                    IconButton(onClick = { showOverflowMenu = true }) {
                        Icon(
                            Icons.Filled.MoreVert,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    DropdownMenu(
                        expanded = showOverflowMenu,
                        onDismissRequest = { showOverflowMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Update Target") },
                            leadingIcon = {
                                Icon(Icons.Filled.CalendarToday, contentDescription = null)
                            },
                            onClick = {
                                showOverflowMenu = false
                                onUpdateTarget()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Pause Goal") },
                            leadingIcon = {
                                Icon(Icons.Filled.Pause, contentDescription = null)
                            },
                            onClick = {
                                showOverflowMenu = false
                                onPauseGoal()
                            }
                        )
                    }
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = Spacing.marginMobile)
            ) {
                Spacer(modifier = Modifier.height(Spacing.sm))

                // Status Indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .clip(RoundedCornerShape(50))
                        .background(Secondary.copy(alpha = 0.1f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Secondary)
                        )
                        Text(
                            text = "  Active • On Track",
                            style = PocketType.categoryLabel,
                            color = Secondary
                        )
                    }
                }

                // Progress Hero
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.lg, bottom = Spacing.md)
                ) {
                    CircularProgressRing(
                        progressPercent = goal.progressPercent,
                        progressColor = progressColor
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${goal.progressPercent}%",
                                style = PocketType.progressDisplayMobile,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Achieved",
                                style = PocketType.categoryLabel,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.padding(top = Spacing.md)
                    ) {
                        Text(
                            text = "${goal.currency} ${"%,d".format(goal.currentAmount)}",
                            style = PocketType.goalTitle,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = " of ${goal.currency} ${"%,d".format(goal.targetAmount)}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }

                // Next Milestone CTA
                if (nextThresholdPercent <= 100 && amountToNextThreshold > 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(Radius.xl))
                            .background(com.example.pocket.ui.theme.PrimaryRed.copy(alpha = 0.05f))
                            .border(
                                width = 1.dp,
                                color = com.example.pocket.ui.theme.PrimaryRed.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(Radius.xl)
                            )
                            .clickable {
                                coroutineScope.launch { milestonesBringIntoView.bringIntoView() }
                            }
                            .padding(Spacing.md),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NEXT MILESTONE",
                                style = PocketType.categoryLabel,
                                color = com.example.pocket.ui.theme.PrimaryRed
                            )
                            Text(
                                text = "${goal.currency} ${"%,d".format(amountToNextThreshold)} to reach $nextThresholdPercent%",
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = "View milestones",
                            tint = com.example.pocket.ui.theme.PrimaryRed
                        )
                    }
                }

                // Quick Stats Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.xl, bottom = Spacing.xl)
                ) {
                    QuickStatItem(
                        label = "PROGRESS",
                        value = "${goal.progressPercent}%",
                        modifier = Modifier.weight(1f)
                    )
                    QuickStatItem(
                        label = "SAVED",
                        value = formatAbbreviated(goal.currentAmount.toLong()),
                        modifier = Modifier.weight(1f),
                        showDivider = true
                    )
                    QuickStatItem(
                        label = "TIME LEFT",
                        value = goal.monthsRemaining?.let { "${it}mo" } ?: "—",
                        modifier = Modifier.weight(1f),
                        showDivider = true
                    )
                    QuickStatItem(
                        label = "TARGET",
                        value = formatAbbreviated(goal.targetAmount.toLong()),
                        modifier = Modifier.weight(1f),
                        showDivider = true
                    )
                }

                // Recent Contributions
                Column(modifier = Modifier.padding(bottom = Spacing.xl)) {
                    Text(
                        text = "Recent Contributions",
                        style = PocketType.categoryLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = Spacing.md)
                    )

                    contributions.forEachIndexed { index, contribution ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Spacing.md),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Secondary.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        contribution.icon,
                                        contentDescription = null,
                                        tint = Secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Column(modifier = Modifier.padding(start = Spacing.gutter)) {
                                    Text(
                                        text = contribution.title,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = contribution.dateLabel.uppercase(),
                                        style = PocketType.timeIndicator,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "+${goal.currency} ${"%,d".format(contribution.amount)}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (index != contributions.lastIndex) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f))
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .padding(top = Spacing.xs)
                            .clickable(onClick = onViewAllHistory),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "VIEW ALL HISTORY",
                            style = PocketType.categoryLabel,
                            color = com.example.pocket.ui.theme.PrimaryRed
                        )
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = com.example.pocket.ui.theme.PrimaryRed,
                            modifier = Modifier
                                .size(14.dp)
                                .padding(start = 4.dp)
                        )
                    }
                }

                // Milestones
                Column(
                    modifier = Modifier
                        .padding(bottom = Spacing.xl)
                        .bringIntoViewRequester(milestonesBringIntoView)
                ) {
                    Text(
                        text = "Milestones",
                        style = PocketType.categoryLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = Spacing.md)
                    )
                    goal.milestones.forEachIndexed { index, milestone ->
                        MilestoneRow(
                            milestone = milestone,
                            isLast = index == goal.milestones.lastIndex,
                            modifier = Modifier.padding(
                                bottom = if (index == goal.milestones.lastIndex) 0.dp else Spacing.gutter
                            )
                        )
                    }
                }

                // Space for the fixed bottom button
                Spacer(modifier = Modifier.height(96.dp))
            }
        }

        // Fixed Bottom CTA
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
                .navigationBarsPadding()
                .padding(horizontal = Spacing.marginMobile, vertical = Spacing.sm)
        ) {
            Button(
                onClick = onAddContribution,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(end = Spacing.xs)
                )
                Text(text = "Add Contribution", style = PocketType.goalTitleMobile, fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun QuickStatItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    showDivider: Boolean = false
) {
    Row(modifier = modifier) {
        if (showDivider) {
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(28.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
        ) {
            Text(
                text = label,
                fontSize = 9.sp,
                letterSpacing = 0.8.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Text(
                text = value,
                style = PocketType.goalTitleMobile,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}