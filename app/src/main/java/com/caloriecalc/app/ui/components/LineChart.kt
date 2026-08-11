package com.caloriecalc.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

data class ChartSeries(
    val points: List<Pair<Float, Float>>,
    val color: Color,
    val label: String
)

/** Minimal multi-series line chart. x/y values are used as-is (already in the domain you want to plot). */
@Composable
fun LineChart(
    series: List<ChartSeries>,
    modifier: Modifier = Modifier
) {
    val hasData = series.any { it.points.size >= 2 }
    if (!hasData) {
        Box(
            modifier = modifier.fillMaxWidth().height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Not enough data yet", style = MaterialTheme.typography.bodyMedium)
        }
        return
    }

    val allPoints = series.flatMap { it.points }
    val minX = allPoints.minOf { it.first }
    val maxX = allPoints.maxOf { it.first }
    val minY = allPoints.minOf { it.second }
    val maxY = allPoints.maxOf { it.second }
    val xRange = (maxX - minX).takeIf { it != 0f } ?: 1f
    val yRange = (maxY - minY).takeIf { it != 0f } ?: 1f

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val strokePx = 3.dp.toPx()
        val dotRadiusPx = 3.dp.toPx()

        series.forEach { s ->
            if (s.points.size < 2) return@forEach
            val path = Path()
            s.points.forEachIndexed { index, (x, y) ->
                val px = ((x - minX) / xRange) * size.width
                val py = size.height - ((y - minY) / yRange) * size.height
                if (index == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            drawPath(
                path = path,
                color = s.color,
                style = Stroke(width = strokePx, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            s.points.forEach { (x, y) ->
                val px = ((x - minX) / xRange) * size.width
                val py = size.height - ((y - minY) / yRange) * size.height
                drawCircle(color = s.color, radius = dotRadiusPx, center = Offset(px, py))
            }
        }
    }
}

@Composable
fun ChartLegend(series: List<ChartSeries>, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        series.forEach { s ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color = s.color, shape = CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = s.label, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
