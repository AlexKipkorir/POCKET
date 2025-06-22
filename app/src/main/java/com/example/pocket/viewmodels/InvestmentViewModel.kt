package com.example.pocket.viewmodels

import androidx.lifecycle.ViewModel
import com.example.pocket.model.Investment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class InvestmentViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _investments = MutableStateFlow<List<Investment>>(emptyList())
    val investments: StateFlow<List<Investment>> = _investments

    init {
        fetchInvestments()
    }

    private fun fetchInvestments() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("investments")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    _investments.value = it.documents.mapNotNull { doc ->
                        val data = doc.data ?: return@mapNotNull null
                        Investment(
                            id = doc.id,
                            name = data["name"] as? String ?: "",
                            amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
                            performance = (data["performance"] as? Number)?.toDouble() ?: 0.0
                        )
                    }
                }
            }
    }

    fun addInvestment(
        name: String,
        amount: Double,
        performance: Double,
        goal: Double?,
        maturityDate: LocalDate?,
        reminder: Boolean,
        sourceUrl: String?
    ) {
        val userId = auth.currentUser?.uid ?: return
        val maturityDateAsDate = maturityDate?.let {
            Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant())
        }
        val newInvestment = mapOf(
            "name" to name,
            "amount" to amount,
            "performance" to performance,
            "goal" to goal,
            "maturityDate" to maturityDateAsDate,
            "reminderEnabled" to reminder,
            "sourceUrl" to sourceUrl,
            "userId" to userId,
            "createdAt" to FieldValue.serverTimestamp()
        )

        firestore.collection("investments").add(newInvestment)
    }

    fun deleteInvestment(investmentId: String) {
        firestore.collection("investments").document(investmentId).delete()
    }

    fun updateInvestment(
        id: String,
        name: String,
        amount: Double,
        performance: Double,
        goal: Double?,
        maturityDate: LocalDate?,
        reminder: Boolean,
        sourceUrl: String?
    ) {
        val maturityDateAsDate = maturityDate?.let {
            Date.from(it.atStartOfDay(ZoneId.systemDefault()).toInstant())
        }
        val updatedInvestment = mapOf(
            "name" to name,
            "amount" to amount,
            "performance" to performance,
            "goal" to goal,
            "maturityDate" to maturityDateAsDate,
            "reminderEnabled" to reminder,
            "sourceUrl" to sourceUrl,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        firestore.collection("investments").document(id).update(updatedInvestment)
    }
    fun toggleReminder(investment: Investment) {
        val newStatus = !investment.reminderEnabled
        firestore.collection("investments")
            .document(investment.id)
            .update("reminderEnabled", newStatus)
    }


}