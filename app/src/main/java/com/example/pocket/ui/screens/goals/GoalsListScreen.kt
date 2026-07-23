package com.example.pocket.ui.screens.goals

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pocket.ui.screens.goals.components.ActiveGoalRow
import com.example.pocket.ui.screens.goals.components.CompletedGoalRow
import com.example.pocket.ui.screens.goals.components.GoalsTopBar
import com.example.pocket.ui.screens.goals.components.StatItem
import com.example.pocket.ui.theme.PocketType
import com.example.pocket.ui.theme.Primary
import com.example.pocket.ui.theme.Spacing
import com.example.pocket.viewmodels.GoalsTab
import com.example.pocket.viewmodels.GoalsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialGoalsScreen(
    navController: NavController,
    viewModel: GoalsViewModel = viewModel(),
    onBackToDashboard: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            GoalsTopBar(
                activeGoalsCount = uiState.totalActive,
                completePercent = uiState.overallProgress,
                onMenuClick = { /* TODO: Open drawer or navigate back */ },
                onSettingsClick = { /* TODO: Show filter options */ }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateGoalFlow() },
                containerColor = Primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .padding(bottom = Spacing.md)
                    .size(56.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add goal", modifier = Modifier.size(28.dp))
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Quick Stats Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.marginMobile, vertical = Spacing.sm),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                StatItem(
                    label = "ACTIVE",
                    value = "${uiState.totalActive}",
                    valueColor = Primary,
                    minWidth = 100.dp
                )
                StatItem(
                    label = "ACHIEVED",
                    value = "${uiState.overallProgress}%",
                    valueColor = Primary,
                    minWidth = 120.dp
                )
                StatItem(
                    label = "TARGET",
                    value = "${(uiState.totalTarget / 1000).toInt()}k",
                    valueColor = MaterialTheme.colorScheme.onSurface,
                    suffix = "KES",
                    minWidth = 120.dp
                )
                StatItem(
                    label = "SAVED",
                    value = "${(uiState.totalSaved / 1000).toInt()}k",
                    valueColor = Primary,
                    suffix = "KES",
                    minWidth = 120.dp
                )
            }

            // Filter Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.marginMobile),
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                FilterChip(
                    label = "All",
                    isSelected = uiState.selectedTab == GoalsTab.ALL,
                    onClick = { viewModel.setTab(GoalsTab.ALL) }
                )
                FilterChip(
                    label = "Active",
                    isSelected = uiState.selectedTab == GoalsTab.ACTIVE,
                    onClick = { viewModel.setTab(GoalsTab.ACTIVE) }
                )
                FilterChip(
                    label = "Completed",
                    isSelected = uiState.selectedTab == GoalsTab.COMPLETED,
                    onClick = { viewModel.setTab(GoalsTab.COMPLETED) }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xs))

            // Goals List
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.filteredGoals.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("No goals yet", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tap the + button to create your first goal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = Spacing.marginMobile, vertical = 8.dp)
                ) {
                    val activeGoals = uiState.filteredGoals.filter { !it.isCompleted }
                    val completedGoals = uiState.filteredGoals.filter { it.isCompleted }

                    if (activeGoals.isNotEmpty()) {
                        itemsIndexed(activeGoals) { index, goal ->
                            // First active goal gets the "hero" thicker progress bar, matching the design.
                            ActiveGoalRow(
                                goal = goal,
                                barHeight = if (index == 0) 16.dp else 8.dp,
                                onClick = {
                                    // Navigate to goal detail screen instead of showing bottom sheet
                                    navController.navigate("goal_detail/${goal.id}")
                                }
                            )
                            if (index < activeGoals.size - 1) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }

                    if (completedGoals.isNotEmpty()) {
                        item {
                            Text(
                                text = "Completed Goals",
                                style = PocketType.categoryLabel,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.padding(top = Spacing.lg, bottom = Spacing.xs)
                            )
                        }
                        itemsIndexed(completedGoals) { index, goal ->
                            CompletedGoalRow(
                                goal = goal,
                                onClick = {
                                    navController.navigate("goal_detail/${goal.id}")
                                }
                            )
                            if (index < completedGoals.size - 1) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // REMOVED: Goal Detail Bottom Sheet - now using full screen navigation

    // Create Goal Flow
    if (uiState.showCreateGoalFlow) {
        CreateGoalFlow(
            viewModel = viewModel,
            onDismiss = { viewModel.dismissCreateGoalFlow() }
        )
    }
}

@Composable
fun FilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(50),
        color = if (isSelected) Primary else MaterialTheme.colorScheme.surface,
        border = if (isSelected) null else
            androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = Spacing.sm, vertical = Spacing.xs)
        )
    }
}