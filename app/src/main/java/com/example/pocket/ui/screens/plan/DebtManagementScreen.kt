package com.example.pocket.ui.screens.debt

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocket.ui.theme.PocketTheme
import com.example.pocket.ui.theme.PrimaryRed
import com.example.pocket.utils.calculateResponsivePadding
import com.example.pocket.utils.rememberWindowSize
import com.example.pocket.viewmodels.DebtManagementViewModel
import com.example.pocket.viewmodels.DebtSheetState
import com.example.pocket.viewmodels.DebtType
import com.example.pocket.viewmodels.RepaymentStrategy
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: DebtManagementViewModel = viewModel()
) {
    val context = LocalContext.current
    val responsivePadding = calculateResponsivePadding()
    val windowSize = rememberWindowSize()
    val coroutineScope = rememberCoroutineScope()

    // Collect state from ViewModel
    val debts by viewModel.debts.collectAsState()
    val debtGoal by viewModel.debtGoal.collectAsState()
    val sheetState by viewModel.sheetState.collectAsState()
    val selectedStrategy = viewModel.selectedStrategy

    // Sheet states
    val addDebtSheetState = rememberModalBottomSheetState()
    val payDebtSheetState = rememberModalBottomSheetState()
    val setGoalSheetState = rememberModalBottomSheetState()

    // Date picker state
    var showDatePicker by remember { mutableStateOf(false) }
    var datePickerType by remember { mutableStateOf("") } // "dueDate" or "goalDate"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Debt Management",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle settings */ }) {
                        Icon(
                            Icons.Outlined.Settings,
                            contentDescription = "Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingAddButton(
                onClick = { viewModel.showAddDebtSheet() }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Total Debt Card
                    TotalDebtCard(
                        totalDebt = viewModel.totalDebt,
                        totalProgress = viewModel.totalProgress,
                        monthlyReduction = viewModel.monthlyDebtReduction,
                        modifier = Modifier.padding(horizontal = responsivePadding)
                    )
                }

                item {
                    // Action Buttons
                    ActionButtonsRow(
                        onAddDebt = { viewModel.showAddDebtSheet() },
                        onPayNow = { viewModel.showPayDebtSheet() },
                        onSetGoal = { viewModel.showSetGoalSheet() },
                        modifier = Modifier.padding(horizontal = responsivePadding)
                    )
                }

                item {
                    // Strategy Section
//                    StrategySection(
//                        selectedStrategy = selectedStrategy,
//                        onStrategySelected = { strategy -> viewModel.setStrategy(strategy) },
//                        projectedDate = viewModel.getProjectedPayoffDate(),
//                        modifier = Modifier.padding(horizontal = responsivePadding)
//                    )
                }

                item {
                    // Active Debts Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = responsivePadding)
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Debts",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "View All",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryRed,
                            modifier = Modifier
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .background(
                                    color = PrimaryRed.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                // Debt List
                items(debts.filter { !it.isPaidOff }) { debt ->
                    DebtItem(
                        debt = debt,
                        onPayClick = { viewModel.showPayDebtSheet(it) },
                        modifier = Modifier.padding(horizontal = responsivePadding)
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }

            // Add Debt Bottom Sheet
            if (sheetState == DebtSheetState.AddDebt) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.hideSheet() },
                    sheetState = addDebtSheetState,
                    dragHandle = { BottomSheetDefaults.DragHandle() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AddDebtSheetContent(
                        viewModel = viewModel,
                        onDatePickerClick = {
                            datePickerType = "dueDate"
                            showDatePicker = true
                        },
                        onAddDebt = {
                            coroutineScope.launch {
                                viewModel.addDebt()
                                addDebtSheetState.hide()
                            }
                        },
                        onDismiss = {
                            coroutineScope.launch {
                                addDebtSheetState.hide()
                                viewModel.hideSheet()
                            }
                        }
                    )
                }
            }

            // Pay Debt Bottom Sheet
            if (sheetState is DebtSheetState.PayDebt) {
                val payDebtState = sheetState as DebtSheetState.PayDebt
                ModalBottomSheet(
                    onDismissRequest = { viewModel.hideSheet() },
                    sheetState = payDebtSheetState,
                    dragHandle = { BottomSheetDefaults.DragHandle() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PayDebtSheetContent(
                        debt = payDebtState.debt,
                        paymentAmount = viewModel.paymentAmount,
                        onPaymentAmountChange = { viewModel.updatePaymentAmount(it) },
                        onPay = {
                            coroutineScope.launch {
                                viewModel.makePayment(payDebtState.debt)
                                payDebtSheetState.hide()
                            }
                        },
                        onDismiss = {
                            coroutineScope.launch {
                                payDebtSheetState.hide()
                                viewModel.hideSheet()
                            }
                        }
                    )
                }
            }

            // Set Goal Bottom Sheet
            if (sheetState == DebtSheetState.SetGoal) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.hideSheet() },
                    sheetState = setGoalSheetState,
                    dragHandle = { BottomSheetDefaults.DragHandle() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SetGoalSheetContent(
                        viewModel = viewModel,
                        onDatePickerClick = {
                            datePickerType = "goalDate"
                            showDatePicker = true
                        },
                        onSetGoal = {
                            coroutineScope.launch {
                                viewModel.setDebtGoal()
                                setGoalSheetState.hide()
                            }
                        },
                        onDismiss = {
                            coroutineScope.launch {
                                setGoalSheetState.hide()
                                viewModel.hideSheet()
                            }
                        }
                    )
                }
            }

            // Date Picker Dialog
            if (showDatePicker) {
                val datePickerState = rememberDatePickerState()
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val date = Instant.ofEpochMilli(millis)
                                        .atZone(ZoneId.systemDefault())
                                        .toLocalDate()
                                    if (datePickerType == "dueDate") {
                                        viewModel.updateDebtDueDate(date)
                                    } else {
                                        viewModel.updateGoalTargetDate(date)
                                    }
                                }
                                showDatePicker = false
                            }
                        ) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel")
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }
        }
    }
}

