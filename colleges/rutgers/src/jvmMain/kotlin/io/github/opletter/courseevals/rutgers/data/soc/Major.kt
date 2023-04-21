package io.github.opletter.courseevals.rutgers.data.soc

import kotlinx.serialization.Serializable

@Serializable
data class Major(
    val code: String,
    val isMajorCode: Boolean,
    val isUnitCode: Boolean,
)