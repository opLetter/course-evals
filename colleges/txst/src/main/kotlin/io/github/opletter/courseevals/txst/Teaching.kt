package io.github.opletter.courseevals.txst

import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.data.Semester
import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.github.opletter.courseevals.common.decodeJson
import io.github.opletter.courseevals.common.remote.DefaultClient
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.cookies.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.serialization.json.*
import java.nio.file.Path

// Code copied from USF which uses the same api
// TOOD: remove duplication

class TeachingData(
    val subject: String,
    val course: String,
    val prof: String,
)

private val client = DefaultClient.config {
    install(HttpCookies)
    install(ContentNegotiation) {
        json()
    }
}

suspend fun getTeachingData(term: String): List<TeachingData> {
    val baseUrl = "https://reg-prod.ec.txstate.edu/StudentRegistrationSsb/ssb"
    val maxSize = 500 // API limit

    client.post("$baseUrl/term/search") {
        parameter("mode", "search")
        contentType(ContentType.Application.FormUrlEncoded)
        setBody(parametersOf("term", term).formUrlEncode())
    }


    fun JsonElement.parseTeachingData(): TeachingData? {
        val prof = jsonObject["faculty"]!!.jsonArray.singleOrNull {
            it.jsonObject["primaryIndicator"]!!.jsonPrimitive.boolean
        } ?: return null
        return TeachingData(
            subject = jsonObject["subject"]!!.jsonPrimitive.content,
            course = jsonObject["courseNumber"]!!.jsonPrimitive.content,
            prof = prof.jsonObject["displayName"]!!.jsonPrimitive.content,
        )
    }

    return flow {
        var offset = 0
        while (true) {
            val res = client.get("$baseUrl/searchResults/searchResults") {
                parameter("txt_term", term)
                parameter("startDatepicker", "")
                parameter("endDatepicker", "")
                parameter("pageOffset", offset)
                parameter("pageMaxSize", maxSize)
                parameter("sortColumn", "subjectDescription")
                parameter("sortDirection", "asc")
            }.body<JsonObject>()

            emitAll(res["data"]!!.jsonArray.mapNotNull { it.parseTeachingData() }.asFlow())
            offset += maxSize
            if (offset >= res["totalCount"]!!.jsonPrimitive.int) break
        }
    }.toList()
}

suspend fun getTeachingProfs(statsByProfDir: Path, term: Semester.Triple): Map<String, Map<String, Set<String>>> {
    return getTeachingData(term.toTXSTString())
        .filter { it.prof != "Faculty, Unassigned" }
        .groupBy { it.subject }
        .filterKeys { it in Prefixes }
        .mapValues { processSubjectData(statsByProfDir, it.key, it.value) }
        .also { teachingMap ->
            val profCount = teachingMap.values.sumOf { subjectMap ->
                subjectMap.keys.count { it[0].isLetter() }
            }
            val courseWithMultipleProfs = teachingMap.values.sumOf { subjectMap ->
                subjectMap.filterKeys { it[0].isDigit() }.values.count { it.size > 1 }
            }
            println("profCount: $profCount, courseWithMultipleProfs: $courseWithMultipleProfs")
        }
}

private fun processSubjectData(
    statsByProfDir: Path,
    subject: String,
    teachingData: List<TeachingData>,
): Map<String, Set<String>> {
    val existingInstructors = statsByProfDir.resolve("0/$subject.json")
        .decodeJson<Map<String, InstructorStats>>()
        .keys

    val teachingInstructors = teachingData.mapNotNull { data ->
        // Teaching name is formatted as "Paul Smith, John" while stats name is formatted as "Smith, John Paul"
        val foundName = existingInstructors
            .singleOrNull { it.normalizeFull() == data.prof.normalizeFull() }
            ?: existingInstructors.singleOrNull { it.normalizeFirstList() == data.prof.normalizeFirstList() }
            ?: existingInstructors.singleOrNull {
                // There's a good amount of inconsistency in first names, so just compare the last name + first initial
                it.take(it.indexOf(',') + 3) == data.prof.take(data.prof.indexOf(',') + 3)
            }
        foundName?.let { it to data.course }
    }

    val coursesToProfs = teachingInstructors
        .groupBy({ it.second }, { it.first })
        .mapValues { it.value.toSortedSet() }

    val profToCourses = coursesToProfs.flatMap { (course, profs) ->
        profs.map { it to course }
    }.groupBy({ it.first }, { it.second }).mapValues { it.value.toSortedSet() }

    return coursesToProfs + profToCourses
}

private fun String.normalizeFirstList(ignoreMiddle: Boolean = true): String {
    return if (!ignoreMiddle || ',' !in this) {
        this.uppercase().filter { it.isLetter() }
    } else {
        val last = this.substringBefore(',')
        val first = this.substringAfterBefore(", ", " ")
        (last + first).normalizeFirstList()
    }
}

private fun String.normalizeFull(ignoreMiddle: Boolean = true): String {
    return if (!ignoreMiddle || ',' !in this) {
        this.uppercase().filter { it.isLetter() }
    } else {
        val last = this.substringBefore(',')
        val first = this.substringAfter(", ")
        (first + last).normalizeFull()
    }
}