package com.example.pocket.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class ActivityItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: String,
    val timestamp: Date,
    val amount: Double? = null,
    val category: String? = null
)

enum class ActivityFilter {
    ALL, TODAY, YESTERDAY, THIS_WEEK, THIS_MONTH
}

data class ActivityUiState(
    val activities: List<ActivityItem> = emptyList(),
    val filteredActivities: List<ActivityItem> = emptyList(),
    val selectedFilter: ActivityFilter = ActivityFilter.ALL,
    val isLoading: Boolean = true,
    val showFilterMenu: Boolean = false
)

class ActivityViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    var uiState by mutableStateOf(ActivityUiState())
        private set

    init {
        loadActivities()
    }

    private fun loadActivities() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)

            try {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    // TODO: Load real data from Firestore
                    // For now, use sample data
                    loadSampleActivities()
                } else {
                    loadSampleActivities()
                }
            } catch (e: Exception) {
                loadSampleActivities()
            }

            applyFilter(uiState.selectedFilter)
            uiState = uiState.copy(isLoading = false)
        }
    }

    private fun loadSampleActivities() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Today
        calendar.set(Calendar.HOUR_OF_DAY, 10)
        calendar.set(Calendar.MINUTE, 30)

        val activities = mutableListOf<ActivityItem>()

        // Today's activities
        activities.add(
            ActivityItem(
                id = "1",
                title = "New Expense Created",
                description = "Category: Dining Out",
                icon = "add",
                timestamp = calendar.time,
                amount = 4500.0,
                category = "Food"
            )
        )

        calendar.set(Calendar.HOUR_OF_DAY, 14)
        calendar.set(Calendar.MINUTE, 45)
        activities.add(
            ActivityItem(
                id = "2",
                title = "Budget Modified",
                description = "Entertainment set to KES 2,000",
                icon = "edit",
                timestamp = calendar.time,
                amount = 2000.0,
                category = "Entertainment"
            )
        )

        // Yesterday
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        activities.add(
            ActivityItem(
                id = "3",
                title = "Smart Alert Triggered",
                description = "Spending is 15% higher than usual",
                icon = "notifications",
                timestamp = calendar.time
            )
        )

        calendar.set(Calendar.HOUR_OF_DAY, 15)
        calendar.set(Calendar.MINUTE, 30)
        activities.add(
            ActivityItem(
                id = "4",
                title = "Category Removed",
                description = "\"Misc Projects\" was deleted",
                icon = "delete",
                timestamp = calendar.time
            )
        )

        // Earlier this week
        calendar.add(Calendar.DAY_OF_YEAR, -2)
        calendar.set(Calendar.HOUR_OF_DAY, 11)
        calendar.set(Calendar.MINUTE, 15)
        activities.add(
            ActivityItem(
                id = "5",
                title = "Bank Connection Synced",
                description = "42 new transactions imported",
                icon = "sync",
                timestamp = calendar.time
            )
        )

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 16)
        calendar.set(Calendar.MINUTE, 45)
        activities.add(
            ActivityItem(
                id = "6",
                title = "Security Preferences Updated",
                description = "Two-factor authentication enabled",
                icon = "settings",
                timestamp = calendar.time
            )
        )

        // Sort by timestamp descending (newest first)
        uiState = uiState.copy(
            activities = activities.sortedByDescending { it.timestamp }
        )
    }

    fun setFilter(filter: ActivityFilter) {
        uiState = uiState.copy(
            selectedFilter = filter,
            showFilterMenu = false
        )
        applyFilter(filter)
    }

    private fun applyFilter(filter: ActivityFilter) {
        val calendar = Calendar.getInstance()
        val today = calendar.time

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        val yesterdayStart = calendar.time

        calendar.time = today
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val weekAgo = calendar.time

        calendar.time = today
        calendar.add(Calendar.MONTH, -1)
        val monthAgo = calendar.time

        val filtered = when (filter) {
            ActivityFilter.ALL -> uiState.activities
            ActivityFilter.TODAY -> uiState.activities.filter {
                it.timestamp.after(getStartOfDay(today))
            }
            ActivityFilter.YESTERDAY -> uiState.activities.filter {
                it.timestamp.after(getStartOfDay(yesterdayStart)) &&
                        it.timestamp.before(getStartOfDay(today))
            }
            ActivityFilter.THIS_WEEK -> uiState.activities.filter {
                it.timestamp.after(weekAgo)
            }
            ActivityFilter.THIS_MONTH -> uiState.activities.filter {
                it.timestamp.after(monthAgo)
            }
        }

        uiState = uiState.copy(filteredActivities = filtered)
    }

    private fun getStartOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    fun toggleFilterMenu() {
        uiState = uiState.copy(showFilterMenu = !uiState.showFilterMenu)
    }

    fun refresh() {
        loadActivities()
    }
}