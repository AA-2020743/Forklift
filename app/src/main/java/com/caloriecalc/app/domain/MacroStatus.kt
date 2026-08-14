package com.caloriecalc.app.domain

/** Visual/semantic status of a macro relative to its healthy range, used to color-code progress UI. */
enum class MacroStatus {
    BELOW_RANGE,
    IN_RANGE,
    ABOVE_RANGE
}

data class MacroProgress(
    val consumedGrams: Double,
    val minGrams: Double,
    val maxGrams: Double,
    val status: MacroStatus
) {
    /** 0..1+ position of [consumedGrams] against [maxGrams], for progress-bar rendering. */
    val fractionOfMax: Float
        get() = if (maxGrams <= 0) 0f else (consumedGrams / maxGrams).toFloat().coerceIn(0f, 2f)
}

object MacroEvaluator {
    fun evaluate(consumedGrams: Double, range: MacroRange): MacroProgress {
        val status = when {
            consumedGrams < range.minGrams -> MacroStatus.BELOW_RANGE
            consumedGrams <= range.maxGrams -> MacroStatus.IN_RANGE
            else -> MacroStatus.ABOVE_RANGE
        }
        return MacroProgress(consumedGrams, range.minGrams, range.maxGrams, status)
    }
}
