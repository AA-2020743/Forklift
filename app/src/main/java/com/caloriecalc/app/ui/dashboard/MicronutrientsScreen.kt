package com.caloriecalc.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MicronutrientsScreen(epochDay: Long, onBack: () -> Unit) {
    val container = rememberAppContainer()
    val viewModel: MicronutrientsViewModel = viewModel(
        factory = SimpleViewModelFactory {
            MicronutrientsViewModel(epochDay, container.profileRepository, container.nutritionLogRepository)
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (epochDay == LocalDate.now().toEpochDay()) "Micronutrients"
                        else "Micronutrients · ${LocalDate.ofEpochDay(epochDay).format(DateTimeFormatter.ofPattern("MMM d"))}"
                    )
                },
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
                    "Sugar and saturated fat limits use 10% of your daily calorie target. " +
                        "For EU-style data, the reported sugar label is used as the combined sugar " +
                        "value. Not medical advice.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Without this, a day of foods that simply have no micronutrient data reads as a
            // wall of 0.0 and looks broken. Name the gap and say what fixes it.
            if (state.foodsLogged > 0 && state.foodsMissingData.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "${state.foodsWithData} of ${state.foodsLogged} logged items " +
                                        "have micronutrient data",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                val named = state.foodsMissingData.take(3).joinToString(", ")
                                val extra = state.foodsMissingData.size - 3
                                val andMore = if (extra > 0) " and $extra more" else ""
                                Text(
                                    text = "No data for $named$andMore. Open the food and add its " +
                                        "values to include them here.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                }
            } else if (state.foodsLogged == 0) {
                item {
                    Text(
                        "Nothing logged ${if (epochDay == LocalDate.now().toEpochDay()) "today" else "on this day"} yet — these fill in as you log food.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            items(state.rows, key = { it.target.label }) { row ->
                val dailyTarget = row.target.dailyTarget
                val fraction = if (dailyTarget <= 0) 0f
                else (row.consumed / dailyTarget).toFloat().coerceIn(0f, 1f)
                val overLimit = row.target.isUpperLimit && row.consumed > dailyTarget
                val color = when {
                    overLimit -> StatusBelowThreshold
                    row.target.isUpperLimit -> StatusOnTarget
                    row.consumed >= dailyTarget -> StatusOnTarget
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
                                "${formatAmount(row.consumed)} / ${formatAmount(dailyTarget)} ${row.target.unit}" +
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
