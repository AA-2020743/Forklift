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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.caloriecalc.app.di.SimpleViewModelFactory
import com.caloriecalc.app.di.rememberAppContainer
import com.caloriecalc.app.ui.components.ChartSeries
import com.caloriecalc.app.ui.components.LineChart
import kotlin.math.roundToInt

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

    val weightSeries = listOf(
        ChartSeries(
            points = state.weightLogs.map { it.epochDay.toFloat() to it.weightKg.toFloat() },
            color = MaterialTheme.colorScheme.primary,
            label = "Weight (kg)"
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
                        Text(
                            text = state.latestWeightKg?.let { "Latest: ${it} kg" } ?: "No weight logged yet",
                            style = MaterialTheme.typography.titleMedium
                        )
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
                        Text("Weight trend (28 days)", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        LineChart(series = weightSeries, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Calorie intake (28 days)", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        LineChart(series = calorieSeries, modifier = Modifier.fillMaxWidth())
                    }
                }
            }

            item {
                val trend = state.trend
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Correlation & suggestions", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        if (trend?.weeklyChangeKg != null) {
                            Text("Trend: ${formatSigned(trend.weeklyChangeKg)} kg/week")
                            trend.estimatedMaintenanceCalories?.let {
                                Text("Estimated true maintenance: ~$it kcal/day")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        Text(trend?.suggestion ?: "Log your weight to see trend insights.")
                    }
                }
            }
        }
    }
}

private fun formatSigned(value: Double): String {
    val rounded = (value * 100).roundToInt() / 100.0
    return if (rounded >= 0) "+$rounded" else "$rounded"
}
