package com.example.data.repository

import com.example.data.AppDatabase
import com.example.data.model.BookingEntity
import com.example.data.model.LabEntity
import com.example.data.model.ReportEntity
import com.example.data.model.TestItemEntity
import com.example.data.model.UserEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

sealed class LoginResult {
    data class Success(val user: UserEntity) : LoginResult()
    data class UserNotFound(val mobile: String) : LoginResult()
    data object InvalidPassword : LoginResult()
    data class RoleMismatch(val user: UserEntity, val attemptedRole: String) : LoginResult()
    data class LabNotApproved(val user: UserEntity) : LoginResult()
}

sealed class RegistrationResult {
    data class Success(val user: UserEntity) : RegistrationResult()
    data class MobileAlreadyRegistered(val mobile: String, val existingRole: String) : RegistrationResult()
}

class HomeLabRepository(private val database: AppDatabase) {

    private val firestore: FirebaseFirestore? = try {
        FirebaseFirestore.getInstance()
    } catch (_: Exception) {
        null
    }

    init {
        startFirestoreSync()
    }

    private fun startFirestoreSync() {
        val db = firestore ?: return
        db.collection("bookings").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            CoroutineScope(Dispatchers.IO).launch {
                for (doc in snapshot.documents) {
                    try {
                        val booking = doc.toObject(BookingEntity::class.java)
                        if (booking != null) {
                            database.bookingDao().insertBooking(booking)
                        }
                    } catch (_: Exception) {}
                }
            }
        }

