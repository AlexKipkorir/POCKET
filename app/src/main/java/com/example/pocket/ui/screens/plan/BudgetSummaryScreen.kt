package com.example.pocket.ui.screens.plan

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pocket.model.AllocationStatus
import com.example.pocket.model.BudgetSummary
import com.example.pocket.model.CategoryType
import com.example.pocket.model.InsightType
import com.example.pocket.ui.theme.PocketTheme
import com.example.pocket.ui.theme.PrimaryRed
import com.example.pocket.utils.calculateResponsivePadding
import com.example.pocket.viewmodels.BudgetViewModel
import com.example.pocket.viewmodels.budgetSummary
import kotlin.math.roundToInt

@Composable
fun BudgetSummaryScreen(
    navController: NavController,
    viewModel: BudgetViewModel = viewModel()
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val summary by remember { mutableStateOf(viewModel.budgetSummary) }
    var triggerShare by remember { mutableStateOf(false) }
    val responsivePadding = calculateResponsivePadding()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(
                            horizontal = responsivePadding,
                            vertical = responsivePadding * 0.75f
                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Back button
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        // Title
                        Text(
                            text = "Budget Summary",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp,
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 48.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            // Verdict Section
            item {
                VerdictSection(
                    summary = summary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding)
                )
            }

            // Budget Stats
            item {
                BudgetStatsSection(
                    summary = summary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding)
                )
            }

            // Allocation Overview
            item {
                AllocationOverviewSection(
                    summary = summary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding)
                )
            }

            // Insights
            item {
                InsightsSection(
                    summary = summary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding)
                )
            }

            // Action Buttons
            item {
                ActionButtonsSection(
                    onConfirm = { navController.navigate("dashboard") },
                    onShare = { triggerShare = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding)
                )
            }

            // Bottom Spacer
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }

        // Share effect
        if (triggerShare) {
            LaunchedEffect(Unit) {
                shareBudgetSummary(context, summary)
                triggerShare = false
            }
        }
    }
}

@Composable
fun VerdictSection(
    summary: BudgetSummary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Check icon in circle
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    color = PrimaryRed.copy(alpha = 0.05f),
                    shape = CircleShape
                )
                .border(
                    width = 2.dp,
                    color = PrimaryRed.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = "Verdict",
                tint = PrimaryRed,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Verdict title
        Text(
            text = "BUDGET VERDICT",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = PrimaryRed,
            fontSize = 10.sp,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Main verdict message
        val verdictMessage = when (summary.allocationStatus) {
            AllocationStatus.BALANCED -> "Your budget is balanced"
            AllocationStatus.UNDER_ALLOCATED -> "Budget partially allocated"
            AllocationStatus.OVER_ALLOCATED -> "Budget allocation exceeded"
        }

        Text(
            text = verdictMessage,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 28.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Subtitle
        Text(
            text = "You've planned every shilling with intention",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun BudgetStatsSection(
    summary: BudgetSummary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Total Planned
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total Planned",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            Text(
                text = "Ksh ${formatAmount(summary.totalPlanned)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp
            )
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        )

        // Allocated
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Allocated",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            Text(
                text = "Ksh ${formatAmount(summary.totalAllocated)}",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp
            )
        }

        // Divider
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        )

        // Remaining
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Remaining",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Ksh ${formatAmount(summary.remainingAmount)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp
                )
                Text(
                    text = if (summary.remainingAmount == 0.0) "FULLY ALLOCATED" else
                        if (summary.remainingAmount > 0) "UNDER ALLOCATED" else "OVER ALLOCATED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun AllocationOverviewSection(
    summary: BudgetSummary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = "Allocation Overview",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 18.sp
            )
            Text(
                text = "Recommended ranges help you stay financially healthy",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                modifier = Modifier.weight(1f, fill = false),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }

        // Progress bars for each category type
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            summary.categoryBreakdown.forEach { breakdown ->
                val categoryName = when (breakdown.categoryType) {
                    CategoryType.LIVING_EXPENSES -> "Living Essentials"
                    CategoryType.SAVINGS_GOALS -> "Savings & Debt"
                    CategoryType.OTHER -> "Lifestyle & Fun"
                }

                val isInRecommendedRange = breakdown.percentage in breakdown.recommendedRange
                val textColor = if (breakdown.categoryType == CategoryType.LIVING_EXPENSES) {
                    MaterialTheme.colorScheme.onBackground
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (breakdown.categoryType == CategoryType.LIVING_EXPENSES) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Medium
                            },
                            color = textColor,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${breakdown.percentage.roundToInt()}%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (breakdown.categoryType == CategoryType.LIVING_EXPENSES) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Medium
                            },
                            color = textColor,
                            fontSize = 14.sp
                        )
                    }

                    // Progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth((breakdown.percentage / 100f).toFloat())
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    when (breakdown.categoryType) {
                                        CategoryType.LIVING_EXPENSES -> {
                                            PrimaryRed
                                        }
                                        CategoryType.SAVINGS_GOALS -> {
                                            PrimaryRed.copy(alpha = 0.4f)
                                        }
                                        else -> {
                                            PrimaryRed.copy(alpha = 0.2f)
                                        }
                                    }
                                )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InsightsSection(
    summary: BudgetSummary,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Insights",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 18.sp
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            summary.insights.forEach { insight ->
                InsightCard(insight = insight)
            }
        }
    }
}

