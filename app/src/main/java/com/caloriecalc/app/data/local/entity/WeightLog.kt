package com.caloriecalc.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "weight_logs", indices = [Index("epochDay", unique = true)])
data class WeightLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val epochDay: Long,
    val weightKg: Double,
    val loggedAtEpochMillis: Long = System.currentTimeMillis()
)
