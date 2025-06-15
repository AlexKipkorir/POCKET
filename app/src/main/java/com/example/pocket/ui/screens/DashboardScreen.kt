package com.example.pocket.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    var userName by remember { mutableStateOf("User") }
    var totalBalance by remember { mutableDoubleStateOf(0.0) }
    var monthlyIncome by remember { mutableDoubleStateOf(0.0) }
    var monthlyExpenses by remember { mutableDoubleStateOf(0.0) }
    var showEditDialog by remember { mutableStateOf(false) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Load user financial data
    LaunchedEffect(user?.uid) {
        user?.let {
            val doc = firestore.collection("users").document(it.uid).get().await()
            if (doc.exists()) {
                userName = doc.getString("name") ?: "User"
                totalBalance = doc.getDouble("totalBalance") ?: 0.0
                monthlyIncome = doc.getDouble("monthlyIncome") ?: 0.0
                monthlyExpenses = doc.getDouble("monthlyExpenses") ?: 0.0
            }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(navController)
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Dashboard") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Open drawer")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Overview")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text("Welcome, $userName!", fontSize = 24.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Your financial overview at a glance:", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))

                DashboardCard("Total Balance", totalBalance, Icons.Default.AttachMoney)
                DashboardCard("Monthly Income", monthlyIncome, Icons.AutoMirrored.Filled.TrendingUp)
                DashboardCard("Monthly Expenses", monthlyExpenses,
                    Icons.AutoMirrored.Filled.TrendingDown
                )

                Spacer(modifier = Modifier.height(24.dp))

                DashboardSection("Budget Planning", "Tap to set or view budgets") {
                    navController.navigate("budget_planning")
                }
                DashboardSection("Bill Reminders", "View upcoming bills") {
                    navController.navigate("bill_reminders")
                }
                DashboardSection("Financial Goals", "Manage your savings goals") {
                    navController.navigate("financial_goals")
                }
                DashboardSection("Investment Tracking", "Track your investments") {
                    navController.navigate("investment_tracking")
                }
            }
        }
    }

    // Edit financial overview dialog
    if (showEditDialog) {
        EditFinancialOverviewDialog(
            currentBalance = totalBalance,
            currentIncome = monthlyIncome,
            currentExpenses = monthlyExpenses,
            onDismiss = { showEditDialog = false },
            onSave = { newBalance, newIncome, newExpenses ->
                user?.let {
                    firestore.collection("users").document(it.uid).set(
                        mapOf(
                            "totalBalance" to newBalance,
                            "monthlyIncome" to newIncome,
                            "monthlyExpenses" to newExpenses
                        ),
                        SetOptions.merge()
                    )
                }
                totalBalance = newBalance
                monthlyIncome = newIncome
                monthlyExpenses = newExpenses
                showEditDialog = false
            }
        )
    }
}

@Composable
fun DashboardCard(title: String, value: Double, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { Text("Ksh$value") },
            leadingContent = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
        )
    }
}

@Composable
fun DashboardSection(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        onClick = onClick
    ) {
        ListItem(
            headlineContent = { Text(title) },
            supportingContent = { Text(subtitle) },
            leadingContent = { Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null) }
        )
    }
}

@Composable
fun DrawerContent(navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Navigation Menu", fontSize = 20.sp, color = Color.White)
        Spacer(modifier = Modifier.height(16.dp))
        DrawerItem("About Us", Icons.Default.Info) { navController.navigate("about_us") }
        DrawerItem("Services", Icons.Default.Build) { navController.navigate("services") }
        DrawerItem("Expense Tracker", Icons.Default.MonetizationOn) { navController.navigate("expense_tracker") }
        DrawerItem("Budget Planning", Icons.Default.AccountBalanceWallet) { navController.navigate("budget_planning") }
        DrawerItem("Financial Reports", Icons.Default.Assessment) { navController.navigate("financial_reports") }
        DrawerItem("Financial Goals", Icons.Default.Savings) { navController.navigate("financial_goals") }
        DrawerItem("Bill Reminders", Icons.Default.Notifications) { navController.navigate("bill_reminders") }
        DrawerItem("Investment Tracking", Icons.AutoMirrored.Filled.TrendingUp) { navController.navigate("investment_tracking") }
        DrawerItem("Logout", Icons.AutoMirrored.Filled.Logout) {
            FirebaseAuth.getInstance().signOut()
            navController.navigate("login") { popUpTo(0) }
        }
    }
}

@Composable
fun DrawerItem(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    ListItem(
        leadingContent = { Icon(icon, contentDescription = null) },
        headlineContent = { Text(title) },
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun EditFinancialOverviewDialog(
    currentBalance: Double,
    currentIncome: Double,
    currentExpenses: Double,
    onDismiss: () -> Unit,
    onSave: (Double, Double, Double) -> Unit
) {
    var balance by remember { mutableStateOf(currentBalance.toString()) }
    var income by remember { mutableStateOf(currentIncome.toString()) }
    var expenses by remember { mutableStateOf(currentExpenses.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Financial Overview") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = balance, onValueChange = { balance = it }, label = { Text("Total Balance") })
                OutlinedTextField(value = income, onValueChange = { income = it }, label = { Text("Monthly Income") })
                OutlinedTextField(value = expenses, onValueChange = { expenses = it }, label = { Text("Monthly Expenses") })
            }
        },
        confirmButton = {
            Button(onClick = {
                val b = balance.toDoubleOrNull()
                val i = income.toDoubleOrNull()
                val e = expenses.toDoubleOrNull()
                if (b != null && i != null && e != null) {
                    onSave(b, i, e)
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
