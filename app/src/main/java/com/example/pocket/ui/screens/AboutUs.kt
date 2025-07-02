package com.example.pocket.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.google.firebase.BuildConfig
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.*
import androidx.compose.runtime.LaunchedEffect
import androidx.core.graphics.toColorInt
import androidx.core.graphics.scale
import com.example.pocket.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutUsScreen(onBack: () -> Unit) {
    val crimson = Color(0xFFDC143C)
    val dark = Color(0xFF121212)
    val white = Color.White
    val context = LocalContext.current
    val versionName = BuildConfig.VERSION_NAME
    var showFaq by remember { mutableStateOf(false) }
    var showEasterEgg by remember { mutableStateOf(false) }
    val uri = remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(uri.value) {
        uri.value?.let {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, it)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share About Us PDF"))
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About Us", color = white) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = white)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = dark)
            )
        },
        bottomBar = {
            Button(
                onClick = {
                    exportAboutUsToPdf(context) { uri ->
                        uri?.let {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "application/pdf"
                                putExtra(Intent.EXTRA_STREAM, it)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(
                                Intent.createChooser(shareIntent, "Share About Us PDF")
                            )
                        } ?: run {
                            Toast.makeText(context, "Failed to generate PDF", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = crimson),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Download, contentDescription = "Export PDF", tint = white)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Export About Us", color = white)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            SectionCard(
                title = "About POCKET Student Finance Tracker",
                content = "POCKET is a powerful finance app designed to help students take control of their money. From managing expenses to setting financial goals, it’s all in your pocket.",
                background = crimson.copy(alpha = 0.05f)
            )

            SectionCard(title = "Key Features", content = null, background = Color(0xFFF5F5F5)) {
                FeatureItem(Icons.Default.AttachMoney, "Budget Planning", "Set financial goals and track spending.")
                FeatureItem(Icons.Default.PieChart, "Expense Tracking", "Categorize and visualize where your money goes.")
                FeatureItem(Icons.Default.Savings, "Savings Goals", "Create and monitor your savings milestones.")
            }

            SectionCard(
                title = "Our Mission",
                content = "To empower students with intuitive, powerful tools that promote financial responsibility and literacy.",
                background = Color(0xFFEDE7F6)
            )

            SectionCard(
                title = "Contact Us",
                content = "📧 support@studentfintracker.com\n📞 +254 702 591 256",
                background = Color(0xFFD0F8CE)
            )

            SectionCard(title = "FAQs", content = null, background = Color(0xFFFFF9C4)) {
                TextButton(onClick = { showFaq = !showFaq }) {
                    Text(if (showFaq) "Hide FAQs" else "Show FAQs", color = crimson)
                }
                AnimatedVisibility(visible = showFaq) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("❓ How do I set a savings goal?\n➡ Go to the Goals tab and tap 'Add Goal'.")
                        Text("❓ Is my data secure?\n➡ Yes! Everything is stored securely on Firebase.")
                        Text("❓ Can I export reports?\n➡ Yes! Use the dashboard to generate PDFs.")
                    }
                }
            }

            SectionCard(
                title = "Newsletter Signup",
                content = "Stay updated with the latest financial tips and updates. Join our newsletter!",
                background = Color(0xFFE1F5FE)
            ) {
                OutlinedTextField(value = "", onValueChange = {}, label = { Text("Email Address") })
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { /* handle signup */ }, colors = ButtonDefaults.buttonColors(crimson)) {
                    Text("Subscribe", color = Color.White)
                }
            }

            SectionCard(
                title = "App Info",
                content = "Version: $versionName",
                background = Color(0xFFE0F7FA)
            ) {
                TextButton(onClick = {
                    val url = "https://studentfintracker.com/privacy"
                    context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                }) {
                    Text("📃 View Privacy Policy", color = crimson)
                }
            }

            SectionCard(
                title = "Get Involved",
                content = null,
                background = Color(0xFFFFF3E0)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Button(
                        onClick = {
                            val packageName = context.packageName
                            val uri = "market://details?id=$packageName".toUri()
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(crimson)
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = Color.White)
                        Spacer(Modifier.width(8.dp))
                        Text("Rate the App", color = Color.White)
                    }

                    OutlinedButton(
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, "Check out POCKET Student Finance Tracker!\nhttps://play.google.com/store/apps/details?id=${context.packageName}")
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        },
                        border = BorderStroke(1.dp, crimson)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = crimson)
                        Spacer(Modifier.width(8.dp))
                        Text("Share App", color = crimson)
                    }
                }
            }

            SectionCard(
                title = "Credits & Contributors",
                content = null,
                background = Color(0xFFE1F5FE)
            ) {
                Column {
                    Text("👨‍💻 Alex Ngeno – Lead Android Developer", fontWeight = FontWeight.SemiBold)
                    Text("🎨 UI/UX Design – Pocket Creative Team")
                    Text("☁️ Backend & Firebase – Open Source Contributors")
                }
            }

            SectionCard(
                title = "Changelog",
                content = "• Added PDF export\n• New onboarding flow\n• UI polish & dark mode fixes\n• About Us page overhaul",
                background = Color(0xFFD1C4E9)
            )

            SectionCard(
                title = "Live Support",
                content = null,
                background = Color(0xFFB3E5FC)
            ) {
                Button(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW,
                        "https://studentfintracker.com/chat".toUri())
                    context.startActivity(intent)
                }) {
                    Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Live Chat Support")
                }
            }

            SectionCard(
                title = "Follow Us",
                content = null,
                background = Color(0xFFFFEBEE)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW,
                            "https://twitter.com/studentfinapp".toUri()))
                    }) { Icon(Icons.Default.Share, contentDescription = "Twitter") }
                    IconButton(onClick = {
                        context.startActivity(Intent(Intent.ACTION_VIEW,
                            "https://github.com/AlexKipkorir/POCKET".toUri()))
                    }) { Icon(Icons.Default.Code, contentDescription = "GitHub") }
                }
            }

            SectionCard(
                title = "Inspire",
                content = "“A budget is telling your money where to go instead of wondering where it went.” – Dave Ramsey",
                background = Color(0xFFF1F8E9)
            )

            SectionCard(
                title = "Explore",
                content = null,
                background = Color(0xFFFAFAFA)
            ) {
                OutlinedButton(onClick = {
                    val intent = Intent(Intent.ACTION_VIEW,
                        "https://studentfintracker.com/app-tour".toUri())
                    context.startActivity(intent)
                }) {
                    Text("Take the App Tour")
                }

                OutlinedButton(onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "mailto:".toUri()
                        putExtra(Intent.EXTRA_EMAIL, arrayOf("support@studentfintracker.com"))
                        putExtra(Intent.EXTRA_SUBJECT, "Feedback on POCKET App")
                    }
                    context.startActivity(intent)
                }) {
                    Text("Submit Feedback")
                }
            }
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

