package com.example.pocket.model

data class ExpenseCategory(
    val transport: Double = 0.0,
    val food: Double = 0.0,
    val shopping: Double = 0.0,
    val otherCosts: Double = 0.0
)

data class ExpensesData(
    val categories: ExpenseCategory = ExpenseCategory(),
    val customCategories: Map<String, Double> = emptyMap()
)

