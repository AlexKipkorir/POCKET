package com.example.pocket.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import com.example.pocket.model.BudgetCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BudgetViewModel : ViewModel() {

    private val _userName = MutableStateFlow("User")
    val userName: StateFlow<String> = _userName

    private val _budgetCategories = MutableStateFlow<List<BudgetCategory>>(emptyList())
    val budgetCategories: StateFlow<List<BudgetCategory>> = _budgetCategories

    private val _plannedBudget = MutableStateFlow(0.0)
    val plannedBudget: StateFlow<Double> = _plannedBudget

    private val _isBudgetVisible = MutableStateFlow(true)
    val isBudgetVisible: StateFlow<Boolean> = _isBudgetVisible

    fun loadInitialData() {
        _userName.value = "John Doe"
        _plannedBudget.value = 20000.0
        _budgetCategories.value = listOf(
            BudgetCategory("Food", 5000.0),
            BudgetCategory("Rent", 10000.0),
            BudgetCategory("Transport", 2000.0)
        )
    }
    fun toggleBudgetVisibility() {
        _isBudgetVisible.value = !_isBudgetVisible.value
    }

    fun showAddCategoryDialog(context: Context) {
        val newCategory = BudgetCategory("New Category", 0.0)
        _budgetCategories.value = _budgetCategories.value + newCategory
    }

    fun updatePlannedBudget(amount: Double) {
        _plannedBudget.value = amount
    }
    fun updateCategoryAmount(name: String, newAmount: Double) {
        _budgetCategories.value - _budgetCategories.value.map {
            if (it.name == name) it.copy(amount = newAmount) else it
        }
    }
    fun deleteCategory(category: BudgetCategory) {
        _budgetCategories.value = _budgetCategories.value.filterNot { it.name == category.name }
    }
    fun saveBudgetsToFirestore(onBudgetSet: (Double) -> Unit) {
        onBudgetSet(_plannedBudget.value)
    }
    fun addCategory(name: String, amount: Double, icon: String) {
        val newCategory = BudgetCategory(name, amount, icon)
        _budgetCategories.value = _budgetCategories.value + newCategory
    }
}