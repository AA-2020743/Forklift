package com.caloriecalc.app.ui.components

import androidx.compose.ui.graphics.Color
import com.caloriecalc.app.domain.MacroStatus
import com.caloriecalc.app.ui.theme.StatusApproaching
import com.caloriecalc.app.ui.theme.StatusBelowThreshold
import com.caloriecalc.app.ui.theme.StatusOnTarget
import com.caloriecalc.app.ui.theme.StatusOverTarget

fun MacroStatus.toColor(): Color = when (this) {
    MacroStatus.BELOW_THRESHOLD -> StatusBelowThreshold
    MacroStatus.APPROACHING -> StatusApproaching
    MacroStatus.ON_TARGET -> StatusOnTarget
    MacroStatus.OVER_TARGET -> StatusOverTarget
}
