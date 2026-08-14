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
 * Looks at what was actually trained in a recent window and flags specific gaps worth
 * addressing — e.g. you've done biceps work but only the short head, not the long head.
 *
 * Deliberately conservative to stay realistic: a muscle *group* that received zero work at
 * all in the window is treated as a program-split choice (e.g. an upper-body-only block) and
 * is never flagged — only sub-group gaps *within* groups you're already actively training are
 * surfaced, and the suggestion list is capped so it reads as a short, actionable note rather
 * than a wall of text.
 */
object MuscleCoverageAnalyzer {

    /** An exercise "counts" toward coverage once at least this many sets were done. */
    private const val MIN_SETS_TO_COUNT = 1
    private const val MAX_SUGGESTIONS = 4
    private const val MAX_EXERCISES_PER_SUGGESTION = 2

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

        // Only muscle groups with *some* coverage this window are eligible for gap-flagging —
        // a group with zero coverage is a program choice, not an oversight.
        val activeGroups = covered.map { it.group }.toSet()

        val missing = (subGroupsToTrack - covered).filter { it.group in activeGroups }.toSet()

        val suggestions = missing.mapNotNull { subGroup ->
            val candidates = allExercises
                .filter { subGroup in it.targetSubGroups && it.id !in trainedExerciseIds }
                .sortedBy { it.targetSubGroups.size }
                .take(MAX_EXERCISES_PER_SUGGESTION)
            if (candidates.isEmpty()) null else ExerciseSuggestion(subGroup, candidates)
        }
            .sortedBy { it.subGroup.displayName }
            .take(MAX_SUGGESTIONS)

        return MuscleCoverageReport(covered, missing, suggestions)
    }
}
