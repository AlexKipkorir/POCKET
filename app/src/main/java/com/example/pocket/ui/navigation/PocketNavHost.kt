package com.example.pocket.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.PaddingValues
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pocket.ui.screens.auth.ForgotPasswordScreen
import com.example.pocket.ui.screens.auth.LoginScreen
import com.example.pocket.ui.screens.auth.SignUpScreen
import com.example.pocket.ui.screens.SplashScreen
import com.example.pocket.ui.screens.auth.OTPSelectionScreen
import com.example.pocket.ui.screens.auth.OTPVerificationScreen

@Composable
fun PocketNavHost(
    navController: NavHostController,
    contentPadding: PaddingValues
) {
    Box(modifier = Modifier.padding(contentPadding)) {
        NavHost(
            navController = navController,
            startDestination = "splash"
        ) {
            composable("splash") { SplashScreen(navController) }
            composable("login") { LoginScreen(navController) }
            composable("signup") { SignUpScreen(navController) }
            composable("forgotPassword") { ForgotPasswordScreen(navController) }
            composable("otp_selection/{email}/{phone}") { backStack ->
                val email = backStack.arguments?.getString("email") ?: ""
                val phone = backStack.arguments?.getString("phone") ?: ""
                OTPSelectionScreen(navController, email, phone)
            }

            composable("otp_verify/{method}/{target}") { backStack ->
                val method = backStack.arguments?.getString("method") ?: "email"  // or "phone"
                val target = backStack.arguments?.getString("target") ?: ""
                OTPVerificationScreen(navController, method, target)
            }




        }
    }
}
