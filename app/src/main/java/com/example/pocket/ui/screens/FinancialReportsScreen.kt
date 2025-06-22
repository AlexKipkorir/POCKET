package com.example.pocket.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.pocket.model.BudgetCategory
import com.example.pocket.ui.theme.crimson
import java.io.File
import java.time.Month
import java.time.Year
import com.example.pocket.utils.getColorForCategory
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll



@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportScreen(onNavigateToDashboard: () -> Unit) {
    var dialogTitle by remember { mutableStateOf("") }
    var dialogContent by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current

    val months = Month.entries.toTypedArray()
    val currentYear = Year.now().value

    var selectedMonth by remember { mutableStateOf(Month.JANUARY) }
    var selectedYear by remember { mutableIntStateOf(currentYear) }

    var monthExpanded by remember { mutableStateOf(false) }
    var yearExpanded by remember { mutableStateOf(false) }

    val pieCategories = listOf(
        BudgetCategory("Food", 4500.0),
        BudgetCategory("Rent", 12000.0),
        BudgetCategory("Transport", 3000.0),
        BudgetCategory("Entertainment", 1500.0),
        BudgetCategory("Others", 1000.0)
    )

    val barData = listOf(
        "Jan" to 25000f,
        "Feb" to 30000f,
        "Mar" to 27000f,
        "Apr" to 32000f,
        "May" to 22000f
    )

    val lineData = listOf(20000f, 24000f, 26000f, 29000f, 31000f)

    val scrollState = rememberScrollState()


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Reports") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = crimson,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            Text(
                "Financial Reports",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = crimson
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Get insights into your spending and savings patterns over time.",
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Month Dropdown
                Box {
                    OutlinedButton(onClick = { monthExpanded = true }) {
                        Text(selectedMonth.name)
                    }
                    DropdownMenu(
                        expanded = monthExpanded,
                        onDismissRequest = { monthExpanded = false }
                    ) {
                        months.forEach { month ->
                            DropdownMenuItem(
                                text = { Text(month.name) },
                                onClick = {
                                    selectedMonth = month
                                    monthExpanded = false
                                    // Optional: call viewModel filter
                                    // viewModel.filterReports(selectedMonth, selectedYear)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Year Dropdown
                Box {
                    OutlinedButton(onClick = { yearExpanded = true }) {
                        Text(selectedYear.toString())
                    }
                    DropdownMenu(
                        expanded = yearExpanded,
                        onDismissRequest = { yearExpanded = false }
                    ) {
                        (2022..currentYear).forEach { year ->
                            DropdownMenuItem(
                                text = { Text(year.toString()) },
                                onClick = {
                                    selectedYear = year
                                    yearExpanded = false
                                    // Optional: call viewModel filter
                                    // viewModel.filterReports(selectedMonth, selectedYear)
                                }
                            )
                        }
                    }
                }
            }

            ReportCard(
                icon = Icons.Default.BarChart,
                title = "Monthly Summary",
                description = "View a summary of your income and expenses for the month.",
                onClick = {
                    dialogTitle = "Monthly Summary"
                    dialogContent = "Income: Ksh 20,000\nExpenses: Ksh 15,000"
                    showDialog = true
                }
            )

            ReportCard(
                icon = Icons.Default.PieChart,
                title = "Expense Breakdown",
                description = "Analyze your expenses by category to see where your money goes.",
                onClick = {
                    dialogTitle = "Expense Breakdown"
                    dialogContent = """
                        • Food: Ksh 4,500
                        • Rent: Ksh 8,000
                        • Transport: Ksh 2,000
                        • Other: Ksh 1,000  
                    """.trimIndent()
                    showDialog = true
                }
            )

            ReportCard(
                icon = Icons.Default.Savings,
                title = "Savings Progress",
                description = "Track your savings progress toward your financial goals.",
                onClick = {
                    dialogTitle = "Savings Progress"
                    dialogContent = "Goal: Ksh 10,000\nSaved: Ksh 6,500"
                    showDialog = true
                }
            )

            ReportCard(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                title = "Income Trends",
                description = "See your income growth over time.",
                onClick = {
                    dialogTitle = "Income Trends"
                    dialogContent = """
                        Jan: Ksh 18,000
                        Feb: Ksh 19,500
                        Mar: Ksh 21,000
                        Apr: Ksh 20,500
                        May: Ksh 21,700
                    """.trimIndent()
                    showDialog = true
                }
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text("Expense Breakdown", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            ExpensePieChart(categories = pieCategories)
            PieChartLegend(categories = pieCategories)

            Spacer(modifier = Modifier.height(24.dp))
            Text("Monthly Spending", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            MonthlyBarChart(data = barData)

            Spacer(modifier = Modifier.height(24.dp))
            Text("Income Trends", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            IncomeLineChart(points = lineData)

            Spacer(modifier = Modifier.height(32.dp))


            Button(
                onClick = onNavigateToDashboard,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Back to Dashboard")
            }

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text(dialogTitle) },
                    text = { Text(dialogContent) },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                val uri = generateBudgetPdf(context, dialogTitle, dialogContent)
                                shareFile(context, uri)
                                showDialog = false
                            }
                        ) {
                            Text("Share")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Close")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ReportCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = crimson, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(description, fontSize = 14.sp, color = Color.DarkGray)
            }
            TextButton(onClick = onClick) {
                Text("View")
            }
        }
    }
}

fun generateBudgetPdf(context: Context, title: String, content: String): Uri {
    val pdf = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
    val page = pdf.startPage(pageInfo)
    val canvas = page.canvas

    val paint = Paint().apply {
        textSize = 12f
    }

    canvas.drawText(title, 10f, 25f, paint)

    val lines = content.split("\n")
    var y = 50f
    for (line in lines) {
        canvas.drawText(line, 10f, y, paint)
        y += 20f
    }

    pdf.finishPage(page)

    val file = File(context.cacheDir, "$title.pdf")
    pdf.writeTo(file.outputStream())
    pdf.close()

    return FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
}

fun shareFile(context: Context, uri: Uri, mime: String = "application/pdf") {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Report"))
}

@Composable
fun ExpensePieChart(
    categories: List<BudgetCategory>
) {
    if (categories.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No expense data", color = Color.Gray)
        }
        return
    }

    val total = categories.sumOf { it.amount }
    val sweepAngles = categories.map { ((it.amount / total) * 360f).toFloat() }

    val animatedAngles = sweepAngles.mapIndexed { index, angle ->
        animateFloatAsState(
            targetValue = angle,
            animationSpec = tween(durationMillis = 1000, delayMillis = index * 100),
            label = "AnimatedPie"
        ).value
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(16.dp)
    ) {
        var startAngle = -90f
        categories.forEachIndexed { index, category ->
            drawArc(
                color = getColorForCategory(category.name),
                startAngle = startAngle,
                sweepAngle = animatedAngles[index],
                useCenter = true
            )
            startAngle += animatedAngles[index]
        }
    }
}

@Composable
fun MonthlyBarChart(
    data: List<Pair<String, Float>>
) {
    val max = data.maxOfOrNull { it.second } ?: 1f
    val barWidth = 24.dp

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            data.forEach { (label, value) ->
                val barHeightRatio = value / max
                val animatedHeight by animateFloatAsState(
                    targetValue = barHeightRatio,
                    animationSpec = tween(durationMillis = 800),
                    label = "BarHeightAnim"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .height((animatedHeight * 150).dp)
                            .width(barWidth)
                            .background(Color(0xFFE53935), shape = RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(label, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun IncomeLineChart(
    points: List<Float>
) {
    if (points.isEmpty()) return

    val maxY = (points.maxOrNull() ?: 1f)
    val path = Path()

    val animatedPoints = points.mapIndexed { index, point ->
        animateFloatAsState(
            targetValue = point,
            animationSpec = tween(800, delayMillis = index * 100),
            label = "LinePointAnim"
        ).value
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(16.dp)
    ) {
        val width = size.width
        val height = size.height
        val pointSpacing = width / (points.size - 1)

        animatedPoints.forEachIndexed { i, value ->
            val x = i * pointSpacing
            val y = height - (value / maxY) * height

            if (i == 0) path.moveTo(x, y)
            else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = Color(0xFFD32F2F),
            style = Stroke(width = 4f, cap = StrokeCap.Round)
        )

        animatedPoints.forEachIndexed { i, value ->
            val x = i * pointSpacing
            val y = height - (value / maxY) * height

            drawCircle(
                color = Color(0xFFD32F2F),
                radius = 6f,
                center = Offset(x, y)
            )
        }
    }
}
























