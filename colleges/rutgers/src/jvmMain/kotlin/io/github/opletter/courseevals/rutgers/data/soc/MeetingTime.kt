package io.github.opletter.courseevals.rutgers.data.soc

import kotlinx.serialization.Serializable

@Serializable
data class MeetingTime(
    val baClassHours: String,
    val buildingCode: String,
    val campusAbbrev: String,
    val campusLocation: String,
    val campusName: String,
    val endTime: String,
    val endTimeMilitary: String,
    val meetingDay: String,
    val meetingModeCode: String,
    val meetingModeDesc: String,
    val pmCode: String,
    val roomNumber: String,
    val startTime: String,
    val startTimeMilitary: String,
)