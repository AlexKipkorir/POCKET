package com.example.pocket.model

import java.util.Date
import java.util.UUID

data class BillReminder(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amount: Double,
    val dueDate: Date,
    val isPaid: Boolean = false,
    val reminderEnabled: Boolean = true
)
