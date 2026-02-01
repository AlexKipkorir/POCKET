package com.example.pocket.viewmodels


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

data class Bill(
    val id: String,
    val name: String,
    val amount: Double,
    val dueDate: Date,
    val category: String,
    val status: BillStatus,
    val iconType: BillIcon,
    val isAutoPay: Boolean = false,
    val isOverdue: Boolean = false,
    val paidDate: Date? = null
)

enum class BillStatus {
    PAID, PENDING, OVERDUE, UPCOMING
}

enum class BillIcon {
    ELECTRICITY, RENT, INTERNET, GYM, CAR_INSURANCE, SUBSCRIPTION, HEALTH
}

data class BillCategory(
    val name: String,
    val totalBudget: Double,
    val remaining: Double,
    val paidCount: Int,
    val totalCount: Int,
    val progressPercentage: Float
)

data class BillSummary(
    val totalBills: Int,
    val overdueBills: Int,
    val totalAmount: Double,
    val monthlyIncrease: Double
)

class BillRemindersViewModel : ViewModel() {
    private val _bills = MutableStateFlow<List<Bill>>(emptyList())
    val bills: StateFlow<List<Bill>> = _bills.asStateFlow()

    private val _categories = MutableStateFlow<List<BillCategory>>(emptyList())
    val categories: StateFlow<List<BillCategory>> = _categories.asStateFlow()

    private val _summary = MutableStateFlow<BillSummary?>(null)
    val summary: StateFlow<BillSummary?> = _summary.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadBills()
    }

    private fun loadBills() {
        viewModelScope.launch {
            _isLoading.value = true

            // Simulate API call delay
            kotlinx.coroutines.delay(500)

            // Generate sample data
            val calendar = Calendar.getInstance()
            val now = Date()

            // Generate bills
            val generatedBills = listOf(
                Bill(
                    id = "1",
                    name = "Electric Bill",
                    amount = 142.50,
                    dueDate = calendar.apply {
                        time = now
                        add(Calendar.DAY_OF_YEAR, -5) // Overdue by 5 days
                    }.time,
                    category = "Utilities",
                    status = BillStatus.OVERDUE,
                    iconType = BillIcon.ELECTRICITY,
                    isOverdue = true
                ),
                Bill(
                    id = "2",
                    name = "Apartment Rent",
                    amount = 800.00,
                    dueDate = calendar.apply {
                        time = now
                        add(Calendar.DAY_OF_YEAR, 3) // Due in 3 days
                    }.time,
                    category = "Housing",
                    status = BillStatus.PENDING,
                    iconType = BillIcon.RENT
                ),
                Bill(
                    id = "3",
                    name = "Internet Service",
                    amount = 75.00,
                    dueDate = calendar.apply {
                        time = now
                        add(Calendar.DAY_OF_YEAR, 2) // Due this week
                        set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY)
                    }.time,
                    category = "Utilities",
                    status = BillStatus.UPCOMING,
                    iconType = BillIcon.INTERNET,
                    isAutoPay = true
                ),
                Bill(
                    id = "4",
                    name = "Gym Membership",
                    amount = 50.00,
                    dueDate = calendar.apply {
                        time = now
                        add(Calendar.DAY_OF_YEAR, 7) // Next week
                    }.time,
                    category = "Health & Fitness",
                    status = BillStatus.UPCOMING,
                    iconType = BillIcon.GYM
                ),
                Bill(
                    id = "5",
                    name = "Car Insurance",
                    amount = 120.00,
                    dueDate = calendar.apply {
                        time = now
                        add(Calendar.DAY_OF_YEAR, 14) // Later
                    }.time,
                    category = "Transportation",
                    status = BillStatus.UPCOMING,
                    iconType = BillIcon.CAR_INSURANCE,
                    isAutoPay = true
                ),
                Bill(
                    id = "6",
                    name = "Health Insurance",
                    amount = 350.00,
                    dueDate = calendar.apply {
                        time = now
                        add(Calendar.DAY_OF_YEAR, -2) // Recently paid
                    }.time,
                    category = "Health",
                    status = BillStatus.PAID,
                    iconType = BillIcon.HEALTH,
                    paidDate = now
                ),
                Bill(
                    id = "7",
                    name = "Netflix",
                    amount = 15.99,
                    dueDate = calendar.apply {
                        time = now
                        add(Calendar.DAY_OF_YEAR, 1)
                    }.time,
                    category = "Subscriptions",
                    status = BillStatus.UPCOMING,
                    iconType = BillIcon.SUBSCRIPTION
                ),
                Bill(
                    id = "8",
                    name = "Spotify",
                    amount = 9.99,
                    dueDate = calendar.apply {
                        time = now
                        add(Calendar.DAY_OF_YEAR, 5)
                    }.time,
                    category = "Subscriptions",
                    status = BillStatus.UPCOMING,
                    iconType = BillIcon.SUBSCRIPTION
                )
            )

            _bills.value = generatedBills

            // Generate categories
            _categories.value = listOf(
                BillCategory(
                    name = "Health",
                    totalBudget = 1000.0,
                    remaining = 620.0,
                    paidCount = 2,
                    totalCount = 8,
                    progressPercentage = 0.25f
                ),
                BillCategory(
                    name = "Utilities",
                    totalBudget = 300.0,
                    remaining = 157.5,
                    paidCount = 1,
                    totalCount = 3,
                    progressPercentage = 0.48f
                ),
                BillCategory(
                    name = "Housing",
                    totalBudget = 1000.0,
                    remaining = 800.0,
                    paidCount = 0,
                    totalCount = 1,
                    progressPercentage = 0.20f
                )
            )

            // Generate summary
            _summary.value = BillSummary(
                totalBills = 8,
                overdueBills = 1,
                totalAmount = 1563.48,
                monthlyIncrease = 120.0
            )

            _isLoading.value = false
        }
    }

    fun markBillAsPaid(billId: String) {
        viewModelScope.launch {
            _bills.update { currentBills ->
                currentBills.map { bill ->
                    if (bill.id == billId) {
                        bill.copy(
                            status = BillStatus.PAID,
                            paidDate = Date(),
                            isOverdue = false
                        )
                    } else {
                        bill
                    }
                }
            }
        }
    }

    fun payBill(billId: String) {
        viewModelScope.launch {
            _bills.update { currentBills ->
                currentBills.map { bill ->
                    if (bill.id == billId) {
                        bill.copy(
                            status = BillStatus.PAID,
                            paidDate = Date(),
                            isOverdue = false
                        )
                    } else {
                        bill
                    }
                }
            }
        }
    }

    fun getBillsByStatus(status: BillStatus): List<Bill> {
        return _bills.value.filter { it.status == status }
    }

    fun getUpcomingBills(): List<Bill> {
        val now = Date()
        val calendar = Calendar.getInstance()
        calendar.time = now
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val oneWeekLater = calendar.time

        return _bills.value.filter { bill ->
            bill.dueDate.after(now) && bill.dueDate.before(oneWeekLater) && bill.status != BillStatus.PAID
        }
    }

    fun getLaterBills(): List<Bill> {
        val now = Date()
        val calendar = Calendar.getInstance()
        calendar.time = now
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val oneWeekLater = calendar.time

        return _bills.value.filter { bill ->
            bill.dueDate.after(oneWeekLater) && bill.status != BillStatus.PAID
        }
    }

    fun getSubscriptionBills(): List<Bill> {
        return _bills.value.filter { bill ->
            bill.category == "Subscriptions" && bill.status != BillStatus.PAID
        }
    }

    fun getTotalSubscriptionCost(): Double {
        return getSubscriptionBills().sumOf { it.amount }
    }
}