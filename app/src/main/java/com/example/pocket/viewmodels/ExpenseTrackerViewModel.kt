package com.example.pocket.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double = 0.0,
    val category: String = "Food",
    val merchant: String = "",
    val note: String = "",
    val date: Date = Date(),
    val paymentMethod: PaymentMethod = PaymentMethod.CASH,
    val isRecurring: Boolean = false,
    val createdAt: Timestamp = Timestamp.now()
)

enum class PaymentMethod {
    CASH, CARD, MOBILE_MONEY, BANK_TRANSFER
}

data class ExpenseCategory(
    val name: String,
    val icon: String,
    val color: String
)

class AddExpenseViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _saveExpenseResult = MutableStateFlow<SaveExpenseResult?>(null)
    val saveExpenseResult = _saveExpenseResult.asStateFlow()

    // Use different name to avoid conflict
    private var _amountText by mutableStateOf("")
    val amountText: String
        get() = _amountText

    var selectedCategory by mutableStateOf("Food")
    var merchant by mutableStateOf("")
    var note by mutableStateOf("")
    var selectedDate by mutableStateOf(Date())
    var selectedPaymentMethod by mutableStateOf(PaymentMethod.CASH)
    var isRecurring by mutableStateOf(false)
    var showDatePicker by mutableStateOf(false)
    var showCategoryPicker by mutableStateOf(false)
    var showPaymentMethodPicker by mutableStateOf(false)
    var recentMerchants by mutableStateOf(emptyList<String>())

    // Categories with icons
    val categories = listOf(
        ExpenseCategory("Food", "🍔", "#FF6B6B"),
        ExpenseCategory("Transport", "🚕", "#4ECDC4"),
        ExpenseCategory("Shopping", "🛍️", "#FFD166"),
        ExpenseCategory("Bills", "🧾", "#06D6A0"),
        ExpenseCategory("Entertainment", "🎬", "#118AB2"),
        ExpenseCategory("Health", "🏥", "#EF476F"),
        ExpenseCategory("Education", "📚", "#7209B7"),
        ExpenseCategory("Grocery", "🛒", "#FF9E6D")
    )

    val paymentMethods = listOf(
        PaymentMethod.CASH,
        PaymentMethod.CARD,
        PaymentMethod.MOBILE_MONEY,
        PaymentMethod.BANK_TRANSFER
    )

    init {
        loadRecentMerchants()
    }

    private fun loadRecentMerchants() {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            try {
                val expenses = firestore.collection("expenses")
                    .whereEqualTo("userId", user.uid)
                    .orderBy("createdAt", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .limit(10)
                    .get()
                    .await()

                recentMerchants = expenses.documents
                    .mapNotNull { it.getString("merchant") }
                    .filter { it.isNotBlank() }
                    .distinct()
                    .take(5)
            } catch (e: Exception) {
                // Use default suggestions
                recentMerchants = listOf("KFC", "Nakumatt", "Uber", "Netflix", "Safaricom")
            }
        }
    }

    fun saveExpense() {
        val user = auth.currentUser ?: return
        val amountValue = amountText.toDoubleOrNull() ?: 0.0

        if (amountValue <= 0) {
            _saveExpenseResult.value =
                SaveExpenseResult.Error("Please enter a valid amount")
            return
        }

        // Ensure merchant is optional
        val merchantValue = merchant.ifBlank { "Unknown" }

        val expense = hashMapOf(
            "userId" to user.uid,
            "amount" to amountValue,
            "category" to selectedCategory,
            "merchant" to merchantValue,
            "note" to note,
            "date" to Timestamp(selectedDate),
            "paymentMethod" to selectedPaymentMethod.name,
            "isRecurring" to isRecurring,
            "createdAt" to Timestamp.now()
        )

        viewModelScope.launch {
            try {
                firestore.collection("expenses")
                    .add(expense)
                    .await()

                updateUserSpend(amountValue)

                _saveExpenseResult.value = SaveExpenseResult.Success
            } catch (e: Exception) {
                _saveExpenseResult.value =
                    SaveExpenseResult.Error("Failed to save expense: ${e.message}")
            }
        }
    }

    private suspend fun updateUserSpend(amount: Double) {
        val user = auth.currentUser ?: return
        val userDoc = firestore.collection("users").document(user.uid)

        try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userDoc)
                val currentSpend = snapshot.getDouble("monthlySpend") ?: 0.0
                transaction.update(userDoc, "monthlySpend", currentSpend + amount)

                // Update daily average (simplified calculation)
                val currentDaily = snapshot.getDouble("dailyAverage") ?: 0.0
                // Calculate new average based on days in month
                val calendar = java.util.Calendar.getInstance()
                val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
                val newDaily = (currentDaily * (daysInMonth - 1) + amount) / daysInMonth
                transaction.update(userDoc, "dailyAverage", newDaily)
            }.await()
        } catch (e: Exception) {
            // Ignore update errors for now
        }
    }

    fun resetForm() {
        _amountText = ""
        selectedCategory = "Food"
        merchant = ""
        note = ""
        selectedDate = Date()
        selectedPaymentMethod = PaymentMethod.CASH
        isRecurring = false
        showDatePicker = false
        showCategoryPicker = false
    }

    // Renamed to avoid conflict with property setter
    fun updateAmount(newAmount: String) {
        // Only allow numbers and one decimal point
        val filtered = newAmount.filter { it.isDigit() || it == '.' }
        if (filtered.count { it == '.' } <= 1) {
            _amountText = filtered
        }
    }

    fun selectMerchantSuggestion(merchantName: String) {
        merchant = merchantName
    }

    fun formatAmount(): String {
        return if (amountText.isNotEmpty()) {
            try {
                val value = amountText.toDouble()
                "KES ${String.format("%,.0f", value)}"
            } catch (e: Exception) {
                "KES 0"
            }
        } else {
            "KES 0"
        }
    }

    fun clearSaveResult() {
        _saveExpenseResult.value = null
    }

    fun isSameDate(date1: Date, date2: Date): Boolean {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return sdf.format(date1) == sdf.format(date2)
    }

    fun getTodayDate(): Date {
        return Date()
    }

    fun getYesterdayDate(): Date {
        val calendar = java.util.Calendar.getInstance()
        calendar.time = Date()
        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        return calendar.time
    }
}

sealed class SaveExpenseResult {
    object Success : SaveExpenseResult()
    data class Error(val message: String) : SaveExpenseResult()
}