package com.example.pocket.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocket.model.FinancialGoal
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GoalsViewModel : ViewModel() {
    //Internal mutable list of goals
    private val _goals = MutableStateFlow<List<FinancialGoal>>(emptyList())
    val goals: StateFlow<List<FinancialGoal>> = _goals

    init {
        loadFakeGoals()
    }

    private fun loadFakeGoals() {
        val sampleGoals = listOf(
            FinancialGoal(name = "Buy Laptop", targetAmount = 35000.0, currentAmount = 10000.0),
            FinancialGoal(name = "Emergency Fund", targetAmount = 50000.0, currentAmount = 15000.0),
            FinancialGoal(name = "Vacation", targetAmount = 30000.0, currentAmount = 30000.0),
        )
        _goals.value = sampleGoals
    }

    fun addGoal(name: String, targetAmount: Double) {
        val newGoal = FinancialGoal(name = name, targetAmount = targetAmount, currentAmount = 0.0)
        _goals.value = _goals.value + newGoal
    }

    fun updateGoalProgress(id: String, amountToAdd: Double) {
        _goals.value = _goals.value.map {
            if (it.id == id) it.copy(currentAmount = it.currentAmount + amountToAdd)
            else it
        }
    }


    fun deleteGoalWithUndo(index: Int, onUndoTimeout: () -> Unit): FinancialGoal? {
        val currentGoals = _goals.value.toMutableList()
        return if (index in currentGoals.indices) {
            val deletedGoal = currentGoals.removeAt(index)
            _goals.value = currentGoals
            viewModelScope.launch {
                delay(5000) // 5 seconds before confirming deletion
                if (!_goals.value.contains(deletedGoal)) {
                    onUndoTimeout()
                }
            }
            deletedGoal
        } else null
    }

    fun restoreGoal(goal: FinancialGoal) {
        _goals.value = _goals.value + goal
    }
}
