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
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.caloriecalc.app.di.SimpleViewModelFactory
import com.caloriecalc.app.di.rememberAppContainer
import com.caloriecalc.app.ui.components.FoodIconBadge
import com.caloriecalc.app.ui.components.SectionHeader
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutListScreen(
    onSessionClick: (Long) -> Unit,
    onManageTemplates: () -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: WorkoutListViewModel = viewModel(
        factory = SimpleViewModelFactory { WorkoutListViewModel(container.workoutRepository, container.workoutTemplateRepository) }
    )
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    val coverage by viewModel.coverageReport.collectAsStateWithLifecycle()
    val dateFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    var showStartDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lifting") },
                actions = {
                    IconButton(onClick = onManageTemplates) {
                        Icon(Icons.Filled.ListAlt, contentDescription = "Templates")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showStartDialog = true }) {
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
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SectionHeader(
                                icon = Icons.Filled.Info,
                                title = "A few gaps, last 14 days",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            report.suggestions.forEach { suggestion ->
                                Text(
                                    text = "${suggestion.subGroup.displayName} — ${suggestion.exercises.joinToString { it.name }}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            }

            item { SectionHeader(Icons.Filled.History, "History") }

            if (sessions.isEmpty()) {
                item { Text("No workouts logged yet. Tap + to start one.") }
            }

            items(sessions, key = { it.id }) { session ->
                Card(modifier = Modifier.fillMaxWidth(), onClick = { onSessionClick(session.id) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            FoodIconBadge(
                                icon = Icons.Filled.FitnessCenter,
                                accentColor = MaterialTheme.colorScheme.primary,
                                size = 32.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = LocalDate.ofEpochDay(session.epochDay).format(dateFormatter),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Text(session.name ?: "Workout", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }

    if (showStartDialog) {
        AlertDialog(
            onDismissRequest = { showStartDialog = false },
            title = { Text("Start workout") },
            text = {
                Column {
                    TextButton(onClick = {
                        showStartDialog = false
                        viewModel.startSession(onCreated = onSessionClick)
                    }) { Text("Blank workout") }
                    if (templates.isNotEmpty()) {
                        Text(
                            "From a template",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        templates.forEach { template ->
                            TextButton(onClick = {
                                showStartDialog = false
                                viewModel.startSession(templateId = template.id, onCreated = onSessionClick)
                            }) { Text(template.name) }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStartDialog = false }) { Text("Cancel") }
            }
        )
    }
}
