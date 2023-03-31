package io.github.opletter.courseevals.usf

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Entry(
    val prof: String,
    val course: String,
    val term: String,
    val enrolled: String,
    val responded: String,
    val ratings: List<List<Int>>,
)

@Serializable
data class FullEntry(
    val deptInfo: String,
    val prof: String,
    val term: String,
    val courseTitle: String,
    val courseID: String,
    val enrolled: String,
    val responded: String,
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