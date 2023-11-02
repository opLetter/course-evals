package io.github.opletter.courseevals.fiu

import io.github.opletter.courseevals.common.data.substringAfterBefore
import kotlinx.serialization.Serializable
import org.jetbrains.kotlinx.dataframe.DataRow

@Serializable
data class Entry(
    val prof: String,
    val courseCode: String,
    val courseName: String,
    val term: String,
    val enrollment: Int,
    val responses: Int,
    val campusOrSchool: String,
    val college: String,
    val department: String,
    val ratings: List<List<Double>>, // backwards questions "Overall..." -> "Description" w/ % rating "Poor" -> "Excellent"
) {
    companion object {
        fun fromRows(rows: List<DataRow<Any?>>): Entry {
            val row = rows.first()
            return Entry(
                prof = row["Instructor Name-value"].toString().removeSurrounding("\""),
                courseCode = row["Course + Name-value"].toString()
                    .removeSurrounding("\"").substringBefore("\\r"),
                courseName = row["Course + Name-value"].toString()
                    .substringAfter("\\r\\n\\r\\n").dropLast(1),
                term = row["Term-value"].toString().removeSurrounding("\""),
                enrollment = row["Enrollment-value"].toString().toInt(),
                responses = row["SUM(Question Responses)-alias"].toString().toInt(),
                campusOrSchool = row["Campus or High School-value"].toString(),
                college = row["Course + Instructor Info-value"].toString().substringAfterBefore("\\r\\n", "\\r\\n"),
                department = row["Course + Instructor Info-value"].toString()
                    .substringAfter("\\r\\n").substringAfterBefore("\\r\\n", "\\r\\n"),
                ratings = rows.groupBy { it["Question-value"].toString() }.map { (_, qRows) ->
                    qRows.map { qRow ->
                        qRow["Measure Values-alias"].toString().drop(1).dropLast(2).toDouble()
                    }
                }
            )
        }
    }
}