package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.BookingDao
import com.example.data.dao.LabDao
import com.example.data.dao.ReportDao
import com.example.data.dao.TestDao
import com.example.data.dao.UserDao
import com.example.data.model.BookingEntity
import com.example.data.model.LabEntity
import com.example.data.model.ReportEntity
import com.example.data.model.TestItemEntity
import com.example.data.model.UserEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        LabEntity::class,
        TestItemEntity::class,
        BookingEntity::class,
        ReportEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun labDao(): LabDao
    abstract fun testDao(): TestDao
    abstract fun bookingDao(): BookingDao
    abstract fun reportDao(): ReportDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "homelab_care_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            INSTANCE?.let { database ->
                                seedDatabase(database)
                            }
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedDatabase(database: AppDatabase) {
            // Seed Labs (So patients can select labs and providers can register/exist)
            val labs = listOf(
                LabEntity(
                    id = 1,
                    name = "Metropolis Central Diagnostics",
                    licenseNumber = "NABL-MC-2024-9128",
                    rating = 4.9,
                    reviewCount = 2840,
                    turnaroundHours = 8,
                    contactMobile = "9876500000",
                    address = "Connaught Place & Cyber City Hubs",
                    badges = "NABL Certified, CAP Accredited"
                ),
                LabEntity(
                    id = 2,
                    name = "Apex Care Laboratories",
                    licenseNumber = "NABL-AC-2023-4011",
                    rating = 4.8,
                    reviewCount = 1420,
                    turnaroundHours = 12,
                    contactMobile = "9876500001",
                    address = "Institutional Area, Sector 32, Gurugram",
                    badges = "NABL Accredited, 100% Sterile Kit"
                ),
                LabEntity(
                    id = 3,
                    name = "Thyrocare Metro Diagnostic Hub",
                    licenseNumber = "CAP-TC-8812",
                    rating = 4.7,
                    reviewCount = 3910,
                    turnaroundHours = 14,
                    contactMobile = "9876500002",
                    address = "Okhla Industrial Area Phase II, New Delhi",
                    badges = "Automated Barcoding, ISO 15189"
                )
            )
            database.labDao().insertLabs(labs)

            // Seed Test Catalog (Essential for booking tests)
            val tests = listOf(
                TestItemEntity(
                    id = 1,
                    name = "Complete Blood Count (CBC) with ESR",
                    shortCode = "CBC",
                    category = "Blood Tests",
                    priceRupees = 350,
                    originalPriceRupees = 550,
                    fastingRequirement = "No fasting required",
                    turnaroundTime = "6 Hours",
                    sampleType = "EDTA Whole Blood (2 ml)",
                    description = "Evaluates overall health and detects wide range of disorders including anemia, infection and leukemia.",
                    parametersCount = 24
                ),
                TestItemEntity(
                    id = 2,
                    name = "Lipid Profile (Cholesterol & Triglycerides)",
                    shortCode = "LIPID",
                    category = "Lipid & Heart",
                    priceRupees = 599,
                    originalPriceRupees = 899,
                    fastingRequirement = "10-12 hrs fasting required",
                    turnaroundTime = "8 Hours",
                    sampleType = "Serum (3 ml)",
                    description = "Measures cholesterol, HDL, LDL, VLDL and triglycerides to assess cardiovascular risk.",
                    parametersCount = 8
                ),
                TestItemEntity(
                    id = 3,
                    name = "Thyroid Profile (T3, T4, TSH Ultra)",
                    shortCode = "THYROID",
                    category = "Thyroid",
                    priceRupees = 499,
                    originalPriceRupees = 750,
                    fastingRequirement = "Morning sample preferred",
                    turnaroundTime = "8 Hours",
                    sampleType = "Serum (2 ml)",
                    description = "Screening and diagnostic evaluation for hypo- and hyper-thyroidism and metabolic health.",
                    parametersCount = 3
                ),
                TestItemEntity(
                    id = 4,
                    name = "HbA1c (Glycosylated Hemoglobin)",
                    shortCode = "HBA1C",
                    category = "Diabetes",
                    priceRupees = 420,
                    originalPriceRupees = 600,
                    fastingRequirement = "No fasting required",
                    turnaroundTime = "6 Hours",
                    sampleType = "EDTA Blood",
                    description = "Reflects average blood sugar levels over the past 3 months for diabetes monitoring.",
                    parametersCount = 2
                ),
                TestItemEntity(
                    id = 5,
                    name = "Fasting Blood Glucose (Sugar)",
                    shortCode = "FBS",
                    category = "Diabetes",
                    priceRupees = 150,
                    originalPriceRupees = 250,
                    fastingRequirement = "8-10 hrs strict fasting",
                    turnaroundTime = "4 Hours",
                    sampleType = "Fluoride Plasma",
                    description = "Primary baseline screening test for prediabetes and diabetes mellitus.",
                    parametersCount = 1
                ),
                TestItemEntity(
                    id = 6,
                    name = "Vitamin D (25-OH) & Vitamin B12 Duo",
                    shortCode = "VIT-D-B12",
                    category = "Vitamins",
                    priceRupees = 999,
                    originalPriceRupees = 1600,
                    fastingRequirement = "No fasting required",
                    turnaroundTime = "12 Hours",
                    sampleType = "Serum (4 ml)",
                    description = "Essential vitamin levels for bone density, immune health, energy, and neurological function.",
                    parametersCount = 2
                ),
                TestItemEntity(
                    id = 7,
                    name = "Liver Function Test (LFT Comprehensive)",
                    shortCode = "LFT",
                    category = "Organ Health",
                    priceRupees = 650,
                    originalPriceRupees = 950,
                    fastingRequirement = "10 hrs fasting required",
                    turnaroundTime = "8 Hours",
                    sampleType = "Serum (3 ml)",
                    description = "Evaluates liver enzymes (SGOT, SGPT, Bilirubin, Alkaline Phosphatase, Protein).",
                    parametersCount = 11
                ),
                TestItemEntity(
                    id = 8,
                    name = "Kidney Function Test (KFT / RFT)",
                    shortCode = "KFT",
                    category = "Organ Health",
                    priceRupees = 550,
                    originalPriceRupees = 800,
                    fastingRequirement = "No fasting required",
                    turnaroundTime = "8 Hours",
                    sampleType = "Serum (3 ml)",
                    description = "Measures serum creatinine, urea, BUN, uric acid, and electrolytes for renal evaluation.",
                    parametersCount = 7
                )
            )
            database.testDao().insertTests(tests)
        }
    }
}
