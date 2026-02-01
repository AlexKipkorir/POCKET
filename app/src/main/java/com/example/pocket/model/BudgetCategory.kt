package com.example.pocket.model

data class BudgetCategory(
    val id: String,
    val name: String,
    val amount: Double,
    val icon: String,
    val categoryType: CategoryType,
    val percentage: Int = 0
)

enum class CategoryType {
    LIVING_EXPENSES, SAVINGS_GOALS, OTHER
}