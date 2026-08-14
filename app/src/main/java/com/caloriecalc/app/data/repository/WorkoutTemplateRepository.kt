package com.caloriecalc.app.data.repository

import com.caloriecalc.app.data.local.dao.WorkoutTemplateDao
import com.caloriecalc.app.data.local.entity.Exercise
import com.caloriecalc.app.data.local.entity.WorkoutTemplate
import com.caloriecalc.app.data.local.entity.WorkoutTemplateExercise
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class TemplateExerciseWithDetails(val templateExercise: WorkoutTemplateExercise, val exercise: Exercise)

class WorkoutTemplateRepository(
    private val dao: WorkoutTemplateDao,
    private val workoutRepository: WorkoutRepository
) {
    fun observeTemplates(): Flow<List<WorkoutTemplate>> = dao.observeTemplates()

    suspend fun getTemplate(id: Long): WorkoutTemplate? = dao.getTemplate(id)

    suspend fun createTemplate(name: String): Long = dao.insertTemplate(WorkoutTemplate(name = name))

    suspend fun deleteTemplate(template: WorkoutTemplate) = dao.deleteTemplate(template)

    fun observeTemplateExercises(templateId: Long): Flow<List<TemplateExerciseWithDetails>> =
        combine(dao.observeTemplateExercises(templateId), workoutRepository.observeExercises()) { entries, exercises ->
            val byId = exercises.associateBy { it.id }
            entries.mapNotNull { entry -> byId[entry.exerciseId]?.let { TemplateExerciseWithDetails(entry, it) } }
        }

    suspend fun addExercise(templateId: Long, exerciseId: Long) {
        val nextOrder = dao.getMaxOrderIndex(templateId) + 1
        dao.insertTemplateExercise(WorkoutTemplateExercise(templateId = templateId, exerciseId = exerciseId, orderIndex = nextOrder))
    }

    suspend fun removeExercise(templateExerciseId: Long) = dao.deleteTemplateExercise(templateExerciseId)
}
