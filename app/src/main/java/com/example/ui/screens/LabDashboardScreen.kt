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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingEntity
import com.example.data.model.UserEntity

val ON_DUTY_LAB_STAFF = listOf(
    Pair("Rohan Sharma (Sr. Phlebotomist)", "+91 98112 34567"),
    Pair("Amit Kumar (Certified Phlebotomist)", "+91 98765 12345"),
    Pair("Suresh Verma (Sample Collector)", "+91 98100 88776")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LabDashboardScreen(
    user: UserEntity?,
    bookings: List<BookingEntity>,
    onAssignPhlebotomist: (bookingId: String, techName: String, techPhone: String) -> Unit,
    onAdvanceStatus: (bookingId: String, nextStatus: String) -> Unit,
    onEnterResultsClick: (BookingEntity) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val labName = user?.labName?.ifBlank { "Metropolis Central Diagnostics" } ?: "Metropolis Central Diagnostics"
    val licenseNo = user?.licenseNumber?.ifBlank { "NABL-DL-2024-9128" } ?: "NABL-DL-2024-9128"

    var selectedFilter by remember { mutableStateOf("ALL") }
    var showAssignDialogForBooking by remember { mutableStateOf<BookingEntity?>(null) }
    var technicianName by remember { mutableStateOf("Rohan Sharma (Sr. Phlebotomist)") }
    var technicianPhone by remember { mutableStateOf("+91 98112 34567") }

    val filteredBookings = when (selectedFilter) {
        "PENDING" -> bookings.filter { it.status == "CONFIRMED" || it.status == "PHLEBOTOMIST_ASSIGNED" }
        "PROCESSING" -> bookings.filter { it.status == "SAMPLE_COLLECTED" || it.status == "IN_TRANSIT" || it.status == "PROCESSING" }
        "COMPLETED" -> bookings.filter { it.status == "REPORT_READY" }
        else -> bookings
    }

    val pendingCount = bookings.count { it.status == "CONFIRMED" || it.status == "PHLEBOTOMIST_ASSIGNED" }
    val inProcessCount = bookings.count { it.status == "SAMPLE_COLLECTED" || it.status == "IN_TRANSIT" || it.status == "PROCESSING" }
    val readyCount = bookings.count { it.status == "REPORT_READY" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = labName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Approved",
                                tint = Color(0xFF0277BD),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Text(
                            text = "License: $licenseNo • Approved Lab",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogoutClick, modifier = Modifier.testTag("lab_logout_button")) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Metrics Summary
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Metric 1: Pending
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "PENDING",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = Color(0xFFE65100)
                            )
                            Text(
                                text = "$pendingCount",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFFE65100)
                            )
                            Text(
                                text = "Awaiting pickup",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = Color.Gray
                            )
                        }
                    }

                    // Metric 2: Processing
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE1F5FE))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "TESTING",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = Color(0xFF0277BD)
                            )
                            Text(
                                text = "$inProcessCount",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF0277BD)
                            )
                            Text(
                                text = "In laboratory",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = Color.Gray
                            )
                        }
                    }

                    // Metric 3: Ready
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "REPORTS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "$readyCount",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "Verified & sent",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Filter Chips
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("ALL", "PENDING", "PROCESSING", "COMPLETED").forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter) },
                            modifier = Modifier.testTag("filter_$filter")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Bookings List
            if (filteredBookings.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No test requests received yet for ${user?.fullName ?: "your lab"}.\nWhen a patient selects your lab, the booking request will appear here.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                textAlign = TextAlign.Center
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            items(filteredBookings) { booking ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .testTag("lab_booking_card_${booking.bookingId}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = booking.bookingId,
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color(0xFF0277BD)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "• ${booking.patientName}",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Text(
                                    text = "Mobile: +91 ${booking.patientMobile}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                color = when (booking.status) {
                                    "REPORT_READY" -> Color(0xFFE8F5E9)
                                    "PROCESSING" -> Color(0xFFE1F5FE)
                                    else -> Color(0xFFFFF3E0)
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = booking.status.replace("_", " "),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = when (booking.status) {
                                        "REPORT_READY" -> Color(0xFF2E7D32)
                                        "PROCESSING" -> Color(0xFF0277BD)
                                        else -> Color(0xFFE65100)
                                    },
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = booking.testNames,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )

                        Text(
                            text = "Pickup: ${booking.patientAddress}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Slot: ${booking.bookingDate} (${booking.timeSlot})",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (booking.technicianName.isNotEmpty()) {
                            Text(
                                text = "Phlebotomist: ${booking.technicianName} (${booking.technicianPhone})",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = Color(0xFF00695C),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Lab Actions Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            when (booking.status) {
                                "CONFIRMED" -> {
                                    Button(
                                        onClick = {
                                            technicianName = booking.technicianName.ifBlank { "Rohan Sharma (Sr. Phlebotomist)" }
                                            technicianPhone = booking.technicianPhone.ifBlank { "+91 98112 34567" }
                                            showAssignDialogForBooking = booking
                                        },
                                        modifier = Modifier.weight(1f).testTag("assign_tech_button_${booking.bookingId}"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD))
                                    ) {
                                        Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Assign Phlebotomist")
                                    }
                                }
                                "PHLEBOTOMIST_ASSIGNED" -> {
                                    Button(
                                        onClick = { onAdvanceStatus(booking.bookingId, "SAMPLE_COLLECTED") },
                                        modifier = Modifier.weight(1f).testTag("sample_collected_button_${booking.bookingId}"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                                    ) {
                                        Icon(imageVector = Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Mark Sample Collected")
                                    }
                                }
                                "SAMPLE_COLLECTED" -> {
                                    Button(
                                        onClick = { onAdvanceStatus(booking.bookingId, "IN_TRANSIT") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00677D))
                                    ) {
                                        Text("Send to Lab (In Transit)")
                                    }
                                }
                                "IN_TRANSIT" -> {
                                    Button(
                                        onClick = { onAdvanceStatus(booking.bookingId, "PROCESSING") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5E35B1))
                                    ) {
                                        Icon(imageVector = Icons.Default.Science, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Receive & Start Testing")
                                    }
                                }
                                "PROCESSING" -> {
                                    Button(
                                        onClick = { onEnterResultsClick(booking) },
                                        modifier = Modifier.weight(1f).testTag("enter_results_button_${booking.bookingId}"),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B873F))
                                    ) {
                                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Enter Results & Release Report")
                                    }
                                }
                                "REPORT_READY" -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = Color(0xFF2E7D32),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Verified report sent to patient",
                                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                color = Color(0xFF2E7D32)
                                            )
                                        }

                                        FilledTonalButton(
                                            onClick = { onEnterResultsClick(booking) },
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Update")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Assign Phlebotomist Dialog
    if (showAssignDialogForBooking != null) {
        val targetBooking = showAssignDialogForBooking!!
        AlertDialog(
            onDismissRequest = { showAssignDialogForBooking = null },
            modifier = Modifier.testTag("assign_phlebotomist_dialog"),
            shape = RoundedCornerShape(16.dp),
            title = {
                Text(
                    text = "Assign Home Phlebotomist",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Booking: ${targetBooking.bookingId} (${targetBooking.patientName})",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Address: ${targetBooking.patientAddress}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "SELECT FROM ON-DUTY LAB STAFF:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ON_DUTY_LAB_STAFF.forEach { (staffName, staffPhone) ->
                            val isSelected = technicianName == staffName && technicianPhone == staffPhone
                            Surface(
                                onClick = {
                                    technicianName = staffName
                                    technicianPhone = staffPhone
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = staffName,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = staffPhone,
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "OR ENTER CUSTOM STAFF DETAILS:",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = technicianName,
                        onValueChange = { technicianName = it },
                        label = { Text("Phlebotomist Name & Title") },
                        modifier = Modifier.fillMaxWidth().testTag("tech_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = technicianPhone,
                        onValueChange = { technicianPhone = it },
                        label = { Text("Technician Contact Phone") },
                        modifier = Modifier.fillMaxWidth().testTag("tech_phone_input"),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onAssignPhlebotomist(targetBooking.bookingId, technicianName, technicianPhone)
                        showAssignDialogForBooking = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD)),
                    modifier = Modifier.testTag("confirm_assign_tech_button")
                ) {
                    Text("Confirm Assignment")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAssignDialogForBooking = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
