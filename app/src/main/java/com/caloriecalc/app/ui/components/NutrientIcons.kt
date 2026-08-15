package com.caloriecalc.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.ui.graphics.vector.ImageVector

/** A loosely evocative icon per micronutrient label (iron -> strength, zinc -> immune shield,
 * vitamins -> a pill), purely decorative — helps a long reference list read at a glance. */
fun nutrientIcon(label: String): ImageVector {
    val n = label.lowercase()
    return when {
        "fiber" in n -> Icons.Filled.Grass
        "sugar" in n -> Icons.Filled.Cookie
        "saturated fat" in n -> Icons.Filled.WaterDrop
        "sodium" in n -> Icons.Filled.Grain
        "potassium" in n -> Icons.Filled.Spa
        "calcium" in n -> Icons.Filled.LocalDrink
        "iron" in n -> Icons.Filled.FitnessCenter
        "magnesium" in n -> Icons.Filled.Bolt
        "zinc" in n -> Icons.Filled.Shield
        else -> Icons.Filled.Medication
    }
}
