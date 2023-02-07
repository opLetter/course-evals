package io.github.opletter.courseevals.fsu.remote

import kotlinx.serialization.Serializable

@Serializable
data class FSUResponse(
    val hasMore: Boolean,
    val results: List<String>,
) {
    override fun toString(): String {
        return "FSUResponse(hasMore=$hasMore, results.size=${results.size})"
    }
}
