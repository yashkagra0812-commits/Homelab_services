package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.OtpRecordEntity

@Dao
interface OtpDao {
    @Query("SELECT * FROM otp_verifications WHERE mobile = :mobile ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestOtpForMobile(mobile: String): OtpRecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOtp(otp: OtpRecordEntity): Long

    @Update
    suspend fun updateOtp(otp: OtpRecordEntity)

    @Query("UPDATE otp_verifications SET isVerified = 1 WHERE id = :otpId")
    suspend fun markVerified(otpId: Long)

    @Query("DELETE FROM otp_verifications WHERE mobile = :mobile")
    suspend fun clearOtpsForMobile(mobile: String)
}
