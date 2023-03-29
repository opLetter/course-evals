package io.github.opletter.courseevals.fsu

import io.github.opletter.courseevals.common.data.InstructorStats
import io.github.opletter.courseevals.common.data.School
import io.github.opletter.courseevals.common.data.SchoolDeptsMap
import io.github.opletter.courseevals.common.data.substringAfterBefore
import io.github.opletter.courseevals.common.remote.ktorClient
import io.github.opletter.courseevals.common.remote.makeFileAndDir
import io.ktor.client.call.*
import io.ktor.client.request.*
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.io.File

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

// https://registrar.fsu.edu/class_search/
fun ByteArray.getTeachingData(): List<TeachingData> {
    return PDDocument.load(this).use { doc ->
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
                .mapValues inner@{ (dept, entries) -> transform(campus, dept, entries) }
                .filterValues { it.isNotEmpty() }
        }
}

suspend fun getTeachingProfs(writeDir: String, term: String = "2023-9") {
    listOf("Undergraduate", "Graduate", "Law", "Medicine").flatMap { type ->
        ktorClient.get("https://registrar.fsu.edu/class_search/$term/$type.pdf")
            .body<ByteArray>()
            .getTeachingData()
    }.processTeachingDataByDept { campus, dept, entries ->
        filterTeachingInstructors(campus, dept, entries)
    }.writeToFiles(writeDir, false)
}

suspend fun getCourseNames(writeDir: String? = null) {
    val validDepts = File("jsonData/statsByProf/schools.json").decodeFromString<Map<String, School>>()
        .flatMap { it.value.depts }.toSet()
    listOf("1", "6", "9").flatMap { term ->
        listOf("Undergraduate", "Graduate", "Law", "Medicine").flatMap { type ->
            ktorClient.get("https://registrar.fsu.edu/class_search/2023-$term/$type.pdf")
                .body<ByteArray>()
                .getTeachingData()
        }
    }.also { data ->
        if (writeDir == null) return@also
        data.flatMap {
            listOf("${it.campus}: ${it.department}") + it.entries.map { entry ->
                if (entry.error.isNotBlank()) "BANANA: ${entry.error}"
                else entry.toString()
            }
        }.joinToString("\n").let {
            val file = makeFileAndDir("$writeDir.txt")
            file.writeText(it)
        }
    }.processTeachingDataByDept { _, dept, entries ->
        if (dept !in validDepts) {
            println("Invalid dept: $dept")
            return@processTeachingDataByDept emptyMap()
        }
        entries.associate { it.courseNumber.drop(3) to it.courseTitle }
    }.writeToFiles("jsonData/extraData/courseNames", false)
}

private fun filterTeachingInstructors(
    campus: String,
    dept: String,
    deptEntries: List<TeachingEntry>,
): Map<String, Set<String>> {
    val existingInstructors = runCatching {
        File("jsonData/statsByProf/$campus/$dept.json")
            .decodeFromString<Map<String, InstructorStats>>().keys
    }.getOrElse { println("no file $dept"); return emptyMap() }
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
            }.toSet()
        }.filterValues { it.isNotEmpty() }

    val profToCourses = coursesToProfs.flatMap { (course, profs) ->
        profs.map { it to course }
    }.groupBy({ it.first }, { it.second }).mapValues { it.value.toSet() }
    return coursesToProfs + profToCourses
}

