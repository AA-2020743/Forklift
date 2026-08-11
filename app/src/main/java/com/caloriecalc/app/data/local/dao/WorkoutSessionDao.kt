package com.caloriecalc.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.caloriecalc.app.data.local.entity.WorkoutSession
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutSessionDao {

    @Insert
    suspend fun insert(session: WorkoutSession): Long

    @Delete
    suspend fun delete(session: WorkoutSession)

    @Query("SELECT * FROM workout_sessions ORDER BY epochDay DESC, startedAtEpochMillis DESC")
    fun observeAll(): Flow<List<WorkoutSession>>

    @Query("SELECT * FROM workout_sessions WHERE id = :id")
    suspend fun getById(id: Long): WorkoutSession?

    @Query("SELECT * FROM workout_sessions WHERE epochDay BETWEEN :fromEpochDay AND :toEpochDay ORDER BY epochDay ASC")
    fun observeInRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<WorkoutSession>>
}
