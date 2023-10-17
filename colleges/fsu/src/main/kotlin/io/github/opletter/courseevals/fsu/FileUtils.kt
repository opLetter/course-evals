package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.writeAsJson
import java.nio.file.Path

inline fun <reified T> SchoolDeptsMap<T>.writeToFiles(
    writeDir: Path,
    writeSchoolMap: Boolean = true,
): SchoolDeptsMap<T> {
    if (writeSchoolMap) {
        val schoolsData = map { (key, value) ->
            key to School(
                code = key,
                name = campusMap[key] ?: error("No name for $key"),
                depts = value.keys.sorted().toSet(),
                campuses = setOf(Campus.valueOf(key.uppercase())),
                level = LevelOfStudy.U,
            )
        }.toMap()
        writeDir.resolve("schools.json").writeAsJson(schoolsData)
    }
    forEachDept { school, dept, reports ->
        writeDir.resolve(school).resolve("$dept.json").writeAsJson(reports)
    }
    return this
}