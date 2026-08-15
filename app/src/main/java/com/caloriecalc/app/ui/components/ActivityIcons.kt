package com.caloriecalc.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Hiking
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pool
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SportsMma
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.ui.graphics.vector.ImageVector
import com.caloriecalc.app.domain.ActivityType

fun activityTypeIcon(type: ActivityType): ImageVector = when (type) {
    ActivityType.WALKING -> Icons.Filled.DirectionsWalk
    ActivityType.RUNNING -> Icons.Filled.DirectionsRun
    ActivityType.HIKING -> Icons.Filled.Hiking
    ActivityType.CYCLING -> Icons.Filled.DirectionsBike
    ActivityType.SWIMMING -> Icons.Filled.Pool
    ActivityType.BOXING -> Icons.Filled.SportsMma
    ActivityType.SPORTS -> Icons.Filled.SportsSoccer
    ActivityType.YOGA -> Icons.Filled.SelfImprovement
    ActivityType.HIIT -> Icons.Filled.LocalFireDepartment
    ActivityType.OTHER -> Icons.Filled.FitnessCenter
}
