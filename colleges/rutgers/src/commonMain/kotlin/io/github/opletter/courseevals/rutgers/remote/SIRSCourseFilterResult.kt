package io.github.opletter.courseevals.rutgers.remote

import kotlinx.serialization.Serializable

@Serializable
data class SIRSCourseFilterResult(
    val depts: List<String>, // empty if no value sent for "school" parameter, otherwise list of all depts
    val schools: List<List<String>>, // each sublist is size 2 in the form [code, name]
)