@Composable
fun TotalDebtCard(
    totalDebt: Double,
    totalProgress: Double,
    monthlyReduction: Double,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = totalProgress.toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "totalProgress"
    )

    // Read colors OUTSIDE Canvas
    val backgroundCircleColor = MaterialTheme.colorScheme.surfaceVariant
    val progressColor = PrimaryRed
    val textColor = MaterialTheme.colorScheme.onBackground
    val secondaryTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
    val strokeWidth = 10.dp

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total Debt",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = secondaryTextColor,
                    letterSpacing = 0.1.sp
                )
                Text(
                    text = formatCurrency(totalDebt),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = textColor,
                    lineHeight = 40.sp
                )
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Payments,
                        contentDescription = null,
                        tint = progressColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${formatCurrency(monthlyReduction)} this month",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = progressColor
                    )
                }
            }

            // Circular Progress
            Box(
                modifier = Modifier.size(112.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokePx = strokeWidth.toPx()
                    val radius = size.minDimension / 2 - strokePx / 2

                    // Background circle
                    drawCircle(
                        color = backgroundCircleColor,
                        radius = radius,
                        style = Stroke(width = strokePx)
                    )

                    // Progress arc
                    val sweepAngle = (animatedProgress / 100f) * 360f
                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokePx, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${animatedProgress.toInt()}%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = "Paid",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryTextColor,
                        letterSpacing = 0.1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ActionButtonsRow(
    onAddDebt: () -> Unit,
    onPayNow: () -> Unit,
    onSetGoal: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Add Debt Button
        Button(
            onClick = onAddDebt,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryRed
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Debt",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Pay Now Button
        OutlinedButton(
            onClick = onPayNow,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Payments,
                    contentDescription = null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Pay Now",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Set Goal Button
        OutlinedButton(
            onClick = onSetGoal,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Flag,
                    contentDescription = null,
                    tint = PrimaryRed,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Set Goal",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

//@Composable
//fun StrategySection(
//    selectedStrategy: RepaymentStrategy,
//    onStrategySelected: (RepaymentStrategy) -> Unit,
//    projectedDate: LocalDate,
//    modifier: Modifier = Modifier
//) {
//    Column(modifier = modifier) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(
//                text = "Strategy",
//                fontSize = 20.sp,
//                fontWeight = FontWeight.Bold,
//                color = MaterialTheme.colorScheme.onBackground
//            )
//
//            // Strategy Toggle
//            Surface(
//                modifier = Modifier
//                    .clip(RoundedCornerShape(12.dp))
//                    .background(MaterialTheme.colorScheme.surfaceVariant),
//                color = MaterialTheme.colorScheme.surfaceVariant
//            ) {
//                Row(
//                    modifier = Modifier.padding(4.dp)
//                ) {
//                    Button(
//                        onClick = { onStrategySelected(RepaymentStrategy.SNOWBALL) },
//                        modifier = Modifier.height(32.dp),
//                        shape = RoundedCornerShape(8.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = if (selectedStrategy == RepaymentStrategy.SNOWBALL) {
//                                MaterialTheme.colorScheme.surface
//                            } else {
//                                Color.Transparent
//                            }
//                        ),
//                        elevation = if (selectedStrategy == RepaymentStrategy.SNOWBALL) {
//                            ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
//                        } else {
//                            ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
//                        }
//                    ) {
//                        Text(
//                            text = "Snowball",
//                            fontSize = 12.sp,
//                            fontWeight = FontWeight.Bold
//                        )
//                    }
//
//                    Button(
//                        onClick = { onStrategySelected(RepaymentStrategy.AVALANCHE) },
//                        modifier = Modifier.height(32.dp),
//                        shape = RoundedCornerShape(8.dp),
//                        colors = ButtonDefaults.buttonColors(
//                            containerColor = if (selectedStrategy == RepaymentStrategy.AVALANCHE) {
//                                MaterialTheme.colorScheme.surface
//                            } else {
//                                Color.Transparent
//                            }
//                        ),
//                        elevation = if (selectedStrategy == RepaymentStrategy.AVALANCHE) {
//                            ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
//                        } else {
//                            ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
//                        }
//                    ) {
//                        Text(
//                            text = "Avalanche",
//                            fontSize = 12.sp,
//                            fontWeight = FontWeight.Medium,
//                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
//                        )
//                    }
//                }
//            }
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        // Progress Visualization
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(96.dp)
//                .padding(horizontal = 8.dp)
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(80.dp)
//                    .align(Alignment.BottomCenter),
//                horizontalArrangement = Arrangement.spacedBy(4.dp),
//                verticalAlignment = Alignment.Bottom
//            ) {
//                // Bars representing debt payoff timeline
//                listOf(85f, 65f, 45f, 25f, 12f).forEachIndexed { index, heightPercentage ->
//                    Column(
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.Bottom
//                    ) {
//                        Box(
//                            modifier = Modifier
//                                .fillMaxWidth()
//                                .weight(heightPercentage / 100)
//                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
//                                .background(
//                                    color = if (index == 4) PrimaryRed else PrimaryRed.copy(alpha = 0.8f)
//                                )
//                        )
//                    }
//                }
//            }
//        }
//
//        // Timeline Labels
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(horizontal = 8.dp),
//            horizontalArrangement = Arrangement.SpaceBetween
//        ) {
//            listOf("NOW", "2025", "2026", "2027", "FREE").forEachIndexed { index, label ->
//                Text(
//                    text = label,
//                    fontSize = 10.sp,
//                    fontWeight = FontWeight.Bold,
//                    color = if (index == 4) PrimaryRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
//                    letterSpacing = 0.1.sp
//                )
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Projected Payoff
//        Text(
//            text = "Projected payoff date: ${projectedDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}",
//            fontSize = 12.sp,
//            fontWeight = FontWeight.Medium,
//            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
//            modifier = Modifier.fillMaxWidth(),
//            textAlign = TextAlign.Center
//        )
//    }
//}

@Composable
fun DebtItem(
    debt: com.example.pocket.viewmodels.Debt,
    onPayClick: (com.example.pocket.viewmodels.Debt) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = debt.progress.toFloat(),
        animationSpec = tween(durationMillis = 800),
        label = "debtProgress"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getDebtTypeIcon(debt.type),
                            contentDescription = null,
                            tint = PrimaryRed,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = debt.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Interest: ${debt.interestRate}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                // Amount and Due Date
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatCurrency(debt.amount),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = debt.dueStatus,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (debt.isOverdue) PrimaryRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        letterSpacing = 0.1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Progress",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        letterSpacing = 0.1.sp
                    )
                    Text(
                        text = "${debt.progress.toInt()}% paid",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(animatedProgress / 100f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(PrimaryRed)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Pay Button
            Button(
                onClick = { onPayClick(debt) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryRed.copy(alpha = 0.1f)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text(
                    text = "Make Payment",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryRed
                )
            }
        }
    }
}

@Composable
fun FloatingAddButton(
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(64.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        color = PrimaryRed,
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                Icons.Filled.Add,
                contentDescription = "Add Debt",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtSheetContent(
    viewModel: DebtManagementViewModel,
    onDatePickerClick: () -> Unit,
    onAddDebt: () -> Unit,
    onDismiss: () -> Unit
) {
    val debtName = viewModel.newDebtName
    val debtAmount = viewModel.newDebtAmount
    val interestRate = viewModel.newDebtInterestRate
    val selectedType = viewModel.newDebtType
    val dueDate = viewModel.newDebtDueDate

    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add New Debt",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "Close")
            }
        }

        // Debt Name
        OutlinedTextField(
            value = debtName,
            onValueChange = { viewModel.updateDebtName(it) },
            label = { Text("Debt Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Amount
        OutlinedTextField(
            value = debtAmount,
            onValueChange = { viewModel.updateDebtAmount(it) },
            label = { Text("Amount") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            prefix = { Text("$") },
            shape = RoundedCornerShape(12.dp)
        )

        // Interest Rate
        OutlinedTextField(
            value = interestRate,
            onValueChange = { viewModel.updateDebtInterestRate(it) },
            label = { Text("Interest Rate (%)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            suffix = { Text("%") },
            shape = RoundedCornerShape(12.dp)
        )

        // Debt Type
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedType.displayName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Debt Type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DebtType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.displayName) },
                        onClick = {
                            viewModel.updateDebtType(type)
                            expanded = false
                        },
                        leadingIcon = {
                            Icon(
                                getDebtTypeIcon(type),
                                contentDescription = null,
                                tint = PrimaryRed
                            )
                        }
                    )
                }
            }
        }

        // Due Date
        OutlinedButton(
            onClick = onDatePickerClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Due Date",
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dueDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy")),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = PrimaryRed
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add Button
        Button(
            onClick = onAddDebt,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryRed
            ),
            enabled = debtName.isNotBlank() && debtAmount.isNotBlank()
        ) {
            Text(
                text = "Add Debt",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PayDebtSheetContent(
    debt: com.example.pocket.viewmodels.Debt?,
    paymentAmount: String,
    onPaymentAmountChange: (String) -> Unit,
    onPay: () -> Unit,
    onDismiss: () -> Unit
) {
    val title = if (debt != null) "Pay ${debt.name}" else "Make Payment"
    val maxAmount = debt?.amount ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "Close")
            }
        }

        if (debt != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Remaining Balance",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Text(
                        text = formatCurrency(debt.amount),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        // Payment Amount
        OutlinedTextField(
            value = paymentAmount,
            onValueChange = onPaymentAmountChange,
            label = { Text("Payment Amount") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            prefix = { Text("$") },
            shape = RoundedCornerShape(12.dp)
        )

        Text(
            text = "Maximum: ${formatCurrency(maxAmount)}",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.padding(start = 4.dp)
        )

        // Quick payment buttons
        if (debt != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(100.0, 250.0, 500.0, maxAmount).forEach { amount ->
                    OutlinedButton(
                        onClick = { onPaymentAmountChange(amount.toString()) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = if (amount == maxAmount) "Full" else formatCurrency(amount),
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Pay Button
        Button(
            onClick = onPay,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryRed
            ),
            enabled = paymentAmount.isNotBlank() && paymentAmount.toDoubleOrNull() ?: 0.0 > 0
        ) {
            Text(
                text = "Make Payment",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SetGoalSheetContent(
    viewModel: DebtManagementViewModel,
    onDatePickerClick: () -> Unit,
    onSetGoal: () -> Unit,
    onDismiss: () -> Unit
) {
    val goalTargetDate = viewModel.goalTargetDate
    val goalTargetAmount = viewModel.goalTargetAmount
    val totalDebt = viewModel.totalDebt

    val monthlyPayment = if (goalTargetAmount.isNotBlank()) {
        val targetAmount = goalTargetAmount.toDoubleOrNull() ?: totalDebt
        val months = java.time.temporal.ChronoUnit.MONTHS.between(
            java.time.LocalDate.now(),
            goalTargetDate
        ).coerceAtLeast(1)
        targetAmount / months
    } else 0.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Set Debt Goal",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.AutoMirrored.Filled.ArrowForwardIos, contentDescription = "Close")
            }
        }

        // Current Debt Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Current Total Debt",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    text = formatCurrency(totalDebt),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Target Amount
        OutlinedTextField(
            value = goalTargetAmount,
            onValueChange = { viewModel.updateGoalTargetAmount(it) },
            label = { Text("Target Amount") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            prefix = { Text("$") },
            shape = RoundedCornerShape(12.dp)
        )

        // Target Date
        OutlinedButton(
            onClick = onDatePickerClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Target Date",
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = goalTargetDate.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Filled.CalendarMonth,
                        contentDescription = null,
                        tint = PrimaryRed
                    )
                }
            }
        }

        // Monthly Payment Calculation
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = PrimaryRed.copy(alpha = 0.1f)
            ),
            border = BorderStroke(1.dp, PrimaryRed.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Monthly Payment Required",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Text(
                    text = formatCurrency(monthlyPayment),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryRed
                )
                Text(
                    text = "to be debt-free by ${goalTargetDate.format(DateTimeFormatter.ofPattern("MMMM yyyy"))}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Set Goal Button
        Button(
            onClick = onSetGoal,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryRed
            ),
            enabled = goalTargetAmount.isNotBlank()
        ) {
            Text(
                text = "Set Goal",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun getDebtTypeIcon(type: DebtType): ImageVector {
    return when (type) {
        DebtType.CREDIT_CARD -> Icons.Filled.CreditCard
        DebtType.STUDENT_LOAN -> Icons.Filled.School
        DebtType.CAR_LOAN -> Icons.Filled.DirectionsCar
        DebtType.PERSONAL_LOAN -> Icons.Filled.Person
        DebtType.MEDICAL -> Icons.Filled.MedicalServices
        DebtType.MORTGAGE -> Icons.Filled.Payments // Using payments as mortgage icon
        DebtType.OTHER -> Icons.Filled.ReceiptLong
    }
}

fun formatCurrency(amount: Double): String {
    val formatter = DecimalFormat("#,##0.00")
    return "$${formatter.format(amount)}"
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Light Mode")
@Composable
fun DebtManagementScreenPreview_Light() {
    PocketTheme(darkTheme = false) {
        DebtManagementScreen(onNavigateBack = {})
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun DebtManagementScreenPreview_Dark() {
    PocketTheme(darkTheme = true) {
        DebtManagementScreen(onNavigateBack = {})
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Compact Screen", widthDp = 360)
@Composable
fun DebtManagementScreenPreview_Compact() {
    PocketTheme(darkTheme = false) {
        DebtManagementScreen(onNavigateBack = {})
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Medium Screen", widthDp = 600)
@Composable
fun DebtManagementScreenPreview_Medium() {
    PocketTheme(darkTheme = false) {
        DebtManagementScreen(onNavigateBack = {})
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, name = "Expanded Screen", widthDp = 840)
@Composable
fun DebtManagementScreenPreview_Expanded() {
    PocketTheme(darkTheme = false) {
        DebtManagementScreen(onNavigateBack = {})
    }
}