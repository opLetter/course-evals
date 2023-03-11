package io.github.opletter.courseevals.common.data

import kotlinx.serialization.Serializable

@Serializable
data class Instructor(
    val name: String,
    val dept: String,
    val latestSem: Int,
)