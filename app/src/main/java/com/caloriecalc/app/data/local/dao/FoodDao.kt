package com.caloriecalc.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.caloriecalc.app.data.local.entity.FoodItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodItem: FoodItem): Long

    @Update
    suspend fun update(foodItem: FoodItem)

    @Query("SELECT * FROM food_items WHERE id = :id")
    suspend fun getById(id: Long): FoodItem?

    @Query("SELECT * FROM food_items WHERE id = :id")
    fun observeById(id: Long): Flow<FoodItem?>

    @Query("SELECT * FROM food_items")
    suspend fun getAll(): List<FoodItem>

    @Query("SELECT * FROM food_items WHERE barcode = :barcode AND isArchived = 0 LIMIT 1")
    suspend fun getByBarcode(barcode: String): FoodItem?

    @Query("SELECT * FROM food_items WHERE isArchived = 0 AND name LIKE '%' || :query || '%' ORDER BY useCount DESC LIMIT 50")
    fun search(query: String): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE isArchived = 0 AND lastUsedAtEpochMillis IS NOT NULL ORDER BY lastUsedAtEpochMillis DESC LIMIT 30")
    fun getRecentlyUsed(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE isArchived = 0 AND useCount > 0 ORDER BY useCount DESC LIMIT 30")
    fun getFrequentlyUsed(): Flow<List<FoodItem>>

    @Query("SELECT * FROM food_items WHERE isArchived = 0 AND isFavorite = 1 ORDER BY name ASC")
    fun getFavorites(): Flow<List<FoodItem>>

    @Query("UPDATE food_items SET useCount = useCount + 1, lastUsedAtEpochMillis = :timestamp WHERE id = :id")
    suspend fun markUsed(id: Long, timestamp: Long)

    @Query("UPDATE food_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE food_items SET isArchived = 1, isFavorite = 0 WHERE id = :id")
    suspend fun archive(id: Long)
}
