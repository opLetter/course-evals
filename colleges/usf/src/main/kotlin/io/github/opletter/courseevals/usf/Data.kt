package io.github.opletter.courseevals.usf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Report(
    /** Information about the course's categorization, generally in the form "Campus - College - Department". */
    val deptInfo: String,
    /** The course's instructor, formatted as "Last, First". */
    val prof: String,
    /** The course's term, formatted as "Spring 2020". */
    val term: String,
    /** The title of the course. */
    val courseTitle: String,
    /** The course ID, formatted as "XXX - #### - ### / CRN : ######". */
    val courseID: String,
    /** The number of students enrolled in the course. */
    val enrolled: String,
    /** The number of students who responded to the evaluation. */
    val responded: String,
    /** The results of the survey, as a list of # of responses 1-5 (# of 1s, # of 2s, ...). */
    val ratings: List<List<Int>>,
)

@Serializable
data class CourseData(
    @SerialName("CatalogID")
    val catalogID: String,
    @SerialName("CatalogName")
    val catalogName: String,
    @SerialName("Code")
    val code: String,
    @SerialName("CourseID")
    val courseID: String,
    @SerialName("CourseName")
    val courseName: String,
    @SerialName("CourseType")
    val courseType: String,
    @SerialName("DepartmentName")
    val departmentName: String,
    @SerialName("EntityID")
    val entityID: String,
    @SerialName("EntityName")
    val entityName: String,
    @SerialName("EntityType")
    val entityType: String,
    @SerialName("GradFlag")
    val gradFlag: String,
    @SerialName("Prefix")
    val prefix: String,
    @SerialName("SchoolOrCollegeName")
    val schoolOrCollegeName: String,
)