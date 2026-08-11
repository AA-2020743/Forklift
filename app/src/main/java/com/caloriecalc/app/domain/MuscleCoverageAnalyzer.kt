package com.caloriecalc.app.domain

import com.caloriecalc.app.data.local.entity.Exercise
import com.caloriecalc.app.data.local.entity.SetEntry

data class ExerciseSuggestion(
    val subGroup: MuscleSubGroup,
    val exercises: List<Exercise>
)

data class MuscleCoverageReport(
    val coveredSubGroups: Set<MuscleSubGroup>,
    val missingSubGroups: Set<MuscleSubGroup>,
    val suggestions: List<ExerciseSuggestion>
)

/**
 * Looks at what was actually trained in a recent window (e.g. the last 7 days) and
 * flags muscle sub-groups (like the biceps' long vs short head, or triceps' three heads)
 * that received no meaningful direct work, then suggests specific exercises to fill the gap.
 */
object MuscleCoverageAnalyzer {

    /** An exercise only "counts" toward coverage once at least this many sets were done. */
    private const val MIN_SETS_TO_COUNT = 2

    fun analyze(
        setsInWindow: List<SetEntry>,
        exercisesById: Map<Long, Exercise>,
        allExercises: List<Exercise>,
        subGroupsToTrack: Set<MuscleSubGroup> = MuscleSubGroup.entries.toSet()
    ): MuscleCoverageReport {
        val setsPerExercise = setsInWindow.groupingBy { it.exerciseId }.eachCount()
        val trainedExerciseIds = setsPerExercise.filterValues { it >= MIN_SETS_TO_COUNT }.keys

        val covered = trainedExerciseIds
            .flatMap { id -> exercisesById[id]?.targetSubGroups.orEmpty() }
            .toSet()
            .intersect(subGroupsToTrack)

        val missing = subGroupsToTrack - covered

        val suggestions = missing.mapNotNull { subGroup ->
            val candidates = allExercises
                .filter { subGroup in it.targetSubGroups && it.id !in trainedExerciseIds }
                .sortedBy { it.targetSubGroups.size }
                .take(3)
            if (candidates.isEmpty()) null else ExerciseSuggestion(subGroup, candidates)
        }.sortedBy { it.subGroup.displayName }

        return MuscleCoverageReport(covered, missing, suggestions)
    }
}
