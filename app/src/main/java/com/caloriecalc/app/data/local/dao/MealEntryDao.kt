package com.caloriecalc.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.caloriecalc.app.data.local.entity.MealEntry
import kotlinx.coroutines.flow.Flow

data class DayMacroTotals(
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val fiberGrams: Double,
    val sugarGrams: Double,
    val saturatedFatGrams: Double,
    val sodiumMg: Double,
    val potassiumMg: Double,
    val calciumMg: Double,
    val ironMg: Double,
    val vitaminCMg: Double,
    val vitaminDMcg: Double,
    val vitaminB12Mcg: Double,
    val magnesiumMg: Double,
    val zincMg: Double
)

@Dao
interface MealEntryDao {

    @Insert
    suspend fun insert(mealEntry: MealEntry): Long

    @Delete
    suspend fun delete(mealEntry: MealEntry)

    @Update
    suspend fun update(mealEntry: MealEntry)

    @Query("UPDATE meal_entries SET loggedAtEpochMillis = :consumedAtEpochMillis WHERE epochDay = :epochDay AND mealSlotId = :mealSlotId")
    suspend fun updateTimeForMeal(epochDay: Long, mealSlotId: Long, consumedAtEpochMillis: Long)

    @Query("SELECT * FROM meal_entries WHERE epochDay = :epochDay ORDER BY loggedAtEpochMillis ASC")
    fun getEntriesForDay(epochDay: Long): Flow<List<MealEntry>>

    @Query("SELECT * FROM meal_entries WHERE epochDay = :epochDay AND mealSlotId = :mealSlotId ORDER BY loggedAtEpochMillis ASC")
    fun getEntriesForMeal(epochDay: Long, mealSlotId: Long): Flow<List<MealEntry>>

    @Query(
        """
        SELECT COALESCE(SUM(calories), 0) AS calories,
               COALESCE(SUM(protein), 0) AS protein,
               COALESCE(SUM(fat), 0) AS fat,
               COALESCE(SUM(carbs), 0) AS carbs,
               COALESCE(SUM(fiberGrams), 0) AS fiberGrams,
               COALESCE(SUM(sugarGrams), 0) AS sugarGrams,
               COALESCE(SUM(saturatedFatGrams), 0) AS saturatedFatGrams,
               COALESCE(SUM(sodiumMg), 0) AS sodiumMg,
               COALESCE(SUM(potassiumMg), 0) AS potassiumMg,
               COALESCE(SUM(calciumMg), 0) AS calciumMg,
               COALESCE(SUM(ironMg), 0) AS ironMg,
               COALESCE(SUM(vitaminCMg), 0) AS vitaminCMg,
               COALESCE(SUM(vitaminDMcg), 0) AS vitaminDMcg,
               COALESCE(SUM(vitaminB12Mcg), 0) AS vitaminB12Mcg,
               COALESCE(SUM(magnesiumMg), 0) AS magnesiumMg,
               COALESCE(SUM(zincMg), 0) AS zincMg
        FROM meal_entries WHERE epochDay = :epochDay
        """
    )
    fun getTotalsForDay(epochDay: Long): Flow<DayMacroTotals>

    /** Total hydration contributed by food and drink logged on a day, in mL. */
    @Query("SELECT COALESCE(SUM(hydrationMl), 0) FROM meal_entries WHERE epochDay = :epochDay")
    fun getHydrationForDay(epochDay: Long): Flow<Double>

    @Query(
        """
        SELECT epochDay, COALESCE(SUM(hydrationMl), 0) AS hydrationMl
        FROM meal_entries WHERE epochDay BETWEEN :fromEpochDay AND :toEpochDay
        GROUP BY epochDay
        """
    )
    fun getHydrationInRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<DayHydration>>

    @Query("SELECT * FROM meal_entries WHERE foodItemId = :foodItemId")
    suspend fun getEntriesForFood(foodItemId: Long): List<MealEntry>

    /** The most recent meal whose combined foods delivered at least [minProteinGrams]. */
    @Query(
        """
        SELECT MAX(loggedAtEpochMillis) FROM meal_entries
        WHERE loggedAtEpochMillis <= :notAfterEpochMillis
        GROUP BY epochDay, mealSlotId
        HAVING SUM(protein) >= :minProteinGrams
        ORDER BY MAX(loggedAtEpochMillis) DESC LIMIT 1
        """
    )
    suspend fun getLastProteinMealTime(minProteinGrams: Double, notAfterEpochMillis: Long): Long?

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

data class DayHydration(val epochDay: Long, val hydrationMl: Double)
