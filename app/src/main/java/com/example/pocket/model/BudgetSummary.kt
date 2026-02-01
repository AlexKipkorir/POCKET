package com.example.pocket.model

data class BudgetSummary(
    val totalPlanned: Double,
    val totalAllocated: Double,
    val remainingAmount: Double,
    val allocationStatus: AllocationStatus,
    val categoryBreakdown: List<CategoryBreakdown>,
    val insights: List<Insight>
)

enum class AllocationStatus {
    BALANCED, UNDER_ALLOCATED, OVER_ALLOCATED
}

data class CategoryBreakdown(
    val categoryType: CategoryType,
    val allocatedAmount: Double,
    val percentage: Double,
    val recommendedRange: ClosedRange<Double> // percentage range
)

data class Insight(
    val type: InsightType,
    val title: String,
    val message: String,
    val icon: String
)

enum class InsightType {
    STRENGTH, SUGGESTION, WARNING
}

