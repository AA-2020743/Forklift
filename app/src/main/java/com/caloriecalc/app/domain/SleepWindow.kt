package com.caloriecalc.app.domain

import com.caloriecalc.app.data.local.entity.UserProfile
import java.time.LocalTime

/**
 * Whether the user is within their waking hours, so reminders never fire mid-sleep.
 *
 * Handles the normal case (wake 07:00, sleep 23:00 — awake is the span between them) and the
 * wrap-around case (wake 07:00, sleep 01:00 — awake spans midnight), which is the one that
 * silently breaks if you just compare two numbers.
 */
object SleepWindow {

    fun isAwake(profile: UserProfile, now: LocalTime = LocalTime.now()): Boolean {
        val wake = profile.wakeHour * 60 + profile.wakeMinute
        val sleep = profile.sleepHour * 60 + profile.sleepMinute
        val current = now.hour * 60 + now.minute

        // Degenerate config (wake == sleep): treat as always awake rather than never.
        if (wake == sleep) return true
        return if (wake < sleep) {
            current in wake until sleep
        } else {
            // Sleep time is past midnight, so the waking window wraps around the day boundary.
            current >= wake || current < sleep
        }
    }

    /**
     * Minutes from [now] until the waking window next opens, or 0 if already awake. Used to
     * park a reminder at the start of the next waking window instead of dropping it.
     */
    fun minutesUntilAwake(profile: UserProfile, now: LocalTime = LocalTime.now()): Long {
        if (isAwake(profile, now)) return 0L
        val wake = profile.wakeHour * 60 + profile.wakeMinute
        val current = now.hour * 60 + now.minute
        val delta = if (wake >= current) wake - current else (24 * 60) - current + wake
        return delta.toLong()
    }
}
