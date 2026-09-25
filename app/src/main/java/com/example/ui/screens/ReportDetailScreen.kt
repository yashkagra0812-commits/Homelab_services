package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReportEntity
import com.example.ui.components.WhatsAppDarkGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    report: ReportEntity?,
    onBackClick: () -> Unit,
    onShareWhatsApp: (ReportEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    if (report == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Report not found")
        }
        return
    }

    // Parse parameters: name|value|unit|refRange|flag;
    val parsedParams = report.parametersJson.split(";").filter { it.isNotBlank() }.map { row ->
        val parts = row.split("|")
        object {
            val name = parts.getOrNull(0) ?: ""
            val value = parts.getOrNull(1) ?: ""
            val unit = parts.getOrNull(2) ?: ""
            val refRange = parts.getOrNull(3) ?: ""
            val flag = parts.getOrNull(4) ?: "NORMAL"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Diagnostic Report") },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("report_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onShareWhatsApp(report) }, modifier = Modifier.testTag("report_share_button")) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share via WhatsApp")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = { onShareWhatsApp(report) },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("whatsapp_notify_button"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppDarkGreen)
                    ) {
                        Icon(imageVector = Icons.Default.Message, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Simulate WhatsApp Notify", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Clinical Report Card (Designed like a genuine pathology report)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pathology_report_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Lab Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = report.labName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp
                                ),
                                color = Color(0xFF004D40)
                            )
                            Text(
                                text = report.labLicense,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color.Gray
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "NABL ACCREDITED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = Color(0xFF1B873F)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))
                    Spacer(modifier = Modifier.height(12.dp))

                    // Patient Meta Info
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Patient Name:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = report.patientName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Age / Gender:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = report.patientAge, style = MaterialTheme.typography.bodySmall, color = Color.Black)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Report ID:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = report.reportId, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace), color = Color(0xFF004D40))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Reported On:", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Text(text = report.reportedAt, style = MaterialTheme.typography.bodySmall, color = Color.Black)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(thickness = 1.dp, color = Color(0xFFE0E0E0))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Test Name
                    Text(
                        text = "DEPARTMENT OF CLINICAL PATHOLOGY",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = Color(0xFF00695C)
                    )
                    Text(
                        text = report.testName,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Parameters Table Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF5F5F5), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Investigation", modifier = Modifier.weight(1.8f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.DarkGray)
                        Text(text = "Result", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.DarkGray, textAlign = TextAlign.Center)
                        Text(text = "Ref Range", modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color.DarkGray, textAlign = TextAlign.End)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Parameters List
                    parsedParams.forEach { param ->
                        val isAbnormal = param.flag != "NORMAL"
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1.8f)) {
                                Text(
                                    text = param.name,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = Color.Black
                                )
                                Text(
                                    text = param.unit,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color.Gray
                                )
                            }

                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = param.value,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAbnormal) Color(0xFFC62828) else Color.Black
                                    )
                                )
                                if (isAbnormal) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (param.flag == "HIGH") "▲" else "▼",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = Color(0xFFC62828)
                                    )
                                }
                            }

                            Text(
                                text = param.refRange,
                                modifier = Modifier.weight(1.3f),
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = Color.DarkGray,
                                textAlign = TextAlign.End
                            )
                        }
                        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Clinical Impression
                    Text(
                        text = "CLINICAL REMARKS & INTERPRETATION:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.DarkGray
                    )
                    Text(
                        text = report.notes,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = Color.DarkGray,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Pathologist Digital Sign-off
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Digitally Signed",
                                    tint = Color(0xFF1B873F),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Digitally Verified",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFF1B873F)
                                )
                            }
                            Text(
                                text = report.pathologistName,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.Black
                            )
                            Text(
                                text = "Consultant Pathologist, Reg. #MCI-28841",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color.Gray
                            )
                        }

                        Box(
                            modifier = Modifier
                                .border(1.dp, Color(0xFF1B873F), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "OFFICIAL REPORT",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 9.sp
                                ),
                                color = Color(0xFF1B873F)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
