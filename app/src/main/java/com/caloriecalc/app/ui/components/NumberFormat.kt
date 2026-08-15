package com.caloriecalc.app.ui.components

/**
 * Formats a gram amount to one decimal place, dropping the decimal when it's a whole number
 * (10.78 -> "10.8", 100.0 -> "100"). A plain `roundToInt()` erases real precision — an entry
 * of 10.78g fat shouldn't display as "11".
 */
fun formatGrams(value: Double): String {
    val rounded = Math.round(value * 10) / 10.0
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
}
