package com.example.pocket.model

import java.util.Date
import java.util.UUID

data class BillReminder(
    val id: String = "",
    val name: String = "",
    val amount: Double = 0.0,
    val dueDate: Date = Date(),
    val isPaid: Boolean = false,
    val reminderEnabled: Boolean = true,
    val recurrence: String = "ONE_TIME",
    val reminderDaysBefore: Int = 1
)
