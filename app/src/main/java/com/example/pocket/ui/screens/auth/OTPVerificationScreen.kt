package com.example.pocket.ui.screens.auth

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

@Composable
fun OTPVerificationScreen(
    navController: NavController,
    method: String,
    target: String
) {
    val context = LocalContext.current
    val auth = Firebase.auth
    var otp by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var isCodeSent by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    //Send code on first composition for phone method
    LaunchedEffect(Unit) {
        if (method == "phone") {
            val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    //Auto Verification
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                navController.navigate("dashboard")
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
        } else {
            isCodeSent = true
        }
    }

     Column(
         modifier = Modifier
             .fillMaxSize()
             .padding(24.dp),
         verticalArrangement = Arrangement.Center,
         horizontalAlignment = Alignment.CenterHorizontally
     ) {
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

         errorMessage?.let {
             Text(it, color = MaterialTheme.colorScheme.error)
             Spacer(modifier = Modifier.height(8.dp))
         }

         Button(
             onClick = {
                 if (method == "phone" && verificationId != null) {
                     isLoading = true
                     val credential = PhoneAuthProvider.getCredential(verificationId!!, otp)
                     auth.signInWithCredential(credential)
                         .addOnCompleteListener { task ->
                             isLoading = false
                             if (task.isSuccessful) {
                                 navController.navigate(("dashboard"))
                             } else {
                                 errorMessage = task.exception?.message
                             }

                         }
                 } else {
                     //Placeholder: Simulate success
                     navController.navigate("dashboard")
                 }
             },
             enabled = otp.length == 6 && isCodeSent && !isLoading,
             modifier = Modifier
                 .fillMaxWidth()
                 .height(50.dp)
         ) {
             if (isLoading) {
                 CircularProgressIndicator(
                     color = Color.Red,
                     modifier = Modifier.size(24.dp)
                 )
             } else {
                 Text("Verify")
             }
         }

     }
}