private fun List<String>.extractPageData(): Pair<String, List<TeachingEntry>> {
    val locations = listOf(
        "Engineerng", "Cty", "Rep", "Asolo", "Sarasota", "Ft Pierce", "Orlando",
        "Pensacola", "Daytona", "Other", "Flornce", "London", "Valncia",
    )
    val locationsSpecial = listOf("Online", "SC")
    return run {
        val headerIndex = indexOf("Facility Id Person Name Location").takeIf { it != -1 }
            ?: indexOf("Person Name Location").takeIf { it != -1 }
            ?: indexOf("Location").takeIf { it != -1 && this[it - 1] == "Name" }
            ?: error("Could not find header")
        drop(headerIndex + 1)
    }.run {
        val splitIndex = this.indexOfFirst { part ->
            part.first().isDigit() && part.takeWhile { it != ' ' }.none { it.isLetter() }
        }
        val rollover = slice(0 until splitIndex)
            .joinToString(" ")
            .splitToSequence(" ")
            .takeWhile {
                it != "Regular" && it != "Academic" && it != "Session" && it != "RegularAcademic" && it != "Nursing"
            }.joinToString(" ")
        val entry = slice(splitIndex..lastIndex)
            .filter {
                it != "Panama" && it != "Sara" && it != "AM AM Sara" && it != "PM PM Sara" && it != "AM PM Sara"
            }.run {
                flatMapIndexed { index, line ->
                    val fixedLine = if (getOrNull(index + 1)?.startsWith("00") == true) line
                    else {
                        locations
                            .firstOrNull { line.trim().endsWith(it) }
                            ?.let { line.trim().replace(it, "Main") }
                            ?: locationsSpecial.firstOrNull { line.endsWith(it) }?.let { line.replace(it, "Main") }
                            ?: line
                    }
                    fixedLine.let {
                        if (it.endsWith("Main") && it != "Main")
                            listOf(it.substringBefore(" Main"), "Main")
                        else listOf(it)
                    }
                }
            }
        rollover to entry.getTeachingEntries()
    }
}

private fun List<String>.getTeachingEntries(): List<TeachingEntry> {
    fun List<String>.splitBy(predicate: (index: Int, element: String) -> Boolean): List<List<String>> =
        flatMapIndexed { index, element ->
            when {
                index == 0 || index == lastIndex -> listOf(index)
                predicate(index, element) -> listOf(index - 1, index + 1)
                else -> emptyList()
            }
        }.windowed(size = 2, step = 2) { (from, to) -> slice(from..to) }

    return splitBy { index, x ->
        val validStart = getOrNull(index + 1)?.firstOrNull()?.isDigit() == true
        validStart && (x.endsWith("Main") || x == "AM AM" || x == "PM PM" || x == "AM PM")
    }.mapNotNull { lineParts -> // filter out dates
        lineParts.filterNot { it.matches("^\\d{1,2}/\\d{1,2}/\\d{4} ".toRegex()) }.takeIf { it.isNotEmpty() }
    }.map { line ->
        when {
            line.size == 1 -> {
                val parts = line.single().split(" ")
                TeachingEntry(
                    courseNumber = parts[1],
                    courseTitle = parts.drop(2).takeWhile { !it.startsWith("00") }.joinToString(" "),
                    instructor = parts
                        .takeLastWhile { it != "-" && "_" !in it }
                        .joinToString("")
                        .replace("  ", " "),
                )
            }

            line.size < 5 -> TeachingEntry(error = line.joinToString(";"))
            else -> {
                val (courseNumber, courseTitle) = if (line[1].firstOrNull()?.isDigit() == true) {
                    val num = (line[0].substringAfter(" ").dropLast(1) + line[1])
                    val title = line.drop(2)
                        .joinToString(" ")
                        .splitToSequence(" ")
                        .takeWhile { !it.startsWith("00") }
                        .joinToString(" ")
                        .replace("  ", " ")
                    num to title
                } else {
                    val num = line[0].substringBefore(" ")
                    val title = line
                        .joinToString(" ")
                        .splitToSequence(" ")
                        .drop(1)
                        .takeWhile { it != "Regular" }
                        .joinToString(" ")
                    num to title
                }
                TeachingEntry(
                    courseNumber = courseNumber,
                    courseTitle = courseTitle,
                    instructor = line
                        .takeLastWhile { it != "AM" && it != "PM" }
                        .joinToString(" ")
                        .replace("  ", " ")
                        .split(" ")
                        .filter { "_" !in it && it != "-" }
                        .joinToString(" ")
                        .replace("Panama", "")
                        .replace("Main ", "") // seems to only matter for the last entry in pdf
                        .trim(),
                )
            }
        }
    }
}

fun mergeEntries(last: TeachingData, rolloverLines: String, entries: List<TeachingEntry>): TeachingData {
    val newLast = last.entries.last().run {
        val firstCol = rolloverLines.substringBefore(" ")
            .takeIf { it.firstOrNull()?.isDigit() == true }
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
                listOf(entry) + acc
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
                listOf(newEntry) + acc.drop(1)
            }
        }
    return last.copy(entries = newEntries)
}