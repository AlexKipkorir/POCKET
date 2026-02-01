package com.example.pocket.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocket.model.AllocationStatus
import com.example.pocket.model.BudgetCategory
import com.example.pocket.model.BudgetSummary
import com.example.pocket.model.CategoryBreakdown
import com.example.pocket.model.CategoryType
import com.example.pocket.model.Insight
import com.example.pocket.model.InsightType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BudgetViewModel : ViewModel() {

    private val _monthlyBudget = MutableStateFlow(42000.0)
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    private val _budgetCategories = MutableStateFlow<List<BudgetCategory>>(emptyList())
    val budgetCategories: StateFlow<List<BudgetCategory>> = _budgetCategories.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            // Simulate loading
            kotlinx.coroutines.delay(500)

            // Initialize with sample data
            _budgetCategories.value = listOf(
                BudgetCategory(
                    id = "1",
                    name = "Housing",
                    amount = 14700.0,
                    icon = "home",
                    categoryType = CategoryType.LIVING_EXPENSES,
                    percentage = 35
                ),
                BudgetCategory(
                    id = "2",
                    name = "Food & Dining",
                    amount = 6300.0,
                    icon = "restaurant",
                    categoryType = CategoryType.LIVING_EXPENSES,
                    percentage = 15
                ),
                BudgetCategory(
                    id = "3",
                    name = "Transport",
                    amount = 4200.0,
                    icon = "commute",
                    categoryType = CategoryType.LIVING_EXPENSES,
                    percentage = 10
                ),
                BudgetCategory(
                    id = "4",
                    name = "Emergency Fund",
                    amount = 10080.0,
                    icon = "account_balance_wallet",
                    categoryType = CategoryType.SAVINGS_GOALS,
                    percentage = 24
                )
            )

            _isLoading.value = false
        }
    }

    fun updateMonthlyBudget(newBudget: Double) {
        _monthlyBudget.value = newBudget
        // Recalculate percentages based on new total
        recalculatePercentages()
    }

    fun updateCategoryAmount(categoryId: String, newAmount: Double) {
        _budgetCategories.update { categories ->
            categories.map { category ->
                if (category.id == categoryId) {
                    category.copy(amount = newAmount)
                } else {
                    category
                }
            }
        }
        recalculatePercentages()
    }

    fun addCategory(name: String, amount: Double, icon: String, categoryType: CategoryType) {
        viewModelScope.launch {
            val newCategory = BudgetCategory(
                id = (System.currentTimeMillis()).toString(),
                name = name,
                amount = amount,
                icon = icon,
                categoryType = categoryType,
                percentage = calculatePercentage(amount)
            )

            _budgetCategories.update { it + newCategory }
            recalculatePercentages()
        }
    }

    fun deleteCategory(categoryId: String) {
        _budgetCategories.update { categories ->
            categories.filterNot { it.id == categoryId }
        }
        recalculatePercentages()
    }

    private fun recalculatePercentages() {
        _budgetCategories.update { categories ->
            val total = _monthlyBudget.value
            categories.map { category ->
                val percentage = if (total > 0) {
                    ((category.amount / total) * 100).toInt()
                } else {
                    0
                }
                category.copy(percentage = percentage)
            }
        }
    }

    private fun calculatePercentage(amount: Double): Int {
        val total = _monthlyBudget.value
        return if (total > 0) {
            ((amount / total) * 100).toInt()
        } else {
            0
        }
    }

    val allocatedAmount: Double
        get() = _budgetCategories.value.sumOf { it.amount }

    val remainingAmount: Double
        get() = _monthlyBudget.value - allocatedAmount

    val allocationPercentage: Int
        get() = if (_monthlyBudget.value > 0) {
            ((allocatedAmount / _monthlyBudget.value) * 100).toInt()
        } else {
            0
        }

    fun getCategoriesByType(type: CategoryType): List<BudgetCategory> {
        return _budgetCategories.value.filter { it.categoryType == type }
    }

    fun getTotalByType(type: CategoryType): Double {
        return getCategoriesByType(type).sumOf { it.amount }
    }

    fun getPercentageByType(type: CategoryType): Int {
        val totalByType = getTotalByType(type)
        return if (_monthlyBudget.value > 0) {
            ((totalByType / _monthlyBudget.value) * 100).toInt()
        } else {
            0
        }
    }
}

val BudgetViewModel.budgetSummary: BudgetSummary
    get() {
        val monthlyBudget = monthlyBudget.value
        val allocatedAmount = allocatedAmount
        val remainingAmount = remainingAmount

        val allocationStatus = when {
            remainingAmount == 0.0 -> AllocationStatus.BALANCED
            remainingAmount > 0.0 -> AllocationStatus.UNDER_ALLOCATED
            else -> AllocationStatus.OVER_ALLOCATED
        }

        val categoryBreakdown = CategoryType.entries.map { type ->
            val totalByType = getTotalByType(type)
            val percentage = if (monthlyBudget > 0) {
                (totalByType / monthlyBudget) * 100
            } else {
                0.0
            }

            // Define recommended ranges based on category type
            val recommendedRange = when (type) {
                CategoryType.LIVING_EXPENSES -> 30.0..50.0
                CategoryType.SAVINGS_GOALS -> 10.0..30.0
                CategoryType.OTHER -> 10.0..30.0
            }

            CategoryBreakdown(
                categoryType = type,
                allocatedAmount = totalByType,
                percentage = percentage,
                recommendedRange = recommendedRange
            )
        }

        val insights = listOf(
            Insight(
                type = InsightType.STRENGTH,
                title = "Strength",
                message = "Housing takes ${getCategoriesByType(CategoryType.LIVING_EXPENSES)
                    .firstOrNull { it.name.contains("Housing", ignoreCase = true) }?.percentage ?: 0}% of your budget. This is within the recommended threshold.",
                icon = "verified_user"
            ),
            Insight(
                type = InsightType.SUGGESTION,
                title = "Suggestion",
                message = "Your savings rate of ${categoryBreakdown.firstOrNull { it.categoryType == CategoryType.SAVINGS_GOALS }?.percentage?.toInt() ?: 0}% will help you reach your goals 2 months faster.",
                icon = "lightbulb"
            )
        )

        return BudgetSummary(
            totalPlanned = monthlyBudget,
            totalAllocated = allocatedAmount,
            remainingAmount = remainingAmount,
            allocationStatus = allocationStatus,
            categoryBreakdown = categoryBreakdown,
            insights = insights
        )
    }