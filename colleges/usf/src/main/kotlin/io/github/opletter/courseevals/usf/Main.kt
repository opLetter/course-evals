package io.github.opletter.courseevals.usf

suspend fun main(args: Array<String>) {
    args.indexOf("-teaching").takeIf { it != -1 }?.let {
        getTeachingProfs(readDir = args[it + 1], writeDir = args[it + 2], term = "202401")
    }
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
    getTeachingProfs(statsDir, "$coreDir/teaching-S24", "202401")
}