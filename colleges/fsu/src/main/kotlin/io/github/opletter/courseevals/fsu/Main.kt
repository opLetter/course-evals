package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.common.SchoolDataApi
import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.path
import io.github.opletter.courseevals.common.remote.WebsitePaths
import io.github.opletter.courseevals.common.runFromArgs
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively
import kotlin.io.path.div

suspend fun main(args: Array<String>) {
    FSUApi.runFromArgs(args)
}

object FSUApi : SchoolDataApi<Semester.Triple> {
    val defaultPaths = WebsitePaths("data-test")

    override val currentSem = Semester.Triple.valueOf(SemesterType.Fall, 2024)

    override suspend fun getSchoolRawData() {
        val reportsDir = defaultPaths.baseDir.path / "reports"
        val organizedReportsDir = defaultPaths.baseDir.path / "organized-reports"
        // preparation - optional?
//        val repository = FSURepository.initLoggedIn()
//        repository.getAllValidCourseKeys() // note client config comment

        getAllData(reportsDir, CourseSearchKeys)
//        val tempDir = defaultPaths.baseDir.path / "reports-temp"
//        fixReportErrors(reportsDir, tempDir) // optional
//        validateReports(reportsDir, tempDir) // optional
        organizeReports(reportsDir, organizedReportsDir)
    }

    @OptIn(ExperimentalPathApi::class)
    override fun getSchoolStatsByProf(rawDataDir: Path): SchoolDeptsMap<Map<String, InstructorStats>> {
        val tempDir = rawDataDir / "organized-reports-temp"
        organizeReports(rawDataDir, tempDir)
        val statsByProf = getStatsByProf(tempDir)
        tempDir.deleteRecursively()
        return statsByProf
    }

    override fun getSchoolSchoolsData(statsByProf: SchoolDeptsMap<Map<String, InstructorStats>>): Map<String, School> {
        return statsByProf.entries.associate { (key, value) ->
            key to School(
                code = key,
                name = campusMap[key] ?: error("No name for $key"),
                depts = value.keys.sorted().toSet(),
                campuses = setOf(Campus.valueOf(key.uppercase())),
                level = LevelOfStudy.U,
            )
        }
    }

    override fun getSchoolAllInstructors(statsByProfDir: Path) = createAllInstructors(statsByProfDir)

    override suspend fun getSchoolDeptNames() = getDeptNames()

    override suspend fun getSchoolCourseNames(
        statsByProfDir: Path,
        existingCourseNamesDir: Path?,
    ): SchoolDeptsMap<Map<String, String>> {
        return getCompleteCourseNames(
            statsByProfDir,
            terms = currentSem.prev(2)..currentSem,
            existingCourseNamesDir = existingCourseNamesDir,
        )
    }

    override suspend fun getSchoolTeachingProfs(statsByProfDir: Path, term: Semester.Triple) =
        getTeachingProfs(statsByProfDir, term)
}

fun Semester.Triple.toFSUString(): String {
    val semStr = when (type) {
        SemesterType.Spring -> "1"
        SemesterType.Summer -> "6"
        SemesterType.Fall -> "9"
    }
    return "$year-$semStr"
}