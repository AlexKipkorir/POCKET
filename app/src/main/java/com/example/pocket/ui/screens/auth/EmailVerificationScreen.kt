package com.example.pocket.ui.screens.auth

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.pocket.data.UserMetadataUploader
import com.example.pocket.ui.theme.PocketTheme
import com.example.pocket.ui.theme.PrimaryRed
import com.example.pocket.utils.calculateResponsivePadding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.delay

@Composable
fun EmailVerificationScreen(
    navController: NavController,
    email: String,
    fullName: String,
    phone: String
) {
    val context = LocalContext.current
    val auth = Firebase.auth

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isChecking by remember { mutableStateOf(false) }
    var verificationAttempts by remember { androidx.compose.runtime.mutableIntStateOf(0) }

    val responsivePadding = calculateResponsivePadding()

    // Mask email for display
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
            "$maskedUsername@$domain"
        } else {
            "••••@example.com"
        }
    }

    fun uploadAndNavigate() {
        UserMetadataUploader.uploadUserMetadata(
            fullName = fullName,
            email = email,
            phone = phone,
            onSuccess = {
                navController.navigate("dashboard") {
                    popUpTo("email_verification") { inclusive = true }
                }
            },
            onFailure = { e ->
                errorMessage = "Failed to save user data: ${e.message}"
                isLoading = false
            }
        )
    }

    fun checkVerification() {
        isChecking = true
        errorMessage = null

        auth.currentUser?.reload()?.addOnSuccessListener {
            isChecking = false
            if (auth.currentUser?.isEmailVerified == true) {
                uploadAndNavigate()
            } else {
                errorMessage = "Email not yet verified. Please click the link in your inbox."
                verificationAttempts++
            }
        }?.addOnFailureListener {
            isChecking = false
            errorMessage = "Failed to check verification status: ${it.localizedMessage}"
        }
    }

    fun resendVerificationEmail() {
        isLoading = true
        errorMessage = null

        val user = auth.currentUser
        user?.sendEmailVerification()
            ?.addOnSuccessListener {
                isLoading = false
                // Show success message or toast
            }
            ?.addOnFailureListener {
                isLoading = false
                errorMessage = "Failed to resend verification email: ${it.localizedMessage}"
            }
    }

    // Auto-check verification status every 5 seconds
    LaunchedEffect(isChecking) {
        if (!isChecking && verificationAttempts < 20) { // Limit attempts
            delay(5000)
            checkVerification()
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
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "POCKET",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Box(modifier = Modifier.size(48.dp)) // Spacer
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Main Content
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(40.dp, Alignment.Top)
            ) {
                // Illustration/Icon Section
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .background(PrimaryRed.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MarkEmailRead,
                            contentDescription = "Email",
                            tint = PrimaryRed,
                            modifier = Modifier.size(80.dp)
                        )

                        // Decorative Badge
                        Card(
                            modifier = Modifier
                                .size(44.dp)
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(50.dp),
                                    spotColor = PrimaryRed.copy(alpha = 0.3f)
                                ),
                            shape = RoundedCornerShape(50.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = PrimaryRed,
                                contentColor = Color.White
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Secure",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }

                // Text Content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding * 1.5f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Check your email",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp
                    )

                    Text(
                        text = "We've sent a verification link to $maskedEmail. Tap the link in the email to continue.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 24.sp
                    )
                }

                // Action Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = responsivePadding * 1.5f),
                    horizontalAlignment = Alignment.CenterHorizontally,
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

                    // Verify Button
                    Button(
                        onClick = { checkVerification() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryRed,
                            contentColor = Color.White
                        ),
                        enabled = !isChecking && !isLoading
                    ) {
                        if (isChecking) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(
                                text = "I've verified",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // Secondary Actions
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        TextButton(
                            onClick = { resendVerificationEmail() },
                            enabled = !isLoading && !isChecking
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Replay,
                                    contentDescription = "Resend",
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Resend email",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        TextButton(
                            onClick = { navController.navigate("change_email") }
                        ) {
                            Text(
                                text = "Change email address",
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Security Badge
                Card(
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                        .clip(RoundedCornerShape(50.dp))
                        .shadow(
                            elevation = 4.dp,
                            shape = RoundedCornerShape(50.dp)
                        ),
                    shape = RoundedCornerShape(50.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Secure",
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "SECURE BANK-GRADE VERIFICATION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

// Preview Screens
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp", showSystemUi = true)
@Composable
fun EmailVerificationScreenPreviewPhone() {
    PocketTheme {
        EmailVerificationScreen(
            navController = rememberNavController(),
            email = "john.doe@example.com",
            fullName = "John Doe",
            phone = "+254712345678"
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp", showSystemUi = true)
@Composable
fun EmailVerificationScreenPreviewPhoneDark() {
    PocketTheme(darkTheme = true) {
        EmailVerificationScreen(
            navController = rememberNavController(),
            email = "john.doe@example.com",
            fullName = "John Doe",
            phone = "+254712345678"
        )
    }
}

@Preview(showBackground = true, device = "spec:width=600dp,height=960dp", showSystemUi = true)
@Composable
fun EmailVerificationScreenPreviewTabletPortrait() {
    PocketTheme {
        EmailVerificationScreen(
            navController = rememberNavController(),
            email = "john.doe@example.com",
            fullName = "John Doe",
            phone = "+254712345678"
        )
    }
}

@Preview(showBackground = true, device = "spec:width=960dp,height=600dp", showSystemUi = true)
@Composable
fun EmailVerificationScreenPreviewTabletLandscape() {
    PocketTheme {
        EmailVerificationScreen(
            navController = rememberNavController(),
            email = "john.doe@example.com",
            fullName = "John Doe",
            phone = "+254712345678"
        )
    }
}