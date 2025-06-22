package com.example.pocket.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocket.viewmodels.ExpenseTrackerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseTrackerScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: ExpenseTrackerViewModel = viewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var newCustomCategory by remember { mutableStateOf("") }

    val customExpenses = viewModel.customExpenses

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Tracker") },
                navigationIcon = {
                    androidx.compose.material.IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White // Make the back icon white
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Categorize Your Expenses", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))

            ExpenseInputField("Transport", viewModel.transport) { viewModel.transport = it }
            ExpenseInputField("Food", viewModel.food) { viewModel.food = it }
            ExpenseInputField("Shopping", viewModel.shopping) { viewModel.shopping = it }
            ExpenseInputField("Other Costs", viewModel.otherCosts) { viewModel.otherCosts = it }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Custom Expenses", style = MaterialTheme.typography.titleMedium)

            customExpenses.forEach { (category, value) ->
                OutlinedTextField(
                    value = value,
                    onValueChange = { viewModel.updateCustomExpense(category, it) },
                    label = { Text(category) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(viewModel.customExpenses.entries.toList()) { entry ->
                    val category = entry.key
                    val value = entry.value

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedTextField(
                            value = value,
                            onValueChange = { viewModel.updateCustomExpense(category, it) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { viewModel.removeCustomExpense(category) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))


            Button(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Custom Expense")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    viewModel.saveExpenses {
                        Toast.makeText(context, "Expenses saved!", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save Expenses")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onNavigateToDashboard,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Go To Dashboard")
            }


            if (showDialog) {
                AlertDialog(
                    onDismissRequest = {
                        showDialog = false
                        newCustomCategory = ""
                    },
                    title = { Text("Add Custom Expense") },
                    text = {
                        OutlinedTextField(
                            value = newCustomCategory,
                            onValueChange = { newCustomCategory = it },
                            label = { Text("Category Name") }
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            if (newCustomCategory.isNotBlank()) {
                                viewModel.addCustomExpense(newCustomCategory.trim())
                                newCustomCategory = ""
                                showDialog = false
                            }
                        }) {
                            Text("Add")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDialog = false
                            newCustomCategory = ""
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun ExpenseInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    )
}
