package com.example.pocket.ui.screens.plan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CarRental
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.pocket.viewmodels.Bill
import com.example.pocket.viewmodels.BillCategory
import com.example.pocket.viewmodels.BillIcon
import com.example.pocket.viewmodels.BillRemindersViewModel
import com.example.pocket.viewmodels.BillStatus
import com.example.pocket.viewmodels.BillSummary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun BillPlanningScreen(
    navController: NavController,
    viewModel: BillRemindersViewModel = viewModel()
) {
    val bills by viewModel.bills.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val summary by viewModel.summary.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val responsivePadding = calculateResponsivePadding()

    LaunchedEffect(Unit) {
        // Initial data load is handled by ViewModel init
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryRed)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
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
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            // Title (centered)
                            Text(
                                text = "Bills",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 20.sp
                            )

                            // Add button
                            IconButton(
                                onClick = { navController.navigate("add_bill") },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Add Bill",
                                    tint = PrimaryRed
                                )
                            }
                        }
                    }
                }

                // Category Progress Section
                categories.firstOrNull()?.let { category ->
                    item {
                        CategoryProgressCard(
                            category = category,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = responsivePadding)
                        )
                    }
                }

                // Action Required Section
                val actionRequiredBills = bills.filter { it.isOverdue || it.status == BillStatus.OVERDUE }
                if (actionRequiredBills.isNotEmpty()) {
                    item {
                        ActionRequiredSection(
                            bills = actionRequiredBills,
                            viewModel = viewModel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = responsivePadding)
                        )
                    }
                }

                // This Week Section
                val thisWeekBills = viewModel.getUpcomingBills()
                if (thisWeekBills.isNotEmpty()) {
                    item {
                        BillSection(
                            title = "This Week",
                            bills = thisWeekBills,
                            viewModel = viewModel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = responsivePadding)
                        )
                    }
                }

                // Later Section
                val laterBills = viewModel.getLaterBills()
                if (laterBills.isNotEmpty()) {
                    item {
                        BillSection(
                            title = "Later",
                            bills = laterBills,
                            viewModel = viewModel,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = responsivePadding)
                        )
                    }
                }

                // Subscription Hub
                val subscriptionBills = viewModel.getSubscriptionBills()
                if (subscriptionBills.isNotEmpty()) {
                    item {
                        SubscriptionHubCard(
                            bills = subscriptionBills,
                            totalCost = viewModel.getTotalSubscriptionCost(),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = responsivePadding)
                        )
                    }
                }

                // Insight Card
                summary?.let {
                    item {
                        InsightCard(
                            summary = it,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = responsivePadding)
                        )
                    }
                }

                // Bottom Spacer
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun CategoryProgressCard(
    category: BillCategory,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = category.name.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "$${category.remaining.toInt()} remaining",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 24.sp
                    )
                }

                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "${category.overdueCount} overdue",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRed,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Progress: ${category.paidCount} of ${category.totalCount} paid",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(category.progressPercentage)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(PrimaryRed)
                )
            }
        }
    }
}

@Composable
fun ActionRequiredSection(
    bills: List<Bill>,
    viewModel: BillRemindersViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                PrimaryRed.copy(alpha = 0.05f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(bottom = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 16.dp, end = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = "Action Required",
                tint = PrimaryRed,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "ACTION REQUIRED",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = PrimaryRed,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
        }

        // Bills list
        Column {
            bills.forEach { bill ->
                ActionRequiredBillItem(
                    bill = bill,
                    onPayClick = { viewModel.payBill(bill.id) },
                    onMarkPaidClick = { viewModel.markBillAsPaid(bill.id) }
                )
            }
        }
    }
}

