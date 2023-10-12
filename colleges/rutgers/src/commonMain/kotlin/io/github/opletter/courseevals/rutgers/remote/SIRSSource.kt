package io.github.opletter.courseevals.rutgers.remote

import io.github.opletter.courseevals.common.data.Semester
import io.github.opletter.courseevals.common.data.pmap
import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.github.opletter.courseevals.rutgers.Entry
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import kotlinx.serialization.json.Json

object SIRSSource {
    private val sirsClient = HttpClient {
        followRedirects = false
        install(Logging) {
            logger = Logger.SIMPLE
        }
        defaultRequest {
            headers["Cookie"] = SIRS_API_KEY
            url("https://sirs.rutgers.edu")
        }
    }

    suspend fun loggedIn(): Boolean {
        return try {
            sirsClient.get("/index.php").status == HttpStatusCode.OK
        } catch (e: Throwable) { // js seems to throw Throwable for CORS restrictions
            false
        }
    }

    private suspend fun HttpResponse.mapSIRSPageToEntries(): List<Entry> =
        body<String>().split("<strong>  ").drop(1).map(::Entry)

    suspend fun getEntriesByDeptOrCourse(
        semester: Semester.Double,
        school: String,
        dept: String,
        course: String? = null,
    ): List<Entry> {
        return sirsClient.get("/index.php") {
            parameter("survey[semester]", semester.type)
            parameter("survey[year]", semester.year)
            parameter("survey[school]", school)
            parameter("survey[dept]", dept)
            parameter("survey[course]", course)
            parameter("mode", "course")
        }.mapSIRSPageToEntries()
    }

    suspend fun getEntriesByLastName(
        lastname: String,
        semester: Semester.Double? = null,
        school: String? = null,
        dept: String? = null,
    ): List<List<String>> {
        return sirsClient.get("/index.php") {
            parameter("survey[lastname]", lastname)
            parameter("survey[semester]", semester?.type)
            parameter("survey[year]", semester?.year)
            parameter("survey[school]", school)
            parameter("survey[dept]", dept)
            parameter("mode", "name")
        }.body<String>().substringAfterBefore("</strong></li><li>", "</a></li></ul>").split("</a></li><li>")
            .map { // this should be an object but no use for it rn
                val id = it.substringAfterBefore("record%5D=", "'")
                val (sem, course, prof) = it.substringAfter(">").split(" &mdash; ")
                listOf(id, sem, course, prof)
            }
    }

    suspend fun getEntriesByID(id: String): List<Entry> {
        return sirsClient.get("/index.php") {
            parameter("survey[record]", id)
            parameter("mode", "name")
        }.mapSIRSPageToEntries()
    }

    suspend fun getEntriesOverSems(
        school: String,
        dept: String,
        semesters: List<Semester.Double>,
    ): List<Entry> = semesters.pmap { getEntriesByDeptOrCourse(it, school, dept) }.flatten()

    suspend fun getSchoolsOrDepts(
        semester: Semester.Double,
        school: String = "",
    ): SIRSCourseFilterResult {
        return sirsClient.config {
            install(ContentNegotiation) {
                serialization(ContentType.Text.Html, Json)
            }
        }.get("/courseFilter.php") {
            parameter("survey[semester]", semester.type)
            parameter("survey[year]", semester.year)
            parameter("survey[school]", school)
//            parameter("mode", "course") // sent by default but doesn't do anything?
        }.body()
    }

    private suspend fun getSpecificSchoolMap(semester: Semester.Double): Map<String, Set<String>> {
        // each subList of "schools" is size 2 (code, name)
        return getSchoolsOrDepts(semester).schools.pmap { (code, _) ->
            val depts = getSchoolsOrDepts(semester, code).depts.toSet()
            code to depts
        }.toMap()
    }

    /** Get a map of school codes to their departments over */
    suspend fun getCompleteSchoolMap(semesters: List<Semester.Double>): Map<String, Set<String>> {
        return semesters
            .pmap { getSpecificSchoolMap(it) }
            .reduce { acc, schoolMap ->
                val newDepts = schoolMap.filterKeys { it !in acc.keys }
                acc.mapValues { (code, depts) ->
                    val otherSchool = schoolMap[code] ?: return@mapValues depts
                    (otherSchool + depts).sorted().toSet()
                } + newDepts
            }
    }
}