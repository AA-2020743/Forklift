package com.caloriecalc.app.ui.foodlog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.caloriecalc.app.domain.findFoodEnergyMismatch
import kotlin.math.roundToInt

/** Whether the macro values a user is typing describe 100g of the food or one whole serving. */
enum class MacroBasis { PER_100G, PER_SERVING }

private fun basisGrams(basis: MacroBasis, servingGrams: Double?): Double =
    if (basis == MacroBasis.PER_100G) 100.0 else (servingGrams?.takeIf { it > 0 } ?: 100.0)

/**
 * Converts a macro value from one basis to another (e.g. per-serving as typed on a nutrition
 * label -> per-100g, the unit [com.caloriecalc.app.data.local.entity.FoodItem] always stores).
 * Used both to compute the final stored values on save, and to keep the on-screen numbers
 * correct when the user flips the basis mid-entry.
 */
fun convertMacroBasis(value: Double, from: MacroBasis, to: MacroBasis, servingGrams: Double?): Double {
    val fromGrams = basisGrams(from, servingGrams)
    val toGrams = basisGrams(to, servingGrams)
    if (fromGrams <= 0) return value
    return value * toGrams / fromGrams
}

/** Rounds to 2dp and trims a trailing ".0" so a round-tripped basis conversion doesn't grow
 * visible floating-point noise like "120.00000001" in the text field. */
fun formatMacroValue(value: Double): String {
    val rounded = Math.round(value * 100) / 100.0
    return if (rounded == rounded.toLong().toDouble()) rounded.toLong().toString() else rounded.toString()
}

fun isNonNegativeNumberOrBlank(value: String): Boolean =
    value.isBlank() || value.toDoubleOrNull()?.let { it.isFinite() && it >= 0.0 } == true

fun isPositiveNumber(value: String): Boolean =
    value.toDoubleOrNull()?.let { it.isFinite() && it > 0.0 } == true

/**
 * The macro-entry portion of the manual/edit food forms: a Per 100g / Per serving toggle plus
 * the calorie/protein/fat/carb fields, labeled to match whichever basis is selected. Serving
 * size is required (and asked for first) in Per serving mode since it's needed to convert back
 * to per-100g for storage; it stays optional in Per 100g mode, where it only exists to unlock
 * the "servings" option later when logging this food.
 */
@Composable
fun FoodMacroFields(
    basis: MacroBasis,
    onBasisChange: (MacroBasis) -> Unit,
    calories: String,
    onCaloriesChange: (String) -> Unit,
    protein: String,
    onProteinChange: (String) -> Unit,
    fat: String,
    onFatChange: (String) -> Unit,
    carbs: String,
    onCarbsChange: (String) -> Unit,
    servingGrams: String,
    onServingGramsChange: (String) -> Unit,
    servingName: String,
    onServingNameChange: (String) -> Unit,
    waterContentPercent: String = "",
    onWaterContentPercentChange: ((String) -> Unit)? = null
) {
    Text("Are these values per 100g or per serving?", style = MaterialTheme.typography.bodyMedium)
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = basis == MacroBasis.PER_100G,
            onClick = { onBasisChange(MacroBasis.PER_100G) },
            label = { Text("Per 100g") }
        )
        FilterChip(
            selected = basis == MacroBasis.PER_SERVING,
            onClick = { onBasisChange(MacroBasis.PER_SERVING) },
            label = { Text("Per serving") }
        )
    }
    Spacer(modifier = Modifier.height(16.dp))

    val unitSuffix = if (basis == MacroBasis.PER_100G) "100g" else "serving"
    val basisLabel = if (basis == MacroBasis.PER_100G) "100 g" else "serving"
    val energyMismatch = findFoodEnergyMismatch(
        calories.toDoubleOrNull(),
        protein.toMacroValueOrNull(),
        fat.toMacroValueOrNull(),
        carbs.toMacroValueOrNull()
    )

    if (basis == MacroBasis.PER_SERVING) {
        OutlinedTextField(
            value = servingGrams,
            onValueChange = onServingGramsChange,
            label = { Text("Serving size in grams") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = servingName,
            onValueChange = onServingNameChange,
            label = { Text("Serving name (optional, e.g. \"1 bar\")") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
    }

    OutlinedTextField(
        value = calories,
        onValueChange = onCaloriesChange,
        label = { Text("Calories / $unitSuffix") },
        isError = energyMismatch != null,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = fat,
        onValueChange = onFatChange,
        label = { Text("Fat g / $unitSuffix") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = carbs,
        onValueChange = onCarbsChange,
        label = { Text("Carbs g / $unitSuffix") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = protein,
        onValueChange = onProteinChange,
        label = { Text("Protein g / $unitSuffix") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )

    CalorieMacroMismatchWarning(
        calories = calories.toDoubleOrNull(),
        protein = protein.toMacroValueOrNull(),
        fat = fat.toMacroValueOrNull(),
        carbs = carbs.toMacroValueOrNull(),
        basisLabel = basisLabel
    )

    if (basis == MacroBasis.PER_100G) {
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = servingGrams,
            onValueChange = onServingGramsChange,
            label = { Text("Serving size in grams (optional — enables \"servings\" when logging)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = servingName,
            onValueChange = onServingNameChange,
            label = { Text("Serving name (optional, e.g. \"1 bar\")") },
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (onWaterContentPercentChange != null) {
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = waterContentPercent,
            onValueChange = onWaterContentPercentChange,
            label = { Text("Water content % (optional — counts toward hydration)") },
            supportingText = { Text("Pre-filled for recognised drinks. Milk ≈ 89, coffee ≈ 99, soup ≈ 92.") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private fun String.toMacroValueOrNull(): Double? = if (isBlank()) 0.0 else toDoubleOrNull()

@Composable
fun CalorieMacroMismatchWarning(
    calories: Double?,
    protein: Double?,
    fat: Double?,
    carbs: Double?,
    basisLabel: String = "100 g"
) {
    val mismatch = findFoodEnergyMismatch(calories, protein, fat, carbs) ?: return
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Nutrition mismatch: the macros imply about ${mismatch.macroCalories.roundToInt()} kcal per " +
            "$basisLabel, but ${mismatch.statedCalories.roundToInt()} kcal is listed. Review these values before logging.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.error
    )
}
