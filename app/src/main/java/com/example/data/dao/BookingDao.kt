package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BookingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE patientMobile = :mobile ORDER BY createdAt DESC")
    fun getBookingsByPatient(mobile: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE labId = :labId ORDER BY createdAt DESC")
    fun getBookingsByLab(labId: Long): Flow<List<BookingEntity>>

    @Query("SELECT b.* FROM bookings b INNER JOIN labs l ON b.labId = l.id WHERE l.contactMobile = :mobile ORDER BY b.createdAt DESC")
    fun getBookingsForLabMobile(mobile: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE labId = :labId OR labName = :labName OR labId IN (SELECT id FROM labs WHERE contactMobile = :mobile) ORDER BY createdAt DESC")
    fun getBookingsForLabUser(labId: Long, labName: String, mobile: String): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE bookingId = :bookingId LIMIT 1")
    suspend fun getBookingById(bookingId: String): BookingEntity?

    @Query("SELECT * FROM bookings WHERE bookingId = :bookingId LIMIT 1")
    fun observeBookingById(bookingId: String): Flow<BookingEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity)

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Query("UPDATE bookings SET status = :status WHERE bookingId = :bookingId")
    suspend fun updateStatus(bookingId: String, status: String)

    @Query("UPDATE bookings SET status = :status, technicianName = :techName, technicianPhone = :techPhone WHERE bookingId = :bookingId")
    suspend fun assignTechnician(bookingId: String, status: String, techName: String, techPhone: String)

    @Query("UPDATE bookings SET status = :status, sampleBarcode = :barcode WHERE bookingId = :bookingId")
    suspend fun markSampleCollected(bookingId: String, status: String, barcode: String)
}
