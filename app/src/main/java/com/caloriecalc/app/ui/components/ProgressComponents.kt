package com.caloriecalc.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.caloriecalc.app.domain.MacroProgress
import com.caloriecalc.app.domain.MacroStatus
import kotlin.math.abs

/**
 * Shows consumed grams against a healthy min-max range (not a single target to hit): a shaded
 * band marks the range, and the filled bar shows how much of it has been eaten so far.
 */
@Composable
fun MacroRangeBar(
    label: String,
    progress: MacroProgress,
    modifier: Modifier = Modifier,
    unit: String = "g",
    icon: ImageVector? = null
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
            Text(
                text = "${formatGrams(progress.consumedGrams)} $unit " +
                    "(${formatGrams(progress.minGrams)}-${formatGrams(progress.maxGrams)} $unit)",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(6.dp))

        val trackColor = MaterialTheme.colorScheme.surfaceVariant
        val rangeBandColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
        val fillColor = progress.status.toColor()

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
        ) {
            val displayMax = maxOf(progress.maxGrams, progress.consumedGrams, 1.0) * 1.1
            val barRadius = CornerRadius(size.height / 2)

            drawRoundRect(color = trackColor, cornerRadius = barRadius)

            val bandStartX = (progress.minGrams / displayMax).toFloat() * size.width
            val bandEndX = (progress.maxGrams / displayMax).toFloat() * size.width
            drawRoundRect(
                color = rangeBandColor,
                topLeft = Offset(bandStartX, 0f),
                size = Size((bandEndX - bandStartX).coerceAtLeast(0f), size.height)
            )

            val fillWidth = (progress.consumedGrams / displayMax).toFloat().coerceIn(0f, 1f) * size.width
            drawRoundRect(
                color = fillColor,
                size = Size(fillWidth, size.height),
                cornerRadius = barRadius
            )
        }

        val hint = when (progress.status) {
            MacroStatus.BELOW_RANGE -> "Below healthy range"
            MacroStatus.ABOVE_RANGE -> "Above range"
            MacroStatus.IN_RANGE -> null
        }
        if (hint != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = hint, color = fillColor, style = MaterialTheme.typography.labelSmall)
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
            Icon(
                imageVector = Icons.Filled.LocalFireDepartment,
                contentDescription = null,
                tint = if (remaining < 0) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Text(text = "${abs(remaining)}", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = if (remaining >= 0) "kcal left" else "kcal over",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}
