package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.fsu.remote.FSURepository
import io.github.opletter.courseevals.fsu.remote.getAllValidCourseKeys

suspend fun main(args: Array<String>) {
    args.indexOf("-teaching").takeIf { it != -1 }?.let {
        getTeachingProfs(readDir = args[it + 1], writeDir = args[it + 2], term = "2024-1")
    }
}

// this function shouldn't be called,
// but we declare it so that the functions it uses can be considered "used"
@Suppress("unused", "FunctionName")
private suspend fun `Overview of data gathering process`() {
    val rootDir = "data-test"
    val reportsDir = "$rootDir/reports"
    val organizedReportsDir = "$rootDir/organized-reports"
    val statsByProfDir = "$rootDir/stats-by-prof"
    val coreDir = "$rootDir/core"
    val courseNamesDir = "$coreDir/course-names"
    val teachingProfsDir = "$coreDir/teaching-S24"

    // preparation - optional?
    val repository = FSURepository().also { it.login() }
    repository.getAllValidCourseKeys() // note client config comment

    // stats-by-prof
    getAllData(reportsDir, CourseSearchKeys)
    fixReportErrors(reportsDir, "_") // optional
    validateReports(reportsDir, "_") // optional
    organizeReports(reportsDir, organizedReportsDir)
    getStatsByProf(organizedReportsDir, statsByProfDir)
    createAllInstructors(statsByProfDir, statsByProfDir)

    // core
    getCompleteCourseNames(statsByProfDir, courseNamesDir)
    getDeptNames(coreDir)
    getTeachingProfs(statsByProfDir, teachingProfsDir, term = "2024-1")
}