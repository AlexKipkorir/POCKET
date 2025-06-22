package com.example.pocket.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DismissDirection
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.rememberDismissState
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocket.model.BillReminder
import com.example.pocket.viewmodels.BillReminderViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
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
    var editingBill by remember { mutableStateOf<BillReminder?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bill Reminders") },
                navigationIcon = {
                    IconButton(onClick = onBackToDashboard) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = "Add") },
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
            FilterDropdown(
                selectedFilter = state.selectedFilter,
                onFilterSelected = { viewModel.filterBills(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.bills.isEmpty()) {
                Text(
                    "No reminders yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                val groupedBills = groupBillsByDueDate(state.bills)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    groupedBills.forEach { (category, bills) ->
                        item {
                            Text(
                                text = category,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(
                            items = bills,
                            key = { it.id }
                        ) { bill ->
                            val dismissState = rememberDismissState(
                                confirmStateChange = { dismissValue ->
                                    if (dismissValue == DismissValue.DismissedToStart) {
                                        coroutineScope.launch {
                                            viewModel.deleteBill(bill)
                                            val result = snackbarHostState.showSnackbar(
                                                "Deleted '${bill.name}'", "Undo"
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                viewModel.undoDelete(context)
                                            }
                                        }
                                    }
                                    true
                                }
                            )

                            SwipeToDismiss(
                                state = dismissState,
                                background = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(70.dp)
                                            .background(Color.Red.copy(alpha = 0.3f)),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Text(
                                            "Delete",
                                            color = Color.White,
                                            modifier = Modifier.padding(end = 20.dp)
                                        )
                                    }
                                },
                                directions = setOf(DismissDirection.EndToStart),
                                dismissContent = {
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
                                        },
                                        onEdit = {
                                            editingBill = bill
                                            showDialog = true
                                        },
                                        onTogglePaid = { viewModel.togglePaidStatus(bill) },
                                        onToggleReminder = { viewModel.toggleReminder(bill) }
                                    )
                                }
                            )
                        }
                    }
                }
            }

            if (showDialog) {
                if (editingBill == null) {
                    AddBillDialog(
                        onDismiss = { showDialog = false },
                        onAdd = { name, amount, dueDate ->
                            viewModel.addBill(
                                context = context,
                                name = name,
                                amount = amount,
                                dueDate = Date.from(dueDate.atStartOfDay(ZoneId.systemDefault()).toInstant()),
                                onComplete = {
                                    showDialog = false
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Bill added!")
                                    }
                                }
                            )
                        },
                        context = context
                    )
                } else {
                    EditBillDialog(
                        bill = editingBill!!,
                        onDismiss = {
                            showDialog = false
                            editingBill = null
                        },
                        onSave = { name, amount, dueDate ->
                            viewModel.updateBill(
                                context = context,
                                billId = editingBill!!.id,
                                name = name,
                                amount = amount,
                                dueDate = dueDate
                            )
                            showDialog = false
                            editingBill = null
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Bill updated!")
                            }
                        },
                        context = context
                    )
                }
            }
        }
    }
}

fun groupBillsByDueDate(bills: List<BillReminder>): Map<String, List<BillReminder>> {
    val today = LocalDate.now()

    return bills.groupBy { bill ->
        val dueDate = bill.dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()

        when {
            dueDate.isBefore(today) -> "Overdue"
            dueDate.isBefore(today.plusDays(7)) -> "This Week"
            dueDate.isBefore(today.plusDays(30)) -> "This Month"
            else -> "Later"
        }
    }
}
@Composable
fun BillCard(
    bill: BillReminder,
    onDelete: () -> Unit,
    onEdit: () -> Unit,
    onTogglePaid: () -> Unit,
    onToggleReminder: () -> Unit
) {
    val dueDate = bill.dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
    val today = LocalDate.now()
    val daysRemaining = ChronoUnit.DAYS.between(today, dueDate)
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    val backgroundColor = when {
        bill.isPaid -> Color(0xFFB2DFDB) // Teal-ish for paid
        dueDate.isBefore(today) -> Color.Red.copy(alpha = 0.1f)
        daysRemaining <= 3 -> Color(0xFFFFF59D)
        else -> Color(0xFFC8E6C9)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = if (bill.isPaid) "✔ ${bill.name}" else bill.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                textDecoration = if (bill.isPaid) TextDecoration.LineThrough else TextDecoration.None
            )
            Text("Amount: Ksh ${"%.2f".format(bill.amount)}")
            Text("Due: ${dueDate.format(formatter)}")
            Text("Days Remaining: $daysRemaining")

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onEdit) {
                    Text("Edit")
                }
                TextButton(onClick = onTogglePaid) {
                    Text(if (bill.isPaid) "Mark Unpaid" else "Mark Paid")
                }
                TextButton(onClick = onToggleReminder) {
                    Text(if (bill.reminderEnabled) "Disable Reminder" else "Enable Reminder")
                }
            }
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
    context: Context,
    initialName: String? = null,
    initialAmount: String? = null,
    initialDate: LocalDate? = null
) {
    var name by remember { mutableStateOf(initialName ?: "") }
    var amount by remember { mutableStateOf(initialAmount ?: "") }
    var date by remember { mutableStateOf(initialDate) }
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

@Composable
fun EditBillDialog(
    bill: BillReminder,
    onDismiss: () -> Unit,
    onSave: (updatedName: String, updatedAmount: Double, updatedDate: LocalDate) -> Unit,
    context: Context
) {
    var name by remember { mutableStateOf(bill.name) }
    var amount by remember { mutableStateOf(bill.amount.toString()) }
    var date by remember {
        mutableStateOf(
            bill.dueDate.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        )
    }

    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Bill Reminder") },
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
                    }, date.year, date.monthValue - 1, date.dayOfMonth).show()
                }) {
                    Text(date.format(formatter))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amount.toDoubleOrNull()
                if (name.isNotBlank() && amt != null) {
                    onSave(name.trim(), amt, date)
                }
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
