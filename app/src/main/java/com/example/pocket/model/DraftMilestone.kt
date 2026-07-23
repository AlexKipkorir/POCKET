package com.example.pocket.model

enum class MilestoneState { COMPLETED, CURRENT, UPCOMING, FINAL }

data class DraftMilestone(
    val percent: Int, // 25, 50, 75, 100
    val amountLabel: String, // "Ksh 37,500"
    val description: String,
    val state: MilestoneState,
)

object SampleMilestones {
    val emergencyFund = listOf(
        DraftMilestone(25, "Ksh 37,500", "\"The Safety Net Start\" - 1 month of basic expenses covered.", MilestoneState.COMPLETED),
        DraftMilestone(50, "Ksh 75,000", "Halfway there! Stability is becoming a reality.", MilestoneState.CURRENT),
        DraftMilestone(75, "Ksh 112,500", "The final stretch. Total peace of mind is close.", MilestoneState.UPCOMING),
        DraftMilestone(100, "Ksh 150,000", "", MilestoneState.FINAL),
    )
}