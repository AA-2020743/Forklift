package com.caloriecalc.app.domain

/** Visual/semantic status of a macro relative to its target, used to color-code progress UI. */
enum class MacroStatus {
    BELOW_THRESHOLD,
    APPROACHING,
    ON_TARGET,
    OVER_TARGET
}

data class MacroProgress(
    val consumedGrams: Double,
    val targetGrams: Double,
    val minGrams: Double? = null,
    val maxGrams: Double? = null,
    val status: MacroStatus
) {
    val fractionOfTarget: Float
        get() = if (targetGrams <= 0) 0f else (consumedGrams / targetGrams).toFloat().coerceIn(0f, 2f)
}

object MacroEvaluator {

    /** Protein has no hard ceiling — only a floor to watch for. */
    fun evaluateProtein(consumedGrams: Double, targetGrams: Double): MacroProgress {
        val status = when {
            targetGrams <= 0 -> MacroStatus.ON_TARGET
            consumedGrams < targetGrams * 0.75 -> MacroStatus.BELOW_THRESHOLD
            consumedGrams < targetGrams -> MacroStatus.APPROACHING
            else -> MacroStatus.ON_TARGET
        }
        return MacroProgress(consumedGrams, targetGrams, status = status)
    }

    /** Fat has a safe floor (hormonal health) and a soft ceiling (calorie budget for carbs/protein). */
    fun evaluateFat(consumedGrams: Double, minGrams: Double, targetGrams: Double, maxGrams: Double): MacroProgress {
        val status = when {
            consumedGrams < minGrams -> MacroStatus.BELOW_THRESHOLD
            consumedGrams < targetGrams -> MacroStatus.APPROACHING
            consumedGrams <= maxGrams -> MacroStatus.ON_TARGET
            else -> MacroStatus.OVER_TARGET
        }
        return MacroProgress(consumedGrams, targetGrams, minGrams, maxGrams, status)
    }

    /** Carbs simply fill whatever calorie budget remains; only flag when the budget is blown. */
    fun evaluateCarbs(consumedGrams: Double, targetGrams: Double): MacroProgress {
        val status = if (targetGrams > 0 && consumedGrams > targetGrams) MacroStatus.OVER_TARGET else MacroStatus.ON_TARGET
        return MacroProgress(consumedGrams, targetGrams, status = status)
    }
}
