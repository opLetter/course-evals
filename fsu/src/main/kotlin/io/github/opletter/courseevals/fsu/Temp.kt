package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.data.School
import io.github.opletter.courseevals.common.data.mapEachDept
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun writeReportsBySchoolsAndDepts() {
    val list = File("test.txt").readLines()
        .map { AreaEntry.fromString(it) }

    val uniqueCodes = list.groupBy({ it.code }, { it.code }).filter { it.value.size == 1 }.keys

    val nodes = buildTree(list)
    val childParentMap = buildChildParentMap(nodes, uniqueCodes)

    CourseSearchKeys.flatMap {
        Json.decodeFromString<List<Report>>(File("json-data/reports-11-cleaned/$it.json").readText())
    }.groupBy { report ->
        childParentMap[report.area.filter { it != ',' }]
            ?: childParentMap[report.area.substringBefore(" - ", "")]
            ?: report.area.also { println("No parent for \"$it\"") }
    }.mapValues { (_, allReports) ->
        allReports.groupBy { it.area }
    }.writeToFiles("json-data/reports-12")
        .mapEachDept { _, _, reports ->
            val allNames = reports.map { it.htmlInstructor.uppercase() }.toSet() - ""

            val nameMappings = allNames.sorted().flatMap { name ->
                val (last, first) = name.split(", ")
                val lastParts = last.split(" ", "-")
                val matching = allNames.filter { otherName ->
                    val (otherLast, otherFirst) = otherName.split(", ")
                    val otherLastParts = otherLast.split(" ", "-")
                    (first.startsWith(otherFirst) || otherFirst.startsWith(first)) &&
                            (otherLastParts.any { it in lastParts } || lastParts.any { it in otherLastParts })
                }
                val chosen = matching.maxByOrNull { it.length } ?: return@flatMap emptyList()
                matching.map { it to chosen }
            }.toMap()

            reports
                .filter { it.htmlInstructor.uppercase().isNotBlank() }
                .groupBy {
                    nameMappings[it.htmlInstructor.uppercase()]
                        ?: throw IllegalStateException("${it.htmlInstructor.uppercase()}\n${nameMappings}")
                }
                .mapValues { (_, reports) ->
                    val filteredReports = reports.filter { it.questions.size >= 13 }.takeIf { it.isNotEmpty() }
                        ?: return@mapValues null
                    InstructorStats(
                        lastSem = reports.maxOf { FSUSemester.valueOf(it.term).numValue },
                        overallStats = filteredReports.getTotalRatings(),
                        courseStats = filteredReports.flatMap {
                            val allCourses = "[A-Z]{3}\\d{4}".toRegex().findAll(it.courseCode).map { it.value }.toSet()
                            allCourses.map { course -> course to it }
                        }.groupBy({ it.first }, { it.second })
                            .mapValues { (_, reports) -> reports.getTotalRatings() }
                    )
                }.filterValues { it != null }
        }.writeToFiles("json-data/reports-12-stats")

    createAllInstructors()
    createDeptNameMap()
}

fun String.formatForFileName(): String {
    return substringBefore(" - ", "").takeIf { it.isNotEmpty() }
        ?: filter { it.isUpperCase() }
}


fun createDeptNameMap(): Map<String, String> {
    val list = File("test.txt").readLines()
        .map { AreaEntry.fromString(it) }
        .associate {
            val key = it.code.takeIf { it.isNotEmpty() } ?: it.cleanName.formatForFileName()
            key to it.cleanName
        }.onEach { println(it) }
    val deptMap =
        Json.decodeFromString<Map<String, School>>(File("json-data/reports-12-stats/schoolMap.json").readText())
            .flatMap { it.value.depts }.toSet()
            .associateWith { a -> list[a] ?: "".also { println("unknown $a") } }
            .mapValues { (code, name) ->
                if (name == "") {
                    if (code.endsWith("Campus")) code.filter { it.isUpperCase() } else code
                } else name
            }.toSortedMap().toMap()
    makeFileAndDir("json-data/extra-data/deptNameMap.json")
        .writeText(Json.encodeToString(deptMap))
    return deptMap
}

fun cleanRawData(oldDir: String, newDir: String) {
    CourseSearchKeys.forEach { key ->
        val reports = Json.decodeFromString<List<Report>>(File("$oldDir/$key.json").readText())
        val newReports = reports.distinctBy { it.ids }.mapNotNull {
            if (it.area == "Florida State University") return@mapNotNull null // todo: temp solution
            val newArea = it.area
                .replace("HSFCS-", "HSFCS - ")
                .replace("ASCOP-", "ASCOP - ")
                .replace("HSRMP -", "ETRMP - ")
            it.takeIf { it.area == newArea } ?: it.copy(area = newArea)
        }
        makeFileAndDir("$newDir/$key.json").writeText(Json.encodeToString(newReports))
    }
}

fun getUsefulData(oldDir: String, newDir: String) {
    CourseCodes.forEach { key ->
        val reports = Json.decodeFromString<List<Report>>(File("$oldDir/$key.json").readText())
        val newReports = reports.distinctBy { it.ids }.map {
            UsefulReport(
                name = it.htmlInstructor.uppercase(),
                term = FSUSemester.valueOf(it.term).numValue,
                courseCode = it.courseCode,
                courseName = it.courseName,
                area = it.area,
                questions = it.questions.map { question ->
                    UsefulReport.UsefulQuestionData(
                        results = question.results,
                        numResponses = question.numResponses,
                        numRespondents = question.numRespondents,
                    )
                }
            )
        }
        makeFileAndDir("$newDir/$key.json").writeText(Json.encodeToString(newReports))
    }
}

fun organizeDataByDept(oldDir: String = "json-data/reports-9", newDir: String = "json-data/reports-10") {
    CourseSearchKeys
        .map { Json.decodeFromString<List<Report>>(File("$oldDir/$it.json").readText()) }
        .flatten()
        .groupBy { it.courseCode.take(3) }
        .forEach { (key, value) ->
            makeFileAndDir("$newDir/$key.json").writeText(Json.encodeToString(value))
        }
}


data class Entry(
    val term: String,
    val courseCode: String,
    val results: List<Int>,
)

@kotlinx.serialization.Serializable
data class UsefulReport(
    val name: String,
    val term: Int,
    val courseCode: String,
    val courseName: String,
    val area: String = "", // missing from <=reports-10
    val questions: List<UsefulQuestionData>,
) {
    @Serializable
    data class UsefulQuestionData(
        val results: List<Int>, // should be size 5 - num responses of each type
        val numResponses: Int,
        val numRespondents: Int,
    )
}