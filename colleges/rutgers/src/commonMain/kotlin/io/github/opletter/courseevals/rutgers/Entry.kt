package io.github.opletter.courseevals.rutgers

import io.github.opletter.courseevals.common.data.Semester
import io.github.opletter.courseevals.common.data.SemesterType
import io.github.opletter.courseevals.common.data.substringAfterBefore
import kotlinx.serialization.Serializable

@Serializable
class Entry(
    val instructor: String,
    val term: String,
    val code: String,
    val courseName: String,
    val indexNum: String?,
    val note: String?,
    val enrolled: Int,
    val responses: Int,
    val scores: List<Double>,
    val questions: List<String>?, // null = standard - not using empty cuz could be actually empty
) {
    constructor(s: String) : this(
        instructor = s.substringBefore("  "),
        term = s.substringAfterBefore("<br> ", "\n"),
        code = s.substringAfterBefore("<br>  ", " "),
        courseName = s.substringAfterBefore("<q>", "<").replace("&amp;", "&"),
        //not always present - generally, but not always, corresponds to class name containing "(Lecture)"
        indexNum = s.substringAfter("index #", "").substringBefore(")").ifBlank { null },
        note = s.substringAfterBefore("<q>", "<br><a")
            .substringAfter("index #")
            .substringAfter("<br>")
            .substringAfter("(")
            .substringBefore(")", "").ifBlank { null },
        enrolled = s.substringAfterBefore("Enrollment=  ", ",").toInt(),
        responses = s.substringAfterBefore("Responses= ", " ").toInt(),
        // all the numbers for one entry, row by row
        scores = s.split("<td  class=\"mono").drop(1)
            .map { it.substringAfterBefore(">", "<").toDouble() },
        questions = s.split("<td  class='qText' >")
            .drop(1)
            .map {
                val question = it.substringAfterBefore(". ", "</td>")
                QsMap[question] ?: question
            }
    )

    val semester
        get() = with(term.split("  ")) {
            Semester.Double.valueOf(SemesterType.valueOf(first()), last().toInt())
        }

    val course get() = code.split(':')[2]

    override fun toString(): String {
        return "$instructor, $term, $code, $courseName, $indexNum, $note, $enrolled, $responses, size:${scores.size}, $questions"
    }
}