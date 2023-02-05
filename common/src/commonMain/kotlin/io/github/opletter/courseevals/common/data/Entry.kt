package io.github.opletter.courseevals.common.data

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
        scores = s.split("<td  class=\"mono").drop(1)
            .map {
                it.substringAfterBefore(">", "<").toDouble()
            },//indices 0-99 are all the numbers for one entry, row by row
        questions = s.split("<td  class='qText' >").map {
            it.substringAfterBefore(". ", "</td>")
        }.run {
            // this gets additional labels besides Strong Disagree/Strong Agree - see record%5D=597617 as ex.
//            s.split("<th class='chart hidden' colspan='5'>\n  \t<th class='text responseCol'>").map {
//                it.substringBefore("\n") to it.data.substringAfterBefore("</th>\n  \t<th  class='text responseCol'>","\n")
//            }
            val mappedQs = drop(1).filter { it != "" }.map { QsMap.getOrElse(it) { it } }
            if (mappedQs == QsMap.values.toList()) null else mappedQs
        }
    )

    val semester
        get() = with(term.split("  ")) { Semester(SemesterType.valueOf(first()), last().toInt()) }

    val course
        get() = code.split(':')[2]

    override fun toString(): String {
        return "$instructor, $term, $code, $courseName, $indexNum, $note, $enrolled, $responses, size:${scores.size}, $questions"
    }
}