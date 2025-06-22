package com.example.pocket.model

import java.util.Date
import java.util.UUID

data class Investment(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amount: Double,
    val performance: Double,
    val goalAmount: Double? = null,
    val maturityDate: Date? = null,
    val sourceUrl: String? = null,
    val reminderEnabled: Boolean = false
)

