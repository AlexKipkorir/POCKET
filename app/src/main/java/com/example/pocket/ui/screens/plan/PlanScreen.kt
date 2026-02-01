package com.example.pocket.ui.screens.plan

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
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

data class PlanningTool(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconColor: Color = PrimaryRed,
    val backgroundColor: Color = PrimaryRed.copy(alpha = 0.05f),
    val badgeCount: Int = 0,
    val badgeColor: Color = PrimaryRed,
    val route: String
)

data class InsightCard(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color = PrimaryRed
)

data class StatCard(
    val id: String,
    val title: String,
    val amount: String,
    val backgroundColor: Color,
    val textColor: Color,
    val isPrimary: Boolean = false
)

@Composable
fun PlanScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    var totalSpent by remember { mutableDoubleStateOf(2200.0) }
    var totalPlanned by remember { mutableDoubleStateOf(3500.0) }
    var budgetUtilization by remember { mutableDoubleStateOf(63.0) }
    var isLoading by remember { mutableStateOf(true) }
    var currentInsightIndex by remember { mutableIntStateOf(0) }

    val responsivePadding = calculateResponsivePadding()

    // All possible insights for rotation
    val allInsights = remember {
        listOf(
            InsightCard(
                id = "optimization_tip",
                title = "Optimization Tip",
                description = "You have $${(totalPlanned - totalSpent).toInt()} unallocated. Move this to Savings Goals.",
                icon = Icons.Filled.Lightbulb
            ),
            InsightCard(
                id = "growth_forecast",
                title = "Growth Forecast",
                description = "Current trends suggest your net worth could increase by 4% this quarter.",
                icon = Icons.AutoMirrored.Filled.TrendingUp
            ),
            InsightCard(
                id = "savings_alert",
                title = "Savings Alert",
                description = "You're on track to save $${(totalPlanned * 0.2).toInt()} this month. Keep it up!",
                icon = Icons.Filled.Savings
            ),
            InsightCard(
                id = "spending_alert",
                title = "Spending Alert",
                description = "Your dining expenses are 15% above budget. Consider adjusting.",
                icon = Icons.Filled.Warning
            ),
            InsightCard(
                id = "investment_tip",
                title = "Investment Tip",
                description = "Consider investing $${((totalPlanned - totalSpent) * 0.3).toInt()} in low-risk mutual funds.",
                icon = Icons.Filled.AttachMoney
            )
        )
    }

    // Auto-rotate insights every 2 seconds
    LaunchedEffect(Unit) {
        while (true) {
            delay(2000) // 2 seconds
            currentInsightIndex = (currentInsightIndex + 1) % allInsights.size
        }
    }

    // Load plan data
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
            isLoading = false
        } ?: run { isLoading = false }
    }

    val availableAmount = totalPlanned - totalSpent
    val statCards = remember(totalSpent, totalPlanned, availableAmount) {
        listOf(
            StatCard(
                id = "total_spent",
                title = "Total Spent",
                amount = "$${totalSpent.toInt()}",
                backgroundColor = PrimaryRed,
                textColor = Color.White,
                isPrimary = true
            ),
            StatCard(
                id = "total_planned",
                title = "Total Planned",
                amount = "$${totalPlanned.toInt()}",
                backgroundColor = Color(0xFFF1F5F9),
                textColor = Color(0xFF64748B)
            ),
            StatCard(
                id = "available",
                title = "Available",
                amount = "$${availableAmount.toInt()}",
                backgroundColor = Color.White,
                textColor = Color(0xFF0F172A)
            )
        )
    }

    val planningTools = remember {
        listOf(
            PlanningTool(
                id = "bills",
                title = "Bills",
                subtitle = "2 bills due",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                badgeCount = 2,
                route = "bill_planning"
            ),
            PlanningTool(
                id = "budgeting",
                title = "Budgeting",
                subtitle = "80% allocated",
                icon = Icons.Filled.Wallet,
                route = "budgeting"
            ),
            PlanningTool(
                id = "summary",
                title = "Summary",
                subtitle = "Monthly view",
                icon = Icons.Filled.PieChart,
                route = "monthly_summary"
            ),
            PlanningTool(
                id = "report",
                title = "Report",
                subtitle = "Monthly review",
                icon = Icons.Filled.Description,
                route = "monthly_report"
            ),
            PlanningTool(
                id = "goals",
                title = "Goals",
                subtitle = "4 active goals",
                icon = Icons.Filled.TrackChanges,
                badgeCount = 4,
                route = "goals"
            ),
            PlanningTool(
                id = "debt",
                title = "Debt",
                subtitle = "Systematic plan",
                icon = Icons.Filled.CardMembership,
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
                        .padding(
                            horizontal = responsivePadding * 1.5f,
                            vertical = responsivePadding
                        )
                ) {
                    // Top Navigation Row - Centered title with notifications on right
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Empty spacer on left to balance the notifications button
                        Spacer(modifier = Modifier.size(40.dp))

                        // Centered title
                        Text(
                            text = "Plan",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 32.sp,
                            lineHeight = 36.sp,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )

                        // Notifications button
                        IconButton(
                            onClick = { navController.navigate("spend_settings") },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // Subtitle
                    Text(
                        text = "Financial planning and budgeting tools",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                }
            }

            // Stats Cards Section (Horizontal Scroll)
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = responsivePadding * 1.5f),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        statCards.forEach { statCard ->
                            StatCardItem(statCard = statCard)
                        }
                    }
                }
            }

            // Budget Utilization Card
            item {
                BudgetUtilizationCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding * 1.5f),
                    spent = totalSpent,
                    planned = totalPlanned,
                    utilization = budgetUtilization
                )
            }

            // Planning Tools Section
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding * 1.5f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Section Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Planning Tools",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp
                        )

                        Text(
                            text = "View All",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRed,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { navController.navigate("planning_tools") }
                        )
                    }

                    // Tools Grid (2 columns)
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
                        .padding(horizontal = responsivePadding * 1.5f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Insights",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 18.sp
                        )

                        // Insight counter indicator
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Dots indicator
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

                    // Single auto-rotating insight card
                    InsightCardItem(
                        insight = allInsights[currentInsightIndex],
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
                .padding(end = responsivePadding * 1.5f, bottom = responsivePadding * 3)
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
                        .size(64.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    PrimaryRed,
                                    PrimaryRed.copy(alpha = 0.8f)
                                )
                            )
                        ),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Create Plan",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Create",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatCardItem(
    statCard: StatCard,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(140.dp)
            .height(90.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = statCard.backgroundColor,
            contentColor = statCard.textColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (statCard.isPrimary) 4.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = statCard.title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = statCard.textColor.copy(alpha = if (statCard.isPrimary) 0.8f else 1f),
                fontSize = 10.sp,
                letterSpacing = 0.5.sp
            )
            Text(
                text = statCard.amount,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = statCard.textColor,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
fun BudgetUtilizationCard(
    modifier: Modifier = Modifier,
    spent: Double,
    planned: Double,
    utilization: Double
) {
    val isOnTrack = utilization <= 85.0

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Budget Utilization",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
                Text(
                    text = "${utilization.toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryRed,
                    fontSize = 14.sp
                )
            }

            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(utilization.toFloat() / 100f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(PrimaryRed)
                )
            }

            // Footer with status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$${spent.toInt()} of $${planned.toInt()} used",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )

                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(
                            if (isOnTrack) PrimaryRed.copy(alpha = 0.05f)
                            else Color(0xFFF59E0B).copy(alpha = 0.05f)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isOnTrack) "ON TRACK" else "OVERSPENT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isOnTrack) PrimaryRed else Color(0xFFF59E0B),
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                }
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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Two rows of 3 items each
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // First row: first 3 items
            tools.take(3).forEach { tool ->
                PlanningToolCard(
                    tool = tool,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(tool.route) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Second row: last 3 items
            tools.drop(3).forEach { tool ->
                PlanningToolCard(
                    tool = tool,
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate(tool.route) }
                )
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
        modifier = modifier
            .clickable(onClick = onClick),
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon with background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
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

            // Title and subtitle
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = tool.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp
                )

                // Subtitle with badge if applicable
                if (tool.badgeCount > 0) {
                    Text(
                        text = "${tool.badgeCount} ${tool.subtitle}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = tool.badgeColor,
                        fontSize = 11.sp
                    )
                } else {
                    Text(
                        text = tool.subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun InsightCardItem(
    insight: InsightCard,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Icon(
                imageVector = insight.icon,
                contentDescription = insight.title,
                tint = insight.iconColor,
                modifier = Modifier.size(24.dp)
            )

            // Content
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp
                )
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }
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