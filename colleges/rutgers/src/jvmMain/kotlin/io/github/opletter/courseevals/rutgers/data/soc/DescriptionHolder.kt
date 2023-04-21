package io.github.opletter.courseevals.rutgers.data.soc

import kotlinx.serialization.Serializable

@Serializable
data class DescriptionHolder(
    val code: String,
    val description: String,
)