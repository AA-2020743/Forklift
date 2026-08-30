package com.caloriecalc.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.caloriecalc.app.ui.components.FoodIconBadge
import com.caloriecalc.app.ui.components.foodItemIcon
import com.caloriecalc.app.ui.components.formatGrams
import com.caloriecalc.app.ui.components.stableAccentColor
import kotlin.math.roundToInt

/** One food's contribution to a single macro on the selected day. */
data class MacroContribution(
    val foodId: Long,
    val foodName: String,
    val mealName: String,
    val grams: Double,
    val macroGrams: Double,
    val loggedAsServing: Boolean,
    val servingSizeGrams: Double?,
    val servingName: String?
)

/**
 * Answers "where did today's protein actually come from?".
 *
 * A single number on the dashboard tells you whether you hit the target but nothing about how,
 * which is the part you can act on — one big source and several trivial ones is a very different
 * day from four even contributors. The sheet leads with a proportional bar (each segment sized
 * and coloured by its food, so the shape of the day is readable before any numbers are), then
 * the same foods ranked underneath with their share.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroBreakdownSheet(
    macroLabel: String,
    macroIcon: ImageVector,
    accent: Color,
    contributions: List<MacroContribution>,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val total = contributions.sumOf { it.macroGrams }
    val ranked = contributions.sortedByDescending { it.macroGrams }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(macroIcon, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "$macroLabel breakdown",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${formatGrams(total)} g from ${ranked.size} food${if (ranked.size == 1) "" else "s"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (ranked.isEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    "Nothing logged for this day yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                return@Column
            }

            Spacer(modifier = Modifier.height(16.dp))
            // Proportional bar: one segment per food, in the same order as the list below and
            // tinted with that food's colour, so bar and list read as the same information.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                ranked.forEach { item ->
                    val share = if (total <= 0.0) 0f else (item.macroGrams / total).toFloat()
                    if (share > 0f) {
                        Box(
                            modifier = Modifier
                                .weight(share)
                                .fillMaxHeight()
                                .background(stableAccentColor(item.foodName))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(
                modifier = Modifier.heightIn(max = 380.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(ranked, key = { "${it.foodId}_${it.loggedAsServing}_${it.servingSizeGrams}" }) { item ->
                    val share = if (total <= 0.0) 0 else ((item.macroGrams / total) * 100).roundToInt()
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        FoodIconBadge(
                            icon = foodItemIcon(item.foodName),
                            accentColor = stableAccentColor(item.foodName),
                            size = 34.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.foodName,
                                fontWeight = FontWeight.Medium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = buildString {
                                    append(item.mealName)
                                    append(" · ")
                                    if (item.loggedAsServing && item.servingSizeGrams != null) {
                                        append(formatGrams(item.grams / item.servingSizeGrams))
                                        append(" serving")
                                        if (item.grams / item.servingSizeGrams != 1.0) append("s")
                                    } else {
                                        append(formatGrams(item.grams))
                                        append(" g")
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("${formatGrams(item.macroGrams)} g", fontWeight = FontWeight.Medium)
                            Text(
                                text = "$share%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
