package io.github.opletter.courseevals.txst.remote.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InstructorDetails(
    @SerialName("displayname")
    val displayName: String,
    @SerialName("netid")
    val netId: String,
    val title: String,
    val departments: List<String>,
    val semesters: List<Int>,
)