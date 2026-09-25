package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey
    val bookingId: String = "", // e.g. "HL-9042"
    val patientMobile: String = "",
    val patientName: String = "",
    val patientAddress: String = "",
    val labId: Long = 0L,
    val labName: String = "",
    val testNames: String = "", // comma-separated names
    val totalAmount: Int = 0,
    val bookingDate: String = "", // "Today", "Tomorrow", or "2026-09-24"
    val timeSlot: String = "", // "07:00 AM - 08:00 AM (Fasting)"
    val status: String = "CONFIRMED", // CONFIRMED, PHLEBOTOMIST_ASSIGNED, SAMPLE_COLLECTED, IN_TRANSIT, PROCESSING, REPORT_READY
    val technicianName: String = "",
    val technicianPhone: String = "",
    val sampleBarcode: String = "",
    val paymentMode: String = "Pay on Collection (Cash/UPI)",
    val createdAt: Long = System.currentTimeMillis()
)
