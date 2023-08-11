package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.remote.makeFileAndDir
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

inline fun <reified T> SchoolDeptsMap<T>.writeToFiles(
    writeDir: String,
    writeSchoolMap: Boolean = true,
): SchoolDeptsMap<T> {
    map { (key, value) ->
        key to School(
            code = key,
            name = campusMap[key] ?: error("No name for $key"),
            depts = value.keys.sorted().toSet(),
            campuses = setOf(Campus.valueOf(key.uppercase())),
            level = LevelOfStudy.U,
        )
    }.toMap().let {
        if (writeSchoolMap) makeFileAndDir("$writeDir/schools.json").writeText(Json.encodeToString(it))
    }
    forEachDept { school, dept, reports ->
        makeFileAndDir("$writeDir/$school/$dept.json")
            .writeText(Json.encodeToString(reports))
    }
    return this
}

fun readResource(pathname: String): String {
    val stream = {}.javaClass.classLoader.getResource(pathname) ?: error("resource not found: $pathname")
    return stream.readText()
}