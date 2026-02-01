package com.example.pocket.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Month
import java.time.Year

data class MonthlyReport(
    val month: Month,
    val year: Int,
    val netBalance: Double,
    val totalIncome: Double,
    val totalSpent: Double,
    val savingsPercentage: Double
)

data class ExpenditureCategory(
    val name: String,
    val amount: Double,
    val percentage: Double,
    val colorVariant: Int
)

data class MonthlyInsight(
    val icon: String,
    val color: String,
    val message: String
)

data class SpendingTrend(
    val month: String,
    val amount: Double,
    val isCurrent: Boolean
)

class FinancialReportViewModel : ViewModel() {
    private val _selectedReport = MutableStateFlow<MonthlyReport?>(null)
    val selectedReport: StateFlow<MonthlyReport?> = _selectedReport.asStateFlow()

    private val _expenditureBreakdown = MutableStateFlow<List<ExpenditureCategory>>(emptyList())
    val expenditureBreakdown: StateFlow<List<ExpenditureCategory>> = _expenditureBreakdown.asStateFlow()

    private val _monthlyInsights = MutableStateFlow<List<MonthlyInsight>>(emptyList())
    val monthlyInsights: StateFlow<List<MonthlyInsight>> = _monthlyInsights.asStateFlow()

    private val _spendingTrends = MutableStateFlow<List<SpendingTrend>>(emptyList())
    val spendingTrends: StateFlow<List<SpendingTrend>> = _spendingTrends.asStateFlow()

    // Remove the private setter and handle updates differently
    private val _selectedMonth = mutableStateOf(Month.MARCH)
    val selectedMonth: Month
        get() = _selectedMonth.value

    private val _selectedYear = mutableIntStateOf(Year.now().value)
    val selectedYear: Int
        get() = _selectedYear.intValue

    init {
        loadSampleData()
    }

    fun setSelectedMonth(month: Month) {
        _selectedMonth.value = month
        loadReportForMonth(month, selectedYear)
    }

    fun setSelectedYear(year: Int) {
        _selectedYear.intValue = year
        loadReportForMonth(selectedMonth, year)
    }

    private fun loadReportForMonth(month: Month, year: Int) {
        // In a real app, this would fetch from database/API
        _selectedReport.value = MonthlyReport(
            month = month,
            year = year,
            netBalance = 22500.0,
            totalIncome = 150000.0,
            totalSpent = 127500.0,
            savingsPercentage = 15.0
        )
    }

    private fun loadSampleData() {
        loadReportForMonth(Month.MARCH, 2025)

        _expenditureBreakdown.value = listOf(
            ExpenditureCategory("Housing & Bills", 57375.0, 45.0, 1),
            ExpenditureCategory("Food & Dining", 31875.0, 25.0, 2),
            ExpenditureCategory("Lifestyle & Other", 38250.0, 30.0, 3)
        )

        _monthlyInsights.value = listOf(
            MonthlyInsight(
                icon = "auto_graph",
                color = "orange",
                message = "Transport spending increased by 18% compared to last month."
            ),
            MonthlyInsight(
                icon = "stars",
                color = "green",
                message = "You saved Ksh 5,000 more than your target this month."
            )
        )

        _spendingTrends.value = listOf(
            SpendingTrend("Oct", 30000.0, false),
            SpendingTrend("Nov", 35000.0, false),
            SpendingTrend("Dec", 32000.0, false),
            SpendingTrend("Jan", 40000.0, false),
            SpendingTrend("Feb", 42000.0, false),
            SpendingTrend("Mar", 38000.0, true)
        )
    }

    fun getTotalExpenditurePercentage(): Double {
        return expenditureBreakdown.value.sumOf { it.percentage }
    }

    fun getMonthAbbreviation(month: Month): String {
        return month.name.take(3)
    }
}