@Composable
fun ActionRequiredBillItem(
    bill: Bill,
    onPayClick: () -> Unit,
    onMarkPaidClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (bill.isOverdue) PrimaryRed.copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getBillIcon(bill.iconType),
                    contentDescription = bill.name,
                    tint = if (bill.isOverdue) PrimaryRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Details
            Column {
                Text(
                    text = bill.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp
                )
                Text(
                    text = if (bill.isOverdue) "OVERDUE" else "Due in ${getDaysUntilDue(bill.dueDate)} days",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (bill.isOverdue) PrimaryRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }

        // Amount and button
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "$${String.format("%.2f", bill.amount)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp
            )

            if (bill.isOverdue) {
                Button(
                    onClick = onPayClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryRed,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(50.dp),
                    modifier = Modifier.height(28.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = "Pay",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            } else {
                OutlinedButton(
                    onClick = onMarkPaidClick,
                    modifier = Modifier.height(28.dp),
                    shape = RoundedCornerShape(50.dp)
                ) {
                    Text(
                        text = "Mark Paid",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BillSection(
    title: String,
    bills: List<Bill>,
    viewModel: BillRemindersViewModel,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Section header
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Bills list
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            bills.forEach { bill ->
                BillItem(
                    bill = bill,
                    onMarkPaidClick = { viewModel.markBillAsPaid(bill.id) }
                )
            }
        }
    }
}

@Composable
fun BillItem(
    bill: Bill,
    onMarkPaidClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { /* Navigate to bill detail */ },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getBillIcon(bill.iconType),
                    contentDescription = bill.name,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Details
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = bill.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp
                    )

                    if (bill.isAutoPay) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFE0F2FE))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "AUTOPAY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1),
                                fontSize = 8.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
                Text(
                    text = formatDate(bill.dueDate),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }

        // Amount
        Text(
            text = "$${String.format("%.2f", bill.amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionHubCard(
    bills: List<Bill>,
    totalCost: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Subscription Hub",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Monthly cost: $${String.format("%.0f", totalCost)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }

                // Avatar stack - using Box with absolute positioning
                Box(
                    modifier = Modifier.size(80.dp)
                ) {
                    listOf(
                        Color.Black to "N",
                        Color(0xFF2563EB) to "D",
                        MaterialTheme.colorScheme.surfaceVariant to "+${bills.size}"
                    ).forEachIndexed { index, (color, text) ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .align(Alignment.CenterStart)
                                .offset(x = (24 * index).dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = text,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (color == MaterialTheme.colorScheme.surfaceVariant)
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                else Color.White,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Review button
            OutlinedButton(
                onClick = { /* Navigate to subscription review */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "Review & Cancel",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun InsightCard(
    summary: BillSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryRed.copy(alpha = 0.05f),
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryRed),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = "Insight",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Message
            Text(
                text = "Bills are $${String.format("%.0f", summary.monthlyIncrease)} higher than last month. Tap to see why.",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 12.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// Helper functions
private fun getBillIcon(iconType: BillIcon): ImageVector {
    return when (iconType) {
        BillIcon.ELECTRICITY -> Icons.Filled.Bolt
        BillIcon.RENT -> Icons.Filled.Home
        BillIcon.INTERNET -> Icons.Filled.Language
        BillIcon.GYM -> Icons.Filled.FitnessCenter
        BillIcon.CAR_INSURANCE -> Icons.Filled.CarRental
        BillIcon.HEALTH -> Icons.Filled.LocalHospital
        BillIcon.SUBSCRIPTION -> Icons.Filled.Wifi
    }
}

private fun getDaysUntilDue(dueDate: Date): Int {
    val now = Calendar.getInstance()
    val due = Calendar.getInstance().apply { time = dueDate }

    val diff = due.timeInMillis - now.timeInMillis
    return (diff / (1000 * 60 * 60 * 24)).toInt().coerceAtLeast(0)
}

private fun formatDate(date: Date): String {
    val format = SimpleDateFormat("MMM dd", Locale.getDefault())
    return format.format(date)
}

// Extension property for BillCategory
private val BillCategory.overdueCount: Int
    get() = (totalCount - paidCount).takeIf { it > 0 } ?: 0

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun BillPlanningScreenPreview() {
    PocketTheme {
        BillPlanningScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun BillPlanningScreenPreviewDark() {
    PocketTheme(darkTheme = true) {
        BillPlanningScreen(navController = rememberNavController())
    }
}