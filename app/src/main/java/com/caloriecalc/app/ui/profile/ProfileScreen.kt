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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.SetMeal
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.caloriecalc.app.data.local.entity.MealSlot
import com.caloriecalc.app.data.local.entity.UserProfile
import com.caloriecalc.app.di.SimpleViewModelFactory
import com.caloriecalc.app.di.rememberAppContainer
import com.caloriecalc.app.domain.ActivityLevel
import com.caloriecalc.app.domain.Goal
import com.caloriecalc.app.domain.NutritionCalculator
import com.caloriecalc.app.domain.Sex
import com.caloriecalc.app.ui.components.FoodIconBadge
import com.caloriecalc.app.ui.components.SectionHeader
import com.caloriecalc.app.ui.components.formatGrams
import com.caloriecalc.app.ui.components.mealSlotAccentColor
import com.caloriecalc.app.ui.components.mealSlotIcon
import com.caloriecalc.app.ui.theme.StatusBelowThreshold
import com.caloriecalc.app.ui.theme.StatusOnTarget
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
    val allMealSlots by container.mealSlotRepository.observeAll().collectAsStateWithLifecycle(initialValue = emptyList())
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
        var fatMinText by remember(loaded) { mutableStateOf(loaded.fatMinGramsPerKg.toString()) }
        var fatMaxText by remember(loaded) { mutableStateOf(loaded.fatMaxGramsPerKg.toString()) }
        var manualCalorieText by remember(loaded) { mutableStateOf(loaded.manualCalorieTarget?.toString() ?: "") }
        var reminderEnabled by remember(loaded) { mutableStateOf(loaded.weightReminderEnabled) }
        var reminderHour by remember(loaded) { mutableIntStateOf(loaded.weightReminderHour) }
        var reminderMinute by remember(loaded) { mutableIntStateOf(loaded.weightReminderMinute) }
        var showTimePicker by remember { mutableStateOf(false) }
        var proteinReminderEnabled by remember(loaded) { mutableStateOf(loaded.proteinReminderEnabled) }
        var proteinGapText by remember(loaded) { mutableStateOf(loaded.proteinGapHours.toString()) }
        var proteinDoseText by remember(loaded) { mutableStateOf(loaded.proteinDoseGrams.toString()) }
        var wakeHour by remember(loaded) { mutableIntStateOf(loaded.wakeHour) }
        var wakeMinute by remember(loaded) { mutableIntStateOf(loaded.wakeMinute) }
        var sleepHour by remember(loaded) { mutableIntStateOf(loaded.sleepHour) }
        var sleepMinute by remember(loaded) { mutableIntStateOf(loaded.sleepMinute) }
        var showWakePicker by remember { mutableStateOf(false) }
        var showSleepPicker by remember { mutableStateOf(false) }
        var mealTimeTarget by remember { mutableStateOf<MealSlot?>(null) }
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
            fatMinGramsPerKg = fatMinText.toDoubleOrNull() ?: loaded.fatMinGramsPerKg,
            fatMaxGramsPerKg = fatMaxText.toDoubleOrNull() ?: loaded.fatMaxGramsPerKg,
            manualCalorieTarget = manualCalorieText.toIntOrNull()
        )
        val liveTargets = remember(liveProfile) { NutritionCalculator.computeTargets(liveProfile) }
        val autoCalorieTarget = remember(liveProfile) { NutritionCalculator.computeAutoCalorieTarget(liveProfile) }
        val suggested = remember(goal, sex) { NutritionCalculator.suggestedRanges(goal, sex) }
        val minimumsExceedCalories = liveTargets.protein.minGrams * 4 + liveTargets.fat.minGrams * 9 >
            liveTargets.calorieTarget

        // Informational check only: fat grams here come from g/kg bodyweight, but a widely-cited
        // guideline is that fat should still land within roughly 20-30% of total calories.
        val fatMinPercentOfCalories = liveTargets.fat.minGrams * 9.0 / liveTargets.calorieTarget * 100.0
        val fatMaxPercentOfCalories = liveTargets.fat.maxGrams * 9.0 / liveTargets.calorieTarget * 100.0
        val fatPercentHealthy = fatMinPercentOfCalories >= 20.0 && fatMaxPercentOfCalories <= 30.0
        val profileInputsValid = positiveInput(bodyWeightText) && positiveInput(heightText) &&
            positiveIntegerInput(ageText) && nonNegativeInput(proteinMinText) &&
            nonNegativeInput(proteinMaxText) &&
            (proteinMaxText.toDoubleOrNull() ?: -1.0) >= (proteinMinText.toDoubleOrNull() ?: 0.0) &&
            nonNegativeInput(fatMinText) && nonNegativeInput(fatMaxText) &&
            (fatMaxText.toDoubleOrNull() ?: -1.0) >= (fatMinText.toDoubleOrNull() ?: 0.0) &&
            (manualCalorieText.isBlank() || positiveInput(manualCalorieText)) &&
            (proteinGapText.toIntOrNull()?.let { it in 1..12 } == true) &&
            positiveInput(proteinDoseText)

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
                        SectionHeader(Icons.Filled.Insights, "Your daily targets")
                        Text(
                            "Updates instantly as you edit below — press Save to keep changes.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Calories: ${liveTargets.calorieTarget} kcal")
                        Text(
                            "Protein: ${formatGrams(liveTargets.protein.minGrams)}-" +
                                "${formatGrams(liveTargets.protein.maxGrams)} g"
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                "Fat: ${formatGrams(liveTargets.fat.minGrams)}-" +
                                    "${formatGrams(liveTargets.fat.maxGrams)} g"
                            )
                            Text(
                                "(${fatMinPercentOfCalories.roundToInt()}-${fatMaxPercentOfCalories.roundToInt()}% of calories)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = if (fatPercentHealthy) StatusOnTarget else StatusBelowThreshold
                            )
                        }
                        Text(
                            "Carbs: ${formatGrams(liveTargets.carbs.minGrams)}-" +
                                "${formatGrams(liveTargets.carbs.maxGrams)} g"
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
                        SectionHeader(Icons.Filled.Person, "Body stats")
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
                        SectionHeader(Icons.Filled.SetMeal, "Nutrition targets")
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
                                label = { Text("Fat min (g/kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = fatMaxText,
                                onValueChange = { fatMaxText = it },
                                label = { Text("Fat max (g/kg)") },
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
                                "Minimum ${formatSuggestion(suggested.fatMinGramsPerKg)} g/kg, good target " +
                                    "0.8-${formatSuggestion(suggested.fatMaxGramsPerKg)} g/kg",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            TextButton(onClick = {
                                fatMinText = formatSuggestion(suggested.fatMinGramsPerKg)
                                fatMaxText = formatSuggestion(suggested.fatMaxGramsPerKg)
                            }) { Text("Use") }
                        }
                        Text(
                            "= ${fatMinPercentOfCalories.roundToInt()}-${fatMaxPercentOfCalories.roundToInt()}% of " +
                                "calories at your current target " +
                                if (fatPercentHealthy) "(within the 20-30% guideline)" else "(outside the 20-30% guideline)",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            color = if (fatPercentHealthy) StatusOnTarget else StatusBelowThreshold
                        )
                        if (sex == Sex.MALE) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Fat floor is also clamped to at least 20% of calories for male sex " +
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
                        SectionHeader(Icons.Filled.RestaurantMenu, "Meals")
                        Text(
                            "Typical meal times anchor the protein-spacing nudges below. New meals start " +
                                "at the time you add them.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        mealSlots.forEach { slot ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    FoodIconBadge(
                                        icon = mealSlotIcon(slot.name),
                                        accentColor = mealSlotAccentColor(slot.name),
                                        size = 32.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(slot.name)
                                }
                                TextButton(onClick = { mealTimeTarget = slot }) {
                                    Text(
                                        if (slot.hasTargetTime) {
                                            formatClock(slot.targetHour ?: 0, slot.targetMinute ?: 0)
                                        } else {
                                            "Set time"
                                        }
                                    )
                                }
                                IconButton(onClick = {
                                    scope.launch { container.mealSlotRepository.removeMeal(slot.id) }
                                }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove ${slot.name}")
                                }
                            }
                        }
                        val archivedMeals = allMealSlots.filter { it.isArchived }
                        if (archivedMeals.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Archived meals",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            archivedMeals.forEach { slot ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        slot.name,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = {
                                        scope.launch { container.mealSlotRepository.restoreMeal(slot.id) }
                                    }) { Text("Restore") }
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
                        SectionHeader(Icons.Filled.AutoAwesome, "AI photo estimation")
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
                        SectionHeader(Icons.Filled.Notifications, "Weight reminder")
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
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(Icons.Filled.SetMeal, "Protein spacing")
                        Text(
                            "Muscle protein synthesis rises after a serving of protein and then " +
                                "settles back, so several spread-out doses beat one big one. " +
                                "Nudges never fire while you're asleep.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Remind me when I go too long")
                            Switch(checked = proteinReminderEnabled, onCheckedChange = { proteinReminderEnabled = it })
                        }
                        if (proteinReminderEnabled) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = proteinGapText,
                                    onValueChange = { proteinGapText = it },
                                    label = { Text("Gap after meal (h)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = proteinDoseText,
                                    onValueChange = { proteinDoseText = it },
                                    label = { Text("Counts as a dose (g)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Text(
                                "The timer starts from the consumed time of the latest meal whose combined foods reach the dose.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Awake ${formatClock(wakeHour, wakeMinute)} – ${formatClock(sleepHour, sleepMinute)}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Row {
                                    TextButton(onClick = { showWakePicker = true }) { Text("Wake") }
                                    TextButton(onClick = { showSleepPicker = true }) { Text("Sleep") }
                                }
                            }
                        }
                    }
                }
            }

            item {
                if (!profileInputsValid) {
                    Text(
                        "Check the numeric settings: values must be positive, and each maximum must be at least its minimum.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
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
                            fatMinGramsPerKg = fatMinText.toDoubleOrNull() ?: loaded.fatMinGramsPerKg,
                            fatMaxGramsPerKg = fatMaxText.toDoubleOrNull() ?: loaded.fatMaxGramsPerKg,
                            manualCalorieTarget = manualCalorieText.toIntOrNull(),
                            weightReminderEnabled = reminderEnabled,
                            weightReminderHour = reminderHour,
                            weightReminderMinute = reminderMinute,
                            proteinReminderEnabled = proteinReminderEnabled,
                            proteinGapHours = proteinGapText.toIntOrNull()?.coerceIn(1, 12) ?: loaded.proteinGapHours,
                            proteinDoseGrams = proteinDoseText.toDoubleOrNull()?.coerceAtLeast(1.0) ?: loaded.proteinDoseGrams,
                            wakeHour = wakeHour,
                            wakeMinute = wakeMinute,
                            sleepHour = sleepHour,
                            sleepMinute = sleepMinute
                        )
                        viewModel.updateProfile(updated)
                        initialProfile = updated
                    },
                    enabled = profileInputsValid,
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

        if (showWakePicker) {
            ClockPickerDialog(
                initialHour = wakeHour,
                initialMinute = wakeMinute,
                is24Hour = DateFormat.is24HourFormat(context),
                onDismiss = { showWakePicker = false },
                onConfirm = { h, m -> wakeHour = h; wakeMinute = m; showWakePicker = false }
            )
        }

        if (showSleepPicker) {
            ClockPickerDialog(
                initialHour = sleepHour,
                initialMinute = sleepMinute,
                is24Hour = DateFormat.is24HourFormat(context),
                onDismiss = { showSleepPicker = false },
                onConfirm = { h, m -> sleepHour = h; sleepMinute = m; showSleepPicker = false }
            )
        }

        val slotForTime = mealTimeTarget
        if (slotForTime != null) {
            ClockPickerDialog(
                initialHour = slotForTime.targetHour ?: 12,
                initialMinute = slotForTime.targetMinute ?: 0,
                is24Hour = DateFormat.is24HourFormat(context),
                onDismiss = { mealTimeTarget = null },
                onConfirm = { h, m ->
                    scope.launch { container.mealSlotRepository.setMealTime(slotForTime.id, h, m) }
                    mealTimeTarget = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ClockPickerDialog(
    initialHour: Int,
    initialMinute: Int,
    is24Hour: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = is24Hour
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = { onConfirm(state.hour, state.minute) }) { Text("OK") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
        text = { TimePicker(state = state) }
    )
}

/** Simple zero-padded 24h clock label, used where a compact fixed-width time reads better. */
private fun formatClock(hour: Int, minute: Int): String =
    "%02d:%02d".format(hour, minute)

private fun formatReminderTime(context: android.content.Context, hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    return DateFormat.getTimeFormat(context).format(calendar.time)
}

private fun formatSuggestion(value: Double): String =
    if (value == value.roundToInt().toDouble()) value.roundToInt().toString() else value.toString()

private fun positiveInput(value: String): Boolean =
    value.toDoubleOrNull()?.let { it.isFinite() && it > 0.0 } == true

private fun nonNegativeInput(value: String): Boolean =
    value.toDoubleOrNull()?.let { it.isFinite() && it >= 0.0 } == true

private fun positiveIntegerInput(value: String): Boolean =
    value.toIntOrNull()?.let { it > 0 } == true
