package io.github.opletter.courseevals.rutgers.data.soc

import io.github.opletter.courseevals.common.data.Campus
import io.github.opletter.courseevals.common.data.LevelOfStudy
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class SOCSchool(
    @SerialName("campus")
    @Serializable(with = CampusesAsStringSerializer::class)
    val campuses: Set<Campus>, // seems to always be either one or all 3
    val code: String,
    val description: String,
    val homeCampus: Campus,
    val level: LevelOfStudy,
)

object CampusesAsStringSerializer : KSerializer<Set<Campus>> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("CampusesAsStringSerializer", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Set<Campus>) {
        encoder.encodeString(value.joinToString())
    }

    override fun deserialize(decoder: Decoder): Set<Campus> {
        return decoder.decodeString().split(", ").map { Campus.valueOf(it) }.toSet()
    }
}