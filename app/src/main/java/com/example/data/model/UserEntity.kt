package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mobile: String = "", // e.g. "9876543210" (normalized 10-digit)
    val password: String = "password123",
    val fullName: String = "",
    val role: String = "PATIENT", // "PATIENT" or "LAB_PROVIDER"
    val dateOfBirth: String = "",
    val address: String = "",
    val isApproved: Boolean = true, // For LAB_PROVIDER: must be admin-approved
    val labName: String = "", // Applicable if role == LAB_PROVIDER
    val licenseNumber: String = "", // Applicable if role == LAB_PROVIDER
    val createdAt: Long = System.currentTimeMillis()
)
