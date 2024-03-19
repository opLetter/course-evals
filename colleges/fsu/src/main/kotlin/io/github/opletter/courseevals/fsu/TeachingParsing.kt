package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.common.data.*
import io.github.opletter.courseevals.common.decodeJson
import io.github.opletter.courseevals.common.remote.DefaultClient
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import org.apache.pdfbox.Loader
import org.apache.pdfbox.text.PDFTextStripper
import java.nio.file.Path

data class TeachingData(
    val campus: String,
    val department: String,
    val entries: List<TeachingEntry>,
)

data class TeachingEntry(
    val courseNumber: String = "",
    val courseTitle: String = "",
    val instructor: String = "",
    val error: String = "",
)

// https://registrar.fsu.edu/scheduling/class-search
fun ByteArray.parseTeachingData(): List<TeachingData> {
    return Loader.loadPDF(this).use { doc ->
        PDFTextStripper().getText(doc).split("Page \\d+".toRegex()).drop(1)
    }.fold(emptyList()) { acc, text ->
        val lines = text.lines()
        val (rolloverLines, entries) = lines.extractPageData()
        val headerRegex = "Campus: (?<campus>.+)Department: (?<dept>.+)".toRegex()
        lines.take(10).mapNotNull { headerRegex.find(it) }.singleOrNull()?.let { lineMatch ->
            acc + TeachingData(
                campus = lineMatch.groups["campus"]!!.value,
                department = lineMatch.groups["dept"]!!.value,
                entries = entries
            )
        } ?: (acc.dropLast(1) + mergeEntries(acc.last(), rolloverLines, entries))
    }
}

inline fun <T, V> List<TeachingData>.processTeachingDataByDept(
    transform: (campus: String, dept: String, entries: List<TeachingEntry>) -> Map<T, V>,
): SchoolDeptsMap<Map<T, V>> {
    return groupBy { it.campus }
        .mapKeys {
            when (it.key) {
                "Main" -> "Main"
                "Pnm Cty F" -> "Pnm"
                "Rep-Panama" -> "Intl"
                else -> "Intl".also { _ -> println("Unknown campus: ${it.key}") }
            }
        }.mapValues { (campus, entries) ->
            entries
                .flatMap { it.entries }
                .groupBy { it.courseNumber.take(3) }
                .mapValues { (dept, entries) -> transform(campus, dept, entries) }
        }
}

suspend fun getTeachingData(term: Semester.Triple): List<TeachingData> {
    return listOf("Undergraduate", "Graduate", "Law", "Medicine").pmap { type ->
        DefaultClient.get("https://registrar.fsu.edu/sites/g/files/upcbnu3886/files/documents/archive-class-search/${term.toFSUString()}$type.pdf")
            .takeIf { it.contentType() == ContentType.Application.Pdf }
            .also { if (it == null) println("No pdf for $term $type") }
            ?.body<ByteArray>()
            ?.parseTeachingData()
            .orEmpty()
    }.flatten()
}

suspend fun getTeachingProfs(statsByProfDir: Path, term: Semester.Triple): SchoolDeptsMap<Map<String, Set<String>>> {
    return getTeachingData(term).processTeachingDataByDept { campus, dept, entries ->
        filterTeachingInstructors(statsByProfDir, campus, dept, entries)
    }
}

