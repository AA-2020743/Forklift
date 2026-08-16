package com.caloriecalc.app.ui.weight

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.caloriecalc.app.data.local.entity.BodyMeasurement
import com.caloriecalc.app.data.local.entity.WeightLog
import com.caloriecalc.app.di.SimpleViewModelFactory
import com.caloriecalc.app.di.rememberAppContainer
import com.caloriecalc.app.ui.components.ChartSeries
import com.caloriecalc.app.ui.components.FoodIconBadge
import com.caloriecalc.app.ui.components.LineChart
import com.caloriecalc.app.ui.components.SectionHeader
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightScreen() {
    val container = rememberAppContainer()
    val viewModel: WeightViewModel = viewModel(
        factory = SimpleViewModelFactory {
            WeightViewModel(container.weightRepository, container.profileRepository, container.nutritionLogRepository)
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    var weightText by remember { mutableStateOf("") }
    var editingLog by remember { mutableStateOf<WeightLog?>(null) }
    val measurements by container.bodyMeasurementRepository.observeAll().collectAsStateWithLifecycle(initialValue = emptyList())
    var showMeasurementDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val today = LocalDate.now().toEpochDay()

    val weightSeries = listOf(
        ChartSeries(
            points = state.weightLogs.map { it.epochDay.toFloat() to it.weightKg.toFloat() },
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
            label = "Scale (kg)"
        ),
        ChartSeries(
            points = state.smoothedPoints.map { it.epochDay.toFloat() to it.trendKg.toFloat() },
            color = MaterialTheme.colorScheme.primary,
            label = "Trend (kg)"
        )
    )
    val calorieSeries = listOf(
        ChartSeries(
            points = state.dailyCalories.entries.sortedBy { it.key }.map { it.key.toFloat() to it.value.toFloat() },
            color = MaterialTheme.colorScheme.tertiary,
            label = "Calories"
        )
    )

    Scaffold(
        topBar = { TopAppBar(title = { Text("Weight") }) }
    ) { padding ->
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
                        SectionHeader(
                            icon = Icons.Filled.MonitorWeight,
                            title = state.trend?.trendWeightKg?.let { "Trend: ${formatKg(it)} kg" }
                                ?: state.latestWeightKg?.let { "Latest: ${formatKg(it)} kg" }
                                ?: "No weight logged yet"
                        )
                        val noise = state.trend?.dailyNoiseKg
                        if (state.trend?.trendWeightKg != null) {
                            Text(
                                text = buildString {
                                    append(state.latestWeightKg?.let { "Scale today ${formatKg(it)} kg" } ?: "")
                                    if (noise != null) {
                                        if (isNotEmpty()) append(" · ")
                                        append("day-to-day swing ±${formatKg(noise)} kg is mostly water")
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = weightText,
                                onValueChange = { weightText = it },
                                label = { Text("Today's weight (kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    weightText.toDoubleOrNull()?.let {
                                        viewModel.logWeight(it)
                                        weightText = ""
                                    }
                                },
                                enabled = weightText.toDoubleOrNull() != null
                            ) {
                                Text("Log")
                            }
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(Icons.Filled.ShowChart, "Weight trend (28 days)")
                        Text(
                            "Faint line is what the scale said; solid line is the smoothed trend " +
                                "progress is judged by.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LineChart(series = weightSeries, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(Icons.Filled.LocalFireDepartment, "Calorie intake (28 days)")
                        Spacer(modifier = Modifier.height(8.dp))
                        LineChart(series = calorieSeries, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            item {
                val trend = state.trend
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(Icons.Filled.Insights, "Correlation & suggestions")
                        Spacer(modifier = Modifier.height(8.dp))
                        if (trend?.weeklyChangeKg != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (trend.weeklyChangeKg >= 0) Icons.Filled.TrendingUp else Icons.Filled.TrendingDown,
                                    contentDescription = null,
                                    tint = if (trend.weeklyChangeKg >= 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Trend: ${formatSigned(trend.weeklyChangeKg)} kg/week")
                            }
                            trend.estimatedMaintenanceCalories?.let {
                                Text("Estimated true maintenance: ~$it kcal/day")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Text(trend?.suggestion ?: "Log your weight to see trend insights.")
                        val adjustment = trend?.suggestedCalorieAdjustment
                        if (adjustment != null && adjustment != 0) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(onClick = { viewModel.applySuggestedAdjustment() }) {
                                Text("Apply ${if (adjustment > 0) "+" else ""}$adjustment kcal/day")
                            }
                        }
                    }
                }
            }

            item {
                val latestMeasurement = measurements.firstOrNull()
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(Icons.Filled.Straighten, "Body measurements")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            latestMeasurement?.let {
                                "Latest (${LocalDate.ofEpochDay(it.epochDay).format(DateTimeFormatter.ofPattern("MMM d"))}): " +
                                    measurementSummary(it)
                            } ?: "No measurements yet",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(onClick = { showMeasurementDialog = true }) {
                            Text("Add / edit today")
                        }
                    }
                }
            }

            item { SectionHeader(Icons.Filled.History, "History") }

            if (state.weightLogs.isEmpty()) {
                item { Text("No entries yet.", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }

            items(state.weightLogs.sortedByDescending { it.epochDay }, key = { it.epochDay }) { log ->
                Card(modifier = Modifier.fillMaxWidth(), onClick = { editingLog = log }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FoodIconBadge(
                                icon = Icons.Filled.MonitorWeight,
                                accentColor = MaterialTheme.colorScheme.primary,
                                size = 32.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(LocalDate.ofEpochDay(log.epochDay).format(DateTimeFormatter.ofPattern("MMM d, yyyy")))
                        }
                        Text("${log.weightKg} kg")
                    }
                }
            }
        }
    }

    val currentEditingLog = editingLog
    if (currentEditingLog != null) {
        var editText by remember(currentEditingLog) { mutableStateOf(currentEditingLog.weightKg.toString()) }
        AlertDialog(
            onDismissRequest = { editingLog = null },
            title = { Text(LocalDate.ofEpochDay(currentEditingLog.epochDay).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))) },
            text = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        label = { Text("Weight (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        viewModel.deleteWeightForDay(currentEditingLog.epochDay)
                        editingLog = null
                    }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete entry")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    editText.toDoubleOrNull()?.let { viewModel.logWeightForDay(currentEditingLog.epochDay, it) }
                    editingLog = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingLog = null }) { Text("Cancel") }
            }
        )
    }

    if (showMeasurementDialog) {
        var todayMeasurement by remember { mutableStateOf<BodyMeasurement?>(null) }
        var loaded by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            todayMeasurement = container.bodyMeasurementRepository.getForDay(today)
            loaded = true
        }

        if (loaded) {
            var waistText by remember { mutableStateOf(todayMeasurement?.waistCm?.toString() ?: "") }
            var chestText by remember { mutableStateOf(todayMeasurement?.chestCm?.toString() ?: "") }
            var armsText by remember { mutableStateOf(todayMeasurement?.armsCm?.toString() ?: "") }
            var thighsText by remember { mutableStateOf(todayMeasurement?.thighsCm?.toString() ?: "") }
            var hipsText by remember { mutableStateOf(todayMeasurement?.hipsCm?.toString() ?: "") }
            var neckText by remember { mutableStateOf(todayMeasurement?.neckCm?.toString() ?: "") }

            AlertDialog(
                onDismissRequest = { showMeasurementDialog = false },
                title = { Text("Today's measurements (cm)") },
                text = {
                    Column {
                        MeasurementField("Waist", waistText) { waistText = it }
                        MeasurementField("Chest", chestText) { chestText = it }
                        MeasurementField("Arms", armsText) { armsText = it }
                        MeasurementField("Thighs", thighsText) { thighsText = it }
                        MeasurementField("Hips", hipsText) { hipsText = it }
                        MeasurementField("Neck", neckText) { neckText = it }
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        scope.launch {
                            container.bodyMeasurementRepository.upsert(
                                BodyMeasurement(
                                    epochDay = today,
                                    waistCm = waistText.toDoubleOrNull(),
                                    chestCm = chestText.toDoubleOrNull(),
                                    armsCm = armsText.toDoubleOrNull(),
                                    thighsCm = thighsText.toDoubleOrNull(),
                                    hipsCm = hipsText.toDoubleOrNull(),
                                    neckCm = neckText.toDoubleOrNull()
                                )
                            )
                        }
                        showMeasurementDialog = false
                    }) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        scope.launch { container.bodyMeasurementRepository.deleteForDay(today) }
                        showMeasurementDialog = false
                    }) { Text("Delete") }
                }
            )
        }
    }
}

@Composable
private fun MeasurementField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(modifier = Modifier.height(4.dp))
}

private fun measurementSummary(m: BodyMeasurement): String {
    val parts = buildList {
        m.waistCm?.let { add("waist ${it}cm") }
        m.chestCm?.let { add("chest ${it}cm") }
        m.armsCm?.let { add("arms ${it}cm") }
        m.thighsCm?.let { add("thighs ${it}cm") }
        m.hipsCm?.let { add("hips ${it}cm") }
        m.neckCm?.let { add("neck ${it}cm") }
    }
    return if (parts.isEmpty()) "no values" else parts.joinToString(", ")
}

private fun formatSigned(value: Double): String {
    val rounded = (value * 100).roundToInt() / 100.0
    return if (rounded >= 0) "+$rounded" else "$rounded"
}

/** One decimal is all a bathroom scale meaningfully resolves. */
private fun formatKg(value: Double): String = ((value * 10).roundToInt() / 10.0).toString()
