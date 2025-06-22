package com.example.pocket.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Switch
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.pocket.model.Investment
import com.example.pocket.viewmodels.InvestmentViewModel
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import android.graphics.Color as AndroidColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentScreen(
    viewModel: InvestmentViewModel,
    onBack: () -> Unit
) {
    val investments by viewModel.investments.collectAsState()
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var editingInvestment by remember { mutableStateOf<Investment?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var sortOption by remember { mutableStateOf("A–Z") }

    val crimson = Color(0xFFDC143C)
    val total = investments.sumOf { it.amount }
    val avgPerformance = if (investments.isNotEmpty()) investments.map { it.performance }.average() else 0.0

    val filteredInvestments = investments
        .filter { it.name.contains(searchQuery, ignoreCase = true) }
        .sortedWith(
            when (sortOption) {
                "Performance" -> compareByDescending { it.performance }
                "Amount" -> compareByDescending { it.amount }
                else -> compareBy { it.name }
            }
        )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Investments", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        exportInvestmentsToPdf(
                            context = context,
                            investments = investments,
                            totalInvested = total,
                            avgReturn = avgPerformance
                        ) { uri -> uri?.let { sharePdf(context, it) } }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Export PDF", tint = Color.White)
                    }
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Investment", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = crimson)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }, containerColor = crimson) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))
            Text("Manage your investments here", fontSize = 16.sp, color = Color.Gray)

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search Investments") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Sort By:")
                var expanded by remember { mutableStateOf(false) }
                Box {
                    TextButton(onClick = { expanded = true }) {
                        Text(sortOption)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("A–Z", "Amount", "Performance").forEach {
                            DropdownMenuItem(
                                text = { Text(it) },
                                onClick = {
                                    sortOption = it
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text("Total Invested: Ksh ${"%,.2f".format(total)}", fontWeight = FontWeight.Bold, color = crimson)
            Text(
                "Average Return: ${"%.1f".format(avgPerformance)}%",
                color = if (avgPerformance >= 0) Color(0xFF43A047) else Color(0xFFE53935)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Your Investments", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            filteredInvestments.forEach { investment ->
                InvestmentCard(
                    investment = investment,
                    onEdit = {
                        editingInvestment = investment
                        showDialog = true
                    },
                    onDelete = { viewModel.deleteInvestment(investment.id) },
                    onToggleReminder = { viewModel.toggleReminder(investment) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Investment Overview", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            InvestmentPieChart(investments)
            Spacer(modifier = Modifier.height(16.dp))
            InvestmentBarChart(investments)
        }

        if (showDialog) {
            AddOrEditInvestmentDialog(
                context = context,
                investment = editingInvestment,
                onDismiss = {
                    showDialog = false
                    editingInvestment = null
                },
                onSave = { name, amount, perf, goal, maturityDate, reminder, sourceUrl ->
                    if (editingInvestment == null) {
                        viewModel.addInvestment(name, amount, perf, goal, maturityDate, reminder, sourceUrl)
                    } else {
                        viewModel.updateInvestment(editingInvestment!!.id, name, amount, perf, goal, maturityDate, reminder, sourceUrl)
                    }
                    showDialog = false
                    editingInvestment = null
                }
            )
        }
    }
}

fun exportInvestmentsToPdf(
    context: Context,
    investments: List<Investment>,
    totalInvested: Double,
    avgReturn: Double,
    onFinished: (Uri?) -> Unit
) {
    val pdfDocument = PdfDocument()

    val paint = Paint().apply {
        color = AndroidColor.BLACK // Android's Color
        textSize = 30f
    }

    val titlePaint = Paint().apply {
        color = AndroidColor.parseColor("#DC143C")
        textSize = 36f
        typeface = Typeface.DEFAULT_BOLD
    }
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    var page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    var y = 60

    canvas.drawText("POCKET Investment Report", 40f, y.toFloat(), titlePaint)
    y += 40
    canvas.drawText("Date: ${LocalDate.now()}", 40f, y.toFloat(), paint)
    y += 40
    canvas.drawText("Total Invested: Ksh ${"%,.2f".format(totalInvested)}", 40f, y.toFloat(), paint)
    y += 30
    canvas.drawText("Average Return: ${"%.1f".format(avgReturn)}%", 40f, y.toFloat(), paint)
    y += 40

    investments.forEachIndexed { index, investment ->
        if (y > 800) {
            pdfDocument.finishPage(page)
            page = pdfDocument.startPage(pageInfo)
            y = 60
        }

        canvas.drawText(
            "${index + 1}. ${investment.name} — Ksh ${"%,.2f".format(investment.amount)} @ ${investment.performance}%",
            40f, y.toFloat(), paint
        )
        y += 25

        investment.goalAmount?.let {
            canvas.drawText("   Goal: Ksh ${"%,.2f".format(it)}", 60f, y.toFloat(), paint)
            y += 20
        }
        investment.maturityDate?.let {
            canvas.drawText("   Maturity: $it", 60f, y.toFloat(), paint)
            y += 20
        }
        investment.sourceUrl?.let {
            canvas.drawText("   Link: ${it.take(60)}", 60f, y.toFloat(), paint)
            y += 20
        }
        y += 10
    }

    pdfDocument.finishPage(page)

    val file = File(context.cacheDir, "investment_report.pdf")
    pdfDocument.writeTo(FileOutputStream(file))
    pdfDocument.close()

    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    onFinished(uri)
}
fun sharePdf(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    context.startActivity(Intent.createChooser(intent, "Share Investment Report via"))
}

@Composable
fun InvestmentCard(
    investment: Investment,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleReminder: () -> Unit
) {
    val today = LocalDate.now()
    val maturityDate = investment.maturityDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
    val daysLeft = maturityDate?.let { ChronoUnit.DAYS.between(today, it).coerceAtLeast(0) }
    val percentAchieved = ((investment.amount / (investment.goalAmount ?: investment.amount)) * 100).coerceAtMost(100.0)

    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(investment.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
            }
            Text("Amount: Ksh ${"%,.2f".format(investment.amount)}")
            Text("Performance: ${investment.performance}%")

            if (investment.goalAmount != null) {
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = (percentAchieved / 100f).toFloat(),
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFFDC143C),
                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                )
                Text("Goal: ${"%,.2f".format(investment.goalAmount)} | Achieved: ${"%.1f".format(percentAchieved)}%")
            }

            if (daysLeft != null) {
                Text("Maturity in $daysLeft days", color = Color.Gray)
            }

            if (!investment.sourceUrl.isNullOrEmpty()) {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(investment.sourceUrl))
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Link, contentDescription = "Source")
                    Spacer(Modifier.width(4.dp))
                    Text("View Source")
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Monthly Review Reminder")
                Spacer(Modifier.weight(1f))
                Switch(
                    checked = investment.reminderEnabled,
                    onCheckedChange = { onToggleReminder() }
                )
            }

            TextButton(
                onClick = onDelete,
                modifier = Modifier.align(Alignment.End),
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Text("Delete")
            }
        }
    }
}

