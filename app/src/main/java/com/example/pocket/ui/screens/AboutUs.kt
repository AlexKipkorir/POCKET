package com.example.pocket.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(onBack: () -> Unit) {
    val crimson = Color(0xFFDC143C)
    val dark = Color(0xFF121212)
    val white = Color.White

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Us", color = white) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = white
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = dark
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            SectionCard(
                title = "About POCKET Student Finance Tracker",
                content = "Student Finance Tracker is a powerful tool designed to help students manage their finances effortlessly. Whether it's tracking monthly expenses, planning a budget, or managing multiple accounts, our app provides all the necessary features to stay on top of your financial game!",
                background = crimson.copy(alpha = 0.05f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            SectionCard(
                title = "Key Features",
                content = null,
                background = Color(0xFFECEFF1)
            ) {
                FeatureItem(Icons.Default.AttachMoney, "Budget Planning", "Set financial goals and track spending.")
                FeatureItem(Icons.Default.PieChart, "Expense Tracking", "Categorize and track where your money goes.")
                FeatureItem(Icons.Default.Savings, "Savings Goals", "Create and monitor your savings progress.")
            }

            Spacer(modifier = Modifier.height(20.dp))

            SectionCard(
                title = "Our Mission",
                content = "Our mission is to empower students with the financial tools they need to succeed. Financial literacy and management should be simple, intuitive, and accessible to all.",
                background = Color(0xFFEDE7F6)
            )

            Spacer(modifier = Modifier.height(20.dp))

            SectionCard(
                title = "Contact Us",
                content = "Email: support@studentfintracker.com\\nPhone: +254 702 591 256",
                background = Color(0xFFD0F8CE)
            )

            Spacer(modifier = Modifier.height(20.dp))

            SectionCard(
                title = "Feedback",
                content = "We value your input. Please share your suggestions or feedback to help us improve.",
                background = Color(0xFFFFF59D)
            )
        }
    }
}

@Composable
fun SectionCard(
    title: String,
    content: String?,
    background: Color,
    contentSlot: @Composable (ColumnScope.() -> Unit)? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = background)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            content?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(it, fontSize = 16.sp)
            }
            contentSlot?.invoke(this)
        }
    }
}

@Composable
fun FeatureItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(modifier = Modifier.padding(vertical = 8.dp)) {
        Icon(icon, contentDescription = title, tint = Color(0xFFDC143C),
            modifier = Modifier.size(30.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title.uppercase(),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }

}



















