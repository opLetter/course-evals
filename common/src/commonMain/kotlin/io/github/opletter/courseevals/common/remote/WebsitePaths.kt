package io.github.opletter.courseevals.common.remote

class WebsitePaths(
    private val baseDir: String,
    private val coreDir: String = "$baseDir/core",
    val statsByProfDir: String = "$baseDir/stats-by-prof",
    val courseNamesDir: String = "$coreDir/course-names",
    val teachingDataDir: String = "$coreDir/teaching-F23",
    val allInstructorsFile: String = "$statsByProfDir/instructors.json",
    val deptNamesFile: String = "$coreDir/dept-names.json",
    val schoolsByCodeFile: String = "$statsByProfDir/schools.json",
)