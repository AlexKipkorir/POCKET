package com.example.pocket.ui.screens.auth

import android.app.Activity
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pocket.data.UserMetadataUploader
import com.example.pocket.ui.theme.PocketTheme
import com.example.pocket.ui.theme.PrimaryRed
import com.example.pocket.utils.calculateResponsivePadding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneOTPVerificationScreen(
    navController: NavController,
    phone: String,
    verificationId: String,
    fullName: String,
    email: String
) {
    val context = LocalContext.current
    val auth = Firebase.auth

    val otpLength = 6
    var otp by rememberSaveable { mutableStateOf(CharArray(otpLength) { ' ' }) }
    var activeOtpIndex by rememberSaveable { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isCodeSent by remember { mutableStateOf(false) }
    var countdown by remember { mutableStateOf(45) }
    var canResend by remember { mutableStateOf(false) }

    val responsivePadding = calculateResponsivePadding()

    // Mask phone number for display
    val maskedPhone = remember(phone) {
        if (phone.length > 4) {
            val lastFour = phone.takeLast(4)
            "+1 ••• ••• $lastFour"
        } else {
            "+1 ••• ••• ••••"
        }
    }

    // Countdown timer
    LaunchedEffect(canResend) {
        if (!canResend && countdown > 0) {
            while (countdown > 0) {
                delay(1000)
                countdown--
            }
            canResend = true
        }
    }

    // Move these function definitions BEFORE the LaunchedEffect that calls them

    fun uploadAndNavigate() {
        UserMetadataUploader.uploadUserMetadata(
            fullName = fullName,
            email = email,
            phone = phone,
            onSuccess = {
                navController.navigate("dashboard") {
                    popUpTo("phone_otp") { inclusive = true }
                }
            },
            onFailure = { e ->
                errorMessage = "Failed to save user data: ${e.message}"
                isLoading = false
            }
        )
    }

    fun verifyOTP() {
        isLoading = true
        errorMessage = null

        val otpString = otp.joinToString("")
        val credential = PhoneAuthProvider.getCredential(verificationId, otpString)

        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    uploadAndNavigate()
                } else {
                    errorMessage = task.exception?.localizedMessage ?: "Verification failed"
                    // Clear OTP on error
                    otp = CharArray(otpLength) { ' ' }
                    activeOtpIndex = 0
                }
            }
    }

    fun resendCode() {
        isLoading = true
        errorMessage = null

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                auth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        uploadAndNavigate()
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                isLoading = false
                errorMessage = "Failed to resend code: ${e.localizedMessage}"
            }

            override fun onCodeSent(
                newVerificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                isLoading = false
                isCodeSent = true
                countdown = 45
                canResend = false
                // Note: In real app, you'd update the verificationId
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(context as Activity)
            .setCallbacks(callbacks)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    // Auto-submit when OTP is complete
    LaunchedEffect(otp) {
        if (otp.all { it != ' ' } && otp.size == otpLength) {
            verifyOTP()
        }
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
            // Top Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = responsivePadding, start = responsivePadding, end = responsivePadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "POCKET",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 3.sp
                )

                Box(modifier = Modifier.size(48.dp)) // Spacer
            }

            // Progress Indicator
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = responsivePadding * 1.5f, vertical = responsivePadding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Step 3 of 4",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Security",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryRed,
                        letterSpacing = 1.sp
                    )
                }

                LinearProgressIndicator(
                    progress = 0.75f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = PrimaryRed,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }

            // Main Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = responsivePadding * 1.5f),
                verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.Top)
            ) {
                // Header
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Verify your phone",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 32.sp,
                        lineHeight = 36.sp
                    )

                    Text(
                        text = "Enter the 6-digit code sent to\n$maskedPhone",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 24.sp
                    )
                }

                // OTP Input Fields
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (index in 0 until otpLength) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (otp[index] != ' ') PrimaryRed.copy(alpha = 0.1f)
                                        else MaterialTheme.colorScheme.surface
                                    )
                                    .clickable {
                                        activeOtpIndex = index
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (otp[index] != ' ') otp[index].toString() else "",
                                    style = TextStyle(
                                        fontSize = 32.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        textAlign = TextAlign.Center
                                    )
                                )
                            }
                        }
                    }

                    // Hidden text field for keyboard input
                    BasicTextField(
                        value = otp.joinToString(""),
                        onValueChange = { newValue ->
                            if (newValue.length <= otpLength) {
                                otp = CharArray(otpLength) { index ->
                                    if (index < newValue.length) newValue[index] else ' '
                                }
                                activeOtpIndex = newValue.length.coerceAtMost(otpLength - 1)
                            }
                        },
                        modifier = Modifier
                            .size(1.dp)
                            .padding(0.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        decorationBox = { innerTextField ->
                            // Hidden
                        }
                    )

                    // Resend & Timer
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Didn't receive the code?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(
                                onClick = { if (canResend) resendCode() },
                                enabled = canResend && !isLoading
                            ) {
                                Text(
                                    text = "Resend code",
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (canResend) PrimaryRed else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Text(
                                text = "|",
                                color = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Text(
                                text = String.format("%02d:%02d", countdown / 60, countdown % 60),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Action Button
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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

                    Button(
                        onClick = { verifyOTP() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryRed,
                            contentColor = Color.White
                        ),
                        enabled = otp.all { it != ' ' } && !isLoading
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
                                    text = "Verify and Continue",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Verify",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Terms Text
                    Text(
                        text = "By continuing, you agree to POCKET's Terms of Service and Security Policy.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = responsivePadding)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // iOS Home Indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
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
}

// Preview Screens
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp", showSystemUi = true)
@Composable
fun PhoneOTPVerificationScreenPreviewPhone() {
    PocketTheme {
        PhoneOTPVerificationScreen(
            navController = rememberNavController(),
            phone = "+254712345678",
            verificationId = "test_verification_id",
            fullName = "John Doe",
            email = "john@example.com"
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp", showSystemUi = true)
@Composable
fun PhoneOTPVerificationScreenPreviewPhoneDark() {
    PocketTheme(darkTheme = true) {
        PhoneOTPVerificationScreen(
            navController = rememberNavController(),
            phone = "+254712345678",
            verificationId = "test_verification_id",
            fullName = "John Doe",
            email = "john@example.com"
        )
    }
}

@Preview(showBackground = true, device = "spec:width=600dp,height=960dp", showSystemUi = true)
@Composable
fun PhoneOTPVerificationScreenPreviewTabletPortrait() {
    PocketTheme {
        PhoneOTPVerificationScreen(
            navController = rememberNavController(),
            phone = "+254712345678",
            verificationId = "test_verification_id",
            fullName = "John Doe",
            email = "john@example.com"
        )
    }
}

@Preview(showBackground = true, device = "spec:width=960dp,height=600dp", showSystemUi = true)
@Composable
fun PhoneOTPVerificationScreenPreviewTabletLandscape() {
    PocketTheme {
        PhoneOTPVerificationScreen(
            navController = rememberNavController(),
            phone = "+254712345678",
            verificationId = "test_verification_id",
            fullName = "John Doe",
            email = "john@example.com"
        )
    }
}