package com.example.pocket.model

data class Goal(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val categoryIcon: String = "home",
    val currentAmount: Int = 0,
    val targetAmount: Int = 1,
    val currency: String = "KES",
    val progressPercent: Int = 0,
    val monthsRemaining: Int? = null,
    val isCompleted: Boolean = false,
    val statusLabel: String? = null,
    val nextMilestoneLabel: String? = null,
    val createdLabel: String? = null,
    val targetDateLabel: String? = null,
    val milestones: List<Milestone> = emptyList(),
    val isPaused: Boolean = false
)

data class Milestone(
    val label: String = "",
    val dateOrStatus: String = "",
    val isReached: Boolean = false,
)

data class GoalDetailsFormState(
    val name: String = "",
    val targetAmount: String = "",
    val targetDate: String = "",
    val category: String = "",
    val description: String = "",
    val autopayEnabled: Boolean = false,
)

/** Sample data matching the goals screenshots */
object SampleGoals {
    val dreamVacation = Goal(
        id = "1",
        title = "Dream Vacation",
        category = "TRAVEL",
        categoryIcon = "flight",
        currentAmount = 52_000,
        targetAmount = 80_000,
        progressPercent = 65,
        monthsRemaining = 5,
        nextMilestoneLabel = "NEXT: KES 10K TO 75%",
        createdLabel = "Jan 2025",
        targetDateLabel = "Dec 2025",
        milestones = listOf(
            Milestone("25% Achieved", "Feb 12, 2025", isReached = true),
            Milestone("50% Achieved", "April 05, 2025", isReached = true),
            Milestone("75% - KES 60,000", "Next Milestone", isReached = false),
        ),
    )

    val homeFund = Goal(
        id = "2",
        title = "Home Fund",
        category = "SAVINGS",
        categoryIcon = "home",
        currentAmount = 90_000,
        targetAmount = 100_000,
        progressPercent = 90,
        monthsRemaining = 1,
        statusLabel = "ON TRACK!",
        createdLabel = "Jan 2025",
        targetDateLabel = "Mar 2025",
    )

    val emergencyFund = Goal(
        id = "3",
        title = "Emergency Fund",
        category = "SAVINGS",
        categoryIcon = "verified",
        currentAmount = 50_000,
        targetAmount = 50_000,
        progressPercent = 100,
        monthsRemaining = null,
        isCompleted = true,
        statusLabel = "ARCHIVED",
        createdLabel = "Jan 2025",
        targetDateLabel = "Mar 2025",
    )

    val all = listOf(dreamVacation, homeFund, emergencyFund)
}