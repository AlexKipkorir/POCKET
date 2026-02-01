package com.example.pocket.ui.screens.plan

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pocket.ui.theme.PrimaryRed
import com.example.pocket.utils.ScreenConfig
import com.example.pocket.utils.calculateResponsivePadding
import com.example.pocket.utils.rememberWindowSize
import com.example.pocket.viewmodels.FinancialReportViewModel
import java.io.File
import java.text.DecimalFormat
import java.time.Month
import androidx.compose.ui.tooling.preview.Preview
import com.example.pocket.ui.theme.PocketTheme

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancialReportScreen(
    onNavigateBack: () -> Unit,
    viewModel: FinancialReportViewModel = viewModel()
) {
    val context = LocalContext.current
    val responsivePadding = calculateResponsivePadding()
    val windowSize = rememberWindowSize()

    var showMonthDropdown by remember { mutableStateOf(false) }
    var showYearDropdown by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    // Collect state flows with lifecycle awareness
    val report by viewModel.selectedReport.collectAsStateWithLifecycle()
    val expenditureBreakdown by viewModel.expenditureBreakdown.collectAsStateWithLifecycle()
    val monthlyInsights by viewModel.monthlyInsights.collectAsStateWithLifecycle()
    val spendingTrends by viewModel.spendingTrends.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        // Initial load
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Monthly Financial Report",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            letterSpacing = 0.1.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBackIos,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { /* Handle more options */ }) {
                        Icon(
                            Icons.Outlined.MoreHoriz,
                            contentDescription = "More options",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Header Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = responsivePadding)
                    .padding(top = 24.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${viewModel.selectedMonth.name} ${viewModel.selectedYear}",
                    fontSize = if (windowSize.screenType.isCompact()) 32.sp else 40.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    lineHeight = 40.sp
                )

                Text(
                    text = "Your intentional spending and saving reflections.",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 8.dp)
                )

                OutlinedButton(
                    onClick = { showMonthDropdown = true },
                    modifier = Modifier.padding(top = 24.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "Change Period",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        Icons.Outlined.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Month Dropdown
                DropdownMenu(
                    expanded = showMonthDropdown,
                    onDismissRequest = { showMonthDropdown = false }
                ) {
                    Month.entries.forEach { month ->
                        DropdownMenuItem(
                            text = { Text(month.name) },
                            onClick = {
                                viewModel.setSelectedMonth(month)
                                showMonthDropdown = false
                            }
                        )
                    }
                }
            }

            // Net Balance Card - Only show if report exists
            report?.let { monthlyReport ->
                NetBalanceCard(
                    report = monthlyReport,
                    modifier = Modifier.padding(horizontal = responsivePadding)
                )
            }

            // Monthly Insights
            MonthlyInsightsSection(
                insights = monthlyInsights,
                modifier = Modifier.padding(
                    horizontal = responsivePadding,
                    vertical = 32.dp
                )
            )

            // Expenditure Breakdown
            ExpenditureBreakdownSection(
                breakdown = expenditureBreakdown,
                totalPercentage = viewModel.getTotalExpenditurePercentage(),
                modifier = Modifier.padding(horizontal = responsivePadding)
            )

            // Spending Trend
            SpendingTrendSection(
                trends = spendingTrends,
                modifier = Modifier.padding(
                    horizontal = responsivePadding,
                    vertical = 32.dp
                )
            )

            // Action Buttons
            ActionButtonSection(
                onShare = { /* Handle share */ },
                onExportPDF = { showExportDialog = true },
                modifier = Modifier.padding(
                    horizontal = responsivePadding,
                    vertical = 32.dp
                )
            )

            // Footer
            Text(
                text = "POCKET FINANCIAL REFLECTION • ${viewModel.getMonthAbbreviation(viewModel.selectedMonth)} ${viewModel.selectedYear}",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
                    .padding(horizontal = responsivePadding),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun NetBalanceCard(
    report: com.example.pocket.viewmodels.MonthlyReport,
    modifier: Modifier = Modifier
) {
    val animatedSavingsPercentage by animateFloatAsState(
        targetValue = report.savingsPercentage.toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "savingsPercentage"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryRed
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            // Background decoration
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(160.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            )

            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header with net balance and savings badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "NET BALANCE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 0.1.sp
                        )
                        Text(
                            text = formatCurrency(report.netBalance),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            lineHeight = 36.sp
                        )
                    }

                    // Savings badge
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(50)),
                        color = Color.White.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "↑",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "SAVED ${report.savingsPercentage.toInt()}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                letterSpacing = 0.1.sp
                            )
                        }
                    }
                }

                // Divider
                Spacer(modifier = Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(Color.White.copy(alpha = 0.1f))
                )

                // Income and Spent
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "TOTAL INCOME",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f),
                            letterSpacing = 0.1.sp
                        )
                        Text(
                            text = formatCurrency(report.totalIncome),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column {
                        Text(
                            text = "TOTAL SPENT",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.6f),
                            letterSpacing = 0.1.sp
                        )
                        Text(
                            text = formatCurrency(report.totalSpent),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyInsightsSection(
    insights: List<com.example.pocket.viewmodels.MonthlyInsight>,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "💡",
                    fontSize = 14.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "MONTHLY INSIGHTS",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                letterSpacing = 0.1.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Insights list
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            insights.forEach { insight ->
                InsightCard(insight = insight)
            }
        }
    }
}

