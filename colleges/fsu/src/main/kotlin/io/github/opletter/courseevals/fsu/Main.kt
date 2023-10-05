package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.fsu.remote.FSURepository
import io.github.opletter.courseevals.fsu.remote.getAllValidCourseKeys

suspend fun main(args: Array<String>) {
    if ("-teaching" in args) {
        val rootDir = "../../data/fsu/processed"
        getTeachingProfs("$rootDir/stats-by-prof", "$rootDir/core/teaching-S24")
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
    val teachingProfsDir = "$coreDir/teaching-F23"

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
    getTeachingProfs(statsByProfDir, teachingProfsDir)
}