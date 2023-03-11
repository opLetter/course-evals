package io.github.opletter.courseevals.common.data

enum class SemesterType {
    Spring, Summer, Fall
}

// not a big fan of the current implementation, but it works
sealed class Semester<T : Semester<T>>(val numValue: Int, validSemesters: List<SemesterType>) : Comparable<T> {
    val type: SemesterType = validSemesters[numValue % validSemesters.size]
    val year: Int = numValue / validSemesters.size

    operator fun rangeTo(other: T): List<T> = (numValue..other.numValue).map { createInstance(it) }
    abstract fun createInstance(numValue: Int): T

    override fun compareTo(other: T): Int = numValue.compareTo(other.numValue)

    override fun toString(): String = "$type $year"

    interface Factory<T : Semester<T>> {
        fun createInstance(numValue: Int): T
        val validSemesters: List<SemesterType>
        fun valueOf(type: SemesterType, year: Int): T {
            require(type in validSemesters)
            return createInstance(validSemesters.indexOf(type) + year * validSemesters.size)
        }
    }

    class RU(numValue: Int) : Semester<RU>(numValue, validSemesters) {
        override fun createInstance(numValue: Int): RU = Companion.createInstance(numValue)

        companion object : Factory<RU> {
            override val validSemesters = listOf(SemesterType.Spring, SemesterType.Fall)
            override fun createInstance(numValue: Int): RU = RU(numValue)
        }
    }

    class FSU(numValue: Int) : Semester<FSU>(numValue, validSemesters) {
        override fun createInstance(numValue: Int): FSU = FSU(numValue)

        companion object : Factory<FSU> {
            override val validSemesters = listOf(SemesterType.Spring, SemesterType.Summer, SemesterType.Fall)
            override fun createInstance(numValue: Int): FSU = FSU(numValue)
        }
    }
}