package com.example.pocket.viewmodels

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModel
import com.example.pocket.R
import com.example.pocket.ui.screens.BillReminderReceiver
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit

data class BillReminder(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val amount: Double,
    val dueDate: Date
)

data class BillReminderUiState(
    val bills: List<BillReminder> = emptyList(),
    val selectedFilter: String = "All"
)

class BillReminderViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(BillReminderUiState())
    val uiState: StateFlow<BillReminderUiState> = _uiState

    private var allBills: List<BillReminder> = emptyList()
    private var lastDeleted: BillReminder? = null

    init {
        fetchBills()

        if (_uiState.value.bills.isEmpty()) {
            val today = LocalDate.now()
            val mockBills = listOf(
                BillReminder(
                    name = "Water Bill",
                    amount = 450.0,
                    dueDate = Date.from(today.plusDays(3).atStartOfDay(ZoneId.systemDefault()).toInstant())
                ),
                BillReminder(
                    name = "Electricity",
                    amount = 1200.0,
                    dueDate = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant())
                ),
                BillReminder(
                    name = "Internet",
                    amount = 3000.0,
                    dueDate = Date.from(today.minusDays(2).atStartOfDay(ZoneId.systemDefault()).toInstant())
                )
            )
            allBills = mockBills
            _uiState.value = _uiState.value.copy(bills = mockBills)
        }
    }

    private fun fetchBills() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("bills")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        val name = doc.getString("name") ?: return@mapNotNull null
                        val amount = doc.getDouble("amount") ?: return@mapNotNull null
                        val dueDate = doc.getTimestamp("dueDate")?.toDate() ?: return@mapNotNull null
                        BillReminder(
                            id = doc.id,
                            name = name,
                            amount = amount,
                            dueDate = dueDate
                        )
                    }
                    allBills = items
                    applyFilter(_uiState.value.selectedFilter)
                }
            }
    }

    fun addBill(
        context: Context,
        name: String,
        amount: Double,
        dueDate: LocalDate,
        onComplete: (() -> Unit)? = null
    ) {
        val userId = auth.currentUser?.uid ?: return
        val date = Date.from(dueDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        val bill = mapOf(
            "name" to name,
            "amount" to amount,
            "dueDate" to Timestamp(date),
            "userId" to userId
        )

        firestore.collection("bills").add(bill)
            .addOnSuccessListener { documentRef ->
                // Delayed reminder
                scheduleBillNotification(
                    context = context,
                    billId = documentRef.id,
                    title = "Upcoming Bill",
                    message = "\"$name\" of Ksh ${"%.2f".format(amount)} is due soon!",
                    dueDate = date
                )

                // Instant confirmation
                showInstantConfirmationNotification(context, name)

                // Optional: refresh and callback
                fetchBills()
                onComplete?.invoke()
            }
            .addOnFailureListener { e ->
                Log.e("addBill", "Failed to add bill: $e")
            }
    }



    fun deleteBill(bill: BillReminder) {
        lastDeleted = bill
        firestore.collection("bills").document(bill.id).delete()
    }

    fun undoDelete(context: Context) {
        lastDeleted?.let { bill ->
            addBill(
                context = context,
                name = bill.name,
                amount = bill.amount,
                dueDate = bill.dueDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            )
            lastDeleted = null
        }
    }

    fun filterBills(filter: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        applyFilter(filter)
    }

    private fun applyFilter(filter: String) {
        val now = Date()
        val filtered = when (filter) {
            "This Week" -> {
                val weekEnd = Date(now.time + 7L * 24 * 60 * 60 * 1000)
                allBills.filter { it.dueDate.after(now) && it.dueDate.before(weekEnd) }
            }
            "This Month" -> {
                val monthEnd = Date(now.time + 30L * 24 * 60 * 60 * 1000)
                allBills.filter { it.dueDate.after(now) && it.dueDate.before(monthEnd) }
            }
            else -> allBills
        }
        _uiState.value = _uiState.value.copy(bills = filtered)
    }

    fun scheduleBillNotification(
        context: Context,
        billId: String,
        title: String,
        message: String,
        dueDate: Date
    ) {
        val intent = Intent(context, BillReminderReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            billId.hashCode(), // unique requestCode
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            dueDate.time - TimeUnit.HOURS.toMillis(12),
            pendingIntent
        )
    }

    private fun showInstantConfirmationNotification(context: Context, billName: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "confirmation_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Bill Confirmation",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_bill)
            .setContentTitle("Reminder Scheduled")
            .setContentText("Your bill \"$billName\" reminder has been added.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        notificationManager.notify(Random().nextInt(), notification)
    }



}
