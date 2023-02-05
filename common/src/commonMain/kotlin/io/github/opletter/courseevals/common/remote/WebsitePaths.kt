package io.github.opletter.courseevals.common.remote

class WebsitePaths(
    private val baseDir: String = "json-data",
    private val extraDir: String = "$baseDir/extra-data",
    val statsByProfDir: String = "$baseDir/data-9-by-prof-stats",
    val courseNamesDir: String = "$extraDir/courseNames",
    val teachingDataDir: String = "$extraDir/S23-teaching",
    val allInstructorsFile: String = "$baseDir/data-9-by-prof/allInstructors.json", // TODO: create & change to "-stats"
    val deptMapFile: String = "$extraDir/deptNameMap.json",
    val schoolMapFile: String = "$statsByProfDir/schoolMap.json",
)