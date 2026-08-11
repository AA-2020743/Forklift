package com.caloriecalc.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.caloriecalc.app.domain.MuscleGroup
import com.caloriecalc.app.domain.MuscleSubGroup

@Entity(tableName = "exercises")
data class Exercise(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val primaryMuscleGroup: MuscleGroup,
    /** The specific sub-groups (e.g. biceps long head) this exercise trains. */
    val targetSubGroups: Set<MuscleSubGroup>,
    val equipment: String? = null,
    val isCustom: Boolean = false
)
