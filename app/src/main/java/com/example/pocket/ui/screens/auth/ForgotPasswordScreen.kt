package com.example.pocket.ui.screens.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.example.pocket.R
import com.example.pocket.ui.theme.PocketTheme

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    auth: FirebaseAuth = Firebase.auth
) {
    var email by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

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

        Spacer(modifier = Modifier.height(16.dp))

        Text("Reset Your Password", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                message = null
            },
            label = { Text("Email Address") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
            textStyle = LocalTextStyle.current.copy(color = Color.Black),
            colors = TextFieldDefaults.colors(
                unfocusedContainerColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                focusedLabelColor = Color.Gray,
                unfocusedLabelColor = Color.Gray,
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        message?.let {
            Text(it, color = if (it.contains("sent")) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                loading = true
                message = null
                focusManager.clearFocus()

                auth.sendPasswordResetEmail(email.trim())
                    .addOnCompleteListener { task ->
                        loading = false
                        message = if (task.isSuccessful) {
                            "Password reset email sent. Please check your inbox."
                        } else {
                            task.exception?.localizedMessage ?: "Reset failed. Try again."
                        }
                    }
            },
            enabled = email.isNotBlank() && !loading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    color = Color.Red,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Send Reset Email")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Back to Login")
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun ForgotPasswordPreview() {
    PocketTheme {
        ForgotPasswordScreen(rememberNavController())
    }
}