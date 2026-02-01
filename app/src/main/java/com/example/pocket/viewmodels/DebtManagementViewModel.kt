package com.example.pocket.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class Debt(
    val id: String,
    val name: String,
    val amount: Double,
    val originalAmount: Double,
    val interestRate: Double,
    val dueDate: LocalDate,
    val type: DebtType,
    val isPaidOff: Boolean = false,
    val createdAt: LocalDate = LocalDate.now()
) {
    val progress: Double get() = ((originalAmount - amount) / originalAmount * 100).coerceIn(0.0, 100.0)
    val isOverdue: Boolean get() = !isPaidOff && dueDate.isBefore(LocalDate.now())
    val daysUntilDue: Long get() = ChronoUnit.DAYS.between(LocalDate.now(), dueDate)
    val dueStatus: String get() = when {
        isOverdue -> "OVERDUE"
        daysUntilDue <= 7 -> "Due in $daysUntilDue days"
        else -> "Due ${dueDate.format(DateTimeFormatter.ofPattern("MMM d"))}"
    }
}

enum class DebtType(val displayName: String, val iconName: String) {
    CREDIT_CARD("Credit Card", "credit_card"),
    STUDENT_LOAN("Student Loan", "school"),
    CAR_LOAN("Car Loan", "directions_car"),
    PERSONAL_LOAN("Personal Loan", "person"),
    MEDICAL("Medical", "medical_services"),
    MORTGAGE("Mortgage", "home"),
    OTHER("Other", "receipt_long")
}

enum class RepaymentStrategy {
    SNOWBALL, AVALANCHE
}

data class DebtGoal(
    val targetDate: LocalDate,
    val targetAmount: Double,
    val monthlyPayment: Double
)

sealed class DebtSheetState {
    object Hidden : DebtSheetState()
    object AddDebt : DebtSheetState()
    data class PayDebt(val debt: Debt? = null) : DebtSheetState()
    object SetGoal : DebtSheetState()
}

class DebtManagementViewModel : ViewModel() {
    // State flows
    private val _debts = MutableStateFlow<List<Debt>>(emptyList())
    val debts: StateFlow<List<Debt>> = _debts.asStateFlow()

    private val _debtGoal = MutableStateFlow<DebtGoal?>(null)
    val debtGoal: StateFlow<DebtGoal?> = _debtGoal.asStateFlow()

    // Sheet State
    private val _sheetState = MutableStateFlow<DebtSheetState>(DebtSheetState.Hidden)
    val sheetState: StateFlow<DebtSheetState> = _sheetState.asStateFlow()

    // UI State
    var selectedStrategy by mutableStateOf(RepaymentStrategy.SNOWBALL)
        private set

    // Form fields for adding debt
    var newDebtName by mutableStateOf("")
        private set

    var newDebtAmount by mutableStateOf("")
        private set

    var newDebtInterestRate by mutableStateOf("")
        private set

    var newDebtType by mutableStateOf(DebtType.CREDIT_CARD)
        private set

    var newDebtDueDate by mutableStateOf(LocalDate.now().plusMonths(1))
        private set

    // Form fields for payment
    var paymentAmount by mutableStateOf("")
        private set

    // Form fields for setting goal
    var goalTargetDate by mutableStateOf(LocalDate.now().plusYears(2))
        private set

    var goalTargetAmount by mutableStateOf("")
        private set

    init {
        loadSampleData()
    }

    private fun loadSampleData() {
        viewModelScope.launch {
            _debts.value = listOf(
                Debt(
                    id = "1",
                    name = "Chase Credit Card",
                    amount = 4200.0,
                    originalAmount = 10500.0,
                    interestRate = 18.5,
                    dueDate = LocalDate.now().plusDays(4),
                    type = DebtType.CREDIT_CARD
                ),
                Debt(
                    id = "2",
                    name = "Student Loan",
                    amount = 12300.0,
                    originalAmount = 14500.0,
                    interestRate = 4.5,
                    dueDate = LocalDate.now().plusMonths(1).withDayOfMonth(15),
                    type = DebtType.STUDENT_LOAN
                ),
                Debt(
                    id = "3",
                    name = "Car Loan",
                    amount = 8000.0,
                    originalAmount = 13800.0,
                    interestRate = 3.2,
                    dueDate = LocalDate.now().plusMonths(1).withDayOfMonth(22),
                    type = DebtType.CAR_LOAN
                )
            )

            _debtGoal.value = DebtGoal(
                targetDate = LocalDate.now().plusYears(3),
                targetAmount = 0.0,
                monthlyPayment = 850.0
            )
        }
    }

    // Computed properties
    val totalDebt: Double
        get() = debts.value.sumOf { it.amount }

    val totalPaid: Double
        get() = debts.value.sumOf { it.originalAmount - it.amount }

    val totalProgress: Double
        get() = if (debts.value.sumOf { it.originalAmount } > 0) {
            (totalPaid / debts.value.sumOf { it.originalAmount } * 100)
        } else 0.0

    val monthlyDebtReduction: Double
        get() = 1200.0 // Simplified - would calculate from payment history

    fun getSortedDebts(strategy: RepaymentStrategy = selectedStrategy): List<Debt> {
        return when (strategy) {
            RepaymentStrategy.SNOWBALL -> debts.value
                .filter { !it.isPaidOff }
                .sortedBy { it.amount }

            RepaymentStrategy.AVALANCHE -> debts.value
                .filter { !it.isPaidOff }
                .sortedByDescending { it.interestRate }
        }
    }

