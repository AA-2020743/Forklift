package com.caloriecalc.app.ui.foodlog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.caloriecalc.app.data.local.entity.Micronutrients

/** Editable per-100g micronutrient values, as text so partial input doesn't fight the user. */
data class MicronutrientDraft(
    val fiber: String = "",
    val sugar: String = "",
    val saturatedFat: String = "",
    val sodium: String = "",
    val potassium: String = "",
    val calcium: String = "",
    val iron: String = "",
    val vitaminC: String = "",
    val vitaminD: String = "",
    val vitaminB12: String = "",
    val magnesium: String = "",
    val zinc: String = ""
) {
    fun toMicronutrients(): Micronutrients = Micronutrients(
        fiberGrams = fiber.toDoubleOrNull(),
        sugarGrams = sugar.toDoubleOrNull(),
        saturatedFatGrams = saturatedFat.toDoubleOrNull(),
        sodiumMg = sodium.toDoubleOrNull(),
        potassiumMg = potassium.toDoubleOrNull(),
        calciumMg = calcium.toDoubleOrNull(),
        ironMg = iron.toDoubleOrNull(),
        vitaminCMg = vitaminC.toDoubleOrNull(),
        vitaminDMcg = vitaminD.toDoubleOrNull(),
        vitaminB12Mcg = vitaminB12.toDoubleOrNull(),
        magnesiumMg = magnesium.toDoubleOrNull(),
        zincMg = zinc.toDoubleOrNull()
    )

    fun isValid(): Boolean = toMicronutrients().allNonNegative()

    companion object {
        fun from(micros: Micronutrients): MicronutrientDraft = MicronutrientDraft(
            fiber = micros.fiberGrams.text(),
            sugar = micros.sugarGrams.text(),
            saturatedFat = micros.saturatedFatGrams.text(),
            sodium = micros.sodiumMg.text(),
            potassium = micros.potassiumMg.text(),
            calcium = micros.calciumMg.text(),
            iron = micros.ironMg.text(),
            vitaminC = micros.vitaminCMg.text(),
            vitaminD = micros.vitaminDMcg.text(),
            vitaminB12 = micros.vitaminB12Mcg.text(),
            magnesium = micros.magnesiumMg.text(),
            zinc = micros.zincMg.text()
        )

        private fun Double?.text(): String = this?.let { formatMacroValue(it) } ?: ""
    }
}

/**
 * Collapsed-by-default micronutrient entry. These are the values that decide whether the
 * Micronutrients screen shows anything at all, but they're far too many to put in front of
 * someone just trying to log a chicken breast — so the section stays folded away, pre-filled
 * from a reference profile where the food's name matched one.
 */
@Composable
fun MicronutrientFields(
    draft: MicronutrientDraft,
    onChange: (MicronutrientDraft) -> Unit,
    prefilled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    TextButton(onClick = { expanded = !expanded }, modifier = Modifier.fillMaxWidth()) {
        Text(if (expanded) "Hide micronutrients" else "Micronutrients (optional)")
        Icon(
            imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
            contentDescription = null
        )
    }

    if (!expanded) {
        if (prefilled) {
            Text(
                "Pre-filled from a reference profile for this food — expand to check or adjust.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Column {
        Text(
            "Per 100g. Anything left blank is treated as unknown, not zero. Added sugar is used " +
                "for the daily-limit warning, using the reported EU sugar value for now.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        MicroRow(
            leftLabel = "Fiber g", leftValue = draft.fiber, onLeftChange = { onChange(draft.copy(fiber = it)) },
            rightLabel = "Total sugar g", rightValue = draft.sugar, onRightChange = { onChange(draft.copy(sugar = it)) }
        )
        MicroRow(
            leftLabel = "Sat. fat g", leftValue = draft.saturatedFat, onLeftChange = { onChange(draft.copy(saturatedFat = it)) },
            rightLabel = "Sodium mg", rightValue = draft.sodium, onRightChange = { onChange(draft.copy(sodium = it)) }
        )
        MicroRow(
            leftLabel = "Potassium mg", leftValue = draft.potassium, onLeftChange = { onChange(draft.copy(potassium = it)) },
            rightLabel = "Calcium mg", rightValue = draft.calcium, onRightChange = { onChange(draft.copy(calcium = it)) }
        )
        MicroRow(
            leftLabel = "Iron mg", leftValue = draft.iron, onLeftChange = { onChange(draft.copy(iron = it)) },
            rightLabel = "Vitamin C mg", rightValue = draft.vitaminC, onRightChange = { onChange(draft.copy(vitaminC = it)) }
        )
        MicroRow(
            leftLabel = "Vitamin D mcg", leftValue = draft.vitaminD, onLeftChange = { onChange(draft.copy(vitaminD = it)) },
            rightLabel = "Vit. B12 mcg", rightValue = draft.vitaminB12, onRightChange = { onChange(draft.copy(vitaminB12 = it)) }
        )
        MicroRow(
            leftLabel = "Magnesium mg", leftValue = draft.magnesium, onLeftChange = { onChange(draft.copy(magnesium = it)) },
            rightLabel = "Zinc mg", rightValue = draft.zinc, onRightChange = { onChange(draft.copy(zinc = it)) }
        )
    }
}

@Composable
private fun MicroRow(
    leftLabel: String,
    leftValue: String,
    onLeftChange: (String) -> Unit,
    rightLabel: String,
    rightValue: String,
    onRightChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = leftValue,
            onValueChange = onLeftChange,
            label = { Text(leftLabel) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
        )
        OutlinedTextField(
            value = rightValue,
            onValueChange = onRightChange,
            label = { Text(rightLabel) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
}
