package com.caloriecalc.app.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import android.text.format.DateFormat
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.caloriecalc.app.data.local.entity.UserProfile
import com.caloriecalc.app.di.SimpleViewModelFactory
import com.caloriecalc.app.di.rememberAppContainer
import com.caloriecalc.app.domain.ActivityLevel
import com.caloriecalc.app.domain.Goal
import com.caloriecalc.app.domain.NutritionCalculator
import com.caloriecalc.app.domain.Sex
import java.util.Calendar
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val container = rememberAppContainer()
    val viewModel: ProfileViewModel = viewModel(
        factory = SimpleViewModelFactory { ProfileViewModel(container.profileRepository, container.reminderScheduler) }
    )
    val mealSlots by container.mealSlotRepository.observeActive().collectAsStateWithLifecycle(initialValue = emptyList())
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var initialProfile by remember { mutableStateOf<UserProfile?>(null) }
    LaunchedEffect(Unit) {
        initialProfile = container.profileRepository.getProfile()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profile") }) }
    ) { padding ->
        val loaded = initialProfile
        if (loaded == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        var bodyWeightText by remember(loaded) { mutableStateOf(loaded.bodyWeightKg.toString()) }
        var heightText by remember(loaded) { mutableStateOf(loaded.heightCm.toString()) }
        var ageText by remember(loaded) { mutableStateOf(loaded.age.toString()) }
        var sex by remember(loaded) { mutableStateOf(loaded.sex) }
        var activityLevel by remember(loaded) { mutableStateOf(loaded.activityLevel) }
        var goal by remember(loaded) { mutableStateOf(loaded.goal) }
        var proteinMinText by remember(loaded) { mutableStateOf(loaded.proteinMinGramsPerKg.toString()) }
        var proteinMaxText by remember(loaded) { mutableStateOf(loaded.proteinMaxGramsPerKg.toString()) }
        var fatMinText by remember(loaded) { mutableStateOf((loaded.fatPercentMin * 100).roundToInt().toString()) }
        var fatMaxText by remember(loaded) { mutableStateOf((loaded.fatPercentMax * 100).roundToInt().toString()) }
        var manualCalorieText by remember(loaded) { mutableStateOf(loaded.manualCalorieTarget?.toString() ?: "") }
        var reminderEnabled by remember(loaded) { mutableStateOf(loaded.weightReminderEnabled) }
        var reminderHour by remember(loaded) { mutableIntStateOf(loaded.weightReminderHour) }
        var reminderMinute by remember(loaded) { mutableIntStateOf(loaded.weightReminderMinute) }
        var showTimePicker by remember { mutableStateOf(false) }
        var newMealName by remember { mutableStateOf("") }
        var geminiApiKeyText by remember { mutableStateOf(container.apiKeyStore.getGeminiApiKey() ?: "") }
        var geminiKeySaved by remember { mutableStateOf(false) }

        // Live preview: recomputed from whatever is currently typed, before Save is pressed.
        val liveProfile = loaded.copy(
            bodyWeightKg = bodyWeightText.toDoubleOrNull() ?: loaded.bodyWeightKg,
            heightCm = heightText.toDoubleOrNull() ?: loaded.heightCm,
            age = ageText.toIntOrNull() ?: loaded.age,
            sex = sex,
            activityLevel = activityLevel,
            goal = goal,
            proteinMinGramsPerKg = proteinMinText.toDoubleOrNull() ?: loaded.proteinMinGramsPerKg,
            proteinMaxGramsPerKg = proteinMaxText.toDoubleOrNull() ?: loaded.proteinMaxGramsPerKg,
            fatPercentMin = (fatMinText.toDoubleOrNull() ?: (loaded.fatPercentMin * 100)) / 100.0,
            fatPercentMax = (fatMaxText.toDoubleOrNull() ?: (loaded.fatPercentMax * 100)) / 100.0,
            manualCalorieTarget = manualCalorieText.toIntOrNull()
        )
        val liveTargets = remember(liveProfile) { NutritionCalculator.computeTargets(liveProfile) }
        val autoCalorieTarget = remember(liveProfile) { NutritionCalculator.computeAutoCalorieTarget(liveProfile) }
        val suggested = remember(goal, sex) { NutritionCalculator.suggestedRanges(goal, sex) }
        val minimumsExceedCalories = liveTargets.protein.minGrams * 4 + liveTargets.fat.minGrams * 9 >
            liveTargets.calorieTarget

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Your daily targets", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Updates instantly as you edit below — press Save to keep changes.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Calories: ${liveTargets.calorieTarget} kcal")
                        Text(
                            "Protein: ${liveTargets.protein.minGrams.roundToInt()}-" +
                                "${liveTargets.protein.maxGrams.roundToInt()} g"
                        )
                        Text(
                            "Fat: ${liveTargets.fat.minGrams.roundToInt()}-" +
                                "${liveTargets.fat.maxGrams.roundToInt()} g"
                        )
                        Text(
                            "Carbs: ${liveTargets.carbs.minGrams.roundToInt()}-" +
                                "${liveTargets.carbs.maxGrams.roundToInt()} g"
                        )
                        if (minimumsExceedCalories) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Your minimum protein + fat needs alone exceed this calorie target, " +
                                    "so carbs are being squeezed to 0g. Raise calories or lower the minimums.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Body stats", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = bodyWeightText,
                            onValueChange = { bodyWeightText = it },
                            label = { Text("Body weight (kg)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = heightText,
                            onValueChange = { heightText = it },
                            label = { Text("Height (cm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = ageText,
                            onValueChange = { ageText = it },
                            label = { Text("Age") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Sex", style = MaterialTheme.typography.bodyMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(Sex.entries.toList()) { option ->
                                FilterChip(
                                    selected = sex == option,
                                    onClick = { sex = option },
                                    label = { Text(option.name.lowercase().replaceFirstChar { c -> c.uppercase() }) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Activity level", style = MaterialTheme.typography.bodyMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(ActivityLevel.entries.toList()) { option ->
                                FilterChip(
                                    selected = activityLevel == option,
                                    onClick = { activityLevel = option },
                                    label = { Text(option.displayName) }
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Goal", style = MaterialTheme.typography.bodyMedium)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(Goal.entries.toList()) { option ->
                                FilterChip(
                                    selected = goal == option,
                                    onClick = { goal = option },
                                    label = { Text(option.displayName) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Nutrition targets", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = proteinMinText,
                                onValueChange = { proteinMinText = it },
                                label = { Text("Protein min (g/kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = proteinMaxText,
                                onValueChange = { proteinMaxText = it },
                                label = { Text("Protein max (g/kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Suggested for ${goal.displayName}: " +
                                    "${formatSuggestion(suggested.proteinMinGramsPerKg)}-" +
                                    "${formatSuggestion(suggested.proteinMaxGramsPerKg)} g/kg",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = {
                                proteinMinText = formatSuggestion(suggested.proteinMinGramsPerKg)
                                proteinMaxText = formatSuggestion(suggested.proteinMaxGramsPerKg)
                            }) { Text("Use") }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = fatMinText,
                                onValueChange = { fatMinText = it },
                                label = { Text("Fat min (% kcal)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = fatMaxText,
                                onValueChange = { fatMaxText = it },
                                label = { Text("Fat max (% kcal)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Suggested: ${(suggested.fatPercentMin * 100).roundToInt()}-" +
                                    "${(suggested.fatPercentMax * 100).roundToInt()}% of calories",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = {
                                fatMinText = (suggested.fatPercentMin * 100).roundToInt().toString()
                                fatMaxText = (suggested.fatPercentMax * 100).roundToInt().toString()
                            }) { Text("Use") }
                        }
                        if (sex == Sex.MALE) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Fat floor is clamped to at least 20% of calories for male sex " +
                                    "regardless of this setting (testosterone-related).",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = manualCalorieText,
                            onValueChange = { manualCalorieText = it },
                            label = { Text("Total calories for this period") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Calculated from your stats + goal: $autoCalorieTarget kcal. " +
                                    "Fat, protein, and carbs above adjust automatically to whatever total you set.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { manualCalorieText = "" }) { Text("Reset") }
                        }
                        val manualBelowSafe = manualCalorieText.toIntOrNull()
                            ?.let { it < NutritionCalculator.MIN_SAFE_CALORIES } ?: false
                        if (manualBelowSafe) {
                            Text(
                                "Below the generally recommended safe minimum of " +
                                    "${NutritionCalculator.MIN_SAFE_CALORIES} kcal/day.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Meals", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        mealSlots.forEach { slot ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(slot.name)
                                IconButton(onClick = {
                                    scope.launch { container.mealSlotRepository.removeMeal(slot.id) }
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove ${slot.name}")
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newMealName,
                                onValueChange = { newMealName = it },
                                label = { Text("New meal name") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(onClick = {
                                val name = newMealName.trim()
                                if (name.isNotEmpty()) {
                                    scope.launch { container.mealSlotRepository.addMeal(name) }
                                    newMealName = ""
                                }
                            }) { Text("Add") }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("AI photo estimation", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Free Gemini API key from Google AI Studio (aistudio.google.com), used only to " +
                                "estimate calories from meal photos you choose to send. Stored encrypted on-device.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = geminiApiKeyText,
                            onValueChange = { geminiApiKeyText = it; geminiKeySaved = false },
                            label = { Text("Gemini API key") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = {
                                container.apiKeyStore.setGeminiApiKey(geminiApiKeyText.trim())
                                geminiKeySaved = true
                            }) { Text("Save key") }
                            if (geminiKeySaved) {
                                Text("Saved", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Weight reminder", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Daily reminder to log weight")
                            Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                        }
                        if (reminderEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Reminder time: ${formatReminderTime(context, reminderHour, reminderMinute)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                TextButton(onClick = { showTimePicker = true }) { Text("Change") }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = {
                        val updated = loaded.copy(
                            bodyWeightKg = bodyWeightText.toDoubleOrNull() ?: loaded.bodyWeightKg,
                            heightCm = heightText.toDoubleOrNull() ?: loaded.heightCm,
                            age = ageText.toIntOrNull() ?: loaded.age,
                            sex = sex,
                            activityLevel = activityLevel,
                            goal = goal,
                            proteinMinGramsPerKg = proteinMinText.toDoubleOrNull() ?: loaded.proteinMinGramsPerKg,
                            proteinMaxGramsPerKg = proteinMaxText.toDoubleOrNull() ?: loaded.proteinMaxGramsPerKg,
                            fatPercentMin = (fatMinText.toDoubleOrNull() ?: (loaded.fatPercentMin * 100)) / 100.0,
                            fatPercentMax = (fatMaxText.toDoubleOrNull() ?: (loaded.fatPercentMax * 100)) / 100.0,
                            manualCalorieTarget = manualCalorieText.toIntOrNull(),
                            weightReminderEnabled = reminderEnabled,
                            weightReminderHour = reminderHour,
                            weightReminderMinute = reminderMinute
                        )
                        viewModel.updateProfile(updated)
                        initialProfile = updated
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
            }
        }

        if (showTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = reminderHour,
                initialMinute = reminderMinute,
                is24Hour = DateFormat.is24HourFormat(context)
            )
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        reminderHour = timePickerState.hour
                        reminderMinute = timePickerState.minute
                        showTimePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
                },
                text = { TimePicker(state = timePickerState) }
            )
        }
    }
}

private fun formatReminderTime(context: android.content.Context, hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    return DateFormat.getTimeFormat(context).format(calendar.time)
}

private fun formatSuggestion(value: Double): String =
    if (value == value.roundToInt().toDouble()) value.roundToInt().toString() else value.toString()
