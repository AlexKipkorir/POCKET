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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Commute
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pocket.model.CategoryType
import com.example.pocket.ui.theme.PocketTheme
import com.example.pocket.ui.theme.PrimaryRed
import com.example.pocket.utils.calculateResponsivePadding
import com.example.pocket.viewmodels.BudgetViewModel
import kotlin.math.roundToInt


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetPlanningScreen(
    navController: NavController,
    viewModel: BudgetViewModel = viewModel()
) {
    val monthlyBudget by viewModel.monthlyBudget.collectAsState()
    val budgetCategories by viewModel.budgetCategories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val allocatedAmount = viewModel.allocatedAmount
    val remainingAmount = viewModel.remainingAmount
    val allocationPercentage = viewModel.allocationPercentage

    val responsivePadding = calculateResponsivePadding()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var showAddDialog by remember { mutableStateOf(false) }

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
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBackIos,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            // Title
                            Text(
                                text = "Intentional Planning",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 16.sp
                            )

                            // Calendar button
                            IconButton(
                                onClick = { /* Navigate to calendar */ },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.CalendarMonth,
                                    contentDescription = "Calendar",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                }

                // Budget Overview Section
                item {
                    BudgetOverviewSection(
                        monthlyBudget = monthlyBudget,
                        allocatedAmount = allocatedAmount,
                        remainingAmount = remainingAmount,
                        allocationPercentage = allocationPercentage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = responsivePadding)
                    )
                }

                // Budget Categories by Type
                item {
                    val livingExpenses = viewModel.getCategoriesByType(CategoryType.LIVING_EXPENSES)
                    val livingExpensesTotal = viewModel.getTotalByType(CategoryType.LIVING_EXPENSES)
                    val livingExpensesPercentage = viewModel.getPercentageByType(CategoryType.LIVING_EXPENSES)

                    CategorySection(
                        title = "Living Expenses",
                        categories = livingExpenses,
                        totalAmount = livingExpensesTotal,
                        totalPercentage = livingExpensesPercentage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = responsivePadding)
                    )
                }

                item {
                    val savingsGoals = viewModel.getCategoriesByType(CategoryType.SAVINGS_GOALS)
                    val savingsGoalsTotal = viewModel.getTotalByType(CategoryType.SAVINGS_GOALS)
                    val savingsGoalsPercentage = viewModel.getPercentageByType(CategoryType.SAVINGS_GOALS)

                    CategorySection(
                        title = "Savings & Goals",
                        categories = savingsGoals,
                        totalAmount = savingsGoalsTotal,
                        totalPercentage = savingsGoalsPercentage,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = responsivePadding)
                    )
                }

                // Review Plan Button
                item {
                    ReviewPlanButton(
                        onClick = { navController.navigate("monthly_summary") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = responsivePadding)
                    )
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
                        onClick = { showAddDialog = true },
                        modifier = Modifier
                            .size(64.dp)
                            .background(PrimaryRed),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add Category",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Bottom Sheet Dialog
            if (showAddDialog) {
                ModalBottomSheet(
                    onDismissRequest = { showAddDialog = false },
                    sheetState = sheetState,
                    containerColor = MaterialTheme.colorScheme.surface,
                    dragHandle = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Divider(
                                modifier = Modifier
                                    .width(40.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                            )
                        }
                    }
                ) {
                    AddBudgetCategorySheet(
                        viewModel = viewModel,
                        onDismiss = { showAddDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = responsivePadding, vertical = 24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetCategorySheet(
    viewModel: BudgetViewModel = viewModel(),
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var categoryName by remember { mutableStateOf("") }
    var categoryAmount by remember { mutableDoubleStateOf(0.0) }
    var selectedCategoryType by remember { mutableStateOf(CategoryType.LIVING_EXPENSES) }
    var selectedIcon by remember { mutableStateOf("home") }

    val monthlyBudget = viewModel.monthlyBudget.collectAsState().value
    val remainingAmount = viewModel.remainingAmount
    val amountInput = remember { mutableStateOf("") }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Add Budget Category",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Remaining budget info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = PrimaryRed.copy(alpha = 0.05f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Remaining Budget",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRed,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "Ksh ${formatAmount(remainingAmount)}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 18.sp
                    )
                }

                Text(
                    text = "${viewModel.allocationPercentage}% allocated",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }
        }

        // Category Name Field
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Category Name",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 12.sp
            )

            OutlinedTextField(
                value = categoryName,
                onValueChange = { categoryName = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "e.g., Entertainment, Utilities, Healthcare",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = PrimaryRed,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Amount Field
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Amount (Ksh)",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 12.sp
                )

                Text(
                    text = "${if (monthlyBudget > 0) {
                        ((categoryAmount / monthlyBudget) * 100).toInt()
                    } else 0}% of total budget",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }

            OutlinedTextField(
                value = amountInput.value,
                onValueChange = { newValue ->
                    amountInput.value = newValue.filter { it.isDigit() }
                    categoryAmount = amountInput.value.toDoubleOrNull() ?: 0.0
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                placeholder = {
                    Text(
                        text = "e.g., 5000",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Text(
                        text = "Ksh",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = PrimaryRed,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        // Category Type Selection
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Category Type",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 12.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CategoryType.entries.forEach { type ->
                    val isSelected = selectedCategoryType == type
                    val typeName = when (type) {
                        CategoryType.LIVING_EXPENSES -> "Living"
                        CategoryType.SAVINGS_GOALS -> "Savings"
                        CategoryType.OTHER -> "Other"
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) PrimaryRed.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedCategoryType = type }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = typeName,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) PrimaryRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Icon Selection
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Select Icon",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 12.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val icons = listOf(
                    "home" to Icons.Filled.Home,
                    "restaurant" to Icons.Filled.Restaurant,
                    "commute" to Icons.Filled.Commute,
                    "account_balance_wallet" to Icons.Filled.AccountBalanceWallet
                )

                icons.forEach { (iconName, icon) ->
                    val isSelected = selectedIcon == iconName
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) PrimaryRed.copy(alpha = 0.1f)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedIcon = iconName }
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = iconName,
                            tint = if (isSelected) PrimaryRed else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Add Button
        Button(
            onClick = {
                if (categoryName.isNotBlank() && categoryAmount > 0) {
                    viewModel.addCategory(categoryName, categoryAmount, selectedIcon, selectedCategoryType)
                    onDismiss()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryRed,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = categoryName.isNotBlank() && categoryAmount > 0 && categoryAmount <= remainingAmount
        ) {
            Text(
                text = "ADD CATEGORY",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        // Validation message
        if (categoryAmount > remainingAmount) {
            Text(
                text = "Amount exceeds remaining budget",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFDC2626),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun BudgetOverviewSection(
    monthlyBudget: Double,
    allocatedAmount: Double,
    remainingAmount: Double,
    allocationPercentage: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Budget Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Monthly Budget
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "MONTHLY BUDGET",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Ksh ${formatAmount(monthlyBudget)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 20.sp
                )
            }

            // Allocated
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "ALLOCATED",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Ksh ${formatAmount(allocatedAmount)}",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 20.sp
                )
            }
        }

        // Remaining Card
        Card(
            modifier = Modifier.fillMaxWidth(),
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
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "REMAINING TO ALLOCATE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp
                )
                Text(
                    text = "Ksh ${formatAmount(remainingAmount)}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryRed,
                    fontSize = 36.sp
                )
            }
        }

        // Allocation Progress
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header with percentage
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ALLOCATION STATUS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "$allocationPercentage%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryRed,
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
                        .fillMaxWidth(allocationPercentage.toFloat() / 100f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(PrimaryRed)
                )
            }

            // Status message
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ErrorOutline,
                    contentDescription = "Status",
                    tint = PrimaryRed,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Ksh ${formatAmount(remainingAmount)} left to assign — almost there.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun CategorySection(
    title: String,
    categories: List<com.example.pocket.model.BudgetCategory>,
    totalAmount: Double,
    totalPercentage: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp,
                letterSpacing = 1.sp
            )
            Text(
                text = "$totalPercentage% Total",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        }

        // Categories list
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                BudgetCategoryItem(
                    category = category,
                    onClick = { /* Navigate to category detail */ },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun BudgetCategoryItem(
    category: com.example.pocket.model.BudgetCategory,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
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
            // Left side: Icon and details
            Row(
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
                    Icon(
                        imageVector = getCategoryIcon(category.icon),
                        contentDescription = category.name,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Details
                Column {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "${category.percentage}% of total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }

            // Right side: Amount and chevron
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Ksh ${formatAmount(category.amount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp
                )
                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "View details",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun ReviewPlanButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier,
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "REVIEW PLAN SUMMARY",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                letterSpacing = 1.sp
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Review",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Helper functions
private fun getCategoryIcon(iconName: String): ImageVector {
    return when (iconName) {
        "home" -> Icons.Filled.Home
        "restaurant" -> Icons.Filled.Restaurant
        "commute" -> Icons.Filled.Commute
        "account_balance_wallet" -> Icons.Filled.AccountBalanceWallet
        else -> Icons.Filled.Home
    }
}

private fun formatAmount(amount: Double): String {
    return if (amount >= 1000) {
        "${(amount / 1000).roundToInt()}k"
    } else {
        amount.toInt().toString()
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun BudgetPlanningScreenPreview() {
    PocketTheme {
        BudgetPlanningScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun BudgetPlanningScreenPreviewDark() {
    PocketTheme(darkTheme = true) {
        BudgetPlanningScreen(navController = rememberNavController())
    }
}

@Preview(showBackground = true, widthDp = 411, heightDp = 600)
@Composable
fun AddBudgetCategorySheetPreview() {
    PocketTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            AddBudgetCategorySheet(
                onDismiss = {},
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}