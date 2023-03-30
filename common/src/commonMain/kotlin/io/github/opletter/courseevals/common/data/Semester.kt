package io.github.opletter.courseevals.common.data

enum class SemesterType {
    Spring, Summer, Fall
}

// not a big fan of the current implementation, but it works
sealed class Semester<T : Semester<T>>(val numValue: Int, validSemesters: List<SemesterType>) : Comparable<T> {
    val type: SemesterType = validSemesters[numValue % validSemesters.size]
    val year: Int = numValue / validSemesters.size

    operator fun rangeTo(other: T): List<T> = (numValue..other.numValue).map { factory.valueOf(it) }
    override fun compareTo(other: T): Int = numValue.compareTo(other.numValue)
    override fun toString(): String = "$type $year"

    protected abstract val factory: Factory<T>

    protected interface Factory<T : Semester<T>> {
        fun valueOf(numValue: Int): T
        val validSemesters: List<SemesterType>
        fun valueOf(type: SemesterType, year: Int): T {
            require(type in validSemesters)
            return valueOf(validSemesters.indexOf(type) + year * validSemesters.size)
        }

        fun valueOf(str: String): T {
            val (year, type) = str.split(" ")
            return valueOf(SemesterType.valueOf(type), year.toInt())
        }
    }

    class RU private constructor(numValue: Int) : Semester<RU>(numValue, validSemesters) {
        override val factory = Companion

        companion object : Factory<RU> {
            override val validSemesters = listOf(SemesterType.Spring, SemesterType.Fall)
            override fun valueOf(numValue: Int): RU = RU(numValue)
        }
    }

    class FSU private constructor(numValue: Int) : Semester<FSU>(numValue, validSemesters) {
        override val factory = Companion

        companion object : Factory<FSU> {
            override val validSemesters = listOf(SemesterType.Spring, SemesterType.Summer, SemesterType.Fall)
            override fun valueOf(numValue: Int): FSU = FSU(numValue)
        }
    }
}