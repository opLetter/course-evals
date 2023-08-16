package io.github.opletter.courseevals.txst.remote

import io.github.opletter.courseevals.txst.remote.data.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import kotlinx.serialization.json.Json

private val client = HttpClient {
    install(ContentNegotiation) {
        serialization(ContentType.Text.Plain, Json { ignoreUnknownKeys = true })
    }
    install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.INFO
    }
    defaultRequest {
        url {
            protocol = URLProtocol.HTTPS
            host = "api.hb2504.txst.edu"
        }
    }
}

suspend fun getBaseProfData(): List<TXSTInstructor> {
    return ('A'..'Z').flatMap { letter ->
        client.get("/py/getinstructorsforletter.py") { parameter("letter", letter) }
            .body<InstructorResponse>()
            .instructors
    }
}

suspend fun getInstructorDetails(id: String): InstructorDetails {
    return client.get("/py/getinstructordetails.py") { parameter("plid", id) }.body()
}

suspend fun getClassesForInstructor(instructorId: String, semester: Int): ClassesResponse {
    return client.get("/py/getclassesforinstructor.py") {
        parameter("plid", instructorId)
        parameter("semester", semester)
    }.body()
}

suspend fun getRatings(classCode: Long): Report<SurveyResponse> {
    return client.get("/py/getspiforclass.py") {
        parameter("class", classCode)
    }.body()
}