package com.example.pocket.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocket.model.BudgetCategory
import com.example.pocket.viewmodels.BudgetViewModel
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator

@Composable
fun BudgetCategoryCard(
    category: BudgetCategory,
    viewModel: BudgetViewModel,
    isBudgetVisible: Boolean,
    plannedBudget: Double
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var editedAmount by remember { mutableStateOf(category.amount.toString()) }
    var visible by remember { mutableStateOf(true) }

    val colorMap = mapOf(
        "Food" to Color(0xFFE57373),
        "Transport" to Color(0xFF64B5F6),
        "Rent" to Color(0xFF81C784),
        "Entertainment" to Color(0xFFFFB74D),
        "Other" to Color(0xFFBA68C8)
    )

    val categoryColor = colorMap[category.name] ?: MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(Unit) {
                    detectHorizontalDragGestures { _, dragAmount ->
                        if (dragAmount < -50) {
                            visible = false
                            viewModel.deleteCategory(category)
                        }
                    }
                },
            colors = CardDefaults.cardColors(containerColor = categoryColor)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    val animatedProgress by animateFloatAsState(
                        targetValue = category.amount.toFloat() / (plannedBudget.toFloat().takeIf { it > 0 } ?: 1f),
                        label = "ProgressAnim"
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = animatedProgress,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "${category.icon} ${category.name}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    AnimatedVisibility(visible = isBudgetVisible) {
                        Text(
                            "Ksh ${"%.2f".format(category.amount)}",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Row {
                    IconButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Amount")
                    }
                    IconButton(onClick = {
                        visible = false
                        viewModel.deleteCategory(category)
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Category")
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Amount for ${category.name}") },
            text = {
                OutlinedTextField(
                    value = editedAmount,
                    onValueChange = { editedAmount = it },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                    label = { Text("New Amount (Ksh)") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val amount = editedAmount.toDoubleOrNull()
                        if (amount != null) {
                            viewModel.updateCategoryAmount(category.name, amount)
                            showEditDialog = false
                        }
                    },
                    enabled = editedAmount.toDoubleOrNull() != null
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

