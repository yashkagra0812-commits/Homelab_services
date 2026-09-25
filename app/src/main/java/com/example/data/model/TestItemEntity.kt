package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tests")
data class TestItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val shortCode: String,
    val category: String, // "Blood Tests", "Diabetes", "Thyroid", "Lipid & Heart", "Vitamins"
    val priceRupees: Int,
    val originalPriceRupees: Int,
    val fastingRequirement: String, // "10-12 hrs fasting", "No fasting required"
    val turnaroundTime: String, // "6 Hours", "12 Hours", "24 Hours"
    val sampleType: String, // "Blood EDTA", "Serum", "Fluoride Plasma"
    val description: String,
    val parametersCount: Int = 4
)
