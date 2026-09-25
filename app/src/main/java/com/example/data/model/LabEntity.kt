package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "labs")
data class LabEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val licenseNumber: String,
    val rating: Double = 4.8,
    val reviewCount: Int = 1250,
    val turnaroundHours: Int = 12,
    val contactMobile: String,
    val address: String,
    val badges: String = "NABL Accredited, ICMR Approved"
)
