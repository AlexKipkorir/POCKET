package com.example.pocket.model

data class BillReminderUiState(
    val bills: List<BillReminder> = emptyList(),
    val selectedFilter: String = "All"
)