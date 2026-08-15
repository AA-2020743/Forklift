package com.caloriecalc.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.local.entity.ActivityLog
import com.caloriecalc.app.data.local.entity.MealSlot
import com.caloriecalc.app.data.local.entity.WorkoutSession
import com.caloriecalc.app.data.repository.ActivityRepository
import com.caloriecalc.app.data.repository.MealSlotRepository
import com.caloriecalc.app.data.repository.NutritionLogRepository
import com.caloriecalc.app.data.repository.ProfileRepository
import com.caloriecalc.app.data.repository.WorkoutRepository
import com.caloriecalc.app.domain.ActivityCalculator
import com.caloriecalc.app.domain.ActivityType
import com.caloriecalc.app.domain.MacroEvaluator
import com.caloriecalc.app.domain.MacroProgress
import com.caloriecalc.app.domain.MacroStatus
import com.caloriecalc.app.domain.NutritionCalculator
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MealSummary(val mealSlot: MealSlot, val calories: Int, val proteinGrams: Double, val itemCount: Int)

/** A unified row for the day's "Activity" feed: either a lifting session or a quick-logged
 * cardio/misc activity, so both can render in one list. */
sealed class ActivityRow {
    data class Lifting(val session: WorkoutSession, val durationMinutes: Int?, val caloriesBurned: Int?) : ActivityRow()
    data class Cardio(val activity: ActivityLog) : ActivityRow()
}

private val emptyMacroProgress = MacroProgress(0.0, 0.0, 0.0, MacroStatus.IN_RANGE)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val calorieConsumed: Int = 0,
    val calorieTarget: Int = 0,
    val proteinProgress: MacroProgress = emptyMacroProgress,
    val fatProgress: MacroProgress = emptyMacroProgress,
    val carbProgress: MacroProgress = emptyMacroProgress,
    val mealSummaries: List<MealSummary> = emptyList(),
    val bodyWeightKg: Double = 75.0,
    val activityRows: List<ActivityRow> = emptyList(),
    val totalCaloriesBurned: Int = 0
)

class DashboardViewModel(
    profileRepository: ProfileRepository,
    nutritionLogRepository: NutritionLogRepository,
    mealSlotRepository: MealSlotRepository,
    workoutRepository: WorkoutRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {

    private val today = LocalDate.now().toEpochDay()

    private val activityAndSessions = combine(
        workoutRepository.observeSessionsInRange(today, today),
        activityRepository.observeForDay(today)
    ) { sessions, activities -> sessions to activities }

    val uiState: StateFlow<DashboardUiState> = combine(
        profileRepository.observeProfile(),
        nutritionLogRepository.totalsForDay(today),
        nutritionLogRepository.entriesForDay(today),
        mealSlotRepository.observeActive(),
        activityAndSessions
    ) { profile, totals, entries, mealSlots, sessionsAndActivities ->
        val (sessions, activities) = sessionsAndActivities
        val targets = NutritionCalculator.computeTargets(profile)
        val mealSummaries = mealSlots.map { slot ->
            val mealEntries = entries.filter { it.entry.mealSlotId == slot.id }
            MealSummary(
                mealSlot = slot,
                calories = mealEntries.sumOf { it.entry.calories }.roundToInt(),
                proteinGrams = mealEntries.sumOf { it.entry.protein },
                itemCount = mealEntries.size
            )
        }

        val liftingRows = sessions.map { session ->
            val durationMinutes = session.endedAtEpochMillis?.let { end ->
                ((end - session.startedAtEpochMillis) / 60_000L).toInt().coerceAtLeast(0)
            }
            val burn = durationMinutes?.let {
                ActivityCalculator.estimateCaloriesBurned(ActivityCalculator.LIFTING_MET, it, profile.bodyWeightKg)
            }
            ActivityRow.Lifting(session, durationMinutes, burn)
        }
        val cardioRows = activities.map { ActivityRow.Cardio(it) }
        val activityRows = (liftingRows + cardioRows).sortedByDescending { row ->
            when (row) {
                is ActivityRow.Lifting -> row.session.startedAtEpochMillis
                is ActivityRow.Cardio -> row.activity.loggedAtEpochMillis
            }
        }
        val totalBurned = liftingRows.sumOf { it.caloriesBurned ?: 0 } + cardioRows.sumOf { it.activity.caloriesBurned }

        DashboardUiState(
            isLoading = false,
            calorieConsumed = totals.calories.roundToInt(),
            calorieTarget = targets.calorieTarget,
            proteinProgress = MacroEvaluator.evaluate(totals.protein, targets.protein),
            fatProgress = MacroEvaluator.evaluate(totals.fat, targets.fat),
            carbProgress = MacroEvaluator.evaluate(totals.carbs, targets.carbs),
            mealSummaries = mealSummaries,
            bodyWeightKg = profile.bodyWeightKg,
            activityRows = activityRows,
            totalCaloriesBurned = totalBurned
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun logActivity(type: ActivityType, durationMinutes: Int, steps: Int?, caloriesOverride: Int?) {
        viewModelScope.launch {
            activityRepository.logActivity(
                epochDay = today,
                type = type,
                durationMinutes = durationMinutes,
                bodyWeightKg = uiState.value.bodyWeightKg,
                steps = steps,
                caloriesBurnedOverride = caloriesOverride
            )
        }
    }

    fun deleteActivity(activity: ActivityLog) {
        viewModelScope.launch { activityRepository.deleteActivity(activity) }
    }
}
