package com.example.pocket.ui.screens.spend

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ShoppingBasket
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

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
    val timestamp: Date,
    val amount: Double,
    val icon: @Composable () -> Unit,
    val iconColor: Color,
    val backgroundColor: Color,
    val category: String
)

enum class FilterPeriod {
    TODAY, WEEKLY, MONTHLY, YEARLY, ALL
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
    var selectedFilterPeriod by remember { mutableStateOf(FilterPeriod.MONTHLY) }

    // New state variables for filtered data
    var filteredTotalSpend by remember { mutableDoubleStateOf(monthlySpend) }
    var filteredDailyAverage by remember { mutableDoubleStateOf(dailyAverage) }

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

                    // Initialize filtered data
                    filteredTotalSpend = monthlySpend
                    filteredDailyAverage = dailyAverage
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
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                iconColor = PrimaryRed,
                backgroundColor = PrimaryRed.copy(alpha = 0.1f),
                onClick = { navController.navigate("add_expense") }
            ),
            SpendCardItem(
                id = "history",
                title = "History",
                icon = Icons.Filled.History,
                iconColor = Color(0xFF6B7280), // Gray color
                backgroundColor = Color(0xFF6B7280).copy(alpha = 0.1f),
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
            )
        )
    }

    val filterPeriods = remember(selectedFilterPeriod) {
        listOf(
            ChipItem(
                id = "today",
                label = "Today",
                isSelected = selectedFilterPeriod == FilterPeriod.TODAY,
                onClick = {
                    selectedFilterPeriod = FilterPeriod.TODAY
                    // Update filtered data for today
                    filteredTotalSpend = 4500.0 // Example: sum of today's expenses
                    filteredDailyAverage = 4500.0 // Today's average (same as total)
                }
            ),
            ChipItem(
                id = "weekly",
                label = "This Week",
                isSelected = selectedFilterPeriod == FilterPeriod.WEEKLY,
                onClick = {
                    selectedFilterPeriod = FilterPeriod.WEEKLY
                    // Update filtered data for this week
                    filteredTotalSpend = 18500.0 // Example: sum of this week's expenses
                    filteredDailyAverage = 2642.86 // Weekly average
                }
            ),
            ChipItem(
                id = "monthly",
                label = "This Month",
                isSelected = selectedFilterPeriod == FilterPeriod.MONTHLY,
                onClick = {
                    selectedFilterPeriod = FilterPeriod.MONTHLY
                    // Reset to original monthly data
                    filteredTotalSpend = monthlySpend
                    filteredDailyAverage = dailyAverage
                }
            ),
            ChipItem(
                id = "yearly",
                label = "This Year",
                isSelected = selectedFilterPeriod == FilterPeriod.YEARLY,
                onClick = {
                    selectedFilterPeriod = FilterPeriod.YEARLY
                    // Update filtered data for this year
                    filteredTotalSpend = monthlySpend * 12 // Example: yearly projection
                    filteredDailyAverage = dailyAverage // Keep same daily average
                }
            ),
            ChipItem(
                id = "all",
                label = "All Time",
                isSelected = selectedFilterPeriod == FilterPeriod.ALL,
                onClick = {
                    selectedFilterPeriod = FilterPeriod.ALL
                    // Update filtered data for all time
                    filteredTotalSpend = 150000.0 // Example: total all-time spend
                    filteredDailyAverage = 1500.0 // All-time average
                }
            )
        )
    }

    val statsChips = remember(filteredTotalSpend, filteredDailyAverage, budgetRemainingPercent) {
        listOf(
            ChipItem(
                id = "total",
                label = "KES ${(filteredTotalSpend / 1000).toInt()}k spent",
                isSelected = true,
                onClick = { }
            ),
            ChipItem(
                id = "average",
                label = "KES ${filteredDailyAverage.toInt()}/day",
                onClick = { }
            ),
            ChipItem(
                id = "budget",
                label = "${budgetRemainingPercent.toInt()}% budget left",
                onClick = { }
            )
        )
    }

    val allActivities = remember {
        val now = Date()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())

        listOf(
            RecentSpendActivity(
                id = "1",
                title = "Coffee Shop",
                timestamp = calendar.apply {
                    time = now
                    add(Calendar.HOUR_OF_DAY, -2)
                }.time,
                amount = 450.0,
                icon = {
                    Icon(
                        Icons.Filled.Coffee,
                        contentDescription = "Coffee",
                        modifier = Modifier.size(24.dp)
                    )
                },
                iconColor = Color(0xFFF59E0B),
                backgroundColor = Color(0xFFF59E0B).copy(alpha = 0.1f),
                category = "Food & Drink"
            ),
            RecentSpendActivity(
                id = "2",
                title = "Internet Bill",
                timestamp = calendar.apply {
                    time = now
                    add(Calendar.DAY_OF_YEAR, -1)
                    set(Calendar.HOUR_OF_DAY, 15)
                    set(Calendar.MINUTE, 20)
                }.time,
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
                timestamp = calendar.apply {
                    time = now
                    add(Calendar.DAY_OF_YEAR, -2)
                    set(Calendar.HOUR_OF_DAY, 11)
                    set(Calendar.MINUTE, 15)
                }.time,
                amount = 2300.0,
                icon = {
                    Icon(
                        Icons.Filled.ShoppingBasket,
                        contentDescription = "Shopping",
                        modifier = Modifier.size(24.dp)
                    )
                },
                iconColor = Color(0xFF22C55E),
                backgroundColor = Color(0xFF22C55E).copy(alpha = 0.1f),
                category = "Groceries"
            ),
            RecentSpendActivity(
                id = "4",
                title = "Netflix Subscription",
                timestamp = calendar.apply {
                    time = now
                    add(Calendar.DAY_OF_YEAR, -3)
                    set(Calendar.HOUR_OF_DAY, 8)
                    set(Calendar.MINUTE, 30)
                }.time,
                amount = 1200.0,
                icon = {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Entertainment",
                        modifier = Modifier.size(24.dp)
                    )
                },
                iconColor = Color(0xFF8B5CF6),
                backgroundColor = Color(0xFF8B5CF6).copy(alpha = 0.1f),
                category = "Entertainment"
            ),
            RecentSpendActivity(
                id = "5",
                title = "Transport",
                timestamp = calendar.apply {
                    time = now
                    add(Calendar.DAY_OF_YEAR, -5)
                    set(Calendar.HOUR_OF_DAY, 9)
                    set(Calendar.MINUTE, 0)
                }.time,
                amount = 800.0,
                icon = {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = "Transport",
                        modifier = Modifier.size(24.dp)
                    )
                },
                iconColor = Color(0xFF3B82F6),
                backgroundColor = Color(0xFF3B82F6).copy(alpha = 0.1f),
                category = "Transport"
            ),
            RecentSpendActivity(
                id = "6",
                title = "Restaurant",
                timestamp = calendar.apply {
                    time = now
                    add(Calendar.DAY_OF_YEAR, -8)
                    set(Calendar.HOUR_OF_DAY, 19)
                    set(Calendar.MINUTE, 45)
                }.time,
                amount = 3200.0,
                icon = {
                    Icon(
                        Icons.Filled.Coffee,
                        contentDescription = "Restaurant",
                        modifier = Modifier.size(24.dp)
                    )
                },
                iconColor = Color(0xFFEC4899),
                backgroundColor = Color(0xFFEC4899).copy(alpha = 0.1f),
                category = "Dining"
            )
        ).sortedByDescending { it.timestamp }
    }

    // Filter activities based on selected period
    val filteredActivities = remember(selectedFilterPeriod, allActivities) {
        val calendar = Calendar.getInstance()
        val now = Date()

        when (selectedFilterPeriod) {
            FilterPeriod.TODAY -> {
                calendar.time = now
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val startOfDay = calendar.time
                allActivities.filter { it.timestamp.after(startOfDay) }
            }
            FilterPeriod.WEEKLY -> {
                calendar.time = now
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val weekAgo = calendar.time
                allActivities.filter { it.timestamp.after(weekAgo) }
            }
            FilterPeriod.MONTHLY -> {
                calendar.time = now
                calendar.add(Calendar.MONTH, -1)
                val monthAgo = calendar.time
                allActivities.filter { it.timestamp.after(monthAgo) }
            }
            FilterPeriod.YEARLY -> {
                calendar.time = now
                calendar.add(Calendar.YEAR, -1)
                val yearAgo = calendar.time
                allActivities.filter { it.timestamp.after(yearAgo) }
            }
            FilterPeriod.ALL -> allActivities
        }
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
                    // Top Navigation Row
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
                        // Total Spend with filter indicator
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Total Spend",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = when (selectedFilterPeriod) {
                                        FilterPeriod.TODAY -> "Today"
                                        FilterPeriod.WEEKLY -> "This Week"
                                        FilterPeriod.MONTHLY -> "This Month"
                                        FilterPeriod.YEARLY -> "This Year"
                                        FilterPeriod.ALL -> "All Time"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = PrimaryRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "KES ${filteredTotalSpend.toInt()}",
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

            // Quick Action Cards Grid - 3 cards with balanced layout
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding * 1.5f),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(responsivePadding)
                    ) {
                        // Left column with Add Expense and History cards
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(responsivePadding)
                        ) {
                            // Add Expense Card
                            SpendCard(
                                cardItem = spendCards[0],
                                modifier = Modifier.fillMaxWidth()
                            )

                            // History Card
                            SpendCard(
                                cardItem = spendCards[1],
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        // Right column with Bills card that takes full height
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            // Bills Card - Full height to match the two cards on the left
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = spendCards[2].onClick),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Max)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // Badge for bills
                                        if (spendCards[2].badgeCount > 0) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.End)
                                                    .clip(RoundedCornerShape(50.dp))
                                                    .background(Color(0xFFF59E0B))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "${spendCards[2].badgeCount} DUE",
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
                                                .background(spendCards[2].backgroundColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = spendCards[2].icon,
                                                contentDescription = spendCards[2].title,
                                                tint = spendCards[2].iconColor,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        // Title and content
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(
                                                text = spendCards[2].title,
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onBackground,
                                                fontSize = 16.sp
                                            )

                                            // Additional content to fill the space
                                            Column(
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                // Bill status indicator
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(8.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xFF22C55E))
                                                    )
                                                    Text(
                                                        text = "2 bills due",
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                        fontSize = 12.sp
                                                    )
                                                }

                                                // Bill summary
                                                Text(
                                                    text = "View and manage upcoming bills",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    fontSize = 11.sp,
                                                    lineHeight = 14.sp
                                                )
                                            }

                                            // Spacer to push content up (optional, for better balance)
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
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
            if (filteredActivities.isEmpty()) {
                item {
                    EmptyActivityState(
                        filterPeriod = selectedFilterPeriod,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = responsivePadding * 1.5f, vertical = 24.dp)
                    )
                }
            } else {
                items(filteredActivities) { activity ->
                    RecentSpendActivityItem(
                        activity = activity,
                        onClick = { navController.navigate("spend_detail/${activity.id}") },
                        horizontalPadding = responsivePadding
                    )
                }
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
    val timeAgo = remember(activity.timestamp) {
        calculateTimeAgo(activity.timestamp)
    }

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
                        text = activity.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                    Text(
                        text = timeAgo,
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

@Composable
fun EmptyActivityState(
    filterPeriod: FilterPeriod,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = "No Activity",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when (filterPeriod) {
                FilterPeriod.TODAY -> "No expenses today"
                FilterPeriod.WEEKLY -> "No expenses this week"
                FilterPeriod.MONTHLY -> "No expenses this month"
                FilterPeriod.YEARLY -> "No expenses this year"
                FilterPeriod.ALL -> "No expenses yet"
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp
        )

        Text(
            text = when (filterPeriod) {
                FilterPeriod.TODAY -> "Add your first expense for today"
                FilterPeriod.WEEKLY -> "Your expenses this week will appear here"
                FilterPeriod.MONTHLY -> "Your monthly expenses will appear here"
                FilterPeriod.YEARLY -> "Your yearly expenses will appear here"
                FilterPeriod.ALL -> "Add your first expense to get started"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 12.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
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