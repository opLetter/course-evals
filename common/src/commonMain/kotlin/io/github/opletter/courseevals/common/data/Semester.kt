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
            val (type, year) = str.split(" ")
            return valueOf(SemesterType.valueOf(type), year.toInt())
        }
    }

    class Double private constructor(numValue: Int) : Semester<Double>(numValue, validSemesters) {
        override val factory = Companion

        companion object : Factory<Double> {
            override val validSemesters = listOf(SemesterType.Spring, SemesterType.Fall)
            override fun valueOf(numValue: Int): Double = Double(numValue)
        }
    }

    class Triple private constructor(numValue: Int) : Semester<Triple>(numValue, validSemesters) {
        override val factory = Companion

        companion object : Factory<Triple> {
            override val validSemesters = listOf(SemesterType.Spring, SemesterType.Summer, SemesterType.Fall)
            override fun valueOf(numValue: Int): Triple = Triple(numValue)
        }
    }
}