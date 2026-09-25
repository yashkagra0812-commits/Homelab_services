package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TimelineStep(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val statusKey: String
)

val TRACKING_STEPS = listOf(
    TimelineStep("Booking Confirmed", "Order received & slots reserved", Icons.Default.AssignmentTurnedIn, "CONFIRMED"),
    TimelineStep("Phlebotomist Assigned", "Sterile kit prepared for home visit", Icons.Default.Person, "PHLEBOTOMIST_ASSIGNED"),
    TimelineStep("Sample Collected", "Vacutainers barcoded & sealed", Icons.Default.WaterDrop, "SAMPLE_COLLECTED"),
    TimelineStep("Sample in Transit", "Cold-chain transport to central lab", Icons.Default.DirectionsCar, "IN_TRANSIT"),
    TimelineStep("Processing in Lab", "Clinical analyzer testing underway", Icons.Default.Science, "PROCESSING"),
    TimelineStep("Report Generated", "Verified by Pathologist & downloadable", Icons.Default.Description, "REPORT_READY")
)

fun getStepIndex(status: String): Int {
    return when (status) {
        "CONFIRMED" -> 0
        "PHLEBOTOMIST_ASSIGNED" -> 1
        "SAMPLE_COLLECTED" -> 2
        "IN_TRANSIT" -> 3
        "PROCESSING" -> 4
        "REPORT_READY" -> 5
        else -> 0
    }
}

@Composable
fun StatusTimelineCard(
    currentStatus: String,
    technicianName: String = "",
    technicianPhone: String = "",
    sampleBarcode: String = "",
    modifier: Modifier = Modifier
) {
    val currentIndex = getStepIndex(currentStatus)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("status_timeline_card"),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "SAMPLE TRACKING WORKFLOW",
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(14.dp))

            TRACKING_STEPS.forEachIndexed { index, step ->
                val isCompleted = index < currentIndex
                val isCurrent = index == currentIndex
                val isPending = index > currentIndex

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("timeline_step_$index"),
                    verticalAlignment = Alignment.Top
                ) {
                    // Left Column: Dot / Icon + Vertical Connecting Line
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(36.dp)
                    ) {
                        val iconBg = when {
                            isCompleted -> Color(0xFF1B873F)
                            isCurrent -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }

                        val iconColor = when {
                            isCompleted || isCurrent -> Color.White
                            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        }

                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(iconBg),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = iconColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = step.icon,
                                    contentDescription = step.title,
                                    tint = iconColor,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        if (index < TRACKING_STEPS.size - 1) {
                            val lineColor = if (isCompleted) Color(0xFF1B873F) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(if (isCurrent && (technicianName.isNotEmpty() || sampleBarcode.isNotEmpty())) 56.dp else 36.dp)
                                    .background(lineColor)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Right Column: Text Details & Extra Info
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = step.title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = when {
                                    isCurrent -> MaterialTheme.colorScheme.primary
                                    isCompleted -> MaterialTheme.colorScheme.onSurface
                                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
                                }
                            )

                            if (isCurrent) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 9.sp),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = step.subtitle,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isPending) 0.5f else 0.8f)
                        )

                        // Highlight extra info for current step
                        if (isCurrent && step.statusKey == "PHLEBOTOMIST_ASSIGNED" && technicianName.isNotEmpty()) {
                            Surface(
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "$technicianName • $technicianPhone",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }

                        if (step.statusKey == "SAMPLE_COLLECTED" && sampleBarcode.isNotEmpty() && index <= currentIndex) {
                            Text(
                                text = "Barcode: $sampleBarcode (Sealed)",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier.padding(top = 3.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}
