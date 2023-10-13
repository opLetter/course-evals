package io.github.opletter.courseevals.rutgers

import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.remote.getCompleteSchoolDeptsMap
import io.github.opletter.courseevals.common.remote.makeFileAndDir
import io.github.opletter.courseevals.common.remote.writeAsJson

fun SchoolDeptsMap<Map<String, InstructorStats>>.getAllInstructors(): Map<String, List<Instructor>> {
    return mapValues { (_, deptMap) ->
        deptMap.flatMap { (dept, statsByProf) ->
            statsByProf.map { (prof, stats) -> Instructor(prof, dept, stats.lastSem) }
        }
    }
}

fun getInstructorStats(readDir: String, writeDir: String): SchoolDeptsMap<Map<String, InstructorStats>> {
    return getCompleteSchoolDeptsMap<List<Entry>>(readDir)
        .semicolonCleanup()
        .mapEachDept { _, _, entries ->
            // filter out depts that aren't used recently to not clutter the UI
            // boundary chosen to be 6 years from present
            // entries are in semester order, so we only need to check the last one
            if (entries.last().semester < Semester.Double.valueOf(SemesterType.Fall, 2018))
                emptyMap()
            else entries.filterValid().mapByProfStats()
        }.filterNotEmpty()
        .also {
            it.writeToFiles(writeDir)
            makeFileAndDir("$writeDir/instructors.json").writeAsJson(it.getAllInstructors())
        }
}


// combines all schools & depts that have semicolons into same dirs/files
fun SchoolDeptsMap<List<Entry>>.semicolonCleanup(): SchoolDeptsMap<List<Entry>> {
    return entries.groupBy({ it.key.substringBefore(";") }, { it.value })
        .mapValues { (_, listOfMaps) ->
            listOfMaps.reduce { acc, map ->
                val newDepts = map.filterKeys { it !in acc.keys }
                acc.mapValues internalMap@{ (dept, entries) ->
                    val otherEntries = map[dept] ?: return@internalMap entries
                    entries + otherEntries
                } + newDepts
            }.entries
                .groupBy({ it.key.substringBefore(";") }, { it.value })
                .mapValues { it.value.flatten() }
        }
}