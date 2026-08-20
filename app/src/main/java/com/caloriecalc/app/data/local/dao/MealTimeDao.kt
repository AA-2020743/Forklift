package com.caloriecalc.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.caloriecalc.app.data.local.entity.MealTime
import kotlinx.coroutines.flow.Flow

@Dao
interface MealTimeDao {

    @Query("SELECT * FROM meal_times WHERE epochDay = :epochDay AND mealSlotId = :mealSlotId")
    suspend fun get(epochDay: Long, mealSlotId: Long): MealTime?

    @Query("SELECT * FROM meal_times WHERE epochDay = :epochDay AND mealSlotId = :mealSlotId")
    fun observe(epochDay: Long, mealSlotId: Long): Flow<MealTime?>

    @Query("SELECT * FROM meal_times WHERE epochDay = :epochDay")
    fun observeForDay(epochDay: Long): Flow<List<MealTime>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(mealTime: MealTime)
}
