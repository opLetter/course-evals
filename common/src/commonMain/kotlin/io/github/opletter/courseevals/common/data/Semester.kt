package io.github.opletter.courseevals.common.data

import kotlinx.serialization.Serializable

@Serializable
data class Semester(val type: SemesterType, val year: Int) : Comparable<Semester> {
    override fun compareTo(other: Semester): Int {
        return numValue.compareTo(other.numValue)
    }

    constructor(num: Int) : this(if (num % 2 == 0) SemesterType.Spring else SemesterType.Fall, num / 2)

    val numValue get() = year * 2 + if (type == SemesterType.Spring) 0 else 1
}

fun Semester.prev(byAmount: Int = 1) = Semester(numValue - byAmount)
fun Semester.next(byAmount: Int = 1) = Semester(numValue + byAmount)

enum class SemesterType(val num: Int) {
    Spring(1), Fall(9);

    fun other(): SemesterType = if (this == Spring) Fall else Spring
}