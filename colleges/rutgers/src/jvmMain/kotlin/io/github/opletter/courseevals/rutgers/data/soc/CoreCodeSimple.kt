package io.github.opletter.courseevals.rutgers.data.soc


import kotlinx.serialization.Serializable

@Serializable
data class CoreCodeSimple(
    val campus: String,
    val code: String,
    val description: String,
    val id: Int,
    val label: String,
)