private fun filterTeachingInstructors(
    statsByProfDir: Path,
    campus: String,
    dept: String,
    deptEntries: List<TeachingEntry>,
): Map<String, Set<String>> {
    val existingInstructors = runCatching {
        statsByProfDir.resolve(campus).resolve("$dept.json").decodeJson<Map<String, InstructorStats>>().keys
    }.getOrElse {
        println("no file $campus $dept")
        return emptyMap()
    }

    val coursesToProfs = deptEntries
        .groupBy { it.courseNumber.drop(3) }
        .mapValues { (_, entries) ->
            entries.map { it.instructor }.toSet() - ""
        }.filterValues { it.isNotEmpty() }
        .mapValues { (_, instructors) ->
            instructors.mapNotNull { instructor ->
                val (last, first) = instructor.uppercase().split(",")
                    .let { if (it.size == 1) it + "" else it }

                existingInstructors.singleOrNull { it == "$last, ${first.substringBefore(" ")}" }
                    ?: existingInstructors.singleOrNull { it == "${last.substringBefore(" ")}, ${first.substringBefore(" ")}" }
                    ?: existingInstructors.singleOrNull { it == "${last.substringBefore(" ")}, $first" }
                    ?: existingInstructors.singleOrNull { it == "$last, $first" }
                    ?: existingInstructors.singleOrNull { it.substringBefore(",") == last }
                    ?: existingInstructors.singleOrNull { it.substringBefore("-") == last }
                    ?: existingInstructors.singleOrNull { it.substringAfterBefore("-", ",") == last }
                    ?: existingInstructors.singleOrNull { it.substringBefore(",") == last.replace("Jr", "") }
            }.toSortedSet()
        }.filterValues { it.isNotEmpty() }

    val profToCourses = coursesToProfs.flatMap { (course, profs) ->
        profs.map { it to course }
    }.groupBy({ it.first }, { it.second }).mapValues { it.value.toSortedSet() }

    return coursesToProfs + profToCourses
}

private fun List<String>.extractPageData(): Pair<String, List<TeachingEntry>> {
    val locations = setOf(
        "Engineerng", "Cty", "Rep", "Asolo", "Sarasota", "Ft Pierce", "Orlando",
        "Pensacola", "Daytona", "Other", "Flornce", "London", "Valncia",
    )
    val locationsSpecial = setOf("Online", "SC")

    val mainLines = run {
        val headerIndex = indexOf("Facility Id Person Name Location").takeIf { it != -1 }
            ?: indexOf("Person Name Location").takeIf { it != -1 }
            ?: indexOf("Location").takeIf { it != -1 && this[it - 1] == "Name" }
            ?: error("Could not find header")
        drop(headerIndex + 1)
    }

    val splitIndex = mainLines.indexOfFirst { part ->
        (part.firstOrNull()?.isDigit() == true) && part.takeWhile { it != ' ' }.none { it.isLetter() }
    }.takeIf { it != -1 } ?: mainLines.lastIndex
    val rollover = mainLines
        .take(splitIndex)
        .flatSplitBySpace()
        .takeWhile {
            it != "Regular" && it != "Academic" && it != "Session" && it != "RegularAcademic" && it != "Nursing"
        }.joinToString(" ")
    val entryLines = mainLines.drop(splitIndex).filter {
        it != "Panama" && it != "Sara" && it != "AM AM Sara" && it != "PM PM Sara" && it != "AM PM Sara"
    }
    val entries = entryLines.flatMapIndexed { index, line ->
        val fixedLine = if (entryLines.getOrNull(index + 1)?.likelyClassSection() == true) {
            line
        } else {
            locations.firstOrNull { line.trim().endsWith(it) }?.let { line.trim().replace(it, "Main") }
                ?: locationsSpecial.firstOrNull { line.endsWith(it) }?.let { line.replace(it, "Main") }
                ?: line
        }
        if (fixedLine.endsWith("Main") && fixedLine != "Main") {
            listOf(fixedLine.substringBefore(" Main"), "Main")
        } else listOf(fixedLine)
    }.getTeachingEntries()

    return rollover to entries
}

