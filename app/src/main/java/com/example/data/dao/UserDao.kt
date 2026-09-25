package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE mobile = :mobile LIMIT 1")
    suspend fun getUserByMobile(mobile: String): UserEntity?

    @Query("SELECT * FROM users WHERE mobile = :mobile LIMIT 1")
    fun observeUserByMobile(mobile: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE role = 'LAB_PROVIDER'")
    fun getAllLabProviders(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isApproved = :approved WHERE id = :userId")
    suspend fun updateApproval(userId: Long, approved: Boolean)
}
