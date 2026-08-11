package com.caloriecalc.app.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.caloriecalc.app.di.SimpleViewModelFactory
import com.caloriecalc.app.di.rememberAppContainer
import com.caloriecalc.app.domain.MuscleGroup

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExercisePickerScreen(
    sessionId: Long,
    onBack: () -> Unit,
    onExerciseSelected: (sessionId: Long, exerciseId: Long) -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: ExercisePickerViewModel = viewModel(
        factory = SimpleViewModelFactory { ExercisePickerViewModel(container.workoutRepository) }
    )
    val exercises by viewModel.exercises.collectAsStateWithLifecycle()

    var query by remember { mutableStateOf("") }
    var selectedGroup by remember { mutableStateOf<MuscleGroup?>(null) }

    val filtered = exercises.filter { exercise ->
        (selectedGroup == null || exercise.primaryMuscleGroup == selectedGroup) &&
            (query.isBlank() || exercise.name.contains(query, ignoreCase = true))
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Choose exercise") },
                    navigationIcon = {
                        IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                    }
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search exercises") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedGroup == null,
                            onClick = { selectedGroup = null },
                            label = { Text("All") }
                        )
                    }
                    items(MuscleGroup.entries.toList()) { group ->
                        FilterChip(
                            selected = selectedGroup == group,
                            onClick = { selectedGroup = if (selectedGroup == group) null else group },
                            label = { Text(group.displayName) }
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered, key = { it.id }) { exercise ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onExerciseSelected(sessionId, exercise.id) }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(exercise.name, style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = exercise.targetSubGroups.joinToString { it.displayName },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
