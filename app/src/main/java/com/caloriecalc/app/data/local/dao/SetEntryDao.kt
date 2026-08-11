package com.caloriecalc.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.caloriecalc.app.data.local.entity.SetEntry
import kotlinx.coroutines.flow.Flow

data class SetWithSessionDate(
    val id: Long,
    val exerciseId: Long,
    val setNumber: Int,
    val weightKg: Double,
    val reps: Int,
    val rpe: Double?,
    val epochDay: Long
)

@Dao
interface SetEntryDao {

    @Insert
    suspend fun insert(setEntry: SetEntry): Long

    @Update
    suspend fun update(setEntry: SetEntry)

    @Delete
    suspend fun delete(setEntry: SetEntry)

    @Query("SELECT * FROM set_entries WHERE workoutSessionId = :sessionId ORDER BY id ASC")
    fun observeForSession(sessionId: Long): Flow<List<SetEntry>>

    @Query(
        """
        SELECT set_entries.* FROM set_entries
        INNER JOIN workout_sessions ON set_entries.workoutSessionId = workout_sessions.id
        WHERE workout_sessions.epochDay BETWEEN :fromEpochDay AND :toEpochDay
        """
    )
    fun observeSetsInRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<SetEntry>>

    @Query(
        """
        SELECT set_entries.id, set_entries.exerciseId, set_entries.setNumber, set_entries.weightKg,
               set_entries.reps, set_entries.rpe, workout_sessions.epochDay
        FROM set_entries
        INNER JOIN workout_sessions ON set_entries.workoutSessionId = workout_sessions.id
        WHERE set_entries.exerciseId = :exerciseId
        ORDER BY workout_sessions.epochDay ASC, set_entries.id ASC
        """
    )
    fun observeHistoryForExercise(exerciseId: Long): Flow<List<SetWithSessionDate>>
}
