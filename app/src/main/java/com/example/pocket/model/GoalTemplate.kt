package com.example.pocket.model


import androidx.compose.ui.graphics.Color

/**
 * One selectable option in the "Create a New Goal" template list (step 1).
 */
data class GoalTemplate(
    val id: String,
    val emoji: String,
    val title: String,
    val subtitle: String,
    val accentColor: Color,
    val heroImageUrl: String? = null,
    val defaultCategory: String,
)

object GoalTemplates {
    val homeDownPayment = GoalTemplate(
        id = "home",
        emoji = "🏠",
        title = "Home Down Payment",
        subtitle = "Brick by brick toward your own keys.",
        accentColor = Color(0xFF006E2F),
        defaultCategory = "Home",
    )

    val dreamVacation = GoalTemplate(
        id = "vacation",
        emoji = "✈️",
        title = "Dream Vacation",
        subtitle = "Collect memories, not just currency.",
        accentColor = Color(0xFFB61722),
        heroImageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800&h=400&fit=crop",
        defaultCategory = "Travel",
    )

    val newCar = GoalTemplate(
        id = "car",
        emoji = "🚗",
        title = "New Car",
        subtitle = "Upgrade your daily commute with ease.",
        accentColor = Color(0xFF825100),
        defaultCategory = "Car",
    )

    val emergencyFund = GoalTemplate(
        id = "emergency",
        emoji = "🏦",
        title = "Emergency Fund",
        subtitle = "Build a safety net for life's surprises.",
        accentColor = Color(0xFFBA1A1A),
        defaultCategory = "Rainy Day",
    )

    val educationFund = GoalTemplate(
        id = "education",
        emoji = "📚",
        title = "Education Fund",
        subtitle = "Invest in knowledge and future growth.",
        accentColor = Color(0xFF006E2F),
        defaultCategory = "Education",
    )

    val retirement = GoalTemplate(
        id = "retirement",
        emoji = "🌅",
        title = "Retirement",
        subtitle = "Securing your golden years today.",
        accentColor = Color(0xFF825100),
        defaultCategory = "Retirement",
    )

    val wedding = GoalTemplate(
        id = "wedding",
        emoji = "💍",
        title = "Wedding",
        subtitle = "Fund your special day without the stress.",
        accentColor = Color(0xFFB61722),
        defaultCategory = "Wedding",
    )

    val custom = GoalTemplate(
        id = "custom",
        emoji = "🎯",
        title = "Custom Goal",
        subtitle = "Something unique? Define it yourself.",
        accentColor = Color(0xFF0B1C30),
        defaultCategory = "Other",
    )

    val all = listOf(
        homeDownPayment, dreamVacation, newCar, emergencyFund,
        educationFund, retirement, wedding, custom,
    )
}