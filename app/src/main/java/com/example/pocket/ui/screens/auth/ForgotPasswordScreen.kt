// ui/screens/auth/ForgotPasswordScreen.kt
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.pocket.R
import com.example.pocket.ui.theme.PocketTheme
import com.example.pocket.ui.theme.PrimaryRed
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    auth: FirebaseAuth = Firebase.auth
) {
    var email by rememberSaveable { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    // Clear message when user starts typing
    LaunchedEffect(email) {
        if (message != null) {
            message = null
            isSuccess = false
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

                }
                Text(
                    text = "POCKET",
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
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.Top)
            ) {
                // Reset Password Card
                Card(
                    modifier = Modifier
                        .width(400.dp)
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
                            .padding(horizontal = 32.dp, vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(32.dp)
                    ) {
                        // Icon & Header Section
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(50.dp))
                                    .background(PrimaryRed.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LockReset,
                                    contentDescription = "Reset Password",
                                    tint = PrimaryRed,
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Forgot Password",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = "No worries! Enter your email address below and we will send you a link to reset your password.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 24.sp
                                )
                            }
                        }

                        // Email Input Section
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "EMAIL ADDRESS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(start = 4.dp)
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = {
                                    Text(
                                        text = "example@email.com",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = "Email",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                shape = RoundedCornerShape(50.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = PrimaryRed.copy(alpha = 0.05f),
                                    unfocusedContainerColor = PrimaryRed.copy(alpha = 0.05f),
                                    focusedIndicatorColor = PrimaryRed.copy(alpha = 0.5f),
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = PrimaryRed,
                                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                                ),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Done
                                ),
                                keyboardActions = KeyboardActions(
                                    onDone = {
                                        focusManager.clearFocus()
                                        if (email.isNotBlank()) {
                                            sendResetEmail(email, auth) { success, msg ->
                                                message = msg
                                                isSuccess = success
                                            }
                                        }
                                    }
                                ),
                                enabled = !isLoading && !isSuccess
                            )
                        }

                        // Message Display
                        if (message != null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(50.dp))
                                        .background(
                                            if (isSuccess) Color(0xFF4CAF50).copy(alpha = 0.1f)
                                            else MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(
                                            id = if (isSuccess) R.drawable.ic_check_circle
                                            else R.drawable.ic_error
                                        ),
                                        contentDescription = if (isSuccess) "Success" else "Error",
                                        tint = if (isSuccess) Color(0xFF4CAF50)
                                        else MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }

                                Text(
                                    text = message!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (isSuccess) Color(0xFF4CAF50)
                                    else MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )
                            }
                        }

                        // Action Button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                sendResetEmail(email, auth) { success, msg ->
                                    message = msg
                                    isSuccess = success
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
                            enabled = email.isNotBlank() && !isLoading && !isSuccess
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = if (isSuccess) "Reset Link Sent" else "Send Reset Link",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        }

                        // Additional Instructions (only show on success)
                        if (isSuccess) {
                            Text(
                                text = "Check your inbox and follow the instructions in the email to reset your password.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }

                // Back to Login Link
                TextButton(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = PrimaryRed
                    ),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Back to Login",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        // Bottom Gradient Decoration
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.BottomCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            PrimaryRed.copy(alpha = 0.05f)
                        )
                    )
                )
        )
    }
}

private fun sendResetEmail(
    email: String,
    auth: FirebaseAuth,
    onComplete: (Boolean, String) -> Unit
) {
    auth.sendPasswordResetEmail(email.trim())
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(true, "Password reset email sent! Please check your inbox and follow the instructions.")
            } else {
                val errorMessage = when {
                    email.isBlank() -> "Please enter your email address"
                    !email.contains("@") -> "Please enter a valid email address"
                    else -> task.exception?.localizedMessage ?: "Failed to send reset email. Please try again."
                }
                onComplete(false, errorMessage)
            }
        }
}

// Preview Screens
@Preview(showBackground = true, device = "spec:width=411dp,height=891dp", showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreviewPhone() {
    PocketTheme {
        ForgotPasswordScreen(
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp", showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreviewPhoneDark() {
    PocketTheme(darkTheme = true) {
        ForgotPasswordScreen(
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, device = "spec:width=600dp,height=960dp", showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreviewTabletPortrait() {
    PocketTheme {
        ForgotPasswordScreen(
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, device = "spec:width=960dp,height=600dp", showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreviewTabletLandscape() {
    PocketTheme {
        ForgotPasswordScreen(
            navController = rememberNavController()
        )
    }
}

@Preview(showBackground = true, device = "spec:width=840dp,height=1180dp", showSystemUi = true)
@Composable
fun ForgotPasswordScreenPreviewLargeTablet() {
    PocketTheme {
        ForgotPasswordScreen(
            navController = rememberNavController()
        )
    }
}