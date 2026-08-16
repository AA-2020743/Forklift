package com.caloriecalc.app.domain

import com.caloriecalc.app.data.local.entity.WeightLog
import kotlin.math.abs

/** One day of the smoothed series. [rawKg] is null on days with no weigh-in (the trend still advances). */
data class SmoothedWeightPoint(
    val epochDay: Long,
    val rawKg: Double?,
    val trendKg: Double
)

data class WeightTrendSeries(
    val points: List<SmoothedWeightPoint>,
    /** Least-squares slope of the trend line, kg per day. Null with fewer than two weigh-ins. */
    val slopeKgPerDay: Double?,
    /** Typical distance between a scale reading and the trend — the day-to-day "noise band". */
    val dailyNoiseKg: Double?,
    val latestTrendKg: Double?
) {
    val weeklyChangeKg: Double? get() = slopeKgPerDay?.times(7.0)
}

/**
 * Turns noisy daily scale readings into a usable trend.
 *
 * Day-to-day body weight swings by up to 1-2 kg on water alone — sodium, carb intake (each gram
 * of stored glycogen binds ~3 g of water), hormones, and simply what's still in your gut. Judging
 * progress from two raw readings is therefore mostly measuring hydration, not fat.
 *
 * The standard fix, popularised by The Hacker's Diet and used by trend-weight apps since, is an
 * exponentially weighted moving average: each day's trend moves only a fraction [ALPHA] of the
 * way toward that day's reading, so a single salty dinner barely registers while a real change
 * pulls the line along within a week or so. Two refinements on top of the classic version:
 *
 *  - The series is walked day by day, not reading by reading, so a gap between weigh-ins doesn't
 *    let one later reading yank the trend as hard as several consecutive ones would.
 *  - Rate of change comes from a least-squares fit across the whole smoothed series rather than
 *    subtracting the first reading from the last, so no single day can define your "weekly change".
 */
object WeightSmoother {

    /**
     * Fraction of the gap to the new reading absorbed per day (~6.6-day half-life). Low enough
     * to ignore water swings, responsive enough to catch a real trend inside a couple of weeks.
     */
    private const val ALPHA = 0.10

    fun smooth(logs: List<WeightLog>): WeightTrendSeries {
        if (logs.isEmpty()) return WeightTrendSeries(emptyList(), null, null, null)

        val byDay = logs.groupBy { it.epochDay }
            .mapValues { (_, sameDay) -> sameDay.map { it.weightKg }.average() }
        val firstDay = byDay.keys.min()
        val lastDay = byDay.keys.max()

        var trend = byDay.getValue(firstDay)
        val points = (firstDay..lastDay).map { day ->
            val raw = byDay[day]
            if (raw != null) trend += ALPHA * (raw - trend)
            SmoothedWeightPoint(epochDay = day, rawKg = raw, trendKg = trend)
        }

        val slope = if (byDay.size >= 2) leastSquaresSlope(points) else null
        val noise = if (byDay.size >= 2) {
            points.mapNotNull { p -> p.rawKg?.let { abs(it - p.trendKg) } }
                .takeIf { it.isNotEmpty() }
                ?.average()
        } else {
            null
        }

        return WeightTrendSeries(
            points = points,
            slopeKgPerDay = slope,
            dailyNoiseKg = noise,
            latestTrendKg = points.lastOrNull()?.trendKg
        )
    }

    /** Ordinary least-squares slope of trendKg against epochDay, in kg/day. */
    private fun leastSquaresSlope(points: List<SmoothedWeightPoint>): Double? {
        if (points.size < 2) return null
        val meanX = points.map { it.epochDay.toDouble() }.average()
        val meanY = points.map { it.trendKg }.average()
        var numerator = 0.0
        var denominator = 0.0
        points.forEach { p ->
            val dx = p.epochDay.toDouble() - meanX
            numerator += dx * (p.trendKg - meanY)
            denominator += dx * dx
        }
        return if (denominator == 0.0) null else numerator / denominator
    }
}
