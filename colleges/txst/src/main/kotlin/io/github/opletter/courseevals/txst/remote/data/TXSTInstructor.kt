package io.github.opletter.courseevals.txst.remote.data

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@Serializable
data class InstructorResponse(val instructors: List<TXSTInstructor>)

@Serializable
data class TXSTInstructor(
    val title: String,
    @SerialName("displayname")
    val displayName: String,
    @OptIn(ExperimentalSerializationApi::class)
    @JsonNames("plid", "hplid")
    val plid: String,
)