private fun List<String>.getTeachingEntries(): List<TeachingEntry> {
    fun List<String>.splitBy(predicate: (index: Int, element: String) -> Boolean): List<List<String>> {
        return flatMapIndexed { index, element ->
            when {
                index == 0 || index == lastIndex -> listOf(index)
                predicate(index, element) -> listOf(index - 1, index + 1)
                else -> emptyList()
            }
        }.windowed(size = 2, step = 2) { (from, to) -> slice(from..to) }
    }

    fun List<String>.getMultilineTeachingEntry(): TeachingEntry {
        val instructor = takeLastWhile { it != "AM" && it != "PM" }
            .flatSplitBySpace()
            .filter { "_" !in it && it != "-" }
            .joinToString(" ") { it.trim() }
            .replace("Panama", "")
            .replace("Main ", "") // seems to only matter for the last entry in pdf

        return if (this[1].firstOrNull()?.isDigit() == true) {
            TeachingEntry(
                courseNumber = this[0].substringAfter(" ").dropLast(1) + this[1],
                courseTitle = this.drop(2)
                    .flatSplitBySpace()
                    .takeWhile { !it.likelyClassSection() }
                    .joinToString(" ") { it.trim() },
                instructor = instructor,
            )
        } else {
            TeachingEntry(
                courseNumber = this[0].substringBefore(" "),
                courseTitle = this
                    .flatSplitBySpace()
                    .drop(1)
                    .takeWhile { it != "Regular" }
                    .joinToString(" ") { it.trim() },
                instructor = instructor,
            )
        }
    }

    return splitBy { index, x ->
        val validStart = getOrNull(index + 1)?.firstOrNull()?.isDigit() == true
        validStart && (x.endsWith("Main") || x == "AM AM" || x == "PM PM" || x == "AM PM")
    }.mapNotNull { lineParts ->
        val line = lineParts
            .filterNot { it.matches("^\\d{1,2}/\\d{1,2}/\\d{4} ".toRegex()) } // filter out dates
            .takeIf { it.isNotEmpty() }
            ?: return@mapNotNull null

        when (line.size) {
            1 -> {
                val parts = line.single().split(" ")
                TeachingEntry(
                    courseNumber = parts[1],
                    courseTitle = parts.drop(2).takeWhile { !it.likelyClassSection() }.joinToString(" "),
                    instructor = parts
                        .takeLastWhile { it != "-" && "_" !in it }
                        .joinToString(" ") { it.trim() },
                )
            }

            2, 3, 4 -> TeachingEntry(error = line.joinToString(";"))
            else -> line.getMultilineTeachingEntry()
        }
    }
}

private fun mergeEntries(last: TeachingData, rolloverLines: String, entries: List<TeachingEntry>): TeachingData {
    val newLast = last.entries.last().run {
        val firstCol = rolloverLines.substringBefore(" ").takeIf { it.firstOrNull()?.isDigit() == true }
        val (number, rollover) = if (firstCol != null) {
            "$courseNumber$firstCol" to rolloverLines.substringAfter(" ", "")
        } else courseNumber to rolloverLines
        copy(
            courseNumber = number,
            courseTitle = ("$courseTitle $rollover").replace("  ", " ").trim(),
        )
    }
    val newEntries = (last.entries.dropLast(1) + newLast + entries)
        .foldRight(emptyList<TeachingEntry>()) { entry, acc ->
            if (
                entry.error.isNotBlank() || entry.courseNumber.length > 6
                || acc.isEmpty() || acc.first().courseNumber.length > 6
            ) {
                acc.prepend(entry)
            } else {
                val prev = acc.first()
                val (newNum, newTitle) = if (prev.error.isBlank()) {
                    prev.courseNumber to prev.courseTitle
                } else {
                    val num = prev.error.substringBefore(" ")
                    val title = prev.error.substringAfterBefore(" ", "Regular ;").trim()
                    num to title
                }
                val newEntry = entry.copy(
                    courseNumber = entry.courseNumber + newNum,
                    courseTitle = (entry.courseTitle + " " + newTitle).replace("  ", ""),
                )
                acc.drop(1).prepend(newEntry)
            }
        }
    return last.copy(entries = newEntries)
}

// assuming less than 200 section ("0001" to "0199")
private fun String.likelyClassSection() = startsWith("00") || startsWith("01")

private fun List<String>.flatSplitBySpace(): List<String> =
    flatMap { it.split(" ") }.filter(String::isNotEmpty)
