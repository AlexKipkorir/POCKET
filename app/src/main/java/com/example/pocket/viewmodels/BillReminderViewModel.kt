package com.example.pocket.viewmodels

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pocket.R
import com.example.pocket.model.BillReminder
import com.example.pocket.ui.screens.spend.BillReminderReceiver
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit

data class BillUiState(
    val bills: List<BillReminder> = emptyList(),
    val filteredBills: List<BillReminder> = emptyList(),
    val selectedFilter: FilterType = FilterType.ALL,
    val isLoading: Boolean = true,
    val showAddBillSheet: Boolean = false,
    val showEditBillSheet: Boolean = false,
    val editingBill: BillReminder? = null,
    val totalDue: Double = 0.0,
    val nextBill: NextBillInfo? = null,
    val overdueCount: Int = 0,
    val thisWeekCount: Int = 0,
    val paidCount: Int = 0
)

data class NextBillInfo(
    val name: String,
    val daysUntil: Int,
    val amount: Double
)

enum class FilterType {
    ALL, OVERDUE, THIS_WEEK, PAID
}

enum class RecurrenceType {
    ONE_TIME, WEEKLY, MONTHLY, YEARLY
}

class BillReminderViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(BillUiState())
    val uiState = _uiState.asStateFlow()

    private var allBills: List<BillReminder> = emptyList()

    init {
        loadBills()
    }

    private fun loadBills() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                firestore.collection("bills")
                    .whereEqualTo("userId", userId)
                    .addSnapshotListener { snapshot, error ->
                        if (error != null) {
                            updateUiState { it.copy(isLoading = false) }
                            return@addSnapshotListener
                        }

                        snapshot?.let {
                            val bills = it.documents.mapNotNull { doc ->
                                try {
                                    BillReminder(
                                        id = doc.id,
                                        name = doc.getString("name") ?: "",
                                        amount = doc.getDouble("amount") ?: 0.0,
                                        dueDate = doc.getTimestamp("dueDate")?.toDate() ?: Date(),
                                        isPaid = doc.getBoolean("isPaid") ?: false,
                                        reminderEnabled = doc.getBoolean("reminderEnabled") ?: true,
                                        recurrence = doc.getString("recurrence") ?: "ONE_TIME",
                                        reminderDaysBefore = doc.getLong("reminderDaysBefore")?.toInt() ?: 1
                                    )
                                } catch (e: Exception) {
                                    null
                                }
                            }

                            allBills = bills.sortedBy { bill -> bill.dueDate }
                            calculateSummary(bills)
                            applyFilter(_uiState.value.selectedFilter)
                            updateUiState { it.copy(isLoading = false) }
                        }
                    }
            } catch (e: Exception) {
                updateUiState { it.copy(isLoading = false) }
            }
        }
    }

    private fun calculateSummary(bills: List<BillReminder>) {
        val today = Date()
        val calendar = Calendar.getInstance()
        calendar.time = today
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val weekFromNow = calendar.time

        val overdue = bills.filter { !it.isPaid && it.dueDate.before(today) }
        val upcoming = bills.filter { !it.isPaid && !it.dueDate.before(today) }
        val thisWeek = upcoming.filter { it.dueDate.before(weekFromNow) }
        val paid = bills.filter { it.isPaid }

        val totalDue = upcoming.sumOf { it.amount }
        val nextBill = upcoming.firstOrNull()?.let {
            val daysUntil = calculateDaysBetween(today, it.dueDate)
            NextBillInfo(it.name, daysUntil, it.amount)
        }

        updateUiState { state ->
            state.copy(
                totalDue = totalDue,
                nextBill = nextBill,
                overdueCount = overdue.size,
                thisWeekCount = thisWeek.size,
                paidCount = paid.size
            )
        }
    }

    private fun calculateDaysBetween(start: Date, end: Date): Int {
        val diff = end.time - start.time
        return TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS).toInt()
    }

    fun addBill(
        context: Context,
        name: String,
        amount: Double,
        dueDate: Date,
        recurrence: RecurrenceType = RecurrenceType.ONE_TIME,
        reminderDaysBefore: Int = 1,
        onComplete: (() -> Unit)? = null
    ) {
        val userId = auth.currentUser?.uid ?: return

        val bill = hashMapOf(
            "userId" to userId,
            "name" to name,
            "amount" to amount,
            "dueDate" to Timestamp(dueDate),
            "isPaid" to false,
            "reminderEnabled" to true,
            "recurrence" to recurrence.name,
            "reminderDaysBefore" to reminderDaysBefore,
            "createdAt" to Timestamp.now()
        )

        firestore.collection("bills")
            .add(bill)
            .addOnSuccessListener { documentRef ->
                scheduleBillNotification(context, documentRef.id, name, amount, dueDate)
                onComplete?.invoke()
                showAddBillSheet(false)
            }
            .addOnFailureListener {
                // Handle error
            }
    }

    fun updateBill(
        context: Context,
        billId: String,
        name: String,
        amount: Double,
        dueDate: Date,
        recurrence: RecurrenceType = RecurrenceType.ONE_TIME,
        reminderDaysBefore: Int = 1
    ) {
        val updates = hashMapOf<String, Any>(
            "name" to name,
            "amount" to amount,
            "dueDate" to Timestamp(dueDate),
            "recurrence" to recurrence.name,
            "reminderDaysBefore" to reminderDaysBefore
        )

        firestore.collection("bills").document(billId)
            .update(updates)
            .addOnSuccessListener {
                scheduleBillNotification(context, billId, name, amount, dueDate)
                showEditBillSheet(false)
                updateUiState { it.copy(editingBill = null) }
            }
    }

    fun toggleBillPaidStatus(bill: BillReminder) {
        firestore.collection("bills").document(bill.id)
            .update("isPaid", !bill.isPaid)
            .addOnSuccessListener {
                loadBills()
            }
    }

    fun deleteBill(billId: String) {
        firestore.collection("bills").document(billId)
            .delete()
            .addOnSuccessListener {
                loadBills()
            }
    }

    fun setFilter(filter: FilterType) {
        updateUiState { it.copy(selectedFilter = filter) }
        applyFilter(filter)
    }

    private fun applyFilter(filter: FilterType) {
        val today = Date()
        val calendar = Calendar.getInstance()
        calendar.time = today
        calendar.add(Calendar.DAY_OF_YEAR, 7)
        val weekFromNow = calendar.time

        val filtered = when (filter) {
            FilterType.ALL -> allBills
            FilterType.OVERDUE -> allBills.filter { !it.isPaid && it.dueDate.before(today) }
            FilterType.THIS_WEEK -> allBills.filter {
                !it.isPaid && it.dueDate.before(weekFromNow) && !it.dueDate.before(today)
            }
            FilterType.PAID -> allBills.filter { it.isPaid }
        }

        updateUiState { it.copy(filteredBills = filtered) }
    }

    fun showAddBillSheet(show: Boolean) {
        updateUiState { it.copy(showAddBillSheet = show) }
    }

    fun showEditBillSheet(show: Boolean, bill: BillReminder? = null) {
        updateUiState { it.copy(showEditBillSheet = show, editingBill = bill) }
    }

    private fun updateUiState(update: (BillUiState) -> BillUiState) {
        _uiState.value = update(_uiState.value)
    }

    private fun scheduleBillNotification(
        context: Context,
        billId: String,
        name: String,
        amount: Double,
        dueDate: Date
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, BillReminderReceiver::class.java).apply {
            putExtra("billId", billId)
            putExtra("title", "Bill Reminder")
            putExtra("message", "\"$name\" - KES ${String.format("%,.2f", amount)} is due tomorrow!")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            billId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule notification 1 day before due date
        val notificationTime = dueDate.time - TimeUnit.DAYS.toMillis(1)
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            notificationTime,
            pendingIntent
        )
    }
}