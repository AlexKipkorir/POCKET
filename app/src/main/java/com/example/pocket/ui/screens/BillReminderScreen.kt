package com.example.pocket.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocket.viewmodels.BillReminder
import com.example.pocket.viewmodels.BillReminderViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillReminderScreen(
    viewModel: BillReminderViewModel = viewModel(),
    onBackToDashboard: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bill Reminders") },
                navigationIcon = {
                    IconButton(onClick = onBackToDashboard) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Dashboard"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add Reminder") },
                text = { Text("Add Bill") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
        ) {

            // Filter Dropdown
            FilterDropdown(
                selectedFilter = state.selectedFilter,
                onFilterSelected = { viewModel.filterBills(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // List of Bill Cards with animation
            AnimatedVisibility(
                visible = state.bills.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.bills, key = { it.id }) { bill ->
                        val billToDelete = bill
                        BillCard(
                            bill = billToDelete,
                            onDelete = {
                                coroutineScope.launch {
                                    viewModel.deleteBill(billToDelete)
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Deleted '${billToDelete.name}'",
                                        actionLabel = "Undo"
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.undoDelete(context)
                                    }
                                }
                            }
                        )
                    }
                }
            }

            // Empty state text
            if (state.bills.isNotEmpty()) {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(), exit = fadeOut()
                ) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.bills, key = { it.id }) { bill ->
                            BillCard(
                                bill = bill,
                                onDelete = {
                                    coroutineScope.launch {
                                        viewModel.deleteBill(bill)
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Deleted '${bill.name}'",
                                            actionLabel = "Undo"
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.undoDelete(context)
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "No reminders yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    textAlign = TextAlign.Center
                )
            }


            // Add Dialog
            if (showDialog) {
                AddBillDialog(
                    onDismiss = { showDialog = false },
                    onAdd = { name, amount, dueDate ->
                        viewModel.addBill(
                            context = context,
                            name = name,
                            amount = amount,
                            dueDate = dueDate,
                            onComplete = {
                                showDialog = false
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Reminder added!")
                                }
                            }
                        )
                    },
                    context = context
                )
            }
        }
    }
}


@Composable
fun BillCard(bill: BillReminder, onDelete: () -> Unit) {
    val today = LocalDate.now()
    val dueDate = bill.dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val daysRemaining = ChronoUnit.DAYS.between(today, dueDate)
    val backgroundColor = when {
        dueDate.isBefore(today) -> Color.Red.copy(alpha = 0.1f)
        daysRemaining <= 3 -> Color(0xFFFFF59D) // Yellow-ish for near-due
        else -> Color(0xFFC8E6C9) // Green-ish for safe
    }

    val cardEnter = remember(bill.id) { Animatable(0f) }

    LaunchedEffect(bill.id) {
        cardEnter.animateTo(1f, animationSpec = tween(500, easing = FastOutSlowInEasing))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = cardEnter.value
                scaleY = cardEnter.value
                alpha = cardEnter.value
            }
            .background(backgroundColor)
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = bill.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Amount: Ksh ${"%.2f".format(bill.amount)}")
            Text("Due: ${dueDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}")
            Text("Days remaining: $daysRemaining")
        }
    }
}



@Composable
fun FilterDropdown(selectedFilter: String, onFilterSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
        OutlinedButton(onClick = { expanded = true }) {
            Text("Filter: $selectedFilter")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            listOf("All", "This Week", "This Month").forEach { filter ->
                DropdownMenuItem(
                    text = { Text(filter) },
                    onClick = {
                        onFilterSelected(filter)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun AddBillDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double, LocalDate) -> Unit,
    context: Context
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf<LocalDate?>(null) }
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Bill Reminder") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Bill Name") })
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedButton(onClick = {
                    val today = LocalDate.now()
                    DatePickerDialog(context, { _, y, m, d ->
                        date = LocalDate.of(y, m + 1, d)
                    }, today.year, today.monthValue - 1, today.dayOfMonth).show()
                }) {
                    Text(date?.format(formatter) ?: "Pick Due Date")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amount.toDoubleOrNull()
                if (name.isNotBlank() && amt != null && date != null) {
                    onAdd(name.trim(), amt, date!!)
                }
            }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
