package com.example.pocket.ui.screens.spend

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pocket.ui.theme.PocketTheme
import com.example.pocket.ui.theme.PrimaryRed
import com.example.pocket.utils.calculateResponsivePadding
import com.example.pocket.viewmodels.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    navController: NavController,
    viewModel: ActivityViewModel = viewModel()
) {
    val responsivePadding = calculateResponsivePadding()
    val uiState = viewModel.uiState
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                shadowElevation = 1.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = responsivePadding * 1.5f,
                            vertical = responsivePadding
                        )
                ) {
                    // Navigation Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Back button
                        IconButton(
                            onClick = { navController.popBackStack() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Title - Centered
                        Text(
                            text = "Activity",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 24.sp
                        )

                        // Filter button
                        IconButton(
                            onClick = { viewModel.toggleFilterMenu() },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Filter",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Filter Chips (if not showing menu)
            if (!uiState.showFilterMenu) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding * 1.5f)
                        .padding(top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActivityFilterChip(
                        label = "All",
                        isSelected = uiState.selectedFilter == ActivityFilter.ALL,
                        onClick = { viewModel.setFilter(ActivityFilter.ALL) }
                    )
                    ActivityFilterChip(
                        label = "Today",
                        isSelected = uiState.selectedFilter == ActivityFilter.TODAY,
                        onClick = { viewModel.setFilter(ActivityFilter.TODAY) }
                    )
                    ActivityFilterChip(
                        label = "This Week",
                        isSelected = uiState.selectedFilter == ActivityFilter.THIS_WEEK,
                        onClick = { viewModel.setFilter(ActivityFilter.THIS_WEEK) }
                    )
                }
            }

            // Activity List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PrimaryRed)
                }
            } else if (uiState.filteredActivities.isEmpty()) {
                EmptyActivityState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(40.dp)
                )
            } else {
                ActivityList(
                    activities = uiState.filteredActivities,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Filter Menu Dropdown
        if (uiState.showFilterMenu) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .clickable { viewModel.toggleFilterMenu() },
                contentAlignment = Alignment.TopEnd
            ) {
                FilterMenu(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = viewModel::setFilter,
                    modifier = Modifier
                        .padding(top = responsivePadding * 8, end = responsivePadding * 1.5f)
                        .width(160.dp)
                )
            }
        }
    }
}

@Composable
fun ActivityList(
    activities: List<ActivityItem>,
    modifier: Modifier = Modifier
) {
    val groupedActivities = groupActivitiesByDate(activities)

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(1.dp)
    ) {
        groupedActivities.forEach { (dateGroup, items) ->
            // Date Header
            this@LazyColumn.item {
                DateGroupHeader(
                    dateGroup = dateGroup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = calculateResponsivePadding() * 1.5f)
                        .padding(top = 24.dp, bottom = 12.dp)
                )
            }

            // Activity Items
            this@LazyColumn.items(items) { activity ->
                ActivityItemRow(
                    activity = activity,
                    modifier = Modifier.fillMaxWidth()
                )

                // Divider (except last item)
                if (items.last() != activity) {
                    this@LazyColumn.item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        )
                    }
                }
            }
        }

        // Bottom spacer
        this@LazyColumn.item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
fun ActivityItemRow(
    activity: ActivityItem,
    modifier: Modifier = Modifier
) {
    val responsivePadding = calculateResponsivePadding()
    val timeAgo = remember(activity.timestamp) {
        calculateTimeAgo(activity.timestamp)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(
                horizontal = responsivePadding * 1.5f,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            val icon = when (activity.icon) {
                "add" -> Icons.Default.Add
                "edit" -> Icons.Default.Edit
                "notifications" -> Icons.Default.Notifications
                "delete" -> Icons.Default.Delete
                "sync" -> Icons.Default.Sync
                "settings" -> Icons.Default.Settings
                else -> Icons.Default.Add
            }

            val iconColor = when (activity.icon) {
                "add" -> PrimaryRed
                "edit" -> Color(0xFF007AFF)
                "notifications" -> Color(0xFF5856D6)
                "delete" -> Color(0xFFFF3B30)
                "sync" -> Color(0xFF34C759)
                "settings" -> Color(0xFF8E8E93)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }

            Icon(
                imageVector = icon,
                contentDescription = activity.title,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }

        // Details
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = activity.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 15.sp,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )

            // Show amount if available
            activity.amount?.let { amount ->
                Text(
                    text = "KES ${String.format("%,.0f", amount)}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (activity.icon == "add") Color.Red else Color(0xFF34C759),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }

        // Time ago
        Text(
            text = timeAgo,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp
        )
    }
}

@Composable
fun DateGroupHeader(
    dateGroup: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = dateGroup.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontSize = 11.sp,
        letterSpacing = 0.5.sp,
        modifier = modifier
    )
}

@Composable
fun ActivityFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) PrimaryRed else MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) PrimaryRed else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
fun FilterMenu(
    selectedFilter: ActivityFilter,
    onFilterSelected: (ActivityFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            ActivityFilter.entries.forEach { filter ->
                FilterMenuItem(
                    label = when (filter) {
                        ActivityFilter.ALL -> "All Activity"
                        ActivityFilter.TODAY -> "Today"
                        ActivityFilter.YESTERDAY -> "Yesterday"
                        ActivityFilter.THIS_WEEK -> "This Week"
                        ActivityFilter.THIS_MONTH -> "This Month"
                    },
                    isSelected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Divider
                if (filter != ActivityFilter.entries.last()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    )
                }
            }
        }
    }
}

@Composable
fun FilterMenuItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) PrimaryRed else MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp
        )

        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "Selected",
                tint = PrimaryRed,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun EmptyActivityState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = "No Activity",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Activity Yet",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 18.sp
        )

        Text(
            text = "Your recent activity will appear here",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

// Helper functions
fun groupActivitiesByDate(activities: List<ActivityItem>): Map<String, List<ActivityItem>> {
    val calendar = Calendar.getInstance()
    val today = calendar.time

    calendar.add(Calendar.DAY_OF_YEAR, -1)
    val yesterdayStart = getStartOfDay(calendar.time)

    calendar.time = today
    calendar.add(Calendar.DAY_OF_YEAR, -7)
    val weekAgo = calendar.time

    calendar.time = today
    calendar.add(Calendar.DAY_OF_YEAR, -30)
    val monthAgo = calendar.time

    return activities.groupBy { activity ->
        when {
            activity.timestamp.after(getStartOfDay(today)) -> "Today"
            activity.timestamp.after(yesterdayStart) && activity.timestamp.before(getStartOfDay(today)) -> "Yesterday"
            activity.timestamp.after(weekAgo) -> "This Week"
            activity.timestamp.after(monthAgo) -> "Earlier This Month"
            else -> "Earlier"
        }
    }
}

fun calculateTimeAgo(timestamp: Date): String {
    val now = Date()
    val diff = now.time - timestamp.time

    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        days > 0 -> "${days}d ago"
        hours > 0 -> "${hours}h ago"
        minutes > 0 -> "${minutes}m ago"
        else -> "Just now"
    }
}

fun getStartOfDay(date: Date): Date {
    val calendar = Calendar.getInstance()
    calendar.time = date
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    return calendar.time
}

// Preview Screens
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun ActivityScreenPreviewPhone() {
    PocketTheme {
        ActivityScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun ActivityScreenPreviewPhoneDark() {
    PocketTheme(darkTheme = true) {
        ActivityScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, device = "spec:width=600dp,height=960dp")
@Composable
fun ActivityScreenPreviewTabletPortrait() {
    PocketTheme {
        ActivityScreen(navController = rememberNavController())
    }
}