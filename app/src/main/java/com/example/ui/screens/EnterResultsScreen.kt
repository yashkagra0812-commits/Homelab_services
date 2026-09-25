package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingEntity
import com.example.data.repository.ParameterItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnterResultsScreen(
    booking: BookingEntity?,
    onBackClick: () -> Unit,
    onGenerateReport: (
        booking: BookingEntity,
        pathologistName: String,
        overallStatus: String,
        parameters: List<ParameterItem>,
        notes: String
    ) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    if (booking == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active booking for result entry")
        }
        return
    }

    var pathologistName by remember { mutableStateOf("Dr. Anita Sengupta, MD (Pathology)") }
    var overallStatus by remember { mutableStateOf("NORMAL") }
    var notes by remember {
        mutableStateOf("All parameters analyzed on calibrated automated clinical hematology/biochemistry analyzer. Internal quality controls verified.")
    }

    // Dynamic parameters based on booked test category
    val defaultParamsForBooking = remember(booking.bookingId, booking.testNames) {
        val testNameLower = booking.testNames.lowercase()
        val params = mutableListOf<ParameterItem>()
        if (testNameLower.contains("thyroid") || testNameLower.contains("tsh") || testNameLower.contains("t3")) {
            params.add(ParameterItem("Total Triiodothyronine (T3)", "1.2", "ng/mL", "0.8 - 2.0", "NORMAL"))
            params.add(ParameterItem("Total Thyroxine (T4)", "8.5", "ug/dL", "5.1 - 14.1", "NORMAL"))
            params.add(ParameterItem("TSH Ultra-sensitive", "2.4", "uIU/mL", "0.4 - 4.5", "NORMAL"))
        }
        if (testNameLower.contains("lipid") || testNameLower.contains("cholesterol")) {
            params.add(ParameterItem("Total Cholesterol", "185", "mg/dL", "< 200", "NORMAL"))
            params.add(ParameterItem("Triglycerides", "140", "mg/dL", "< 150", "NORMAL"))
            params.add(ParameterItem("HDL Cholesterol", "52", "mg/dL", "> 40", "NORMAL"))
            params.add(ParameterItem("LDL Cholesterol", "105", "mg/dL", "< 100", "NORMAL"))
        }
        if (testNameLower.contains("diabetes") || testNameLower.contains("sugar") || testNameLower.contains("fbs") || testNameLower.contains("hba1c")) {
            params.add(ParameterItem("Fasting Blood Sugar (FBS)", "92", "mg/dL", "70 - 100", "NORMAL"))
            params.add(ParameterItem("HbA1c (Glycosylated Hb)", "5.6", "%", "< 5.7", "NORMAL"))
        }
        if (testNameLower.contains("vitamin") || testNameLower.contains("vit")) {
            params.add(ParameterItem("Vitamin D (25-OH)", "34.5", "ng/mL", "30.0 - 100.0", "NORMAL"))
            params.add(ParameterItem("Vitamin B12", "410", "pg/mL", "211 - 911", "NORMAL"))
        }
        if (testNameLower.contains("liver") || testNameLower.contains("lft")) {
            params.add(ParameterItem("SGOT (AST)", "24", "U/L", "0 - 35", "NORMAL"))
            params.add(ParameterItem("SGPT (ALT)", "28", "U/L", "0 - 45", "NORMAL"))
            params.add(ParameterItem("Serum Bilirubin Total", "0.8", "mg/dL", "0.2 - 1.2", "NORMAL"))
        }
        if (params.isEmpty() || testNameLower.contains("cbc") || testNameLower.contains("blood")) {
            params.add(ParameterItem("Hemoglobin (Hb)", "14.2", "g/dL", "13.0 - 17.0", "NORMAL"))
            params.add(ParameterItem("Total WBC Count", "6800", "/cumm", "4000 - 11000", "NORMAL"))
            params.add(ParameterItem("Platelet Count", "240000", "/cumm", "150000 - 450000", "NORMAL"))
            params.add(ParameterItem("RBC Count", "4.8", "mil/cumm", "4.5 - 5.5", "NORMAL"))
            params.add(ParameterItem("Packed Cell Volume (PCV)", "42.1", "%", "40.0 - 50.0", "NORMAL"))
            params.add(ParameterItem("ESR (Westergren)", "10", "mm/hr", "0 - 15", "NORMAL"))
        }
        params
    }

    val initialParameters = remember(booking.bookingId) {
        mutableStateListOf<ParameterItem>().apply { addAll(defaultParamsForBooking) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Enter Test Results") },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("enter_results_back_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            onGenerateReport(
                                booking,
                                pathologistName,
                                overallStatus,
                                initialParameters.toList(),
                                notes
                            )
                        },
                        enabled = !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("generate_report_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B873F))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(imageVector = Icons.Default.Verified, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GENERATE & DISPATCH REPORT",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Patient & Sample Meta Card
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Booking: ${booking.bookingId}",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF0277BD)
                            )
                            Text(
                                text = "Patient: ${booking.patientName}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tests: ${booking.testNames}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Mobile: +91 ${booking.patientMobile} (Receives report on WhatsApp)",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Overall Status Selector
            item {
                Text(
                    text = "OVERALL CLINICAL STATUS",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("NORMAL", "ATTENTION", "CRITICAL").forEach { status ->
                        FilterChip(
                            selected = overallStatus == status,
                            onClick = { overallStatus = status },
                            label = { Text(status) },
                            modifier = Modifier.testTag("status_chip_$status")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Parameters Entry Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ANALYTICAL PARAMETERS",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${initialParameters.size} Parameters",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            itemsIndexed(initialParameters) { index, param ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.6f)) {
                            Text(
                                text = param.name,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Ref: ${param.referenceRange} ${param.unit}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Value Input Box
                        OutlinedTextField(
                            value = param.value,
                            onValueChange = { newValue ->
                                initialParameters[index] = param.copy(value = newValue)
                            },
                            modifier = Modifier
                                .width(90.dp)
                                .height(50.dp)
                                .testTag("param_input_$index"),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Flag toggle (NORMAL / HIGH)
                        Surface(
                            onClick = {
                                val nextFlag = when (param.flag) {
                                    "NORMAL" -> "HIGH"
                                    "HIGH" -> "LOW"
                                    else -> "NORMAL"
                                }
                                initialParameters[index] = param.copy(flag = nextFlag)
                            },
                            shape = RoundedCornerShape(6.dp),
                            color = when (param.flag) {
                                "HIGH", "LOW" -> Color(0xFFFFEBEE)
                                else -> Color(0xFFE8F5E9)
                            }
                        ) {
                            Text(
                                text = param.flag,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = when (param.flag) {
                                    "HIGH", "LOW" -> Color(0xFFC62828)
                                    else -> Color(0xFF2E7D32)
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Pathologist & Notes
            item {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = pathologistName,
                    onValueChange = { pathologistName = it },
                    label = { Text("Certifying Pathologist Name") },
                    modifier = Modifier.fillMaxWidth().testTag("pathologist_name_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Clinical Notes & Interpretation") },
                    modifier = Modifier.fillMaxWidth().testTag("clinical_notes_input"),
                    minLines = 3,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
