package com.caloriecalc.app.ui.workout

import android.os.VibrationEffect
import android.os.Vibrator
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.caloriecalc.app.di.SimpleViewModelFactory
import com.caloriecalc.app.di.rememberAppContainer
import java.util.Locale
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogSetScreen(
    sessionId: Long,
    exerciseId: Long,
    onBack: () -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: LogSetViewModel = viewModel(
        factory = SimpleViewModelFactory { LogSetViewModel(sessionId, exerciseId, container.workoutRepository) }
    )
    val exercise by viewModel.exercise.collectAsStateWithLifecycle()
    val sets by viewModel.sets.collectAsStateWithLifecycle()
    val lastTimeSets by viewModel.lastTimeSets.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var weightText by remember { mutableStateOf("") }
    var repsText by remember { mutableStateOf("") }
    var restDurationSeconds by remember { mutableStateOf(90) }
    var restRemaining by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(restRemaining) {
        val remaining = restRemaining
        if (remaining != null && remaining > 0) {
            delay(1000)
            restRemaining = remaining - 1
        } else if (remaining == 0) {
            val vibrator = ContextCompat.getSystemService(context, Vibrator::class.java)
            vibrator?.vibrate(VibrationEffect.createOneShot(300, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(exercise?.name ?: "Exercise") },
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
            if (lastTimeSets.isNotEmpty()) {
                Text(
                    "Last time: ${lastTimeSets.joinToString { "${it.weightKg}kg x ${it.reps}" }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            val currentRest = restRemaining
            if (currentRest != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Rest: ${formatRest(currentRest)}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Row {
                            TextButton(onClick = { restRemaining = currentRest + 30 }) { Text("+30s") }
                            TextButton(onClick = { restRemaining = null }) { Text("Skip") }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(60, 90, 120).forEach { seconds ->
                        FilterChip(
                            selected = restDurationSeconds == seconds,
                            onClick = { restDurationSeconds = seconds },
                            label = { Text("${seconds}s rest") }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Weight (kg)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = repsText,
                    onValueChange = { repsText = it },
                    label = { Text("Reps") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    val weight = weightText.toDoubleOrNull()
                    val reps = repsText.toIntOrNull()
                    if (weight != null && reps != null) {
                        viewModel.addSet(weight, reps)
                        restRemaining = restDurationSeconds
                        // Deliberately keep both fields as-is: most sets in a working set
                        // repeat the same weight/reps, so tapping "Add set" again should
                        // just work without retyping anything.
                    }
                },
                enabled = weightText.toDoubleOrNull() != null && repsText.toIntOrNull() != null,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Add set")
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Logged sets", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sets, key = { it.id }) { set ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Set ${set.setNumber}: ${set.weightKg}kg x ${set.reps} reps")
                            Row {
                                IconButton(onClick = { viewModel.addSet(set.weightKg, set.reps) }) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = "Duplicate this set")
                                }
                                IconButton(onClick = { viewModel.deleteSet(set) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = "Remove")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatRest(seconds: Int): String = String.format(Locale.US, "%d:%02d", seconds / 60, seconds % 60)
