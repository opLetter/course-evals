package io.github.opletter.courseevals.txst

suspend fun main() {
    // TODO: update teaching profs w/flag
    val rootDir = "data-test"
    val statsDir = "$rootDir/stats-by-prof"
    val coreDir = "$rootDir/core"
    val term = "202410" // this term means fall 2023 - see parseSemester for a bit more info

    getTeachingProfs(statsDir, "$coreDir/teaching-F23", term = term)
}

// this function shouldn't be called,
// but we declare it so that the functions it uses can be considered "used"
@Suppress("unused", "FunctionName")
private suspend fun `Overview of data gathering process`() {
    val rootDir = "data-test"
    val statsDir = "$rootDir/stats-by-prof"
    val coreDir = "$rootDir/core"
    val term = "202410" // this term means fall 2023 - see parseSemester for a bit more info

    getAndSaveBaseProfData(rootDir)
    getInstructorReports(rootDir)

    getStatsByProf(rootDir, statsDir)
    getSchoolsData(statsDir)
    getAllInstructors(statsDir, statsDir)

    getDeptNames(coreDir, term = term)
    getCourseNames(statsDir, "$coreDir/course-names")
    getTeachingProfs(statsDir, "$coreDir/teaching-F23", term = term)
}