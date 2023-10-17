package io.github.opletter.courseevals.rutgers.remote

import io.github.opletter.courseevals.common.data.Campus
import io.github.opletter.courseevals.common.data.Semester
import io.github.opletter.courseevals.common.data.SemesterType
import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.github.opletter.courseevals.rutgers.data.soc.Course
import io.github.opletter.courseevals.rutgers.data.soc.SOCData
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.compression.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

private const val SOC_BASE_URL = "https://sis.rutgers.edu/soc"

object SOCSource {
    private val client = HttpClient()
    suspend fun getCourses(
        semester: Semester.Double,
        campus: Campus,
    ): List<Course> {
        return client.config {
            install(ContentEncoding) {
                gzip(1.0F)
            }
            install(ContentNegotiation) {
                json()
            }
        }.get("$SOC_BASE_URL/api/courses.json") {
            parameter("term", if (semester.type == SemesterType.Fall) 9 else 1)
            parameter("year", semester.year)
            parameter("campus", campus)
        }.body()
    }

    suspend fun getSOCData(): SOCData {
        return client.get(SOC_BASE_URL).bodyAsText()
            .substringAfterBefore("<div id=\"initJsonData\" style=\"display:none;\">", "</div>")
            .let { Json.decodeFromString(it) }
    }
}

suspend fun SOCSource.getCoursesOverTime(
    latestSemester: Semester.Double,
    semestersBack: Int = 5,
): List<Course> {
    return listOf(Campus.NB, Campus.CM, Campus.NK).flatMap { campus ->
        (semestersBack - 1 downTo 0).flatMap {
            getCourses(latestSemester.prev(it), campus)
        }
    }
}