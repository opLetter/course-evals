package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.remote.makeFileAndDir
import io.github.opletter.courseevals.common.remote.writeAsJson

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
        if (writeSchoolMap) makeFileAndDir("$writeDir/schools.json").writeAsJson(it)
    }
    forEachDept { school, dept, reports ->
        makeFileAndDir("$writeDir/$school/$dept.json").writeAsJson(reports)
    }
    return this
}