package com.example.pocket.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var userName by remember { mutableStateOf("User") }
    var totalBalance by remember { mutableStateOf(0.0) }
    var monthlyIncome by remember { mutableStateOf(0.0) }
    var monthlyExpenses by remember { mutableStateOf(0.0) }
    var showEditDialog by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dashboard") },
                actions = {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        scope.launch { drawerState.open() }
                    }) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
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
            DashboardCard("Monthly Income", monthlyIncome, Icons.Default.TrendingUp)
            DashboardCard("Monthly Expenses", monthlyExpenses, Icons.Default.TrendingDown)

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
                        )
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
            leadingContent = { Icon(Icons.Default.ArrowForward, contentDescription = null) }
        )
    }
}

@Composable
fun DrawerContent(navController: NavController) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Navigation Menu", fontSize = 20.sp, color = Color.Blue)
        Spacer(modifier = Modifier.height(16.dp))
        DrawerItem("About Us", Icons.Default.Info) { navController.navigate("about_us") }
        DrawerItem("Services", Icons.Default.Build) { navController.navigate("services") }
        DrawerItem("Expense Tracker", Icons.Default.MonetizationOn) { navController.navigate("expense_tracker") }
        DrawerItem("Budget Planning", Icons.Default.AccountBalanceWallet) { navController.navigate("budget_planning") }
        DrawerItem("Financial Reports", Icons.Default.Assessment) { navController.navigate("financial_reports") }
        DrawerItem("Financial Goals", Icons.Default.Savings) { navController.navigate("financial_goals") }
        DrawerItem("Bill Reminders", Icons.Default.Notifications) { navController.navigate("bill_reminders") }
        DrawerItem("Investment Tracking", Icons.Default.TrendingUp) { navController.navigate("investment_tracking") }
        DrawerItem("Logout", Icons.Default.Logout) {
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
