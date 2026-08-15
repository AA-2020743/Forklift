package com.caloriecalc.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.caloriecalc.app.domain.ActivityType

/** A quick-logged cardio/misc activity for a given day (walking, boxing, cycling, etc.). */
@Entity(tableName = "activity_logs", indices = [Index("epochDay")])
data class ActivityLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val epochDay: Long,
    val type: ActivityType,
    val durationMinutes: Int,
    /** Only meaningful for step-tracked types like Walking/Running. */
    val steps: Int? = null,
    val caloriesBurned: Int,
    val loggedAtEpochMillis: Long = System.currentTimeMillis()
)
