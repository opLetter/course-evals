package io.github.opletter.courseevals.rutgers

import io.github.opletter.courseevals.common.data.School
import io.github.opletter.courseevals.common.data.Semester
import io.github.opletter.courseevals.common.data.SemesterType
import io.github.opletter.courseevals.common.data.pmap
import io.github.opletter.courseevals.rutgers.remote.SIRSSource
import io.github.opletter.courseevals.rutgers.remote.SOCSource

suspend fun getGeneralSchoolMap(
    semester: Semester.Double = Semester.Double.valueOf(SemesterType.Fall, 2022),
): Map<String, School> {
    val sirsData = SIRSSource.getSchoolsOrDepts(semester)
    val socData = SOCSource.getSOCData()

    val sirsSchools = sirsData.schools.filter { it[1].isNotEmpty() }.map { it[0] }

    val requiredParensNames = socData.schools
        .map { it.description }
        .groupBy { it.substringBefore(" (") }
        .filterValues { it.size > 1 }
        .values
        .flatten()

    return socData.schools.toList().pmap { school ->
        if (school.code !in sirsSchools)
            return@pmap null
        val depts = (4028..4044).flatMap { num ->
            val mySemester = if (num % 2 == 0) SemesterType.Spring else SemesterType.Fall
            val myYear = num / 2
            SIRSSource.getSchoolsOrDepts(Semester.Double.valueOf(mySemester, myYear), school.code).depts
        }.filter { i -> i.isNotEmpty() }.sorted().toSet()

        val newName = school.description
            .let { if (it !in requiredParensNames) it.substringBefore(" (") else it }
            .replace("(UGrad)", "(U)").replace("(Grad)", "(G)")
        school.code to School(school.code, newName, depts, school.campuses, school.level)
    }.filterNotNull().toMap()
}