package com.caloriecalc.app.ui.workout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplatesScreen(
    onBack: () -> Unit,
    onTemplateClick: (Long) -> Unit
) {
    val container = rememberAppContainer()
    val viewModel: TemplatesViewModel = viewModel(
        factory = SimpleViewModelFactory { TemplatesViewModel(container.workoutTemplateRepository) }
    )
    val templates by viewModel.templates.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Templates") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "New template")
            }
        }
    ) { padding ->
        if (templates.isEmpty()) {
            Column(modifier = Modifier.fillMaxWidth().padding(padding).padding(32.dp)) {
                Text("No templates yet. Save a routine like \"Push Day\" to reuse it every session.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(templates, key = { it.id }) { template ->
                    Card(modifier = Modifier.fillMaxWidth(), onClick = { onTemplateClick(template.id) }) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(template.name, style = MaterialTheme.typography.bodyLarge)
                            IconButton(onClick = { viewModel.deleteTemplate(template) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete template")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New template") },
            text = {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name (e.g. \"Push Day\")") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val trimmed = name.trim()
                        if (trimmed.isNotEmpty()) {
                            viewModel.createTemplate(trimmed) { id -> onTemplateClick(id) }
                        }
                        showCreateDialog = false
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }
}
