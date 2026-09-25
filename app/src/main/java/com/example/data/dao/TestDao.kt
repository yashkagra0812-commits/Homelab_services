package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.TestItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TestDao {
    @Query("SELECT * FROM tests ORDER BY category ASC, priceRupees ASC")
    fun getAllTests(): Flow<List<TestItemEntity>>

    @Query("SELECT * FROM tests WHERE id = :id LIMIT 1")
    suspend fun getTestById(id: Long): TestItemEntity?

    @Query("SELECT * FROM tests WHERE id IN (:ids)")
    suspend fun getTestsByIds(ids: List<Long>): List<TestItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTests(tests: List<TestItemEntity>)
}
