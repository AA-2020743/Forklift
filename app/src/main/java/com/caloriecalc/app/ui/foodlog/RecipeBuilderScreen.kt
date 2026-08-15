package com.caloriecalc.app.ui.foodlog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.caloriecalc.app.data.local.entity.FoodItem
import com.caloriecalc.app.data.local.entity.Micronutrients
import com.caloriecalc.app.di.rememberAppContainer
import com.caloriecalc.app.ui.components.FoodIconBadge
import com.caloriecalc.app.ui.components.foodItemIcon
import com.caloriecalc.app.ui.components.formatGrams
import com.caloriecalc.app.ui.components.stableAccentColor
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private data class RecipeIngredient(val food: FoodItem, val gramsText: String)

/**
 * Generalizes the Protein Shake pattern to any number of ingredients: search and add
 * existing foods with a gram amount each, see a live running total, and save the whole
 * thing as one loggable food. Unlike the shake (whose "100g" stands for the whole shake,
 * since it's drunk in one go), a recipe stores genuine per-100g values — the natural shape
 * for a dish you might portion differently next time — with the as-built batch weight kept
 * as its serving size for a one-tap "the whole batch" log.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeBuilderScreen(
    mealSlotId: Long,
    onBack: () -> Unit,
    onSaved: (foodId: Long, mealSlotId: Long) -> Unit
) {
    val container = rememberAppContainer()
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var query by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<FoodItem>>(emptyList()) }
    val ingredients = remember { mutableStateListOf<RecipeIngredient>() }

    LaunchedEffect(query) {
        searchResults = if (query.isBlank()) emptyList() else container.foodRepository.searchLocal(query).first()
    }

    val validIngredients = ingredients.mapNotNull { ing ->
        val grams = ing.gramsText.toDoubleOrNull()
        if (grams != null && grams > 0) ing.food to grams else null
    }
    val totalGrams = validIngredients.sumOf { it.second }
    val totalCalories = validIngredients.sumOf { (food, grams) -> food.caloriesPer100g * grams / 100.0 }
    val totalFat = validIngredients.sumOf { (food, grams) -> food.fatPer100g * grams / 100.0 }
    val totalCarbs = validIngredients.sumOf { (food, grams) -> food.carbsPer100g * grams / 100.0 }
    val totalProtein = validIngredients.sumOf { (food, grams) -> food.proteinPer100g * grams / 100.0 }
    val totalMicros = validIngredients.fold(Micronutrients()) { acc, (food, grams) ->
        acc + food.micronutrients.scaledBy(grams / 100.0)
    }

    val allGramsValid = ingredients.isNotEmpty() &&
        ingredients.all { (it.gramsText.toDoubleOrNull() ?: 0.0) > 0.0 }
    val isValid = name.isNotBlank() && allGramsValid && totalGrams > 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Build a recipe") },
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
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Recipe name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text("Ingredients", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (ingredients.isEmpty()) {
                Text(
                    "Search below and tap a food to add it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                ingredients.forEachIndexed { index, ingredient ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FoodIconBadge(
                            icon = foodItemIcon(ingredient.food.name),
                            accentColor = stableAccentColor(ingredient.food.name),
                            size = 28.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ingredient.food.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = ingredient.gramsText,
                            onValueChange = { ingredients[index] = ingredient.copy(gramsText = it) },
                            label = { Text("g") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.widthIn(min = 84.dp)
                        )
                        IconButton(onClick = { ingredients.removeAt(index) }) {
                            Icon(Icons.Filled.Close, contentDescription = "Remove ${ingredient.food.name}")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                label = { Text("Search foods to add") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            searchResults.take(5).forEach { food ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    onClick = {
                        ingredients.add(RecipeIngredient(food, "100"))
                        query = ""
                        searchResults = emptyList()
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FoodIconBadge(
                            icon = foodItemIcon(food.name),
                            accentColor = stableAccentColor(food.name),
                            size = 28.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(food.name, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "${food.caloriesPer100g.roundToInt()} kcal / 100g",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Whole batch", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("${formatGrams(totalGrams)} g · ${totalCalories.roundToInt()} kcal")
                    Text("Fat: ${formatGrams(totalFat)} g")
                    Text("Carbs: ${formatGrams(totalCarbs)} g")
                    Text("Protein: ${formatGrams(totalProtein)} g")
                    if (totalGrams > 0.0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Per 100g: ${(totalCalories / totalGrams * 100).roundToInt()} kcal — " +
                                "logging this food later works at any gram amount.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    scope.launch {
                        val food = container.foodRepository.createManualFood(
                            name = name.trim(),
                            brand = null,
                            caloriesPer100g = totalCalories / totalGrams * 100.0,
                            proteinPer100g = totalProtein / totalGrams * 100.0,
                            fatPer100g = totalFat / totalGrams * 100.0,
                            carbsPer100g = totalCarbs / totalGrams * 100.0,
                            servingSizeGrams = totalGrams,
                            servingName = "Whole batch (${formatGrams(totalGrams)} g)",
                            micronutrients = totalMicros.scaledBy(100.0 / totalGrams)
                        )
                        onSaved(food.id, mealSlotId)
                    }
                },
                enabled = isValid,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save & continue")
            }
        }
    }
}
