package com.caloriecalc.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.caloriecalc.app.data.local.entity.WorkoutTemplate
import com.caloriecalc.app.data.local.entity.WorkoutTemplateExercise
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutTemplateDao {

    @Insert
    suspend fun insertTemplate(template: WorkoutTemplate): Long

    @Delete
    suspend fun deleteTemplate(template: WorkoutTemplate)

    @Query("SELECT * FROM workout_templates ORDER BY name ASC")
    fun observeTemplates(): Flow<List<WorkoutTemplate>>

    @Query("SELECT * FROM workout_templates WHERE id = :id")
    suspend fun getTemplate(id: Long): WorkoutTemplate?

    @Insert
    suspend fun insertTemplateExercise(templateExercise: WorkoutTemplateExercise): Long

    @Query("DELETE FROM workout_template_exercises WHERE id = :id")
    suspend fun deleteTemplateExercise(id: Long)

    @Query("SELECT * FROM workout_template_exercises WHERE templateId = :templateId ORDER BY orderIndex ASC")
    fun observeTemplateExercises(templateId: Long): Flow<List<WorkoutTemplateExercise>>

    @Query("SELECT COALESCE(MAX(orderIndex), -1) FROM workout_template_exercises WHERE templateId = :templateId")
    suspend fun getMaxOrderIndex(templateId: Long): Int

    @Query("SELECT COUNT(*) FROM workout_template_exercises WHERE templateId = :templateId")
    suspend fun countExercises(templateId: Long): Int
}
