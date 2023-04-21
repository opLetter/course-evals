package io.github.opletter.courseevals.rutgers

import io.github.opletter.courseevals.common.data.Semester
import io.github.opletter.courseevals.common.remote.makeFileAndDir
import io.github.opletter.courseevals.rutgers.remote.SIRSSource
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

// The code I last used to get all the data
//val semesters = Semester.Double.valueOf(SemesterType.Fall, 2013)..Semester.Double.valueOf(SemesterType.Fall, 2022)
//val schoolMap = SIRSSource.getCompleteSchoolMap(semesters)
//getEntriesFromSIRS(schoolMap, "jsonData/entries", semesters)
suspend fun getEntriesFromSIRS(
    sirsSchools: Map<String, Set<String>>,
    writeDir: String,
    semesters: List<Semester.Double>,
) {
    sirsSchools.forEach { (schoolCode, depts) ->
        // ensures depts only present in extraEntries are preserved
        depts.forEach depts@{ dept ->
            // Wanted to make this more async but that breaks Rutgers servers
            val entries = SIRSSource.getEntriesOverSems(schoolCode, dept, semesters)

            if (entries.isEmpty()) return@depts

            val deptStr = dept.replace(":", "sc") // ensure valid filename
            makeFileAndDir("$writeDir/$schoolCode/$deptStr.json")
                .writeText(Json.encodeToString(entries))
        }
    }
}