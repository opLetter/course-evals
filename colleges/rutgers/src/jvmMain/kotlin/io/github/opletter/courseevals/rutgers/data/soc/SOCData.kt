package io.github.opletter.courseevals.rutgers.data.soc


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SOCData(
    val buildings: List<Building>,
    val coreCodes: List<CoreCodeSimple>,
    val currentTermDate: CurrentTermDate,
    val subjects: List<DescriptionHolder>,
    @SerialName("units")
    val schools: List<SOCSchool>,
)