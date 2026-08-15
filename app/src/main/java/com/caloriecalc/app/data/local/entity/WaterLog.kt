package com.caloriecalc.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Running total of water logged for a day — one row per day, updated in place as more is added. */
@Entity(tableName = "water_logs", indices = [Index("epochDay", unique = true)])
data class WaterLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val epochDay: Long,
    val amountMl: Int,
    val updatedAtEpochMillis: Long = System.currentTimeMillis()
)
