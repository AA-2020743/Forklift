package com.caloriecalc.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.caloriecalc.app.data.local.entity.MealEntry
import com.caloriecalc.app.domain.MealType
import kotlinx.coroutines.flow.Flow

data class DayMacroTotals(
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)

@Dao
interface MealEntryDao {

    @Insert
    suspend fun insert(mealEntry: MealEntry): Long

    @Delete
    suspend fun delete(mealEntry: MealEntry)

    @Query("SELECT * FROM meal_entries WHERE epochDay = :epochDay ORDER BY loggedAtEpochMillis ASC")
    fun getEntriesForDay(epochDay: Long): Flow<List<MealEntry>>

    @Query("SELECT * FROM meal_entries WHERE epochDay = :epochDay AND mealType = :mealType ORDER BY loggedAtEpochMillis ASC")
    fun getEntriesForMeal(epochDay: Long, mealType: MealType): Flow<List<MealEntry>>

    @Query(
        """
        SELECT COALESCE(SUM(calories), 0) AS calories,
               COALESCE(SUM(protein), 0) AS protein,
               COALESCE(SUM(fat), 0) AS fat,
               COALESCE(SUM(carbs), 0) AS carbs
        FROM meal_entries WHERE epochDay = :epochDay
        """
    )
    fun getTotalsForDay(epochDay: Long): Flow<DayMacroTotals>

    @Query(
        """
        SELECT epochDay, COALESCE(SUM(calories), 0) AS calories,
               COALESCE(SUM(protein), 0) AS protein,
               COALESCE(SUM(fat), 0) AS fat,
               COALESCE(SUM(carbs), 0) AS carbs
        FROM meal_entries WHERE epochDay BETWEEN :fromEpochDay AND :toEpochDay
        GROUP BY epochDay
        """
    )
    fun getTotalsInRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<DayMacroTotalsWithDay>>
}

data class DayMacroTotalsWithDay(
    val epochDay: Long,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double
)
