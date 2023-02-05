package io.github.opletter.courseevals.common.remote

import io.github.opletter.courseevals.common.data.Instructor
import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.data.School

interface WebsiteDataSource {
    suspend fun getStatsByProf(school: String, dept: String): Map<String, InstructorStats>
    suspend fun getCourseNamesOrEmpty(school: String, dept: String): Map<String, String>
    suspend fun getTeachingDataOrEmpty(school: String, dept: String): Map<String, List<String>>

    suspend fun getAllInstructors(): Map<String, List<Instructor>>
    suspend fun getDeptMap(): Map<String, String>
    suspend fun getSchoolMap(): Map<String, School>
}

/** Useful for testing */
suspend fun WebsiteDataSource.getAllData(school: String, dept: String, print: Boolean = true) {
    val data = listOf(
        getSchoolMap(),
        getAllInstructors(),
        getDeptMap(),
        getStatsByProf(school, dept),
        getCourseNamesOrEmpty(school, dept),
        getTeachingDataOrEmpty(school, dept),
    ).onEach { require(it.isNotEmpty()) }
    if (print) data.forEach { println(it) }
}
