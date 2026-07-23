package com.example.pocket.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocket.model.DraftMilestone
import com.example.pocket.model.Goal
import com.example.pocket.model.GoalDetailsFormState
import com.example.pocket.model.GoalTemplate
import com.example.pocket.model.Milestone
import com.example.pocket.model.MilestoneState
import com.example.pocket.model.SampleGoals
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*


data class GoalsUiState(
    val goals: List<Goal> = emptyList(),
    val filteredGoals: List<Goal> = emptyList(),
    val activeGoals: List<Goal> = emptyList(),
    val completedGoals: List<Goal> = emptyList(),
    val isLoading: Boolean = true,
    val selectedGoal: Goal? = null,
    val showGoalDetail: Boolean = false,
    val showCreateGoalFlow: Boolean = false,
    val totalActive: Int = 0,
    val totalCompleted: Int = 0,
    val totalTarget: Double = 0.0,
    val totalSaved: Double = 0.0,
    val overallProgress: Int = 0,
    val selectedTab: GoalsTab = GoalsTab.ALL
)

enum class GoalsTab {
    ALL, ACTIVE, COMPLETED
}

class GoalsViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(GoalsUiState())
    val uiState = _uiState.asStateFlow()

    private var allGoals: List<Goal> = emptyList()

    init {
        loadGoals()
    }

    private fun loadGoals() {
        val userId = auth.currentUser?.uid ?: run {
            loadSampleData()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                firestore.collection("goals")
                    .whereEqualTo("userId", userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                            loadSampleData()
                            return@addSnapshotListener
                        }

                        snapshot?.let {
                            val goals = it.documents.mapNotNull { doc ->
                                try {
                                    val data = doc.data ?: return@mapNotNull null
                                    Goal(
                                        id = doc.id,
                                        title = data["title"] as? String ?: "",
                                        category = data["category"] as? String ?: "",
                                        categoryIcon = data["categoryIcon"] as? String ?: "home",
                                        currentAmount = (data["currentAmount"] as? Number)?.toInt() ?: 0,
                                        targetAmount = (data["targetAmount"] as? Number)?.toInt() ?: 1,
                                        currency = data["currency"] as? String ?: "KES",
                                        progressPercent = (data["progressPercent"] as? Number)?.toInt() ?: 0,
                                        monthsRemaining = (data["monthsRemaining"] as? Number)?.toInt(),
                                        isCompleted = data["isCompleted"] as? Boolean ?: false,
                                        statusLabel = data["statusLabel"] as? String,
                                        nextMilestoneLabel = data["nextMilestoneLabel"] as? String,
                                        createdLabel = data["createdLabel"] as? String,
                                        targetDateLabel = data["targetDateLabel"] as? String,
                                        milestones = (data["milestones"] as? List<Map<String, Any>>)?.mapNotNull { milestoneData ->
                                            try {
                                                Milestone(
                                                    label = milestoneData["label"] as? String ?: "",
                                                    dateOrStatus = milestoneData["dateOrStatus"] as? String ?: "",
                                                    isReached = milestoneData["isReached"] as? Boolean ?: false
                                                )
                                            } catch (e: Exception) {
                                                null
                                            }
                                        } ?: emptyList()
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            allGoals = goals
                            updateUiState()
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                loadSampleData()
            }
        }
    }

    private fun loadSampleData() {
        allGoals = SampleGoals.all
        updateUiState()
        _uiState.value = _uiState.value.copy(isLoading = false)
    }

    private fun updateUiState() {
        val active = allGoals.filter { !it.isCompleted }
        val completed = allGoals.filter { it.isCompleted }
        val totalTarget = active.sumOf { it.targetAmount.toDouble() }
        val totalSaved = active.sumOf { it.currentAmount.toDouble() }
        val overallProgress = if (totalTarget > 0) ((totalSaved / totalTarget) * 100).toInt() else 0

        _uiState.value = _uiState.value.copy(
            goals = allGoals,
            filteredGoals = allGoals,
            activeGoals = active,
            completedGoals = completed,
            totalActive = active.size,
            totalCompleted = completed.size,
            totalTarget = totalTarget,
            totalSaved = totalSaved,
            overallProgress = overallProgress
        )
        applyFilter(_uiState.value.selectedTab)
    }

    fun setTab(tab: GoalsTab) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
        applyFilter(tab)
    }

    private fun applyFilter(tab: GoalsTab) {
        val filtered = when (tab) {
            GoalsTab.ALL -> allGoals
            GoalsTab.ACTIVE -> allGoals.filter { !it.isCompleted }
            GoalsTab.COMPLETED -> allGoals.filter { it.isCompleted }
        }
        _uiState.value = _uiState.value.copy(filteredGoals = filtered)
    }

    fun selectGoal(goal: Goal) {
        _uiState.value = _uiState.value.copy(
            selectedGoal = goal,
            showGoalDetail = true
        )
    }

    fun dismissGoalDetail() {
        _uiState.value = _uiState.value.copy(
            selectedGoal = null,
            showGoalDetail = false
        )
    }

    fun showCreateGoalFlow() {
        _uiState.value = _uiState.value.copy(showCreateGoalFlow = true)
    }

    fun dismissCreateGoalFlow() {
        _uiState.value = _uiState.value.copy(showCreateGoalFlow = false)
    }

    fun createGoalFromTemplate(
        template: GoalTemplate,
        details: GoalDetailsFormState,
        milestones: List<DraftMilestone> = emptyList()
    ) {
        val userId = auth.currentUser?.uid ?: run {
            // For demo, add to sample data
            val targetAmount = details.targetAmount.toIntOrNull() ?: 0
            val newGoal = Goal(
                id = UUID.randomUUID().toString(),
                title = details.name.ifBlank { template.title },
                category = details.category.ifBlank { template.defaultCategory },
                categoryIcon = when (details.category.lowercase()) {
                    "travel" -> "flight"
                    "home" -> "home"
                    "car" -> "car"
                    "rainy day" -> "verified"
                    else -> "home"
                },
                currentAmount = 0,
                targetAmount = targetAmount,
                progressPercent = 0,
                createdLabel = formatDate(Date()),
                targetDateLabel = details.targetDate.ifBlank { null },
                milestones = milestones.map { milestone ->
                    Milestone(
                        label = "${milestone.percent}% - ${milestone.amountLabel}",
                        dateOrStatus = milestone.description.ifBlank {
                            if (milestone.state == MilestoneState.COMPLETED) "Achieved" else "Next Milestone"
                        },
                        isReached = milestone.state == MilestoneState.COMPLETED
                    )
                }
            )
            allGoals = allGoals + newGoal
            updateUiState()
            dismissCreateGoalFlow()
            return
        }

        val targetAmount = details.targetAmount.toIntOrNull() ?: 0

        val goal = hashMapOf(
            "userId" to userId,
            "title" to details.name.ifBlank { template.title },
            "targetAmount" to targetAmount,
            "currentAmount" to 0,
            "category" to details.category.ifBlank { template.defaultCategory },
            "categoryIcon" to when (details.category.lowercase()) {
                "travel" -> "flight"
                "home" -> "home"
                "car" -> "car"
                "rainy day" -> "verified"
                else -> "home"
            },
            "currency" to "KES",
            "progressPercent" to 0,
            "isCompleted" to false,
            "createdLabel" to formatDate(Date()),
            "targetDateLabel" to details.targetDate.ifBlank { null },
//            "createdAt" to com.google.firebase.firestore.Timestamp.now()
        )

        viewModelScope.launch {
            try {
                val docRef = firestore.collection("goals").add(goal).await()
                val goalId = docRef.id

                // Create milestones
                milestones.forEach { milestone ->
                    val milestoneMap = hashMapOf(
                        "goalId" to goalId,
                        "label" to "${milestone.percent}% - ${milestone.amountLabel}",
                        "dateOrStatus" to milestone.description.ifBlank {
                            if (milestone.state == MilestoneState.COMPLETED) "Achieved" else "Next Milestone"
                        },
                        "isReached" to (milestone.state == MilestoneState.COMPLETED)
                    )
                    firestore.collection("goalMilestones").add(milestoneMap).await()
                }

                loadGoals()
                dismissCreateGoalFlow()
            } catch (e: Exception) {
                // Fallback to local
                val newGoal = Goal(
                    id = UUID.randomUUID().toString(),
                    title = details.name.ifBlank { template.title },
                    category = details.category.ifBlank { template.defaultCategory },
                    categoryIcon = "home",
                    currentAmount = 0,
                    targetAmount = targetAmount,
                    progressPercent = 0,
                    createdLabel = formatDate(Date()),
                    targetDateLabel = details.targetDate.ifBlank { null },
                    milestones = milestones.map { milestone ->
                        Milestone(
                            label = "${milestone.percent}% - ${milestone.amountLabel}",
                            dateOrStatus = milestone.description.ifBlank {
                                if (milestone.state == MilestoneState.COMPLETED) "Achieved" else "Next Milestone"
                            },
                            isReached = milestone.state == MilestoneState.COMPLETED
                        )
                    }
                )
                allGoals = allGoals + newGoal
                updateUiState()
                dismissCreateGoalFlow()
            }
        }
    }

    fun addContribution(goalId: String, amount: Int) {
        viewModelScope.launch {
            try {
                val goalRef = firestore.collection("goals").document(goalId)
                val goal = goalRef.get().await()
                val currentAmount = (goal.getDouble("currentAmount") ?: 0.0).toInt()
                val newAmount = currentAmount + amount
                val targetAmount = (goal.getDouble("targetAmount") ?: 1.0).toInt()
                val newProgress = ((newAmount.toDouble() / targetAmount) * 100).toInt()

                goalRef.update(
                    "currentAmount", newAmount,
                    "progressPercent", newProgress
                ).await()

                loadGoals()
            } catch (e: Exception) {
                // Fallback to local update
                val updatedGoals = allGoals.map { goal ->
                    if (goal.id == goalId) {
                        val newAmount = goal.currentAmount + amount
                        val newProgress = ((newAmount.toDouble() / goal.targetAmount) * 100).toInt()
                        goal.copy(
                            currentAmount = newAmount,
                            progressPercent = newProgress
                        )
                    } else {
                        goal
                    }
                }
                allGoals = updatedGoals
                updateUiState()
            }
        }
    }

    fun deleteGoal(goalId: String) {
        viewModelScope.launch {
            try {
                firestore.collection("goals").document(goalId).delete().await()
                loadGoals()
            } catch (e: Exception) {
                allGoals = allGoals.filter { it.id != goalId }
                updateUiState()
            }
        }
    }

    fun toggleGoalCompletion(goalId: String) {
        viewModelScope.launch {
            try {
                val goal = allGoals.find { it.id == goalId }
                if (goal != null) {
                    firestore.collection("goals").document(goalId)
                        .update("isCompleted", !goal.isCompleted)
                        .await()
                    loadGoals()
                }
            } catch (e: Exception) {
                val updatedGoals = allGoals.map { goal ->
                    if (goal.id == goalId) {
                        goal.copy(isCompleted = !goal.isCompleted)
                    } else {
                        goal
                    }
                }
                allGoals = updatedGoals
                updateUiState()
            }
        }
    }

    fun updateGoal(goalId: String, title: String, targetAmount: Int, targetDate: Date?) {
        viewModelScope.launch {
            try {
                val updates = mutableMapOf<String, Any>()
                updates["title"] = title
                updates["targetAmount"] = targetAmount
                targetDate?.let {
                    updates["targetDateLabel"] = formatDate(it)
                }

                firestore.collection("goals").document(goalId)
                    .update(updates)
                    .await()
                loadGoals()
            } catch (e: Exception) {
                val updatedGoals = allGoals.map { goal ->
                    if (goal.id == goalId) {
                        goal.copy(
                            title = title,
                            targetAmount = targetAmount,
                            targetDateLabel = targetDate?.let { formatDate(it) }
                        )
                    } else {
                        goal
                    }
                }
                allGoals = updatedGoals
                updateUiState()
            }
        }
    }

    private fun formatDate(date: Date): String {
        val format = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        return format.format(date)
    }

    fun formatCurrency(amount: Int): String {
        return "KES ${String.format("%,d", amount)}"
    }

    fun getProgressColor(progress: Int): androidx.compose.ui.graphics.Color {
        return when {
            progress >= 75 -> androidx.compose.ui.graphics.Color(0xFF22C55E)
            progress >= 50 -> androidx.compose.ui.graphics.Color(0xFFF59E0B)
            else -> com.example.pocket.ui.theme.PrimaryRed
        }
    }

    fun togglePauseGoal(goalId: String, paused: Boolean) {
        viewModelScope.launch {
            try {
                firestore.collection("goals").document(goalId)
                    .update("isPaused", paused)
                    .await()
                loadGoals()
            } catch (e: Exception) {
                // Fallback to local update
                val updatedGoals = allGoals.map { goal ->
                    if (goal.id == goalId) {
                        goal.copy(isPaused = paused)
                    } else {
                        goal
                    }
                }
                allGoals = updatedGoals
                updateUiState()
            }
        }
    }

    fun generateMilestones(targetAmount: Int, goalTitle: String): List<DraftMilestone> {
        val percentages = listOf(25, 50, 75, 100)
        val descriptions = listOf(
            "The Safety Net Start - 1 month of basic expenses covered.",
            "Halfway there! Stability is becoming a reality.",
            "The final stretch. Total peace of mind is close.",
            "" // Goal complete
        )

        return percentages.mapIndexed { index, percent ->
            val amount = (targetAmount * percent / 100)
            DraftMilestone(
                percent = percent,
                amountLabel = "KES ${String.format("%,d", amount)}",
                description = descriptions.getOrElse(index) { "" },
                state = if (percent == 25) MilestoneState.COMPLETED else MilestoneState.UPCOMING
            )
        }.toMutableList().apply {
            // Set current milestone
            if (size > 1) {
                this[1] = this[1].copy(state = MilestoneState.CURRENT)
            }
            // Set final milestone
            if (last().percent == 100) {
                this[lastIndex] = this[lastIndex].copy(state = MilestoneState.FINAL)
            }
        }
    }
}