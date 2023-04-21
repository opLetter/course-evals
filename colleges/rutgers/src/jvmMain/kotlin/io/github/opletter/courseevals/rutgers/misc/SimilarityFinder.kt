package io.github.opletter.courseevals.rutgers.misc

import kotlin.math.max
import kotlin.math.min

fun similarity(s1: String?, s2: String?): Double {
    if (s1 == null || s2 == null) return 0.0
    val longerLength = max(s1.length, s2.length)
    return (longerLength - levenshtein(s1, s2)) / longerLength.toDouble()
}

// taken from https://gist.github.com/ademar111190/34d3de41308389a0d0d8#file-levenshtein-kt
private fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
    if (lhs == rhs) return 0
    if (lhs.isEmpty()) return rhs.length
    if (rhs.isEmpty()) return lhs.length

    val lhsLength = lhs.length + 1
    val rhsLength = rhs.length + 1

    var cost = Array(lhsLength) { it }
    var newCost = Array(lhsLength) { 0 }

    for (i in 1 until rhsLength) {
        newCost[0] = i

        for (j in 1 until lhsLength) {
            val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1

            val costReplace = cost[j - 1] + match
            val costInsert = cost[j] + 1
            val costDelete = newCost[j - 1] + 1

            newCost[j] = min(min(costInsert, costDelete), costReplace)
        }

        val swap = cost
        cost = newCost
        newCost = swap
    }

    return cost[lhsLength - 1]
}