@Composable
fun AddOrEditInvestmentDialog(
    context: Context,
    investment: Investment?,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        amount: Double,
        performance: Double,
        goalAmount: Double?,
        maturityDate: LocalDate?,
        reminder: Boolean,
        sourceUrl: String?
    ) -> Unit
) {
    val isEditing = investment != null
    val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    var name by remember { mutableStateOf(investment?.name ?: "") }
    var amount by remember { mutableStateOf(investment?.amount?.toString() ?: "") }
    var performance by remember { mutableStateOf(investment?.performance?.toString() ?: "") }
    var goalAmount by remember { mutableStateOf(investment?.goalAmount?.toString() ?: "") }
    var maturityDate by remember {
        mutableStateOf(
            investment?.maturityDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
        )
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var reminder by remember { mutableStateOf(investment?.reminderEnabled ?: true) }
    var sourceUrl by remember { mutableStateOf(investment?.sourceUrl ?: "") }


    if (showDatePicker) {
        LaunchedEffect(Unit) {
            DatePickerDialog(
                context,
                { _, year, month, day ->
                    maturityDate = LocalDate.of(year, month + 1, day)
                    showDatePicker = false
                },
                LocalDate.now().year,
                LocalDate.now().monthValue - 1,
                LocalDate.now().dayOfMonth
            ).show()
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (isEditing) "Edit Investment" else "Add Investment")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") }
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = performance,
                    onValueChange = { performance = it },
                    label = { Text("Performance %") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = goalAmount,
                    onValueChange = { goalAmount = it },
                    label = { Text("Goal Amount (optional)") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )

                OutlinedButton(onClick = { showDatePicker = true }) {
                    Text(maturityDate?.format(formatter) ?: "Pick Maturity Date")
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Monthly Reminder")
                    Switch(checked = reminder, onCheckedChange = { reminder = it })
                }

                OutlinedTextField(
                    value = sourceUrl,
                    onValueChange = { sourceUrl = it },
                    label = { Text("Source Link (optional)") }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amount.toDoubleOrNull()
                val perf = performance.toDoubleOrNull()
                val goal = goalAmount.toDoubleOrNull()

                if (name.isNotBlank() && amt != null && perf != null) {
                    onSave(
                        name.trim(),
                        amt,
                        perf,
                        goal,
                        maturityDate,
                        reminder,
                        sourceUrl.ifBlank { null }
                    )
                    onDismiss()
                }
            }) {
                Text(if (isEditing) "Update" else "Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun InvestmentPieChart(
    investments: List<Investment>
) {
    if (investments.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No investment data", color = Color.Gray)
        }
        return
    }

    val total = investments.sumOf { it.amount }
    if (total == 0.0) return

    val crimsonThemeColors = listOf(
        Color(0xFFDC143C), // crimson
        Color(0xFFE53935), // soft red
        Color(0xFFB71C1C), // dark crimson
        Color(0xFFFFCDD2), // light red
        Color(0xFFD32F2F), // fire red
        Color(0xFF880E4F)  // deep rose
    )

    val sweepAngles = investments.map { ((it.amount / total) * 360f).toFloat() }
    val animatedAngles = sweepAngles.mapIndexed { index, angle ->
        animateFloatAsState(
            targetValue = angle,
            animationSpec = tween(durationMillis = 1000, delayMillis = index * 100),
            label = "AnimatedPie"
        ).value
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            "Asset Distribution",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black
        )
        Spacer(Modifier.height(10.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(16.dp)
        ) {
            var startAngle = -90f
            investments.forEachIndexed { index, investment ->
                drawArc(
                    color = crimsonThemeColors[index % crimsonThemeColors.size],
                    startAngle = startAngle,
                    sweepAngle = animatedAngles[index],
                    useCenter = true
                )
                startAngle += animatedAngles[index]
            }
        }
    }
}

@Composable
fun InvestmentBarChart(investments: List<Investment>) {
    if (investments.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No performance data", color = Color.Gray)
        }
        return
    }

    val max = investments.maxOfOrNull { it.performance.toFloat() } ?: 1f
    val barWidth = 28.dp

    val crimsonThemeColors = listOf(
        Color(0xFFDC143C),
        Color(0xFFE57373),
        Color(0xFFB71C1C),
        Color(0xFFD32F2F),
        Color(0xFFFF8A80),
        Color(0xFF880E4F)
    )

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            "Performance Overview (%)",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color.Black
        )
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            investments.forEachIndexed { index, investment ->
                val ratio = investment.performance.toFloat() / max
                val animatedHeight by animateFloatAsState(
                    targetValue = ratio,
                    animationSpec = tween(durationMillis = 800),
                    label = "PerformanceAnim"
                )

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Box(
                        modifier = Modifier
                            .height((animatedHeight * 150).dp)
                            .width(barWidth)
                            .background(
                                color = crimsonThemeColors[index % crimsonThemeColors.size],
                                shape = RoundedCornerShape(6.dp)
                            )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        investment.name.take(6),
                        fontSize = 12.sp,
                        maxLines = 1,
                        color = Color.Black
                    )
                }
            }
        }
    }
}


