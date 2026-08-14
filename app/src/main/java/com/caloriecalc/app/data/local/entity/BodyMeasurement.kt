package com.caloriecalc.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "body_measurements", indices = [Index("epochDay", unique = true)])
data class BodyMeasurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val epochDay: Long,
    val waistCm: Double? = null,
    val chestCm: Double? = null,
    val armsCm: Double? = null,
    val thighsCm: Double? = null,
    val hipsCm: Double? = null,
    val neckCm: Double? = null,
    val loggedAtEpochMillis: Long = System.currentTimeMillis()
)
