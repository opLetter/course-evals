package io.github.opletter.courseevals.rutgers

import io.github.opletter.courseevals.common.data.Semester
import io.github.opletter.courseevals.common.data.SemesterType
import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.github.opletter.courseevals.common.writeAsJson
import io.github.opletter.courseevals.rutgers.remote.SIRSSource
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.name
import kotlin.io.path.readText


suspend fun main(args: Array<String>) {
    args.indexOf("-teaching").takeIf { it != -1 }?.let {
        getTeachingData(
            readDir = Path.of(args[it + 1]),
            writeDir = Path.of(args[it + 2]),
            term = Semester.Double.valueOf(SemesterType.Spring, 2024)
        )
    }
}

// this function shouldn't be called,
// but we declare it so that the functions it uses can be considered "used"
@Suppress("unused", "FunctionName")
private suspend fun `Overview of data gathering process`() {
    val baseDir = Path.of("data-test")
    val rawDir = baseDir / "raw"
    val processedDir = baseDir / "processed"
    val statsByProfDir = processedDir / "stats-by-prof"
    val coreDir = processedDir / "core"

    val semesters = Semester.Double.valueOf(SemesterType.Fall, 2013)..
            Semester.Double.valueOf(SemesterType.Spring, 2023)
    val schoolMap = SIRSSource.getCompleteSchoolMap(semesters)
    getEntriesFromSIRS(schoolMap, rawDir, semesters)

    getInstructorStats(rawDir, statsByProfDir)
    copyInstructorStatsWithoutStats(statsByProfDir, statsByProfDir.let { it.parent.resolve("${it.name}-cleaned") })

    getDeptNames(coreDir / "dept-names")
    generateCourseNameMappings(
        latestSemester = Semester.Double.valueOf(SemesterType.Fall, 2023),
        semestersBack = 5,
        schoolsDir = statsByProfDir,
        writeDir = coreDir / "course-names",
        oldDataPath = Path.of("jsonData/old/courseNames") // TODO: inspect
    )
//    getTeachingData(statsByProfDir, coreDir / "teaching-F23")
}

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