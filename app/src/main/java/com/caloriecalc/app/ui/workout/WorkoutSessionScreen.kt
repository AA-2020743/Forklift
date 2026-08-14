package com.caloriecalc.app.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.caloriecalc.app.di.SimpleViewModelFactory
import com.caloriecalc.app.di.rememberAppContainer
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScreen(
    sessionId: Long,
    onBack: () -> Unit,
    onAddExercise: (Long) -> Unit,
    onExerciseClick: (sessionId: Long, exerciseId: Long) -> Unit,
    onExerciseHistory: (Long) -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: WorkoutSessionViewModel = viewModel(
        factory = SimpleViewModelFactory {
            WorkoutSessionViewModel(sessionId, container.workoutRepository, container.workoutTemplateRepository)
        }
    )
    val session by viewModel.session.collectAsStateWithLifecycle()
    val groups by viewModel.groupedSets.collectAsStateWithLifecycle()
    val planned by viewModel.plannedExercises.collectAsStateWithLifecycle()

    val title = session?.let {
        LocalDate.ofEpochDay(it.epochDay).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
    } ?: "Workout"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onAddExercise(sessionId) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add exercise")
            }
        }
    ) { padding ->
        if (groups.isEmpty() && planned.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("No exercises logged yet. Tap + to add one.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (planned.isNotEmpty()) {
                    item { Text("Planned", style = MaterialTheme.typography.titleMedium) }
                    items(planned, key = { "planned_${it.id}" }) { exercise ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(exercise.name)
                                Button(onClick = { onExerciseClick(sessionId, exercise.id) }) {
                                    Text("Log")
                                }
                            }
                        }
                    }
                }

                if (groups.isNotEmpty()) {
                    item { Text("Logged", style = MaterialTheme.typography.titleMedium) }
                }
                items(groups, key = { it.exercise.id }) { group ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onExerciseClick(sessionId, group.exercise.id) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(group.exercise.name, fontWeight = FontWeight.Medium)
                                IconButton(onClick = { onExerciseHistory(group.exercise.id) }) {
                                    Icon(Icons.Filled.History, contentDescription = "View history")
                                }
                            }
                            group.sets.forEach { set ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Set ${set.setNumber}: ${set.weightKg}kg x ${set.reps}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    IconButton(onClick = { viewModel.deleteSet(set) }) {
                                        Icon(Icons.Filled.Delete, contentDescription = "Remove set")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
