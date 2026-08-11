package com.caloriecalc.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.caloriecalc.app.data.local.entity.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(userProfile: UserProfile)

    // id is always UserProfile.SINGLETON_ID (1); a literal is used here since Room's
    // @Query annotation value must be a compile-time constant string.
    @Query("SELECT * FROM user_profile WHERE id = 1")
    fun observe(): Flow<UserProfile?>

    // id is always UserProfile.SINGLETON_ID (1); a literal is used here since Room's
    // @Query annotation value must be a compile-time constant string.
    @Query("SELECT * FROM user_profile WHERE id = 1")
    suspend fun get(): UserProfile?
}
