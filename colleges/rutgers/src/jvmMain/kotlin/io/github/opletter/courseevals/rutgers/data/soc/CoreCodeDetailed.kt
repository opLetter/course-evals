package io.github.opletter.courseevals.rutgers.data.soc

import kotlinx.serialization.Serializable

@Serializable
data class CoreCodeDetailed(
    val code: String,
    val coreCode: String,
    val coreCodeDescription: String,
    val coreCodeReferenceId: String,
    val course: String,
    val description: String,
    val effective: String,
    val id: String,
    val lastUpdated: Long,
    val offeringUnitCampus: String,
    val offeringUnitCode: String,
    val subject: String,
    val supplement: String,
    val term: String,
    val unit: String,
    val year: String,
)