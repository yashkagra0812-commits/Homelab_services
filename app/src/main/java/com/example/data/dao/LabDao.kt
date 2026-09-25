package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.LabEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LabDao {
    @Query("SELECT * FROM labs ORDER BY rating DESC")
    fun getAllLabs(): Flow<List<LabEntity>>

    @Query("SELECT * FROM labs WHERE id = :id LIMIT 1")
    suspend fun getLabById(id: Long): LabEntity?

    @Query("SELECT * FROM labs WHERE contactMobile = :mobile LIMIT 1")
    suspend fun getLabByMobile(mobile: String): LabEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLabs(labs: List<LabEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLab(lab: LabEntity): Long
}
