package com.example.pocket.ui.screens.spend

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pocket.model.BillReminder
import com.example.pocket.ui.theme.PocketTheme
import com.example.pocket.ui.theme.PrimaryRed
import com.example.pocket.utils.calculateResponsivePadding
import com.example.pocket.viewmodels.BillReminderViewModel
import com.example.pocket.viewmodels.FilterType
import com.example.pocket.viewmodels.NextBillInfo
import com.example.pocket.viewmodels.RecurrenceType
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillReminderScreen(
    navController: NavController,
    viewModel: BillReminderViewModel = viewModel()
) {
    val context = LocalContext.current
    val responsivePadding = calculateResponsivePadding()
    val listState = rememberLazyListState()
    val uiState by viewModel.uiState.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .pointerInput(Unit) {
                detectTapGestures(onTap = {})
            }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding * 1.5f)
                        .padding(top = responsivePadding, bottom = responsivePadding * 0.5f),
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

                    // Title
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Bills",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 24.sp
                        )
                        if (uiState.thisWeekCount > 0) {
                            Text(
                                text = "${uiState.thisWeekCount} bill${if (uiState.thisWeekCount > 1) "s" else ""} due this week",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        } else if (uiState.overdueCount > 0) {
                            Text(
                                text = "${uiState.overdueCount} overdue bill${if (uiState.overdueCount > 1) "s" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        } else {
                            Text(
                                text = "All bills are paid",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Settings button
                    IconButton(
                        onClick = { /* TODO: Settings */ },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreHoriz,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Summary Card
            item {
                if (uiState.totalDue > 0 || uiState.overdueCount > 0) {
                    SummaryCard(
                        totalDue = uiState.totalDue,
                        nextBill = uiState.nextBill,
                        overdueCount = uiState.overdueCount,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = responsivePadding * 1.5f)
                    )
                }
            }

            // Filter Chips
            item {
                FilterChipsRow(
                    selectedFilter = uiState.selectedFilter,
                    onFilterSelected = viewModel::setFilter,
                    overdueCount = uiState.overdueCount,
                    thisWeekCount = uiState.thisWeekCount,
                    paidCount = uiState.paidCount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding * 1.5f)
                )
            }

            // Bill Groups
            if (uiState.filteredBills.isEmpty()) {
                item {
                    EmptyState(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        onAddBill = { viewModel.showAddBillSheet(true) }
                    )
                }
            } else {
                // Group bills by status
                val groupedBills = groupBillsByStatus(uiState.filteredBills)

                groupedBills.forEach { (status, bills) ->
                    if (bills.isNotEmpty()) {
                        item {
                            BillGroupHeader(
                                status = status,
                                count = bills.size,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = responsivePadding * 1.5f)
                            )
                        }

                        items(bills) { bill ->
                            BillItem(
                                bill = bill,
                                onTogglePaid = { viewModel.toggleBillPaidStatus(bill) },
                                onPay = {
                                    viewModel.toggleBillPaidStatus(bill)
                                    Toast.makeText(context, "Bill marked as paid", Toast.LENGTH_SHORT).show()
                                },
                                onEdit = { viewModel.showEditBillSheet(true, bill) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = responsivePadding * 1.5f)
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // Bottom spacer for FAB
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Add Bill FAB
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = responsivePadding, bottom = responsivePadding * 3)
        ) {
            FloatingActionButton(
                onClick = { viewModel.showAddBillSheet(true) },
                containerColor = PrimaryRed,
                contentColor = Color.White,
                modifier = Modifier
                    .size(56.dp)
                    .shadow(8.dp, shape = CircleShape, spotColor = PrimaryRed.copy(alpha = 0.3f))
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add Bill",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Loading state
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryRed)
            }
        }
    }

    // Add Bill Bottom Sheet
    if (uiState.showAddBillSheet) {
        AddBillBottomSheet(
            onDismiss = { viewModel.showAddBillSheet(false) },
            onAddBill = { name, amount, dueDate, recurrence, reminderDays ->
                viewModel.addBill(
                    context = context,
                    name = name,
                    amount = amount,
                    dueDate = dueDate,
                    recurrence = recurrence,
                    reminderDaysBefore = reminderDays,
                    onComplete = {
                        coroutineScope.launch {
                            Toast.makeText(context, "Bill added", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            }
        )
    }

    // Edit Bill Bottom Sheet
    if (uiState.showEditBillSheet && uiState.editingBill != null) {
        EditBillBottomSheet(
            bill = uiState.editingBill!!,
            onDismiss = { viewModel.showEditBillSheet(false) },
            onUpdateBill = { name, amount, dueDate, recurrence, reminderDays ->
                viewModel.updateBill(
                    context = context,
                    billId = uiState.editingBill!!.id,
                    name = name,
                    amount = amount,
                    dueDate = dueDate,
                    recurrence = recurrence,
                    reminderDaysBefore = reminderDays
                )
            },
            onDeleteBill = {
                viewModel.deleteBill(uiState.editingBill!!.id)
                viewModel.showEditBillSheet(false)
                Toast.makeText(context, "Bill deleted", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun SummaryCard(
    totalDue: Double,
    nextBill: NextBillInfo?,
    overdueCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                PrimaryRed.copy(alpha = 0.1f),
                RoundedCornerShape(16.dp)
            ),
        color = PrimaryRed.copy(alpha = 0.05f),
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(PrimaryRed.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AccountBalanceWallet,
                    contentDescription = "Total Due",
                    tint = PrimaryRed,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "KES ${String.format("%,.0f", totalDue)} total due",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp
                )

                nextBill?.let {
                    Text(
                        text = "Next: ${it.name} in ${it.daysUntil} day${if (it.daysUntil != 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRed,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                if (overdueCount > 0) {
                    Text(
                        text = "$overdueCount overdue",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                        color = Color.Red,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FilterChipsRow(
    selectedFilter: FilterType,
    onFilterSelected: (FilterType) -> Unit,
    overdueCount: Int,
    thisWeekCount: Int,
    paidCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            label = "All",
            isSelected = selectedFilter == FilterType.ALL,
            onClick = { onFilterSelected(FilterType.ALL) }
        )

        FilterChip(
            label = "Overdue",
            isSelected = selectedFilter == FilterType.OVERDUE,
            onClick = { onFilterSelected(FilterType.OVERDUE) },
            badgeCount = overdueCount
        )

        FilterChip(
            label = "This Week",
            isSelected = selectedFilter == FilterType.THIS_WEEK,
            onClick = { onFilterSelected(FilterType.THIS_WEEK) },
            badgeCount = thisWeekCount
        )

        FilterChip(
            label = "Paid",
            isSelected = selectedFilter == FilterType.PAID,
            onClick = { onFilterSelected(FilterType.PAID) },
            badgeCount = paidCount
        )
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    badgeCount: Int? = null,
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
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )

            badgeCount?.takeIf { it > 0 }?.let { count ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else PrimaryRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$count",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) PrimaryRed else Color.White,
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}

@Composable
fun BillGroupHeader(
    status: BillStatus,
    count: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = status.displayName.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = status.color(),
            fontSize = 10.sp,
            letterSpacing = 1.sp
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BillItem(
    bill: BillReminder,
    onTogglePaid: () -> Unit,
    onPay: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
    val today = Date()
    val daysRemaining = remember(bill.dueDate) {
        calculateDaysBetween(today, bill.dueDate)
    }

    val status = when {
        bill.isPaid -> BillStatus.PAID
        daysRemaining < 0 -> BillStatus.OVERDUE
        daysRemaining <= 7 -> BillStatus.THIS_WEEK
        else -> BillStatus.LATER
    }

    val itemModifier = modifier
        .pointerInput(Unit) {
            detectTapGestures(
                onLongPress = { onEdit() }
            )
        }

    Box(
        modifier = modifier
    ) {
        // Swipe to Pay background
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(12.dp))
                .background(PrimaryRed.copy(alpha = 0.1f))
                .padding(end = 80.dp), // Leave space for Pay button
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Pay",
                    tint = PrimaryRed,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "PAY NOW",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryRed,
                    fontSize = 10.sp,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // Bill content
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surface)
                .clickable { onEdit() },
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(billIconColor(bill.name).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = billIcon(bill.name),
                            contentDescription = bill.name,
                            tint = billIconColor(bill.name),
                            modifier = Modifier.size(24.dp)
                        )

                        // Overdue indicator
                        if (status == BillStatus.OVERDUE && !bill.isPaid) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.Red)
                                    .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                            )
                        }
                    }

                    // Details
                    Column {
                        Text(
                            text = bill.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (bill.isPaid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp,
                            textDecoration = if (bill.isPaid) androidx.compose.ui.text.style.TextDecoration.LineThrough else androidx.compose.ui.text.style.TextDecoration.None
                        )

                        Text(
                            text = when {
                                bill.isPaid -> "Paid"
                                status == BillStatus.OVERDUE -> "${-daysRemaining} days ago • Overdue"
                                daysRemaining == 0 -> "Due today"
                                daysRemaining == 1 -> "Due tomorrow"
                                else -> "Due in $daysRemaining days"
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = when {
                                bill.isPaid -> Color(0xFF22C55E)
                                status == BillStatus.OVERDUE -> Color.Red
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontSize = 11.sp
                        )

                        Text(
                            text = dateFormat.format(bill.dueDate),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // Amount
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "KES ${String.format("%,.0f", bill.amount)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (bill.isPaid) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp,
                        textDecoration = if (bill.isPaid) androidx.compose.ui.text.style.TextDecoration.LineThrough else androidx.compose.ui.text.style.TextDecoration.None
                    )

                    if (!bill.isPaid) {
                        TextButton(
                            onClick = onPay,
                            modifier = Modifier.height(28.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = PrimaryRed
                            )
                        ) {
                            Text(
                                text = "PAY",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    modifier: Modifier = Modifier,
    onAddBill: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.ReceiptLong,
            contentDescription = "No bills",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No bills yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Text(
            text = "Add your first bill to stay organized",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onAddBill,
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryRed,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Your First Bill")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillBottomSheet(
    onDismiss: () -> Unit,
    onAddBill: (String, Double, Date, RecurrenceType, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(Date()) }
    var selectedRecurrence by remember { mutableStateOf(RecurrenceType.ONE_TIME) }
    var reminderDays by remember { mutableIntStateOf(1) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // Header
            Text(
                text = "Add Bill",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 22.sp
            )

            // Amount Input (display)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "AMOUNT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )

                Text(
                    text = if (amount.isNotEmpty()) "KES $amount" else "KES 0",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 36.sp
                )

                // Hidden input to capture keyboard
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                            amount = newValue
                        }
                    },
                    modifier = Modifier.height(0.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Transparent,
                        unfocusedTextColor = Color.Transparent
                    )
                )
            }

            // Bill Name
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "BILL NAME",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g. Rent, Internet, Electricity") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = PrimaryRed.copy(alpha = 0.5f),
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
            }

            // Date & Reminder
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "DUE DATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )

                    DatePickerChip(
                        selectedDate = selectedDate,
                        onDateSelected = { selectedDate = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "REMINDER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )

                    ReminderPickerChip(
                        daysBefore = reminderDays,
                        onDaysSelected = { reminderDays = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Recurrence
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "RECURRENCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )

                RecurrenceSelector(
                    selectedRecurrence = selectedRecurrence,
                    onRecurrenceSelected = { selectedRecurrence = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Add Button
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amountValue > 0) {
                        onAddBill(
                            name,
                            amountValue,
                            selectedDate,
                            selectedRecurrence,
                            reminderDays
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryRed,
                    contentColor = Color.White
                ),
                enabled = name.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0
            ) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Bill Reminder",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBillBottomSheet(
    bill: BillReminder,
    onDismiss: () -> Unit,
    onUpdateBill: (String, Double, Date, RecurrenceType, Int) -> Unit,
    onDeleteBill: () -> Unit
) {
    var name by remember { mutableStateOf(bill.name) }
    var amount by remember { mutableStateOf(bill.amount.toString()) }
    var selectedDate by remember { mutableStateOf(bill.dueDate) }
    var selectedRecurrence by remember { mutableStateOf(
        RecurrenceType.valueOf(bill.recurrence)
    ) }
    var reminderDays by remember { mutableIntStateOf(bill.reminderDaysBefore) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Text(
                text = "Edit Bill",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 22.sp
            )

            // Amount Input
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "AMOUNT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )

                Text(
                    text = if (amount.isNotEmpty()) "KES $amount" else "KES 0",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 36.sp
                )
            }

            // Bill Name
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "BILL NAME",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("e.g. Rent, Internet, Electricity") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedIndicatorColor = PrimaryRed.copy(alpha = 0.5f),
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )
                )
            }

            // Date and Reminder Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Due Date
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "DUE DATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )

                    DatePickerChip(
                        selectedDate = selectedDate,
                        onDateSelected = { selectedDate = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Reminder
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "REMINDER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        letterSpacing = 1.sp
                    )

                    ReminderPickerChip(
                        daysBefore = reminderDays,
                        onDaysSelected = { reminderDays = it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Recurrence
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "RECURRENCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )

                RecurrenceSelector(
                    selectedRecurrence = selectedRecurrence,
                    onRecurrenceSelected = { selectedRecurrence = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Delete Button
                OutlinedButton(
                    onClick = onDeleteBill,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.Red
                    )
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete")
                }

                // Save Button
                Button(
                    onClick = {
                        val amountValue = amount.toDoubleOrNull() ?: 0.0
                        if (name.isNotBlank() && amountValue > 0) {
                            onUpdateBill(name, amountValue, selectedDate, selectedRecurrence, reminderDays)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryRed,
                        contentColor = Color.White
                    )
                ) {
                    Text("Save Changes")
                }
            }
        }
    }
}

@Composable
fun DatePickerChip(
    selectedDate: Date,
    onDateSelected: (Date) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM d, yyyy", Locale.getDefault()) }

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { /* TODO: Show date picker */ },
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dateFormat.format(selectedDate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )

            Icon(
                imageVector = Icons.Filled.CalendarMonth,
                contentDescription = "Pick Date",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun ReminderPickerChip(
    daysBefore: Int,
    onDaysSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (daysBefore == 1) "1 day before" else "$daysBefore days before",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )

            Icon(
                imageVector = Icons.Filled.Notifications,
                contentDescription = "Reminder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun RecurrenceSelector(
    selectedRecurrence: RecurrenceType,
    onRecurrenceSelected: (RecurrenceType) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RecurrenceChip(
                label = "One-time",
                isSelected = selectedRecurrence == RecurrenceType.ONE_TIME,
                onClick = { onRecurrenceSelected(RecurrenceType.ONE_TIME) },
                modifier = Modifier.weight(1f)
            )

            RecurrenceChip(
                label = "Monthly",
                isSelected = selectedRecurrence == RecurrenceType.MONTHLY,
                onClick = { onRecurrenceSelected(RecurrenceType.MONTHLY) },
                modifier = Modifier.weight(1f)
            )

            RecurrenceChip(
                label = "Yearly",
                isSelected = selectedRecurrence == RecurrenceType.YEARLY,
                onClick = { onRecurrenceSelected(RecurrenceType.YEARLY) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun RecurrenceChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) PrimaryRed else Color.Transparent
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )
    }
}

// Helper functions
enum class BillStatus(val displayName: String) {
    OVERDUE("Overdue"),
    THIS_WEEK("This Week"),
    LATER("Later This Month"),
    PAID("Paid")
}

@Composable
fun BillStatus.color(): Color {
    return when (this) {
        BillStatus.OVERDUE -> Color.Red
        BillStatus.THIS_WEEK -> Color(0xFFF59E0B)
        BillStatus.LATER -> MaterialTheme.colorScheme.onSurfaceVariant
        BillStatus.PAID -> Color(0xFF22C55E)
    }
}


fun groupBillsByStatus(bills: List<BillReminder>): Map<BillStatus, List<BillReminder>> {
    val today = Date()
    val calendar = Calendar.getInstance()
    calendar.time = today
    calendar.add(Calendar.DAY_OF_YEAR, 30)
    val monthFromNow = calendar.time

    return bills.groupBy { bill ->
        when {
            bill.isPaid -> BillStatus.PAID
            bill.dueDate.before(today) -> BillStatus.OVERDUE
            bill.dueDate.before(monthFromNow) -> BillStatus.THIS_WEEK
            else -> BillStatus.LATER
        }
    }
}

fun calculateDaysBetween(start: Date, end: Date): Int {
    val diff = end.time - start.time
    return (diff / (24 * 60 * 60 * 1000)).toInt()
}

fun billIcon(billName: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when {
        billName.contains("rent", ignoreCase = true) -> Icons.Filled.Home
        billName.contains("internet", ignoreCase = true) -> Icons.Filled.Wifi
        billName.contains("electric", ignoreCase = true) -> Icons.Filled.FlashOn
        billName.contains("water", ignoreCase = true) -> Icons.Filled.WaterDrop
        billName.contains("gym", ignoreCase = true) -> Icons.Filled.FitnessCenter
        billName.contains("insurance", ignoreCase = true) -> Icons.Filled.Shield
        billName.contains("phone", ignoreCase = true) -> Icons.Filled.Phone
        else -> Icons.Filled.Receipt
    }
}

fun billIconColor(billName: String): Color {
    return when {
        billName.contains("rent", ignoreCase = true) -> Color(0xFF8B5CF6)
        billName.contains("internet", ignoreCase = true) -> Color(0xFF3B82F6)
        billName.contains("electric", ignoreCase = true) -> Color(0xFFF59E0B)
        billName.contains("water", ignoreCase = true) -> Color(0xFF0EA5E9)
        billName.contains("gym", ignoreCase = true) -> Color(0xFFEF4444)
        billName.contains("insurance", ignoreCase = true) -> Color(0xFF10B981)
        billName.contains("phone", ignoreCase = true) -> Color(0xFF8B5CF6)
        else -> PrimaryRed
    }
}

// Preview Screens
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun BillReminderScreenPreviewPhone() {
    PocketTheme {
        BillReminderScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun BillReminderScreenPreviewPhoneDark() {
    PocketTheme(darkTheme = true) {
        BillReminderScreen(navController = rememberNavController())
    }
}