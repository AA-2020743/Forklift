package com.caloriecalc.app.data.seed

import com.caloriecalc.app.data.local.entity.Exercise
import com.caloriecalc.app.domain.MuscleGroup
import com.caloriecalc.app.domain.MuscleSubGroup

/**
 * A starter exercise library with fine-grained sub-group tagging (e.g. biceps long vs
 * short head, all three triceps heads) so [com.caloriecalc.app.domain.MuscleCoverageAnalyzer]
 * has enough coverage to make useful suggestions from day one.
 */
object ExerciseSeedData {

    val exercises: List<Exercise> = listOf(
        // Chest
        ex("Incline Barbell Bench Press", MuscleGroup.CHEST, setOf(MuscleSubGroup.CHEST_UPPER), "Barbell"),
        ex("Incline Dumbbell Press", MuscleGroup.CHEST, setOf(MuscleSubGroup.CHEST_UPPER), "Dumbbell"),
        ex("Flat Barbell Bench Press", MuscleGroup.CHEST, setOf(MuscleSubGroup.CHEST_MID), "Barbell"),
        ex("Flat Dumbbell Press", MuscleGroup.CHEST, setOf(MuscleSubGroup.CHEST_MID), "Dumbbell"),
        ex("Push-Up", MuscleGroup.CHEST, setOf(MuscleSubGroup.CHEST_MID), "Bodyweight"),
        ex("Decline Bench Press", MuscleGroup.CHEST, setOf(MuscleSubGroup.CHEST_LOWER), "Barbell"),
        ex("Chest Dip", MuscleGroup.CHEST, setOf(MuscleSubGroup.CHEST_LOWER, MuscleSubGroup.TRICEPS_LATERAL_HEAD), "Bodyweight"),

        // Back
        ex("Pull-Up", MuscleGroup.BACK, setOf(MuscleSubGroup.BACK_LATS), "Bodyweight"),
        ex("Lat Pulldown", MuscleGroup.BACK, setOf(MuscleSubGroup.BACK_LATS), "Cable"),
        ex("Straight-Arm Pulldown", MuscleGroup.BACK, setOf(MuscleSubGroup.BACK_LATS), "Cable"),
        ex("Seated Cable Row", MuscleGroup.BACK, setOf(MuscleSubGroup.BACK_UPPER), "Cable"),
        ex("Chest-Supported Row", MuscleGroup.BACK, setOf(MuscleSubGroup.BACK_UPPER), "Dumbbell"),
        ex("Barbell Shrug", MuscleGroup.BACK, setOf(MuscleSubGroup.BACK_TRAPS), "Barbell"),
        ex("Dumbbell Shrug", MuscleGroup.BACK, setOf(MuscleSubGroup.BACK_TRAPS), "Dumbbell"),
        ex("Deadlift", MuscleGroup.BACK, setOf(MuscleSubGroup.BACK_LOWER, MuscleSubGroup.HAMSTRINGS, MuscleSubGroup.GLUTES), "Barbell"),
        ex("Back Extension", MuscleGroup.BACK, setOf(MuscleSubGroup.BACK_LOWER), "Bodyweight"),

        // Shoulders
        ex("Overhead Barbell Press", MuscleGroup.SHOULDERS, setOf(MuscleSubGroup.SHOULDERS_ANTERIOR), "Barbell"),
        ex("Dumbbell Shoulder Press", MuscleGroup.SHOULDERS, setOf(MuscleSubGroup.SHOULDERS_ANTERIOR), "Dumbbell"),
        ex("Front Raise", MuscleGroup.SHOULDERS, setOf(MuscleSubGroup.SHOULDERS_ANTERIOR), "Dumbbell"),
        ex("Lateral Raise", MuscleGroup.SHOULDERS, setOf(MuscleSubGroup.SHOULDERS_LATERAL), "Dumbbell"),
        ex("Cable Lateral Raise", MuscleGroup.SHOULDERS, setOf(MuscleSubGroup.SHOULDERS_LATERAL), "Cable"),
        ex("Face Pull", MuscleGroup.SHOULDERS, setOf(MuscleSubGroup.SHOULDERS_POSTERIOR, MuscleSubGroup.BACK_UPPER), "Cable"),
        ex("Reverse Pec Deck", MuscleGroup.SHOULDERS, setOf(MuscleSubGroup.SHOULDERS_POSTERIOR), "Machine"),
        ex("Bent-Over Rear Delt Raise", MuscleGroup.SHOULDERS, setOf(MuscleSubGroup.SHOULDERS_POSTERIOR), "Dumbbell"),

        // Biceps
        ex("Incline Dumbbell Curl", MuscleGroup.BICEPS, setOf(MuscleSubGroup.BICEPS_LONG_HEAD), "Dumbbell"),
        ex("Drag Curl", MuscleGroup.BICEPS, setOf(MuscleSubGroup.BICEPS_LONG_HEAD), "Barbell"),
        ex("Preacher Curl", MuscleGroup.BICEPS, setOf(MuscleSubGroup.BICEPS_SHORT_HEAD), "EZ Bar"),
        ex("Concentration Curl", MuscleGroup.BICEPS, setOf(MuscleSubGroup.BICEPS_SHORT_HEAD), "Dumbbell"),
        ex("Hammer Curl", MuscleGroup.BICEPS, setOf(MuscleSubGroup.BRACHIALIS, MuscleSubGroup.FOREARMS), "Dumbbell"),
        ex("Cross-Body Hammer Curl", MuscleGroup.BICEPS, setOf(MuscleSubGroup.BRACHIALIS), "Dumbbell"),

        // Triceps
        ex("Overhead Triceps Extension", MuscleGroup.TRICEPS, setOf(MuscleSubGroup.TRICEPS_LONG_HEAD), "Dumbbell"),
        ex("Lying Skull Crusher", MuscleGroup.TRICEPS, setOf(MuscleSubGroup.TRICEPS_LONG_HEAD), "EZ Bar"),
        ex("Cable Overhead Extension", MuscleGroup.TRICEPS, setOf(MuscleSubGroup.TRICEPS_LONG_HEAD), "Cable"),
        ex("Rope Triceps Pushdown", MuscleGroup.TRICEPS, setOf(MuscleSubGroup.TRICEPS_LATERAL_HEAD), "Cable"),
        ex("Close-Grip Bench Press", MuscleGroup.TRICEPS, setOf(MuscleSubGroup.TRICEPS_LATERAL_HEAD, MuscleSubGroup.CHEST_MID), "Barbell"),
        ex("Reverse-Grip Pushdown", MuscleGroup.TRICEPS, setOf(MuscleSubGroup.TRICEPS_MEDIAL_HEAD), "Cable"),
        ex("Diamond Push-Up", MuscleGroup.TRICEPS, setOf(MuscleSubGroup.TRICEPS_MEDIAL_HEAD), "Bodyweight"),

        // Legs
        ex("Back Squat", MuscleGroup.QUADS, setOf(MuscleSubGroup.QUADS, MuscleSubGroup.GLUTES), "Barbell"),
        ex("Leg Press", MuscleGroup.QUADS, setOf(MuscleSubGroup.QUADS), "Machine"),
        ex("Leg Extension", MuscleGroup.QUADS, setOf(MuscleSubGroup.QUADS), "Machine"),
        ex("Bulgarian Split Squat", MuscleGroup.QUADS, setOf(MuscleSubGroup.QUADS, MuscleSubGroup.GLUTES), "Dumbbell"),
        ex("Romanian Deadlift", MuscleGroup.HAMSTRINGS, setOf(MuscleSubGroup.HAMSTRINGS, MuscleSubGroup.GLUTES), "Barbell"),
        ex("Leg Curl", MuscleGroup.HAMSTRINGS, setOf(MuscleSubGroup.HAMSTRINGS), "Machine"),
        ex("Good Morning", MuscleGroup.HAMSTRINGS, setOf(MuscleSubGroup.HAMSTRINGS, MuscleSubGroup.BACK_LOWER), "Barbell"),
        ex("Hip Thrust", MuscleGroup.GLUTES, setOf(MuscleSubGroup.GLUTES), "Barbell"),
        ex("Glute Bridge", MuscleGroup.GLUTES, setOf(MuscleSubGroup.GLUTES), "Bodyweight"),
        ex("Standing Calf Raise", MuscleGroup.CALVES, setOf(MuscleSubGroup.CALVES_GASTROC), "Machine"),
        ex("Seated Calf Raise", MuscleGroup.CALVES, setOf(MuscleSubGroup.CALVES_SOLEUS), "Machine"),

        // Core
        ex("Cable Crunch", MuscleGroup.CORE, setOf(MuscleSubGroup.CORE_RECTUS), "Cable"),
        ex("Hanging Leg Raise", MuscleGroup.CORE, setOf(MuscleSubGroup.CORE_RECTUS), "Bodyweight"),
        ex("Crunch", MuscleGroup.CORE, setOf(MuscleSubGroup.CORE_RECTUS), "Bodyweight"),
        ex("Russian Twist", MuscleGroup.CORE, setOf(MuscleSubGroup.CORE_OBLIQUES), "Bodyweight"),
        ex("Side Plank", MuscleGroup.CORE, setOf(MuscleSubGroup.CORE_OBLIQUES), "Bodyweight"),
        ex("Cable Wood Chop", MuscleGroup.CORE, setOf(MuscleSubGroup.CORE_OBLIQUES), "Cable"),

        // Forearms
        ex("Wrist Curl", MuscleGroup.FOREARMS, setOf(MuscleSubGroup.FOREARMS), "Dumbbell"),
        ex("Farmer's Carry", MuscleGroup.FOREARMS, setOf(MuscleSubGroup.FOREARMS), "Dumbbell"),
        ex("Reverse Curl", MuscleGroup.FOREARMS, setOf(MuscleSubGroup.FOREARMS, MuscleSubGroup.BRACHIALIS), "Barbell")
    )

    private fun ex(name: String, group: MuscleGroup, subGroups: Set<MuscleSubGroup>, equipment: String) =
        Exercise(name = name, primaryMuscleGroup = group, targetSubGroups = subGroups, equipment = equipment)
}
