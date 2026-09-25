package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.ReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {
    @Query("SELECT * FROM reports ORDER BY reportedAt DESC")
    fun getAllReports(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE patientMobile = :mobile ORDER BY reportedAt DESC")
    fun getReportsByPatient(mobile: String): Flow<List<ReportEntity>>

    @Query("SELECT * FROM reports WHERE bookingId = :bookingId LIMIT 1")
    suspend fun getReportByBookingId(bookingId: String): ReportEntity?

    @Query("SELECT * FROM reports WHERE reportId = :reportId LIMIT 1")
    fun observeReportById(reportId: String): Flow<ReportEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity)
}
