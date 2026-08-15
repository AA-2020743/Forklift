package com.caloriecalc.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.caloriecalc.app.data.local.entity.ActivityLog
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {

    @Insert
    suspend fun insert(activity: ActivityLog): Long

    @Delete
    suspend fun delete(activity: ActivityLog)

    @Query("SELECT * FROM activity_logs WHERE epochDay = :epochDay ORDER BY loggedAtEpochMillis DESC")
    fun observeForDay(epochDay: Long): Flow<List<ActivityLog>>
}
