package com.caloriecalc.app.domain

import kotlin.math.roundToInt

/**
 * Hydration contributed by food and drink, not just what you pour into a glass.
 *
 * Roughly 20-30% of daily fluid intake comes from food in a normal diet, and most drinks are
 * ~90-99% water by weight, so counting only "plain water" systematically understates intake.
 * The counter-worry — that coffee and tea don't count because caffeine is a diuretic — doesn't
 * hold at normal intakes: the fluid in a caffeinated drink more than offsets its mild diuretic
 * effect. Alcohol is the real exception, being a genuine net negative at typical serving sizes.
 *
 * Two numbers per item, kept separate on purpose:
 *  - [waterContentPercent]: how much of the food is water by weight (a physical property).
 *  - hydration factor: how much of that water actually counts toward hydration.
 *
 * Multiplying them gives the effective contribution. Everything sits at 1.0 except alcohol,
 * which is discounted rather than ignored so a beer doesn't read as a glass of water.
 */
object HydrationCalculator {

    /**
     * Best-guess water content by weight for a food, matched on name keywords, or null when
     * there's no reasonable guess. Only ever a pre-filled default — always user-editable, since
     * a guess from a name is exactly the kind of thing that should be easy to override.
     */
    fun guessWaterContentPercent(name: String): Double? {
        val n = name.lowercase()
        return when {
            n.containsAny("water", "sparkling", "mineral water") -> 100.0
            n.containsAny("coffee", "espresso", "americano", "tea", "herbal") -> 99.0
            n.containsAny("beer", "lager", "pils") -> 93.0
            n.containsAny("wine", "prosecco", "champagne") -> 86.0
            n.containsAny("broth", "bouillon", "soup") -> 92.0
            n.containsAny("juice", "smoothie", "lemonade", "squash") -> 88.0
            n.containsAny("milk", "kefir", "buttermilk", "melk") -> 89.0
            n.containsAny("soda", "cola", "fanta", "sprite", "energy drink", "iced tea") -> 90.0
            n.containsAny("yoghurt", "yogurt", "kwark", "quark", "skyr") -> 82.0
            n.containsAny("protein shake", "shake") -> 85.0
            n.containsAny("watermelon", "cucumber", "lettuce", "celery", "tomato") -> 95.0
            n.containsAny("orange", "melon", "strawberr", "grapefruit", "peach") -> 89.0
            n.containsAny("apple", "pear", "grape", "pineapple", "blueberr") -> 84.0
            n.containsAny("broccoli", "spinach", "carrot", "pepper", "courgette", "zucchini") -> 90.0
            n.containsAny("potato", "rice", "pasta", "noodle") -> 70.0
            else -> null
        }
    }

    /**
     * How much of an item's water counts toward hydration. Alcohol suppresses vasopressin and
     * drives net fluid loss at typical serving strengths, so it's discounted; everything else
     * counts in full.
     */
    fun hydrationFactor(name: String): Double {
        val n = name.lowercase()
        return when {
            n.containsAny("beer", "lager", "pils", "wine", "prosecco", "champagne", "vodka", "whisky", "rum", "gin") -> 0.5
            else -> 1.0
        }
    }

    /** Effective hydration in mL from [grams] of a food that is [waterContentPercent] water. */
    fun hydrationMl(grams: Double, waterContentPercent: Double?, name: String): Double {
        val pct = waterContentPercent ?: return 0.0
        return grams * (pct / 100.0) * hydrationFactor(name)
    }

    fun hydrationMlRounded(grams: Double, waterContentPercent: Double?, name: String): Int =
        hydrationMl(grams, waterContentPercent, name).roundToInt()

    private fun String.containsAny(vararg needles: String): Boolean = needles.any { contains(it) }
}
