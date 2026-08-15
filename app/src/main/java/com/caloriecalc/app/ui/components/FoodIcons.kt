package com.caloriecalc.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Blender
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BrunchDining
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.DinnerDining
import androidx.compose.material.icons.filled.Egg
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.FreeBreakfast
import androidx.compose.material.icons.filled.Icecream
import androidx.compose.material.icons.filled.LocalBar
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LocalPizza
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.RamenDining
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.RiceBowl
import androidx.compose.material.icons.filled.SetMeal
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.caloriecalc.app.ui.theme.MealAccentBreakfast
import com.caloriecalc.app.ui.theme.MealAccentDinner
import com.caloriecalc.app.ui.theme.MealAccentLunch
import com.caloriecalc.app.ui.theme.MealAccentPalette
import com.caloriecalc.app.ui.theme.MealAccentPostWorkout
import com.caloriecalc.app.ui.theme.MealAccentPreWorkout
import com.caloriecalc.app.ui.theme.MealAccentSnack
import kotlin.math.abs

/** Picks a food-themed icon for a meal slot from common keywords in its name. */
fun mealSlotIcon(name: String): ImageVector {
    val n = name.lowercase()
    return when {
        "breakfast" in n -> Icons.Filled.FreeBreakfast
        "brunch" in n -> Icons.Filled.BrunchDining
        "lunch" in n -> Icons.Filled.LunchDining
        "dinner" in n || "supper" in n -> Icons.Filled.DinnerDining
        "pre" in n && "workout" in n -> Icons.Filled.Bolt
        "post" in n && "workout" in n -> Icons.Filled.Blender
        "snack" in n -> Icons.Filled.Cookie
        "dessert" in n || "sweet" in n -> Icons.Filled.Icecream
        "coffee" in n -> Icons.Filled.Coffee
        "drink" in n || "shake" in n || "smoothie" in n -> Icons.Filled.LocalDrink
        else -> Icons.Filled.RestaurantMenu
    }
}

/** Whether a meal slot is one of the three main meals (Breakfast/Lunch/Dinner), by keyword —
 * used to limit meal-level protein call-outs to the meals that carry most of the day's intake. */
fun isMainMeal(name: String): Boolean {
    val n = name.lowercase()
    return "breakfast" in n || "brunch" in n || "lunch" in n || "dinner" in n || "supper" in n
}

/** A stable color from a small rotating palette, keyed off a name so the same string always
 * lands on the same color — used as a fallback wherever there's no more specific rule. */
fun stableAccentColor(name: String): Color =
    MealAccentPalette[abs(name.lowercase().hashCode()) % MealAccentPalette.size]

/**
 * Deterministic accent color for a meal slot's icon badge. Recognizable built-ins get a
 * hand-picked, mood-matched color; anything custom gets a stable color from a small rotating
 * palette (keyed off the name, so the same custom meal always lands on the same color).
 */
fun mealSlotAccentColor(name: String): Color {
    val n = name.lowercase()
    return when {
        "breakfast" in n || "brunch" in n -> MealAccentBreakfast
        "lunch" in n -> MealAccentLunch
        "dinner" in n || "supper" in n -> MealAccentDinner
        "pre" in n && "workout" in n -> MealAccentPreWorkout
        "post" in n && "workout" in n -> MealAccentPostWorkout
        "snack" in n -> MealAccentSnack
        else -> stableAccentColor(name)
    }
}

/**
 * A loose, purely decorative food-themed icon for an individual food/product name — adds
 * visual variety to food lists but isn't derived from any nutrition data.
 */
fun foodItemIcon(name: String): ImageVector {
    val n = name.lowercase()
    return when {
        "coffee" in n || "espresso" in n || "latte" in n -> Icons.Filled.LocalCafe
        "beer" in n || "wine" in n || "cocktail" in n -> Icons.Filled.LocalBar
        "pizza" in n -> Icons.Filled.LocalPizza
        "rice" in n || "noodle" in n || "pasta" in n -> Icons.Filled.RiceBowl
        "ramen" in n || "soup" in n -> Icons.Filled.RamenDining
        "egg" in n -> Icons.Filled.Egg
        "chicken" in n || "beef" in n || "meat" in n || "fish" in n || "salmon" in n || "steak" in n ||
            "pork" in n || "turkey" in n -> Icons.Filled.SetMeal
        "salad" in n || "vegetable" in n || "greens" in n || "spinach" in n -> Icons.Filled.Spa
        "cake" in n || "cookie" in n || "chocolate" in n || "candy" in n || "dessert" in n ||
            "ice cream" in n -> Icons.Filled.Icecream
        "shake" in n || "smoothie" in n || "juice" in n || "milk" in n -> Icons.Filled.LocalDrink
        "burger" in n || "fries" in n -> Icons.Filled.Fastfood
        else -> Icons.Filled.Restaurant
    }
}

/** A small circular colored badge with a centered icon — used for meal slots and food rows. */
@Composable
fun FoodIconBadge(
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .background(accentColor.copy(alpha = 0.16f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}
