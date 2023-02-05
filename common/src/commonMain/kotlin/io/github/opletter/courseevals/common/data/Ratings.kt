package io.github.opletter.courseevals.common.data

typealias Ratings = List<List<Int>>

class RatingStats(
    val ratings: List<Double>,
    val numResponses: Int,
)

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

fun Collection<Ratings>.combine(): Ratings {
    return reduce { accByQuestion, responsesByQ ->
        // nums from each entry get zipped with each other, by question
        accByQuestion.zip(responsesByQ) { accRatings, ratings ->
            accRatings.zip(ratings, Int::plus)
        }
    }
}

fun List<Int>.getRatingStats(): Pair<Double, Int> {
    val numResponses = sum()
    val ave = mapIndexed { index, num ->
        (index + 1) * num
    }.sum().toDouble() / numResponses
    return ave.roundToDecimal(2) to numResponses
}

fun List<Int>.getRatingAve(): Double = getRatingStats().first

fun Ratings.getAvesAndTotal(): RatingStats { // list of aves per question + ave # of responses
    return RatingStats(ratings = map { it.getRatingAve() }, numResponses = map { it.sum() }.average().toInt())
}