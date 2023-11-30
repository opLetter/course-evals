package io.github.opletter.courseevals.common

import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.remote.WebsitePaths
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteRecursively

// functions use "School" prefix to avoid name collisions with school-specific implementations
// however, "write" functions could potentially drop the prefix since there shouldn't be collisions for those
interface SchoolDataApi<T : Semester<T>> {
    val currentSem: T

    suspend fun getSchoolRawData()

    fun getSchoolStatsByProf(rawDataDir: Path): SchoolDeptsMap<Map<String, InstructorStats>>
    fun getSchoolSchoolsData(statsByProf: SchoolDeptsMap<Map<String, InstructorStats>>): Map<String, School>
    fun getSchoolAllInstructors(statsByProfDir: Path): Map<String, List<Instructor>>
    suspend fun getSchoolDeptNames(): Map<String, String>
    suspend fun getSchoolCourseNames(
        statsByProfDir: Path,
        existingCourseNamesDir: Path?,
    ): SchoolDeptsMap<Map<String, String>>

    suspend fun getSchoolTeachingProfs(
        statsByProfDir: Path,
        term: T = currentSem,
    ): SchoolDeptsMap<Map<String, Set<String>>>

    fun writeSchoolStatsByProf(
        outputDir: Path,
        schoolsOutputFile: Path,
        rawDataDir: Path,
    ): SchoolDeptsMap<Map<String, InstructorStats>> {
        val statsByProf = getSchoolStatsByProf(rawDataDir).writeToFiles(outputDir)
        schoolsOutputFile.writeAsJson(getSchoolSchoolsData(statsByProf).toSortedMap().toMap())
        return statsByProf
    }

    fun writeSchoolAllInstructors(outputFile: Path, statsByProfDir: Path) =
        outputFile.writeAsJson(getSchoolAllInstructors(statsByProfDir).toSortedMap().toMap())

    suspend fun writeSchoolDeptNames(outputFile: Path) =
        outputFile.writeAsJson(getSchoolDeptNames().toSortedMap().toMap())

    suspend fun writeSchoolCourseNames(
        outputDir: Path,
        statsByProfDir: Path,
        existingCourseNamesDir: Path? = outputDir,
    ) = getSchoolCourseNames(statsByProfDir, existingCourseNamesDir).filterNotEmpty().writeToFiles(outputDir)

    suspend fun writeSchoolTeachingProfs(
        outputDir: Path,
        statsByProfDir: Path,
        term: T = currentSem,
    ) = getSchoolTeachingProfs(statsByProfDir, term).filterNotEmpty().writeToFiles(outputDir)
}


// for colleges with only one "school"
abstract class SimpleSchoolDataApi<T : Semester<T>> : SchoolDataApi<T> {
    abstract val depts: Set<String>

    override fun getSchoolSchoolsData(statsByProf: SchoolDeptsMap<Map<String, InstructorStats>>) =
        School("0", "All", depts, setOf(Campus.MAIN), LevelOfStudy.U).toSchoolMap()

    protected fun <T> T.toSchoolMap() = mapOf("0" to this)
}

suspend fun <T : Semester<T>> SchoolDataApi<T>.runFromArgs(args: Array<String>) {
    args.indexOf("--teaching").takeIf { it != -1 }?.let {
        writeSchoolTeachingProfs(outputDir = Path.of(args[it + 1]), statsByProfDir = Path.of(args[it + 2]))
    }
    args.indexOf("--write-all").takeIf { it != -1 }?.let {
        val paths = WebsitePaths(args[it + 1].lowercase())
        val rawDataDir = Path.of(args[it + 2].lowercase())
        writeAllProcessedData(paths, rawDataDir)
    }
}

suspend fun SchoolDataApi<*>.writeAllProcessedData(paths: WebsitePaths, rawDataDir: Path) {
    writeSchoolStatsByProf(paths.statsByProfDir.path, paths.schoolsByCodeFile.path, rawDataDir)
    writeSchoolAllInstructors(paths.allInstructorsFile.path, paths.statsByProfDir.path)
    writeSchoolDeptNames(paths.deptNamesFile.path)
    writeSchoolCourseNames(paths.courseNamesDir.path, paths.statsByProfDir.path)
    writeSchoolTeachingProfs(paths.teachingDataDir.path, paths.statsByProfDir.path)
}

@OptIn(ExperimentalPathApi::class)
inline fun <reified T> SchoolDeptsMap<T>.writeToFiles(outputDir: Path): SchoolDeptsMap<T> {
    outputDir.deleteRecursively()
    forEachDept { school, dept, reports ->
        outputDir.resolve(school).resolve("$dept.json").writeAsJson(reports)
    }
    return this
}

@JvmName("writeToFilesMap")
@OptIn(ExperimentalPathApi::class)
inline fun <reified T> SchoolDeptsMap<Map<String, T>>.writeToFiles(outputDir: Path): SchoolDeptsMap<Map<String, T>> {
    outputDir.deleteRecursively()
    forEachDept { school, dept, reports ->
        outputDir.resolve(school).resolve("$dept.json").writeAsJson(reports.toSortedMap().toMap())
    }
    return this
}