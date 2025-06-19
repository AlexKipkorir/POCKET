package com.example.pocket.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocket.model.FinancialGoal
import com.example.pocket.viewmodels.GoalsViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.UUID


@Composable
fun FinancialGoalsScreen(
    viewModel: GoalsViewModel,
    onBackToDashboard: () -> Unit
) {
    val goals by viewModel.goals.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(16.dp)
                .padding(paddingValues)
        ) {

            Text("Savings Goals", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
            Spacer(modifier = Modifier.height(8.dp))
            Text("Plan and monitor your savings to reach your financial targets.")
            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { showDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Add Goal", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                itemsIndexed(goals) { index, goal ->
                    GoalCard(
                        goal = goal,
                        onProgressUpdate = { id, newAmount ->
                            viewModel.updateGoalProgress(id, newAmount)
                        },
                        onDelete = {
                            val deleted = viewModel.deleteGoalWithUndo(index) { /* Optional: Confirm final delete */ }
                            deleted?.let { deletedGoal ->
                                coroutineScope.launch {
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Goal deleted",
                                        actionLabel = "Undo"
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.restoreGoal(deletedGoal)
                                    }
                                }
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onBackToDashboard,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Back to Dashboard", color = Color.White)
            }
        }
    }

    if (showDialog) {
        AddGoalDialog(
            onDismiss = { showDialog = false },
            onConfirm = {
                viewModel.addGoal(it.name, it.targetAmount)
                showDialog = false
            }
        )
    }
}

@Composable
fun GoalCard(
    goal: FinancialGoal,
    onProgressUpdate: (String, Double) -> Unit,
    onDelete: () -> Unit
) {
    var progressAmount by remember { mutableStateOf("") }

    val progress = if (goal.targetAmount == 0.0) 0f else (goal.currentAmount / goal.targetAmount).toFloat()
    val isComplete = progress >= 1f

    Card(
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    progress = progress,
                    modifier = Modifier.size(48.dp),
                    color = Color(0xFFD32F2F),
                    strokeWidth = 6.dp
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(goal.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Target: Ksh ${goal.targetAmount}")
                    Text("Saved: Ksh ${goal.currentAmount}")
                }
                if (isComplete) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Completed",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = progressAmount,
                onValueChange = { progressAmount = it },
                label = { Text("Add Progress") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(4.dp))
            Button(
                onClick = {
                    val amount = progressAmount.toDoubleOrNull() ?: 0.0
                    if (amount > 0.0) onProgressUpdate(goal.id, amount)
                    progressAmount = ""
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
            ) {
                Text("Update Progress", color = Color.White)
            }
        }
    }
}

@Composable
fun AddGoalDialog(onDismiss: () -> Unit, onConfirm: (FinancialGoal) -> Unit) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Savings Goal") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Goal Name") })
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Target Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                if (error.isNotEmpty()) Text(error, color = Color.Red, fontSize = 12.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isBlank() || amount.toDoubleOrNull() == null) {
                    error = "Please enter valid values"
                } else {
                    onConfirm(
                        FinancialGoal(
                            id = UUID.randomUUID().toString(),
                            name = name.trim(),
                            targetAmount = amount.toDouble()
                        )
                    )
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun PreviewFinancialGoalsScreen() {
    val sampleGoals = listOf(
        FinancialGoal("1", "Buy Laptop", 35000.0, 10000.0),
        FinancialGoal("2", "Vacation", 50000.0, 25000.0)
    )
    val goalsFlow = MutableStateFlow(sampleGoals)
    val goals by goalsFlow.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        goals.forEach {
            GoalCard(
                goal = it,
                onProgressUpdate = { _, _ -> },
                onDelete = {}
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}


