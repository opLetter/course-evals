package io.github.opletter.courseevals.txst.remote.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Report<T : ResponseFormat>(
    @SerialName("class")
    val course: ReportClass,
    @SerialName("spimulti")
    val responses: List<T>,
)

sealed interface ResponseFormat

@Serializable
data class ReportClass(
    val semester: Int,
    val title: String,
    @SerialName("student_count")
    val studentCount: Int,
    val section: String,
    val number: String,
)

@Serializable
data class SurveyResponse(
    @SerialName("response_count")
    val responseCount: Int,
    val instructor: TXSTInstructor,
    val scores: ReportScores,
) : ResponseFormat

@Serializable
data class ReportScores(
    @SerialName("the-instructor-provided-opportunity-to-learn")
    val opportunityToLearn: ResponseCounts,
    @SerialName("the-instructor-communicated-effectively")
    val communication: ResponseCounts,
    @SerialName("the-instructor-conducted-class-as-scheduled")
    val hadClassAsScheduled: ResponseCounts,
    @SerialName("the-course-goals-were-made-clear")
    val clearGoals: ResponseCounts,
    @SerialName("the-course-was-organized-effectively")
    val organized: ResponseCounts,
)

@Serializable
data class ResponseCounts(
    val disagree: Int,
    val neutral: Int,
    @SerialName("strongly-disagree")
    val stronglyDisagree: Int,
    val agree: Int,
    @SerialName("strongly-agree")
    val stronglyAgree: Int,
)

@Serializable
data class SaveableResponse(
    val responseCount: Int,
    val instructor: TXSTInstructor,
    val scores: List<List<Int>>,
) : ResponseFormat

fun ResponseCounts.toList(): List<Int> = listOf(stronglyDisagree, disagree, neutral, agree, stronglyAgree)

fun ReportScores.toList(): List<List<Int>> = listOf(
    opportunityToLearn.toList(),
    communication.toList(),
    hadClassAsScheduled.toList(),
    clearGoals.toList(),
    organized.toList(),
)