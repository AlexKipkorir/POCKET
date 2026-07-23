package com.example.pocket.ui.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalBottomSheet(
    onDismiss: () -> Unit,
    onAddGoal: (String, Int, String, String, Date?) -> Unit
) {
    var selectedTemplate by remember { mutableStateOf<GoalTemplate?>(null) }
    var step by remember { mutableStateOf(0) } // 0: Template, 1: Details

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
        ) {
            if (step == 0) {
                TemplateSelectionStep(
                    selectedTemplate = selectedTemplate,
                    onTemplateSelected = {
                        selectedTemplate = it
                        step = 1
                    },
                    onDismiss = onDismiss
                )
            } else {
                GoalDetailsStep(
                    template = selectedTemplate,
                    onAddGoal = { title, targetAmount, category, icon, targetDate ->
                        onAddGoal(title, targetAmount, category, icon, targetDate)
                    },
                    onBack = { step = 0 },
                    onDismiss = onDismiss
                )
            }
        }
    }
}

data class GoalTemplate(
    val id: String,
    val name: String,
    val icon: String,
    val description: String,
    val category: String,
    val defaultAmount: Int? = null,
    val defaultTimeframe: String? = null
)

@Composable
fun TemplateSelectionStep(
    selectedTemplate: GoalTemplate?,
    onTemplateSelected: (GoalTemplate) -> Unit,
    onDismiss: () -> Unit
) {
    val templates = listOf(
        GoalTemplate("1", "Home Down Payment", "🏠", "Your first home", "Home", 500000, "2 years"),
        GoalTemplate("2", "Dream Vacation", "✈️", "Explore the world", "Travel", 150000, "6 months"),
        GoalTemplate("3", "New Car", "🚗", "Freedom on wheels", "Transport", 800000, "1 year"),
        GoalTemplate("4", "Emergency Fund", "🏦", "Peace of mind", "Savings", 100000, "6 months"),
        GoalTemplate("5", "Education Fund", "📚", "Invest in knowledge", "Education", 200000, "2 years"),
        GoalTemplate("6", "Retirement", "🌅", "Golden years", "Savings", 1000000, "10 years"),
        GoalTemplate("7", "Custom Goal", "🎯", "Create your own", "Custom", null, null)
    )

    Column {
        Text(
            text = "Create a New Goal",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 22.sp
        )

        Text(
            text = "What are you saving for?",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.heightIn(max = 400.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(templates) { template ->
                TemplateItem(
                    template = template,
                    isSelected = selectedTemplate?.id == template.id,
                    onClick = { onTemplateSelected(template) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        ) {
            Text("Cancel")
        }
    }
}

@Composable
fun TemplateItem(
    template: GoalTemplate,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) Color(0xFFDB143C).copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) Color(0xFFDB143C) else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Color(0xFFDB143C).copy(alpha = 0.1f) else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = template.icon,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = template.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color(0xFFDB143C) else MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                )
                Text(
                    text = template.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )

                if (template.defaultAmount != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "KES ${String.format("%,d", template.defaultAmount)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        template.defaultTimeframe?.let {
                            Text(
                                text = "• $it",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Selected",
                    tint = Color(0xFFDB143C),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun GoalDetailsStep(
    template: GoalTemplate?,
    onAddGoal: (String, Int, String, String, Date?) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(template?.name ?: "") }
    var targetAmount by remember { mutableStateOf(template?.defaultAmount?.toString() ?: "") }
    var targetDate by remember { mutableStateOf<Date?>(null) }
    var category by remember { mutableStateOf(template?.category ?: "Custom") }
    var icon by remember { mutableStateOf(template?.icon ?: "🎯") }
    var showDatePicker by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Back"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Goal Details",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Close"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Goal Name
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Goal Name") },
            placeholder = { Text("e.g. Dream Vacation to Bali") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedIndicatorColor = Color(0xFFDB143C).copy(alpha = 0.5f),
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Target Amount
        OutlinedTextField(
            value = targetAmount,
            onValueChange = {
                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                    targetAmount = it
                }
            },
            label = { Text("Target Amount") },
            placeholder = { Text("KES 150,000") },
            leadingIcon = { Text("KES", fontSize = 14.sp) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedIndicatorColor = Color(0xFFDB143C).copy(alpha = 0.5f),
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Target Date
        OutlinedTextField(
            value = targetDate?.let { dateFormat.format(it) } ?: "",
            onValueChange = {},
            label = { Text("Target Date (optional)") },
            placeholder = { Text("Select date") },
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(
                        imageVector = Icons.Filled.CalendarToday,
                        contentDescription = "Pick Date"
                    )
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            readOnly = true,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedIndicatorColor = Color(0xFFDB143C).copy(alpha = 0.5f),
                unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Icon Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val icons = listOf("🎯", "✈️", "🏠", "🚗", "🏦", "📚", "🌅", "💍", "🎓", "🏥")
            icons.forEach { iconOption ->
                IconSelectorItem(
                    icon = iconOption,
                    isSelected = icon == iconOption,
                    onClick = { icon = iconOption }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val amountValue = targetAmount.toIntOrNull() ?: 0
                if (title.isNotBlank() && amountValue > 0) {
                    onAddGoal(title, amountValue, category, icon, targetDate)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFDB143C),
                contentColor = Color.White
            ),
            enabled = title.isNotBlank() && (targetAmount.toIntOrNull() ?: 0) > 0
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Create",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Create Goal",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun IconSelectorItem(
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(
                if (isSelected) Color(0xFFDB143C).copy(alpha = 0.1f)
                else Color.Transparent
            )
            .border(
                if (isSelected) 2.dp else 0.dp,
                Color(0xFFDB143C),
                CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = icon,
            fontSize = 20.sp
        )
    }
}