package io.github.opletter.courseevals.rutgers.data.soc


import kotlinx.serialization.Serializable

@Serializable
data class CurrentTermDate(
    val campus: String,
    val date: String,
    val term: Int,
    val year: Int,
)