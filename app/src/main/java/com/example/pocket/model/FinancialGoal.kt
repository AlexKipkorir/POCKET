package com.example.pocket.model

import java.util.UUID

data class FinancialGoal(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double = 0.0
)
