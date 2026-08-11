package com.caloriecalc.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.caloriecalc.app.data.local.entity.WeightLog
import kotlinx.coroutines.flow.Flow

@Dao
interface WeightLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(weightLog: WeightLog)

    @Query("SELECT * FROM weight_logs ORDER BY epochDay ASC")
    fun observeAll(): Flow<List<WeightLog>>

    @Query("SELECT * FROM weight_logs WHERE epochDay BETWEEN :fromEpochDay AND :toEpochDay ORDER BY epochDay ASC")
    fun observeInRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<WeightLog>>

    @Query("SELECT * FROM weight_logs WHERE epochDay = :epochDay LIMIT 1")
    suspend fun getForDay(epochDay: Long): WeightLog?

    @Query("SELECT * FROM weight_logs ORDER BY epochDay DESC LIMIT 1")
    fun observeLatest(): Flow<WeightLog?>
}
