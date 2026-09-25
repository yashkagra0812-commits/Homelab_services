package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.WhatsAppSimulationDialog
import com.example.ui.screens.BookTestScreen
import com.example.ui.screens.EnterResultsScreen
import com.example.ui.screens.LabDashboardScreen
import com.example.ui.screens.LabRegistrationScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PatientDashboardScreen
import com.example.ui.screens.PatientRegistrationScreen
import com.example.ui.screens.ReportDetailScreen
import com.example.ui.screens.RoleSelectionScreen
import com.example.ui.screens.TrackStatusScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.HomeLabViewModel
import com.example.ui.viewmodel.HomeLabViewModelFactory

class MainActivity : ComponentActivity() {

    private val viewModel: HomeLabViewModel by viewModels {
        HomeLabViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                HomeLabCareApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun HomeLabCareApp(viewModel: HomeLabViewModel) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val bookingState by viewModel.bookingState.collectAsStateWithLifecycle()

    val labs by viewModel.allLabs.collectAsStateWithLifecycle()
    val tests by viewModel.allTests.collectAsStateWithLifecycle()
    val allBookings by viewModel.allBookings.collectAsStateWithLifecycle()

    val patientMobile = authState.currentUser?.mobile ?: authState.mobileNumber
    val patientBookings by viewModel.getPatientBookings(patientMobile).collectAsStateWithLifecycle(initialValue = emptyList())
    val patientReports by viewModel.getPatientReports(patientMobile).collectAsStateWithLifecycle(initialValue = emptyList())

    val labBookings by viewModel.getBookingsForLabUser(authState.currentUser).collectAsStateWithLifecycle(initialValue = emptyList())
    val allReports by viewModel.getAllReports().collectAsStateWithLifecycle(initialValue = emptyList())

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(authState.successMessage) {
        authState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // System Back Navigation Handling
    BackHandler(enabled = authState.currentScreen != AppScreen.ROLE_SELECTION) {
        when (authState.currentScreen) {
            AppScreen.PATIENT_LOGIN, AppScreen.LAB_LOGIN -> viewModel.navigateTo(AppScreen.ROLE_SELECTION)
            AppScreen.PATIENT_REGISTER -> viewModel.navigateTo(AppScreen.PATIENT_LOGIN)
            AppScreen.LAB_REGISTER -> viewModel.navigateTo(AppScreen.LAB_LOGIN)
            AppScreen.BOOK_TEST -> viewModel.navigateTo(AppScreen.PATIENT_DASHBOARD)
            AppScreen.TRACK_STATUS -> viewModel.navigateTo(AppScreen.PATIENT_DASHBOARD)
            AppScreen.REPORT_DETAIL -> viewModel.navigateTo(
                if (authState.currentUser?.role == "LAB_PROVIDER") AppScreen.LAB_DASHBOARD else AppScreen.PATIENT_DASHBOARD
            )
            AppScreen.ENTER_RESULTS -> viewModel.navigateTo(AppScreen.LAB_DASHBOARD)
            AppScreen.PATIENT_DASHBOARD, AppScreen.LAB_DASHBOARD -> viewModel.logout()
            else -> viewModel.navigateTo(AppScreen.ROLE_SELECTION)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when (authState.currentScreen) {
            AppScreen.ROLE_SELECTION -> {
                RoleSelectionScreen(
                    onSelectPatient = { viewModel.selectRole("PATIENT") },
                    onSelectLabProvider = { viewModel.selectRole("LAB_PROVIDER") },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.PATIENT_LOGIN, AppScreen.LAB_LOGIN -> {
                LoginScreen(
                    role = authState.selectedRole,
                    mobileNumber = authState.mobileNumber,
                    password = authState.password,
                    onMobileChange = { viewModel.onMobileChanged(it) },
                    onPasswordChange = { viewModel.onPasswordChanged(it) },
                    onLoginClick = { viewModel.loginWithPassword() },
                    onBackToRoleSelection = { viewModel.navigateTo(AppScreen.ROLE_SELECTION) },
                    onNavigateToPatientRegister = { viewModel.navigateTo(AppScreen.PATIENT_REGISTER) },
                    onNavigateToLabRegister = { viewModel.navigateTo(AppScreen.LAB_REGISTER) },
                    isLoading = authState.isLoading,
                    errorMessage = authState.errorMessage,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.PATIENT_REGISTER -> {
                PatientRegistrationScreen(
                    initialMobileNumber = authState.mobileNumber,
                    onCreateProfile = { mobile, name, pass, dob, addr -> viewModel.createPatientProfile(mobile, name, pass, dob, addr) },
                    onBackClick = { viewModel.navigateTo(AppScreen.PATIENT_LOGIN) },
                    isLoading = authState.isLoading,
                    errorMessage = authState.errorMessage,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.LAB_REGISTER -> {
                LabRegistrationScreen(
                    initialMobileNumber = authState.mobileNumber,
                    onSubmitApplication = { mobile, name, pass, lic, addr ->
                        viewModel.registerLabProvider(mobile, name, pass, lic, addr)
                    },
                    onBackClick = { viewModel.navigateTo(AppScreen.LAB_LOGIN) },
                    isLoading = authState.isLoading,
                    errorMessage = authState.errorMessage,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.PATIENT_DASHBOARD -> {
                PatientDashboardScreen(
                    user = authState.currentUser,
                    activeBookings = patientBookings,
                    recentReports = patientReports,
                    availableTests = tests,
                    onBookTestClick = { test -> viewModel.startBooking(test) },
                    onTrackBookingClick = { booking -> viewModel.viewBookingTracking(booking) },
                    onViewReportClick = { report -> viewModel.viewReportDetail(report) },
                    onLogoutClick = { viewModel.logout() },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.BOOK_TEST -> {
                BookTestScreen(
                    labs = labs,
                    tests = tests,
                    selectedLab = bookingState.selectedLab,
                    selectedTests = bookingState.selectedTests,
                    selectedDate = bookingState.selectedDate,
                    selectedTimeSlot = bookingState.selectedTimeSlot,
                    patientName = authState.currentUser?.fullName ?: "Yash Kagra",
                    patientAddress = authState.currentUser?.address ?: "Sector 14, Gurugram",
                    onSelectLab = { viewModel.selectLab(it) },
                    onToggleTest = { viewModel.toggleTestSelection(it) },
                    onSelectDate = { viewModel.setBookingDate(it) },
                    onSelectTimeSlot = { viewModel.setBookingTimeSlot(it) },
                    onConfirmBooking = { viewModel.confirmBooking() },
                    onBackClick = { viewModel.navigateTo(AppScreen.PATIENT_DASHBOARD) },
                    isLoading = authState.isLoading,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.TRACK_STATUS -> {
                val booking = bookingState.activeTrackingBooking ?: patientBookings.firstOrNull() ?: allBookings.firstOrNull()
                TrackStatusScreen(
                    booking = booking,
                    onBackClick = { viewModel.navigateTo(AppScreen.PATIENT_DASHBOARD) },
                    onViewReportClick = {
                        val report = patientReports.firstOrNull { it.bookingId == booking?.bookingId }
                            ?: patientReports.firstOrNull()
                        if (report != null) {
                            viewModel.viewReportDetail(report)
                        } else {
                            viewModel.navigateTo(AppScreen.PATIENT_DASHBOARD)
                        }
                    },
                    hasReportReady = booking?.status == "REPORT_READY",
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.REPORT_DETAIL -> {
                ReportDetailScreen(
                    report = bookingState.activeReportDetail ?: patientReports.firstOrNull() ?: allReports.firstOrNull(),
                    onBackClick = {
                        viewModel.navigateTo(
                            if (authState.currentUser?.role == "LAB_PROVIDER") AppScreen.LAB_DASHBOARD else AppScreen.PATIENT_DASHBOARD
                        )
                    },
                    onShareWhatsApp = { report ->
                        viewModel.shareExistingReportOnWhatsApp(report)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.LAB_DASHBOARD -> {
                LabDashboardScreen(
                    user = authState.currentUser,
                    bookings = labBookings,
                    onAssignPhlebotomist = { id, name, phone ->
                        viewModel.assignTechnician(id, name, phone)
                    },
                    onAdvanceStatus = { id, status ->
                        viewModel.updateBookingStatus(id, status)
                    },
                    onEnterResultsClick = { booking ->
                        viewModel.openEnterResultsForBooking(booking)
                    },
                    onLogoutClick = { viewModel.logout() },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AppScreen.ENTER_RESULTS -> {
                EnterResultsScreen(
                    booking = bookingState.activeLabProcessingBooking ?: allBookings.firstOrNull(),
                    onBackClick = { viewModel.navigateTo(AppScreen.LAB_DASHBOARD) },
                    onGenerateReport = { booking, pathologist, status, params, notes ->
                        viewModel.generateReportAndNotify(booking, pathologist, status, params, notes)
                    },
                    isLoading = authState.isLoading,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }

    // Role Security Mismatch & Accreditation Alert Dialog
    authState.securityAlert?.let { alert ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissSecurityAlert() },
            title = {
                Text(
                    text = alert.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Text(text = alert.message, style = MaterialTheme.typography.bodyMedium)
            },
            confirmButton = {
                Button(
                    onClick = {
                        alert.onConfirmAction?.invoke()
                        viewModel.dismissSecurityAlert()
                    }
                ) {
                    Text(alert.suggestedActionLabel)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSecurityAlert() }) {
                    Text("Dismiss")
                }
            }
        )
    }

    // WhatsApp Dispatch Simulation Dialog
    authState.whatsAppNotification?.let { notification ->
        WhatsAppSimulationDialog(
            data = notification,
            onDismiss = { viewModel.dismissWhatsAppDialog() }
        )
    }
}
