package com.caloriecalc.app.domain

import com.caloriecalc.app.data.local.entity.WeightLog
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

data class WeightTrendResult(
    val weeklyChangeKg: Double?,
    val averageDailyCalories: Double?,
    val estimatedMaintenanceCalories: Int?,
    val suggestion: String,
    /** A concrete kcal/day delta to apply to the calorie target, if the trend suggests one. */
    val suggestedCalorieAdjustment: Int? = null
)

private data class Suggestion(val text: String, val adjustmentKcal: Int? = null)

/**
 * Correlates the observed weight trend against logged calorie intake to sanity-check
 * calorie targets and body-weight goals, e.g. "you're cutting but not losing weight,
 * your real maintenance looks higher than estimated — eat less or check your logging".
 */
object WeightTrendAnalyzer {

    /** Roughly 7700 kcal per kg of body fat. */
    private const val KCAL_PER_KG = 7700.0

    /** The representative kcal/day nudge offered by the "apply" action (matches the "150-250" text ranges). */
    private const val STANDARD_ADJUSTMENT = 200

    fun analyze(
        weightLogsInWindow: List<WeightLog>,
        dailyCaloriesByEpochDay: Map<Long, Double>,
        calorieTarget: Int,
        goal: Goal,
        currentBodyWeightKg: Double
    ): WeightTrendResult {
        if (weightLogsInWindow.size < 2) {
            return WeightTrendResult(
                weeklyChangeKg = null,
                averageDailyCalories = null,
                estimatedMaintenanceCalories = null,
                suggestion = "Log your weight a few more times this week to unlock trend insights."
            )
        }

        val sorted = weightLogsInWindow.sortedBy { it.epochDay }
        val first = sorted.first()
        val last = sorted.last()
        val daySpan = (last.epochDay - first.epochDay).coerceAtLeast(1)
        val weeklyChangeKg = (last.weightKg - first.weightKg) / daySpan * 7.0

        val relevantCalories = (first.epochDay..last.epochDay).mapNotNull { dailyCaloriesByEpochDay[it] }
        val averageDailyCalories = if (relevantCalories.isNotEmpty()) relevantCalories.average() else null

        val estimatedMaintenance = averageDailyCalories?.let { avg ->
            (avg - (weeklyChangeKg * KCAL_PER_KG / 7.0)).roundToInt()
        }

        val pctPerWeek = weeklyChangeKg / currentBodyWeightKg * 100.0

        val suggestion = buildSuggestion(goal, weeklyChangeKg, pctPerWeek, estimatedMaintenance, calorieTarget)

        return WeightTrendResult(
            weeklyChangeKg,
            averageDailyCalories,
            estimatedMaintenance,
            suggestion.text,
            suggestion.adjustmentKcal
        )
    }

    private fun buildSuggestion(
        goal: Goal,
        weeklyChangeKg: Double,
        pctPerWeek: Double,
        estimatedMaintenance: Int?,
        calorieTarget: Int
    ): Suggestion {
        val change = format(weeklyChangeKg)
        return when (goal) {
            Goal.LOSE -> when {
                weeklyChangeKg > -0.1 -> maintenanceHint(
                    "Your weight is roughly flat ($change kg/week) even near your calorie target.",
                    estimatedMaintenance, calorieTarget, lower = true
                )
                pctPerWeek < -1.0 -> Suggestion(
                    "You're losing faster than the recommended 0.5-1% of body weight per week " +
                        "($change kg/week). Consider adding 150-250 kcal/day to protect muscle mass.",
                    STANDARD_ADJUSTMENT
                )
                else -> Suggestion("Weight trend ($change kg/week) is in a healthy range for fat loss. Keep it up.")
            }
            Goal.GAIN -> when {
                weeklyChangeKg < 0.05 -> maintenanceHint(
                    "Weight isn't moving up much on your current intake ($change kg/week).",
                    estimatedMaintenance, calorieTarget, lower = false
                )
                pctPerWeek > 0.5 -> Suggestion(
                    "You're gaining faster than the ~0.25-0.5% of body weight per week guideline for " +
                        "lean gains ($change kg/week) — likely adding more fat than needed. Consider trimming 150-250 kcal/day.",
                    -STANDARD_ADJUSTMENT
                )
                else -> Suggestion("Weight trend ($change kg/week) is in a good range for a lean bulk. Keep it up.")
            }
            Goal.MAINTAIN -> when {
                abs(pctPerWeek) > 0.3 -> {
                    val direction = if (weeklyChangeKg > 0) "up" else "down"
                    Suggestion(
                        "Weight is drifting $direction ($change kg/week) while aiming to maintain. Consider adjusting " +
                            "intake by ${if (weeklyChangeKg > 0) "-150 to -250" else "+150 to +250"} kcal/day.",
                        if (weeklyChangeKg > 0) -STANDARD_ADJUSTMENT else STANDARD_ADJUSTMENT
                    )
                }
                else -> Suggestion("Weight is stable ($change kg/week) — nice consistency.")
            }
            Goal.RECOMPOSITION -> when {
                pctPerWeek < -0.75 -> Suggestion(
                    "You're losing weight faster than ideal for a recomposition ($change kg/week), risking muscle " +
                        "along with fat. Consider adding 150-250 kcal/day.",
                    STANDARD_ADJUSTMENT
                )
                weeklyChangeKg > 0.15 -> Suggestion(
                    "Weight is trending up ($change kg/week), more than a recomposition calls for. Consider " +
                        "trimming 150-250 kcal/day.",
                    -STANDARD_ADJUSTMENT
                )
                else -> Suggestion("Weight trend ($change kg/week) fits a recomposition — roughly flat to a slow loss. Keep it up.")
            }
            Goal.RECOMPOSITION_LEAN_BULK -> when {
                weeklyChangeKg < 0.05 -> maintenanceHint(
                    "Weight isn't moving up much on your current intake ($change kg/week).",
                    estimatedMaintenance, calorieTarget, lower = false
                )
                pctPerWeek > 0.35 -> Suggestion(
                    "You're gaining faster than a lean-tilted recomposition calls for ($change kg/week). Consider " +
                        "trimming 150-250 kcal/day.",
                    -STANDARD_ADJUSTMENT
                )
                else -> Suggestion("Weight trend ($change kg/week) fits a lean bulking tilt. Keep it up.")
            }
        }
    }

    private fun maintenanceHint(prefix: String, estimatedMaintenance: Int?, calorieTarget: Int, lower: Boolean): Suggestion {
        if (estimatedMaintenance == null) {
            return Suggestion("$prefix Log a few more days of food to estimate your real maintenance calories.")
        }
        val diff = estimatedMaintenance - calorieTarget
        val direction = if (lower) "lower your intake" else "raise your intake"
        val text = "$prefix Based on your logs, your real maintenance looks closer to ~$estimatedMaintenance kcal/day " +
            "(about ${if (diff >= 0) "+" else ""}$diff vs. your $calorieTarget kcal target) — consider adjusting to " +
            "$direction by 150-250 kcal/day."
        return Suggestion(text, if (lower) -STANDARD_ADJUSTMENT else STANDARD_ADJUSTMENT)
    }

    private fun format(value: Double): String = String.format(Locale.US, "%.2f", value)
}