@Composable
fun InsightCard(
    insight: com.example.pocket.model.Insight,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Badge
            val badgeColor = when (insight.type) {
                InsightType.STRENGTH -> Color(0xFF10B981) // Emerald
                InsightType.SUGGESTION -> PrimaryRed
                InsightType.WARNING -> Color(0xFFF59E0B) // Amber
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(badgeColor.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = insight.title.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = badgeColor,
                    fontSize = 10.sp,
                    letterSpacing = 1.5.sp
                )
            }

            // Content
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = when (insight.icon) {
                        "verified_user" -> Icons.Filled.VerifiedUser
                        "lightbulb" -> Icons.Filled.Lightbulb
                        else -> Icons.Filled.Lightbulb
                    },
                    contentDescription = insight.type.toString(),
                    tint = badgeColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = insight.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun ActionButtonsSection(
    onConfirm: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Confirm button
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryRed,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            )
        ) {
            Text(
                text = "CONFIRM & START TRACKING",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        // Note
        Text(
            text = "You can edit your budget anytime",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Share button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onShare),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Share,
                contentDescription = "Share",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Share Summary",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
    }
}

// Helper functions
private fun formatAmount(amount: Double): String {
    return when {
        amount in 1000.0..<1000000.0 -> "${(amount / 1000).roundToInt()}k"
        amount >= 1000000 -> "${(amount / 1000000).toInt()}M"
        amount == 0.0 -> "0"
        else -> amount.toInt().toString()
    }
}

private fun shareBudgetSummary(context: Context, summary: BudgetSummary) {
    val shareText = buildString {
        append("📊 Budget Summary - POCKET\n\n")
        append("💰 Total Planned: Ksh ${formatAmount(summary.totalPlanned)}\n")
        append("✅ Allocated: Ksh ${formatAmount(summary.totalAllocated)}\n")
        append("📈 Remaining: Ksh ${formatAmount(summary.remainingAmount)}\n\n")
        append("🔍 Allocation Overview:\n")
        summary.categoryBreakdown.forEach { breakdown ->
            val categoryName = when (breakdown.categoryType) {
                CategoryType.LIVING_EXPENSES -> "Living Essentials"
                CategoryType.SAVINGS_GOALS -> "Savings & Debt"
                CategoryType.OTHER -> "Lifestyle & Fun"
            }
            append("• $categoryName: ${breakdown.percentage.roundToInt()}%\n")
        }
        append("\n💡 Insights:\n")
        summary.insights.forEach { insight ->
            append("• ${insight.message}\n")
        }
        append("\nGenerated by POCKET App")
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, shareText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share Budget Summary")
    context.startActivity(shareIntent)
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun BudgetSummaryScreenPreview() {
    PocketTheme {
        BudgetSummaryScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun BudgetSummaryScreenPreviewDark() {
    PocketTheme(darkTheme = true) {
        BudgetSummaryScreen(navController = rememberNavController())
    }
}