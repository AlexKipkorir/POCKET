package com.example.pocket.data

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object UserMetadataUploader {

    fun uploadUserMetadata(
        fullName: String,
        email: String,
        phone: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        if (user != null) {
            val userData = mapOf(
                "name" to fullName,
                "email" to email,
                "phone" to phone,
                "totalBalance" to 0.0,
                "monthlyIncome" to 0.0,
                "monthlyExpenses" to 0.0
            )

            db.collection("users").document(user.uid)
                .set(userData)
                .addOnSuccessListener {
                    Log.d("Firestore", "User metadata uploaded")
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Failed to upload user metadata", e)
                    onFailure(e)
                }
        } else {
            onFailure(Exception("User not logged in"))
        }
    }

}