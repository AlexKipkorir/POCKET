package com.example.pocket.ui.screens.goals

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.pocket.model.GoalTemplate
import com.example.pocket.model.GoalDetailsFormState
import com.example.pocket.model.DraftMilestone
import com.example.pocket.model.GoalTemplates
import com.example.pocket.viewmodels.GoalsViewModel

private sealed class CreateGoalStep {
    data object PickTemplate : CreateGoalStep()
    data class Details(val template: GoalTemplate) : CreateGoalStep()
    data class Milestones(
        val template: GoalTemplate,
        val details: GoalDetailsFormState,
        val milestones: List<DraftMilestone> = emptyList()
    ) : CreateGoalStep()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGoalFlow(
    viewModel: GoalsViewModel,
    onDismiss: () -> Unit,
) {
    var step by remember { mutableStateOf<CreateGoalStep>(CreateGoalStep.PickTemplate) }

    when (val current = step) {
        is CreateGoalStep.PickTemplate -> {
            CreateGoalTemplateSheet(
                onDismiss = onDismiss,
                onContinue = { template ->
                    step = CreateGoalStep.Details(template)
                },
                templates = GoalTemplates.all
            )
        }

        is CreateGoalStep.Details -> {
            GoalDetailsScreen(
                template = current.template,
                onBack = { step = CreateGoalStep.PickTemplate },
                onCreateGoal = { formState ->
                    val targetAmount = formState.targetAmount.toIntOrNull() ?: 0
                    val milestones = viewModel.generateMilestones(targetAmount, formState.name)
                    step = CreateGoalStep.Milestones(current.template, formState, milestones)
                }
            )
        }

        is CreateGoalStep.Milestones -> {
            GoalMilestonesScreen(
                goalTitle = current.details.name,
                goalTargetLabel = "${current.details.targetAmount.ifBlank { "0" }} ${current.details.name}",
                projectedCompletionLabel = current.details.targetDate.ifBlank { "—" },
                monthlyCommitLabel = calculateMonthlyCommit(
                    current.details.targetAmount.toIntOrNull() ?: 0,
                    current.details.targetDate
                ),
                milestones = current.milestones.ifEmpty {
                    viewModel.generateMilestones(
                        current.details.targetAmount.toIntOrNull() ?: 0,
                        current.details.name
                    )
                },
                onClose = onDismiss,
                onSkip = {
                    viewModel.createGoalFromTemplate(current.template, current.details, current.milestones)
                    onDismiss()
                },
                onCompleteSetup = {
                    viewModel.createGoalFromTemplate(current.template, current.details, current.milestones)
                    onDismiss()
                }
            )
        }
    }
}

private fun calculateMonthlyCommit(targetAmount: Int, targetDate: String): String {
    return if (targetAmount > 0) {
        // Simple calculation - in production, use proper date parsing
        val monthly = targetAmount / 12
        "KES ${String.format("%,d", monthly)}"
    } else {
        "KES 0"
    }
}