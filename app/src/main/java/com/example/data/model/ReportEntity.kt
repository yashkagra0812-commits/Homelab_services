package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey
    val reportId: String = "", // e.g. "REP-8831"
    val bookingId: String = "",
    val patientName: String = "",
    val patientMobile: String = "",
    val patientAge: String = "28 Yrs / Male",
    val labName: String = "",
    val labLicense: String = "NABL / ICMR Accredited Lab",
    val testName: String = "",
    val collectedAt: String = "",
    val reportedAt: String = "",
    val pathologistName: String = "Dr. S. K. Gupta, MD (Pathology)",
    val overallStatus: String = "NORMAL", // "NORMAL", "ATTENTION", "CRITICAL"
    val parametersJson: String = "", // serialized parameters: name|value|unit|refRange|flag
    val notes: String = "All results cross-checked and verified in automated clinical analyzers. Please correlate clinically."
)
