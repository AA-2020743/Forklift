package com.caloriecalc.app.reminder

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.caloriecalc.app.CalorieCalcApp
import com.caloriecalc.app.domain.SleepWindow
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
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
 *  - Only meals whose combined entries reach [proteinDoseGrams] reset the clock, so a coffee or
 *    piece of fruit doesn't count as "fed", while several foods in one meal count together.
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
        if (!profile.proteinReminderEnabled) {
            NotificationHelper.clearProteinGapReminder(applicationContext)
            return Result.success()
        }

        val now = LocalTime.now()
        if (!SleepWindow.isAwake(profile, now)) {
            // Asleep: hold the nudge and resume right at wake-up rather than dropping it.
            val minutes = SleepWindow.minutesUntilAwake(profile, now).coerceAtLeast(1)
            container.reminderScheduler.scheduleProteinGapCheck(minutes)
            return Result.success()
        }

        val nowMillis = System.currentTimeMillis()
        val lastMealMillis = container.nutritionLogRepository.lastProteinMealTime(
            profile.proteinDoseGrams,
            nowMillis
        )
        val timing = calculateProteinGapTiming(nowMillis, lastMealMillis, profile.proteinGapHours)

        // With no qualifying entry ever logged there's no gap to measure — stay quiet rather
        // than nagging a brand-new user who hasn't started logging yet.
        if (timing.isDue) {
            NotificationHelper.showProteinGapReminder(
                context = applicationContext,
                minutesSinceLastProtein = timing.elapsedMinutes ?: 0,
                nextMealName = nearestMealName(container, now)
            )
        } else {
            // A qualifying meal resets the gap. Remove any older notification instead of leaving
            // a stale nudge visible until the user happens to tap it.
            NotificationHelper.clearProteinGapReminder(applicationContext)
        }

        container.reminderScheduler.scheduleProteinGapCheck(timing.nextDelayMinutes)
        return Result.success()
    }

    /** The meal whose current-day time (or typical time) is closest to now, to make the nudge concrete. */
    private suspend fun nearestMealName(
        container: com.caloriecalc.app.di.AppContainer,
        now: LocalTime
    ): String? {
        val nowMinutes = now.hour * 60 + now.minute
        val today = LocalDate.now().toEpochDay()
        val candidates = container.mealSlotRepository.observeActive().first()
            .filter { it.remindersEnabled }
            .mapNotNull { slot ->
                val mealTime = container.nutritionLogRepository.getMealTime(today, slot.id)
                    ?.let { millis ->
                        Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalTime()
                            .let { time -> time.hour * 60 + time.minute }
                    }
                    ?: slot.targetMinuteOfDay
                mealTime?.let { slot to it }
            }
        return candidates
            .minByOrNull { (_, mealMinutes) ->
                val diff = kotlin.math.abs(mealMinutes - nowMinutes)
                minOf(diff, 24 * 60 - diff)
            }
            ?.takeIf { (_, mealMinutes) ->
                val diff = kotlin.math.abs(mealMinutes - nowMinutes)
                minOf(diff, 24 * 60 - diff) <= 90
            }
            ?.first
            ?.name
    }
}