fun exportAboutUsToPdf(
    context: Context,
    onComplete: (Uri?) -> Unit
) {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas

    // Paint styles
    val titlePaint = Paint().apply {
        color = "#DC143C".toColorInt()
        textSize = 18f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        isAntiAlias = true
    }

    val bodyPaint = Paint().apply {
        color = AndroidColor.BLACK
        textSize = 14f
        isAntiAlias = true
    }

    val footerPaint = Paint().apply {
        color = AndroidColor.GRAY
        textSize = 12f
        textAlign = Paint.Align.CENTER
    }

    // Optional: Add app logo
    try {
        val logo = BitmapFactory.decodeResource(context.resources, R.drawable.pocket_logo)
        val scaled = logo.scale(60, 60)
        canvas.drawBitmap(scaled, 500f, 20f, null)
    } catch (e: Exception) {
        // Ignore if logo missing
    }

    // Page header
    canvas.drawText("POCKET | About Us", 40f, 40f, titlePaint)

    var y = 80f
    val maxWidth = 500f
    val lineSpacing = 20f

    fun drawWrappedText(text: String, paint: Paint): Float {
        var yPos = y
        val words = text.split(" ")
        var line = ""

        for (word in words) {
            val temp = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(temp) > maxWidth) {
                canvas.drawText(line, 40f, yPos, paint)
                yPos += lineSpacing
                line = word
            } else {
                line = temp
            }
        }

        if (line.isNotEmpty()) {
            canvas.drawText(line, 40f, yPos, paint)
            yPos += lineSpacing
        }

        return yPos
    }

    fun drawSection(title: String, content: String) {
        canvas.drawText(title, 40f, y, titlePaint)
        y += 24f
        content.split("\n").forEach {
            y = drawWrappedText(it, bodyPaint)
        }
        y += 16f
    }

    drawSection("About POCKET", "Student Finance Tracker helps students manage their finances effortlessly.")
    drawSection("Key Features", "• Budget Planning\n• Expense Tracking\n• Savings Goals")
    drawSection("Our Mission", "To empower students with intuitive, accessible tools for smarter financial decisions.")
    drawSection("Contact Us", "Email: support@studentfintracker.com\nPhone: +254 702 591 256")
    drawSection("Credits", "Designed & built by Alex Ngeno and contributors.")
    drawSection("App Version", "v1.0.0\nPrivacy Policy: studentfintracker.com/privacy")

    // Footer with page number
    canvas.drawText("Page 1", pageInfo.pageWidth / 2f, pageInfo.pageHeight - 40f, footerPaint)

    document.finishPage(page)

    try {
        val file = File(context.cacheDir, "AboutUs.pdf")
        document.writeTo(FileOutputStream(file))
        document.close()

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        onComplete(uri)
    } catch (e: IOException) {
        e.printStackTrace()
        onComplete(null)
    }
}


















