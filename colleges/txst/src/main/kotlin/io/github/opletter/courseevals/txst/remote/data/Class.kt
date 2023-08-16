package io.github.opletter.courseevals.txst.remote.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClassesResponse(val classes: List<Class>)

@Serializable
data class Class(
    @SerialName("begin_date")
    val beginDate: Long,
    val title: String,
    val section: String,
    val number: String,
    val spi: Boolean,
    // skip syllabus
    val semester: Int,
    @SerialName("indexnum")
    val indexNum: Long,
)