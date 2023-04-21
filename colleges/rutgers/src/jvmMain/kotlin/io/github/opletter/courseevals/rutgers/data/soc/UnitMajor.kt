package io.github.opletter.courseevals.rutgers.data.soc

import kotlinx.serialization.Serializable

@Serializable
data class UnitMajor(
    val majorCode: String,
    val unitCode: String,
)