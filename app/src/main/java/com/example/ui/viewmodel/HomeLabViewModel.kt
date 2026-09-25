package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.model.BookingEntity
import com.example.data.model.LabEntity
import com.example.data.model.ReportEntity
import com.example.data.model.TestItemEntity
import com.example.data.model.UserEntity
import com.example.data.repository.HomeLabRepository
import com.example.data.repository.LoginResult
import com.example.data.repository.RegistrationResult
import kotlinx.coroutines.flow.Flow
import com.example.data.repository.ParameterItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppScreen {
    ROLE_SELECTION,
    PATIENT_LOGIN,
    LAB_LOGIN,
    PATIENT_REGISTER,
    LAB_REGISTER,
    PATIENT_DASHBOARD,
    BOOK_TEST,
    TRACK_STATUS,
    REPORT_DETAIL,
    LAB_DASHBOARD,
    ENTER_RESULTS
}

data class SecurityAlertState(
    val title: String,
    val message: String,
    val isRoleMismatch: Boolean = false,
    val actualRole: String = "",
    val suggestedActionLabel: String = "OK",
    val onConfirmAction: (() -> Unit)? = null
)

data class WhatsAppNotificationData(
    val recipientName: String,
    val recipientPhone: String,
    val messageText: String,
    val isSuccessToast: Boolean = false
)

data class AuthUiState(
    val currentScreen: AppScreen = AppScreen.ROLE_SELECTION,
    val selectedRole: String = "PATIENT", // "PATIENT" or "LAB_PROVIDER"
    val mobileNumber: String = "",
    val password: String = "password123",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val currentUser: UserEntity? = null,
    val securityAlert: SecurityAlertState? = null,
    val whatsAppNotification: WhatsAppNotificationData? = null
)

data class BookingFlowState(
    val selectedLab: LabEntity? = null,
    val selectedTests: Set<TestItemEntity> = emptySet(),
    val selectedDate: String = "Tomorrow, 24 Sep",
    val selectedTimeSlot: String = "07:30 AM - 08:30 AM (Fasting)",
    val customPatientName: String = "",
    val customAddress: String = "",
    val activeTrackingBooking: BookingEntity? = null,
    val activeReportDetail: ReportEntity? = null,
    val activeLabProcessingBooking: BookingEntity? = null
)

class HomeLabViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: HomeLabRepository

    private val _authState = MutableStateFlow(AuthUiState())
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    private val _bookingState = MutableStateFlow(BookingFlowState())
    val bookingState: StateFlow<BookingFlowState> = _bookingState.asStateFlow()

    val allLabs: StateFlow<List<LabEntity>>
    val allTests: StateFlow<List<TestItemEntity>>
    val allBookings: StateFlow<List<BookingEntity>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = HomeLabRepository(db)

        allLabs = repository.allLabs.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allTests = repository.allTests.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        allBookings = repository.allBookings.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    fun getPatientBookings(mobile: String) = repository.getBookingsForPatient(mobile)
    fun getPatientReports(mobile: String) = repository.getReportsForPatient(mobile)
    fun getAllReports() = repository.getAllReports()
    fun getLabBookings(labId: Long) = repository.getBookingsForLab(labId)
    fun getLabBookingsForMobile(mobile: String) = repository.getBookingsForLabMobile(mobile)

    fun getBookingsForLabUser(user: UserEntity?): Flow<List<BookingEntity>> {
        val mobile = user?.mobile ?: ""
        val labName = user?.labName ?: ""
        val labId = user?.id ?: 0L
        return repository.getBookingsForLabUser(labId, labName, mobile)
    }

    fun navigateTo(screen: AppScreen) {
        _authState.update { it.copy(currentScreen = screen, errorMessage = null, successMessage = null) }
    }

    fun selectRole(role: String) {
        _authState.update {
            it.copy(
                selectedRole = role,
                mobileNumber = if (role == "PATIENT") "9876543210" else "9876500000",
                currentScreen = if (role == "PATIENT") AppScreen.PATIENT_LOGIN else AppScreen.LAB_LOGIN,
                errorMessage = null
            )
        }
    }

    fun onMobileChanged(mobile: String) {
        val cleaned = mobile.filter { it.isDigit() }.take(10)
        _authState.update { it.copy(mobileNumber = cleaned, errorMessage = null) }
    }

    fun onPasswordChanged(password: String) {
        _authState.update { it.copy(password = password, errorMessage = null) }
    }

    fun loginWithPassword() {
        val mobile = _authState.value.mobileNumber
        val password = _authState.value.password
        val role = _authState.value.selectedRole

        if (mobile.length < 10) {
            _authState.update { it.copy(errorMessage = "Please enter a valid 10-digit mobile number") }
            return
        }
        if (password.isBlank()) {
            _authState.update { it.copy(errorMessage = "Please enter your password") }
            return
        }

        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.loginWithPassword(mobile, password, role)
            _authState.update { it.copy(isLoading = false) }

            when (result) {
                is LoginResult.Success -> {
                    _authState.update {
                        it.copy(
                            currentUser = result.user,
                            currentScreen = if (result.user.role == "PATIENT") AppScreen.PATIENT_DASHBOARD else AppScreen.LAB_DASHBOARD,
                            successMessage = "Logged in successfully as ${result.user.fullName}"
                        )
                    }
                }
                is LoginResult.UserNotFound -> {
                    _authState.update {
                        it.copy(
                            errorMessage = if (role == "PATIENT") "Account not found. Please create a new patient profile." else "Account not found. Please apply for lab accreditation."
                        )
                    }
                }
                is LoginResult.InvalidPassword -> {
                    _authState.update {
                        it.copy(errorMessage = "Incorrect password. Please check and try again.")
                    }
                }
                is LoginResult.RoleMismatch -> {
                    val user = result.user
                    val attempted = result.attemptedRole
                    val message = if (attempted == "LAB_PROVIDER") {
                        "Access Denied: Number +91 $mobile is registered as a PATIENT, not an accredited Lab Provider."
                    } else {
                        "Notice: Number +91 $mobile is registered as a LAB PROVIDER."
                    }
                    _authState.update {
                        it.copy(
                            securityAlert = SecurityAlertState(
                                title = "Security Role Verification",
                                message = message,
                                isRoleMismatch = true,
                                actualRole = user.role,
                                suggestedActionLabel = "Switch to ${if (user.role == "PATIENT") "Patient" else "Lab"} Mode",
                                onConfirmAction = {
                                    _authState.update { current ->
                                        current.copy(
                                            securityAlert = null,
                                            currentUser = user,
                                            selectedRole = user.role,
                                            currentScreen = if (user.role == "PATIENT") AppScreen.PATIENT_DASHBOARD else AppScreen.LAB_DASHBOARD
                                        )
                                    }
                                }
                            )
                        )
                    }
                }
                is LoginResult.LabNotApproved -> {
                    _authState.update {
                        it.copy(
                            securityAlert = SecurityAlertState(
                                title = "Application Under Review",
                                message = "Your laboratory profile is pending administrative approval.",
                                suggestedActionLabel = "OK",
                                onConfirmAction = {
                                    _authState.update { current -> current.copy(securityAlert = null) }
                                }
                            )
                        )
                    }
                }
            }
        }
    }

    fun dismissSecurityAlert() {
        _authState.update { it.copy(securityAlert = null) }
    }

    fun createPatientProfile(mobile: String, fullName: String, password: String, dob: String, address: String) {
        val cleanMobile = mobile.filter { it.isDigit() }.take(10)
        if (cleanMobile.length < 10 || fullName.isBlank() || password.isBlank() || address.isBlank()) {
            _authState.update { it.copy(errorMessage = "Please fill in all mandatory fields with a valid 10-digit mobile number") }
            return
        }

        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.registerPatient(
                mobile = cleanMobile,
                password = password,
                fullName = fullName,
                dateOfBirth = dob.ifBlank { "01/01/1995" },
                address = address
            )
            when (result) {
                is RegistrationResult.Success -> {
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            mobileNumber = cleanMobile,
                            currentUser = result.user,
                            currentScreen = AppScreen.PATIENT_DASHBOARD,
                            successMessage = "Welcome, ${result.user.fullName}! Profile created."
                        )
                    }
                }
                is RegistrationResult.MobileAlreadyRegistered -> {
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Mobile number +91 $cleanMobile is already registered. Please login with your existing password."
                        )
                    }
                }
            }
        }
    }

    fun registerLabProvider(
        mobile: String,
        labName: String,
        password: String,
        licenseNumber: String,
        address: String,
        autoApprove: Boolean = true
    ) {
        val cleanMobile = mobile.filter { it.isDigit() }.take(10)
        if (cleanMobile.length < 10 || labName.isBlank() || password.isBlank() || licenseNumber.isBlank() || address.isBlank()) {
            _authState.update { it.copy(errorMessage = "Please fill in all mandatory fields with a valid 10-digit mobile number") }
            return
        }

        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.registerLabProvider(
                mobile = cleanMobile,
                password = password,
                labName = labName,
                licenseNumber = licenseNumber,
                address = address,
                autoApprove = autoApprove
            )
            when (result) {
                is RegistrationResult.Success -> {
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            mobileNumber = cleanMobile,
                            currentUser = result.user,
                            currentScreen = AppScreen.LAB_DASHBOARD,
                            successMessage = "Lab provider verified and activated!"
                        )
                    }
                }
                is RegistrationResult.MobileAlreadyRegistered -> {
                    _authState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Mobile number +91 $cleanMobile is already registered as a ${result.existingRole}. Please login using your existing account."
                        )
                    }
                }
            }
        }
    }

    fun logout() {
        _authState.update {
            AuthUiState(
                currentScreen = AppScreen.ROLE_SELECTION,
                selectedRole = "PATIENT",
                mobileNumber = "9876543210"
            )
        }
        _bookingState.update { BookingFlowState() }
    }

    // Booking Flow
    fun startBooking(selectedTest: TestItemEntity? = null) {
        _bookingState.update {
            it.copy(
                selectedLab = allLabs.value.firstOrNull(),
                selectedTests = if (selectedTest != null) setOf(selectedTest) else setOfNotNull(allTests.value.firstOrNull()),
                selectedDate = "Tomorrow, 24 Sep",
                selectedTimeSlot = "07:30 AM - 08:30 AM (Fasting)",
                customPatientName = _authState.value.currentUser?.fullName ?: "Yash Kagra",
                customAddress = _authState.value.currentUser?.address ?: "Flat 402, Green Valley, Gurugram"
            )
        }
        _authState.update { it.copy(currentScreen = AppScreen.BOOK_TEST) }
    }

    fun selectLab(lab: LabEntity) {
        _bookingState.update { it.copy(selectedLab = lab) }
    }

    fun toggleTestSelection(test: TestItemEntity) {
        _bookingState.update { current ->
            val updated = current.selectedTests.toMutableSet()
            if (updated.contains(test)) {
                if (updated.size > 1) updated.remove(test)
            } else {
                updated.add(test)
            }
            current.copy(selectedTests = updated)
        }
    }

    fun setBookingDate(date: String) {
        _bookingState.update { it.copy(selectedDate = date) }
    }

    fun setBookingTimeSlot(slot: String) {
        _bookingState.update { it.copy(selectedTimeSlot = slot) }
    }

    fun confirmBooking() {
        val state = _bookingState.value
        val lab = state.selectedLab ?: allLabs.value.firstOrNull() ?: return
        val tests = state.selectedTests.toList().ifEmpty { allTests.value.take(1) }
        val patientMobile = _authState.value.currentUser?.mobile ?: _authState.value.mobileNumber
        val patientName = state.customPatientName.ifBlank { _authState.value.currentUser?.fullName ?: "Yash Kagra" }
        val address = state.customAddress.ifBlank { _authState.value.currentUser?.address ?: "Gurugram" }

        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }
            val booking = repository.createBooking(
                patientMobile = patientMobile,
                patientName = patientName,
                patientAddress = address,
                lab = lab,
                tests = tests,
                bookingDate = state.selectedDate,
                timeSlot = state.selectedTimeSlot
            )
            _bookingState.update { it.copy(activeTrackingBooking = booking) }
            _authState.update {
                it.copy(
                    isLoading = false,
                    currentScreen = AppScreen.TRACK_STATUS,
                    successMessage = "Booking Confirmed! ID: ${booking.bookingId}"
                )
            }
        }
    }

    fun viewBookingTracking(booking: BookingEntity) {
        _bookingState.update { it.copy(activeTrackingBooking = booking) }
        _authState.update { it.copy(currentScreen = AppScreen.TRACK_STATUS) }
    }

    fun viewReportDetail(report: ReportEntity) {
        _bookingState.update { it.copy(activeReportDetail = report) }
        _authState.update { it.copy(currentScreen = AppScreen.REPORT_DETAIL) }
    }

    fun openEnterResultsForBooking(booking: BookingEntity) {
        _bookingState.update { it.copy(activeLabProcessingBooking = booking) }
        _authState.update { it.copy(currentScreen = AppScreen.ENTER_RESULTS) }
    }

    // Lab Workflow Actions
    fun assignTechnician(bookingId: String, techName: String, techPhone: String) {
        viewModelScope.launch {
            repository.assignPhlebotomist(bookingId, techName, techPhone)
        }
    }

    fun markSampleCollected(bookingId: String, barcode: String) {
        viewModelScope.launch {
            repository.markSampleCollected(bookingId, barcode)
        }
    }

    fun updateBookingStatus(bookingId: String, status: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, status)
        }
    }

    fun generateReportAndNotify(
        booking: BookingEntity,
        pathologistName: String,
        overallStatus: String,
        parameters: List<ParameterItem>,
        notes: String
    ) {
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true) }
            val report = repository.generateLabReport(
                booking = booking,
                pathologistName = pathologistName,
                overallStatus = overallStatus,
                parametersList = parameters,
                summaryNotes = notes
            )
            _authState.update {
                it.copy(
                    isLoading = false,
                    currentScreen = AppScreen.LAB_DASHBOARD,
                    successMessage = "Report ${report.reportId} published successfully!",
                    whatsAppNotification = WhatsAppNotificationData(
                        recipientName = booking.patientName,
                        recipientPhone = "+91 " + booking.patientMobile,
                        messageText = "Hello ${booking.patientName}, your test report for ${booking.testNames} from ${booking.labName} is ready! View your verified report online: homelabcare.in/r/${report.reportId}"
                    )
                )
            }
        }
    }

    fun shareExistingReportOnWhatsApp(report: ReportEntity) {
        _authState.update {
            it.copy(
                whatsAppNotification = WhatsAppNotificationData(
                    recipientName = report.patientName,
                    recipientPhone = if (report.patientMobile.startsWith("+91")) report.patientMobile else "+91 ${report.patientMobile}",
                    messageText = "Hello ${report.patientName}, your test report for ${report.testName} from ${report.labName} is ready! View your verified report online: homelabcare.in/r/${report.reportId}"
                )
            )
        }
    }

    fun dismissWhatsAppDialog() {
        _authState.update { it.copy(whatsAppNotification = null) }
    }
}

class HomeLabViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeLabViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeLabViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
