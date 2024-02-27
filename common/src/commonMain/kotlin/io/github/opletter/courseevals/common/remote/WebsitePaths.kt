package io.github.opletter.courseevals.common.remote

import kotlin.jvm.JvmInline

class WebsitePaths(
    baseDir: String,
    coreDir: String = baseDir / "core",
    statsByProfDir: String = baseDir / "stats-by-prof",
    courseNamesDir: String = coreDir / "course-names",
    teachingDataDir: String = coreDir / "teaching-F24",
    allInstructorsFile: String = statsByProfDir / "instructors.json",
    deptNamesFile: String = coreDir / "dept-names.json",
    schoolsByCodeFile: String = statsByProfDir / "schools.json",
) {
    val baseDir = PathWrapper(baseDir)
    val coreDir = PathWrapper(coreDir)
    val statsByProfDir = PathWrapper(statsByProfDir)
    val courseNamesDir = PathWrapper(courseNamesDir)
    val teachingDataDir = PathWrapper(teachingDataDir)
    val allInstructorsFile = PathWrapper(allInstructorsFile)
    val deptNamesFile = PathWrapper(deptNamesFile)
    val schoolsByCodeFile = PathWrapper(schoolsByCodeFile)
}

private operator fun String.div(other: String) = "$this/$other"

// could set up expect/actual with Path on jvm, but not needed for now
/** A wrapper class for representing file paths, useful for safer conversion to `java.nio.file.Path` in JVM sources. */
@JvmInline
value class PathWrapper internal constructor(val value: String) {
    override fun toString() = value
}