package com.caloriecalc.app.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
fun WorkoutListScreen(
    onSessionClick: (Long) -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: WorkoutListViewModel = viewModel(
        factory = SimpleViewModelFactory { WorkoutListViewModel(container.workoutRepository) }
    )
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val coverage by viewModel.coverageReport.collectAsStateWithLifecycle()
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    Scaffold(
        topBar = { TopAppBar(title = { Text("Lifting") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.startSession(onSessionClick) }) {
                Icon(Icons.Filled.Add, contentDescription = "Start workout")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val report = coverage
            if (report != null && report.suggestions.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Undertrained this week", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))
                            report.suggestions.forEach { suggestion ->
                                Text(
                                    text = "${suggestion.subGroup.displayName}: try ${suggestion.exercises.joinToString { it.name }}",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }

            item { Text("History", style = MaterialTheme.typography.titleMedium) }

            if (sessions.isEmpty()) {
                item { Text("No workouts logged yet. Tap + to start one.") }
            }

            items(sessions, key = { it.id }) { session ->
                Card(modifier = Modifier.fillMaxWidth(), onClick = { onSessionClick(session.id) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = LocalDate.ofEpochDay(session.epochDay).format(dateFormatter),
                            fontWeight = FontWeight.Medium
                        )
                        Text(session.name ?: "Workout", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
