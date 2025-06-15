package com.example.pocket.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocket.viewmodels.BudgetViewModel
import com.example.pocket.components.BudgetCategoryCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController


@ExperimentalMaterial3Api
@Composable
fun BudgetPlanningScreen(
    onNavigateToDashboard: () -> Unit,
    onBudgetSet: (Double) -> Unit,
    viewModel: BudgetViewModel = viewModel(),
    navController: NavController,
) {
    val context = LocalContext.current

    val userName by viewModel.userName.collectAsState()
    val budgetCategories by viewModel.budgetCategories.collectAsState()
    val plannedBudget by viewModel.plannedBudget.collectAsState()
    val isBudgetVisible by viewModel.isBudgetVisible.collectAsState()

    var budgetInput by remember { mutableStateOf(plannedBudget.toString()) }

    val totalBudget = budgetCategories.sumOf { it.amount }
    val isOverBudget = totalBudget > plannedBudget

    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadInitialData()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Planning") },
                actions = {
                    IconButton(onClick = { viewModel.toggleBudgetVisibility() }) {
                        Icon(
                            imageVector = if (isBudgetVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showAddDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Welcome, $userName", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                "Manage your expenses and stay on track with your finances.",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
            Text("Planned Budget", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = budgetInput,
                onValueChange = {
                    budgetInput = it
                    viewModel.updatePlannedBudget(it.toDoubleOrNull() ?: 0.0)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(fontSize = 30.sp)
            )

            Spacer(modifier = Modifier.height(20.dp))
            Text("Total Budget: Ksh ${"%.2f".format(totalBudget)}", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))

            if (plannedBudget > 0) {
                Text(
                    if (isOverBudget) "You have exceeded your Planned Budget!"
                    else "Your expenses are within the Planned Budget!",
                    color = if (isOverBudget) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.saveBudgetsToFirestore(onBudgetSet = onBudgetSet)
                    Toast.makeText(context, "Budgets saved successfully!", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text("Save Budget")
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { navController.navigate("summary") }
            ) {
                Text("View Budget Summary")
            }


            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onNavigateToDashboard
            ) {
                Text("Back to Dashboard")
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Budget Categories", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn {
                items(budgetCategories) { category ->
                    BudgetCategoryCard(
                        category = category,
                        viewModel = viewModel,
                        isBudgetVisible = isBudgetVisible,
                        plannedBudget = plannedBudget
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
            if (showAddDialog) {
                AddCategoryDialog(
                    onDismiss = { showAddDialog = false },
                    onAddCategory = { name, amount, icon ->
                        viewModel.addCategory(name, amount, icon)
                        showAddDialog = false
                    }
                )
            }

        }
    }

}

@Composable
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onAddCategory: (String, Double, String) -> Unit// name, amount, icon
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("🛒") }

    var nameError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val iconOptions = listOf("🛒", "🚗", "🏠", "🎮", "📚", "💡", "🍽️", "📱")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Budget Category") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = false
                    },
                    label = { Text("Category Name") },
                    isError = nameError,
                    singleLine = true
                )
                if (nameError) {
                    Text(
                        text = "Please enter a category name.",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it
                        amountError = false
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    label = { Text("Amount (Ksh)") },
                    isError = amountError,
                    singleLine = true
                )
                if (amountError) {
                    Text(
                        text = "Enter a valid amount (e.g. 2000)",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Select Icon", fontWeight = FontWeight.SemiBold)

                Row(modifier = Modifier.padding(top = 8.dp)) {
                    iconOptions.forEach { icon ->
                        Text(
                            text = icon,
                            fontSize = 28.sp,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .then(if (selectedIcon == icon) Modifier else Modifier)
                                .padding(4.dp)
                                .background(
                                    if (selectedIcon == icon)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    else
                                        Color.Transparent,
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(6.dp)
                                .clickable { selectedIcon = icon }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    nameError = name.isBlank()
                    amountError = amount.toDoubleOrNull() == null

                    if (!nameError && !amountError) {
                        onAddCategory(name.trim(), amount.toDouble(), selectedIcon)
                    }
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

