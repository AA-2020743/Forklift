package com.caloriecalc.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "workout_sessions",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplate::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("epochDay"), Index("templateId")]
)
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val epochDay: Long,
    val startedAtEpochMillis: Long = System.currentTimeMillis(),
    val name: String? = null,
    val notes: String? = null,
    /** The template this session was started from, if any — drives the "planned" list in-session. */
    val templateId: Long? = null
)
