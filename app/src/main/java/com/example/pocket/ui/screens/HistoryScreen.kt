package com.example.pocket.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


data class HistoryLog(
    val title: String,
    val description: String,
    val amount: Double,
    val date: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onBack: () -> Unit
) {
    val logs = remember {
        listOf(
            HistoryLog("Added Category", "Added 'Groceries", 4500.0, "2025-06-14"),
            HistoryLog("Edited Category", "Updated 'Rent' from 12k to 13k", 13000.0, "2025-06-13"),
            HistoryLog("Deleted Category", "Removed 'Games'", 0.0, "2025-06-12")
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transaction History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
        ) {
            items(logs) { log ->
                HistoryCard(log)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun HistoryCard(log: HistoryLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(log.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(log.description, fontSize = 14.sp)
            Text("Ksh ${"%.2f".format(log.amount)}", fontWeight = FontWeight.Medium)
            Text("Date: ${log.date}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}