package io.github.opletter.courseevals.common.remote

import io.github.opletter.courseevals.common.data.Semester
import io.github.opletter.courseevals.common.data.SemesterType
import kotlin.jvm.JvmInline

class WebsitePaths(
    baseDir: String,
    coreDir: String = baseDir / "core",
    statsByProfDir: String = baseDir / "stats-by-prof",
    courseNamesDir: String = coreDir / "course-names",
    teachingDataDir: String = coreDir / "teaching-F25",
    allInstructorsFile: String = statsByProfDir / "instructors.json",
    deptNamesFile: String = coreDir / "dept-names.json",
    schoolsByCodeFile: String = statsByProfDir / "schools.json",
) {
    constructor(baseDir: String, semester: Semester<*>) :
            this(baseDir, teachingDataDir = baseDir / "core/teaching-${semester.toShortString()}")

    val baseDir = PathWrapper(baseDir)
    val coreDir = PathWrapper(coreDir)
    val statsByProfDir = PathWrapper(statsByProfDir)
    val courseNamesDir = PathWrapper(courseNamesDir)
    val teachingDataDir = PathWrapper(teachingDataDir)
    val allInstructorsFile = PathWrapper(allInstructorsFile)
    val deptNamesFile = PathWrapper(deptNamesFile)
    val schoolsByCodeFile = PathWrapper(schoolsByCodeFile)
}

private fun Semester<*>.toShortString(): String {
    val prefix = when (type) {
        SemesterType.Fall -> "F"
        SemesterType.Spring -> "S"
        SemesterType.Summer -> "SU"
    }
    return prefix + (year % 100)
}


private operator fun String.div(other: String) = "$this/$other"

// could set up expect/actual with Path on jvm, but not needed for now
/** A wrapper class for representing file paths, useful for safer conversion to `java.nio.file.Path` in JVM sources. */
@JvmInline
value class PathWrapper internal constructor(val value: String) {
    override fun toString() = value
}