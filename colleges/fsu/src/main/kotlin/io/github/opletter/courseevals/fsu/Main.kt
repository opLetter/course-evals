package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.fsu.remote.FSURepository
import io.github.opletter.courseevals.fsu.remote.getAllValidCourseKeys
import java.nio.file.Path
import kotlin.io.path.div

suspend fun main(args: Array<String>) {
    args.indexOf("-teaching").takeIf { it != -1 }?.let {
        getTeachingProfs(readDir = Path.of(args[it + 1]), writeDir = Path.of(args[it + 2]), term = "2024-1")
    }
}

// this function shouldn't be called,
// but we declare it so that the functions it uses can be considered "used"
@Suppress("unused", "FunctionName")
private suspend fun `Overview of data gathering process`() {
    val rootDir = Path.of("data-test")
    val reportsDir = rootDir / "reports"
    val organizedReportsDir = rootDir / "organized-reports"
    val statsByProfDir = rootDir / "stats-by-prof"
    val coreDir = rootDir / "core"
    val courseNamesDir = coreDir / "course-names"
    val teachingProfsDir = coreDir / "teaching-S24"

    // preparation - optional?
    val repository = FSURepository.initLoggedIn()
    repository.getAllValidCourseKeys() // note client config comment

    // stats-by-prof
    getAllData(reportsDir, CourseSearchKeys)
    fixReportErrors(reportsDir, Path.of("_")) // optional
    validateReports(reportsDir, Path.of("_")) // optional
    organizeReports(reportsDir, organizedReportsDir)
    getStatsByProf(organizedReportsDir, statsByProfDir)
    createAllInstructors(statsByProfDir, statsByProfDir)

    // core
    getCompleteCourseNames(statsByProfDir, courseNamesDir)
    getDeptNames(coreDir)
    getTeachingProfs(statsByProfDir, teachingProfsDir, term = "2024-1")
}