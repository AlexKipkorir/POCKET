package com.example.pocket.ui.screens.spend

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Speed
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
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

/** Design-system neutrals shared with the Plan screen. */
private val CardBorder = Color(0xFFE5E7EB)
private val Slate800 = Color(0xFF1E293B)
private val Slate500 = Color(0xFF64748B)
private val Slate400 = Color(0xFF94A3B8)
private val SurfaceContainer = Color(0xFFF1F0F0)
private val SuccessGreen = Color(0xFF22C55E)
private val WarningAmber = Color(0xFFF59E0B)
private val ExpenseRed = Color(0xFFE11D48)

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
    val icon: ImageVector? = null,
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
                icon = Icons.Filled.AddCircle,
                iconColor = PrimaryRed,
                backgroundColor = PrimaryRed.copy(alpha = 0.1f),
                onClick = { navController.navigate("add_expense") }
            ),
            SpendCardItem(
                id = "history",
                title = "History",
                icon = Icons.Filled.History,
                iconColor = Slate500,
                backgroundColor = SurfaceContainer,
                onClick = { navController.navigate("history") }
            ),
            SpendCardItem(
                id = "bills",
                title = "Bills",
                icon = Icons.AutoMirrored.Filled.ReceiptLong,
                iconColor = WarningAmber,
                backgroundColor = WarningAmber.copy(alpha = 0.12f),
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
                icon = Icons.AutoMirrored.Filled.TrendingDown,
                onClick = { }
            ),
            ChipItem(
                id = "average",
                label = "KES ${filteredDailyAverage.toInt()}/day",
                icon = Icons.Filled.Speed,
                onClick = { }
            ),
            ChipItem(
                id = "budget",
                label = "${budgetRemainingPercent.toInt()}% budget left",
                icon = Icons.Filled.PieChart,
                onClick = { }
            )
        )
    }

    val allActivities = remember {
        val now = Date()
        val calendar = Calendar.getInstance()

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
                iconColor = WarningAmber,
                backgroundColor = WarningAmber.copy(alpha = 0.1f),
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
                iconColor = SuccessGreen,
                backgroundColor = SuccessGreen.copy(alpha = 0.1f),
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

    val budgetUsedPercent = (100.0 - budgetRemainingPercent).coerceIn(0.0, 100.0)

    // NOTE: no Scaffold / bottom bar here on purpose — the bottom nav is
    // provided globally by the host, same as the Plan screen.
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
                Row(
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
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { navController.navigate("menu") },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Menu",
                            tint = Slate500
                        )
                    }

                    Text(
                        text = "Spend",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRed,
                        fontSize = 20.sp,
                        lineHeight = 24.sp
                    )

                    IconButton(
                        onClick = { navController.navigate("spend_settings") },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Settings,
                            contentDescription = "Settings",
                            tint = Slate500
                        )
                    }
                }
            }

            // Summary Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding),
                    shape = RoundedCornerShape(12.dp),
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
                            .padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "THIS MONTH",
                                    fontWeight = FontWeight.Medium,
                                    color = Slate500,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "KES ${"%,d".format(filteredTotalSpend.toInt())}",
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Slate800,
                                    fontSize = 34.sp,
                                    lineHeight = 38.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Icon(
                                imageVector = Icons.Filled.AccountBalanceWallet,
                                contentDescription = null,
                                tint = PrimaryRed.copy(alpha = 0.15f),
                                modifier = Modifier.size(48.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Budget Usage",
                                    color = Slate500,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "${budgetRemainingPercent.toInt()}% left",
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryRed,
                                    fontSize = 13.sp
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(CardBorder)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth((budgetUsedPercent / 100f).toFloat())
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(50))
                                        .background(SuccessGreen)
                                )
                            }
                        }
                    }
                }
            }

            // Period Filters
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = responsivePadding),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filterPeriods.forEach { chip ->
                        FilterChip(chip = chip, onClick = chip.onClick)
                    }
                }
            }

            // Quick Action Cards (bento grid: two stacked rows on the left, one tall card on the right)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SpendActionRow(cardItem = spendCards[0], modifier = Modifier.fillMaxWidth())
                        SpendActionRow(cardItem = spendCards[1], modifier = Modifier.fillMaxWidth())
                    }

                    BillsQuickCard(
                        cardItem = spendCards[2],
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }

            // Stats Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = responsivePadding),
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
                        .padding(horizontal = responsivePadding),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent Activity",
                        fontWeight = FontWeight.Bold,
                        color = Slate800,
                        fontSize = 18.sp
                    )

                    Text(
                        text = "View All",
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRed,
                        fontSize = 13.sp,
                        modifier = Modifier.clickable { navController.navigate("history") }
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
                            .padding(horizontal = responsivePadding, vertical = 24.dp)
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

        // Floating Action Button (pill, matching the design)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = responsivePadding, bottom = responsivePadding * 3)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(14.dp),
                    spotColor = PrimaryRed.copy(alpha = 0.4f)
                )
                .clip(RoundedCornerShape(14.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(PrimaryRed, PrimaryRed.copy(alpha = 0.85f))
                    )
                )
                .clickable { navController.navigate("add_expense") }
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Expense",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Add",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/** Compact horizontal row card used for "Add Expense" and "History" quick actions. */
@Composable
fun SpendActionRow(
    cardItem: SpendCardItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = cardItem.onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Slate800
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(cardItem.backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = cardItem.icon,
                    contentDescription = cardItem.title,
                    tint = cardItem.iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = cardItem.title,
                fontWeight = FontWeight.Bold,
                color = Slate800,
                fontSize = 16.sp
            )
        }
    }
}

