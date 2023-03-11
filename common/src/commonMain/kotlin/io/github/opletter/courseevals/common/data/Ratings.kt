package io.github.opletter.courseevals.common.data

typealias Ratings = List<List<Int>>

class RatingStats(
    val ratings: List<Double>,
    val numResponses: Int,
)

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