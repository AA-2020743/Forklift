package com.caloriecalc.app.ui.foodlog

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Blender
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.caloriecalc.app.data.local.dao.MealTemplateSummary
import com.caloriecalc.app.data.local.entity.FoodItem
import com.caloriecalc.app.di.SimpleViewModelFactory
import com.caloriecalc.app.di.rememberAppContainer
import com.caloriecalc.app.ui.components.FoodIconBadge
import com.caloriecalc.app.ui.components.foodItemIcon
import com.caloriecalc.app.ui.components.stableAccentColor
import com.caloriecalc.app.ui.navigation.QUICK_ADD_MEAL_SLOT_ID
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodScreen(
    mealSlotId: Long,
    epochDay: Long,
    onBack: () -> Unit,
    onScanBarcode: (Long) -> Unit,
    onManualEntry: (Long) -> Unit,
    onPhotoEstimate: (Long) -> Unit,
    onFoodSelected: (foodId: Long, mealSlotId: Long) -> Unit,
    onQuickAdded: (Long) -> Unit,
    onEditFood: (foodId: Long) -> Unit,
    onCreateProteinShake: (Long) -> Unit,
    onCreateRecipe: (Long) -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: AddFoodViewModel = viewModel(
        factory = SimpleViewModelFactory { AddFoodViewModel(container.foodRepository, container.mealTemplateRepository) }
    )
    val query by viewModel.query.collectAsStateWithLifecycle()
    val localResults by viewModel.localResults.collectAsStateWithLifecycle()
    val recent by viewModel.recent.collectAsStateWithLifecycle()
    val frequent by viewModel.frequent.collectAsStateWithLifecycle()
    val onlineResults by viewModel.onlineResults.collectAsStateWithLifecycle()
    val isSearchingOnline by viewModel.isSearchingOnline.collectAsStateWithLifecycle()
    val onlineError by viewModel.onlineError.collectAsStateWithLifecycle()
    val hasSearchedOnline by viewModel.hasSearchedOnline.collectAsStateWithLifecycle()
    val templates by viewModel.templates.collectAsStateWithLifecycle()

    val isQuickAdd = mealSlotId == QUICK_ADD_MEAL_SLOT_ID
    val isToday = epochDay == LocalDate.now().toEpochDay()
    var mealName by remember { mutableStateOf("meal") }
    LaunchedEffect(mealSlotId) {
        if (!isQuickAdd) {
            mealName = container.mealSlotRepository.getById(mealSlotId)?.name ?: "meal"
        }
    }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showQuickAddDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val onSelect: (FoodItem) -> Unit = { food ->
        scope.launch {
            val saved = viewModel.selectFood(food)
            onFoodSelected(saved.id, mealSlotId)
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        val base = if (isQuickAdd) "Scan or add food" else "Add to $mealName"
                        Text(
                            if (isToday) base
                            else "$base — ${LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ofPattern("EEE, MMM d"))}"
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        if (!isQuickAdd) {
                            IconButton(onClick = { onPhotoEstimate(mealSlotId) }) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = "Estimate from photo")
                            }
                            IconButton(onClick = { showQuickAddDialog = true }) {
                                Icon(Icons.Filled.Bolt, contentDescription = "Quick add calories")
                            }
                        }
                        IconButton(onClick = { onScanBarcode(mealSlotId) }) {
                            Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scan barcode")
                        }
                        IconButton(onClick = { onCreateProteinShake(mealSlotId) }) {
                            Icon(Icons.Filled.Blender, contentDescription = "Create protein shake")
                        }
                        IconButton(onClick = { onCreateRecipe(mealSlotId) }) {
                            Icon(Icons.Filled.DinnerDining, contentDescription = "Build a recipe")
                        }
                        IconButton(onClick = { onManualEntry(mealSlotId) }) {
                            Icon(Icons.Filled.Add, contentDescription = "Enter manually")
                        }
                    }
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = viewModel::onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Search foods") },
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) }
                )
                ScrollableTabRow(selectedTabIndex = selectedTab, edgePadding = 16.dp) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Search") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Recent") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Frequent") })
                    if (!isQuickAdd) {
                        Tab(selected = selectedTab == 3, onClick = { selectedTab = 3 }, text = { Text("Templates") })
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                0 -> SearchTabContent(
                    query = query,
                    localResults = localResults,
                    onlineResults = onlineResults,
                    isSearchingOnline = isSearchingOnline,
                    onlineError = onlineError,
                    hasSearchedOnline = hasSearchedOnline,
                    onSearchOnline = viewModel::searchOnline,
                    onSelect = onSelect,
                    onEditFood = onEditFood
                )
                1 -> FoodListContent(recent, onSelect, onEditFood, "No recently used foods yet.")
                2 -> FoodListContent(frequent, onSelect, onEditFood, "No frequently used foods yet.")
                else -> TemplatesTabContent(
                    templates = templates,
                    onApply = { templateId ->
                        scope.launch {
                            viewModel.applyTemplate(templateId, mealSlotId, epochDay)
                            onQuickAdded(mealSlotId)
                        }
                    },
                    onDelete = viewModel::deleteTemplate
                )
            }
        }
    }

    if (showQuickAddDialog) {
        var label by remember { mutableStateOf("") }
        var calories by remember { mutableStateOf("") }
        var protein by remember { mutableStateOf("") }
        var fat by remember { mutableStateOf("") }
        var carbs by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showQuickAddDialog = false },
            title = { Text("Quick add calories") },
            text = {
                Column {
                    Text(
                        "For eating out or whenever you just know the calories.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Label (optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = calories,
                        onValueChange = { calories = it },
                        label = { Text("Calories") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fat,
                            onValueChange = { fat = it },
                            label = { Text("Fat g") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = carbs,
                            onValueChange = { carbs = it },
                            label = { Text("Carbs g") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = protein,
                            onValueChange = { protein = it },
                            label = { Text("Protein g") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val cal = calories.toDoubleOrNull()
                        if (cal != null) {
                            scope.launch {
                                // Store as "per 100g" = the whole logged amount, then log exactly
                                // 100g of it — a simple way to reuse the existing per-100g model
                                // for a food whose total amount is what was actually typed.
                                val food = container.foodRepository.createManualFood(
                                    name = label.trim().ifBlank { "Quick add" },
                                    brand = null,
                                    caloriesPer100g = cal,
                                    proteinPer100g = protein.toDoubleOrNull() ?: 0.0,
                                    fatPer100g = fat.toDoubleOrNull() ?: 0.0,
                                    carbsPer100g = carbs.toDoubleOrNull() ?: 0.0,
                                    servingSizeGrams = null,
                                    servingName = null
                                )
                                container.nutritionLogRepository.logFood(
                                    food,
                                    mealSlotId,
                                    epochDay,
                                    100.0
                                )
                                onQuickAdded(mealSlotId)
                            }
                        }
                        showQuickAddDialog = false
                    },
                    enabled = calories.toDoubleOrNull() != null
                ) { Text("Log") }
            },
            dismissButton = {
                TextButton(onClick = { showQuickAddDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SearchTabContent(
    query: String,
    localResults: List<FoodItem>,
    onlineResults: List<FoodItem>,
    isSearchingOnline: Boolean,
    onlineError: String?,
    hasSearchedOnline: Boolean,
    onSearchOnline: () -> Unit,
    onSelect: (FoodItem) -> Unit,
    onEditFood: (Long) -> Unit
) {
    if (query.isBlank()) {
        EmptyState("Search for a food by name, or scan a barcode.")
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (localResults.isNotEmpty()) {
            item { Text("From your food list", style = MaterialTheme.typography.titleMedium) }
            items(localResults, key = { "local_${it.id}" }) { FoodRow(it, onSelect, onEditFood) }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Open Food Facts", style = MaterialTheme.typography.titleMedium)
                Button(onClick = onSearchOnline, enabled = !isSearchingOnline) {
                    Text(if (isSearchingOnline) "Searching…" else "Search online")
                }
            }
        }
        when {
            isSearchingOnline -> item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }
            // A failed search and a search that genuinely matched nothing used to look
            // identical (both just showed nothing). Say which one happened.
            onlineError != null -> item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.CloudOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = onlineError,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "You can still add \"$query\" manually with the + button.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
            }
            onlineResults.isNotEmpty() ->
                items(onlineResults, key = { "online_${it.barcode ?: it.name}" }) {
                    FoodRow(it, onSelect, onEditFood)
                }
            hasSearchedOnline -> item {
                Text(
                    "No products matched \"$query\" on Open Food Facts. It's crowd-sourced, so " +
                        "coverage varies — adding it manually only takes a moment.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun FoodListContent(
    foods: List<FoodItem>,
    onSelect: (FoodItem) -> Unit,
    onEditFood: (Long) -> Unit,
    emptyMessage: String
) {
    if (foods.isEmpty()) {
        EmptyState(emptyMessage)
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(foods, key = { it.id }) { FoodRow(it, onSelect, onEditFood) }
    }
}

@Composable
private fun TemplatesTabContent(
    templates: List<MealTemplateSummary>,
    onApply: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    if (templates.isEmpty()) {
        EmptyState("No saved templates yet. Log a meal, then \"Save as template\" from its screen.")
        return
    }
    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(templates, key = { it.id }) { template ->
            Card(modifier = Modifier.fillMaxWidth(), onClick = { onApply(template.id) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(template.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "${template.itemCount} item${if (template.itemCount == 1) "" else "s"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = { onDelete(template.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete ${template.name}")
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodRow(food: FoodItem, onSelect: (FoodItem) -> Unit, onEditFood: (Long) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), onClick = { onSelect(food) }) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FoodIconBadge(icon = foodItemIcon(food.name), accentColor = stableAccentColor(food.name))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(food.name, style = MaterialTheme.typography.bodyLarge)
                val brandPrefix = food.brand?.let { "$it · " } ?: ""
                Text(
                    text = "$brandPrefix${food.caloriesPer100g.toInt()} kcal / 100g",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (food.id != 0L) {
                IconButton(onClick = { onEditFood(food.id) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit ${food.name}")
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Restaurant,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