@Composable
fun InsightCard(insight: com.example.pocket.viewmodels.MonthlyInsight) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Icon
            val iconColor = when (insight.color) {
                "orange" -> Color(0xFFFF9800)
                "green" -> Color(0xFF4CAF50)
                else -> MaterialTheme.colorScheme.primary
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = when (insight.icon) {
                        "auto_graph" -> "📈"
                        "stars" -> "⭐"
                        else -> "💡"
                    },
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Message
            Text(
                text = insight.message,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun ExpenditureBreakdownSection(
    breakdown: List<com.example.pocket.viewmodels.ExpenditureCategory>,
    totalPercentage: Double,
    modifier: Modifier = Modifier
) {
    val animatedPercentage by animateFloatAsState(
        targetValue = totalPercentage.toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "totalPercentage"
    )

    // Read composable values OUTSIDE Canvas
    val backgroundCircleColor =
        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    val progressColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurface

    val strokeWidth = 6.dp

    Column(modifier = modifier) {

        // Section header with circular progress
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(
                    text = "EXPENDITURE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                    letterSpacing = 0.1.sp
                )
                Text(
                    text = "Breakdown",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Circular progress indicator
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {

                    val strokePx = strokeWidth.toPx()
                    val radius = size.minDimension / 2 - strokePx / 2

                    // Background circle
                    drawCircle(
                        color = backgroundCircleColor,
                        radius = radius,
                        style = Stroke(width = strokePx)
                    )

                    // Progress arc
                    val sweepAngle = (animatedPercentage / 100f) * 360f
                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(
                            width = strokePx,
                            cap = StrokeCap.Round
                        )
                    )
                }

                Text(
                    text = "${animatedPercentage.toInt()}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Breakdown items
        Column(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            breakdown.forEach { category ->
                ExpenditureItem(category = category)
            }
        }
    }
}

@Composable
fun ExpenditureItem(category: com.example.pocket.viewmodels.ExpenditureCategory) {
    val animatedPercentage by animateFloatAsState(
        targetValue = category.percentage.toFloat(),
        animationSpec = tween(durationMillis = 800),
        label = "categoryPercentage"
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Title and amount
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = category.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = formatCurrency(category.amount),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${category.percentage.toInt()}%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }

        // Progress bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        ) {
            val barColor = when (category.colorVariant) {
                1 -> MaterialTheme.colorScheme.primary
                2 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                3 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.primary
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedPercentage / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
    }
}

@Composable
fun SpendingTrendSection(
    trends: List<com.example.pocket.viewmodels.SpendingTrend>,
    modifier: Modifier = Modifier
) {
    val animatedHeights = trends.mapIndexed { index, trend ->
        animateFloatAsState(
            targetValue = (trend.amount / (trends.maxOfOrNull { it.amount } ?: 1.0)).toFloat(),
            animationSpec = tween(durationMillis = 800, delayMillis = index * 100),
            label = "barHeight$index"
        )
    }

    Column(modifier = modifier) {
        // Section header
        Column {
            Text(
                text = "ANALYSIS",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                letterSpacing = 0.1.sp
            )
            Text(
                text = "Spending Trend",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(top = 24.dp)
        ) {
            // Bars
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                trends.forEachIndexed { index, trend ->
                    val barHeight = animatedHeights[index].value * 120.dp
                    val barColor = if (trend.isCurrent) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .width(16.dp)
                                .height(barHeight)
                                .clip(RoundedCornerShape(4.dp))
                                .background(barColor)
                        )
                    }
                }
            }

            // Month labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                trends.forEach { trend ->
                    Text(
                        text = trend.month,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (trend.isCurrent) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        },
                        letterSpacing = 0.1.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Analysis text
        Text(
            text = "Your spending peaked in February but has stabilized this month. Overall, you are 3% lower than your 6-month average.",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            lineHeight = 20.sp
        )
    }
}

@Composable
fun ActionButtonSection(
    onShare: () -> Unit,
    onExportPDF: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = onShare,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📤",
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Share Summary",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        OutlinedButton(
            onClick = onExportPDF,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.PictureAsPdf,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Export as PDF",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

fun formatCurrency(amount: Double): String {
    val formatter = DecimalFormat("#,###")
    return "Ksh ${formatter.format(amount)}"
}

// Helper extension for responsive sizing
fun ScreenConfig.ScreenType.isCompact(): Boolean {
    return this == ScreenConfig.ScreenType.Compact
}

// PDF Generation functions (update these as needed)
fun generateFinancialReportPdf(
    context: Context,
    title: String,
    content: String
): Uri {
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

    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )
}

fun shareFinancialReport(
    context: Context,
    uri: Uri,
    mime: String = "application/pdf"
) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Report"))
}


@Preview(showBackground = true, name = "Light Mode")
@Composable
fun FinancialReportScreenPreview_Light() {
    PocketTheme(darkTheme = false) {
        FinancialReportScreen(onNavigateBack = {})
    }
}

@Preview(showBackground = true, name = "Dark Mode", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun FinancialReportScreenPreview_Dark() {
    PocketTheme(darkTheme = true) {
        FinancialReportScreen(onNavigateBack = {})
    }
}

@Preview(showBackground = true, name = "Compact Screen", widthDp = 360)
@Composable
fun FinancialReportScreenPreview_Compact() {
    PocketTheme(darkTheme = false) {
        FinancialReportScreen(onNavigateBack = {})
    }
}

@Preview(showBackground = true, name = "Medium Screen", widthDp = 600)
@Composable
fun FinancialReportScreenPreview_Medium() {
    PocketTheme(darkTheme = false) {
        FinancialReportScreen(onNavigateBack = {})
    }
}

@Preview(showBackground = true, name = "Expanded Screen", widthDp = 840)
@Composable
fun FinancialReportScreenPreview_Expanded() {
    PocketTheme(darkTheme = false) {
        FinancialReportScreen(onNavigateBack = {})
    }
}