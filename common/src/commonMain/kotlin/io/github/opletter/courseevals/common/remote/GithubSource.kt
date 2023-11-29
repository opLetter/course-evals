package io.github.opletter.courseevals.common.remote

import io.github.opletter.courseevals.common.data.Instructor
import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.data.School
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/** This class contains only the methods that are used by the website,
 * even though other data is also accessible from GitHub.
 * In practice, that data can just be accessed locally.
 */
class GithubSource(
    private val paths: WebsitePaths,
    repoPath: String = "opletter/course-evals-data",
    token: String? = null,
) : WebsiteDataSource {
    private val ghClient = HttpClient {
        // only use official api if needed for authentication, as it is rate limited
        install(ContentNegotiation) {
            if (token == null) json(Json, ContentType.Text.Plain) else json()
        }
        defaultRequest {
            url {
                protocol = URLProtocol.HTTPS
                host = if (token == null) "raw.githubusercontent.com" else "api.github.com"
                encodedPath = if (token == null) "/$repoPath/master/" else "/repos/$repoPath/contents/"
            }
            accept(ContentType.parse("application/vnd.github.raw"))
            token?.let { bearerAuth(it) }
        }
    }

    override suspend fun getStatsByProf(school: String, dept: String): Map<String, InstructorStats> =
        ghClient.get("${paths.statsByProfDir}/$school/$dept.json").body()

    override suspend fun getCourseNamesOrEmpty(school: String, dept: String): Map<String, String> {
        return try {
            ghClient.get("${paths.courseNamesDir}/$school/$dept.json").body()
        } catch (e: JsonConvertException) {
            emptyMap()
        }
    }

    override suspend fun getTeachingDataOrEmpty(school: String, dept: String): Map<String, List<String>> {
        return try {
            ghClient.get("${paths.teachingDataDir}/$school/$dept.json").body()
        } catch (e: JsonConvertException) {
            emptyMap()
        }
    }

    override suspend fun getAllInstructors(): Map<String, List<Instructor>> =
        ghClient.get(paths.allInstructorsFile.value).body()

    override suspend fun getDeptNames(): Map<String, String> = ghClient.get(paths.deptNamesFile.value).body()

    override suspend fun getSchoolsByCode(): Map<String, School> = ghClient.get(paths.schoolsByCodeFile.value).body()
}