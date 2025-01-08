package io.github.opletter.courseevals.usf

import io.github.opletter.courseevals.common.SimpleSchoolDataApi
import io.github.opletter.courseevals.common.data.Semester
import io.github.opletter.courseevals.common.data.SemesterType
import io.github.opletter.courseevals.common.path
import io.github.opletter.courseevals.common.remote.WebsitePaths
import io.github.opletter.courseevals.common.runFromArgs
import java.nio.file.Path
import kotlin.io.path.div

suspend fun main(args: Array<String>) {
    USFApi.getSchoolDeptNames()
    return
    USFApi.runFromArgs(args)
}

object USFApi : SimpleSchoolDataApi<Semester.Triple>() {
    val defaultPaths = WebsitePaths("data-test")

    override val depts: Set<String> = Prefixes.toSet()
    override val currentSem = Semester.Triple.valueOf(SemesterType.Spring, 2025)

    override suspend fun getSchoolRawData() {
        // IMPORTANT: Make sure to set `VIEWSTATE` and `EVENTVALIDATION` before running this
        val reportsDir = defaultPaths.baseDir.path / "reports"
        getData(reportsDir)
    }

    override fun getSchoolStatsByProf(rawDataDir: Path) = getStatsByProf(getReportsFromFiles(rawDataDir)).toSchoolMap()

    override fun getSchoolAllInstructors(statsByProfDir: Path) = getAllInstructors(statsByProfDir).toSchoolMap()

    override suspend fun getSchoolDeptNames() = getDeptNames()

    override suspend fun getSchoolCourseNames(statsByProfDir: Path, existingCourseNamesDir: Path?) =
        getCompleteCourseNames(statsByProfDir, existingCourseNamesDir).toSchoolMap()

    override suspend fun getSchoolTeachingProfs(statsByProfDir: Path, term: Semester.Triple) =
        getTeachingProfs(statsByProfDir, term).toSchoolMap()
}