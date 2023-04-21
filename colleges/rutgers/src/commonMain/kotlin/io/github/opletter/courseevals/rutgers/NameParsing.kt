package io.github.opletter.courseevals.rutgers

fun findMatchingName(originalName: String, existingNames: Collection<String>, code: String): String? {
    val name = originalName.run {
        // add comma if missing between first + last name
        if (" " in this && "," !in this) replace(" ", ", ")
        else this
    }.replace(".", "").let { manualNameAdjustment(it, code) }

    return existingNames.singleOrNull { it.startsWith(name) }
        ?: existingNames.singleOrNull { it == name.take(name.indexOf(",") + 3) } // for "Last, Initial"
        ?: existingNames.singleOrNull { it.substringBefore(",") == name.substringBefore(",") }
}
