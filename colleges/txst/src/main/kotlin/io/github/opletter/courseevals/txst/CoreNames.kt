package io.github.opletter.courseevals.txst

import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.data.pmap
import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.github.opletter.courseevals.common.getCompleteSchoolDeptsMap
import io.github.opletter.courseevals.common.remote.DefaultClient
import io.github.opletter.courseevals.common.writeAsJson
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import java.nio.file.Path

// consider also using data from http://mycatalog.txstate.edu/courses/ or raw reports
suspend fun getCourseNames(readDir: Path, writeDir: Path) {
    val data = getCompleteSchoolDeptsMap<Map<String, InstructorStats>>(readDir).getValue("0")
    val courseNames = (2021..2023).pmap { getCourseNamesFromState(it.toString()) }
        .reduce { acc, map ->
            acc.mapValues { (prefix, names) ->
                (names.toMap() + map[prefix]?.toMap().orEmpty()).toList()
            } + map.filterKeys { it !in acc.keys }
        }
        .mapValues { (prefix, nameMap) ->
            val courses = data[prefix]?.flatMap { it.value.courseStats.keys }?.toSet().orEmpty()
            nameMap.filter { it.first in courses }.toMap()
        }.filterValues { it.isNotEmpty() }

    courseNames.forEach { (prefix, names) ->
        writeDir.resolve("0/$prefix.json").writeAsJson(names)
    }
}

private suspend fun getCourseNamesFromState(year: String): Map<String, List<Pair<String, String>>> {
    val payload = Parameters.build {
        append("DefaultYear", year)
        append("CourseYear", year)
        append("FICE", "003615")
        append("Rubric", "")
        append("CourseNum", "")
        append("CIP", "")
        append("CourseFunding", "")
        append("CourseLevel", "")
        append("courseactive", "")
        append("outputtype", "Screen")
        append("BAction", "Display Courses")
    }
    return DefaultClient.submitForm("http://www.txhighereddata.org/interactive/UnivCourse/search.cfm", payload)
        .bodyAsText()
        .lines()
        .filter {
            it.trimStart().startsWith("<td valign=\"top\" >") ||
                    it.trimStart().startsWith("<td valign=\"top\"  class=\"ClassB\">")
        }.chunked(4) { info ->
            val parts = info.map { it.substringAfterBefore(">", "<").trim() }
            Triple(parts[0].filter { it != ' ' }, parts[1], parts[3])
        }.groupBy({ it.first }, { it.second to it.third })
}

suspend fun getDeptNames(writeDir: Path?, term: String): Map<String, String> {
    val prefixNames = DefaultClient.submitForm(
        "https://ssb-prod.ec.txstate.edu/PROD/bwckgens.p_proc_term_date",
        Parameters.build {
            append("p_calling_proc", "bwckschd.p_disp_dyn_sched")
            append("p_term", term)
        }
    ).bodyAsText()
        .lines()
        .dropWhile { it != "<select name=\"sel_subj\" size=\"10\" MULTIPLE ID=\"subj_id\">" }
        .drop(1)
        .takeWhile { it.startsWith("<OPTION") }
        .associate {
            it.substringAfterBefore("VALUE=\"", "\"").filter { it != ' ' } to
                    it.substringAfterBefore(">", "<").replace("&amp;", "&")
        }.filterKeys { it in Prefixes }
    check(prefixNames.size == Prefixes.size)
    writeDir?.resolve("dept-names.json")?.writeAsJson(prefixNames.toSortedMap().toMap())
    return prefixNames
}