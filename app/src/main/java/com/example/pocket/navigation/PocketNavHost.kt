package com.example.pocket.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.pocket.ui.screens.BillReminderScreen
import com.example.pocket.ui.screens.BudgetPlanningScreen
import com.example.pocket.ui.screens.BudgetSummaryScreen
import com.example.pocket.ui.screens.DashboardScreen
import com.example.pocket.ui.screens.ExpenseTrackerScreen
import com.example.pocket.ui.screens.FinancialGoalsScreen
import com.example.pocket.ui.screens.FinancialReportScreen
import com.example.pocket.ui.screens.HistoryScreen
import com.example.pocket.ui.screens.InvestmentScreen
import com.example.pocket.ui.screens.SplashScreen
import com.example.pocket.ui.screens.auth.ForgotPasswordScreen
import com.example.pocket.ui.screens.auth.LoginScreen
import com.example.pocket.ui.screens.auth.OTPSelectionScreen
import com.example.pocket.ui.screens.auth.OTPVerificationScreen
import com.example.pocket.ui.screens.auth.SignUpScreen
import com.example.pocket.viewmodels.BillReminderViewModel
import com.example.pocket.viewmodels.BudgetViewModel
import com.example.pocket.viewmodels.GoalsViewModel
import com.example.pocket.viewmodels.InvestmentViewModel


@ExperimentalMaterial3Api
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

            composable(
                "otp_verify/{method}/{target}/{fullName}/{email}"
            ) { backStack ->
                val method = backStack.arguments?.getString("method") ?: "email"
                val target = backStack.arguments?.getString("target") ?: ""
                val fullName = backStack.arguments?.getString("fullName") ?: ""
                val email = backStack.arguments?.getString("email") ?: ""

                OTPVerificationScreen(
                    navController = navController,
                    method = method,
                    target = target,
                    fullName = fullName,
                    email = email
                )
            }

            composable("dashboard") { DashboardScreen(navController) }
            composable("budget_planning") {
                BudgetPlanningScreen(
                    navController = navController,
                    onNavigateToDashboard = { navController.navigate("dashboard") },
                    onBudgetSet = { newBudget ->
                    }
                )
            }
            composable("summary") {
                val viewModel: BudgetViewModel = viewModel()
                BudgetSummaryScreen(
                    viewModel = viewModel,
                    onNavigateToHistory = { navController.navigate("history") },
                    onBack = { navController.popBackStack() }
                )
            }
            composable("history") {
                HistoryScreen(onBack = { navController.popBackStack() })
            }
            composable("expense_tracker") {
                ExpenseTrackerScreen(
                    onNavigateToDashboard = {
                        navController.popBackStack("dashboard", inclusive = false)
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable("financial_reports") {
                FinancialReportScreen(
                    onNavigateToDashboard = {
                        navController.popBackStack("dashboard", inclusive = false)
                    }
                )
            }
            composable("financial_goals") {
                val viewModel: GoalsViewModel = viewModel()

                FinancialGoalsScreen(
                    viewModel = viewModel,
                    onBackToDashboard = {
                        navController.popBackStack("dashboard", inclusive = false)
                    }
                )
            }
            composable("bill_reminders") {
                val viewModel: BillReminderViewModel = viewModel()
                BillReminderScreen(
                    viewModel = viewModel,
                    onBackToDashboard = {
                        navController.popBackStack("dashboard", inclusive = false)
                    }
                )
            }
            composable("investment_tracking") {
                val viewModel: InvestmentViewModel = viewModel()
                InvestmentScreen(
                    viewModel = viewModel,
                    onBack = {
                        navController.popBackStack("dashboard", inclusive = false)
                    }
                )
            }
        }
    }
}
