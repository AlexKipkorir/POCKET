package com.example.pocket.ui.screens.spend

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
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
import kotlinx.coroutines.tasks.await

data class SpendCardItem(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val iconColor: Color,
    val backgroundColor: Color,
    val badgeCount: Int = 0,
    val onClick: () -> Unit
)

data class ChipItem(
    val id: String,
    val label: String,
    val isSelected: Boolean = false,
    val onClick: () -> Unit
)

data class RecentSpendActivity(
    val id: String,
    val title: String,
    val time: String,
    val amount: Double,
    val icon: @Composable () -> Unit,
    val iconColor: Color,
    val backgroundColor: Color,
    val category: String
)

enum class FilterPeriod {
    DAILY, WEEKLY, MONTHLY, YEARLY
}

@Composable
fun SpendScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    var monthlySpend by remember { mutableDoubleStateOf(74000.0) }
    var dailyAverage by remember { mutableDoubleStateOf(2100.0) }
    var budgetRemainingPercent by remember { mutableDoubleStateOf(15.0) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedFilterPeriod by remember { mutableIntStateOf(2) } // 0: Daily, 1: Weekly, 2: Monthly, 3: Yearly

    val responsivePadding = calculateResponsivePadding()

    // Load spend data
    LaunchedEffect(user?.uid) {
        user?.let {
            try {
                val doc = firestore.collection("users").document(it.uid).get().await()
                if (doc.exists()) {
                    monthlySpend = doc.getDouble("monthlySpend") ?: 74000.0
                    dailyAverage = doc.getDouble("dailyAverage") ?: 2100.0
                    budgetRemainingPercent = doc.getDouble("budgetRemainingPercent") ?: 15.0
                }
            } catch (e: Exception) {
                // Use default values
            }
            isLoading = false
        } ?: run { isLoading = false }
    }

    val spendCards = remember {
        listOf(
            SpendCardItem(
                id = "add_expense",
                title = "Add Expense",
                icon = Icons.Filled.ReceiptLong,
                iconColor = PrimaryRed,
                backgroundColor = PrimaryRed.copy(alpha = 0.1f),
                onClick = { navController.navigate("add_expense") }
            ),
            SpendCardItem(
                id = "history",
                title = "History",
                icon = Icons.Filled.History,
                iconColor = Color.Gray,
                backgroundColor = Color.Gray.copy(alpha = 0.1f),
                onClick = { navController.navigate("history") }
            ),
            SpendCardItem(
                id = "bills",
                title = "Bills",
                icon = Icons.Filled.CalendarToday,
                iconColor = Color(0xFFF59E0B), // Warning color
                backgroundColor = Color(0xFFF59E0B).copy(alpha = 0.1f),
                badgeCount = 2,
                onClick = { navController.navigate("bill_reminders") }
            ),
            SpendCardItem(
                id = "investments",
                title = "Investments",
                subtitle = "Money invested",
                icon = Icons.Filled.TrendingUp,
                iconColor = Color(0xFF22C55E), // Success color
                backgroundColor = Color(0xFF22C55E).copy(alpha = 0.1f),
                onClick = { navController.navigate("investment_tracking") }
            )
        )
    }

    val filterPeriods = remember {
        listOf(
            ChipItem(
                id = "daily",
                label = "Today",
                isSelected = selectedFilterPeriod == 0,
                onClick = { selectedFilterPeriod = 0 }
            ),
            ChipItem(
                id = "weekly",
                label = "This Week",
                isSelected = selectedFilterPeriod == 1,
                onClick = { selectedFilterPeriod = 1 }
            ),
            ChipItem(
                id = "monthly",
                label = "This Month",
                isSelected = selectedFilterPeriod == 2,
                onClick = { selectedFilterPeriod = 2 }
            ),
            ChipItem(
                id = "yearly",
                label = "This Year",
                isSelected = selectedFilterPeriod == 3,
                onClick = { selectedFilterPeriod = 3 }
            )
        )
    }

    val statsChips = remember {
        listOf(
            ChipItem(
                id = "monthly",
                label = "KES ${(monthlySpend / 1000).toInt()}k spent",
                isSelected = true,
                onClick = { }
            ),
            ChipItem(
                id = "daily",
                label = "KES ${(dailyAverage).toInt()}/day",
                onClick = { }
            ),
            ChipItem(
                id = "budget",
                label = "${budgetRemainingPercent.toInt()}% budget left",
                onClick = { }
            )
        )
    }

    val recentActivities = remember {
        listOf(
            RecentSpendActivity(
                id = "1",
                title = "Coffee Shop",
                time = "Today, 9:41 AM",
                amount = 450.0,
                icon = {
                    Icon(
                        Icons.Filled.Coffee,
                        contentDescription = "Coffee",
                        modifier = Modifier.size(24.dp)
                    )
                },
                iconColor = Color(0xFFF59E0B), // Warning color
                backgroundColor = Color(0xFFF59E0B).copy(alpha = 0.1f),
                category = "Food & Drink"
            ),
            RecentSpendActivity(
                id = "2",
                title = "Internet Bill",
                time = "Yesterday, 3:20 PM",
                amount = 5000.0,
                icon = {
                    Icon(
                        Icons.Filled.Wifi,
                        contentDescription = "Wifi",
                        modifier = Modifier.size(24.dp)
                    )
                },
                iconColor = PrimaryRed,
                backgroundColor = PrimaryRed.copy(alpha = 0.1f),
                category = "Utilities"
            ),
            RecentSpendActivity(
                id = "3",
                title = "Supermarket",
                time = "Nov 22, 11:15 AM",
                amount = 2300.0,
                icon = {
                    Icon(
                        Icons.Filled.ShoppingBasket,
                        contentDescription = "Shopping",
                        modifier = Modifier.size(24.dp)
                    )
                },
                iconColor = Color(0xFF22C55E), // Success color
                backgroundColor = Color(0xFF22C55E).copy(alpha = 0.1f),
                category = "Groceries"
            ),
            RecentSpendActivity(
                id = "4",
                title = "Netflix Subscription",
                time = "Nov 21, 8:30 AM",
                amount = 1200.0,
                icon = {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Entertainment",
                        modifier = Modifier.size(24.dp)
                    )
                },
                iconColor = Color(0xFF8B5CF6), // Purple
                backgroundColor = Color(0xFF8B5CF6).copy(alpha = 0.1f),
                category = "Entertainment"
            )
        )
    }

    // Filter activities based on selected period
    val filteredActivities = when (selectedFilterPeriod) {
        0 -> recentActivities.filter { it.time.contains("Today") }
        1 -> recentActivities.filter { it.time.contains("Today") || it.time.contains("Yesterday") }
        else -> recentActivities
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                    // Top Navigation Row - Removed back button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Empty space where back button was
                        Spacer(modifier = Modifier.size(40.dp))

                        // Title moved to center
                        Text(
                            text = "Spend",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 32.sp,
                            lineHeight = 36.sp
                        )

                        // Settings button
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
                        text = "Track and understand your expenses",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding * 1.5f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Total Spend
                        Column {
                            Text(
                                text = "Total Spend",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "KES ${monthlySpend.toInt()}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 32.sp
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
                                    .fillMaxWidth((budgetRemainingPercent / 100f).toFloat())
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PrimaryRed)
                            )
                        }

                        // Budget Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Budget used",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(100 - budgetRemainingPercent).toInt()}%",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
            }

            // Filter Chips
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Filter by period",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(horizontal = responsivePadding * 1.5f)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = responsivePadding * 1.5f),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filterPeriods.forEach { chip ->
                            FilterChip(
                                chip = chip,
                                onClick = chip.onClick
                            )
                        }
                    }
                }
            }

            // Quick Action Cards Grid
            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(horizontal = responsivePadding * 1.5f)
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding * 1.5f),
                    horizontalArrangement = Arrangement.spacedBy(responsivePadding)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(responsivePadding)
                    ) {
                        // Add Expense Card
                        SpendCard(
                            cardItem = spendCards[0],
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Bills Card
                        SpendCard(
                            cardItem = spendCards[2],
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(responsivePadding)
                    ) {
                        // History Card
                        SpendCard(
                            cardItem = spendCards[1],
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Investments Card
                        SpendCard(
                            cardItem = spendCards[3],
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Stats Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = responsivePadding * 1.5f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    statsChips.forEach { chip ->
                        StatsChip(chip = chip)
                    }
                }
            }

            // Recent Activity Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding * 1.5f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp
                    )

                    Text(
                        text = "${filteredActivities.size} transactions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }
            }

            // Recent Activity List
            items(filteredActivities) { activity ->
                RecentSpendActivityItem(
                    activity = activity,
                    onClick = { navController.navigate("spend_detail/${activity.id}") },
                    horizontalPadding = responsivePadding
                )
            }

            // Bottom Spacer for FAB
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Enhanced Floating Action Button
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
                    onClick = { navController.navigate("add_expense") },
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.verticalGradient(
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
                            contentDescription = "Add Expense",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "Add",
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
fun SpendCard(
    cardItem: SpendCardItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(onClick = cardItem.onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Badge for bills
                if (cardItem.badgeCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFFF59E0B))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${cardItem.badgeCount} DUE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }

                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(cardItem.backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = cardItem.icon,
                        contentDescription = cardItem.title,
                        tint = cardItem.iconColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Title and Subtitle
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = cardItem.title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 16.sp
                    )

                    cardItem.subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    chip: ChipItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50.dp))
            .background(
                if (chip.isSelected) PrimaryRed
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = chip.label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (chip.isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (chip.isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
fun StatsChip(
    chip: ChipItem,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (chip.isSelected) PrimaryRed.copy(alpha = 0.1f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = chip.onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = chip.label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (chip.isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (chip.isSelected) PrimaryRed else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
fun RecentSpendActivityItem(
    activity: RecentSpendActivity,
    onClick: () -> Unit,
    horizontalPadding: androidx.compose.ui.unit.Dp,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding * 1.5f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(activity.backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    activity.icon()
                }

                // Details
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp
                    )
                    Text(
                        text = activity.time,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    Text(
                        text = activity.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }

            // Amount
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "-KES ${activity.amount.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE11D48), // Expense red color
                    fontSize = 14.sp
                )
                Text(
                    text = "Expense",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }
    }
}

// Preview Screens
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun SpendScreenPreviewPhone() {
    PocketTheme {
        SpendScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun SpendScreenPreviewPhoneDark() {
    PocketTheme(darkTheme = true) {
        SpendScreen(navController = rememberNavController())
    }
}