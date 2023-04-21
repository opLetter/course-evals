package io.github.opletter.courseevals.rutgers.data.soc


import kotlinx.serialization.Serializable

@Serializable
data class Building(
    val campus: String,
    val code: String,
    val id: String,
    val name: String,
)