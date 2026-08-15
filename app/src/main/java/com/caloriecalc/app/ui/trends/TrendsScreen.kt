package com.caloriecalc.app.ui.trends

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.caloriecalc.app.di.SimpleViewModelFactory
import com.caloriecalc.app.di.rememberAppContainer
import com.caloriecalc.app.ui.components.ChartSeries
import com.caloriecalc.app.ui.components.LineChart
import com.caloriecalc.app.ui.components.SectionHeader
import com.caloriecalc.app.ui.components.formatGrams
import com.caloriecalc.app.ui.theme.StatusApproaching
import com.caloriecalc.app.ui.theme.StatusBelowThreshold
import com.caloriecalc.app.ui.theme.StatusOnTarget
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendsScreen(onBack: () -> Unit) {
    val container = rememberAppContainer()
    val viewModel: TrendsViewModel = viewModel(
        factory = SimpleViewModelFactory {
            TrendsViewModel(
                container.profileRepository,
                container.nutritionLogRepository,
                container.waterRepository
            )
        }
    )
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trends") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(TrendWindow.entries.toList()) { window ->
                        FilterChip(
                            selected = state.window == window,
                            onClick = { viewModel.selectWindow(window) },
                            label = { Text(window.label) }
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(Icons.Filled.CalendarMonth, "Daily average")
                        Text(
                            text = "${state.daysLogged} of ${state.window.days} days have food logged. " +
                                "Averages are over the whole window, so untracked days count as zero.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        AverageRow(
                            icon = Icons.Filled.LocalFireDepartment,
                            label = "Calories",
                            value = "${state.avgCalories} kcal",
                            target = "target ${state.calorieTarget}",
                            accent = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        AverageRow(
                            icon = Icons.Filled.SetMeal,
                            label = "Protein",
                            value = "${formatGrams(state.avgProtein)} g",
                            target = "min ${formatGrams(state.proteinTargetMin)} g",
                            accent = MaterialTheme.colorScheme.tertiary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        AverageRow(
                            icon = Icons.Filled.WaterDrop,
                            label = "Fat",
                            value = "${formatGrams(state.avgFat)} g",
                            target = null,
                            accent = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        AverageRow(
                            icon = Icons.Filled.RiceBowl,
                            label = "Carbs",
                            value = "${formatGrams(state.avgCarbs)} g",
                            target = null,
                            accent = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        AverageRow(
                            icon = Icons.Filled.WaterDrop,
                            label = "Water",
                            value = "${state.avgWaterMl} ml",
                            target = "target ${state.waterTargetMl} ml",
                            accent = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        SectionHeader(Icons.Filled.BarChart, "Calories per day")
                        Spacer(modifier = Modifier.height(12.dp))
                        CalorieBars(
                            days = state.days,
                            target = state.calorieTarget
                        )
                    }
                }
            }

            if (state.days.size >= 2) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            SectionHeader(Icons.Filled.SetMeal, "Protein per day")
                            Spacer(modifier = Modifier.height(12.dp))
                            LineChart(
                                series = listOf(
                                    ChartSeries(
                                        points = state.days.map { it.epochDay.toFloat() to it.protein.toFloat() },
                                        color = MaterialTheme.colorScheme.tertiary,
                                        label = "Protein (g)"
                                    )
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AverageRow(
    icon: ImageVector,
    label: String,
    value: String,
    target: String?,
    accent: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accent.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(label, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(value, fontWeight = FontWeight.Medium)
            if (target != null) {
                Text(
                    text = target,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Compact per-day calorie bars. Each bar is colored against the calorie target so an
 * over/under pattern is visible without reading any numbers. */
@Composable
private fun CalorieBars(days: List<TrendDay>, target: Int) {
    if (days.isEmpty()) {
        Text(
            "Nothing logged in this window yet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }
    val maxValue = maxOf(days.maxOf { it.calories }, target.toDouble(), 1.0)
    val dayFormatter = DateTimeFormatter.ofPattern("EEEEE")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        days.forEach { day ->
            val fraction = (day.calories / maxValue).toFloat().coerceIn(0f, 1f)
            val barColor = when {
                day.calories <= 0.0 -> MaterialTheme.colorScheme.surfaceVariant
                day.calories >= target * 0.9 && day.calories <= target * 1.1 -> StatusOnTarget
                day.calories < target * 0.9 -> StatusApproaching
                else -> StatusBelowThreshold
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((110 * fraction).dp.coerceAtLeast(3.dp))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(barColor)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = LocalDate.ofEpochDay(day.epochDay).format(dayFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
