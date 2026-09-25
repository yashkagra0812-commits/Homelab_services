package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingEntity
import com.example.data.model.ReportEntity
import com.example.ui.components.StatusTimelineCard

val STATUS_WORKFLOW = listOf(
    "CONFIRMED",
    "PHLEBOTOMIST_ASSIGNED",
    "SAMPLE_COLLECTED",
    "IN_TRANSIT",
    "PROCESSING",
    "REPORT_READY"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackStatusScreen(
    booking: BookingEntity?,
    onBackClick: () -> Unit,
    onViewReportClick: () -> Unit,
    hasReportReady: Boolean = false,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    if (booking == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active booking selected")
        }
        return
    }

    val currentStatus = booking.status
    val currentIndex = STATUS_WORKFLOW.indexOf(currentStatus).coerceAtLeast(0)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Home Sample") },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("track_status_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
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

            // Booking ID & Lab Info Header
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("booking_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "BOOKING ${booking.bookingId}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Ordered: ${booking.bookingDate} • ${booking.timeSlot}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            color = if (booking.status == "REPORT_READY") Color(0xFFE8F5E9) else MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = booking.status.replace("_", " "),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = if (booking.status == "REPORT_READY") Color(0xFF2E7D32) else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = booking.testNames,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Partner Lab: ${booking.labName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Text(
                        text = "Pickup At: ${booking.patientAddress}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Phlebotomist Card if assigned
            if (booking.technicianName.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE0F2F1))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(Color(0xFF00796B), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Phlebotomist",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Assigned Phlebotomist",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = Color(0xFF004D40)
                            )
                            Text(
                                text = booking.technicianName,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF004D40)
                            )
                            Text(
                                text = "Sterile kit verified • Contact: ${booking.technicianPhone}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = Color(0xFF00695C)
                            )
                        }

                        val context = LocalContext.current
                        IconButton(onClick = {
                            if (booking.technicianPhone.isNotEmpty()) {
                                val cleanPhone = booking.technicianPhone.replace(" ", "")
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$cleanPhone"))
                                context.startActivity(dialIntent)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call",
                                tint = Color(0xFF00796B)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step-by-step Timeline Stepper
            StatusTimelineCard(
                currentStatus = booking.status,
                technicianName = booking.technicianName,
                technicianPhone = booking.technicianPhone,
                sampleBarcode = booking.sampleBarcode.ifEmpty { "VAC-HL-${booking.bookingId.takeLast(4)}-01" }
            )

            // View Report Button if Ready
            if (booking.status == "REPORT_READY" || hasReportReady) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onViewReportClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("view_completed_report_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B873F))
                ) {
                    Icon(imageVector = Icons.Default.Description, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("VIEW VERIFIED LAB REPORT", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}