    fun getProjectedPayoffDate(): LocalDate {
        val sortedDebts = getSortedDebts()
        var remaining = totalDebt
        var date = LocalDate.now()
        val monthlyPayment = debtGoal.value?.monthlyPayment ?: 500.0

        while (remaining > 0) {
            remaining -= monthlyPayment
            date = date.plusMonths(1)

            // Add interest
            val monthlyInterest = sortedDebts.sumOf { debt ->
                (debt.amount * debt.interestRate / 100 / 12)
            }
            remaining += monthlyInterest

            if (date.year > 2030) break // Safety break
        }

        return date
    }

    // Sheet Actions
    fun showAddDebtSheet() {
        _sheetState.value = DebtSheetState.AddDebt
    }

    fun showPayDebtSheet(debt: Debt? = null) {
        paymentAmount = ""
        _sheetState.value = DebtSheetState.PayDebt(debt)
    }

    fun showSetGoalSheet() {
        goalTargetAmount = totalDebt.toString()
        _sheetState.value = DebtSheetState.SetGoal
    }

    fun hideSheet() {
        _sheetState.value = DebtSheetState.Hidden
        resetAllForms()
    }

    fun setStrategy(strategy: RepaymentStrategy) {
        selectedStrategy = strategy
    }

    // Form field updates
    fun updateDebtName(name: String) {
        newDebtName = name
    }

    fun updateDebtAmount(amount: String) {
        newDebtAmount = amount
    }

    fun updateDebtInterestRate(rate: String) {
        newDebtInterestRate = rate
    }

    fun updateDebtType(type: DebtType) {
        newDebtType = type
    }

    fun updateDebtDueDate(date: LocalDate) {
        newDebtDueDate = date
    }

    fun updateGoalTargetDate(date: LocalDate) {
        goalTargetDate = date
    }

    fun updateGoalTargetAmount(amount: String) {
        goalTargetAmount = amount
    }

    fun updatePaymentAmount(amount: String) {
        paymentAmount = amount
    }

    // CRUD Operations
    fun addDebt() {
        if (newDebtName.isBlank() || newDebtAmount.isBlank()) return

        viewModelScope.launch {
            val amount = newDebtAmount.toDoubleOrNull() ?: 0.0
            val interestRate = newDebtInterestRate.toDoubleOrNull() ?: 0.0

            val newDebt = Debt(
                id = System.currentTimeMillis().toString(),
                name = newDebtName,
                amount = amount,
                originalAmount = amount,
                interestRate = interestRate,
                dueDate = newDebtDueDate,
                type = newDebtType
            )

            _debts.value = _debts.value + newDebt
            hideSheet()
        }
    }

    fun makePayment(debt: Debt? = null) {
        val payment = paymentAmount.toDoubleOrNull() ?: return

        viewModelScope.launch {
            val debtsList = _debts.value.toMutableList()

            if (debt != null) {
                // Pay specific debt
                val index = debtsList.indexOfFirst { it.id == debt.id }
                if (index != -1) {
                    val currentDebt = debtsList[index]
                    val newAmount = (currentDebt.amount - payment).coerceAtLeast(0.0)
                    val updatedDebt = currentDebt.copy(
                        amount = newAmount,
                        isPaidOff = newAmount <= 0
                    )
                    debtsList[index] = updatedDebt
                }
            } else {
                // Pay according to strategy
                var remainingPayment = payment
                val sortedDebts = getSortedDebts()

                sortedDebts.forEach { currentDebt ->
                    if (remainingPayment > 0) {
                        val index = debtsList.indexOfFirst { it.id == currentDebt.id }
                        if (index != -1) {
                            val paymentAmount = minOf(remainingPayment, currentDebt.amount)
                            val newAmount = currentDebt.amount - paymentAmount
                            val updatedDebt = currentDebt.copy(
                                amount = newAmount,
                                isPaidOff = newAmount <= 0
                            )
                            debtsList[index] = updatedDebt
                            remainingPayment -= paymentAmount
                        }
                    }
                }
            }

            _debts.value = debtsList
            hideSheet()
        }
    }

    fun setDebtGoal() {
        viewModelScope.launch {
            val targetAmount = goalTargetAmount.toDoubleOrNull() ?: totalDebt
            val months = ChronoUnit.MONTHS.between(
                LocalDate.now(),
                goalTargetDate
            ).coerceAtLeast(1)
            val monthlyPayment = (targetAmount / months).coerceAtLeast(100.0)

            _debtGoal.value = DebtGoal(
                targetDate = goalTargetDate,
                targetAmount = targetAmount,
                monthlyPayment = monthlyPayment
            )

            hideSheet()
        }
    }

    fun deleteDebt(debtId: String) {
        viewModelScope.launch {
            _debts.value = _debts.value.filter { it.id != debtId }
        }
    }

    private fun resetAllForms() {
        // Reset add debt form
        newDebtName = ""
        newDebtAmount = ""
        newDebtInterestRate = ""
        newDebtType = DebtType.CREDIT_CARD
        newDebtDueDate = LocalDate.now().plusMonths(1)

        // Reset payment form
        paymentAmount = ""

        // Reset goal form
        goalTargetAmount = ""
        goalTargetDate = LocalDate.now().plusYears(2)
    }
}