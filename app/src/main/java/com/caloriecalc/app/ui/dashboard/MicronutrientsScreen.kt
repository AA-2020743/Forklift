package com.caloriecalc.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.caloriecalc.app.di.SimpleViewModelFactory
import com.caloriecalc.app.di.rememberAppContainer
import com.caloriecalc.app.ui.components.nutrientIcon
import com.caloriecalc.app.ui.theme.StatusApproaching
import com.caloriecalc.app.ui.theme.StatusBelowThreshold
import com.caloriecalc.app.ui.theme.StatusOnTarget
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicronutrientsScreen(onBack: () -> Unit) {
    val container = rememberAppContainer()
    val viewModel: MicronutrientsViewModel = viewModel(
        factory = SimpleViewModelFactory {
            MicronutrientsViewModel(container.profileRepository, container.nutritionLogRepository)
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Micronutrients") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                Text(
                    "Generic adult reference values, not personalized medical advice.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(state.rows, key = { it.target.label }) { row ->
                val fraction = if (row.target.dailyTarget <= 0) 0f
                else (row.consumed / row.target.dailyTarget).toFloat().coerceIn(0f, 1f)
                val overLimit = row.target.isUpperLimit && row.consumed > row.target.dailyTarget
                val color = when {
                    overLimit -> StatusBelowThreshold
                    row.target.isUpperLimit -> StatusOnTarget
                    row.consumed >= row.target.dailyTarget -> StatusOnTarget
                    fraction >= 0.5f -> StatusApproaching
                    else -> StatusBelowThreshold
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .background(color.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = nutrientIcon(row.target.label),
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(row.target.label, fontWeight = FontWeight.Medium)
                            Text(
                                "${formatAmount(row.consumed)} / ${formatAmount(row.target.dailyTarget)} ${row.target.unit}" +
                                    if (row.target.isUpperLimit) " max" else ""
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = fraction,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = color,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun formatAmount(value: Double): String =
    if (value < 10) ((value * 10).roundToInt() / 10.0).toString() else value.roundToInt().toString()
