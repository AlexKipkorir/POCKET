package com.example.pocket.viewmodels

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.pocket.model.ExpenseCategory
import com.example.pocket.model.ExpensesData


class ExpenseTrackerViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    var transport by mutableStateOf("")
    var food by mutableStateOf("")
    var shopping by mutableStateOf("")
    var otherCosts by mutableStateOf("")

    var customExpenses = mutableStateMapOf<String, String>()
        private set

    init {
        loadExpenses()
    }

    fun loadExpenses() {
        val user = auth.currentUser ?: return
        firestore.collection("expenses").document(user.uid).get().addOnSuccessListener { doc ->
            doc?.data?.let { data ->
                val categories = data["categories"] as? Map<*, *>
                transport = (categories?.get("transport") as? Number)?.toString() ?: ""
                food = (categories?.get("food") as? Number)?.toString() ?: ""
                shopping = (categories?.get("shopping") as? Number)?.toString() ?: ""
                otherCosts = (categories?.get("otherCosts") as? Number)?.toString() ?: ""

                val customs = data["customCategories"] as? Map<*, *>
                customs?.forEach { (key, value) ->
                    if (key is String && value is Number) {
                        customExpenses[key] = value.toString()
                    }
                }

            }

        }
    }

    fun saveExpenses(onSuccess: () -> Unit) {
        val user = auth.currentUser ?: return

        val categories = ExpenseCategory(
            transport = transport.toDoubleOrNull() ?: 0.0,
            food = food.toDoubleOrNull() ?: 0.0,
            shopping = shopping.toDoubleOrNull() ?: 0.0,
            otherCosts = otherCosts.toDoubleOrNull() ?: 0.0
        )

        val custom = customExpenses.mapValues { it.value.toDoubleOrNull() ?: 0.0 }

        val expensesData = ExpensesData(categories, custom)

        firestore.collection("expenses").document(user.uid).set(expensesData)
            .addOnSuccessListener { onSuccess() }
    }

    fun addCustomExpense(category: String) {
        if (!customExpenses.containsKey(category)) {
            customExpenses[category] = ""
        }
    }

    fun removeCustomExpense(name: String) {
        val user = auth.currentUser ?: return
        firestore.collection("expenses").document(user.uid)
            .update("customCategories.$name", FieldValue.delete())
        customExpenses.remove(name)
    }

    fun updateCustomExpense(category: String, value: String) {
        customExpenses[category] = value
    }

}