package com.caloriecalc.app.ui.trends

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.caloriecalc.app.data.repository.NutritionLogRepository
import com.caloriecalc.app.data.repository.ProfileRepository
import com.caloriecalc.app.data.repository.WaterRepository
import com.caloriecalc.app.domain.NutritionCalculator
import com.caloriecalc.app.domain.WaterCalculator
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

enum class TrendWindow(val days: Int, val label: String) {
    WEEK(7, "7 days"),
    FORTNIGHT(14, "14 days"),
    MONTH(30, "30 days")
}

/** One day's roll-up. Days with nothing logged are still present, with zeroes, so the
 * averages honestly reflect "per day over the window" rather than "per day I bothered to log". */
data class TrendDay(
    val epochDay: Long,
    val calories: Double,
    val protein: Double,
    val fat: Double,
    val carbs: Double,
    val waterMl: Int
) {
    val hasFoodLogged: Boolean get() = calories > 0.0
}

data class TrendsUiState(
    val window: TrendWindow = TrendWindow.WEEK,
    val days: List<TrendDay> = emptyList(),
    val avgCalories: Int = 0,
    val avgProtein: Double = 0.0,
    val avgFat: Double = 0.0,
    val avgCarbs: Double = 0.0,
    val avgWaterMl: Int = 0,
    val daysLogged: Int = 0,
    val calorieTarget: Int = 0,
    val proteinTargetMin: Double = 0.0,
    val waterTargetMl: Int = 0
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class TrendsViewModel(
    profileRepository: ProfileRepository,
    nutritionLogRepository: NutritionLogRepository,
    waterRepository: WaterRepository
) : ViewModel() {

    private val _window = MutableStateFlow(TrendWindow.WEEK)

    private val rangeData = _window.flatMapLatest { window ->
        val today = LocalDate.now().toEpochDay()
        val from = today - (window.days - 1)
        combine(
            nutritionLogRepository.totalsInRange(from, today),
            waterRepository.observeInRange(from, today)
        ) { totals, waterLogs ->
            val totalsByDay = totals.associateBy { it.epochDay }
            val waterByDay = waterLogs.associateBy { it.epochDay }
            (from..today).map { day ->
                val t = totalsByDay[day]
                TrendDay(
                    epochDay = day,
                    calories = t?.calories ?: 0.0,
                    protein = t?.protein ?: 0.0,
                    fat = t?.fat ?: 0.0,
                    carbs = t?.carbs ?: 0.0,
                    waterMl = waterByDay[day]?.amountMl ?: 0
                )
            }
        }
    }

    val uiState: StateFlow<TrendsUiState> = combine(
        profileRepository.observeProfile(),
        rangeData,
        _window
    ) { profile, days, window ->
        val targets = NutritionCalculator.computeTargets(profile)
        val n = days.size.coerceAtLeast(1)
        TrendsUiState(
            window = window,
            days = days,
            avgCalories = (days.sumOf { it.calories } / n).toInt(),
            avgProtein = days.sumOf { it.protein } / n,
            avgFat = days.sumOf { it.fat } / n,
            avgCarbs = days.sumOf { it.carbs } / n,
            avgWaterMl = (days.sumOf { it.waterMl }.toDouble() / n).toInt(),
            daysLogged = days.count { it.hasFoodLogged },
            calorieTarget = targets.calorieTarget,
            proteinTargetMin = targets.protein.minGrams,
            waterTargetMl = WaterCalculator.recommendedMl(profile)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TrendsUiState())

    fun selectWindow(window: TrendWindow) {
        _window.value = window
    }
}
