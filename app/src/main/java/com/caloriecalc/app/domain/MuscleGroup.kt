package com.caloriecalc.app.domain

enum class MuscleGroup(val displayName: String) {
    CHEST("Chest"),
    BACK("Back"),
    SHOULDERS("Shoulders"),
    BICEPS("Biceps"),
    TRICEPS("Triceps"),
    QUADS("Quads"),
    HAMSTRINGS("Hamstrings"),
    GLUTES("Glutes"),
    CALVES("Calves"),
    CORE("Core"),
    FOREARMS("Forearms")
}

/** Finer-grained targets used for exercise-coverage / suggestion logic. */
enum class MuscleSubGroup(val group: MuscleGroup, val displayName: String) {
    CHEST_UPPER(MuscleGroup.CHEST, "Upper Chest"),
    CHEST_MID(MuscleGroup.CHEST, "Mid Chest"),
    CHEST_LOWER(MuscleGroup.CHEST, "Lower Chest"),

    BACK_LATS(MuscleGroup.BACK, "Lats"),
    BACK_UPPER(MuscleGroup.BACK, "Upper Back / Rhomboids"),
    BACK_TRAPS(MuscleGroup.BACK, "Traps"),
    BACK_LOWER(MuscleGroup.BACK, "Lower Back / Erectors"),

    SHOULDERS_ANTERIOR(MuscleGroup.SHOULDERS, "Front Delts"),
    SHOULDERS_LATERAL(MuscleGroup.SHOULDERS, "Side Delts"),
    SHOULDERS_POSTERIOR(MuscleGroup.SHOULDERS, "Rear Delts"),

    BICEPS_LONG_HEAD(MuscleGroup.BICEPS, "Biceps (Long Head)"),
    BICEPS_SHORT_HEAD(MuscleGroup.BICEPS, "Biceps (Short Head)"),
    BRACHIALIS(MuscleGroup.BICEPS, "Brachialis"),

    TRICEPS_LONG_HEAD(MuscleGroup.TRICEPS, "Triceps (Long Head)"),
    TRICEPS_LATERAL_HEAD(MuscleGroup.TRICEPS, "Triceps (Lateral Head)"),
    TRICEPS_MEDIAL_HEAD(MuscleGroup.TRICEPS, "Triceps (Medial Head)"),

    QUADS(MuscleGroup.QUADS, "Quadriceps"),
    HAMSTRINGS(MuscleGroup.HAMSTRINGS, "Hamstrings"),
    GLUTES(MuscleGroup.GLUTES, "Glutes"),

    CALVES_GASTROC(MuscleGroup.CALVES, "Calves (Gastrocnemius)"),
    CALVES_SOLEUS(MuscleGroup.CALVES, "Calves (Soleus)"),

    CORE_RECTUS(MuscleGroup.CORE, "Abs (Rectus Abdominis)"),
    CORE_OBLIQUES(MuscleGroup.CORE, "Obliques"),

    FOREARMS(MuscleGroup.FOREARMS, "Forearms")
}
