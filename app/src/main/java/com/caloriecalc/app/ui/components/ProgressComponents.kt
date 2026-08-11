package com.caloriecalc.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caloriecalc.app.domain.MacroProgress
import com.caloriecalc.app.domain.MacroStatus
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun MacroProgressRow(
    label: String,
    progress: MacroProgress,
    modifier: Modifier = Modifier,
    unit: String = "g"
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                text = "${progress.consumedGrams.roundToInt()} / ${progress.targetGrams.roundToInt()} $unit",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress.fractionOfTarget.coerceIn(0f, 1f),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = progress.status.toColor(),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        val hint = when (progress.status) {
            MacroStatus.BELOW_THRESHOLD -> "Below recommended threshold"
            MacroStatus.OVER_TARGET -> "Over target"
            else -> null
        }
        if (hint != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = hint, color = progress.status.toColor(), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun CalorieRing(
    consumed: Int,
    target: Int,
    modifier: Modifier = Modifier
) {
    val fraction = if (target <= 0) 0f else (consumed.toFloat() / target).coerceIn(0f, 1f)
    val remaining = target - consumed
    Box(modifier = modifier.size(160.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = fraction,
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 14.dp,
            color = if (remaining < 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "${abs(remaining)}", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = if (remaining >= 0) "kcal left" else "kcal over",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
