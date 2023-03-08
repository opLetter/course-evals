package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.common.data.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun makeFileAndDir(filename: String): File = File(filename).apply { parentFile.mkdirs() }

inline fun <reified T> File.decodeFromString(): T = Json.decodeFromString(this.readText())

inline fun <reified T> SchoolDeptsMap<T>.writeToFiles(
    writeDir: String,
    writeSchoolMap: Boolean = true,
): SchoolDeptsMap<T> {
    map { (key, value) ->
        key to School(
            code = key,
            name = campusMap[key] ?: throw IllegalStateException("No name for $key"),
            depts = value.keys.sorted().toSet(),
            campuses = Campus.values().toSet(),
            level = LevelOfStudy.U,
        )
    }.toMap().let {
        if (writeSchoolMap) makeFileAndDir("$writeDir/schoolMap.json").writeText(Json.encodeToString(it))
    }
    forEachDept { school, dept, reports ->
        makeFileAndDir("$writeDir/$school/$dept.json")
            .writeText(Json.encodeToString(reports))
    }
    return this
}

inline fun <reified T> getCompleteSchoolDeptsMap(dir: String): SchoolDeptsMap<T> {
    val schoolMap = Json.decodeFromString<Map<String, School>>(File("$dir/schoolMap.json").readText())
    return schoolMap.mapValues { (code, school) ->
        school.depts.associateWith {
            Json.decodeFromString(File("$dir/$code/$it.json").readText())
        }
    }
}