package io.github.opletter.courseevals.rutgers

import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.github.opletter.courseevals.common.remote.makeFileAndDir
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File


suspend fun main(args: Array<String>) {
    if ("-teaching" in args)
        getTeachingData("jsonData/extraData/teachingF23")
//    getInstructorStats()
//    copyInstructorStatsWithoutStats()
//    getDeptNames()
//    generateCourseNameMappings(Semester.Double.valueOf(SemesterType.Fall, 2023))
//    getTeachingData("jsonData/extraData/teachingF23")

//    getGeneralSchoolMap().onEach { println(it) }
//        .let {
//            makeFileAndDir("jsonData/entries/schools.json")
//                .writeText(Json.encodeToString(it.toSortedMap().toMap()))
//        }
//    getCompleteSchoolDeptsMap<Map<String, InstructorStats>>("jsonData/statsByProf")
//        .printPossibleNameAdjustments(false)
}

fun writeNameMappingsToJson() {
    File("nameMappings.txt").readText()
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
            makeFileAndDir("jsonData/nameMappings-2/$code.json")
                .writeText(Json.encodeToString(map))
        }
}


