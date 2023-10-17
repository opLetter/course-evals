package io.github.opletter.courseevals.usf

import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.decodeJson
import io.github.opletter.courseevals.common.readResource
import io.github.opletter.courseevals.common.writeAsJson
import java.nio.file.Path

suspend fun getDeptNames(writeDir: Path?): Map<String, String> {
    // some depts are no longer in use, but we still want the names
    // acquired from https://usfonline.admin.usf.edu/pls/prod/bwckschd.p_disp_dyn_sched
    val presetNames = mapOf(
        "EAP" to "English for Foreign Students",
        "EED" to "Education: Emotional Disordrs",
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
    return prefixNames.also {
        writeDir?.resolve("dept-names.json")?.writeAsJson(it.toSortedMap().toMap())
    }
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
    readDir: Path,
    writeDir: Path?,
): Map<String, Map<String, String>> {
    val fromUSF = getCourseNamesFromUSF()
    val fromCsv = getCourseNamesFromCsv()
    val combined = fromCsv + fromUSF.mapValues { (key, value) -> fromCsv[key]?.plus(value) ?: value }

    return combined
        .filterKeys { it in Prefixes }
        .mapValues { (key, subMap) ->
            val courseWithData = readDir.resolve("0/$key.json")
                .decodeJson<Map<String, InstructorStats>>()
                .flatMap { it.value.courseStats.keys }
                .toSet()
            subMap.filterKeys { it in courseWithData }
        }.onEach { (prefix, data) ->
            writeDir?.resolve("0/$prefix.json")?.writeAsJson(data.toSortedMap().toMap())
        }
}