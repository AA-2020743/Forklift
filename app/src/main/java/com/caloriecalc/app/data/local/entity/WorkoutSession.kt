package com.caloriecalc.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions", indices = [Index("epochDay")])
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val epochDay: Long,
    val startedAtEpochMillis: Long = System.currentTimeMillis(),
    val name: String? = null,
    val notes: String? = null
)
