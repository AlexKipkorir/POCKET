package com.example.pocket.ui.screens.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CardMembership
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pocket.ui.theme.PocketTheme
import com.example.pocket.ui.theme.PrimaryRed
import com.example.pocket.utils.calculateResponsivePadding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

/** Amber used for the "overspent" budget status badge. */
private val WarningAmber = Color(0xFFF59E0B)

/** Neutral hairline border used on cards throughout the Plan screen (design "outline" token). */
private val CardBorder = Color(0xFFE5E7EB)

/** Slate text scale used for secondary copy, matching the design system's slate palette. */
private val Slate800 = Color(0xFF1E293B)
private val Slate500 = Color(0xFF64748B)
private val Slate400 = Color(0xFF94A3B8)
private val Slate200 = Color(0xFFE2E8F0)
private val Slate100 = Color(0xFFF1F5F9)
private val Slate700 = Color(0xFF334155)

data class PlanningTool(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconColor: Color = PrimaryRed,
    val backgroundColor: Color = PrimaryRed.copy(alpha = 0.1f),
    val badgeCount: Int = 0,
    val badgeColor: Color = PrimaryRed,
    val route: String
)

data class InsightCard(
    val id: String,
    val label: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color = PrimaryRed,
    val ctaLabel: String,
    val ctaRoute: String,
    val isHighlighted: Boolean = false
)

