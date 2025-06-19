package com.example.pocket.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pocket.model.BudgetCategory
import com.example.pocket.viewmodels.BudgetViewModel
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import android.content.Context
import android.content.Intent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.Composable
import com.example.pocket.utils.getColorForCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetSummaryScreen(
    viewModel: BudgetViewModel,
    onNavigateToHistory: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val plannedBudget = viewModel.plannedBudget.collectAsState().value
    var triggerShare by remember { mutableStateOf(false) }

    // Fake data (replace with live ViewModel later)
    val categories = listOf(
        BudgetCategory(name = "Food", amount = 4500.0),
        BudgetCategory(name = "Rent", amount = 12000.0),
        BudgetCategory(name = "Transport", amount = 3000.0),
        BudgetCategory(name = "Entertainment", amount = 2000.0),
        BudgetCategory(name = "Misc", amount = 1500.0)
    )
    val totalBudget = categories.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Budget Summary") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, contentDescription = "History")
                    }
                    IconButton(onClick = {
                        triggerShare = true
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share Budget")
                    }
                }
            )
            if (triggerShare) {
                LaunchedEffect(Unit) {
                    shareBudget(context, categories, plannedBudget)
                    triggerShare = false
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "Total Budget: Ksh ${"%.2f".format(totalBudget)}",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(categories) { category ->
                SummaryCard(category)
            }

            item {
                Text("Spending Breakdown", fontWeight = FontWeight.SemiBold)
            }

            item {
                PieChartView(categories = categories)
            }

            item {
                PieChartLegend(categories = categories)
            }
        }
    }
}


@Composable
fun SummaryCard(category: BudgetCategory) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F1F1))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Colored dot indicator
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(getColorForCategory(category.name), CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = category.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "Ksh ${"%.2f".format(category.amount)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

fun shareBudget(
    context: Context,
    categories: List<BudgetCategory>,
    plannedBudget: Double
) {
    val total = categories.sumOf { it.amount }
    val budgetText = buildString {
        append("📊 Budget Summary (POCKET App)\n\n")
        append("Planned Budget: Ksh ${"%.2f".format(plannedBudget)}\n")
        append("Total Budget: Ksh ${"%.2f".format(total)}\n")
        append("Remaining: Ksh ${"%.2f".format(plannedBudget - total)}\n\n")
        append("🔸 Categories:\n")
        categories.forEach {
            append("• ${it.name}: Ksh ${"%.2f".format(it.amount)}\n")
        }
    }

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, budgetText)
        type = "text/plain"
    }

    val shareIntent = Intent.createChooser(sendIntent, "Share Budget Summary via")
    context.startActivity(shareIntent)
}
@Composable
fun PieChartView(categories: List<BudgetCategory>) {
    if (categories.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No categories yet", color = Color.Gray)
        }
    } else {
        val total = categories.sumOf { it.amount }
        val proportions = remember(categories) {
            categories.map { it.amount / total }
        }
        val sweepAngles = proportions.map { it.toFloat() * 360f }

        val animatedSweepAngles = sweepAngles.mapIndexed { index, sweep ->
            animateFloatAsState(
                targetValue = sweep,
                animationSpec = tween(
                    durationMillis = 800,
                    delayMillis = index * 100,
                    easing = FastOutSlowInEasing
                ),
                label = "AnimatedSweep"
            ).value
        }

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            var startAngle = -90f
            categories.forEachIndexed { index, category ->
                drawArc(
                    color = getColorForCategory(category.name),
                    startAngle = startAngle,
                    sweepAngle = animatedSweepAngles[index],
                    useCenter = true
                )
                startAngle += animatedSweepAngles[index]
            }
        }
    }
}

fun getColorForCategory(name: String): Color {
    return when (name.lowercase()) {
        "food" -> Color(0xFFE57373)
        "rent" -> Color(0xFF64B5F6)
        "transport" -> Color(0xFF81C784)
        "entertainment" -> Color(0xFFFFB74D)
        else -> Color(0xFFBA68C8) // default
    }
}

@Composable
fun PieChartLegend(categories: List<BudgetCategory>) {
    Column(modifier = Modifier.fillMaxWidth()) {
        categories.forEach {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(getColorForCategory(it.name), shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("${it.name}: Ksh ${"%.2f".format(it.amount)}")
            }
        }
    }
}
