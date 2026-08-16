package com.caloriecalc.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.local.dao.DayMacroTotals
import com.caloriecalc.app.data.local.entity.ActivityLog
import com.caloriecalc.app.data.local.entity.MealSlot
import com.caloriecalc.app.data.local.entity.WaterLog
import com.caloriecalc.app.data.local.entity.WorkoutSession
import com.caloriecalc.app.data.repository.ActivityRepository
import com.caloriecalc.app.data.repository.MealEntryWithFood
import com.caloriecalc.app.data.repository.MealSlotRepository
import com.caloriecalc.app.data.repository.NutritionLogRepository
import com.caloriecalc.app.data.repository.ProfileRepository
import com.caloriecalc.app.data.repository.WaterRepository
import com.caloriecalc.app.data.repository.WorkoutRepository
import com.caloriecalc.app.domain.ActivityCalculator
import com.caloriecalc.app.domain.ActivityType
import com.caloriecalc.app.domain.MacroEvaluator
import com.caloriecalc.app.domain.MacroProgress
import com.caloriecalc.app.domain.MacroStatus
import com.caloriecalc.app.domain.NutritionCalculator
import com.caloriecalc.app.domain.WaterCalculator
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
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
    val selectedEpochDay: Long = LocalDate.now().toEpochDay(),
    val isToday: Boolean = true,
    val calorieConsumed: Int = 0,
    val calorieTarget: Int = 0,
    val proteinProgress: MacroProgress = emptyMacroProgress,
    val fatProgress: MacroProgress = emptyMacroProgress,
    val carbProgress: MacroProgress = emptyMacroProgress,
    val mealSummaries: List<MealSummary> = emptyList(),
    /** Per-food contributions for the day, so a macro row can explain what made it up. */
    val proteinContributions: List<MacroContribution> = emptyList(),
    val fatContributions: List<MacroContribution> = emptyList(),
    val carbContributions: List<MacroContribution> = emptyList(),
    val bodyWeightKg: Double = 75.0,
    val activityRows: List<ActivityRow> = emptyList(),
    val totalCaloriesBurned: Int = 0,
    val waterMl: Int = 0,
    /** Hydration from logged food and drink, counted separately from poured water. */
    val hydrationFromFoodMl: Int = 0,
    val waterTargetMl: Int = 2000
) {
    val totalHydrationMl: Int get() = waterMl + hydrationFromFoodMl
}

private data class DayScopedBundle(
    val sessions: List<WorkoutSession>,
    val activities: List<ActivityLog>,
    val water: WaterLog?,
    val totals: DayMacroTotals,
    val entries: List<MealEntryWithFood>,
    val hydrationFromFoodMl: Double
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    profileRepository: ProfileRepository,
    private val nutritionLogRepository: NutritionLogRepository,
    mealSlotRepository: MealSlotRepository,
    private val workoutRepository: WorkoutRepository,
    private val activityRepository: ActivityRepository,
    private val waterRepository: WaterRepository
) : ViewModel() {

    private val today = LocalDate.now().toEpochDay()
    private val selectedEpochDay = MutableStateFlow(today)

    private val dayScoped = selectedEpochDay.flatMapLatest { day ->
        // Water and food-derived hydration are paired first: `combine` tops out at five typed
        // sources, and these two are the pair that always get read together anyway.
        val hydration = combine(
            waterRepository.observeForDay(day),
            nutritionLogRepository.hydrationForDay(day)
        ) { water, fromFood -> water to fromFood }

        combine(
            workoutRepository.observeSessionsInRange(day, day),
            activityRepository.observeForDay(day),
            hydration,
            nutritionLogRepository.totalsForDay(day),
            nutritionLogRepository.entriesForDay(day)
        ) { sessions, activities, (water, fromFood), totals, entries ->
            DayScopedBundle(sessions, activities, water, totals, entries, fromFood)
        }
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        profileRepository.observeProfile(),
        mealSlotRepository.observeActive(),
        dayScoped,
        selectedEpochDay
    ) { profile, mealSlots, bundle, day ->
        val (sessions, activities, water, totals, entries, hydrationFromFood) = bundle
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

        val mealNameById = mealSlots.associate { it.id to it.name }
        fun contributions(macroOf: (com.caloriecalc.app.data.local.entity.MealEntry) -> Double) =
            entries.filter { macroOf(it.entry) > 0.0 }
                .map { item ->
                    MacroContribution(
                        foodName = item.food.name,
                        mealName = mealNameById[item.entry.mealSlotId] ?: "Meal",
                        grams = item.entry.grams,
                        macroGrams = macroOf(item.entry)
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
            selectedEpochDay = day,
            isToday = day == today,
            calorieConsumed = totals.calories.roundToInt(),
            calorieTarget = targets.calorieTarget,
            proteinProgress = MacroEvaluator.evaluate(totals.protein, targets.protein),
            fatProgress = MacroEvaluator.evaluate(totals.fat, targets.fat),
            carbProgress = MacroEvaluator.evaluate(totals.carbs, targets.carbs),
            mealSummaries = mealSummaries,
            proteinContributions = contributions { it.protein },
            fatContributions = contributions { it.fat },
            carbContributions = contributions { it.carbs },
            bodyWeightKg = profile.bodyWeightKg,
            activityRows = activityRows,
            totalCaloriesBurned = totalBurned,
            waterMl = water?.amountMl ?: 0,
            hydrationFromFoodMl = hydrationFromFood.roundToInt(),
            waterTargetMl = WaterCalculator.recommendedMl(profile)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun logActivity(type: ActivityType, durationMinutes: Int, steps: Int?, caloriesOverride: Int?) {
        if (selectedEpochDay.value != today) return
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
        if (selectedEpochDay.value != today) return
        viewModelScope.launch { activityRepository.deleteActivity(activity) }
    }

    /** Applies to whichever day is being viewed, so past days can be corrected like meals can. */
    fun addWater(deltaMl: Int) {
        val day = selectedEpochDay.value
        viewModelScope.launch { waterRepository.addWater(day, deltaMl) }
    }

    fun selectDay(epochDay: Long) {
        selectedEpochDay.value = epochDay.coerceAtMost(today)
    }

    fun goToPreviousDay() {
        selectedEpochDay.value -= 1
    }

    fun goToNextDay() {
        selectedEpochDay.value = (selectedEpochDay.value + 1).coerceAtMost(today)
    }
}