@Composable
fun PlanScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    var totalSpent by remember { mutableDoubleStateOf(2200.0) }
    var totalPlanned by remember { mutableDoubleStateOf(3500.0) }
    var budgetUtilization by remember { mutableDoubleStateOf(63.0) }
    var currentInsightIndex by remember { mutableIntStateOf(0) }

    val responsivePadding = calculateResponsivePadding()

    // All possible insights for rotation
    val allInsights = remember {
        listOf(
            InsightCard(
                id = "optimization_tip",
                label = "Top Tip",
                description = "You have $${(totalPlanned - totalSpent).toInt()} in unallocated funds. Consider moving this to your Savings Goals.",
                icon = Icons.Filled.Lightbulb,
                iconColor = PrimaryRed,
                ctaLabel = "Move Funds",
                ctaRoute = "budgeting",
                isHighlighted = true
            ),
            InsightCard(
                id = "growth_forecast",
                label = "Growth Forecast",
                description = "Current trends suggest your net worth could increase by 4% this quarter.",
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                iconColor = Color(0xFF2563EB),
                ctaLabel = "View Details",
                ctaRoute = "monthly_report"
            ),
            InsightCard(
                id = "savings_alert",
                label = "Savings Alert",
                description = "You're on track to save $${(totalPlanned * 0.2).toInt()} this month. Keep it up!",
                icon = Icons.Filled.Savings,
                iconColor = Color(0xFF2563EB),
                ctaLabel = "View Goals",
                ctaRoute = "goals"
            ),
            InsightCard(
                id = "spending_alert",
                label = "Spending Alert",
                description = "Your dining expenses are 15% above budget. Consider adjusting.",
                icon = Icons.Filled.Warning,
                iconColor = Color(0xFF2563EB),
                ctaLabel = "Review Budget",
                ctaRoute = "budgeting"
            ),
            InsightCard(
                id = "investment_tip",
                label = "Investment",
                description = "Consider investing $${((totalPlanned - totalSpent) * 0.3).toInt()} in low-risk mutual funds.",
                icon = Icons.Filled.AttachMoney,
                iconColor = Color(0xFF2563EB),
                ctaLabel = "Explore",
                ctaRoute = "monthly_report"
            )
        )
    }

    // Auto-rotate insights every 2 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000)
            currentInsightIndex = (currentInsightIndex + 1) % allInsights.size
        }
    }

    // Load plan data, falling back to the defaults above if the doc is missing or the fetch fails
    LaunchedEffect(user?.uid) {
        user?.let {
            try {
                val doc = firestore.collection("users").document(it.uid).get().await()
                if (doc.exists()) {
                    totalSpent = doc.getDouble("totalSpent") ?: 2200.0
                    totalPlanned = doc.getDouble("totalPlanned") ?: 3500.0
                    budgetUtilization = doc.getDouble("budgetUtilization") ?: 63.0
                }
            } catch (e: Exception) {
                // Use default values
            }
        }
    }

    val availableAmount = totalPlanned - totalSpent

    val planningTools = remember {
        listOf(
            PlanningTool(
                id = "bills",
                title = "Bill Planning",
                subtitle = "2 due soon",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                iconColor = PrimaryRed,
                backgroundColor = PrimaryRed.copy(alpha = 0.1f),
                badgeCount = 2,
                badgeColor = Color(0xFFF97316), // orange status dot, matches design
                route = "bill_planning"
            ),
            PlanningTool(
                id = "budgeting",
                title = "Budgeting",
                subtitle = "80% allocated",
                icon = Icons.Filled.Wallet,
                iconColor = PrimaryRed,
                backgroundColor = PrimaryRed.copy(alpha = 0.1f),
                badgeCount = 1,
                badgeColor = PrimaryRed,
                route = "budgeting"
            ),
            PlanningTool(
                id = "summary",
                title = "Summary",
                subtitle = "Monthly overview",
                icon = Icons.Filled.PieChart,
                iconColor = Color(0xFF2563EB),
                backgroundColor = Color(0xFF3B82F6).copy(alpha = 0.1f),
                route = "monthly_summary"
            ),
            PlanningTool(
                id = "report",
                title = "Report",
                subtitle = "Financial report",
                icon = Icons.Filled.Description,
                iconColor = Color(0xFF9333EA),
                backgroundColor = Color(0xFFA855F7).copy(alpha = 0.1f),
                route = "monthly_report"
            ),
            PlanningTool(
                id = "goals",
                title = "Goals",
                subtitle = "4 active goals",
                icon = Icons.Filled.TrackChanges,
                iconColor = Color(0xFF059669),
                backgroundColor = Color(0xFF10B981).copy(alpha = 0.1f),
                badgeCount = 4,
                badgeColor = Color(0xFF059669),
                route = "goals"
            ),
            PlanningTool(
                id = "debt",
                title = "Debt",
                subtitle = "Systematic plan",
                icon = Icons.Filled.CardMembership,
                iconColor = Color(0xFF4F46E5),
                backgroundColor = Color(0xFF6366F1).copy(alpha = 0.1f),
                route = "debt_management"
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.05f)
                        )
                        .padding(
                            horizontal = responsivePadding,
                            vertical = responsivePadding
                        )
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { navController.navigate("spend_settings") },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FilterList,
                                contentDescription = "Filter",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }

                        Text(
                            text = "Plan",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp,
                            lineHeight = 24.sp
                        )

                        IconButton(
                            onClick = { navController.navigate("spend_settings") },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.FilterList,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }

            // Stats Cards Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SimpleStatCard(
                        title = "Total Planned",
                        amount = "$${totalPlanned.toInt()}",
                        modifier = Modifier.weight(1f)
                    )
                    SimpleStatCard(
                        title = "Total Spent",
                        amount = "$${totalSpent.toInt()}",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Budget Utilization Card
            item {
                BudgetUtilizationCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding),
                    remaining = availableAmount,
                    utilization = budgetUtilization
                )
            }

            // Planning Tools Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionTitle(text = "Planning Tools")

                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRed,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { navController.navigate("planning_tools") }
                        )
                    }

                    PlanningToolsGrid(
                        tools = planningTools,
                        navController = navController,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Insights Section with auto-rotating card
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionTitle(text = "Planning Tools")

                        // Dots indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            repeat(allInsights.size) { index ->
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (index == currentInsightIndex) PrimaryRed
                                            else PrimaryRed.copy(alpha = 0.3f)
                                        )
                                )
                            }
                        }
                    }

                    InsightCardItem(
                        insight = allInsights[currentInsightIndex],
                        onCtaClick = { route -> navController.navigate(route) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Bottom Spacer for FAB
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Floating Action Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = responsivePadding, bottom = responsivePadding * 3)
        ) {
            Box(
                modifier = Modifier
                    .shadow(
                        elevation = 16.dp,
                        shape = CircleShape,
                        spotColor = PrimaryRed.copy(alpha = 0.5f)
                    )
                    .clip(CircleShape)
            ) {
                IconButton(
                    onClick = { navController.navigate("budgeting") },
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    PrimaryRed,
                                    PrimaryRed.copy(alpha = 0.8f)
                                )
                            )
                        ),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Create Plan",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }
}

