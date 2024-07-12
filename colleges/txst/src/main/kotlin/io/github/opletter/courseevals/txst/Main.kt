package io.github.opletter.courseevals.txst

import io.github.opletter.courseevals.common.SimpleSchoolDataApi
import io.github.opletter.courseevals.common.data.Semester
import io.github.opletter.courseevals.common.data.SemesterType
import io.github.opletter.courseevals.common.path
import io.github.opletter.courseevals.common.readResource
import io.github.opletter.courseevals.common.remote.WebsitePaths
import io.github.opletter.courseevals.common.runFromArgs
import io.github.opletter.courseevals.txst.remote.data.TXSTInstructor
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.div

suspend fun main(args: Array<String>) {
    TXSTApi.runFromArgs(args)
}

object TXSTApi : SimpleSchoolDataApi<Semester.Triple>() {
    override val depts: Set<String> = Prefixes.toSet()
    override val currentSem = Semester.Triple.valueOf(SemesterType.Fall, 2024)

    val defaultPaths = WebsitePaths("data-test")

    private val profsFileName = "profs.json"
    fun getProfsFromResource() = Json.decodeFromString<List<TXSTInstructor>>(readResource(profsFileName))

    override suspend fun getSchoolRawData() {
        // TODO: preserve existing profs
        getAndSaveBaseProfData(Path.of("src/main/resources") / profsFileName)
        getInstructorReports(defaultPaths.baseDir.path / "reports", getProfsFromResource())
    }

    override fun getSchoolStatsByProf(rawDataDir: Path) =
        getStatsByProf(rawDataDir, getProfsFromResource()).toSchoolMap()

    override fun getSchoolAllInstructors(statsByProfDir: Path) = getAllInstructors(statsByProfDir).toSchoolMap()

    override suspend fun getSchoolDeptNames() = getDeptNames(currentSem)

    // TODO: use existing course names
    override suspend fun getSchoolCourseNames(statsByProfDir: Path, existingCourseNamesDir: Path?) =
        getCourseNames(statsByProfDir, 2021..2023).toSchoolMap()

    override suspend fun getSchoolTeachingProfs(statsByProfDir: Path, term: Semester.Triple) =
        getTeachingProfs(statsByProfDir, term).toSchoolMap()
}

// For some reason, for fall the "year" part is  1 + the actual year
fun parseSemester(semester: Int): Semester.Triple {
    val type = when (semester % 100) {
        10 -> SemesterType.Fall
        30 -> SemesterType.Spring
        50 -> SemesterType.Summer
        else -> error("Invalid semester: $semester")
    }
    val year = semester / 100 - if (type == SemesterType.Fall) 1 else 0
    return Semester.Triple.valueOf(type, year)
}

fun Semester.Triple.toTXSTString(): String {
    val semStr = when (type) {
        SemesterType.Fall -> 10
        SemesterType.Spring -> 30
        SemesterType.Summer -> 50
    }.toString()
    val yearStr = (year + if (type == SemesterType.Fall) 1 else 0).toString()
    return "$yearStr$semStr"
}