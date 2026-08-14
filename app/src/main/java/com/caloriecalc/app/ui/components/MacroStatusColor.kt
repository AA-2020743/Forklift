package com.caloriecalc.app.ui.components

import androidx.compose.ui.graphics.Color
import com.caloriecalc.app.domain.MacroStatus
import com.caloriecalc.app.ui.theme.StatusApproaching
import com.caloriecalc.app.ui.theme.StatusBelowThreshold
import com.caloriecalc.app.ui.theme.StatusOnTarget

fun MacroStatus.toColor(): Color = when (this) {
    MacroStatus.BELOW_RANGE -> StatusBelowThreshold
    MacroStatus.IN_RANGE -> StatusOnTarget
    MacroStatus.ABOVE_RANGE -> StatusApproaching
}