/** Bold section heading used above the Planning Tools and Insights lists. */
@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        fontSize = 18.sp,
        modifier = modifier
    )
}

/** Plain white stat card — label + bold amount, matching the design system's "Total Planned / Total Spent" tiles. */
@Composable
private fun SimpleStatCard(
    title: String,
    amount: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Slate800
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Slate500,
                fontSize = 14.sp
            )
            Text(
                text = amount,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Slate800,
                fontSize = 24.sp
            )
        }
    }
}

@Composable
fun BudgetUtilizationCard(
    modifier: Modifier = Modifier,
    remaining: Double,
    utilization: Double
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Slate800
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with percentage badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budget Utilization",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate800,
                    fontSize = 14.sp
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(PrimaryRed.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${utilization.toInt()}%",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRed,
                        fontSize = 12.sp
                    )
                }
            }

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Slate100)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(utilization.toFloat().coerceIn(0f, 100f) / 100f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(PrimaryRed, PrimaryRed.copy(alpha = 0.85f))
                            )
                        )
                )
            }

            // Footer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${remaining.toInt()} REMAINING",
                    fontWeight = FontWeight.Medium,
                    color = Slate500,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "Updated just now",
                    fontStyle = FontStyle.Italic,
                    color = Slate400,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun PlanningToolsGrid(
    tools: List<PlanningTool>,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        tools.chunked(2).forEach { rowTools ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                rowTools.forEach { tool ->
                    PlanningToolCard(
                        tool = tool,
                        modifier = Modifier.weight(1f),
                        onClick = { navController.navigate(tool.route) }
                    )
                }
                // Keep a balanced row if the list has an odd count.
                if (rowTools.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun PlanningToolCard(
    tool: PlanningTool,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Slate800
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon with tinted background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tool.backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.title,
                    tint = tool.iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = tool.title,
                    fontWeight = FontWeight.Bold,
                    color = Slate800,
                    fontSize = 16.sp,
                    lineHeight = 20.sp
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (tool.badgeCount > 0) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(tool.badgeColor)
                        )
                    }
                    Text(
                        text = tool.subtitle,
                        fontWeight = FontWeight.Medium,
                        color = Slate500,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun InsightCardItem(
    insight: InsightCard,
    onCtaClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val highlightColor = if (insight.isHighlighted) PrimaryRed else Slate800
    val cardBackground = if (insight.isHighlighted) PrimaryRed.copy(alpha = 0.05f) else Color.White
    val cardBorderColor = if (insight.isHighlighted) PrimaryRed.copy(alpha = 0.1f) else CardBorder

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackground,
            contentColor = Slate800
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (insight.isHighlighted) 0.dp else 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = insight.icon,
                    contentDescription = insight.label,
                    tint = insight.iconColor,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = insight.label.uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = insight.iconColor,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }

            Text(
                text = highlightAmounts(insight.description, highlightColor),
                color = Slate700,
                fontSize = 13.sp,
                lineHeight = 19.sp
            )

            if (insight.isHighlighted) {
                Button(
                    onClick = { onCtaClick(insight.ctaRoute) },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryRed),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 20.dp,
                        vertical = 10.dp
                    )
                ) {
                    Text(text = insight.ctaLabel, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(Slate100)
                        .border(1.dp, Slate200, RoundedCornerShape(50))
                        .clickable { onCtaClick(insight.ctaRoute) }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = insight.ctaLabel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate700
                    )
                }
            }
        }
    }
}

/** Bolds dollar-amount substrings (e.g. "$450") within [text] using [color], for the insight description copy. */
private fun highlightAmounts(text: String, color: Color): AnnotatedString {
    val regex = Regex("""\$[\d,]+""")
    return buildAnnotatedString {
        var lastIndex = 0
        for (match in regex.findAll(text)) {
            append(text.substring(lastIndex, match.range.first))
            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = color)) {
                append(match.value)
            }
            lastIndex = match.range.last + 1
        }
        append(text.substring(lastIndex))
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PlanScreenPreviewPhone() {
    PocketTheme {
        PlanScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun PlanScreenPreviewPhoneDark() {
    PocketTheme(darkTheme = true) {
        PlanScreen(navController = rememberNavController())
    }
}