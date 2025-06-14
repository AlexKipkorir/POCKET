package com.example.pocket.ui.screens.auth

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pocket.R
import com.example.pocket.ui.theme.PocketTheme
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit


@Composable
fun OTPSelectionScreen(
    navController: NavController,
    email: String,
    phone: String
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.pocket_logo),
            contentDescription = "App Logo",
            modifier = Modifier.size(100.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Choose Verification Method",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Secure your account by verifying via your phone number or email.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                Log.d("OTPSelection", "Phone selected: $phone")
                val options = PhoneAuthOptions.newBuilder(auth)
                    .setPhoneNumber(phone)
                    .setTimeout(60L, TimeUnit.SECONDS)
                    .setActivity(context as Activity)
                    .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                            Log.d("OTPSelection", "Phone verification completed automatically.")
                        }

                        override fun onVerificationFailed(e: FirebaseException) {
                            Log.e("OTPSelection", "Phone verification failed: ${e.message}")
                            Toast.makeText(
                                context,
                                "Phone verification failed: ${e.localizedMessage}",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                        override fun onCodeSent(
                            verificationId: String,
                            token: PhoneAuthProvider.ForceResendingToken
                        ) {
                            Log.d("OTPSelection", "OTP sent to $phone. Verification ID: $verificationId")
                            navController.navigate("otp_verify/phone/$verificationId")
                        }
                    })
                    .build()
                PhoneAuthProvider.verifyPhoneNumber(options)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Verify via Phone ($phone)", color = MaterialTheme.colorScheme.onPrimary)
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                val user = auth.currentUser
                if (user != null) {
                    Log.d("OTPSelection", "Sending email verification to ${user.email}")
                    user.sendEmailVerification()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Verification email sent.", Toast.LENGTH_SHORT).show()
                                Log.d("OTPSelection", "Email verification sent to ${user.email}")
                                navController.navigate("otp_verify/email/$email")
                            } else {
                                Log.e("OTPSelection", "Email sending failed: ${task.exception?.message}")
                                Toast.makeText(
                                    context,
                                    "Failed to send verification email.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                } else {
                    Toast.makeText(
                        context,
                        "User not found. Please sign up first.",
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("OTPSelection", "User is null, can't send email.")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Verify via Email ($email)")
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun OTPSelectionScreenPreview() {
    PocketTheme {
        OTPSelectionScreen(
            navController = rememberNavController(),
            email = "user@example.com",
            phone = "+254700000000"
        )
    }
}