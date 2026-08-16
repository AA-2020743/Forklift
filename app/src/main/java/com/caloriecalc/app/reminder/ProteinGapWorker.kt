package com.caloriecalc.app.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.caloriecalc.app.CalorieCalcApp
import com.caloriecalc.app.domain.SleepWindow
import java.time.LocalTime
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.first

/**
 * Watches the gap since the last meaningful dose of protein and nudges when it grows past the
 * user's configured limit.
 *
 * The reasoning is the muscle-protein-synthesis one: a serving of protein raises synthesis for a
 * few hours and then it settles back toward baseline, so several spaced doses across the day
 * beat the same total eaten in one or two sittings. A gap of roughly 3-5 hours between servings
 * is the usual practical recommendation, which is what [proteinGapHours] defaults to.
 *
 * Two things keep this from becoming a nuisance:
 *  - Only entries of at least [proteinDoseGrams] reset the clock, so a coffee or a piece of fruit
 *    doesn't count as "fed" and, conversely, doesn't trigger a nudge on its own.
 *  - Nothing fires outside the waking window. When the check lands mid-sleep the worker doesn't
 *    just skip it — it re-arms itself for the moment the user is due to wake, so the first
 *    morning check happens on time instead of waiting for the next hourly tick.
 */
class ProteinGapWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as CalorieCalcApp).appContainer
        val profile = container.profileRepository.getProfile()
        if (!profile.proteinReminderEnabled) return Result.success()

        val now = LocalTime.now()
        if (!SleepWindow.isAwake(profile, now)) {
            // Asleep: hold the nudge and resume right at wake-up rather than dropping it.
            val minutes = SleepWindow.minutesUntilAwake(profile, now).coerceAtLeast(1)
            container.reminderScheduler.scheduleProteinGapCheck(minutes)
            return Result.success()
        }

        val lastEntry = container.nutritionLogRepository.lastProteinEntry(profile.proteinDoseGrams)
        val gapMillis = lastEntry?.let { System.currentTimeMillis() - it.loggedAtEpochMillis }
        val gapHours = gapMillis?.let { it / 3_600_000.0 }

        // With no qualifying entry ever logged there's no gap to measure — stay quiet rather
        // than nagging a brand-new user who hasn't started logging yet.
        if (gapHours != null && gapHours >= profile.proteinGapHours) {
            NotificationHelper.showProteinGapReminder(
                context = applicationContext,
                hoursSinceLastProtein = gapHours.roundToInt(),
                nextMealName = nearestMealName(container, now)
            )
        }

        container.reminderScheduler.scheduleProteinGapCheck(ReminderScheduler.PROTEIN_CHECK_INTERVAL_MINUTES)
        return Result.success()
    }

    /** The meal slot whose target time is closest to now, to make the nudge concrete. */
    private suspend fun nearestMealName(
        container: com.caloriecalc.app.di.AppContainer,
        now: LocalTime
    ): String? {
        val nowMinutes = now.hour * 60 + now.minute
        return container.mealSlotRepository.observeActive().first()
            .filter { it.remindersEnabled && it.targetMinuteOfDay != null }
            .minByOrNull { slot ->
                val diff = kotlin.math.abs(slot.targetMinuteOfDay!! - nowMinutes)
                minOf(diff, 24 * 60 - diff)
            }
            ?.takeIf { slot ->
                val diff = kotlin.math.abs(slot.targetMinuteOfDay!! - nowMinutes)
                minOf(diff, 24 * 60 - diff) <= 90
            }
            ?.name
    }
}
