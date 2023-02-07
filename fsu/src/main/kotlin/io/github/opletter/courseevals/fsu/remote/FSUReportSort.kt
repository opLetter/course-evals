package io.github.opletter.courseevals.fsu.remote

enum class FSUReportSort(private val value: String) {
    BEST_MATCH("BestMatch"),
    AVERAGE_LH("AvgLowHigh"),
    AVERAGE_HL("AvgHighLow"),
    RESPONDENTS_LH("RespondentsLowHigh"),
    RESPONDENTS_HL("RespondentsHighLow"),
    COURSE("Course"),
    INSTRUCTOR("Instructor"),
    TERM("Term");

    override fun toString(): String = value
}