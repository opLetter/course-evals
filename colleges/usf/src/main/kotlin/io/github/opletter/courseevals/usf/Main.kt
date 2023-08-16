package io.github.opletter.courseevals.usf

suspend fun main(args: Array<String>) {
    // TODO: restore
//    if ("-teaching" in args)
//        getTeachingProfs()
}

// this function shouldn't be called,
// but we declare it so that the functions it uses can be considered "used"
@Suppress("unused", "FunctionName")
private suspend fun `Overview of data gathering process`() {
    val rootDir = "data-test"
    val reportsDir = "$rootDir/reports"
    val statsDir = "$rootDir/stats-by-prof"
    val coreDir = "$rootDir/core"

    getData(reportsDir)
    getStatsByProf(getReportsFromFiles(reportsDir), statsDir)
    getSchoolsData(statsDir)
    getAllInstructors(statsDir, statsDir)

    getDeptNames(coreDir)
    getCompleteCourseNames(statsDir, "$coreDir/course-names")
    getTeachingProfs(statsDir, "$coreDir/teaching-F23")
}