package io.github.opletter.courseevals.common

import io.github.opletter.courseevals.common.data.School
import io.github.opletter.courseevals.common.data.SchoolDeptsMap
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun makeFileAndDir(filename: String): File = File(filename).apply { parentFile?.mkdirs() }

inline fun <reified T> File.decodeJson(): T = Json.decodeFromString(this.readText())
inline fun <reified T> File.writeAsJson(value: T) = this.writeText(Json.encodeToString(value))

inline fun <reified T> getCompleteSchoolDeptsMap(dir: String): SchoolDeptsMap<T> {
    val schoolsByCode = File("$dir/schools.json").decodeJson<Map<String, School>>()
    return schoolsByCode.mapValues { (code, school) ->
        school.depts.associateWith { File("$dir/$code/$it.json").decodeJson() }
    }
}

fun readResource(pathname: String): String {
    val stream = {}.javaClass.classLoader.getResource(pathname) ?: error("resource not found: $pathname")
    return stream.readText()
}