/** Tall card spanning both rows on the right — matches the design's "Bills" bento tile. */
@Composable
fun BillsQuickCard(
    cardItem: SpendCardItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = cardItem.onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Slate800
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Max)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(cardItem.backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = cardItem.icon,
                        contentDescription = cardItem.title,
                        tint = cardItem.iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = cardItem.title,
                        fontWeight = FontWeight.Bold,
                        color = Slate800,
                        fontSize = 18.sp
                    )
                    if (cardItem.badgeCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(WarningAmber)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${cardItem.badgeCount} DUE SOON",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 10.sp
                            )
                        }
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
            .clip(RoundedCornerShape(50))
            .background(if (chip.isSelected) PrimaryRed else Color.White)
            .border(
                width = 1.dp,
                color = if (chip.isSelected) Color.Transparent else CardBorder,
                shape = RoundedCornerShape(50)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Text(
            text = chip.label,
            fontWeight = if (chip.isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (chip.isSelected) Color.White else Slate800,
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
            .clip(RoundedCornerShape(8.dp))
            .background(SurfaceContainer)
            .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
            .clickable(onClick = chip.onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            chip.icon?.let {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = Slate500,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = chip.label,
                fontWeight = FontWeight.Medium,
                color = Slate500,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun RecentSpendActivityItem(
    activity: RecentSpendActivity,
    onClick: () -> Unit,
    horizontalPadding: Dp,
    modifier: Modifier = Modifier
) {
    val displayTimestamp = remember(activity.timestamp) {
        formatActivityTimestamp(activity.timestamp)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Slate800
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(SurfaceContainer),
                    contentAlignment = Alignment.Center
                ) {
                    activity.icon()
                }

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = activity.title,
                        fontWeight = FontWeight.Bold,
                        color = Slate800,
                        fontSize = 16.sp
                    )
                    Text(
                        text = displayTimestamp,
                        color = Slate500,
                        fontSize = 12.sp
                    )
                }
            }

            Text(
                text = "-KES ${"%,d".format(activity.amount.toInt())}",
                fontWeight = FontWeight.Bold,
                color = ExpenseRed,
                fontSize = 15.sp
            )
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
            tint = Slate400.copy(alpha = 0.6f),
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
            fontWeight = FontWeight.Medium,
            color = Slate800,
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
            color = Slate500,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

/** Formats a timestamp as "Today, 9:41 AM" / "Yesterday, 4:15 PM" / "MMM dd, h:mm a", matching the design. */
private fun formatActivityTimestamp(timestamp: Date): String {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val today = Calendar.getInstance()
    val activityDay = Calendar.getInstance().apply { time = timestamp }
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }

    fun isSameDay(a: Calendar, b: Calendar) =
        a.get(Calendar.YEAR) == b.get(Calendar.YEAR) && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

    return when {
        isSameDay(today, activityDay) -> "Today, ${timeFormat.format(timestamp)}"
        isSameDay(yesterday, activityDay) -> "Yesterday, ${timeFormat.format(timestamp)}"
        else -> SimpleDateFormat("MMM dd, h:mm a", Locale.getDefault()).format(timestamp)
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