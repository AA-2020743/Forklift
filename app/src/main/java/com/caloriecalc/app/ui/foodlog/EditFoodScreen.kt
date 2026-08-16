package com.caloriecalc.app.ui.foodlog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.caloriecalc.app.data.local.entity.FoodItem
import com.caloriecalc.app.di.rememberAppContainer
import kotlinx.coroutines.launch

/**
 * Edits a food already in the user's list. Storage is always per-100g ([FoodItem]), so this
 * always opens in Per 100g mode showing exactly what's stored; switching to Per serving (and
 * back) live-converts the numbers via [convertMacroBasis], same as the create form.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditFoodScreen(
    foodId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val container = rememberAppContainer()
    val scope = rememberCoroutineScope()

    var loaded by remember { mutableStateOf<FoodItem?>(null) }
    LaunchedEffect(foodId) {
        loaded = container.foodRepository.getById(foodId)
    }

    val food = loaded
    if (food == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Edit food") },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                    }
                )
            }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        return
    }

    var name by remember(food) { mutableStateOf(food.name) }
    var brand by remember(food) { mutableStateOf(food.brand ?: "") }
    var basis by remember(food) { mutableStateOf(MacroBasis.PER_100G) }
    var calories by remember(food) { mutableStateOf(formatMacroValue(food.caloriesPer100g)) }
    var protein by remember(food) { mutableStateOf(formatMacroValue(food.proteinPer100g)) }
    var fat by remember(food) { mutableStateOf(formatMacroValue(food.fatPer100g)) }
    var carbs by remember(food) { mutableStateOf(formatMacroValue(food.carbsPer100g)) }
    var servingGrams by remember(food) { mutableStateOf(food.servingSizeGrams?.let { formatMacroValue(it) } ?: "") }
    var servingName by remember(food) { mutableStateOf(food.servingName ?: "") }
    var waterContent by remember(food) {
        mutableStateOf(food.waterContentPercent?.let { formatMacroValue(it) } ?: "")
    }

    val onBasisChange: (MacroBasis) -> Unit = { newBasis ->
        val grams = servingGrams.toDoubleOrNull()
        calories = calories.toDoubleOrNull()?.let { formatMacroValue(convertMacroBasis(it, basis, newBasis, grams)) } ?: calories
        protein = protein.toDoubleOrNull()?.let { formatMacroValue(convertMacroBasis(it, basis, newBasis, grams)) } ?: protein
        fat = fat.toDoubleOrNull()?.let { formatMacroValue(convertMacroBasis(it, basis, newBasis, grams)) } ?: fat
        carbs = carbs.toDoubleOrNull()?.let { formatMacroValue(convertMacroBasis(it, basis, newBasis, grams)) } ?: carbs
        basis = newBasis
    }

    val servingGramsValue = servingGrams.toDoubleOrNull()
    val isValid = name.isNotBlank() && calories.toDoubleOrNull() != null &&
        (basis == MacroBasis.PER_100G || (servingGramsValue != null && servingGramsValue > 0))

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit food") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Changes only apply going forward — meals you've already logged with this food " +
                    "keep the values they were logged with.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = brand, onValueChange = { brand = it }, label = { Text("Brand (optional)") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))

            FoodMacroFields(
                basis = basis,
                onBasisChange = onBasisChange,
                calories = calories,
                onCaloriesChange = { calories = it },
                protein = protein,
                onProteinChange = { protein = it },
                fat = fat,
                onFatChange = { fat = it },
                carbs = carbs,
                onCarbsChange = { carbs = it },
                servingGrams = servingGrams,
                onServingGramsChange = { servingGrams = it },
                servingName = servingName,
                onServingNameChange = { servingName = it },
                waterContentPercent = waterContent,
                onWaterContentPercentChange = { waterContent = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    scope.launch {
                        container.foodRepository.updateFood(
                            food.copy(
                                name = name.trim(),
                                brand = brand.trim().takeIf { it.isNotBlank() },
                                caloriesPer100g = convertMacroBasis(calories.toDoubleOrNull() ?: 0.0, basis, MacroBasis.PER_100G, servingGramsValue),
                                proteinPer100g = convertMacroBasis(protein.toDoubleOrNull() ?: 0.0, basis, MacroBasis.PER_100G, servingGramsValue),
                                fatPer100g = convertMacroBasis(fat.toDoubleOrNull() ?: 0.0, basis, MacroBasis.PER_100G, servingGramsValue),
                                carbsPer100g = convertMacroBasis(carbs.toDoubleOrNull() ?: 0.0, basis, MacroBasis.PER_100G, servingGramsValue),
                                servingSizeGrams = servingGramsValue,
                                servingName = servingName.trim().takeIf { it.isNotBlank() },
                                waterContentPercent = waterContent.toDoubleOrNull()?.coerceIn(0.0, 100.0)
                            )
                        )
                        onSaved()
                    }
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save changes")
            }
        }
    }
}
