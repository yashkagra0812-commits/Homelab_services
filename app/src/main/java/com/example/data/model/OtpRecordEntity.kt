package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "otp_verifications")
data class OtpRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val mobile: String,
    val otpCode: String,
    val expiresAt: Long,
    val attemptCount: Int = 0,
    val isVerified: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
