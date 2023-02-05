package io.github.opletter.courseevals.common.remote

import io.github.opletter.courseevals.common.data.Instructor
import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.data.School
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.json.*

/** This class contains only the methods that are used by the website,
 * even though other data is also accessible from GitHub.
 * In practice, that data can just be accessed locally.
 */
class GithubSource(
    repoPath: String = "DennisTsar/Rutgers-SIRS",
    private val paths: WebsitePaths = WebsitePaths(),
    ghToken: String? = null,
) : RemoteApi, WebsiteDataSource {
    private val ghClient = client.config {
        install(ContentNegotiation) {
            json()
        }
        defaultRequest {
            url {
                protocol = URLProtocol.HTTPS
                host = "api.github.com"
                encodedPath = "/repos/$repoPath/contents/"
            }
            accept(ContentType.parse("application/vnd.github.raw"))
            ghToken?.let { bearerAuth(it) }
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
        ghClient.get(paths.allInstructorsFile).body()

    override suspend fun getDeptMap(): Map<String, String> = ghClient.get(paths.deptMapFile).body()

    override suspend fun getSchoolMap(): Map<String, School> = ghClient.get(paths.schoolMapFile).body()

    companion object {
        val FakeSource = GithubSource(
            repoPath = "DennisTsar/RU-SIRS",
            paths = WebsitePaths(
                baseDir = "fake-data",
                allInstructorsFile = "fake-data/data-9-by-prof-stats/allInstructors.json" // will not be required soon
            )
        )
    }
}