        db.collection("reports").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            CoroutineScope(Dispatchers.IO).launch {
                for (doc in snapshot.documents) {
                    try {
                        val report = doc.toObject(ReportEntity::class.java)
                        if (report != null) {
                            database.reportDao().insertReport(report)
                        }
                    } catch (_: Exception) {}
                }
            }
        }
    }

    val allLabs: Flow<List<LabEntity>> = database.labDao().getAllLabs()
    val allTests: Flow<List<TestItemEntity>> = database.testDao().getAllTests()
    val allBookings: Flow<List<BookingEntity>> = database.bookingDao().getAllBookings()

    fun getBookingsForPatient(mobile: String): Flow<List<BookingEntity>> =
        database.bookingDao().getBookingsByPatient(mobile)

    fun getBookingsForLab(labId: Long): Flow<List<BookingEntity>> =
        database.bookingDao().getBookingsByLab(labId)

    fun getBookingsForLabMobile(mobile: String): Flow<List<BookingEntity>> =
        database.bookingDao().getBookingsForLabMobile(mobile)

    fun getBookingsForLabUser(labId: Long, labName: String, mobile: String): Flow<List<BookingEntity>> =
        database.bookingDao().getBookingsForLabUser(labId, labName, mobile)

    fun getReportsForPatient(mobile: String): Flow<List<ReportEntity>> =
        database.reportDao().getReportsByPatient(mobile)

    fun getAllReports(): Flow<List<ReportEntity>> =
        database.reportDao().getAllReports()

    fun observeBooking(bookingId: String): Flow<BookingEntity?> =
        database.bookingDao().observeBookingById(bookingId)

    fun observeReport(reportId: String): Flow<ReportEntity?> =
        database.reportDao().observeReportById(reportId)

    suspend fun getUserByMobile(mobile: String): UserEntity? {
        val cleanMobile = normalizeMobile(mobile)
        return database.userDao().getUserByMobile(cleanMobile)
    }

    /**
     * Authenticates user via Mobile Number and Password and enforces role verification.
     */
    suspend fun loginWithPassword(
        mobile: String,
        enteredPassword: String,
        targetRole: String
    ): LoginResult {
        val cleanMobile = normalizeMobile(mobile)
        val existingUser = database.userDao().getUserByMobile(cleanMobile)
            ?: return LoginResult.UserNotFound(cleanMobile)

        if (existingUser.password != enteredPassword.trim()) {
            return LoginResult.InvalidPassword
        }

        if (targetRole == "LAB_PROVIDER") {
            if (existingUser.role != "LAB_PROVIDER") {
                return LoginResult.RoleMismatch(existingUser, "LAB_PROVIDER")
            }
            if (!existingUser.isApproved) {
                return LoginResult.LabNotApproved(existingUser)
            }
            return LoginResult.Success(existingUser)
        } else {
            if (existingUser.role != "PATIENT") {
                return LoginResult.RoleMismatch(existingUser, "PATIENT")
            }
            return LoginResult.Success(existingUser)
        }
    }

    suspend fun registerPatient(
        mobile: String,
        password: String,
        fullName: String,
        dateOfBirth: String,
        address: String
    ): RegistrationResult {
        val cleanMobile = normalizeMobile(mobile)
        val existingUser = database.userDao().getUserByMobile(cleanMobile)
        if (existingUser != null) {
            return RegistrationResult.MobileAlreadyRegistered(cleanMobile, existingUser.role)
        }
        val newUser = UserEntity(
            mobile = cleanMobile,
            password = password.trim(),
            fullName = fullName.trim(),
            role = "PATIENT",
            dateOfBirth = dateOfBirth.trim(),
            address = address.trim(),
            isApproved = true
        )
        val id = database.userDao().insertUser(newUser)
        return RegistrationResult.Success(newUser.copy(id = id))
    }

    suspend fun registerLabProvider(
        mobile: String,
        password: String,
        labName: String,
        licenseNumber: String,
        address: String,
        autoApprove: Boolean = true
    ): RegistrationResult {
        val cleanMobile = normalizeMobile(mobile)
        val existingUser = database.userDao().getUserByMobile(cleanMobile)
        if (existingUser != null) {
            return RegistrationResult.MobileAlreadyRegistered(cleanMobile, existingUser.role)
        }
        val newUser = UserEntity(
            mobile = cleanMobile,
            password = password.trim(),
            fullName = labName.trim(),
            role = "LAB_PROVIDER",
            labName = labName.trim(),
            licenseNumber = licenseNumber.trim(),
            address = address.trim(),
            isApproved = autoApprove
        )
        val id = database.userDao().insertUser(newUser)
        // Also register in labs table
        database.labDao().insertLab(
            LabEntity(
                name = labName.trim(),
                licenseNumber = licenseNumber.trim(),
                contactMobile = cleanMobile,
                address = address.trim(),
                badges = "NABL Certified, High Precision"
            )
        )
        return RegistrationResult.Success(newUser.copy(id = id))
    }

    suspend fun createBooking(
        patientMobile: String,
        patientName: String,
        patientAddress: String,
        lab: LabEntity,
        tests: List<TestItemEntity>,
        bookingDate: String,
        timeSlot: String
    ): BookingEntity {
        val bookingId = "HL-" + (1000 + Random.nextInt(9000))
        val totalAmount = tests.sumOf { it.priceRupees }
        val testNames = tests.joinToString(", ") { it.name }

        val booking = BookingEntity(
            bookingId = bookingId,
            patientMobile = normalizeMobile(patientMobile),
            patientName = patientName,
            patientAddress = patientAddress,
            labId = lab.id,
            labName = lab.name,
            testNames = testNames,
            totalAmount = totalAmount,
            bookingDate = bookingDate,
            timeSlot = timeSlot,
            status = "CONFIRMED",
            paymentMode = "Pay on Collection (Cash/UPI)"
        )
        database.bookingDao().insertBooking(booking)
        try {
            firestore?.collection("bookings")?.document(booking.bookingId)?.set(booking)
        } catch (_: Exception) {}
        return booking
    }

    suspend fun updateBookingStatus(bookingId: String, newStatus: String) {
        database.bookingDao().updateStatus(bookingId, newStatus)
        try {
            firestore?.collection("bookings")?.document(bookingId)?.update("status", newStatus)
        } catch (_: Exception) {}

        if (newStatus == "REPORT_READY") {
            val booking = database.bookingDao().getBookingById(bookingId)
            if (booking != null) {
                val existingReport = database.reportDao().getReportByBookingId(bookingId)
                if (existingReport == null) {
                    generateLabReport(
                        booking = booking,
                        pathologistName = "Dr. S. K. Gupta, MD (Pathology)",
                        overallStatus = "NORMAL",
                        parametersList = listOf(
                            ParameterItem("Hemoglobin (Hb)", "14.5", "g/dL", "13.0 - 17.0", "NORMAL"),
                            ParameterItem("Total Leucocyte Count", "7200", "/cumm", "4000 - 11000", "NORMAL"),
                            ParameterItem("Platelet Count", "250000", "/cumm", "150000 - 450000", "NORMAL"),
                            ParameterItem("Fasting Blood Sugar", "95", "mg/dL", "70 - 100", "NORMAL")
                        ),
                        summaryNotes = "Automated clinical analyzer analysis complete. All parameters within normal physiological limits."
                    )
                }
            }
        }
    }

    suspend fun assignPhlebotomist(
        bookingId: String,
        technicianName: String,
        technicianPhone: String
    ) {
        database.bookingDao().assignTechnician(
            bookingId = bookingId,
            status = "PHLEBOTOMIST_ASSIGNED",
            techName = technicianName,
            techPhone = technicianPhone
        )
        try {
            firestore?.collection("bookings")?.document(bookingId)?.update(
                mapOf(
                    "status" to "PHLEBOTOMIST_ASSIGNED",
                    "technicianName" to technicianName,
                    "technicianPhone" to technicianPhone
                )
            )
        } catch (_: Exception) {}
    }

    suspend fun markSampleCollected(bookingId: String, barcode: String) {
        database.bookingDao().markSampleCollected(
            bookingId = bookingId,
            status = "SAMPLE_COLLECTED",
            barcode = barcode
        )
        try {
            firestore?.collection("bookings")?.document(bookingId)?.update(
                mapOf(
                    "status" to "SAMPLE_COLLECTED",
                    "sampleBarcode" to barcode
                )
            )
        } catch (_: Exception) {}
    }

    suspend fun generateLabReport(
        booking: BookingEntity,
        pathologistName: String,
        overallStatus: String,
        parametersList: List<ParameterItem>,
        summaryNotes: String
    ): ReportEntity {
        val reportId = "REP-" + (1000 + Random.nextInt(9000))
        val now = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        val paramsJson = parametersList.joinToString(";") {
            "${it.name}|${it.value}|${it.unit}|${it.referenceRange}|${it.flag}"
        }

        val report = ReportEntity(
            reportId = reportId,
            bookingId = booking.bookingId,
            patientName = booking.patientName,
            patientMobile = booking.patientMobile,
            patientAge = "28 Yrs / Male",
            labName = booking.labName,
            testName = booking.testNames,
            collectedAt = "Sample Collected on ${booking.bookingDate}",
            reportedAt = now,
            pathologistName = pathologistName,
            overallStatus = overallStatus,
            parametersJson = paramsJson,
            notes = summaryNotes
        )

        database.reportDao().insertReport(report)
        database.bookingDao().updateStatus(booking.bookingId, "REPORT_READY")
        try {
            firestore?.collection("reports")?.document(reportId)?.set(report)
            firestore?.collection("bookings")?.document(booking.bookingId)?.update("status", "REPORT_READY")
        } catch (_: Exception) {}
        return report
    }

    private fun normalizeMobile(mobile: String): String {
        return mobile.replace(Regex("[^0-9]"), "").takeLast(10)
    }
}

data class ParameterItem(
    val name: String,
    val value: String,
    val unit: String,
    val referenceRange: String,
    val flag: String = "NORMAL" // NORMAL, HIGH, LOW
)
