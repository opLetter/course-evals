package io.github.opletter.courseevals.txst

suspend fun main(args: Array<String>) {
    args.indexOf("-teaching").takeIf { it != -1 }?.let {
        getTeachingProfs(readDir = args[it + 1], writeDir = args[it + 2], term = "202430")
    }
}

// this function shouldn't be called,
// but we declare it so that the functions it uses can be considered "used"
@Suppress("unused", "FunctionName")
private suspend fun `Overview of data gathering process`() {
    val rootDir = "data-test"
    val statsDir = "$rootDir/stats-by-prof"
    val coreDir = "$rootDir/core"
    val term = "202430" // this term means spring 2024 - see parseSemester for a bit more info

    getAndSaveBaseProfData(rootDir)
    getInstructorReports(rootDir)

    getStatsByProf(rootDir, statsDir)
    getSchoolsData(statsDir)
    getAllInstructors(statsDir, statsDir)

    getDeptNames(coreDir, term = term)
    getCourseNames(statsDir, "$coreDir/course-names")
    getTeachingProfs(statsDir, "$coreDir/teaching-F23", term = term)
}