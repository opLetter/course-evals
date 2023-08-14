package io.github.opletter.courseevals.usf

import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.remote.makeFileAndDir
import io.github.opletter.courseevals.common.remote.readResource
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

suspend fun getDeptNames(writeDir: String?): Map<String, String> {
    // some depts are no longer in use, but we still want the names
    // acquired from https://usfonline.admin.usf.edu/pls/prod/bwckschd.p_disp_dyn_sched
    val presetNames = mapOf(
        "CJT" to "Criminal Justice Technologies",
        "EED" to "Education: Emotional Disordrs",
        "ELR" to "Electrical Labs & Related Areas",
        "ETI" to "Engineering Technology: Indst",
        "FOW" to "For & Bibl Lang, Comp Lit",
        "HBR" to "Modern Hebrew Language",
        "ISC" to "Interdisciplinary Sciences",
        "ISE" to "Not Used in Scns",
        "SLA" to "Second Language Acquisition",
    )
    val curNames = getCourseData().associate { it.prefix to it.courseType.substringAfter(" - ") }
    val prefixNames = (presetNames + curNames).filter { it.key in Prefixes }
    check(Prefixes.all { it in prefixNames }) {
        "not all prefixes have names: ${Prefixes.filter { it !in prefixNames }}"
    }
    writeDir?.let {
        makeFileAndDir("$it/dept-names.json")
            .writeText(Json.encodeToString(prefixNames.toSortedMap().toMap()))
    }
    return prefixNames
}

private suspend fun getCourseNamesFromUSF(): Map<String, Map<String, String>> {
    return getCourseData()
        .groupBy { it.prefix }
        .mapValues { (_, data) ->
            data.associate { it.code to it.courseName }
        }
}

// CSV downloaded from https://flscns.fldoe.org/PbCourseDescriptions.aspx
private fun getCourseNamesFromCsv(): Map<String, Map<String, String>> {
    return readResource("CourseDescriptions.csv")
        .split("USF,")
        .drop(1)
        .groupBy { it.take(3) }
        .mapValues { (_, data) ->
            data.associate { line ->
                val name = line.drop(4).substringAfter(",").let {
                    if (it.startsWith("\"")) it.drop(1).substringBefore("\"")
                    else it.substringBefore(",")
                }
                line.drop(4).takeWhile { it != ' ' && it != ',' } to name
            }
        }
}

suspend fun getCompleteCourseNames(
    readDir: String,
    writeDir: String?,
): Map<String, Map<String, String>> {
    val fromUSF = getCourseNamesFromUSF()
    val fromCsv = getCourseNamesFromCsv()
    val combined = fromCsv + fromUSF.mapValues { (key, value) -> fromCsv[key]?.plus(value) ?: value }

    return combined
        .filterKeys { it in Prefixes }
        .mapValues { (key, subMap) ->
            val courseWithData = File("$readDir/0/$key.json")
                .let { Json.decodeFromString<Map<String, InstructorStats>>(it.readText()) }
                .flatMap { it.value.courseStats.keys }
                .toSet()
            subMap.filterKeys { it in courseWithData }
        }.onEach { (prefix, data) ->
            makeFileAndDir("$writeDir/0/$prefix.json")
                .writeText(Json.encodeToString(data.toSortedMap().toMap()))
        }
}