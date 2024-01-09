package io.github.opletter.courseevals.common

import io.github.opletter.courseevals.common.data.School
import io.github.opletter.courseevals.common.data.SchoolDeptsMap
import io.github.opletter.courseevals.common.remote.PathWrapper
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

inline fun <reified T> Path.decodeJson(): T = Json.decodeFromString(this.readText())
inline fun <reified T> Path.decodeJsonIfExists(): T? = if (exists()) decodeJson() else null
inline fun <reified T> Path.writeAsJson(value: T) = createParentDirectories().writeText(Json.encodeToString(value))

inline fun <reified T> getCompleteSchoolDeptsMap(dir: Path): SchoolDeptsMap<T> {
    val schoolsByCode = dir.resolve("schools.json").decodeJson<Map<String, School>>()
    return schoolsByCode.mapValues { (code, school) ->
        school.depts.associateWith { dir.resolve(code).resolve("$it.json").decodeJson<T>() }
    }
}

fun readResource(pathname: String): String {
    val stream = {}.javaClass.classLoader.getResource(pathname) ?: error("resource not found: $pathname")
    return stream.readText()
}

val PathWrapper.path: Path get() = Path.of(value)