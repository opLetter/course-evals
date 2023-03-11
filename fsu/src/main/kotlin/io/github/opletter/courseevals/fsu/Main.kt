package io.github.opletter.courseevals.fsu

import kotlin.collections.component1
import kotlin.collections.component2

suspend fun main() {
//    getStatsByProf()
//    createAllInstructors()
//    getCourseNames()
//    getTeachingProfs("jsonData/extraData/teachingF23")
}

data class FSUSemester(val type: FSUSemesterType, val year: Int) : Comparable<FSUSemester> {
    override fun compareTo(other: FSUSemester): Int {
        return numValue.compareTo(other.numValue)
    }

    val numValue get() = year * 3 + type.ordinal

    companion object {
        fun valueOf(num: Int): FSUSemester = FSUSemester(FSUSemesterType.values()[num % 3], num / 3)
        fun valueOf(str: String): FSUSemester {
            val (year, type) = str.split(" ")
            return FSUSemester(FSUSemesterType.valueOf(type), year.toInt())
        }
    }
}

operator fun FSUSemester.rangeTo(other: FSUSemester): List<FSUSemester> =
    (numValue..other.numValue).map { FSUSemester.valueOf(it) }

fun FSUSemester.prev(byAmount: Int = 1) = FSUSemester.valueOf(numValue - byAmount)
fun FSUSemester.next(byAmount: Int = 1) = FSUSemester.valueOf(numValue + byAmount)

enum class FSUSemesterType {
    Spring, Summer, Fall
}