package com.example.pocket.ui.screens.goals

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocket.model.GoalTemplate
import com.example.pocket.model.GoalDetailsFormState
import com.example.pocket.ui.screens.goals.components.CategoryOption
import com.example.pocket.ui.screens.goals.components.CategoryPill
import com.example.pocket.ui.screens.goals.components.StepProgressDashes
import com.example.pocket.ui.screens.goals.components.UnderlineTextField
import com.example.pocket.ui.theme.PocketType
import com.example.pocket.ui.theme.Radius
import com.example.pocket.ui.theme.Spacing
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailsScreen(
    template: GoalTemplate,
    onBack: () -> Unit,
    onHelp: () -> Unit = {},
    onCreateGoal: (GoalDetailsFormState) -> Unit,
    estimatedMonthlyPlan: String = "KES 2,500",
    categoryOptions: List<CategoryOption> = listOf(
        CategoryOption("✈️", "Travel"),
        CategoryOption("🏠", "Home"),
        CategoryOption("🛡️", "Rainy Day"),
        CategoryOption("📚", "Education"),
        CategoryOption("🚗", "Car"),
        CategoryOption("🌅", "Retirement"),
        CategoryOption("💍", "Wedding"),
        CategoryOption("🎯", "Other"),
    ),
) {
    var name by remember { mutableStateOf(template.title + (if (template.id == "vacation") " to Bali" else "")) }
    var targetAmount by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(template.defaultCategory) }
    var description by remember { mutableStateOf("") }
    var autopay by remember { mutableStateOf(true) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surface,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                    .padding(horizontal = Spacing.marginMobile),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "Goal Details",
                    style = PocketType.goalTitleMobile,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onHelp) {
                    Icon(
                        Icons.Filled.HelpOutline,
                        contentDescription = "Help",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                    .padding(horizontal = Spacing.marginMobile, vertical = Spacing.sm),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = autopay,
                        onCheckedChange = { autopay = it },
                        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                    )
                    Text(
                        text = "Enable Auto-pay from Main Wallet",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Button(
                    onClick = {
                        onCreateGoal(
                            GoalDetailsFormState(
                                name = name,
                                targetAmount = targetAmount,
                                targetDate = targetDate,
                                category = selectedCategory,
                                description = description,
                                autopayEnabled = autopay,
                            ),
                        )
                    },
                    shape = RoundedCornerShape(4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(top = Spacing.sm),
                    enabled = name.isNotBlank() && targetAmount.isNotBlank()
                ) {
                    Text(text = "Create Goal", fontWeight = FontWeight.Bold)
                    Icon(
                        Icons.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.padding(start = Spacing.xs)
                    )
                }
            }
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState()),
        ) {
            // Hero — real photo if the template has one, otherwise a soft gradient
            Box(modifier = Modifier.fillMaxWidth().height(192.dp)) {
                if (template.heroImageUrl != null) {
                    AsyncImage(
                        model = template.heroImageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        template.accentColor.copy(alpha = 0.35f),
                                        template.accentColor.copy(alpha = 0.08f)
                                    ),
                                ),
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = template.emoji, fontSize = 64.sp)
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color.Transparent, MaterialTheme.colorScheme.surface),
                            ),
                        ),
                )
            }

            Column(modifier = Modifier.padding(horizontal = Spacing.marginMobile).padding(top = Spacing.xs))  {
                StepProgressDashes(step = 2, total = 3, modifier = Modifier.padding(bottom = Spacing.md))

                // Goal name + icon
                Row(
                    verticalAlignment = Alignment.Bottom,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.xs),
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(text = template.emoji, fontSize = 22.sp)
                    }
                    Spacer(modifier = Modifier.padding(start = Spacing.xs))
                    UnderlineTextField(
                        label = "GOAL NAME",
                        value = name,
                        onValueChange = { name = it },
                        placeholder = "Enter name...",
                        modifier = Modifier.weight(1f).padding(start = Spacing.gutter),
                    )
                }

                // Amount + date grid
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.gutter),
                ) {
                    UnderlineTextField(
                        label = "TARGET AMOUNT",
                        value = targetAmount,
                        onValueChange = { targetAmount = it },
                        placeholder = "0.00",
                        textStyle = PocketType.goalTitle,
                        leadingContent = {
                            Text(
                                text = "KES ",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                    UnderlineTextField(
                        label = "TARGET DATE",
                        value = targetDate,
                        onValueChange = { targetDate = it },
                        placeholder = "Select date",
                        textStyle = PocketType.goalTitle,
                        leadingContent = {
                            Icon(
                                Icons.Filled.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp).padding(end = Spacing.xs),
                            )
                        },
                        modifier = Modifier.weight(1f),
                    )
                }

                // Category pills
                Column(modifier = Modifier.padding(top = Spacing.md)) {
                    Text(
                        text = "CATEGORY",
                        style = PocketType.categoryLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = Spacing.sm),
                    )
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    ) {
                        categoryOptions.forEach { option ->
                            CategoryPill(
                                option = option,
                                selected = selectedCategory == option.label,
                                onClick = { selectedCategory = option.label },
                            )
                        }
                    }
                }

                // Description
                Column(modifier = Modifier.padding(top = Spacing.md)) {
                    UnderlineTextField(
                        label = "DESCRIPTION (OPTIONAL)",
                        value = description,
                        onValueChange = { description = it },
                        placeholder = "Add a note to yourself...",
                        textStyle = PocketType.bodyMain,
                    )
                }

                // Estimated plan card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.md)
                        .clip(RoundedCornerShape(Radius.xl))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), RoundedCornerShape(Radius.xl))
                        .padding(Spacing.md),
                ) {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Column {
                                Text(
                                    text = "ESTIMATED PLAN",
                                    style = PocketType.categoryLabel,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = estimatedMonthlyPlan,
                                        style = PocketType.progressDisplayMobile,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = " / month",
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                                    .padding(Spacing.xs),
                            ) {
                                Icon(
                                    Icons.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Text(
                            text = "Based on your target date and current balance. We recommend setting up auto-save.",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = Spacing.sm),
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }
}