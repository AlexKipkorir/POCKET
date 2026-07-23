package com.example.pocket.ui.screens.goals

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocket.model.DraftMilestone
import com.example.pocket.model.MilestoneState
import com.example.pocket.model.SampleMilestones
import com.example.pocket.ui.screens.goals.components.MilestoneTimelineItem
import com.example.pocket.ui.theme.PocketType
import com.example.pocket.ui.theme.Radius
import com.example.pocket.ui.theme.Secondary
import com.example.pocket.ui.theme.Spacing

@Composable
fun GoalMilestonesScreen(
    goalTitle: String,
    goalTargetLabel: String,
    projectedCompletionLabel: String,
    monthlyCommitLabel: String,
    milestones: List<DraftMilestone>,
    onClose: () -> Unit,
    onSkip: () -> Unit,
    onCompleteSetup: () -> Unit,
    onAddCustomMilestone: () -> Unit = {},
    heroProgressPercent: Int = 25,
) {
    var animationStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationStarted = true }
    val animatedProgress by animateFloatAsState(
        targetValue = if (animationStarted) heroProgressPercent / 100f else 0f,
        animationSpec = tween(700),
        label = "heroProgress",
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    .padding(horizontal = Spacing.marginMobile),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onClose) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "New Goal",
                    style = PocketType.goalTitleMobile,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(40.dp))
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .padding(horizontal = Spacing.marginMobile, vertical = Spacing.sm),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Secondary.copy(alpha = 0.1f))
                            .padding(Spacing.xs),
                    ) {
                        Icon(
                            Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = Secondary
                        )
                    }
                    Text(
                        text = "Smart-milestones have been generated based on common financial safety patterns.",
                        fontSize = 12.sp,
                        lineHeight = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = Spacing.sm).weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    OutlinedButton(
                        onClick = onSkip,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f).height(56.dp),
                    ) {
                        Text(
                            text = "Skip",
                            style = PocketType.goalTitle,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Button(
                        onClick = onCompleteSetup,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                        ),
                        modifier = Modifier.weight(2f).height(56.dp),
                    ) {
                        Text(
                            text = "Complete Goal Set-up",
                            style = PocketType.goalTitle,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.marginMobile),
        ) {
            // Hero header
            Column(modifier = Modifier.padding(top = Spacing.md, bottom = Spacing.lg)) {
                Text(
                    text = "STEP 3 OF 3",
                    style = PocketType.categoryLabel,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = Spacing.base),
                )
                Text(
                    text = "Define your milestones",
                    style = PocketType.goalTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = Spacing.xs),
                )
                Text(
                    text = "Break down your $goalTargetLabel into manageable wins.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                )
            }

            // Projected completion / monthly commit preview
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(Radius.xl))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), RoundedCornerShape(Radius.xl))
                    .padding(Spacing.md)
                    .padding(bottom = Spacing.lg),
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(bottom = Spacing.sm),
                ) {
                    Column {
                        Text(
                            text = "PROJECTED COMPLETION",
                            style = PocketType.categoryLabel,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = projectedCompletionLabel,
                            style = PocketType.goalTitle,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "MONTHLY COMMIT",
                            style = PocketType.categoryLabel,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = monthlyCommitLabel,
                            style = PocketType.goalTitle,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(50))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                            .height(16.dp)
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.colorScheme.primary),
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.base),
                ) {
                    Text(
                        text = "Progress start",
                        style = PocketType.timeIndicator,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Target Achieved",
                        style = PocketType.timeIndicator,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Milestone timeline
            Column(modifier = Modifier.padding(top = Spacing.md)) {
                milestones.forEachIndexed { index, milestone ->
                    MilestoneTimelineItem(
                        milestone = milestone,
                        showConnector = index != milestones.lastIndex,
                        showDivider = index != milestones.lastIndex,
                    )
                }

                OutlinedButton(
                    onClick = onAddCustomMilestone,
                    shape = RoundedCornerShape(Radius.xl),
                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                ) {
                    Icon(
                        Icons.Filled.AddCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "  ADD CUSTOM MILESTONE",
                        style = PocketType.categoryLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))
        }
    }
}