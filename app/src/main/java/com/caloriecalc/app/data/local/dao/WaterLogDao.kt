package com.caloriecalc.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.caloriecalc.app.data.local.entity.WaterLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WaterLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: WaterLog)

    @Query("SELECT * FROM water_logs WHERE epochDay = :epochDay LIMIT 1")
    fun observeForDay(epochDay: Long): Flow<WaterLog?>

    @Query("SELECT * FROM water_logs WHERE epochDay = :epochDay LIMIT 1")
    suspend fun getForDay(epochDay: Long): WaterLog?
}
