package com.caloriecalc.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.caloriecalc.app.data.local.entity.MealSlot
import kotlinx.coroutines.flow.Flow

@Dao
interface MealSlotDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(slots: List<MealSlot>)

    @Insert
    suspend fun insert(slot: MealSlot): Long

    @Query("SELECT * FROM meal_slots WHERE isArchived = 0 ORDER BY sortOrder ASC, id ASC")
    fun observeActive(): Flow<List<MealSlot>>

    @Query("SELECT * FROM meal_slots ORDER BY sortOrder ASC, id ASC")
    fun observeAll(): Flow<List<MealSlot>>

    @Query("SELECT * FROM meal_slots WHERE id = :id")
    suspend fun getById(id: Long): MealSlot?

    @Query("SELECT COUNT(*) FROM meal_slots")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM meal_slots WHERE isArchived = 0")
    suspend fun countActive(): Int

    @Query("SELECT COALESCE(MAX(sortOrder), -1) FROM meal_slots")
    suspend fun getMaxSortOrder(): Int

    @Query("UPDATE meal_slots SET isArchived = 1 WHERE id = :id")
    suspend fun archive(id: Long)

    @Query("UPDATE meal_slots SET isArchived = 0 WHERE id = :id")
    suspend fun unarchive(id: Long)
}
