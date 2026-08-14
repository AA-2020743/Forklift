package com.caloriecalc.app.data.repository

import com.caloriecalc.app.data.local.dao.ExerciseDao
import com.caloriecalc.app.data.local.dao.SetEntryDao
import com.caloriecalc.app.data.local.dao.SetWithSessionDate
import com.caloriecalc.app.data.local.dao.WorkoutSessionDao
import com.caloriecalc.app.data.local.entity.Exercise
import com.caloriecalc.app.data.local.entity.SetEntry
import com.caloriecalc.app.data.local.entity.WorkoutSession
import com.caloriecalc.app.data.seed.ExerciseSeedData
import kotlinx.coroutines.flow.Flow

class WorkoutRepository(
    private val exerciseDao: ExerciseDao,
    private val sessionDao: WorkoutSessionDao,
    private val setEntryDao: SetEntryDao
) {
    suspend fun ensureSeeded() {
        if (exerciseDao.count() == 0) {
            exerciseDao.insertAll(ExerciseSeedData.exercises)
        }
    }

    fun observeExercises(): Flow<List<Exercise>> = exerciseDao.observeAll()

    suspend fun getExerciseById(id: Long): Exercise? = exerciseDao.getById(id)

    suspend fun addCustomExercise(exercise: Exercise): Long = exerciseDao.insert(exercise.copy(isCustom = true))

    fun observeSessions(): Flow<List<WorkoutSession>> = sessionDao.observeAll()

    fun observeSessionsInRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<WorkoutSession>> =
        sessionDao.observeInRange(fromEpochDay, toEpochDay)

    suspend fun getSession(id: Long): WorkoutSession? = sessionDao.getById(id)

    suspend fun startSession(epochDay: Long, name: String? = null, templateId: Long? = null): Long =
        sessionDao.insert(WorkoutSession(epochDay = epochDay, name = name, templateId = templateId))

    suspend fun deleteSession(session: WorkoutSession) = sessionDao.delete(session)

    fun observeSetsForSession(sessionId: Long): Flow<List<SetEntry>> = setEntryDao.observeForSession(sessionId)

    suspend fun addSet(set: SetEntry): Long = setEntryDao.insert(set)

    suspend fun updateSet(set: SetEntry) = setEntryDao.update(set)

    suspend fun deleteSet(set: SetEntry) = setEntryDao.delete(set)

    fun observeSetsInRange(fromEpochDay: Long, toEpochDay: Long): Flow<List<SetEntry>> =
        setEntryDao.observeSetsInRange(fromEpochDay, toEpochDay)

    fun observeExerciseHistory(exerciseId: Long): Flow<List<SetWithSessionDate>> =
        setEntryDao.observeHistoryForExercise(exerciseId)
}
