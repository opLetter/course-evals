package io.github.opletter.courseevals.rutgers

import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.data.Ratings
import io.github.opletter.courseevals.common.data.combine

// returns list of (# of 1s, # of 2s, ... # of 5s) for each question
// note that entries must have scores.size>=100 - maybe throw error?
// ***IMPORTANT NOTE*** By default, don't give ratings for question index 7 - as it's mostly irrelevant
fun List<Entry>.getTotalRatings(excludeQuestion7: Boolean = true): Ratings {
    return map { entry ->
        // group by question (there are 10 nums in table per question)
        // + we only care about first 5 nums per Q (the actual ratings) which are all int amounts
        entry.scores.chunked(10)
            .map { it.subList(0, 5).map(Double::toInt) }
            .run { if (excludeQuestion7) (subList(0, 7) + subList(8, 10)) else this }
    }.combine()
}

fun List<Entry>.mapByProfStats(): Map<String, InstructorStats> {
    val adjustedNames = autoNameAdjustments()
    return groupBy { entry ->
        entry.formatFullName().let { adjustedNames[it] ?: it }
    }.mapValues { (_, allEntries) ->
        val courseRatings = allEntries
            .groupBy { it.course }
            .mapValues { (_, entries) -> entries.getTotalRatings().subList(0, 9) }
        InstructorStats(
            lastSem = allEntries.last().semester.numValue,
            overallStats = allEntries.getTotalRatings().subList(0, 9),
            courseStats = courseRatings
        )
    }
}