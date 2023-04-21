package io.github.opletter.courseevals.rutgers.data.soc

import kotlinx.serialization.Serializable

@Serializable
data class CrossListedSections(
    val courseNumber: String,
    val offeringUnitCampus: String,
    val offeringUnitCode: String,
    val primaryRegistrationIndex: String,
    val registrationIndex: String,
    val sectionNumber: String,
    val subjectCode: String,
    val supplementCode: String,
)