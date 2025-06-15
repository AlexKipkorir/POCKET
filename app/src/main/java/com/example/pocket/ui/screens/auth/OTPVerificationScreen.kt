package com.example.pocket.ui.screens.auth

import android.app.Activity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pocket.data.UserMetadataUploader
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit


@Composable
fun OTPVerificationScreen(
    navController: NavController,
    method: String,
    target: String,
    fullName: String,
    email: String
) {
    val context = LocalContext.current
    val auth = Firebase.auth
    var otp by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var isCodeSent by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var emailSent by remember { mutableStateOf(false) }

    fun uploadAndNavigate() {
        UserMetadataUploader.uploadUserMetadata(
            fullName = fullName,
            email = email,
            phone = if (method == "phone") target else auth.currentUser?.phoneNumber ?: "",
            onSuccess = {
                navController.navigate("dashboard") {
                    popUpTo("otp") { inclusive = true }
                }
            },
            onFailure = { e -> errorMessage = "Firestore upload failed: ${e.message}" }
        )
    }

    LaunchedEffect(Unit) {
        if (method == "phone") {
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    auth.signInWithCredential(credential).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            uploadAndNavigate()
                        } else {
                            errorMessage = "Verification failed: ${task.exception?.message}"
                        }
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    errorMessage = "Verification failed: ${e.localizedMessage}"
                }

                override fun onCodeSent(vid: String, token: PhoneAuthProvider.ForceResendingToken) {
                    verificationId = vid
                    isCodeSent = true
                }
            }

            val options = PhoneAuthOptions.newBuilder(auth)
                .setPhoneNumber(target)
                .setTimeout(60L, TimeUnit.SECONDS)
                .setActivity(context as Activity)
                .setCallbacks(callbacks)
                .build()

            PhoneAuthProvider.verifyPhoneNumber(options)
        } else if (method == "email" && !emailSent) {
            val user = auth.currentUser
            user?.sendEmailVerification()
                ?.addOnSuccessListener { emailSent = true }
                ?.addOnFailureListener { errorMessage = it.localizedMessage }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (method == "phone") {
            Text("Enter OTP sent to $target", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = otp,
                onValueChange = {
                    otp = it
                    errorMessage = null
                },
                label = { Text("OTP") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (verificationId != null) {
                        isLoading = true
                        val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
                        auth.signInWithCredential(credential)
                            .addOnCompleteListener { task ->
                                isLoading = false
                                if (task.isSuccessful) {
                                    uploadAndNavigate()
                                } else {
                                    errorMessage = task.exception?.message
                                }
                            }
                    }
                },
                enabled = otp.length == 6 && isCodeSent && !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("Verify")
            }
        } else {
            Text("A verification link was sent to $email", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    isLoading = true
                    auth.currentUser?.reload()?.addOnSuccessListener {
                        isLoading = false
                        if (auth.currentUser?.isEmailVerified == true) {
                            uploadAndNavigate()
                        } else {
                            errorMessage = "Email not yet verified. Please click the link in your inbox."
                        }
                    }?.addOnFailureListener {
                        isLoading = false
                        errorMessage = it.localizedMessage
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                else Text("Continue")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }


}

