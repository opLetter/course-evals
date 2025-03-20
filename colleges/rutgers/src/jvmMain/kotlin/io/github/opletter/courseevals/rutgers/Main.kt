package io.github.opletter.courseevals.rutgers

import io.github.opletter.courseevals.common.SchoolDataApi
import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.getCompleteSchoolDeptsMap
import io.github.opletter.courseevals.common.remote.WebsitePaths
import io.github.opletter.courseevals.common.runFromArgs
import io.github.opletter.courseevals.common.writeAsJson
import io.github.opletter.courseevals.rutgers.remote.SIRSSource
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.readText


suspend fun main(args: Array<String>) {
    RutgersApi.runFromArgs(args)
}

object RutgersApi : SchoolDataApi<Semester.Double> {
    val defaultPaths = WebsitePaths("data-test")

    override val currentSem = Semester.Double.valueOf(SemesterType.Fall, 2025)

    override suspend fun getSchoolRawData() {
        val baseDir = Path.of("data-test")
        val rawDir = baseDir / "raw"

        val semesters = Semester.Double.valueOf(SemesterType.Fall, 2013)..
                Semester.Double.valueOf(SemesterType.Spring, 2023)
        val schoolMap = SIRSSource.getCompleteSchoolMap(semesters)
        getEntriesFromSIRS(schoolMap, rawDir, semesters)
    }

    override fun getSchoolStatsByProf(rawDataDir: Path): SchoolDeptsMap<Map<String, InstructorStats>> {
        return getInstructorStats(rawDataDir)
    }

    override fun getSchoolSchoolsData(statsByProf: SchoolDeptsMap<Map<String, InstructorStats>>): Map<String, School> {
        // TODO: should this be suspend?
        return runBlocking { getGeneralSchoolMap() }
    }

    override fun getSchoolAllInstructors(statsByProfDir: Path) =
        getCompleteSchoolDeptsMap<Map<String, InstructorStats>>(statsByProfDir)
            .getAllInstructors()

    override suspend fun getSchoolDeptNames() = getDeptNames()

    override suspend fun getSchoolCourseNames(
        statsByProfDir: Path,
        existingCourseNamesDir: Path?,
    ): SchoolDeptsMap<Map<String, String>> {
        return generateCourseNameMappings(
            latestSemester = currentSem,
            semestersBack = 5,
            schoolsDir = statsByProfDir,
            oldDataPath = existingCourseNamesDir,
        )
    }

    override suspend fun getSchoolTeachingProfs(statsByProfDir: Path, term: Semester.Double) =
        getTeachingData(readDir = statsByProfDir, term = term)
}

// this function shouldn't be called,
// but we declare it so that the functions it uses can be considered "used"
//@Suppress("unused", "FunctionName")
//private suspend fun `Overview of data gathering process`() {
//    val baseDir = Path.of("data-test")
//    val rawDir = baseDir / "raw"
//    val genDir = baseDir / "generated"
//    val statsByProfDir = genDir / "stats-by-prof"
//    val coreDir = genDir / "core"
//
//    val semesters = Semester.Double.valueOf(SemesterType.Fall, 2013)..
//            Semester.Double.valueOf(SemesterType.Spring, 2023)
//    val schoolMap = SIRSSource.getCompleteSchoolMap(semesters)
//    getEntriesFromSIRS(schoolMap, rawDir, semesters)
//
//    getInstructorStats(rawDir, statsByProfDir)
//    copyInstructorStatsWithoutStats(statsByProfDir, statsByProfDir.let { it.parent.resolve("${it.name}-cleaned") })
//
//    getDeptNames(coreDir / "dept-names")
//    generateCourseNameMappings(
//        latestSemester = Semester.Double.valueOf(SemesterType.Fall, 2023),
//        semestersBack = 5,
//        schoolsDir = statsByProfDir,
//        writeDir = coreDir / "course-names",
//        oldDataPath = Path.of("jsonData/old/courseNames") // TODO: inspect
//    )
////    getTeachingData(statsByProfDir, coreDir / "teaching-F23")
//}

fun writeNameMappingsToJson() {
    Path.of("nameMappings.txt").readText()
        .split("}\n")
        .map { deptStr ->
            val dept = deptStr.substringAfterBefore("\"", "\"")
            val namesMap = deptStr.lines().drop(2).dropLast(2).flatMap { line ->
                val value = line.substringAfterBefore("->", "//").trim(' ', '"')
                line.substringBefore("->")
                    .split("\", \"")
                    .map { it.trim(' ', '"') to value }
            }.toMap()
            dept to namesMap
        }.groupBy { it.first.substringBefore(':') }
        .mapValues { (_, v) ->
            v.toMap().mapKeys { (k, _) -> k.substringAfter(':') }
        }.forEach { (code, map) ->
            // or skip groupBy + mapValues & use "nameMappings/${code.replace(':','/').json"
            Path.of("jsonData/nameMappings-2/$code.json").writeAsJson(map)
        }
}