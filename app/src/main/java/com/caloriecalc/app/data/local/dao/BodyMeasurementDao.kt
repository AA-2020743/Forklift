package com.caloriecalc.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.caloriecalc.app.data.local.entity.BodyMeasurement
import kotlinx.coroutines.flow.Flow

@Dao
interface BodyMeasurementDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(measurement: BodyMeasurement)

    @Query("SELECT * FROM body_measurements ORDER BY epochDay DESC")
    fun observeAll(): Flow<List<BodyMeasurement>>

    @Query("SELECT * FROM body_measurements WHERE epochDay = :epochDay LIMIT 1")
    suspend fun getForDay(epochDay: Long): BodyMeasurement?

    @Query("DELETE FROM body_measurements WHERE epochDay = :epochDay")
    suspend fun deleteForDay(epochDay: Long)
}
