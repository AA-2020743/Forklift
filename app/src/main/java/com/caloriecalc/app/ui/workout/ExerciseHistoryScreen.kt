package com.caloriecalc.app.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.caloriecalc.app.di.SimpleViewModelFactory
import com.caloriecalc.app.di.rememberAppContainer
import com.caloriecalc.app.ui.components.ChartSeries
import com.caloriecalc.app.ui.components.LineChart
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExerciseHistoryScreen(
    exerciseId: Long,
    onBack: () -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: ExerciseHistoryViewModel = viewModel(
        factory = SimpleViewModelFactory { ExerciseHistoryViewModel(exerciseId, container.workoutRepository) }
    )
    val exercise by viewModel.exercise.collectAsStateWithLifecycle()
    val history by viewModel.history.collectAsStateWithLifecycle()

    val bestPerDay = history
        .groupBy { it.epochDay }
        .map { (day, sets) -> day to (sets.maxOfOrNull { it.weightKg } ?: 0.0) }
        .sortedBy { it.first }

    val series = listOf(
        ChartSeries(
            points = bestPerDay.map { (day, weight) -> day.toFloat() to weight.toFloat() },
            color = MaterialTheme.colorScheme.primary,
            label = "Best set (kg)"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise?.name ?: "History") },
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
        ) {
            Text("Heaviest set per session", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LineChart(series = series, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(24.dp))
            Text("All sets", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            val formatter = DateTimeFormatter.ofPattern("MMM d")
            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(history.reversed(), key = { it.id }) { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(LocalDate.ofEpochDay(entry.epochDay).format(formatter))
                        Text("${entry.weightKg}kg x ${entry.reps}")
                    }
                }
            }
        }
    }
}
