package io.github.opletter.courseevals.usf

import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.decodeJsonIfExists
import io.github.opletter.courseevals.common.getCompleteSchoolDeptsMap
import io.github.opletter.courseevals.common.readResource
import java.nio.file.Path

suspend fun getDeptNames(): Map<String, String> {
    // some depts are no longer in use, but we still want the names
    // acquired from https://usfonline.admin.usf.edu/pls/prod/bwckschd.p_disp_dyn_sched
    val presetNames = mapOf(
        "ASH" to "Asian History",
        "EED" to "Education: Emotional Disordrs",
        "FOW" to "For & Bibl Lang, Comp Lit",
        "HBR" to "Modern Hebrew Language",
        "ISC" to "Interdisciplinary Sciences",
        "ISE" to "Not Used in Scns",
        "SLA" to "Second Language Acquisition",
        "SMT" to "Science or Mathematics Teaching",
        "SPT" to "Spanish Culture in Translation or Translation Skills",
    )
    val curNames = getCourseData().associate { it.prefix to it.courseType.substringAfter(" - ") }
    val prefixNames = (presetNames + curNames).filterKeys { it in Prefixes }
    check(Prefixes.all { it in prefixNames }) { "Missing prefix names: ${Prefixes - prefixNames.keys}" }
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
    statsByProfDir: Path,
    existingCourseNamesDir: Path? = null,
): Map<String, Map<String, String>> {
    val fromUSF = getCourseNamesFromUSF()
    val fromCsv = getCourseNamesFromCsv()
    val statsByProf = getCompleteSchoolDeptsMap<Map<String, InstructorStats>>(statsByProfDir).getValue("0")

    // implementation reused for FSU
    return (fromCsv + fromUSF)
        .filterKeys { it in Prefixes }
        .mapValues { (key, value) ->
            val goodNames = value.filterValues { !it.allLettersUpperCase() }
            val existingGoodNames = existingCourseNamesDir?.resolve("0/$key.json")
                ?.decodeJsonIfExists<Map<String, String>>()
                ?.filterValues { !it.allLettersUpperCase() }
                .orEmpty()
            val combined = fromCsv[key].orEmpty() + value + existingGoodNames + goodNames
            val courseWithData = statsByProf.getValue(key).flatMap { it.value.courseStats.keys }.toSet()

            combined.filterKeys { it in courseWithData }
        }
}

private fun String.allLettersUpperCase() = all { !it.isLetter() || it.isUpperCase() }