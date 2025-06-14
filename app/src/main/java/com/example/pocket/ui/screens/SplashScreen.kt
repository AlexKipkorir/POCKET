package com.example.pocket.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.delay
import com.example.pocket.R
import com.example.pocket.ui.theme.PocketTheme
import androidx.compose.ui.tooling.preview.Preview


@Composable
fun SplashScreen(navController: NavController) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(1000, easing = EaseOutBounce)
        )
        delay(2000)
        navController.navigate("login") {
            popUpTo("splash") { inclusive = true }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.primary
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .scale(scale.value)
                    .size(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pocket_logo),
                    contentDescription = "App Logo"
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "POCKET",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    PocketTheme {
        SplashScreen(navController = rememberNavController())
    }
}
