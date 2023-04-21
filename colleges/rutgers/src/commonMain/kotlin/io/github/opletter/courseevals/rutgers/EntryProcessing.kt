package io.github.opletter.courseevals.rutgers

import io.github.opletter.courseevals.common.data.prepend

fun List<Entry>.filterValid(): List<Entry> {
    return filterNot { entry ->
        val prof = entry.instructor
        listOf("do not use", "error", "faculty", "proctortrack", "instructor")
            .any { it in prof.lowercase() }
                || prof in listOf("", ",", "-", "--", "TA", "(Recitation)")
                || prof.likelyMultipleProfs()
                || entry.scores.size < 100 // for now
    }
}

// extra stuffs
private val extraSpaceNameParts = setOf("MC", "O'")
private val forwardSpecialNameParts = extraSpaceNameParts + setOf("DER", "DE", "DA", "DEL", "LA", "UZ", "EL", "VAN")
private val backwardSpecialNameParts = forwardSpecialNameParts + setOf("II", "III", "IV", "COL")

private fun String.likelyMultipleProfs(): Boolean {
    val listOfAccepted = backwardSpecialNameParts + setOf(
        "", "ED.D.", "EZZAT", "DAGRACIA", "DESILVA", "SAI", "RUI", "GRACA", "MAI", "N", "FACHE"
    )
    val altered = replace(" \\(.*\\)|-".toRegex(), "") // replaces content in parentheses & removes dashes
        .replace("&quot;", "\"")
        .uppercase()
    val filtered = altered.split(" ", ",") - listOfAccepted
    return ";" in altered || filtered.size > 3
}

// This function attempts to figure out and fix differently formatted names that actually refer to the same prof
// Currently it does:
// 1. Matches a last-name only prof to one that has that last name also a first name, if present and unique
// ex. "SMITH" turns to "SMITH, JOHN" if there are no other "SMITH"s
// 2. Matches a last + first initial name with a last + full first name, if present and unique
// ex. "SMITH, J" turns to "SMITH, JOHN" if there are no other "SMITH, J..."s
// 3. Ignores dashes and apostrophes when matching, but includes those special chars in the final name if present
// ex. "OREILLYJAMES, JOHN" turns to "O'REILLY-JAMES, JOHN" if the latter is present
// 4. Matches first names (for common last) if one is a substring of the other
// ex. "SMITH, JOHN" turns to "SMITH, JOHNNY" if the latter is present (also Ron/Ronald, Ken/Kenneth, etc.)
// 5. Assumes if a last name consists of only one letter, it's actually the first name, and treats the supposed
// first name as the last name. Note that all other rules are applied to this new name.
// ex. "J, SMITH" is assumed to be "SMITH, J" and will turn to "SMITH, JOHN" if the latter is present
fun List<Entry>.autoNameAdjustments(): Map<String, String> {
    val names = map { it.formatFullName() }

    val specialChars = setOf('-', '\'')
    fun String.removeSpecialChars(): String = filterNot { it in specialChars }

    val specialCharMap = names.filter { name -> specialChars.any { it in name } }
        .associateBy { it.removeSpecialChars() }

    // #5
    val (flippedNames, properNames) = names
        .map { it.removeSpecialChars() }
        .partition { it.substringBefore(", ", "").length == 1 }

    fun String.flipName(): String = split(", ").reversed().joinToString(", ")
    val fixedNames = flippedNames.map { it.flipName() }

    val nameMappings = properNames.plus(fixedNames).toSet()
        .groupBy(
            keySelector = { it.substringBefore(",") },
            valueTransform = { it.substringAfter(", ", "") },
        ).flatMap { (lastName, commonLast) ->
            val byFirstInitial = commonLast
                .minus("") // we don't care about string that only had last name
                .groupBy { it[0] } // group by 1st initial - since some entries only have 1st initial

            byFirstInitial.flatMap { (initial, commonInitial) ->
                val fullFirsts = commonInitial
                    .minus(initial.toString()) // we don't care about with only initial for first name matching
                    .sortedBy { it.length } // sorted to check if shortest name matches longer & to use the longest name
                    .takeIf { it.isNotEmpty() } // when only initial is present -> need to differentiate from false
                // #4
                when (fullFirsts?.all { it.startsWith(fullFirsts[0]) }) {
                    true -> commonInitial.associateWith { fullFirsts.last() }
                    false -> emptyMap()
                    null -> mapOf(initial to initial) // so that let {} below adds last name to map
                }.map { "$lastName, ${it.key}" to "$lastName, ${it.value}" }
            }.let {
                // pair "Smith" with first name if only one exists
                if (byFirstInitial.size == 1 && it.isNotEmpty() && "" in commonLast)
                    it.plus(lastName to it[0].second)
                else it
            }
        }.flatMap { (originalKey, value) ->
            // Here we adjust the map to account for the flipped and special char containing names we've been ignoring
            // Name mappings should use the special char version if it exists,
            // and flipped names need to be mapped to the same thing that the un-flipped name maps to
            val newValue = specialCharMap[value] ?: value
            listOfNotNull(
                originalKey,
                originalKey.takeIf { it in fixedNames }?.flipName(),
            ).flatMap { key ->
                listOfNotNull(
                    key to newValue,
                    specialCharMap[key]?.let { it to newValue },
                )
            }
        }
    return specialCharMap + nameMappings // note that this order is important as keys from first are overwritten
}


fun Entry.formatFullName(): String {
    // un-separate the specials from other parts of the name - accounts for first/last names with spaces within them
    // first combine them forwards, then backwards
    fun List<String>.foldInSpecialNameParts(): List<String> {
        return fold(emptyList<String>()) { acc, s ->
            acc.lastOrNull()
                ?.takeIf { it.substringAfterLast(" ") in forwardSpecialNameParts }
                ?.let {
                    // fixes extra space added in some names like "MC CORMICK"
                    val charBetween = if (it.substringAfterLast(" ") in extraSpaceNameParts) "" else " "
                    acc.dropLast(1) + "$it$charBetween$s"
                } ?: (acc + s)
        }.foldRight(emptyList()) { s, acc ->
            acc.firstOrNull()
                ?.takeIf { it.substringBefore(" ") in backwardSpecialNameParts }
                ?.let { acc.drop(1).prepend("$s $it") }
                ?: acc.prepend(s)
        }
    }

    val name = instructor
        .trim()
        .replace(" \\(.*\\)|,|\\.".toRegex(), "") // remove stuff in parentheses + remove commas & periods
        .uppercase()
        .split(" ")
        .foldInSpecialNameParts()
        .take(2) // ignore everything after last + first
        .joinToString(", ")
    return manualNameAdjustment(name, code)
}