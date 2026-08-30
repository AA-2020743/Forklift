package com.caloriecalc.app.domain

import com.caloriecalc.app.data.local.entity.Micronutrients

/**
 * Best-guess micronutrient profiles per 100g for common whole foods, matched on name keywords.
 *
 * Why this exists: micronutrients only ever arrived from Open Food Facts, and that database is
 * strong on packaged goods but sparse on vitamins, while anything typed in by hand had no way
 * to carry micronutrient data at all. The practical result was a Micronutrients screen reading
 * 0.0 across the board for anyone logging ordinary food — technically correct ("nothing was
 * recorded") but useless, and easy to mistake for a broken feature.
 *
 * These are rounded reference values for the plain, unprepared food, in the spirit of a food
 * composition table rather than a lab result. They exist to pre-fill the form so the screen
 * reflects reality without manual data entry for every egg; every value stays editable, and
 * anything not matched here is simply left unknown rather than guessed at.
 *
 * Units follow [Micronutrients]: grams for fiber/sugar/saturated fat, mg for minerals and
 * vitamin C, mcg for vitamin D and B12.
 */
object MicronutrientEstimator {

    private data class Profile(val keywords: List<String>, val micros: Micronutrients)

    private val profiles = listOf(
        Profile(
            listOf("egg"),
            Micronutrients(
                sugarGrams = 0.4, saturatedFatGrams = 3.1, sodiumMg = 124.0, potassiumMg = 126.0,
                calciumMg = 50.0, ironMg = 1.2, vitaminDMcg = 2.0, vitaminB12Mcg = 1.1,
                magnesiumMg = 10.0, zincMg = 1.3
            )
        ),
        Profile(
            listOf("chicken", "turkey", "kip"),
            Micronutrients(
                sugarGrams = 0.0, saturatedFatGrams = 1.0, sodiumMg = 70.0, potassiumMg = 256.0,
                calciumMg = 12.0, ironMg = 0.9, vitaminB12Mcg = 0.3, magnesiumMg = 27.0, zincMg = 1.0
            )
        ),
        Profile(
            listOf("beef", "steak", "mince", "lamb"),
            Micronutrients(
                sugarGrams = 0.0, saturatedFatGrams = 5.0, sodiumMg = 66.0, potassiumMg = 318.0,
                calciumMg = 18.0, ironMg = 2.6, vitaminB12Mcg = 2.6, magnesiumMg = 21.0, zincMg = 4.8
            )
        ),
        Profile(
            listOf("salmon", "mackerel", "sardine", "herring"),
            Micronutrients(
                sugarGrams = 0.0, saturatedFatGrams = 3.1, sodiumMg = 59.0, potassiumMg = 363.0,
                calciumMg = 12.0, ironMg = 0.8, vitaminDMcg = 11.0, vitaminB12Mcg = 3.2,
                magnesiumMg = 29.0, zincMg = 0.6
            )
        ),
        Profile(
            listOf("tuna", "cod", "tilapia", "pollock"),
            Micronutrients(
                sugarGrams = 0.0, saturatedFatGrams = 0.3, sodiumMg = 50.0, potassiumMg = 350.0,
                calciumMg = 16.0, ironMg = 0.8, vitaminDMcg = 1.5, vitaminB12Mcg = 2.2,
                magnesiumMg = 33.0, zincMg = 0.5
            )
        ),
        Profile(
            listOf("milk", "melk", "kefir", "buttermilk"),
            Micronutrients(
                sugarGrams = 4.8, saturatedFatGrams = 1.0, sodiumMg = 44.0, potassiumMg = 150.0,
                calciumMg = 120.0, ironMg = 0.0, vitaminDMcg = 1.2, vitaminB12Mcg = 0.5,
                magnesiumMg = 11.0, zincMg = 0.4
            )
        ),
        Profile(
            listOf("yoghurt", "yogurt", "kwark", "quark", "skyr"),
            Micronutrients(
                sugarGrams = 4.0, saturatedFatGrams = 0.6, sodiumMg = 50.0, potassiumMg = 141.0,
                calciumMg = 110.0, ironMg = 0.1, vitaminB12Mcg = 0.6, magnesiumMg = 11.0, zincMg = 0.6
            )
        ),
        Profile(
            listOf("cheese", "kaas", "cheddar", "gouda", "mozzarella"),
            Micronutrients(
                sugarGrams = 1.3, saturatedFatGrams = 19.0, sodiumMg = 620.0, potassiumMg = 98.0,
                calciumMg = 720.0, ironMg = 0.7, vitaminB12Mcg = 1.1, magnesiumMg = 28.0, zincMg = 3.1
            )
        ),
        Profile(
            listOf("pasta", "fusilli", "spaghetti", "penne", "noodle"),
            Micronutrients(
                fiberGrams = 3.2, sugarGrams = 2.7, saturatedFatGrams = 0.3, sodiumMg = 6.0,
                potassiumMg = 223.0, calciumMg = 21.0, ironMg = 1.3, magnesiumMg = 53.0, zincMg = 1.4
            )
        ),
        Profile(
            listOf("rice", "rijst"),
            Micronutrients(
                fiberGrams = 1.3, sugarGrams = 0.1, saturatedFatGrams = 0.1, sodiumMg = 5.0,
                potassiumMg = 86.0, calciumMg = 10.0, ironMg = 0.8, magnesiumMg = 25.0, zincMg = 0.8
            )
        ),
        Profile(
            listOf("bread", "brood", "tortilla", "wrap", "bagel", "toast"),
            Micronutrients(
                fiberGrams = 6.0, sugarGrams = 4.0, saturatedFatGrams = 0.7, sodiumMg = 450.0,
                potassiumMg = 230.0, calciumMg = 90.0, ironMg = 2.4, magnesiumMg = 60.0, zincMg = 1.5
            )
        ),
        Profile(
            listOf("oat", "haver", "muesli", "granola"),
            Micronutrients(
                fiberGrams = 10.0, sugarGrams = 1.0, saturatedFatGrams = 1.2, sodiumMg = 2.0,
                potassiumMg = 429.0, calciumMg = 54.0, ironMg = 4.7, magnesiumMg = 177.0, zincMg = 4.0
            )
        ),
        Profile(
            listOf("cashew", "almond", "walnut", "peanut", "pistachio", "hazelnut", "nut"),
            Micronutrients(
                fiberGrams = 3.3, sugarGrams = 5.9, saturatedFatGrams = 7.8, sodiumMg = 12.0,
                potassiumMg = 660.0, calciumMg = 37.0, ironMg = 6.7, magnesiumMg = 292.0, zincMg = 5.8
            )
        ),
        Profile(
            listOf("banana"),
            Micronutrients(
                fiberGrams = 2.6, sugarGrams = 12.2, saturatedFatGrams = 0.1, sodiumMg = 1.0,
                potassiumMg = 358.0, calciumMg = 5.0, ironMg = 0.3, vitaminCMg = 8.7,
                magnesiumMg = 27.0, zincMg = 0.2
            )
        ),
        Profile(
            listOf("apple", "pear"),
            Micronutrients(
                fiberGrams = 2.4, sugarGrams = 10.4, saturatedFatGrams = 0.0, sodiumMg = 1.0,
                potassiumMg = 107.0, calciumMg = 6.0, ironMg = 0.1, vitaminCMg = 4.6,
                magnesiumMg = 5.0, zincMg = 0.0
            )
        ),
        Profile(
            listOf("orange", "mandarin", "clementine", "grapefruit"),
            Micronutrients(
                fiberGrams = 2.4, sugarGrams = 9.4, saturatedFatGrams = 0.0, sodiumMg = 0.0,
                potassiumMg = 181.0, calciumMg = 40.0, ironMg = 0.1, vitaminCMg = 53.0,
                magnesiumMg = 10.0, zincMg = 0.1
            )
        ),
        Profile(
            listOf("berry", "strawberr", "blueberr", "raspberr"),
            Micronutrients(
                fiberGrams = 2.5, sugarGrams = 6.0, saturatedFatGrams = 0.0, sodiumMg = 1.0,
                potassiumMg = 160.0, calciumMg = 16.0, ironMg = 0.4, vitaminCMg = 45.0,
                magnesiumMg = 13.0, zincMg = 0.2
            )
        ),
        Profile(
            listOf("broccoli", "spinach", "kale", "spinazie"),
            Micronutrients(
                fiberGrams = 2.6, sugarGrams = 1.7, saturatedFatGrams = 0.1, sodiumMg = 33.0,
                potassiumMg = 460.0, calciumMg = 78.0, ironMg = 1.5, vitaminCMg = 60.0,
                magnesiumMg = 55.0, zincMg = 0.5
            )
        ),
        Profile(
            listOf("potato", "aardappel"),
            Micronutrients(
                fiberGrams = 2.2, sugarGrams = 0.8, saturatedFatGrams = 0.0, sodiumMg = 6.0,
                potassiumMg = 425.0, calciumMg = 12.0, ironMg = 0.8, vitaminCMg = 20.0,
                magnesiumMg = 23.0, zincMg = 0.3
            )
        ),
        Profile(
            listOf("bean", "lentil", "chickpea", "hummus", "boon", "linze"),
            Micronutrients(
                fiberGrams = 7.9, sugarGrams = 2.0, saturatedFatGrams = 0.2, sodiumMg = 2.0,
                potassiumMg = 369.0, calciumMg = 49.0, ironMg = 3.3, vitaminCMg = 1.5,
                magnesiumMg = 36.0, zincMg = 1.3
            )
        ),
        Profile(
            listOf("honey", "honing"),
            Micronutrients(
                fiberGrams = 0.2, sugarGrams = 82.0, addedSugarGrams = 82.0, saturatedFatGrams = 0.0, sodiumMg = 4.0,
                potassiumMg = 52.0, calciumMg = 6.0, ironMg = 0.4, vitaminCMg = 0.5,
                magnesiumMg = 2.0, zincMg = 0.2
            )
        ),
        Profile(
            listOf("whey", "protein powder", "protein shake"),
            Micronutrients(
                sugarGrams = 4.0, saturatedFatGrams = 1.5, sodiumMg = 250.0, potassiumMg = 500.0,
                calciumMg = 400.0, ironMg = 1.0, vitaminB12Mcg = 1.0, magnesiumMg = 60.0, zincMg = 1.5
            )
        ),
        Profile(
            listOf("olive oil", "oil", "butter", "boter"),
            Micronutrients(
                sugarGrams = 0.0, saturatedFatGrams = 14.0, sodiumMg = 2.0, potassiumMg = 1.0,
                calciumMg = 1.0, ironMg = 0.1, magnesiumMg = 0.0, zincMg = 0.0
            )
        )
    )

    /** Best-guess per-100g micronutrients for [name], or null when nothing plausibly matches. */
    fun guessPer100g(name: String): Micronutrients? {
        val n = name.lowercase()
        // Longest keyword wins, so "protein shake" beats a bare "shake"-adjacent match.
        return profiles
            .mapNotNull { profile ->
                profile.keywords.filter { n.contains(it) }.maxByOrNull { it.length }?.let { it.length to profile }
            }
            .maxByOrNull { it.first }
            ?.second
            ?.micros
    }

    /** True when every tracked field is null — i.e. the food contributes nothing to the screen. */
    fun isEmpty(micros: Micronutrients): Boolean = with(micros) {
        listOf(
            fiberGrams, sugarGrams, addedSugarGrams, saturatedFatGrams, sodiumMg, potassiumMg, calciumMg,
            ironMg, vitaminCMg, vitaminDMcg, vitaminB12Mcg, magnesiumMg, zincMg
        ).all { it == null }
    }
}
