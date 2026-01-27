package com.example.pocket.ui.screens.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pocket.ui.theme.PocketTheme
import com.example.pocket.ui.theme.PrimaryRed
import com.example.pocket.utils.calculateCardWidth
import com.example.pocket.utils.calculateResponsivePadding
import com.example.pocket.utils.responsiveCardModifier
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

sealed class VerificationMethod {
    object Phone : VerificationMethod()
    object Email : VerificationMethod()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OTPSelectionScreen(
    navController: NavController,
    email: String,
    phone: String
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()

    var selectedMethod by rememberSaveable { mutableStateOf<VerificationMethod>(VerificationMethod.Phone) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val responsivePadding = calculateResponsivePadding()
    val cardWidth = calculateCardWidth()

    // Extract masked versions for display
    val maskedEmail = remember(email) {
        if (email.contains("@")) {
            val parts = email.split("@")
            val username = parts[0]
            val domain = parts[1]
            val maskedUsername = if (username.length > 2) {
                "${username.first()}${"•".repeat(username.length - 2)}${username.last()}"
            } else {
                "••"
            }
            "e$maskedUsername@$domain"
        } else {
            "e••••@example.com"
        }
    }

    val maskedPhone = remember(phone) {
        if (phone.length > 4) {
            val lastFour = phone.takeLast(4)
            "+1 ••• ••• $lastFour"
        } else {
            "+1 ••• ••• ••••"
        }
    }

    // Clear error when selection changes
    LaunchedEffect(selectedMethod) {
        errorMessage = null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        PrimaryRed.copy(alpha = 0.12f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            // Top App Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "Security Check",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(end = 48.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.Top)
            ) {
                // Header Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding * 1.5f),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Verify your identity",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 28.sp,
                        lineHeight = 32.sp
                    )

                    Text(
                        text = "Pick a way to receive your one-time password to keep your POCKET safe.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )
                }

                // Verification Options Card
                Card(
                    modifier = Modifier
                        .responsiveCardModifier()
                        .shadow(
                            elevation = 24.dp,
                            shape = RoundedCornerShape(24.dp),
                            spotColor = PrimaryRed.copy(alpha = 0.1f)
                        ),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Phone Option
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (selectedMethod is VerificationMethod.Phone) {
                                        PrimaryRed.copy(alpha = 0.05f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .clickable(
                                    enabled = !isLoading,
                                    onClick = { selectedMethod = VerificationMethod.Phone }
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(PrimaryRed.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sms,
                                        contentDescription = "SMS",
                                        tint = PrimaryRed,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Mobile Number",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = maskedPhone,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PrimaryRed,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                RadioButton(
                                    selected = selectedMethod is VerificationMethod.Phone,
                                    onClick = { selectedMethod = VerificationMethod.Phone },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = PrimaryRed,
                                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    enabled = !isLoading
                                )
                            }
                        }

                        // Email Option
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (selectedMethod is VerificationMethod.Email) {
                                        PrimaryRed.copy(alpha = 0.05f)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .clickable(
                                    enabled = !isLoading,
                                    onClick = { selectedMethod = VerificationMethod.Email }
                                )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(PrimaryRed.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Email",
                                        tint = PrimaryRed,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = "Email Address",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = maskedEmail,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = PrimaryRed,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                RadioButton(
                                    selected = selectedMethod is VerificationMethod.Email,
                                    onClick = { selectedMethod = VerificationMethod.Email },
                                    colors = RadioButtonDefaults.colors(
                                        selectedColor = PrimaryRed,
                                        unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    enabled = !isLoading
                                )
                            }
                        }
                    }
                }

                // Security Info & Action Section
                Column(
                    modifier = Modifier
                        .responsiveCardModifier()
                        .padding(horizontal = responsivePadding * 1.5f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Security Message
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Security",
                            tint = PrimaryRed,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "This helps us protect your account from unauthorized access and keeps your assets secure.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PrimaryRed,
                            lineHeight = 22.sp
                        )
                    }

                    // Error Message
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Send Code Button
                    Button(
                        onClick = {
                            isLoading = true
                            errorMessage = null

                            when (selectedMethod) {
                                is VerificationMethod.Phone -> {
                                    sendPhoneVerification(
                                        phone = phone,
                                        auth = auth,
                                        context = context,
                                        onSuccess = { verificationId ->
                                            isLoading = false
                                            navController.navigate("otp_verify/phone/$verificationId")
                                        },
                                        onError = { message ->
                                            isLoading = false
                                            errorMessage = message
                                        }
                                    )
                                }

                                is VerificationMethod.Email -> {
                                    sendEmailVerification(
                                        email = email,
                                        auth = auth,
                                        onSuccess = {
                                            isLoading = false
                                            navController.navigate("otp_verify/email/$email")
                                        },
                                        onError = { message ->
                                            isLoading = false
                                            errorMessage = message
                                        }
                                    )
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryRed,
                            contentColor = Color.White
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Send Code",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Send",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // iOS Home Indicator
                Box(
                    modifier = Modifier
                        .width(128.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                )
            }
        }
    }
}

private fun sendPhoneVerification(
    phone: String,
    auth: FirebaseAuth,
    context: Context,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    Log.d("OTPSelection", "Initiating phone verification for: $phone")

    try {
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
                    onError("Phone verification failed: ${e.localizedMessage}")
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.d("OTPSelection", "OTP sent to $phone. Verification ID: $verificationId")
                    onSuccess(verificationId)
                }
            })
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    } catch (e: Exception) {
        Log.e("OTPSelection", "Error setting up phone verification: ${e.message}")
        onError("Failed to initiate phone verification. Please try again.")
    }
}

private fun sendEmailVerification(
    email: String,
    auth: FirebaseAuth,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    Log.d("OTPSelection", "Initiating email verification for: $email")

    val user = auth.currentUser
    if (user != null) {
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("OTPSelection", "Email verification sent to ${user.email}")
                    onSuccess()
                } else {
                    Log.e("OTPSelection", "Email sending failed: ${task.exception?.message}")
                    onError("Failed to send verification email. Please try again.")
                }
            }
            .addOnFailureListener { e ->
                Log.e("OTPSelection", "Email verification error: ${e.message}")
                onError("Email verification failed: ${e.localizedMessage}")
            }
    } else {
        Log.e("OTPSelection", "User is null, can't send email.")
        onError("User not found. Please sign up first.")
    }
}

// Preview Screens
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp", showSystemUi = true)
@Composable
fun OTPSelectionScreenPreviewPhone() {
    PocketTheme {
        OTPSelectionScreen(
            navController = rememberNavController(),
            email = "user@example.com",
            phone = "+254712345678"
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp", showSystemUi = true)
@Composable
fun OTPSelectionScreenPreviewPhoneDark() {
    PocketTheme(darkTheme = true) {
        OTPSelectionScreen(
            navController = rememberNavController(),
            email = "user@example.com",
            phone = "+254712345678"
        )
    }
}

@Preview(showBackground = true, device = "spec:width=600dp,height=960dp", showSystemUi = true)
@Composable
fun OTPSelectionScreenPreviewTabletPortrait() {
    PocketTheme {
        OTPSelectionScreen(
            navController = rememberNavController(),
            email = "user@example.com",
            phone = "+254712345678"
        )
    }
}

@Preview(showBackground = true, device = "spec:width=960dp,height=600dp", showSystemUi = true)
@Composable
fun OTPSelectionScreenPreviewTabletLandscape() {
    PocketTheme {
        OTPSelectionScreen(
            navController = rememberNavController(),
            email = "user@example.com",
            phone = "+254712345678"
        )
    }
}

@Preview(showBackground = true, device = "spec:width=840dp,height=1180dp", showSystemUi = true)
@Composable
fun OTPSelectionScreenPreviewLargeTablet() {
    PocketTheme {
        OTPSelectionScreen(
            navController = rememberNavController(),
            email = "user@example.com",
            phone = "+254712345678"
        )
    }
}