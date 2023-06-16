package io.github.opletter.courseevals.common.remote

import io.github.opletter.courseevals.common.data.School
import io.github.opletter.courseevals.common.data.SchoolDeptsMap
import kotlinx.serialization.json.Json
import java.io.File

fun makeFileAndDir(filename: String): File = File(filename).apply { parentFile?.mkdirs() }

inline fun <reified T> File.decodeFromString(): T = Json.decodeFromString(this.readText())

inline fun <reified T> getCompleteSchoolDeptsMap(dir: String): SchoolDeptsMap<T> {
    val schoolsByCode = Json.decodeFromString<Map<String, School>>(File("$dir/schools.json").readText())
    return schoolsByCode.mapValues { (code, school) ->
        school.depts.associateWith {
            Json.decodeFromString(File("$dir/$code/$it.json").readText())
        }
    }
}